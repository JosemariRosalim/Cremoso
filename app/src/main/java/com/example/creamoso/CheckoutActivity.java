package com.example.creamoso;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private String userInput;
    private DatabaseHelper dbHelper;
    private double totalAmount = 0.0;
    private StringBuilder itemsSummary = new StringBuilder();
    private ImageView ivGcashQr, ivReceiptPreview;
    private EditText etGcashName, etGcashRef;
    private String base64Receipt = "";
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        userInput = getIntent().getStringExtra("USER_INPUT");
        
        ivGcashQr = findViewById(R.id.iv_gcash_qr);
        ivReceiptPreview = findViewById(R.id.iv_receipt_preview);
        etGcashName = findViewById(R.id.et_gcash_name);
        etGcashRef = findViewById(R.id.et_gcash_ref);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivReceiptPreview.setImageURI(uri);
                        processImage(uri);
                    }
                }
        );

        calculateTotalAndSummary();
        loadGcashQr();

        findViewById(R.id.btn_back_checkout).setOnClickListener(v -> finish());
        findViewById(R.id.btn_select_receipt).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        findViewById(R.id.btn_confirm_payment).setOnClickListener(v -> {
            String gcashName = etGcashName.getText().toString().trim();
            String gcashRef = etGcashRef.getText().toString().trim();

            if (base64Receipt.isEmpty()) {
                Toast.makeText(this, "Please select a screenshot of your receipt", Toast.LENGTH_SHORT).show();
                return;
            }

            if (gcashName.isEmpty() || gcashRef.isEmpty()) {
                Toast.makeText(this, "Please enter payment details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.placeOrder(userInput, totalAmount, itemsSummary.toString())) {
                sendOrderToFirebase(gcashName, gcashRef);
                Toast.makeText(this, "Order Placed! Please wait for admin verification.", Toast.LENGTH_LONG).show();
                dbHelper.clearCart(userInput);
                finish();
            } else {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            
            // Compress image for Base64 storage
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] bytes = baos.toByteArray();
            base64Receipt = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGcashQr() {
        DatabaseReference qrRef = FirebaseDatabase.getInstance().getReference("settings").child("gcash_qr");
        qrRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFinishing() && !isDestroyed()) {
                    String url = snapshot.getValue(String.class);
                    if (url != null && !url.isEmpty() && ivGcashQr != null) {
                        Glide.with(CheckoutActivity.this).load(url).into(ivGcashQr);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendOrderToFirebase(String gcashName, String gcashRef) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount() + 1;
                String orderNumber = String.format(Locale.getDefault(), "%03d", count);
                String orderId = ordersRef.push().getKey();

                Map<String, Object> orderData = new HashMap<>();
                orderData.put("orderId", orderId);
                orderData.put("orderNumber", orderNumber);
                orderData.put("user", userInput);
                orderData.put("total", totalAmount);
                orderData.put("items", itemsSummary.toString());
                orderData.put("status", "PENDING");
                orderData.put("paymentName", gcashName);
                orderData.put("paymentRef", gcashRef);
                orderData.put("paymentReceipt", base64Receipt); // Save screenshot as string
                orderData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

                if (orderId != null) ordersRef.child(orderId).setValue(orderData);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void calculateTotalAndSummary() {
        Cursor cursor = dbHelper.getCartItems(userInput);
        if (cursor != null) {
            totalAmount = 0; itemsSummary.setLength(0);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("product_price"));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                totalAmount += (price * qty);
                itemsSummary.append(qty).append("x ").append(name).append(", ");
            }
            cursor.close();
            if (itemsSummary.length() > 2) itemsSummary.setLength(itemsSummary.length() - 2);
        }
        TextView tvTotal = findViewById(R.id.tv_total_to_pay);
        if (tvTotal != null) tvTotal.setText("Amount to Pay: P " + String.format(Locale.getDefault(), "%.2f", totalAmount));
    }
}