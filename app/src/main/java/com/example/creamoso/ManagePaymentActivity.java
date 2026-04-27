package com.example.creamoso;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManagePaymentActivity extends AppCompatActivity {

    private ImageView ivCurrentQr;
    private EditText etQrUrl;
    private DatabaseReference paymentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_payment);

        ivCurrentQr = findViewById(R.id.iv_current_qr);
        etQrUrl = findViewById(R.id.et_gcash_qr_url);
        paymentRef = FirebaseDatabase.getInstance().getReference("settings").child("gcash_qr");

        findViewById(R.id.btn_back_manage_payment).setOnClickListener(v -> finish());

        loadCurrentQr();

        findViewById(R.id.btn_save_qr_url).setOnClickListener(v -> saveQrUrl());
    }

    private void loadCurrentQr() {
        paymentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                if (url != null && !url.isEmpty()) {
                    etQrUrl.setText(url);
                    Glide.with(ManagePaymentActivity.this)
                            .load(url)
                            .placeholder(R.drawable.creamoso_logoicon)
                            .into(ivCurrentQr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagePaymentActivity.this, "Failed to load QR", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveQrUrl() {
        String url = etQrUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please paste a URL first", Toast.LENGTH_SHORT).show();
            return;
        }

        paymentRef.setValue(url).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "GCash QR Updated Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}