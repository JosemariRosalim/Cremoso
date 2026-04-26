package com.example.creamoso;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MyProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName;
    private TextView tvMobile, tvEmail, tvBirthday;
    private DatabaseHelper dbHelper;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        dbHelper = new DatabaseHelper(this);
        userKey = getIntent().getStringExtra("USER_INPUT");

        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        tvBirthday = findViewById(R.id.tv_display_birthday);
        tvMobile = findViewById(R.id.tv_display_mobile);
        tvEmail = findViewById(R.id.tv_display_email);

        loadUserData();

        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());

        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());

        findViewById(R.id.tv_delete_account).setOnClickListener(v -> deleteAccount());
    }

    private void loadUserData() {
        if (userKey == null) return;
        
        Cursor cursor = dbHelper.getUserData(userKey);
        if (cursor != null && cursor.moveToFirst()) {
            etFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
            etLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
            
            String bday = cursor.getString(cursor.getColumnIndexOrThrow("birthday"));
            tvBirthday.setText(bday != null && !bday.isEmpty() ? bday : "Not provided");
            
            String mob = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            tvMobile.setText(mob != null && !mob.isEmpty() ? mob : "Not provided");
            
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            tvEmail.setText(email != null && !email.isEmpty() ? email : "Not provided");
            
            cursor.close();
        }
    }

    private void saveProfile() {
        String fName = etFirstName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        // Since birthday is now display-only in this specific request, we pass current value
        String currentBday = tvBirthday.getText().toString();

        if (dbHelper.updateProfile(userKey, fName, lName, currentBday)) {
            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAccount() {
        if (dbHelper.deleteUser(userKey)) {
            Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Action Failed", Toast.LENGTH_SHORT).show();
        }
    }
}