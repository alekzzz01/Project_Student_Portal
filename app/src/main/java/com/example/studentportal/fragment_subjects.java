package com.example.studentportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

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

import com.mikhaellopez.circularimageview.CircularImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class fragment_subjects extends Fragment {

    TextView studentnumber, nameheader;
    CircularImageView profileImage;
    AutoCompleteTextView dropdown;
    TableLayout tableLayout;
    private String currentStudentNumber;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_subjects, container, false);

        // Initialize UI components
        dropdown = rootView.findViewById(R.id.role);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        if (currentStudentNumber != null) {
            // Student number successfully retrieved
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        // Populate dropdown and set selection listener
        setupDropdown();

        return rootView;
    }

    private void setupDropdown() {
        // Data options for database queries (year and semester)
        String[][] dataOptions = {
                {"2021-2022", "FIRST"},
                {"2021-2022", "SECOND"},
                {"2022-2023", "FIRST"},
                {"2022-2023", "SECOND"},
                {"2023-2024", "FIRST"},
                {"2023-2024", "SECOND"},
                {"2024-2025", "FIRST"},
                {"2024-2025", "SECOND"}
        };

        // Display options with year labels
        String[] displayOptions = {
                "1ST YEAR - 1ST SEMESTER",
                "1ST YEAR - 2ND SEMESTER",
                "2ND YEAR - 1ST SEMESTER",
                "2ND YEAR - 2ND SEMESTER",
                "3RD YEAR - 1ST SEMESTER",
                "3RD YEAR - 2ND SEMESTER",
                "4TH YEAR - 1ST SEMESTER",
                "4TH YEAR - 2ND SEMESTER"
        };

        // ArrayAdapter to populate the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                displayOptions
        );

        dropdown.setAdapter(adapter);

        // Set default selection (1ST YEAR - 1ST SEMESTER)
        int defaultIndex = 0; // Index for "1ST YEAR - 1ST SEMESTER" in chronological order
        dropdown.setText(displayOptions[defaultIndex], false);

        // Load subjects for the default selection
        loadSubjects(dataOptions[defaultIndex][0], dataOptions[defaultIndex][1]);

        // Handle dropdown selection
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            // Get data corresponding to the selected display option
            String year = dataOptions[position][0];
            String semester = dataOptions[position][1];

            // Load subjects for the selected school year and semester
            loadSubjects(year, semester);
        });
    }

    private void loadSubjects(String year, String semester) {
        // Clear previous rows (except the header row)
        getActivity().runOnUiThread(() -> {
            int childCount = tableLayout.getChildCount();
            if (childCount > 1) {
                tableLayout.removeViews(1, childCount - 1); // Remove all rows except header
                Log.e("Table", "Cleared previous rows");
            }
        });

        new Thread(() -> {
            try {
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Complex query joining multiple tables to get the enrolled subjects for the student
                String query = "SELECT s.subjectCode, s.subjectTitle FROM enrollscheduletbl es " +
                              "JOIN enrollsubjectenrolled ee ON es.schedcode = ee.schedcode " +
                              "JOIN enrollsubjectstbl s ON es.subjectCode = s.subjectCode " +
                              "WHERE ee.studentNumber = ? AND ee.schoolYear = ? AND ee.semester = ?";

                Log.e("SQL Query", "Executing query: " + query +
                      " with studentNumber = " + currentStudentNumber +
                      ", schoolYear = " + year +
                      ", semester = " + semester);

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, currentStudentNumber);
                statement.setString(2, year);
                statement.setString(3, semester);
                ResultSet resultSet = statement.executeQuery();

                // Log the result count
                int resultCount = 0;

                while (resultSet.next()) {
                    resultCount++;
                    String subjectCode = resultSet.getString("subjectCode");
                    String subjectTitle = resultSet.getString("subjectTitle");
                    Log.e("Subject Data", "Subject found: " + subjectCode + " - " + subjectTitle);

                    // Add row to the table on the main thread
                    getActivity().runOnUiThread(() -> addTableRow(subjectCode, subjectTitle));
                }

                // Log the number of results found
                Log.e("SQL Query", "Number of subjects found: " + resultCount);

                if (resultCount == 0) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "No subjects found for the selected semester", Toast.LENGTH_SHORT).show();
                    });
                }

                resultSet.close();
                statement.close();
                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() -> {
                    Log.e("Database Error", "Error: " + e.getMessage());
                    Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void addTableRow(String subjectCode, String subjectTitle) {
        TableRow row = new TableRow(getActivity());

        // Set padding for the row
        row.setPadding(0, 10, 0, 10); // Increased vertical padding for better spacing
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create and set the Subject Code TextView
        TextView subjectCodeView = new TextView(getActivity());
        subjectCodeView.setText(subjectCode);
        subjectCodeView.setPadding(20, 20, 20, 20); // Add padding for better readability
        subjectCodeView.setGravity(Gravity.CENTER);  // Center text horizontally
        subjectCodeView.setTextSize(16);  // Set text size for readability
        subjectCodeView.setTextColor(getResources().getColor(R.color.black));
        subjectCodeView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));  // Equal space distribution

        // Create and set the Subject Title TextView
        TextView subjectTitleView = new TextView(getActivity());
        subjectTitleView.setText(subjectTitle);
        subjectTitleView.setPadding(20, 20, 20, 20); // Add padding for better readability
        subjectTitleView.setGravity(Gravity.CENTER);  // Center text horizontally
        subjectTitleView.setTextColor(getResources().getColor(R.color.black));
        subjectTitleView.setTextSize(16);  // Set text size for readability
        subjectTitleView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));  // Subject title column gets more space

        // Add both TextViews to the row
        row.addView(subjectCodeView);
        row.addView(subjectTitleView);

        // Create a separator view
        View separator = new View(getActivity());
        separator.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2 // Separator height
        ));
        separator.setBackgroundColor(getResources().getColor(R.color.gray)); // Set separator color

        // Add the row and the separator to the TableLayout
        getActivity().runOnUiThread(() -> {
            tableLayout.addView(row);
            tableLayout.addView(separator); // Add the separator below the row

            Log.e("Table", "Row added: " + subjectCode + " - " + subjectTitle);

            // Log the number of rows (excluding separators)
            Log.e("Table", "Total rows: " + (tableLayout.getChildCount() / 2));
        });
    }

}
