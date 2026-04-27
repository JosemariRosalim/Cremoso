package com.example.creamoso;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class AdminActivity extends AppCompatActivity {

    private TextView tvSales, tvOrdersCount;
    private LinearLayout containerPending;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        tvSales = findViewById(R.id.tv_admin_sales);
        tvOrdersCount = findViewById(R.id.tv_admin_orders_count);
        containerPending = findViewById(R.id.container_pending_orders);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        View navBar = findViewById(R.id.admin_nav_bar);
        ViewCompat.setOnApplyWindowInsetsListener(navBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.bottomMargin = insets.bottom;
            v.setLayoutParams(mlp);
            return windowInsets;
        });

        View appBar = findViewById(R.id.appBarLayout2);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

        setupClickListeners();
        setupNavigation();
        listenForLiveUpdates();
    }

    private void listenForLiveUpdates() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                containerPending.removeAllViews();
                double totalSales = 0;
                int ordersCount = 0;
                int pendingShown = 0;

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    String status = orderSnapshot.child("status").getValue(String.class);
                    Double amount = orderSnapshot.child("total").getValue(Double.class);
                    String orderNum = orderSnapshot.child("orderNumber").getValue(String.class);
                    String firebaseKey = orderSnapshot.getKey();
                    
                    if (amount == null) amount = 0.0;

                    if (!"CANCELLED".equals(status)) {
                        totalSales += amount;
                        ordersCount++;
                    }

                    if ("PENDING".equals(status) && pendingShown < 5) {
                        String displayId = (orderNum != null) ? orderNum : (firebaseKey != null && firebaseKey.length() > 6 ? firebaseKey.substring(firebaseKey.length() - 6).toUpperCase() : firebaseKey);
                        addPendingOrderRow(displayId, amount);
                        pendingShown++;
                    }
                }

                tvSales.setText(String.format("P %.2f", totalSales));
                tvOrdersCount.setText(String.valueOf(ordersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addPendingOrderRow(String displayId, double total) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_pending_order_dashboard, containerPending, false);
        
        TextView tvTitle = row.findViewById(R.id.tv_pending_order_title);
        TextView tvPrice = row.findViewById(R.id.tv_pending_order_price);
        
        tvTitle.setText("Order - " + displayId);
        tvPrice.setText(String.format("P %.2f", total));

        row.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminOrdersActivity.class);
            startActivity(intent);
        });

        containerPending.addView(row);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_admin_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(AdminActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        findViewById(R.id.card_admin_menu).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, EditProductsActivity.class)));

        findViewById(R.id.card_admin_inventory).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, StockManagementActivity.class)));
            
        findViewById(R.id.tv_view_all_orders).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, AdminOrdersActivity.class)));
    }

    private void setupNavigation() {
        findViewById(R.id.admin_nav_dashboard).setOnClickListener(v -> {});
        findViewById(R.id.admin_nav_orders).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, AdminOrdersActivity.class)));
        findViewById(R.id.admin_nav_profile).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, ManagePaymentActivity.class)));
    }
}