package com.example.creamoso;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        findViewById(R.id.btn_back_help).setOnClickListener(v -> finish());

        findViewById(R.id.item_faq).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Frequently Asked Questions...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.item_contact_us).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Contact Support...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.item_privacy_policy).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show());

        findViewById(R.id.item_terms).setOnClickListener(v -> 
            Toast.makeText(this, "Opening Terms and Conditions...", Toast.LENGTH_SHORT).show());
    }
}