package com.example.tuitionapp_sujon.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tuitionapp_sujon.guardian.ViewingSearchingTutorProfileActivity;
import com.example.tuitionapp_sujon.R;
import com.example.tuitionapp_sujon.system.HomePageActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminHomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth mAuth ;
    private GoogleSignInClient mGoogleSignInClient ;
    private DrawerLayout drawerLayout ;
    private NavigationView navigationView ;
    private View view ;
    private TextView nameTextView, emailTextView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);
        setTitle("ADMIN PANEL");
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder().requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        drawerLayout = findViewById(R.id.drawer_layout) ;
        navigationView = findViewById(R.id.navigation_view) ;

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close) ;
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        view = navigationView.getHeaderView(0) ;

        nameTextView = (TextView)view.findViewById(R.id.name) ;
        emailTextView = (TextView)view.findViewById(R.id.email) ;

        nameTextView.setText("Admin");
        emailTextView.setText("tuitionapsspl02@gmail.com");

    }

    public void goToViewingAndSearchingTutorProfileActivity(View view) {
        Intent intent = new Intent(this, ViewingSearchingTutorProfileActivity.class);
        intent.putExtra("user", "admin") ;
        startActivity(intent);
        finish();
    }

    public void goToCandidateTutorProfileAdminViewActivity(View view){
        Intent intent = new Intent(this, AdminTutorProfileViewActivity.class) ;
        intent.putExtra("flag" , "approveTutor") ;
        startActivity(intent);
        finish();
    }

    public void goToCandidateTutorProfileAdminBlockViewActivity(View view){
        Intent intent = new Intent(this, AdminTutorProfileViewActivity.class) ;
        intent.putExtra("flag" , "blockTutor" ) ;
        startActivity(intent);
        finish();
    }

    public void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
        Intent intent = new Intent(this, HomePageActivity.class) ;
        startActivity(intent);
        finish();
    }

    public void openDrawerOperation(View view){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerVisible(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out:
                signOut();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
