package com.example.creamoso;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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

import java.util.HashMap;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    private String orderId, userInput;
    private TextView tvStatus, tvDate, tvTotal, tvOrderId;
    private LinearLayout containerItems;
    private MaterialCardView cardRating;
    private RatingBar ratingBar;
    private EditText etFeedback;
    private DatabaseReference orderRef;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);

        View root = findViewById(R.id.main_order_details_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, 0, insets.right, insets.bottom);
            return windowInsets;
        });

        dbHelper = new DatabaseHelper(this);
        orderId = getIntent().getStringExtra("ORDER_ID");
        userInput = getIntent().getStringExtra("USER_INPUT");

        tvOrderId = findViewById(R.id.tv_title_order_id);
        tvStatus = findViewById(R.id.tv_details_status);
        tvDate = findViewById(R.id.tv_details_date);
        tvTotal = findViewById(R.id.tv_details_total);
        containerItems = findViewById(R.id.container_receipt_items);
        cardRating = findViewById(R.id.card_rating);
        ratingBar = findViewById(R.id.rating_bar);
        etFeedback = findViewById(R.id.et_feedback);

        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        findViewById(R.id.btn_back_details).setOnClickListener(v -> finish());
        
        loadOrderData();

        findViewById(R.id.btn_submit_rating).setOnClickListener(v -> submitFeedback());
        
        findViewById(R.id.btn_reorder).setOnClickListener(v -> handleReorder());
    }

    private void loadOrderData() {
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String date = snapshot.child("timestamp").getValue(String.class);
                    Double total = snapshot.child("total").getValue(Double.class);
                    String items = snapshot.child("items").getValue(String.class);

                    tvStatus.setText(status);
                    tvDate.setText("Date: " + date);
                    tvTotal.setText(String.format("P%.2f", total));
                    tvOrderId.setText("Order #" + orderId.substring(orderId.length() - 6).toUpperCase());

                    if ("COMPLETED".equals(status)) {
                        tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                        // Check if already rated
                        if (!snapshot.hasChild("rating")) {
                            cardRating.setVisibility(View.VISIBLE);
                        } else {
                            cardRating.setVisibility(View.GONE);
                        }
                    } else {
                        tvStatus.setTextColor(Color.parseColor("#FF9800"));
                        cardRating.setVisibility(View.GONE);
                    }

                    displayReceiptItems(items);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayReceiptItems(String itemsStr) {
        containerItems.removeAllViews();
        if (itemsStr == null) return;

        String[] itemArray = itemsStr.split(", ");
        for (String item : itemArray) {
            TextView tvItem = new TextView(this);
            tvItem.setText(item);
            tvItem.setPadding(0, 8, 0, 8);
            tvItem.setTextColor(Color.BLACK);
            containerItems.addView(tvItem);
        }
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String feedback = etFeedback.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("comment", feedback);

        orderRef.child("feedback").setValue(ratingData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
            cardRating.setVisibility(View.GONE);
        });
    }

    private void handleReorder() {
        // Simple reorder: Go to Home screen. 
        // For a full "Instant Cart" reorder, we would parse the items string and call dbHelper.addToCart
        Toast.makeText(this, "Adding items back to cart...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("USER_INPUT", userInput);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}