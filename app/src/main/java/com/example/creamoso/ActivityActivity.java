package com.example.creamoso;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActivityActivity extends AppCompatActivity {

    private String userInput;
    private LinearLayout containerOrders, layoutEmpty;
    private TextView tabOngoing, tabHistory;
    private DatabaseReference ordersRef;
    private List<DataSnapshot> allOrders = new ArrayList<>();
    private boolean isShowingHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activity);

        View root = findViewById(R.id.main_activity_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        userInput = getIntent().getStringExtra("USER_INPUT");
        containerOrders = findViewById(R.id.container_orders);
        layoutEmpty = findViewById(R.id.layout_empty_activity);
        tabOngoing = findViewById(R.id.tab_ongoing);
        tabHistory = findViewById(R.id.tab_history);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        setupNavigation();
        setupTabs();
        loadOrders();
    }

    private void setupTabs() {
        tabOngoing.setOnClickListener(v -> {
            isShowingHistory = false;
            updateTabUI();
            filterAndDisplayOrders();
        });

        tabHistory.setOnClickListener(v -> {
            isShowingHistory = true;
            updateTabUI();
            filterAndDisplayOrders();
        });
    }

    private void updateTabUI() {
        if (!isShowingHistory) {
            tabOngoing.setTextColor(getResources().getColor(R.color.teal_background));
            tabOngoing.setTypeface(null, Typeface.BOLD);
            tabOngoing.setBackgroundResource(R.drawable.white_circle);
            tabOngoing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.teal_light)));

            tabHistory.setTextColor(Color.GRAY);
            tabHistory.setTypeface(null, Typeface.NORMAL);
            tabHistory.setBackground(null);
        } else {
            tabHistory.setTextColor(getResources().getColor(R.color.teal_background));
            tabHistory.setTypeface(null, Typeface.BOLD);
            tabHistory.setBackgroundResource(R.drawable.white_circle);
            tabHistory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.teal_light)));

            tabOngoing.setTextColor(Color.GRAY);
            tabOngoing.setTypeface(null, Typeface.NORMAL);
            tabOngoing.setBackground(null);
        }
    }

    private void loadOrders() {
        if (userInput == null) return;

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String orderUser = orderSnapshot.child("user").getValue(String.class);
                    if (userInput.equals(orderUser)) {
                        allOrders.add(orderSnapshot);
                    }
                }
                filterAndDisplayOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityActivity.this, "Error loading orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayOrders() {
        containerOrders.removeAllViews();
        boolean hasOrders = false;

        // Display newest first
        for (int i = allOrders.size() - 1; i >= 0; i--) {
            DataSnapshot orderSnapshot = allOrders.get(i);
            String status = orderSnapshot.child("status").getValue(String.class);
            
            boolean isCompleted = "COMPLETED".equals(status);
            
            if (isShowingHistory == isCompleted) {
                hasOrders = true;
                displayOrderCard(orderSnapshot);
            }
        }

        layoutEmpty.setVisibility(hasOrders ? View.GONE : View.VISIBLE);
        TextView tvEmpty = layoutEmpty.findViewById(android.R.id.text1); // You might need to add an ID to the textview inside layout_empty
        // (Self-correction: Using the existing structure from activity_activity.xml)
        if (!hasOrders) {
            TextView tvMsg = (TextView) layoutEmpty.getChildAt(0);
            tvMsg.setText(isShowingHistory ? "No order history yet." : "No ongoing orders yet.");
        }
    }

    private void displayOrderCard(DataSnapshot snapshot) {
        String id = snapshot.getKey();
        String status = snapshot.child("status").getValue(String.class);
        Double total = snapshot.child("total").getValue(Double.class);
        String date = snapshot.child("timestamp").getValue(String.class);
        String items = snapshot.child("items").getValue(String.class);

        MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_order_activity, containerOrders, false);
        
        TextView tvId = card.findViewById(R.id.tv_order_id);
        TextView tvStatus = card.findViewById(R.id.tv_order_status);
        TextView tvDetails = card.findViewById(R.id.tv_order_details);
        TextView tvTotal = card.findViewById(R.id.tv_order_total);
        TextView tvDate = card.findViewById(R.id.tv_order_date);

        String shortId = id.length() > 6 ? id.substring(id.length() - 6).toUpperCase() : id;
        tvId.setText("Order #" + shortId);
        tvStatus.setText(status);
        tvDetails.setText(items);
        tvTotal.setText(String.format("P%.2f", total != null ? total : 0.0));
        tvDate.setText(date);

        if ("PENDING".equals(status)) {
            tvStatus.setTextColor(Color.parseColor("#FF9800"));
        } else if ("COMPLETED".equals(status)) {
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        }

        card.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityActivity.this, OrderDetailsActivity.class);
            intent.putExtra("ORDER_ID", id);
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
        });

        containerOrders.addView(card);
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> navigateTo(HomeActivity.class));
        findViewById(R.id.nav_activity).setOnClickListener(v -> {});
        findViewById(R.id.nav_cart).setOnClickListener(v -> navigateTo(CartActivity.class));
        findViewById(R.id.nav_account).setOnClickListener(v -> navigateTo(AccountActivity.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(ActivityActivity.this, cls);
        intent.putExtra("USER_INPUT", userInput);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}