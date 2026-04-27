package com.example.creamoso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminOrdersActivity extends AppCompatActivity {

    private LinearLayout container;
    private TextView tvNoOrders, tabActive, tabHistory;
    private DatabaseReference ordersRef, usersRef;
    private boolean showingHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        container = findViewById(R.id.container_admin_orders);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        tabActive = findViewById(R.id.tab_active_orders);
        tabHistory = findViewById(R.id.tab_order_history);
        
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        findViewById(R.id.btn_back_admin_orders).setOnClickListener(v -> finish());

        tabActive.setOnClickListener(v -> switchTab(false));
        tabHistory.setOnClickListener(v -> switchTab(true));

        loadOrdersFromFirebase();
    }

    private void switchTab(boolean history) {
        showingHistory = history;
        int activeColor = getResources().getColor(R.color.teal_background);
        int grayColor = getResources().getColor(R.color.gray_text);
        tabHistory.setTextColor(history ? activeColor : grayColor);
        tabActive.setTextColor(history ? grayColor : activeColor);
        loadOrdersFromFirebase();
    }

    private void loadOrdersFromFirebase() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                boolean hasData = false;
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        String status = orderSnapshot.child("status").getValue(String.class);
                        if (showingHistory) {
                            if (!"COMPLETED".equals(status)) continue;
                        } else {
                            if ("COMPLETED".equals(status)) continue;
                        }

                        hasData = true;
                        String firebaseKey = orderSnapshot.getKey();
                        String orderNum = orderSnapshot.child("orderNumber").getValue(String.class);
                        String user = orderSnapshot.child("user").getValue(String.class);
                        Double total = orderSnapshot.child("total").getValue(Double.class);
                        String items = orderSnapshot.child("items").getValue(String.class);
                        String receiptBase64 = orderSnapshot.child("paymentReceipt").getValue(String.class);
                        String payName = orderSnapshot.child("paymentName").getValue(String.class);
                        String payRef = orderSnapshot.child("paymentRef").getValue(String.class);

                        addOrderCard(firebaseKey, orderNum, user, total, status, items, receiptBase64, payName, payRef);
                    }
                }
                tvNoOrders.setVisibility(hasData ? View.GONE : View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addOrderCard(String key, String num, String user, Double total, String status, String items, String receipt, String pName, String pRef) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(12 * getResources().getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Order - " + (num != null ? num : "New") + " [" + status + "]");
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor("READY".equals(status) ? Color.BLUE : ("PENDING".equals(status) ? Color.RED : Color.GRAY));

        TextView tvDetails = new TextView(this);
        tvDetails.setText("Customer: " + user + "\nTotal: P" + total + "\nItems: " + items);
        tvDetails.setTextSize(12);

        layout.addView(tvTitle);
        layout.addView(tvDetails);

        if (pName != null) {
            TextView tvPay = new TextView(this);
            tvPay.setText("Paid by: " + pName + " (Ref: " + pRef + ")");
            tvPay.setTextSize(11);
            tvPay.setTextColor(Color.DKGRAY);
            layout.addView(tvPay);
        }

        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setPadding(0, 16, 0, 0);

        if (receipt != null && !receipt.isEmpty()) {
            com.google.android.material.button.MaterialButton btnView = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnView.setText("VIEW RECEIPT");
            btnView.setOnClickListener(v -> showReceiptDialog(receipt));
            btnLayout.addView(btnView);
        }

        if (!showingHistory) {
            com.google.android.material.button.MaterialButton btnAction = new com.google.android.material.button.MaterialButton(this);
            btnAction.setText("PENDING".equals(status) ? "MARK READY" : "MARK DONE");
            btnAction.setOnClickListener(v -> updateStatus(key, num, "PENDING".equals(status) ? "READY" : "COMPLETED"));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2); p.leftMargin = 16;
            btnAction.setLayoutParams(p);
            btnLayout.addView(btnAction);
        }

        layout.addView(btnLayout);
        card.addView(layout);
        container.addView(card);
    }

    private void showReceiptDialog(String base64) {
        try {
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(bitmap);
            iv.setAdjustViewBounds(true);
            new AlertDialog.Builder(this).setTitle("GCash Receipt").setView(iv).setPositiveButton("Close", null).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(String key, String num, String newStatus) {
        ordersRef.child(key).child("status").setValue(newStatus).addOnSuccessListener(aVoid -> 
            Toast.makeText(this, "Order " + num + " updated to " + newStatus, Toast.LENGTH_SHORT).show());
    }
}