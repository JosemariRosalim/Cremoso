package com.example.creamoso;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class CartActivity extends AppCompatActivity {

    private String userInput;
    private DatabaseHelper dbHelper;
    private LinearLayout layoutEmpty, containerItems;
    private View layoutItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

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

            while (cursor.moveToNext()) {
                int cartId = cursor.getInt(cursor.getColumnIndexOrThrow("cart_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("product_price"));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

                addCartItemRow(cartId, name, price, qty, imageUrl);
            }
            cursor.close();
        } else {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutItems.setVisibility(View.GONE);
        }
    }

    private void addCartItemRow(int cartId, String name, double price, int qty, String imageUrl) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_cart_product, containerItems, false);
        
        ImageView ivProduct = rowView.findViewById(R.id.iv_cart_product_image);
        TextView tvName = rowView.findViewById(R.id.tv_cart_product_name);
        TextView tvDetails = rowView.findViewById(R.id.tv_cart_product_details);
        TextView tvPrice = rowView.findViewById(R.id.tv_cart_product_price);
        ImageView btnRemove = rowView.findViewById(R.id.btn_remove_from_cart);

        tvName.setText(name);
        tvDetails.setText("Quantity: " + qty);
        tvPrice.setText(String.format("P%.2f", (price * qty)));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(ivProduct);
        }

        btnRemove.setOnClickListener(v -> {
            // Add removal logic here if needed
            Toast.makeText(this, "Item removal not implemented yet", Toast.LENGTH_SHORT).show();
        });

        containerItems.addView(rowView);
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