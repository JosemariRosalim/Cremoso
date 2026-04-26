package com.example.creamoso;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CartActivity extends AppCompatActivity {

    private String userInput;
    private DatabaseHelper dbHelper;
    private LinearLayout layoutEmpty, containerItems;
    private View layoutItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Enable Edge-to-Edge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // 2. Responsive adjustment for white border navigation
        View root = findViewById(R.id.main_cart_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        userInput = getIntent().getStringExtra("USER_INPUT");

        layoutEmpty = findViewById(R.id.layout_empty_cart);
        layoutItems = findViewById(R.id.layout_cart_items);
        containerItems = findViewById(R.id.container_items);

        loadCartItems();
        setupNavigation();
        
        findViewById(R.id.btn_back_cart).setOnClickListener(v -> finish());

        findViewById(R.id.tv_go_to_menu).setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            intent.putExtra("USER_INPUT", userInput);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_checkout).setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
        });
    }

    private void loadCartItems() {
        if (userInput == null) return;

        containerItems.removeAllViews();
        Cursor cursor = dbHelper.getCartItems(userInput);

        if (cursor != null && cursor.getCount() > 0) {
            layoutEmpty.setVisibility(View.GONE);
            layoutItems.setVisibility(View.VISIBLE);

            // Add Header
            addCartHeader();

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("product_price"));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

                addCartItemRow(name, price, qty);
            }
            cursor.close();
        } else {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutItems.setVisibility(View.GONE);
        }
    }

    private void addCartHeader() {
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 16, 0, 16);

        TextView tvQty = createColumnTextView("Qty", 1, true);
        TextView tvItem = createColumnTextView("Item", 3, true);
        TextView tvPrice = createColumnTextView("Price", 2, true);

        headerLayout.addView(tvQty);
        headerLayout.addView(tvItem);
        headerLayout.addView(tvPrice);

        containerItems.addView(headerLayout);

        // Add a simple separator line
        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        separator.setBackgroundColor(Color.LTGRAY);
        containerItems.addView(separator);
    }

    private void addCartItemRow(String name, double price, int qty) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 24, 0, 24);

        TextView tvQty = createColumnTextView(String.valueOf(qty), 1, false);
        TextView tvItem = createColumnTextView(name, 3, false);
        TextView tvPrice = createColumnTextView("P" + String.format("%.2f", (price * qty)), 2, false);
        tvPrice.setGravity(Gravity.END);

        itemLayout.addView(tvQty);
        itemLayout.addView(tvItem);
        itemLayout.addView(tvPrice);

        containerItems.addView(itemLayout);
    }

    private TextView createColumnTextView(String text, int weight, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        textView.setText(text);
        textView.setTextSize(isHeader ? 14 : 16);
        textView.setTextColor(Color.BLACK);
        if (isHeader) {
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(Color.GRAY);
        }
        return textView;
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_activity).setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ActivityActivity.class);
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_account).setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, AccountActivity.class);
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
            finish();
        });
    }
}