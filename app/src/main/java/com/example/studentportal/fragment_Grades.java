package com.example.studentportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class fragment_Grades extends Fragment {

    private AutoCompleteTextView semesterDropdown;
    private TableLayout tableLayout;
    private String currentStudentNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__grades, container, false);

        // Initialize UI components
        semesterDropdown = rootView.findViewById(R.id.role);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        // Get student number from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        if (currentStudentNumber != null) {
            setupDropdown();
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void setupDropdown() {
        // Define semester options
        String[] semesterOptions = {
                "4TH YEAR - 1ST SEMESTER",
                "4TH YEAR - 2ND SEMESTER"
        };

        // Set up ArrayAdapter for the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                semesterOptions
        );
        semesterDropdown.setAdapter(adapter);

        // Set default selection
        String defaultSelection = "4TH YEAR - 1ST SEMESTER"; // Default semester
        semesterDropdown.setText(defaultSelection, false);

        // Load grades for the default semester
        loadGrades(currentStudentNumber, "2023-2024", "FIRST");

        // Handle dropdown selection changes
        semesterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSemester = semesterOptions[position];
            String semester = selectedSemester.contains("1ST") ? "FIRST" : "SECOND";
            loadGrades(currentStudentNumber, "2023-2024", semester);
        });
    }

    private void loadGrades(String studentNumber, String schoolYear, String semester) {
        // Clear previous rows (except the header row)
        getActivity().runOnUiThread(() -> {
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1); // Keep the header row
            }
        });

        new Thread(() -> {
            try {
                // Get the connection using ConnectionClass
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                if (connection != null) {
                    // Query to get grades for the student
                    String query = "SELECT subjectCode, myGrade, units FROM enrollgradestbl WHERE studentNumber = ? AND schoolYear = ? AND semester = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, studentNumber);
                    statement.setString(2, schoolYear);
                    statement.setString(3, semester);
                    ResultSet resultSet = statement.executeQuery();

                    // Iterate through the result and populate the table
                    while (resultSet.next()) {
                        String subjectCode = resultSet.getString("subjectCode");
                        double myGrade = resultSet.getDouble("myGrade");
                        double units = resultSet.getDouble("units");

                        // Update UI on the main thread
                        getActivity().runOnUiThread(() -> addRowToTable(subjectCode, myGrade, units));
                    }



                    resultSet.close();
                    statement.close();
                    connection.close();
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Database connection failed", Toast.LENGTH_SHORT).show());
                }
            } catch (SQLException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Dynamically add rows to the table layout
// Dynamically add rows to the table layout
    private void addRowToTable(String subjectCode, Double gradeValue, Double unitsValue) {
        // Create a new table row
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Subject TextView
        TextView subjectTextView = new TextView(getContext());
        subjectTextView.setText(subjectCode);
        subjectTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        subjectTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        subjectTextView.setPadding(16, 16, 16, 16);
        subjectTextView.setTextColor(getResources().getColor(R.color.black));
        subjectTextView.setGravity(Gravity.CENTER); // Center alignment

        // Grade TextView
        TextView gradeTextView = new TextView(getContext());
        gradeTextView.setText(String.format("%.2f", gradeValue)); // Ensure grade is formatted to 2 decimal places
        gradeTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        gradeTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        gradeTextView.setPadding(16, 16, 16, 16);
        gradeTextView.setTextColor(getResources().getColor(R.color.black));
        gradeTextView.setGravity(Gravity.CENTER); // Center alignment

        // Units TextView
        TextView unitsTextView = new TextView(getContext());
        unitsTextView.setText(String.format("%.2f", unitsValue)); // Ensure units are formatted to 2 decimal places
        unitsTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        unitsTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        unitsTextView.setPadding(16, 16, 16, 16);
        unitsTextView.setTextColor(getResources().getColor(R.color.black));
        unitsTextView.setGravity(Gravity.CENTER); // Center alignment

        // Add TextViews to the row
        row.addView(subjectTextView);
        row.addView(gradeTextView);
        row.addView(unitsTextView);

        // Add row to the table layout
        tableLayout.addView(row);

        // Add a separator view
        View separator = new View(getContext());
        TableRow.LayoutParams separatorLayoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                2 // Height of the separator
        );
        separatorLayoutParams.setMargins(0, 0, 0, 16); // Optional: add some margin
        separator.setLayoutParams(separatorLayoutParams);
        separator.setBackgroundColor(getResources().getColor(R.color.gray)); // Set the color of the separator

        // Add separator to the table layout
        tableLayout.addView(separator);
    }



}
