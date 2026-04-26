package com.example.creamoso;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        userInput = getIntent().getStringExtra("USER_INPUT");

        calculateTotalAndSummary();

        findViewById(R.id.btn_back_checkout).setOnClickListener(v -> finish());

        findViewById(R.id.btn_confirm_payment).setOnClickListener(v -> {
            // 1. Save to local SQLite (existing logic)
            if (dbHelper.placeOrder(userInput, totalAmount, itemsSummary.toString())) {
                
                // 2. ALSO send to Firebase (new logic)
                sendOrderToFirebase();

                Toast.makeText(this, "Order Placed! Please wait for admin verification.", Toast.LENGTH_LONG).show();
                dbHelper.clearCart(userInput);
                finish();
            } else {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOrderToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ordersRef = database.getReference("orders");

        // Generate a unique ID for the order
        String orderId = ordersRef.push().getKey();

        // Create the order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("user", userInput);
        orderData.put("total", totalAmount);
        orderData.put("items", itemsSummary.toString());
        orderData.put("status", "PENDING");
        orderData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

        // Push to Firebase
        if (orderId != null) {
            ordersRef.child(orderId).setValue(orderData);
        }
    }

    private void calculateTotalAndSummary() {
        if (userInput == null) return;
        Cursor cursor = dbHelper.getCartItems(userInput);
        totalAmount = 0.0;
        itemsSummary.setLength(0);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("product_price"));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                
                totalAmount += (price * qty);
                itemsSummary.append(qty).append("x ").append(name).append(", ");
            }
            cursor.close();
            
            // Remove trailing comma
            if (itemsSummary.length() > 2) {
                itemsSummary.setLength(itemsSummary.length() - 2);
            }
        }

        TextView tvTotal = findViewById(R.id.tv_total_to_pay);
        tvTotal.setText("Total Amount: P " + String.format("%.2f", totalAmount));
    }
}