package com.example.tfg.Activity;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Uri imageUri;
    private ImageView profileImage;
    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button registerBtn;
    private ProgressDialog progressDialog;

    // Datos Cloudinary - cambia por los tuyos
    private final String CLOUD_NAME = "dlnrrq7er";
    private final String UPLOAD_PRESET = "unsigned_preset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        profileImage = findViewById(R.id.profileImage);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        registerBtn.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(profileImage);
        }
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
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();

                        if (imageUri != null) {
                            uploadImageToCloudinary(userId, username, email, password, user);
                        } else {
                            // Sin imagen
                            updateProfileAndFirestore(userId, username, email, password, null, user);
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadImageToCloudinary(String userId, String username, String email, String password, FirebaseUser user) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg",
                            RequestBody.create(MediaType.parse("image/*"), imageBytes))
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String jsonStr = response.body().string();
                        try {
                            JSONObject json = new JSONObject(jsonStr);
                            String imageUrl = json.getString("secure_url");

                            runOnUiThread(() -> {
                                updateProfileAndFirestore(userId, username, email, password, imageUrl, user);
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Error parseando respuesta", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error al subir imagen a Cloudinary", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(this, "Error leyendo la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileAndFirestore(String userId, String username, String email, String password, @Nullable String photoUrl, FirebaseUser user) {
        UserProfileChangeRequest.Builder profileBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(username);

        if (photoUrl != null) {
            profileBuilder.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(profileBuilder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserInFirestore(userId, username, email, password, photoUrl);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error actualizando perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserInFirestore(String userId, String username, String email, String password, @Nullable String photoUrl) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("email", email);
        userMap.put("password", md5(password));
        if (photoUrl != null) {
            userMap.put("photoUrl", photoUrl);
        }

        mFirestore.collection("Users").document(userId)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro completado", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error guardando en Firestore", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : messageDigest) {
                String h = Integer.toHexString(0xFF & b);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
