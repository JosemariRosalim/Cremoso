package com.example.creamoso;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ManagePaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_payment);

        findViewById(R.id.btn_back_manage_payment).setOnClickListener(v -> finish());

        findViewById(R.id.btn_update_qr).setOnClickListener(v -> 
            Toast.makeText(this, "Select QR Code from Gallery...", Toast.LENGTH_SHORT).show());
    }
}