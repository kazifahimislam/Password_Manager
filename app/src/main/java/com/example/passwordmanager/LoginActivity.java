package com.example.passwordmanager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.passwordmanager.datasecurity.DataEncryption;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    int sign_in_code = 123;
    FirebaseDatabase database;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);

        Button gAuthButton = findViewById(R.id.gAuthButton);
        auth = FirebaseAuth.getInstance();
        database =  FirebaseDatabase.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();


        gsc = GoogleSignIn.getClient(this, gso);
        progressBar = findViewById(R.id.progressBar);


        gAuthButton.setOnClickListener(view -> {
            gAuthButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            
            signIn();

        });
    }

    private void signIn() {
        Intent i = gsc.getSignInIntent();
        startActivityIfNeeded(i,sign_in_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sign_in_code){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                auth(account.getIdToken());
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void auth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            HashMap<String, Object> map = new HashMap<>();
                            assert user != null;
                            DataEncryption dataEncryption = null;
                            try {
                                dataEncryption = new DataEncryption();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            String encryptedName = "";
                            String encryptedEmail = "";
                            String encryptedUid = "";
                            String encryptedProfile = "";

                            try {
                                encryptedName = dataEncryption.encrypt(Objects.requireNonNull(user.getDisplayName()));
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Failed to encrypt user name", Toast.LENGTH_SHORT).show();
                                
                            }
                            try {
                                encryptedEmail = dataEncryption.encrypt(Objects.requireNonNull(user.getEmail()));
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Failed to encrypt user email", Toast.LENGTH_SHORT).show();
                            }
                            try {
                                encryptedUid = dataEncryption.encrypt(user.getUid());
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Failed to encrypt user uid", Toast.LENGTH_SHORT).show();
                            }
                            try {
                                encryptedProfile = dataEncryption.encrypt(Objects.requireNonNull(user.getPhotoUrl()).toString());
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Failed to encrypt user Profile Picture", Toast.LENGTH_SHORT).show();
                            }


                            map.put("name", encryptedName);
                            map.put("email", encryptedEmail);
                            map.put("uid", encryptedUid);
                            map.put("profilePic", encryptedProfile);
                            database.getReference().child("users").child(user.getUid()).setValue(map).addOnSuccessListener(aVoid -> {
                                        Log.d("DatabaseSuccess", "Data saved successfully");
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("DatabaseError", "Data save failed: " + e.getMessage());
                                    });
                        }else{
                            progressBar.setVisibility(View.GONE);
                            Button gAuthButton = findViewById(R.id.gAuthButton);
                            gAuthButton.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}