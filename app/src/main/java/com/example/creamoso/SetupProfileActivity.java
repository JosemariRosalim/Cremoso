package com.example.creamoso;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SetupProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etBirthday;
    private CheckBox cbTerms;
    private MaterialButton btnFinish;
    private String phoneNumber;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        dbHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etBirthday = findViewById(R.id.et_birthday);
        cbTerms = findViewById(R.id.cb_terms);
        btnFinish = findViewById(R.id.btn_finish_setup);

        etBirthday.setOnClickListener(v -> showDatePicker());

        btnFinish.setOnClickListener(v -> handleFinishSetup());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> etBirthday.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    private void handleFinishSetup() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Save to local SQLite
        if (dbHelper.addUser(phoneNumber)) {
            dbHelper.updateProfile(phoneNumber, firstName, lastName, birthday);
        }

        // 2. Save to Firebase Realtime Database
        saveUserToFirebase(firstName, lastName, email, birthday);

        // 3. Email Verification if provided
        if (!email.isEmpty()) {
            verifyEmail(email);
        } else {
            proceedToHome();
        }
    }

    private void saveUserToFirebase(String fName, String lName, String email, String bday) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", fName);
        userData.put("lastName", lName);
        userData.put("email", email);
        userData.put("birthday", bday);
        userData.put("mobile", phoneNumber);
        userData.put("type", "mobile");
        userData.put("registeredAt", System.currentTimeMillis());

        usersRef.child(phoneNumber).setValue(userData);
    }

    private void verifyEmail(String email) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updateEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.sendEmailVerification().addOnCompleteListener(vTask -> {
                        Toast.makeText(this, "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                        proceedToHome();
                    });
                } else {
                    Log.e("SetupProfile", "Failed to update email", task.getException());
                    proceedToHome();
                }
            });
        } else {
            proceedToHome();
        }
    }

    private void proceedToHome() {
        Intent intent = new Intent(SetupProfileActivity.this, HomeActivity.class);
        intent.putExtra("USER_INPUT", phoneNumber);
        startActivity(intent);
        finish();
    }
}