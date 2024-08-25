package com.example.passwordmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ImageButton profileImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        profileImg = findViewById(R.id.profileImg);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is logged in and has a profile picture
        if (user != null && user.getPhotoUrl() != null) {
            String profilePicUrl = user.getPhotoUrl().toString();

            Intent i = new Intent();
//        String profilePicUrl = i.getStringExtra("profilePic");

            Glide.with(this)
                    .load(profilePicUrl)// Error image
                    .circleCrop()                            // Circular cropping
                    .into(profileImg);

        }
    }}