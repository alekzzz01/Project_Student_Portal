package com.example.studentportal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class fragment_dashboard extends Fragment {

    TextView name_header;
    private String currentStudentNumber; // Store current student number

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        name_header = rootView.findViewById(R.id.et_Name);

        // Retrieve the current student number from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        // Log the retrieved student number for debugging
        Log.d("FragmentDashboard", "Retrieved student number: " + currentStudentNumber);

        // Load the student name from database if student number is available
        if (currentStudentNumber != null) {
            loadUserName(currentStudentNumber); // Load the name from database
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        ImageView kebabMenu = rootView.findViewById(R.id.kebab_menu);
        kebabMenu.setOnClickListener(v -> showPopupMenu(v));

        return rootView;
    }


    // Method to load the user's name from the database
    private void loadUserName(String studentNumber) {
        new Thread(() -> {
            try {
                // Get the connection using ConnectionClass
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Query the enrollstudentinformation table
                String query = "SELECT firstName, lastName FROM enrollstudentinformation WHERE studentNumber = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, studentNumber);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");

                    // Update the name on the main thread (UI thread)
                    getActivity().runOnUiThread(() -> name_header.setText(firstName + " " + lastName));
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show());
                }

                resultSet.close();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Method to show PopupMenu for announcement actions
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.kebab_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_view_details) {
                Toast.makeText(getActivity(), "View Announcement Details", Toast.LENGTH_SHORT).show();
                return true;
            } else if (menuItem.getItemId() == R.id.action_delete) {
                Toast.makeText(getActivity(), "Announcement Deleted", Toast.LENGTH_SHORT).show();
                return true;
            } else if (menuItem.getItemId() == R.id.action_share) {
                Toast.makeText(getActivity(), "Share Announcement", Toast.LENGTH_SHORT).show();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this announcement!");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }
}
