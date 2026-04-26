package com.example.creamoso;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountActivity extends AppCompatActivity {

    private String userInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

        View root = findViewById(R.id.main_account_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsets;
            });
        }

        userInput = getIntent().getStringExtra("USER_INPUT");
        if (userInput != null) {
            TextView tvName = findViewById(R.id.tv_profile_name);
            tvName.setText(userInput);
        }

        setupClickListeners();
        setupNavigation();
    }

    private void setupClickListeners() {
        findViewById(R.id.item_my_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MyProfileActivity.class);
                intent.putExtra("USER_INPUT", userInput);
                startActivity(intent);
            }
        });

        findViewById(R.id.item_help_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, HelpCenterActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.item_updates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountActivity.this, "Checking for updates...", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_logout_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AccountActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(AccountActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                intent.putExtra("USER_INPUT", userInput);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        findViewById(R.id.nav_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ActivityActivity.class);
                intent.putExtra("USER_INPUT", userInput);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        findViewById(R.id.nav_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, CartActivity.class);
                intent.putExtra("USER_INPUT", userInput);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }
}