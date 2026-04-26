package com.example.creamoso;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

public class HomeActivity extends AppCompatActivity {

    private String userInput;
    private GridLayout gridProducts;
    private DatabaseReference productsRef;
    private DatabaseHelper dbHelper;
    private List<Product> allProducts = new ArrayList<>();
    private String currentCategory = "ALL";
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        gridProducts = findViewById(R.id.grid_products);
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        View root = findViewById(R.id.home_root_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, 0, insets.right, insets.bottom);
            return windowInsets;
        });

        userInput = getIntent().getStringExtra("USER_INPUT");
        updateGreeting();
        setupNavigation();
        setupCategoryTabs();
        loadProductsFromFirebase();

        // Handle Back Press to prevent accidental exit
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity(); // Closes the whole app
                    return;
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(HomeActivity.this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
    }

    private void updateGreeting() {
        if (userInput != null) {
            TextView tvUser = findViewById(R.id.tv_home_user_name);
            Cursor cursor = dbHelper.getUserData(userInput);
            if (cursor != null && cursor.moveToFirst()) {
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
                tvUser.setText("Hi, " + (firstName != null && !firstName.isEmpty() ? firstName : userInput));
                cursor.close();
            } else {
                tvUser.setText("Hi, " + userInput);
            }
        }
    }

    private void setupCategoryTabs() {
        TextView tabDeals = findViewById(R.id.home_category_deals);
        TextView tabSoftServe = findViewById(R.id.home_category_soft_serve);
        TextView tabCoffee = findViewById(R.id.home_category_coffee);

        tabDeals.setOnClickListener(v -> filterByCategory("ALL", tabDeals));
        tabSoftServe.setOnClickListener(v -> filterByCategory("Soft Serve", tabSoftServe));
        tabCoffee.setOnClickListener(v -> filterByCategory("Iced Coffee", tabCoffee));
    }

    private void filterByCategory(String category, TextView activeTab) {
        currentCategory = category;
        ((TextView)findViewById(R.id.home_category_deals)).setTextColor(Color.GRAY);
        ((TextView)findViewById(R.id.home_category_soft_serve)).setTextColor(Color.GRAY);
        ((TextView)findViewById(R.id.home_category_coffee)).setTextColor(Color.GRAY);
        
        activeTab.setTextColor(getResources().getColor(R.color.teal_background));
        activeTab.setTypeface(null, android.graphics.Typeface.BOLD);

        updateGrid();
    }

    private void loadProductsFromFirebase() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) allProducts.add(product);
                }
                updateGrid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGrid() {
        gridProducts.removeAllViews();
        for (Product product : allProducts) {
            if (currentCategory.equals("ALL") || product.getCategory().equalsIgnoreCase(currentCategory)) {
                displayProduct(product);
            }
        }
    }

    private void displayProduct(Product product) {
        MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_product_grid, gridProducts, false);
        TextView tvName = card.findViewById(R.id.tv_product_name);
        TextView tvPrice = card.findViewById(R.id.tv_product_price);
        
        tvName.setText(product.getName());
        tvPrice.setText(String.format("P%.2f", product.getPrice()));

        card.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_NAME", product.getName());
            intent.putExtra("PRODUCT_PRICE", String.valueOf(product.getPrice()));
            intent.putExtra("PRODUCT_CATEGORY", product.getCategory());
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
        });

        gridProducts.addView(card);
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {});
        findViewById(R.id.nav_activity).setOnClickListener(v -> navigateTo(ActivityActivity.class));
        findViewById(R.id.nav_cart).setOnClickListener(v -> navigateTo(CartActivity.class));
        findViewById(R.id.nav_account).setOnClickListener(v -> navigateTo(AccountActivity.class));
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(HomeActivity.this, cls);
        intent.putExtra("USER_INPUT", userInput);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}