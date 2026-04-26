package com.example.creamoso;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    private String verificationId;
    private String phoneNumber;
    private EditText etOtp;
    private TextView tvSubtitle, tvTimer;
    private MaterialButton btnVerify;
    private ImageButton btnBack;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);

        View root = findViewById(R.id.main_otp_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        verificationId = getIntent().getStringExtra("VERIFICATION_ID");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        etOtp = findViewById(R.id.et_otp);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvTimer = findViewById(R.id.tv_timer);
        btnVerify = findViewById(R.id.btn_verify);
        btnBack = findViewById(R.id.btn_back);

        tvSubtitle.setText("We sent a 6-digit verification code to\n" + phoneNumber);

        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (code.length() == 6) {
                verifyCode(code);
            } else {
                Toast.makeText(this, "Enter 6-digit code", Toast.LENGTH_SHORT).show();
            }
        });

        startTimer();
    }

    private void startTimer() {
        new CountDownTimer(90000, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("You may request a new code %02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                tvTimer.setText("You can now request a new code.");
            }
        }.start();
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (dbHelper.checkUser(phoneNumber)) {
                            Intent intent = new Intent(OtpActivity.this, HomeActivity.class);
                            intent.putExtra("USER_INPUT", phoneNumber);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(OtpActivity.this, SetupProfileActivity.class);
                            intent.putExtra("PHONE_NUMBER", phoneNumber);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(OtpActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}