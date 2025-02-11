package com.chaitany.carbonview;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    ImageView useView;
    SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "TaskCompletionPrefs"; // SharedPreferences file name
    private static final String COMPLETED_TASK_PREFIX = "completed_task_";

    private static final String COMPLETION_DATE_PREFIX = "completion_date_";

    Integer stock;

    private LinearLayout layoutEmergencyContact, layoutMedicalStock, layoutHealthMonitor, layoutmealplan,
            layout_bmi_index, layout_elder_connect, layout_exercise,layout_hospital;
    private LinearLayout morningTasksLayout, afternoonTasksLayout, nightTasksLayout;
    private ImageView menuButton,meta;
    private DatabaseReference tasksRef;
    private String userPhone;
    private static final String PREFS_TIME_BLOCKS = "time_blocks";
    private static final String KEY_MORNING_COMPLETED = "morning_done";
    private static final String KEY_AFTERNOON_COMPLETED = "afternoon_done";
    private static final String KEY_NIGHT_COMPLETED = "night_done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);


        sharedPreferences = getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        userPhone = sharedPreferences.getString("mobile", null);

        // Initialize Firebase
        if (userPhone != null) {
            tasksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userPhone).child("tasks");
        }

        // Handling window insets for a smooth UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.miana), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupNavigationDrawer();
        setupUserProfile();
        setupClickListeners();

    }



    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }







    private void initializeViews() {
        useView=findViewById(R.id.user_png_dot);
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.menubars);
        layout_bmi_index = findViewById(R.id.layout_bmi_index);
        layoutEmergencyContact = findViewById(R.id.layout_emergency_contact);
        layoutMedicalStock = findViewById(R.id.layout_medical_stock);
        layoutHealthMonitor = findViewById(R.id.layout_health_monitor);
        layoutmealplan = findViewById(R.id.mealplans);
        layout_elder_connect = findViewById(R.id.layout_elder_connect);
        layout_exercise = findViewById(R.id.layoutexercise);
        layout_hospital=findViewById(R.id.nearbyhospital);
        meta=findViewById(R.id.meta);



        // Initialize task section layouts
        morningTasksLayout = findViewById(R.id.morningTasksLayout);
        afternoonTasksLayout = findViewById(R.id.afternoonTasksLayout);
        nightTasksLayout = findViewById(R.id.nightTasksLayout);
    }

    private void setupNavigationDrawer() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupUserProfile() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        SharedPreferences sharedPref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        String name = sharedPref.getString("name", "User Name");
        String mobile = sharedPref.getString("mobile", "XXXX XX XXXX");
        String gender = sharedPref.getString("gender", "Gender");

        TextView username = findViewById(R.id.username);
        ImageView userimg = findViewById(R.id.user_png_dot);
        TextView usernameTextView = headerView.findViewById(R.id.username);
        TextView mobileTextView = headerView.findViewById(R.id.mobile);
        ImageView user_png = headerView.findViewById(R.id.user_png);

        if (gender.equals("Male")) {
            user_png.setImageResource(R.drawable.img_oldman);
            userimg.setImageResource(R.drawable.img_oldman);
        }
        if (gender.equals("Female")) {
            user_png.setImageResource(R.drawable.img_oldfemale);
            userimg.setImageResource(R.drawable.img_oldfemale);
        }

        username.setText("Hi, " + name);
        usernameTextView.setText(name);
        mobileTextView.setText(mobile);
    }




























    private void setupClickListeners() {

        useView.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, ProfileActivity.class)));

        meta.setOnClickListener(v->{



            startActivity(new Intent(Dashboard.this, AiActivity.class));
        });
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLogged", false);
            editor.apply();
            intent = new Intent(Dashboard.this, Login.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_profile) {
            intent = new Intent(Dashboard.this, ProfileActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_share) {
            shareApp();
        } else if (item.getItemId() == R.id.nav_feedback) {
            intent = new Intent(Dashboard.this, FeedbackActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_about) {
            intent = new Intent(Dashboard.this, AboutActivity.class);
            startActivity(intent);
        } else {
            return false;
        }






        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        String appLink = "https://play.google.com/store/apps/details?id=com.chaitany.agewell";
        String shareMessage = "Check out this amazing app that helps you and your older person to stay healthy and care! \n\n" +
                "Download it here: " + appLink + "\n\n" +
                "Stay healthy and fit!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Our App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    // Task model class
    public static class Task {
        private String medicineName;
        private String medicineId;
        private String time;

        private String mealTime;
        private int quantity;
        private String taskName;

        public Task() {
            this.medicineName = "Unknown Medicine";  // Set default value
        }

        // Required empty constructor for Firebase
        public Task(String medicineName) {
            this.medicineName = medicineName;
        }

        // Constructor for predefined tasks
        public Task(String taskname, String time, String mealTime, int quantity, String medicineId,String medicineName) {
            this.taskName = taskname;
            this.time = time;
            this.mealTime = mealTime;
            this.quantity = quantity;
            this.medicineId = medicineId;
            this.medicineName = medicineName;
        }

        // Getters and setter
        //
        // s

        public String getMedicineName() {
            return medicineName;
        }
        public void setMedicineName() {
            this.medicineName = medicineName;
        }

        public String getMedicineId() {
            return medicineId;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMealTime() {
            return mealTime;
        }

        public void setMealTime(String mealTime) {
            this.mealTime = mealTime;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }


    }
    @Override
    protected void onResume() {
        super.onResume();

        // Call the handleNewDay function to check and update tasks for the new day

    }

}