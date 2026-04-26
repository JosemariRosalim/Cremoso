package com.example.creamoso;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        // Fix for 3-button navigation: Push the bar UP using margins instead of padding
        View navBar = findViewById(R.id.admin_nav_bar);
        ViewCompat.setOnApplyWindowInsetsListener(navBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.bottomMargin = insets.bottom;
            v.setLayoutParams(mlp);
            return windowInsets;
        });

        // Responsive top adjustment
        View appBar = findViewById(R.id.appBarLayout2);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

        setupClickListeners();
        setupNavigation();
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
            Toast.makeText(AdminActivity.this, "Opening Inventory Levels...", Toast.LENGTH_SHORT).show());
    }

    private void setupNavigation() {
        findViewById(R.id.admin_nav_dashboard).setOnClickListener(v -> {});
        findViewById(R.id.admin_nav_orders).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, AdminOrdersActivity.class)));
        findViewById(R.id.admin_nav_profile).setOnClickListener(v -> 
            startActivity(new Intent(AdminActivity.this, ManagePaymentActivity.class)));
    }
}