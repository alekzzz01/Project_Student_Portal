package com.example.studentportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class fragment_dashboard extends Fragment {

    TextView name_header, newgrade;
    private String currentStudentNumber; // Store current student number
    private String userRole; // Store the user's role

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        name_header = rootView.findViewById(R.id.et_Name);
        newgrade = rootView.findViewById(R.id.newGrade);

        // Retrieve the user role and student number from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userRole = sharedPreferences.getString("role", "Visitor");
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        // Log the retrieved user role and student number for debugging
        Log.d("FragmentDashboard", "Retrieved role: " + userRole);
        Log.d("FragmentDashboard", "Retrieved student number: " + currentStudentNumber);

        // Check the role and set the header accordingly
        if (userRole.equalsIgnoreCase("Visitor")) {
            name_header.setText("VISITOR");
        } else if (currentStudentNumber != null) {
            // Load the student's name and grade from the database if the role is not Visitor
            loadUserName(currentStudentNumber);
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    // Method to load the user's name and grade from the database
    private void loadUserName(String studentNumber) {
        new Thread(() -> {
            try {
                // Get the connection using ConnectionClass
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Query the enrollstudentinformation table
                String nameQuery = "SELECT firstName, lastName FROM enrollstudentinformation WHERE studentNumber = ?";
                PreparedStatement nameStatement = connection.prepareStatement(nameQuery);
                nameStatement.setString(1, studentNumber);
                ResultSet nameResultSet = nameStatement.executeQuery();

                if (nameResultSet.next()) {
                    String firstName = nameResultSet.getString("firstName");
                    String lastName = nameResultSet.getString("lastName");

                    // Update the name on the main thread (UI thread)
                    getActivity().runOnUiThread(() -> name_header.setText(firstName + " " + lastName));
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show());
                }

                nameResultSet.close();
                nameStatement.close();

                String gradeQuery = "SELECT mygrade, subjectcode FROM enrollgradestbl WHERE studentnumber = ?";
                PreparedStatement gradeStatement = connection.prepareStatement(gradeQuery);
                gradeStatement.setString(1, studentNumber);
                ResultSet gradeResultSet = gradeStatement.executeQuery();

                if (gradeResultSet.next()) {
                    String subjectCode = gradeResultSet.getString("subjectcode");
                    String grade = gradeResultSet.getString("mygrade");

                    String displayText = subjectCode + " : " + grade;

                    getActivity().runOnUiThread(() -> newgrade.setText(displayText));
                } else {
                    getActivity().runOnUiThread(() ->
                            newgrade.setText("Grade not found."));
                }


                gradeResultSet.close();
                gradeStatement.close();

                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
