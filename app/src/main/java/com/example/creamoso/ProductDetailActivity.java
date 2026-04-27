package com.example.creamoso;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

    private int quantity = 1;
    private double basePrice = 0.0;
    private double totalPrice = 0.0;
    private DatabaseHelper dbHelper;
    private String userKey, productName, imageUrl;

    private CheckBox cbChocolate, cbCaramel, cbStrawberry, cbMango, cbStrawberryFruit, cbBanana;
    private RadioGroup rgSize;
    private TextView tvQuantity, tvPrice;
    private com.google.android.material.button.MaterialButton btnAddToCart;
    private ImageView ivProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        // Responsive Safe Area Fix
        View root = findViewById(R.id.main_detail_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, 0, insets.right, insets.bottom);
            return windowInsets;
        });

        dbHelper = new DatabaseHelper(this);
        productName = getIntent().getStringExtra("PRODUCT_NAME");
        String priceStr = getIntent().getStringExtra("PRODUCT_PRICE");
        userKey = getIntent().getStringExtra("USER_INPUT");
        imageUrl = getIntent().getStringExtra("PRODUCT_IMAGE");

        if (priceStr != null) basePrice = Double.parseDouble(priceStr);

        // Initialize UI
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPrice = findViewById(R.id.tv_detail_price);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        rgSize = findViewById(R.id.rg_size);
        ivProduct = findViewById(R.id.iv_product_detail_image);

        cbChocolate = findViewById(R.id.cb_chocolate);
        cbCaramel = findViewById(R.id.cb_caramel);
        cbStrawberry = findViewById(R.id.cb_strawberry);
        cbMango = findViewById(R.id.cb_mango);
        cbStrawberryFruit = findViewById(R.id.cb_strawberry_fruit);
        cbBanana = findViewById(R.id.cb_banana);

        ((TextView)findViewById(R.id.tv_detail_name)).setText(productName);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(ivProduct);
        }

        setupListeners();
        calculateTotal();
    }

    private void setupListeners() {
        findViewById(R.id.btn_back_detail).setOnClickListener(v -> finish());

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            quantity++;
            calculateTotal();
        });

        findViewById(R.id.btn_minus).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                calculateTotal();
            }
        });

        // Listen for changes in customization
        rgSize.setOnCheckedChangeListener((group, checkedId) -> calculateTotal());
        
        View.OnClickListener updateCheck = v -> calculateTotal();
        cbChocolate.setOnClickListener(updateCheck);
        cbCaramel.setOnClickListener(updateCheck);
        cbStrawberry.setOnClickListener(updateCheck);
        cbMango.setOnClickListener(updateCheck);
        cbStrawberryFruit.setOnClickListener(updateCheck);
        cbBanana.setOnClickListener(updateCheck);

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void calculateTotal() {
        double currentItemPrice = basePrice;

        // Size Price
        if (rgSize.getCheckedRadioButtonId() == R.id.rb_grande) {
            currentItemPrice += 20.0;
        }

        // Sauces Price (+10 each)
        if (cbChocolate.isChecked()) currentItemPrice += 10.0;
        if (cbCaramel.isChecked()) currentItemPrice += 10.0;
        if (cbStrawberry.isChecked()) currentItemPrice += 10.0;

        // Fruits Price (+15 each)
        if (cbMango.isChecked()) currentItemPrice += 15.0;
        if (cbStrawberryFruit.isChecked()) currentItemPrice += 15.0;
        if (cbBanana.isChecked()) currentItemPrice += 15.0;

        totalPrice = currentItemPrice * quantity;

        tvQuantity.setText(String.valueOf(quantity));
        tvPrice.setText(String.format("P %.2f", currentItemPrice));
        btnAddToCart.setText(String.format("ADD TO CART - P %.2f", totalPrice));
    }

    private void addToCart() {
        if (userKey == null || userKey.isEmpty()) {
            Toast.makeText(this, "Please login to order", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder details = new StringBuilder(productName);
        details.append(" (").append(rgSize.getCheckedRadioButtonId() == R.id.rb_grande ? "Grande" : "Moyen").append(")");
        
        StringBuilder addOns = new StringBuilder();
        if (cbChocolate.isChecked()) addOns.append("Choco Sauce, ");
        if (cbCaramel.isChecked()) addOns.append("Caramel Sauce, ");
        if (cbStrawberry.isChecked()) addOns.append("Strawberry Sauce, ");
        if (cbMango.isChecked()) addOns.append("Mango, ");
        if (cbStrawberryFruit.isChecked()) addOns.append("Strawberry, ");
        if (cbBanana.isChecked()) addOns.append("Banana, ");

        if (addOns.length() > 0) {
            details.append(" + ").append(addOns.substring(0, addOns.length() - 2));
        }

        double finalUnitPrice = totalPrice / quantity;

        if (dbHelper.addToCart(userKey, details.toString(), finalUnitPrice, quantity, imageUrl)) {
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show();
        }
    }
}