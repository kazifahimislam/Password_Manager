package com.example.passwordmanager;

import static com.google.firebase.auth.AuthKt.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    int sign_in_code = 123;
    FirebaseDatabase database;
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
        DatabaseReference ref = database.getReference("test");
        ref.setValue("Hello, World!")
                .addOnSuccessListener(aVoid -> {
                    // Data was successfully written
                    Log.d("DatabaseTest", "Data saved successfully");
                    Toast.makeText(LoginActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle the failure
                    Log.e("DatabaseTest", "Data save failed: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, "Data save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();


        gsc = GoogleSignIn.getClient(this, gso);

        gAuthButton.setOnClickListener(view -> {
            
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
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            }
        }
    }

    private void auth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            HashMap<String,Object> map = new HashMap<>();
                            assert user != null;
                            map.put("id",user.getUid());
                            map.put("name",user.getDisplayName());
                            map.put("email",user.getEmail());
                            map.put("uid",user.getUid());
                            map.put("profile",user.getPhotoUrl().toString());
                            database.getReference().child("users").child(user.getUid()).setValue(map).addOnFailureListener(e -> {
                                // Handle the failure
                                Toast.makeText(LoginActivity.this, "Data save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}