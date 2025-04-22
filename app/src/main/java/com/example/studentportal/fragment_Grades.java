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

                    // Variables for GWA calculation
                    double totalPoints = 0;
                    double totalUnits = 0;

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

                                // Add to GWA calculation
                                totalPoints += myGrade * units;
                                totalUnits += units;
                            } else {
                                // Handle non-numeric grades (e.g., "INC", "DRP")
                                getActivity().runOnUiThread(() -> addRowToTable(subjectCode, myGradeStr, units));
                            }
                        } else {
                            // Handle non-numeric units (unlikely but for completeness)
                            getActivity().runOnUiThread(() -> addRowToTable(subjectCode, myGradeStr, unitsStr));
                        }
                    }

                    // Calculate and display GWA if grades were found
                    final double gwa = gradesFound && totalUnits > 0 ? totalPoints / totalUnits : 0;
                    final boolean hasGrades = gradesFound;
                    final double finalTotalUnits = totalUnits;

                    // If no grades were found, display a message
                    if (!gradesFound) {
                        getActivity().runOnUiThread(() -> {
                            displayNoGradesMessage();
                            Toast.makeText(getActivity(), "Grades not yet uploaded", Toast.LENGTH_SHORT).show();
                        });
                    } else if (totalUnits > 0) {
                        // Display GWA
                        getActivity().runOnUiThread(() -> displayGWA(gwa, finalTotalUnits));
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

    // Method to display the GWA at the bottom of the table
    private void displayGWA(double gwa, double totalUnits) {
        // Add some spacing
        View spacer = new View(getContext());
        spacer.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                30 // Height of the spacer
        ));
        tableLayout.addView(spacer);

        // Create container for GWA display
        TableRow gwaContainer = new TableRow(getContext());
        gwaContainer.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        gwaContainer.setBackgroundColor(getResources().getColor(R.color.green)); // Green background
        gwaContainer.setPadding(0, 16, 0, 16);

        // GWA Label
        TextView gwaLabel = new TextView(getContext());
        gwaLabel.setText("GWA:");
        gwaLabel.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsbold));
        gwaLabel.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f));
        gwaLabel.setPadding(32, 16, 16, 16);
        gwaLabel.setTextColor(getResources().getColor(R.color.white));
        gwaLabel.setTextSize(16);
        gwaLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        // GWA Value
        TextView gwaValue = new TextView(getContext());
        gwaValue.setText(String.format("%.2f", gwa));
        gwaValue.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsbold));
        gwaValue.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        gwaValue.setPadding(16, 16, 32, 16);
        gwaValue.setTextColor(getResources(). getColor(R.color.white)); // Yellow text for GWA value
        gwaValue.setTextSize(18);
        gwaValue.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        // Add views to row
        gwaContainer.addView(gwaLabel);
        gwaContainer.addView(gwaValue);

        // Add row to table
        tableLayout.addView(gwaContainer);

        // Add total units row
        TableRow unitsContainer = new TableRow(getContext());
        unitsContainer.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        unitsContainer.setBackgroundColor(getResources().getColor(R.color.yellow)); // Light yellow background
        unitsContainer.setPadding(0, 0, 0, 16);

        // Units Label
        TextView unitsLabel = new TextView(getContext());
        unitsLabel.setText("Total Units:");
        unitsLabel.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        unitsLabel.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f));
        unitsLabel.setPadding(32, 8, 16, 16);
        unitsLabel.setTextColor(getResources().getColor(R.color.black)); // Black text for readability
        unitsLabel.setTextSize(14);
        unitsLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        // Units Value
        TextView unitsValue = new TextView(getContext());
        unitsValue.setText(String.format("%.1f", totalUnits));
        unitsValue.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium));
        unitsValue.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        unitsValue.setPadding(16, 8, 32, 16);
        unitsValue.setTextColor(getResources().getColor(R.color.black)); // Black text for readability
        unitsValue.setTextSize(14);
        unitsValue.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        // Add views to row
        unitsContainer.addView(unitsLabel);
        unitsContainer.addView(unitsValue);

        // Add row to table
        tableLayout.addView(unitsContainer);
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