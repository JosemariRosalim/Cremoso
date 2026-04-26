package com.example.creamoso;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String ADMIN_EMAIL = "josemarirosalim@gmail.com";
    
    private TextView tabMobile, tabEmail, tvPrefix, tvHint;
    private EditText etInput, etPassword;
    private LinearLayout layoutPassword;
    private Button btnNext;
    private com.google.android.gms.common.SignInButton btnGoogle;
    private boolean isMobileSelected = true;
    private DatabaseHelper dbHelper;
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Edge-to-Edge for full-screen responsive behavior
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Apply WindowInsets to the root view to avoid status and navigation bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login_root), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        dbHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        tabMobile = findViewById(R.id.tab_mobile);
        tabEmail = findViewById(R.id.tab_email);
        tvPrefix = findViewById(R.id.tv_prefix);
        tvHint = findViewById(R.id.tv_hint);
        etInput = findViewById(R.id.et_input);
        etPassword = findViewById(R.id.et_password);
        layoutPassword = findViewById(R.id.layout_password);
        btnNext = findViewById(R.id.btn_next);
        btnGoogle = findViewById(R.id.btn_google_signin);

        tabMobile.setOnClickListener(v -> selectMobileTab());
        tabEmail.setOnClickListener(v -> selectEmailTab());
        btnNext.setOnClickListener(v -> handleLoginRegister());
        
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        setupPhoneCallbacks();
    }

    private void setupPhoneCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w("PhoneAuth", "onVerificationFailed", e);
                Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                intent.putExtra("VERIFICATION_ID", verificationId);
                intent.putExtra("PHONE_NUMBER", "+63" + etInput.getText().toString().trim());
                startActivity(intent);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String phoneNumber = "+63" + etInput.getText().toString().trim();
                        if (dbHelper.checkUser(phoneNumber)) {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("USER_INPUT", phoneNumber);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, SetupProfileActivity.class);
                            intent.putExtra("PHONE_NUMBER", phoneNumber);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String email = mAuth.getCurrentUser().getEmail();
                        
                        // Check for Admin Access
                        if (email != null && email.equalsIgnoreCase(ADMIN_EMAIL)) {
                            Toast.makeText(this, "Admin Access Granted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                            finish();
                            return;
                        }

                        if (!dbHelper.checkUser(email)) {
                            dbHelper.addUser(email);
                            Intent intent = new Intent(LoginActivity.this, SetupProfileActivity.class);
                            intent.putExtra("EMAIL", email);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("USER_INPUT", email);
                            startActivity(intent);
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectMobileTab() {
        isMobileSelected = true;
        tabMobile.setBackgroundResource(R.drawable.rounded_card);
        tabMobile.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF9C4")));
        tabMobile.setTextColor(getResources().getColor(R.color.teal_background));
        tabMobile.setAlpha(1.0f);

        tabEmail.setBackground(null);
        tabEmail.setTextColor(Color.WHITE);
        tabEmail.setAlpha(0.7f);

        tvPrefix.setVisibility(View.VISIBLE);
        layoutPassword.setVisibility(View.GONE);
        etInput.setHint("Enter Mobile Number");
        etInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        tvHint.setText("Use your mobile number to login/register");
    }

    private void selectEmailTab() {
        isMobileSelected = false;
        tabEmail.setBackgroundResource(R.drawable.rounded_card);
        tabEmail.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF9C4")));
        tabEmail.setTextColor(getResources().getColor(R.color.teal_background));
        tabEmail.setAlpha(1.0f);

        tabMobile.setBackground(null);
        tabMobile.setTextColor(Color.WHITE);
        tabMobile.setAlpha(0.7f);

        tvPrefix.setVisibility(View.GONE);
        layoutPassword.setVisibility(View.VISIBLE);
        etInput.setHint("Enter Email Address");
        etInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        tvHint.setText("Use your email address to login/register");
    }

    private void handleLoginRegister() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter " + (isMobileSelected ? "mobile number" : "email"), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for Admin Access based on Email
        if (!isMobileSelected && input.equalsIgnoreCase(ADMIN_EMAIL)) {
            String password = etPassword.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter admin password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(input, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Admin Access Granted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            return;
        }

        if (isMobileSelected) {
            startPhoneNumberVerification("+63" + input);
        } else {
            String password = etPassword.getText().toString().trim();
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            
            mAuth.signInWithEmailAndPassword(input, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.putExtra("USER_INPUT", input);
                        startActivity(intent);
                        finish();
                    } else {
                        registerEmailUser(input, password);
                    }
                });
        }
    }

    private void registerEmailUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    dbHelper.addUser(email);
                    Intent intent = new Intent(LoginActivity.this, SetupProfileActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}