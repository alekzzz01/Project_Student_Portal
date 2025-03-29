// In fragment_dashboard.java

package com.example.studentportal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class fragment_dashboard extends Fragment {

    TextView name_header, newgrade;
    private String currentStudentNumber; // Store current student number
    private String userRole; // Store the user's role
    Button incomplete, conditional, dropped, failed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        name_header = rootView.findViewById(R.id.et_Name);

        incomplete = rootView.findViewById(R.id.btn_incomplete);
        conditional = rootView.findViewById(R.id.btn_conditional);
        dropped = rootView.findViewById(R.id.btn_dropped);
        failed = rootView.findViewById(R.id.btn_failed);

        // Retrieve the user role and student number from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userRole = sharedPreferences.getString("role", "Visitor");
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        // Log the retrieved user role and student number for debugging
        Log.d("FragmentDashboard", "Retrieved role: " + userRole);
        Log.d("FragmentDashboard", "Retrieved student number: " + currentStudentNumber);

        // Check the role and redirect if the user is a Visitor
        if (userRole.equalsIgnoreCase("Visitor")) {
            // Redirect to fragment_Map
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.navHostFragment, new fragment_Map())
                    .commit();
            return rootView;
        }

        // Load the student's name and grade from the database if the role is not Visitor
        if (currentStudentNumber != null) {
            loadUserName(currentStudentNumber);
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        incomplete.setOnClickListener(v -> executeQuery("incomplete"));
        conditional.setOnClickListener(v -> executeQuery("conditional"));
        dropped.setOnClickListener(v -> executeQuery("dropped"));
        failed.setOnClickListener(v -> executeQuery("failed"));

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
                    getActivity().runOnUiThread(() -> name_header.setText(firstName + " " + lastName));
                }

                nameResultSet.close();
                nameStatement.close();
                connection.close();

            } catch (SQLException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // In fragment_dashboard.java

    private void executeQuery(String queryType) {
        new Thread(() -> {
            try {
                // Get the connection using ConnectionClass
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                String query = "SELECT e.*, es.instructor FROM enrollgradestbl e " +
                        "LEFT JOIN enrollscheduletbl es ON e.subjectcode = es.subjectcode " +
                        "WHERE e.studentnumber = ?";
                String dialogTitle = "";
                switch (queryType) {
                    case "incomplete":
                        query += " AND e.mygrade = 'INC'";
                        dialogTitle = "Incomplete";
                        break;
                    case "conditional":
                        query += " AND e.mygrade = '4.0'";
                        dialogTitle = "Conditional";
                        break;
                    case "dropped":
                        query += " AND e.mygrade = 'DRP'";
                        dialogTitle = "Dropped";
                        break;
                    case "failed":
                        query += " AND e.mygrade = '5.0'";
                        dialogTitle = "Failed";
                        break;
                }

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, currentStudentNumber);
                ResultSet resultSet = statement.executeQuery();

                // Process the result set and store the data
                List<Map<String, String>> data = new ArrayList<>();
                while (resultSet.next()) {
                    String grade = resultSet.getString("mygrade");
                    String units = resultSet.getString("units");

                    // Check if the grade is numeric and greater than the units for the "failed" query
                    if (!queryType.equals("failed") || (isNumeric(grade) && Double.parseDouble(grade) > Double.parseDouble(units))) {
                        Map<String, String> row = new HashMap<>();
                        row.put("schoolYear", resultSet.getString("schoolyear"));
                        row.put("semester", resultSet.getString("semester"));
                        row.put("subjectCode", resultSet.getString("subjectcode"));
                        row.put("units", units);
                        row.put("grade", grade);

                        // Get the description from enrollsubjecttbl
                        String subjectCode = resultSet.getString("subjectcode");
                        String descriptionQuery = "SELECT description FROM enrollsubjectstbl WHERE subjectCode = ?";
                        PreparedStatement descriptionStatement = connection.prepareStatement(descriptionQuery);
                        descriptionStatement.setString(1, subjectCode);
                        ResultSet descriptionResultSet = descriptionStatement.executeQuery();
                        if (descriptionResultSet.next()) {
                            row.put("description", descriptionResultSet.getString("description"));
                        }
                        descriptionResultSet.close();
                        descriptionStatement.close();

                        // Get instructor from enrollscheduletbl
                        String instructor = resultSet.getString("instructor");
                        if (instructor != null && !instructor.isEmpty()) {
                            row.put("instructor", instructor);
                        } else {
                            // Fallback to schedcode for non-failed cases
                            row.put("instructor", queryType.equals("failed") ? "No instructor found" : resultSet.getString("schedcode"));
                        }

                        if (!queryType.equals("failed")) {
                            row.put("remarks", resultSet.getString("graded"));
                        }

                        data.add(row);
                    }
                }

                resultSet.close();
                statement.close();
                connection.close();

                // Show the dialog with the retrieved data
                String finalDialogTitle = dialogTitle;
                getActivity().runOnUiThread(() -> showDialog(data, finalDialogTitle));
            } catch (SQLException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
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

    // Method to show the dialog with the retrieved data
    private void showDialog(List<Map<String, String>> data, String dialogTitle) {
        // Inflate the dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);

        // Set the dialog title
        TextView titleView = dialogView.findViewById(R.id.dialog);
        titleView.setText(dialogTitle);

        // Find the TableLayout in the dialog layout
        TableLayout tableLayout = dialogView.findViewById(R.id.tableLayout1);

        // Clear any existing rows
        tableLayout.removeAllViews();

        // Iterate through the data and add rows to the table
        for (Map<String, String> row : data) {
            // Add title rows (green background)
            addTitleRow(tableLayout, "School Year");
            addDataRow(tableLayout, row.get("schoolYear"));

            addTitleRow(tableLayout, "Semester");
            addDataRow(tableLayout, row.get("semester"));

            addTitleRow(tableLayout, "Subject Code");
            addDataRow(tableLayout, row.get("subjectCode"));

            addTitleRow(tableLayout, "Description");
            addDataRow(tableLayout, row.get("description"));

            addTitleRow(tableLayout, "Grade");
            addDataRow(tableLayout, row.get("grade"));

            addTitleRow(tableLayout, "Units");
            addDataRow(tableLayout, row.get("units"));

            addTitleRow(tableLayout, "Instructor");
            addDataRow(tableLayout, row.get("instructor"));
        }

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void addTitleRow(TableLayout tableLayout, String title) {
        TableRow titleRow = new TableRow(getContext());
        titleRow.setBackgroundColor(getResources().getColor(R.color.header_background));

        TextView titleTextView = new TextView(getContext());
        titleTextView.setText(title);
        titleTextView.setPadding(10, 10, 10, 10);
        titleTextView.setTextColor(getResources().getColor(R.color.white));
        titleTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        titleRow.addView(titleTextView);
        tableLayout.addView(titleRow);
    }

    private void addDataRow(TableLayout tableLayout, String data) {
        TableRow dataRow = new TableRow(getContext());
        dataRow.setBackgroundColor(getResources().getColor(R.color.white));

        TextView dataTextView = new TextView(getContext());
        dataTextView.setText(data);
        dataTextView.setPadding(10, 10, 10, 10);
        dataTextView.setTextColor(getResources().getColor(R.color.black));
        dataTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        dataRow.addView(dataTextView);
        tableLayout.addView(dataRow);
    }
}