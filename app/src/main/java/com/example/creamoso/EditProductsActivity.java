package com.example.creamoso;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditProductsActivity extends AppCompatActivity {

    private LinearLayout container;
    private DatabaseReference productsRef;
    private final String[] categories = {"Float", "Yogurt Parfait", "Frozen Yogurt"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_products);

        container = findViewById(R.id.container_edit_products);
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(), insets.bottom + 16);
            return windowInsets;
        });

        findViewById(R.id.btn_back_edit_products).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_product).setOnClickListener(v -> showAddProductDialog());

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
                        addProductCard(product);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addProductCard(Product product) {
        MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_edit_product, container, false);
        
        TextView tvName = card.findViewById(R.id.tv_item_name);
        TextView tvCategory = card.findViewById(R.id.tv_item_category);
        TextView tvPrice = card.findViewById(R.id.tv_item_price);
        ImageView btnEdit = card.findViewById(R.id.btn_edit_item);
        ImageView ivProduct = card.findViewById(R.id.iv_item_image);

        tvName.setText(product.getName());
        tvCategory.setText("Category: " + product.getCategory());
        tvPrice.setText(String.format("P%.2f", product.getPrice()));
        
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this).load(product.getImageUrl()).into(ivProduct);
        }

        btnEdit.setOnClickListener(v -> showEditProductDialog(product));
        container.addView(card);
    }

    private void showAddProductDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = view.findViewById(R.id.et_product_name);
        EditText etPrice = view.findViewById(R.id.et_product_base_price);
        EditText etUrl = view.findViewById(R.id.et_product_image_url);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        LinearLayout containerAddons = view.findViewById(R.id.container_addons);
        MaterialButton btnAddAddon = view.findViewById(R.id.btn_add_addon);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnAddAddon.setOnClickListener(v -> addAddonRow(containerAddons, null));

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String imageUrl = etUrl.getText().toString().trim();
                    
                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = productsRef.push().getKey();
                    Product product = new Product(id, name, spinnerCategory.getSelectedItem().toString(), Double.parseDouble(priceStr), imageUrl);
                    product.setAddOns(getAddOnsFromContainer(containerAddons));
                    if (id != null) productsRef.child(id).setValue(product);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = view.findViewById(R.id.et_product_name);
        EditText etPrice = view.findViewById(R.id.et_product_base_price);
        EditText etUrl = view.findViewById(R.id.et_product_image_url);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        LinearLayout containerAddons = view.findViewById(R.id.container_addons);
        MaterialButton btnAddAddon = view.findViewById(R.id.btn_add_addon);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        etName.setText(product.getName());
        etPrice.setText(String.valueOf(product.getPrice()));
        etUrl.setText(product.getImageUrl());
        
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(product.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        if (product.getAddOns() != null) {
            for (Product.AddOn addon : product.getAddOns()) addAddonRow(containerAddons, addon);
        }

        btnAddAddon.setOnClickListener(v -> addAddonRow(containerAddons, null));

        new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    product.setName(etName.getText().toString().trim());
                    product.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
                    product.setImageUrl(etUrl.getText().toString().trim());
                    product.setCategory(spinnerCategory.getSelectedItem().toString());
                    product.setAddOns(getAddOnsFromContainer(containerAddons));
                    productsRef.child(product.getId()).setValue(product);
                })
                .setNegativeButton("Delete", (dialog, which) -> productsRef.child(product.getId()).removeValue())
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void addAddonRow(LinearLayout container, Product.AddOn addon) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_addon_row, container, false);
        EditText etN = row.findViewById(R.id.et_addon_name);
        EditText etP = row.findViewById(R.id.et_addon_price);
        if (addon != null) {
            etN.setText(addon.getName());
            etP.setText(String.valueOf(addon.getPrice()));
        }
        row.findViewById(R.id.btn_remove_addon).setOnClickListener(v -> container.removeView(row));
        container.addView(row);
    }

    private List<Product.AddOn> getAddOnsFromContainer(LinearLayout container) {
        List<Product.AddOn> list = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View row = container.getChildAt(i);
            String n = ((EditText)row.findViewById(R.id.et_addon_name)).getText().toString().trim();
            String p = ((EditText)row.findViewById(R.id.et_addon_price)).getText().toString().trim();
            if (!n.isEmpty() && !p.isEmpty()) list.add(new Product.AddOn(n, Double.parseDouble(p)));
        }
        return list;
    }
}