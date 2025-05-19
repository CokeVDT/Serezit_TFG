package com.example.tfg.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tfg.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText usernameEditText, emailEditText, passwordEditText;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;



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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("name", username);
                        map.put("email", email);
                        map.put("password", password);

                        FirebaseUser user = mAuth.getCurrentUser();
                        String id = mAuth.getCurrentUser().getUid();
                        mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>(){
                            @Override
                            public void onComplete(@NonNull Task<Void> task2) {
                                if (task2.isSuccessful()){
                                    finish();
                                }
                            }
                        });
                        // Actualizar el displayName del usuario
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Redirigir a MainActivity después de actualizar el nombre
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this,
                                                "Error al guardar nombre de usuario",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}