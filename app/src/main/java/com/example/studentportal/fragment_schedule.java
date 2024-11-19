package com.example.studentportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class fragment_schedule extends Fragment {

    TextView studentnumberr, nameheader;
    CircularImageView profileImage;
    private String currentStudentNumber; // Store current student number

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Initialize UI components
        studentnumberr = rootView.findViewById(R.id.studentnumber);
        nameheader = rootView.findViewById(R.id.et_Name);
        profileImage = rootView.findViewById(R.id.profilepicture);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        if (currentStudentNumber != null) {
            studentnumberr.setText(currentStudentNumber); // Set the student number in the TextView
            loadUserName(currentStudentNumber); // Load the name from database
            loadStudentSchedule(currentStudentNumber); // Load the student's schedule
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void loadUserName(String currentStudentNumber) {
        new Thread(() -> {
            try {
                // Get the connection using ConnectionClass
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Query the enrollstudentinformation table
                String query = "SELECT firstName, lastName FROM enrollstudentinformation WHERE studentNumber = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, currentStudentNumber);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");

                    // Update the name on the main thread (UI thread)
                    getActivity().runOnUiThread(() -> nameheader.setText(firstName + " " + lastName));
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

    private void loadStudentSchedule(String currentStudentNumber) {
        new Thread(() -> {
            try {
                // Establish database connection
                ConnectionClass connectionClass = new ConnectionClass();
                Connection connection = connectionClass.CONN();

                // Query 1: Get schedcodes from enrollsubjectenrolled table
                String queryEnroll = "SELECT schedcode FROM enrollsubjectenrolled WHERE studentnumber = ?";
                PreparedStatement statement1 = connection.prepareStatement(queryEnroll);
                statement1.setString(1, currentStudentNumber);
                ResultSet resultSet1 = statement1.executeQuery();

                ArrayList<String> schedCodes = new ArrayList<>();

                // Collect all schedcodes
                while (resultSet1.next()) {
                    schedCodes.add(resultSet1.getString("schedcode"));
                }

                resultSet1.close();
                statement1.close();

                if (!schedCodes.isEmpty()) {
                    // Query 2: Fetch schedule details using schedcodes
                    String querySchedule = "SELECT subjectCode, instructor, timein1, room1 FROM enrollscheduletbl WHERE schedcode = ?";
                    PreparedStatement statement2 = connection.prepareStatement(querySchedule);

                    for (String schedCode : schedCodes) {
                        statement2.setString(1, schedCode);
                        ResultSet resultSet2 = statement2.executeQuery();

                        while (resultSet2.next()) {
                            String subject = resultSet2.getString("subjectCode");
                            String days = resultSet2.getString("instructor");
                            String time = resultSet2.getString("timein1");
                            String room = resultSet2.getString("room1");

                            // Update the UI on the main thread
                            getActivity().runOnUiThread(() -> addScheduleRow(subject, days, time, room));
                        }
                        resultSet2.close();
                    }

                    statement2.close();
                } else {
                    // Notify user if no schedule found
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "No schedules found for this student.", Toast.LENGTH_SHORT).show());
                }

                connection.close();

            } catch (SQLException e) {
                // Handle SQL exceptions
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void addScheduleRow(String subject, String instructor, String time, String room) {
        // Reference the TableLayout
        TableLayout tableLayout = getView().findViewById(R.id.tableLayout);

        // Create a new TableRow
        TableRow tableRow = new TableRow(getActivity());
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tableRow.setPadding(10, 10, 10, 10); // Add padding for the row
        tableRow.setBackgroundColor(getResources().getColor(R.color.white));

        // Create TextViews for each column with proper alignment and weight
        TextView subjectView = createTextView(subject, 1f);
        TextView instructorView = createTextView(instructor, 2f);
        TextView timeView = createTextView(time, 1f);
        TextView roomView = createTextView(room, 1f);

        // Add TextViews to the TableRow
        tableRow.addView(subjectView);
        tableRow.addView(instructorView);
        tableRow.addView(timeView);
        tableRow.addView(roomView);

        // Add TableRow to TableLayout
        tableLayout.addView(tableRow);
    }

    private TextView createTextView(String text, float weight) {
        TextView textView = new TextView(getActivity());
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, // Use 0 width for weight distribution
                TableRow.LayoutParams.WRAP_CONTENT,
                weight);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(10, 10, 10, 10); // Add padding for the text
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(14);
        return textView;
    }

}
