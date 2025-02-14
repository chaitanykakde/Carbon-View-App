package com.chaitany.carbonview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth; // Firebase Auth instance
    LinearLayout adddata, uploadreport, viewreport, aiinsights, Connectiot, compare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        initializeViews();
        setupNavigationDrawer();

        setUponClickListener();

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load user information
        loadUserInfo();
    }

    private void setUponClickListener() {
        adddata.setOnClickListener(view -> startActivity(new Intent(Dashboard.this, AddData.class)));
        uploadreport.setOnClickListener(view -> startActivity(new Intent(Dashboard.this, UploadReport.class)));
        viewreport.setOnClickListener(view -> startActivity(new Intent(Dashboard.this, CompanyFinalEmissionData.class)));
        aiinsights.setOnClickListener(view -> startActivity(new Intent(Dashboard.this, EstimationGrid.class)));
        Connectiot.setOnClickListener(view -> startActivity(new Intent(Dashboard.this, ConnectIOT.class)));
        compare.setOnClickListener(view -> startActivity(new Intent(Dashboard.this, Compare.class)));
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        menuIcon = findViewById(R.id.menuIcon);
        adddata = findViewById(R.id.add_data);
        uploadreport = findViewById(R.id.uploadreport);
        viewreport = findViewById(R.id.viewreport);
        aiinsights = findViewById(R.id.aiinsights);
        Connectiot = findViewById(R.id.connectiot);
        compare = findViewById(R.id.compare);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Set click listener for menu icon
        menuIcon.setOnClickListener(v -> toggleNavigationDrawer());
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void toggleNavigationDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void loadUserInfo() {
        FirebaseUser  currentUser  = auth.getCurrentUser ();
        if (currentUser  != null) {
            String userName = currentUser .getDisplayName(); // Get user name
            String userEmail = currentUser .getEmail(); // Get user email

            // Update the navigation header with user info
            NavigationView navigationView = findViewById(R.id.navigationView);
            View headerView = navigationView.getHeaderView(0); // Get the header view
            TextView usernameTextView = headerView.findViewById(R.id.username);
            TextView emailTextView = headerView.findViewById(R.id.mobile); // Assuming mobile is used for email in the header

            usernameTextView.setText(userName != null ? userName : "User  Name");
            emailTextView.setText(userEmail != null ? userEmail : "Email Address");
        } else {
            Toast.makeText(this, "User  not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_logout) {
            handleLogout();
        } else {
            handleNavigationItem(itemId);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        // Sign out from Firebase
        auth.signOut();

        // Clear SharedPreferences
        sharedPreferences = getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogged", false);
        editor.apply();

        // Navigate to Login Activity
        startActivity(new Intent(Dashboard.this, Login.class));
        finish();
    }

    private void handleNavigationItem(int itemId) {
        Intent intent = null;
        if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        } else if (itemId == R.id.nav_share) {
            shareApp();
            return;
        } else if (itemId == R.id.nav_feedback) {
            intent = new Intent(this, FeedbackActivity.class);
        } else if (itemId == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Feature not implemented", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareApp() {
        String appLink = "https://play.google.com/store/apps/details?id=com.chaitany.agewell";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, appLink);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}