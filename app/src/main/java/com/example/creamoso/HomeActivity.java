package com.example.creamoso;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private String userInput;
    private LinearLayout layoutDynamicMenu;
    private DatabaseReference productsRef;
    private DatabaseHelper dbHelper;
    private List<Product> allProducts = new ArrayList<>();
    private final String[] categories = {"Float", "Yogurt Parfait", "Frozen Yogurt"};
    private boolean doubleBackToExitPressedOnce = false;

    private TextView tabAll, tabFloat, tabParfait, tabFrozenYogurt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        layoutDynamicMenu = findViewById(R.id.layout_dynamic_menu);
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        View root = findViewById(R.id.home_root_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, 0, insets.right, insets.bottom);
            return windowInsets;
        });

        userInput = getIntent().getStringExtra("USER_INPUT");
        setupNavigation();
        setupCategoryTabs();
        loadProductsFromFirebase();

        findViewById(R.id.nav_home).setOnClickListener(v -> {});
        findViewById(R.id.nav_activity).setOnClickListener(v -> navigateTo(ActivityActivity.class));
        findViewById(R.id.nav_cart).setOnClickListener(v -> navigateTo(CartActivity.class));
        findViewById(R.id.nav_account).setOnClickListener(v -> navigateTo(AccountActivity.class));
    }

    private void setupCategoryTabs() {
        tabAll = findViewById(R.id.home_category_all);
        tabFloat = findViewById(R.id.home_category_float);
        tabParfait = findViewById(R.id.home_category_parfait);
        tabFrozenYogurt = findViewById(R.id.home_category_frozen_yogurt);

        tabAll.setOnClickListener(v -> scrollToSection(null));
        tabFloat.setOnClickListener(v -> scrollToSection("Float"));
        tabParfait.setOnClickListener(v -> scrollToSection("Yogurt Parfait"));
        tabFrozenYogurt.setOnClickListener(v -> scrollToSection("Frozen Yogurt"));
    }

    private void scrollToSection(String category) {
        if (category == null) {
            findViewById(R.id.home_root_layout).scrollTo(0, 0);
        } else {
            View section = layoutDynamicMenu.findViewWithTag(category);
            if (section != null) {
                section.getParent().requestChildFocus(section, section);
            }
        }
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
                buildCategorizedMenu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void buildCategorizedMenu() {
        layoutDynamicMenu.removeAllViews();
        Map<String, List<Product>> categorizedMap = new HashMap<>();
        for (String cat : categories) categorizedMap.put(cat, new ArrayList<>());

        for (Product p : allProducts) {
            if (categorizedMap.containsKey(p.getCategory())) {
                categorizedMap.get(p.getCategory()).add(p);
            }
        }

        for (String cat : categories) {
            List<Product> products = categorizedMap.get(cat);
            if (products != null && !products.isEmpty()) {
                addCategorySection(cat, products);
            }
        }
    }

    private void addCategorySection(String categoryName, List<Product> products) {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.layout_menu_section, layoutDynamicMenu, false);
        sectionView.setTag(categoryName);
        
        TextView tvTitle = sectionView.findViewById(R.id.tv_section_title);
        tvTitle.setText(categoryName.toUpperCase());
        
        GridLayout grid = sectionView.findViewById(R.id.grid_section_products);
        for (Product product : products) {
            displayProductInGrid(grid, product);
        }

        layoutDynamicMenu.addView(sectionView);
    }

    private void displayProductInGrid(GridLayout grid, Product product) {
        MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_product_grid, grid, false);
        
        TextView tvName = card.findViewById(R.id.tv_product_name);
        TextView tvPrice = card.findViewById(R.id.tv_product_price);
        TextView tvStock = card.findViewById(R.id.tv_product_stock);
        ImageView ivProduct = card.findViewById(R.id.iv_product_image);
        View overlay = card.findViewById(R.id.view_out_of_stock_overlay);
        TextView label = card.findViewById(R.id.tv_out_of_stock_label);
        
        tvName.setText(product.getName());
        tvPrice.setText(String.format("P%.2f", product.getPrice()));
        
        // Update Stock Display
        if (product.isAvailable() && product.getStockCount() > 0) {
            tvStock.setVisibility(View.VISIBLE);
            tvStock.setText(product.getStockCount() + " left");
            overlay.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
            card.setEnabled(true);
        } else {
            tvStock.setVisibility(View.GONE);
            overlay.setVisibility(View.VISIBLE);
            label.setVisibility(View.VISIBLE);
            card.setEnabled(false);
        }
        
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this).load(product.getImageUrl()).into(ivProduct);
        }

        card.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            intent.putExtra("PRODUCT_NAME", product.getName());
            intent.putExtra("PRODUCT_PRICE", String.valueOf(product.getPrice()));
            intent.putExtra("PRODUCT_CATEGORY", product.getCategory());
            intent.putExtra("PRODUCT_IMAGE", product.getImageUrl());
            intent.putExtra("USER_INPUT", userInput);
            startActivity(intent);
        });

        grid.addView(card);
    }

    private void setupNavigation() {
        // Already handled in onCreate
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(HomeActivity.this, cls);
        intent.putExtra("USER_INPUT", userInput);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}