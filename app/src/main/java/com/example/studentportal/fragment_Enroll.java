package com.example.studentportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class fragment_Enroll extends Fragment {

    // initial state of enrollment
    private boolean isEnrollmentOpen = true;
    TextView studentNumber, name;
    private String currentStudentNumber; // Store current student number


    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__enroll, container, false);



        // Enrollment form for showing of subject per section when click
        studentNumber = rootView.findViewById(R.id.studentnumber);
        name = rootView.findViewById(R.id.et_Name);


        RadioGroup radioGroup = rootView.findViewById(R.id.radioGroup);
        TableLayout tableLayout1 = rootView.findViewById(R.id.tableLayout1);
        TableLayout tableLayout2 = rootView.findViewById(R.id.tableLayout2);


        tableLayout1.setVisibility(View.GONE);
        tableLayout2.setVisibility(View.GONE);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                tableLayout1.setVisibility(View.GONE);
                tableLayout2.setVisibility(View.GONE);


                // Show the selected TableLayout
                if (checkedId == R.id.radioButton) {
                    tableLayout1.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radioButton1) {
                    tableLayout2.setVisibility(View.VISIBLE);
                }
            }
        });


        //


        LinearLayout layoutSubjectSelection = rootView.findViewById(R.id.EnrollmentForm);
        LinearLayout layoutEnrollmentAlert = rootView.findViewById(R.id.NotYetEnrollment);

        // Check if enrollment is open and show the appropriate layout
        if (isEnrollmentOpen) {
            layoutSubjectSelection.setVisibility(View.VISIBLE);  // Show subject selection
            layoutEnrollmentAlert.setVisibility(View.GONE);       // Hide alert
        } else {
            layoutSubjectSelection.setVisibility(View.GONE);      // Hide subject selection
            layoutEnrollmentAlert.setVisibility(View.VISIBLE);    // Show alert
        }


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        // Log the retrieved student number for debugging
        Log.d("FragmentDashboard", "Retrieved student number: " + currentStudentNumber);

        // Load the student name from database if student number is available
        if (currentStudentNumber != null) {
            loadUserName(currentStudentNumber); // Load the name from database
            studentNumber.setText(currentStudentNumber);
        } else {
            Toast.makeText(getActivity(), "Student number not found", Toast.LENGTH_SHORT).show();
        }




        return rootView;




    }

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
                    getActivity().runOnUiThread(() -> name.setText(firstName + " " + lastName));
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
}