package com.example.tuitionappv1.guardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.tuitionappv1.R;
import com.example.tuitionappv1.system.HomePageActivity;
import com.example.tuitionappv1.tuition_post.TuitionPostActivity;
import com.google.firebase.auth.FirebaseAuth;

public class GuardianHomePageActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_home_page);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
    }

    public void goToGuardianPostForTuitionActivity(View view) {
        Intent intent = new Intent(this, TuitionPostActivity.class);
        startActivity(intent);
        finish();

    }

    public void goToGuardianTutorProfileViewActivity(View view){
        //Intent intent = new Intent(this, GuardianTutorProfileViewActivity.class) ;
        Intent intent = new Intent(this, ViewingSearchingTutorProfileActivity.class) ;
        intent.putExtra("user","guardian") ;
        startActivity(intent);
        finish();
    }

    public void signOut(View view) {
        mAuth.signOut();

        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
        finish();

    }
}