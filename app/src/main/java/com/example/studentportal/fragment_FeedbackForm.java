package com.example.studentportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class fragment_FeedbackForm extends Fragment {

    private EditText etFullName, etPurposeOfVisit, etAttendingStaff, etEmail, etAgency, etComment;
    private RadioGroup rgCourtesy, rgQuality, rgTimeliness, rgEfficiency, rgCleanliness, rgComfort;
    private Button submitButton;
    private AutoCompleteTextView dropdownProgram, dropdownDepartment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__feedback_form, container, false);

        // Initialize UI elements
        etFullName = view.findViewById(R.id.fullname_et);
        etPurposeOfVisit = view.findViewById(R.id.purpose_et);
        etAttendingStaff = view.findViewById(R.id.staff_et);
        etEmail = view.findViewById(R.id.email_et);
        etAgency = view.findViewById(R.id.agency_et);
        rgCourtesy = view.findViewById(R.id.courtesy_rdgrp);
        rgQuality = view.findViewById(R.id.quality_rdgrp);
        rgTimeliness = view.findViewById(R.id.timeliness_rdgrp);
        rgEfficiency = view.findViewById(R.id.efficiency_rdgrp);
        rgCleanliness = view.findViewById(R.id.cleanliness_rdgrp);
        rgComfort = view.findViewById(R.id.comfort_rdgrp);
        submitButton = view.findViewById(R.id.submit_btn);
        dropdownProgram = view.findViewById(R.id.program);
        dropdownDepartment = view.findViewById(R.id.department);
        etComment = view.findViewById(R.id.comment_et);

        // Handle form submission
        submitButton.setOnClickListener(v -> handleSubmit());

        String[] programs = {
                "Bachelor of Science in Fisheries and Aquatic Sciences (BSFAS)",
                "Bachelor of Science in Computer Science (BSCS)",
                "Bachelor of Science in Information Technology (BSIT)",
                "Bachelor of Elementary Education (BEEd)",
                "Bachelor of Secondary Education (BSEd)",
                "Bachelor of Science in Hospitality Management (BSHM)",
                "Bachelor of Science in Business Administration (BSBA)",
                "Visitor"
        };

        ArrayAdapter<String> programAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, programs);
        dropdownProgram.setAdapter(programAdapter);

        String[] departments = {
                "Fisheries and Aquatic Sciences Department",
                "Information Technology Department",
                "Teacher Education Department",
                "Management Department",
                "Visitor"
        };

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, departments);
        dropdownDepartment.setAdapter(departmentAdapter);

        return view;
    }

    private void handleSubmit() {
        String fullName = etFullName.getText().toString().trim();
        String purposeOfVisit = etPurposeOfVisit.getText().toString().trim();
        String attendingStaff = etAttendingStaff.getText().toString().trim();
        String agency = etAgency.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String selectedProgram = dropdownProgram.getText().toString();
        String selectedDepartment = dropdownDepartment.getText().toString();
        String comment = etComment.getText().toString().trim();

        int courtesyRating = getSelectedRating(rgCourtesy);
        int qualityRating = getSelectedRating(rgQuality);
        int timelinessRating = getSelectedRating(rgTimeliness);
        int efficiencyRating = getSelectedRating(rgEfficiency);
        int cleanlinessRating = getSelectedRating(rgCleanliness);
        int comfortRating = getSelectedRating(rgComfort);

        // Retrieve user role from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "Visitor"); // Default role is Visitor

        boolean hasError = false;

        // Validate input
        if (fullName.isEmpty()) {
            etFullName.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Full Name is required", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            etFullName.setBackgroundResource(0); // Reset background
        }

        if (purposeOfVisit.isEmpty()) {
            etPurposeOfVisit.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Purpose of Visit is required", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            etPurposeOfVisit.setBackgroundResource(0); // Reset background
        }

        if (attendingStaff.isEmpty()) {
            etAttendingStaff.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Attending Staff is required", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            etAttendingStaff.setBackgroundResource(0); // Reset background
        }

        if (selectedProgram.isEmpty()) {
            dropdownProgram.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Program is required", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            dropdownProgram.setBackgroundResource(0); // Reset background
        }

        if (selectedDepartment.isEmpty()) {
            dropdownDepartment.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Department is required", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            dropdownDepartment.setBackgroundResource(0); // Reset background
        }

        if (courtesyRating == -1) {
            rgCourtesy.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Please rate Courtesy", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            rgCourtesy.setBackgroundResource(0); // Reset background
        }

        if (qualityRating == -1) {
            rgQuality.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Please rate Quality", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            rgQuality.setBackgroundResource(0); // Reset background
        }

        if (timelinessRating == -1) {
            rgTimeliness.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Please rate Timeliness", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            rgTimeliness.setBackgroundResource(0); // Reset background
        }

        if (efficiencyRating == -1) {
            rgEfficiency.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Please rate Efficiency", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            rgEfficiency.setBackgroundResource(0); // Reset background
        }

        if (cleanlinessRating == -1) {
            rgCleanliness.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Please rate Cleanliness", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            rgCleanliness.setBackgroundResource(0); // Reset background
        }

        if (comfortRating == -1) {
            rgComfort.setBackgroundResource(R.drawable.red_border);
            Toast.makeText(requireContext(), "Please rate Comfort", Toast.LENGTH_SHORT).show();
            hasError = true;
        } else {
            rgComfort.setBackgroundResource(0); // Reset background
        }

        if (hasError) {
            return;
        }

        // Insert feedback into database
        new Thread(() -> {
            try (Connection connection = new ConnectionClass().CONN()) {
                if (connection != null) {
                    String query = "INSERT INTO enrollfeedbacktbl " +
                            "(full_name, purpose_of_visit, attending_staff, courtesy_rating, service_quality_rating, " +
                            "service_timeliness_rating, created_at, role, department, program, agency, email, " +
                            "service_efficiency_rating, cleanliness_rating, comfort_rating, comment) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, fullName);
                    statement.setString(2, purposeOfVisit);
                    statement.setString(3, attendingStaff);
                    statement.setInt(4, courtesyRating);
                    statement.setInt(5, qualityRating);
                    statement.setInt(6, timelinessRating);
                    statement.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis())); // Set current timestamp
                    statement.setString(8, userRole);
                    statement.setString(9, selectedDepartment);
                    statement.setString(10, selectedProgram);
                    statement.setString(11, agency);
                    statement.setString(12, email);
                    statement.setInt(13, efficiencyRating);
                    statement.setInt(14, cleanlinessRating);
                    statement.setInt(15, comfortRating);
                    statement.setString(16, comment);

                    statement.executeUpdate();
                    getActivity().runOnUiThread(() -> {
                        etFullName.setText("");
                        etPurposeOfVisit.setText("");
                        etAttendingStaff.setText("");
                        etAgency.setText("");
                        etEmail.setText("");
                        dropdownProgram.setText("");
                        dropdownDepartment.setText("");
                        rgCourtesy.clearCheck();
                        rgQuality.clearCheck();
                        rgTimeliness.clearCheck();
                        rgEfficiency.clearCheck();
                        rgCleanliness.clearCheck();
                        rgComfort.clearCheck();
                        etComment.setText("");
                        Toast.makeText(getContext(), "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    });
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