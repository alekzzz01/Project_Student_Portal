package com.example.studentportal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);

        NavigationView navigationView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navigationView, navController);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.nav_home) {
                    navController.navigate(R.id.nav_home); // Navigate to the dashboard (home) fragment
                } else if (itemId == R.id.nav_grades) {
                    navController.navigate(R.id.action_dashboard_to_grades);
                } else if (itemId == R.id.nav_Calendar) {
                    navController.navigate(R.id.action_dashboard_to_calendar);
                } else if (itemId == R.id.nav_Schedule) {
                    navController.navigate(R.id.action_dashboard_to_schedule);
                } else if (itemId == R.id.nav_Subject) {
                    navController.navigate(R.id.action_dashboard_to_Subject);
                } else if (itemId == R.id.nav_Enroll) {
                    navController.navigate(R.id.action_dashboard_to_enroll);
                } else if (itemId == R.id.nav_Forms) {
                    navController.navigate(R.id.action_dashboard_to_form);
                } else if (itemId == R.id.nav_logout) {
                    showLogoutConfirmationDialog(); // Show logout confirmation dialog
                }

                drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
                return true;
            }
        });


    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle logout
                        FirebaseAuth.getInstance().signOut();
                        redirectToSignInPage();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Function to redirect to the Sign-In page after logout
    private void redirectToSignInPage() {
        Intent intent = new Intent(this, LoginActivity.class); // Replace with your SignInActivity
        startActivity(intent);
        finish(); // Close the current activity
    }
}