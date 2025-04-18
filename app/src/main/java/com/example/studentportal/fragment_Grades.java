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
        // Define semester options for all years
        String[] semesterOptions = {
                "1ST YEAR - 1ST SEMESTER", "1ST YEAR - 2ND SEMESTER",
                "2ND YEAR - 1ST SEMESTER", "2ND YEAR - 2ND SEMESTER",
                "3RD YEAR - 1ST SEMESTER", "3RD YEAR - 2ND SEMESTER",
                "4TH YEAR - 1ST SEMESTER", "4TH YEAR - 2ND SEMESTER"
        };

        // Set up ArrayAdapter for the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                semesterOptions
        );
        semesterDropdown.setAdapter(adapter);

        // Set default selection to 1ST YEAR - 1ST SEMESTER
        String defaultSelection = "1ST YEAR - 1ST SEMESTER";
        semesterDropdown.setText(defaultSelection, false);

        // Map academic years to school years (assuming current is 2023-2024 for 4th year)
        // This will calculate backward for earlier years
        String fourthYearSchoolYear = "2024-2025";
        String thirdYearSchoolYear = "2023-2024";
        String secondYearSchoolYear = "2022-2023";
        String firstYearSchoolYear = "2021-2022";

        // Load grades for the default semester (1st year, 1st semester)
        loadGrades(currentStudentNumber, firstYearSchoolYear, "FIRST");

        // Handle dropdown selection changes
        semesterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = semesterOptions[position];
            String schoolYear;
            String semester;

            // Determine the school year based on the selected year
            if (selectedOption.contains("1ST YEAR")) {
                schoolYear = firstYearSchoolYear;
            } else if (selectedOption.contains("2ND YEAR")) {
                schoolYear = secondYearSchoolYear;
            } else if (selectedOption.contains("3RD YEAR")) {
                schoolYear = thirdYearSchoolYear;
            } else { // 4TH YEAR
                schoolYear = fourthYearSchoolYear;
            }

            // Determine which semester was selected
            semester = selectedOption.contains("1ST SEMESTER") ? "FIRST" : "SECOND";

            // Load the grades for the selected year and semester
            loadGrades(currentStudentNumber, schoolYear, semester);
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

                    // Flag to track if any grades were found
                    boolean gradesFound = false;

                    // Iterate through the result and populate the table
                    while (resultSet.next()) {
                        gradesFound = true;
                        String subjectCode = resultSet.getString("subjectCode");
                        String myGradeStr = resultSet.getString("myGrade");
                        String unitsStr = resultSet.getString("units");

                        // Check if the grade and units are numeric
                        if (isNumeric(unitsStr)) {
                            double units = Double.parseDouble(unitsStr);

                            // Handle numeric grades
                            if (isNumeric(myGradeStr)) {
                                double myGrade = Double.parseDouble(myGradeStr);
                                getActivity().runOnUiThread(() -> addRowToTable(subjectCode, myGrade, units));
                            } else {
                                // Handle non-numeric grades (e.g., "INC", "DRP")
                                getActivity().runOnUiThread(() -> addRowToTable(subjectCode, myGradeStr, units));
                            }
                        } else {
                            // Handle non-numeric units (unlikely but for completeness)
                            getActivity().runOnUiThread(() -> addRowToTable(subjectCode, myGradeStr, unitsStr));
                        }
                    }

                    // If no grades were found, display a message
                    if (!gradesFound) {
                        getActivity().runOnUiThread(() -> {
                            displayNoGradesMessage();
                            Toast.makeText(getActivity(), "Grades not yet uploaded", Toast.LENGTH_SHORT).show();
                        });
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

    // Method to display a message when no grades are found
    private void displayNoGradesMessage() {
        // Create a new table row
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Message TextView spans all columns
        TextView messageTextView = new TextView(getContext());
        messageTextView.setText("Grades not yet uploaded");
        messageTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3f);
        params.span = 3; // Span across all 3 columns
        messageTextView.setLayoutParams(params);
        messageTextView.setPadding(16, 32, 16, 32);
        messageTextView.setTextColor(getResources().getColor(R.color.black));
        messageTextView.setGravity(Gravity.CENTER);
        messageTextView.setTextSize(16);

        // Add TextView to the row
        row.addView(messageTextView);

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

    // Helper method to check if a string is numeric
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Dynamically add rows to the table layout
    private void addRowToTable(String subjectCode, Object gradeValue, Object unitsValue) {
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
        gradeTextView.setText(gradeValue.toString()); // Display grade as string
        gradeTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        gradeTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        gradeTextView.setPadding(16, 16, 16, 16);
        gradeTextView.setTextColor(getResources().getColor(R.color.black));
        gradeTextView.setGravity(Gravity.CENTER); // Center alignment

        // Units TextView
        TextView unitsTextView = new TextView(getContext());
        unitsTextView.setText(unitsValue.toString()); // Display units as string
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
