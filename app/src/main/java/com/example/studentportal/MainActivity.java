package com.example.studentportal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize components
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.navHostFragment);

        // Set up navigation
        NavigationUI.setupWithNavController(navigationView, navController);

        // Open Drawer when clicking on the menu button
        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Retrieve the role from SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "Visitor"); // Default role is Visitor

        // Restrict menu items based on role
        restrictMenuForVisitor(userRole);

        // Handle Navigation Item Clicks
        setupNavigationListener();
    }

    /**
     * Hides menu items for "Visitor" role.
     */
    private void restrictMenuForVisitor(String userRole) {
        Menu menu = navigationView.getMenu();

        if (userRole.equalsIgnoreCase("Visitor")) {
            // Hide or disable restricted items
            menu.findItem(R.id.nav_grades).setVisible(false);
            menu.findItem(R.id.nav_Calendar).setVisible(false);
            menu.findItem(R.id.nav_Schedule).setVisible(false);
            menu.findItem(R.id.nav_Subject).setVisible(false);
            menu.findItem(R.id.nav_Forms).setVisible(false);
        }
    }

    /**
     * Sets up navigation item selection behavior.
     */
    private void setupNavigationListener() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.nav_home) {
                    navController.navigate(R.id.nav_home); // Go to Home
                } else if (itemId == R.id.nav_grades) {
                    navController.navigate(R.id.action_dashboard_to_grades);
                } else if (itemId == R.id.nav_Calendar) {
                    navController.navigate(R.id.action_dashboard_to_calendar);
                } else if (itemId == R.id.nav_Schedule) {
                    navController.navigate(R.id.action_dashboard_to_schedule);
                } else if (itemId == R.id.nav_Subject) {
                    navController.navigate(R.id.action_dashboard_to_Subject);
                } else if (itemId == R.id.nav_Forms) {
                    navController.navigate(R.id.action_dashboard_to_form);
                } else if (itemId == R.id.nav_logout) {
                    showLogoutConfirmationDialog(); // Handle logout
                }

                drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer after item selection
                return true;
            }
        });
    }

    /**
     * Displays a logout confirmation dialog.
     */
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        performLogout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * Handles user logout and redirects to the login screen.
     */
    private void performLogout() {
        // Clear stored user role or session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to LoginActivity
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
