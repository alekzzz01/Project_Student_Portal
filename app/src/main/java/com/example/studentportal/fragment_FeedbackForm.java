package com.example.studentportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class fragment_FeedbackForm extends Fragment {

    private EditText etFullName, etPurposeOfVisit, etAttendingStaff;
    private RadioGroup rgPoliteness, rgQuality, rgTimeliness;
    private Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__feedback_form, container, false);

        // Initialize UI elements
        etFullName = view.findViewById(R.id.fullname_et);
        etPurposeOfVisit = view.findViewById(R.id.purpose_et);
        etAttendingStaff = view.findViewById(R.id.staff_et);
        rgPoliteness = view.findViewById(R.id.politeness_rdgrp);
        rgQuality = view.findViewById(R.id.quality_rdgrp);
        rgTimeliness = view.findViewById(R.id.timeliness_rdgrp);
        submitButton = view.findViewById(R.id.submit_btn);

        // Handle form submission
        submitButton.setOnClickListener(v -> handleSubmit());

        return view;
    }

    private void handleSubmit() {
        String fullName = etFullName.getText().toString().trim();
        String purposeOfVisit = etPurposeOfVisit.getText().toString().trim();
        String attendingStaff = etAttendingStaff.getText().toString().trim();
        int politenessRating = getSelectedRating(rgPoliteness);
        int qualityRating = getSelectedRating(rgQuality);
        int timelinessRating = getSelectedRating(rgTimeliness);

        // Retrieve user role from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "Visitor"); // Default role is Visitor

        // Validate input
        if (fullName.isEmpty() || purposeOfVisit.isEmpty() || attendingStaff.isEmpty() ||
                politenessRating == -1 || qualityRating == -1 || timelinessRating == -1) {
            Toast.makeText(getContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert feedback into database
        new Thread(() -> {
            try (Connection connection = new ConnectionClass().CONN()) {
                if (connection != null) {
                    String query = "INSERT INTO enrollfeedbacktbl " +
                            "(full_name, purpose_of_visit, attending_staff, " +
                            "staff_politeness_rating, service_quality_rating, timeliness_rating, role) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, fullName);
                    statement.setString(2, purposeOfVisit);
                    statement.setString(3, attendingStaff);
                    statement.setInt(4, politenessRating);
                    statement.setInt(5, qualityRating);
                    statement.setInt(6, timelinessRating);
                    statement.setString(7, userRole);

                    statement.executeUpdate();

                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Database connection error.", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private int getSelectedRating(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return -1; // No selection
        }
        RadioButton selectedButton = radioGroup.findViewById(selectedId);
        String text = selectedButton.getText().toString();

        // Extract rating from text
        return Integer.parseInt(text.replaceAll("\\D", ""));
    }
}
