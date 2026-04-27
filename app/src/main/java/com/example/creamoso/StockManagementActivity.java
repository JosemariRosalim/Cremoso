package com.example.creamoso;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StockManagementActivity extends AppCompatActivity {

    private LinearLayout container;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock_management);

        container = findViewById(R.id.container_stock_items);
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_stock), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

        findViewById(R.id.btn_back_stock).setOnClickListener(v -> finish());

        loadProducts();
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        addStockCard(product);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addStockCard(Product product) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_stock_product, container, false);
        
        TextView tvName = card.findViewById(R.id.tv_stock_product_name);
        TextView tvCategory = card.findViewById(R.id.tv_stock_category);
        EditText etCount = card.findViewById(R.id.et_stock_count);
        MaterialSwitch switchAvailable = card.findViewById(R.id.switch_stock_available);
        LinearLayout addonContainer = card.findViewById(R.id.container_stock_addons);

        tvName.setText(product.getName());
        tvCategory.setText(product.getCategory());
        etCount.setText(String.valueOf(product.getStockCount()));
        switchAvailable.setChecked(product.isAvailable());

        // PRODUCT AVAILABILITY TOGGLE
        switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            productsRef.child(product.getId()).child("available").setValue(isChecked);
        });

        // STOCK COUNTER LOGIC
        card.findViewById(R.id.btn_stock_plus).setOnClickListener(v -> {
            int current = Integer.parseInt(etCount.getText().toString());
            productsRef.child(product.getId()).child("stockCount").setValue(current + 1);
        });

        card.findViewById(R.id.btn_stock_minus).setOnClickListener(v -> {
            int current = Integer.parseInt(etCount.getText().toString());
            if (current > 0) {
                productsRef.child(product.getId()).child("stockCount").setValue(current - 1);
            }
        });

        // MANAGE ADD-ONS
        if (product.getAddOns() != null) {
            for (int i = 0; i < product.getAddOns().size(); i++) {
                Product.AddOn addon = product.getAddOns().get(i);
                addAddonToggle(addonContainer, product.getId(), i, addon);
            }
        }

        container.addView(card);
    }

    private void addAddonToggle(LinearLayout container, String productId, int index, Product.AddOn addon) {
        MaterialSwitch sw = new MaterialSwitch(this);
        sw.setText(addon.getName());
        sw.setChecked(addon.isAvailable());
        sw.setTextSize(13); // Fixed: Removed 'sp' unit which is not valid in Java code
        sw.setPadding(0, 8, 0, 8);
        
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            productsRef.child(productId).child("addOns").child(String.valueOf(index)).child("available").setValue(isChecked);
        });

        container.addView(sw);
    }
}