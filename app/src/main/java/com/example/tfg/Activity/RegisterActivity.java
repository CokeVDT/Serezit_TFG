package com.example.tfg.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText usernameEditText, emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);

        findViewById(R.id.registerBtn).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEditText.setError("Ingresa un nombre de usuario");
            return;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Ingresa tu email");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar nombre en perfil
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Guardar en Realtime Database
                                            saveUserToDatabase(user.getUid(), username, email, progressDialog);
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this,
                                                    "Error al guardar perfil",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String uid, String username, String email, ProgressDialog progressDialog) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("favorites", new ArrayList<>());
        userData.put("assignedProducts", new ArrayList<>());
        userData.put("cart", new HashMap<>());


        mDatabase.child("Users").child(uid).setValue(userData)
                .addOnCompleteListener(dbTask -> {
                    progressDialog.dismiss();
                    if (dbTask.isSuccessful()) {
                        // Redirección con flags para limpiar el stack
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error al guardar datos",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}