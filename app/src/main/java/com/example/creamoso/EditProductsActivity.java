package com.example.creamoso;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProductsActivity extends AppCompatActivity {

    private LinearLayout container;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Enable Edge-to-Edge to allow the app to draw behind system bars
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_products);

        container = findViewById(R.id.container_edit_products);
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        // 2. Adjust for responsive top (Status Bar) and bottom (Navigation Bar)
        View header = findViewById(R.id.header_edit_products);
        View root = findViewById(android.R.id.content);
        
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Push header down so it doesn't hide behind the time/battery (Status Bar)
            header.setPadding(header.getPaddingLeft(), insets.top, header.getPaddingRight(), header.getPaddingBottom());
            
            // Push bottom container up so it doesn't hide behind buttons/gestures (Navigation Bar)
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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductCard(Product product) {
        MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_edit_product, container, false);
        
        TextView tvName = card.findViewById(R.id.tv_item_name);
        TextView tvCategory = card.findViewById(R.id.tv_item_category);
        TextView tvPrice = card.findViewById(R.id.tv_item_price);
        ImageView btnEdit = card.findViewById(R.id.btn_edit_item);

        tvName.setText(product.getName());
        tvCategory.setText("Category: " + product.getCategory());
        tvPrice.setText(String.format("P%.2f", product.getPrice()));

        btnEdit.setOnClickListener(v -> showEditProductDialog(product));

        container.addView(card);
    }

    private void showAddProductDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = view.findViewById(R.id.et_product_name);
        EditText etCategory = view.findViewById(R.id.et_product_category);
        EditText etPrice = view.findViewById(R.id.et_product_price);

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String category = etCategory.getText().toString();
                    double price = Double.parseDouble(etPrice.getText().toString());
                    
                    String id = productsRef.push().getKey();
                    Product product = new Product(id, name, category, price, "");
                    if (id != null) productsRef.child(id).setValue(product);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null);
        EditText etName = view.findViewById(R.id.et_product_name);
        EditText etCategory = view.findViewById(R.id.et_product_category);
        EditText etPrice = view.findViewById(R.id.et_product_price);

        etName.setText(product.getName());
        etCategory.setText(product.getCategory());
        etPrice.setText(String.valueOf(product.getPrice()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    product.setName(etName.getText().toString());
                    product.setCategory(etCategory.getText().toString());
                    product.setPrice(Double.parseDouble(etPrice.getText().toString()));
                    productsRef.child(product.getId()).setValue(product);
                })
                .setNegativeButton("Delete", (dialog, which) -> productsRef.child(product.getId()).removeValue())
                .show();
    }
}