package com.example.creamoso;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminOrdersActivity extends AppCompatActivity {

    private LinearLayout container;
    private TextView tvNoOrders;
    private DatabaseReference ordersRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        container = findViewById(R.id.container_admin_orders);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        findViewById(R.id.btn_back_admin_orders).setOnClickListener(v -> finish());

        loadOrdersFromFirebase();
    }

    private void loadOrdersFromFirebase() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    tvNoOrders.setVisibility(View.GONE);
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        String id = orderSnapshot.getKey();
                        String user = orderSnapshot.child("user").getValue(String.class);
                        Double total = orderSnapshot.child("total").getValue(Double.class);
                        String status = orderSnapshot.child("status").getValue(String.class);
                        String date = orderSnapshot.child("timestamp").getValue(String.class);
                        String items = orderSnapshot.child("items").getValue(String.class);

                        if (total == null) total = 0.0;
                        addOrderCard(id, user, total, status, date, items);
                    }
                } else {
                    tvNoOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addOrderCard(String id, String user, double total, String status, String date, String items) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(12 * getResources().getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView tvTitle = new TextView(this);
        String shortId = id.length() > 6 ? id.substring(id.length() - 6).toUpperCase() : id;
        tvTitle.setText("Order #" + shortId + " - " + status);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(status.equals("READY") ? Color.BLUE : (status.equals("PENDING") ? Color.RED : Color.GRAY));

        TextView tvUser = new TextView(this);
        tvUser.setText("Customer: " + user + "\nDate: " + date);
        tvUser.setTextSize(12);

        layout.addView(tvTitle);
        layout.addView(tvUser);

        // ACTION BUTTONS
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 16, 0, 0);

        if ("PENDING".equals(status)) {
            com.google.android.material.button.MaterialButton btnReady = new com.google.android.material.button.MaterialButton(this);
            btnReady.setText("MARK AS READY");
            btnReady.setOnClickListener(v -> updateStatus(id, user, shortId, "READY"));
            btnLayout.addView(btnReady);
        } else if ("READY".equals(status)) {
            com.google.android.material.button.MaterialButton btnDone = new com.google.android.material.button.MaterialButton(this);
            btnDone.setText("MARK AS COMPLETED");
            btnDone.setOnClickListener(v -> updateStatus(id, user, shortId, "COMPLETED"));
            btnLayout.addView(btnDone);
        }

        layout.addView(btnLayout);
        card.addView(layout);
        container.addView(card);
    }

    private void updateStatus(String orderId, String userEmail, String shortId, String newStatus) {
        ordersRef.child(orderId).child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> {
                if ("READY".equals(newStatus)) {
                    notifyCustomer(userEmail, "Your order #" + shortId + " is READY! Please pick it up at the counter. ☕");
                }
                Toast.makeText(this, "Order is now " + newStatus, Toast.LENGTH_SHORT).show();
            });
    }

    private void notifyCustomer(String userEmail, String message) {
        if (userEmail == null) return;
        String safeKey = userEmail.replace(".", ",");
        usersRef.child(safeKey).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.getValue(String.class);
                if (token != null) {
                    sendNotificationSignal(token, message);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendNotificationSignal(String token, String message) {
        DatabaseReference notifyRef = FirebaseDatabase.getInstance().getReference("notification_requests");
        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("token", token);
        notification.put("title", "Order Ready! ☕");
        notification.put("body", message);
        notification.put("timestamp", System.currentTimeMillis());
        notifyRef.push().setValue(notification);
    }
}