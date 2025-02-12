package com.chaitany.carbonview;

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

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private SharedPreferences sharedPreferences;
    LinearLayout adddata,uploadreport,viewreport,aiinsights,forecast,compare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        initializeViews();
        setupNavigationDrawer();
        setupUserProfile();
        setuponlickilistener();

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setuponlickilistener() {
        adddata.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, AddData.class);
            startActivity(intent);
        });
        uploadreport.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, UploadReport.class);
            startActivity(intent);
        });

        viewreport.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, ViewReport.class);
            startActivity(intent);
        });

        aiinsights.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, AiInsights.class);
            startActivity(intent);
        });

        forecast.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, Forecast.class);
            startActivity(intent);
        });

        compare.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, Compare.class);
            startActivity(intent);
        });


    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        menuIcon = findViewById(R.id.menuIcon);
        adddata=findViewById(R.id.add_data);
        uploadreport=findViewById(R.id.uploadreport);
        viewreport=findViewById(R.id.viewreport);
        aiinsights=findViewById(R.id.aiinsights);
        forecast=findViewById(R.id.forecast);
        compare=findViewById(R.id.compare);



        // Ensure this exists in your app_bar_header.xml

        // Set click listener for menu icon
        menuIcon.setOnClickListener(v -> toggleNavigationDrawer());
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupUserProfile() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        SharedPreferences sharedPref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        String name = sharedPref.getString("name", "User Name");
        String mobile = sharedPref.getString("mobile", "XXXX XX XXXX");

        TextView username = findViewById(R.id.username);
        TextView usernameHeader = headerView.findViewById(R.id.username);
        TextView mobileHeader = headerView.findViewById(R.id.mobile);

     //   username.setText(getString("hello", name));
        usernameHeader.setText(name);
        mobileHeader.setText(mobile);
    }

    private void toggleNavigationDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogged", false);
        editor.apply();

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
      //  String shareMessage = getString(R.string.share_message, appLink);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
       // shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
      ////  shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
      //  startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
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