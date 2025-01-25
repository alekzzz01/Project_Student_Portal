package com.example.studentportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.sql.ResultSet;

public class fragment_Usermanagement extends Fragment {
    TextView name, studentnumber, address, bday;
    EditText oldpass, newpass, retypepass;
    Button changebtn;
    private String currentStudentNumber;
    ConnectionClass connectionClass;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment__usermanagement, container, false);

        name = rootview.findViewById(R.id.et_Name);
        studentnumber = rootview.findViewById(R.id.et_studentnumber);
        address = rootview.findViewById(R.id.et_address);
        bday = rootview.findViewById(R.id.et_birthday);

        oldpass = rootview.findViewById(R.id.oldPassword);
        newpass = rootview.findViewById(R.id.newPassword);
        retypepass = rootview.findViewById(R.id.retypePassword);

        changebtn = rootview.findViewById(R.id.update_btn);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentStudentNumber = sharedPreferences.getString("studentNumber", null);

        connectionClass = new ConnectionClass();

        // Fetch user data from the database
        new FetchUserDataTask().execute(currentStudentNumber);

        // Handle password update button click
        changebtn.setOnClickListener(v -> {
            String oldPassword = oldpass.getText().toString().trim();
            String newPassword = newpass.getText().toString().trim();
            String retypePassword = retypepass.getText().toString().trim();

            boolean hasError = false;

            if (oldPassword.isEmpty()) {
                oldpass.setBackgroundResource(R.drawable.red_border);
                Toast.makeText(getActivity(), "Old Password is required", Toast.LENGTH_SHORT).show();
                hasError = true;
            } else {
                oldpass.setBackgroundResource(0); // Reset background
            }

            if (newPassword.isEmpty()) {
                newpass.setBackgroundResource(R.drawable.red_border);
                Toast.makeText(getActivity(), "New Password is required", Toast.LENGTH_SHORT).show();
                hasError = true;
            } else {
                newpass.setBackgroundResource(0); // Reset background
            }

            if (retypePassword.isEmpty()) {
                retypepass.setBackgroundResource(R.drawable.red_border);
                Toast.makeText(getActivity(), "Retype Password is required", Toast.LENGTH_SHORT).show();
                hasError = true;
            } else {
                retypepass.setBackgroundResource(0); // Reset background
            }

            if (!newPassword.equals(retypePassword)) {
                retypepass.setBackgroundResource(R.drawable.red_border);
                Toast.makeText(getActivity(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                hasError = true;
            } else if (!hasError) {
                retypepass.setBackgroundResource(0); // Reset background
            }

            if (!hasError) {
                new UpdatePasswordTask().execute(oldPassword, newPassword);
            }
        });

        return rootview;
    }

    private class FetchUserDataTask extends AsyncTask<String, Void, Boolean> {
        String firstName, lastName, middleName, dateOfBirth, errorMessage, formattedAddress;

        @Override
        protected Boolean doInBackground(String... params) {
            String studentNumber = params[0];

            Connection conn = connectionClass.CONN();
            if (conn == null) {
                errorMessage = "Error in connection with SQL server";
                return false;
            }

            try {
                String query = "SELECT firstName, lastName, middleName, dateOfBirth, street, barangay, municipality FROM enrollstudentinformation WHERE studentNumber = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, studentNumber);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    firstName = rs.getString("firstName");
                    lastName = rs.getString("lastName");
                    middleName = rs.getString("middleName");

                    // Retrieve and format the date
                    Date dbDate = rs.getDate("dateOfBirth");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateOfBirth = dateFormat.format(dbDate);

                    // Retrieve address components and format them with line breaks
                    String street = rs.getString("street");
                    String barangay = rs.getString("barangay");
                    String municipality = rs.getString("municipality");

                    formattedAddress = String.format("%s\n%s\n%s", street, barangay, municipality);

                    return true;
                } else {
                    errorMessage = "No data found for this student number";
                    return false;
                }
            } catch (Exception e) {
                errorMessage = "Error: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                name.setText(String.format("%s %s %s", firstName, middleName, lastName));
                studentnumber.setText(currentStudentNumber);
                bday.setText(dateOfBirth);
                address.setText(formattedAddress);
            } else {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdatePasswordTask extends AsyncTask<String, Void, Boolean> {
        String errorMessage;

        @Override
        protected Boolean doInBackground(String... params) {
            String oldPassword = params[0];
            String newPassword = params[1];

            Connection conn = connectionClass.CONN();
            if (conn == null) {
                errorMessage = "Error in connection with SQL server";
                return false;
            }

            try {
                // Verify old password
                String query = "SELECT secretdoor FROM enrollpswdstudtbl WHERE studentNumber = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, currentStudentNumber);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("secretdoor");
                    if (!dbPassword.equals(oldPassword)) {
                        errorMessage = "Old password is incorrect";
                        return false;
                    }
                } else {
                    errorMessage = "Student number not found";
                    return false;
                }

                // Update to new password
                String updateQuery = "UPDATE enrollpswdstudtbl SET secretdoor = ? WHERE studentNumber = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, currentStudentNumber);

                int rowsUpdated = updateStmt.executeUpdate();
                return rowsUpdated > 0;

            } catch (Exception e) {
                errorMessage = "Error: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                oldpass.setText("");
                newpass.setText("");
                retypepass.setText("");
            } else {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

