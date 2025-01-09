package com.example.studentportal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginTabFragment extends Fragment {

    EditText studentemail, password;
    Button login, visitorButton;
    TextView forgotpass;
    ConnectionClass connectionClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_tab_fragment, container, false);

        studentemail = root.findViewById(R.id.studentemail_et);
        password = root.findViewById(R.id.password_et);
        login = root.findViewById(R.id.login_btn);
        visitorButton = root.findViewById(R.id.visitor_btn);
        forgotpass = root.findViewById(R.id.forgotpassword);

        connectionClass = new ConnectionClass();

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Please proceed to MIS for assistance", Toast.LENGTH_SHORT).show();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = studentemail.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (emailAddress.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Assume student role for login
                    new LoginTask("Student").execute(emailAddress, pass);
                }
            }
        });

        visitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Directly redirect to MainActivity when "Login as Guest" is clicked
                Toast.makeText(getActivity(), "Logged in as Visitor", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish(); // Finish current activity
            }
        });

        return root;
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        String email, password, studentNumber, role;
        String errorMessage = "";

        // Constructor to accept the role
        public LoginTask(String role) {
            this.role = role;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            email = params[0];
            password = params[1];

            Connection conn = connectionClass.CONN();
            if (conn == null) {
                errorMessage = "Error in connection with SQL server";
                return false;
            }

            try {
                String query;
                PreparedStatement stmt;

                if (role.equalsIgnoreCase("Visitor")) {
                    // Visitor logic is no longer needed here
                    return true; // Allow direct access for visitors
                } else { // Assuming role is "Student"
                    query = "SELECT studentnumber FROM enrollpswdstudtbl WHERE studentnumber = ? AND secretdoor = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, email);
                    stmt.setString(2, password);
                }

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    studentNumber = rs.getString("studentnumber"); // Save student number for students
                    return true; // Login successful
                } else {
                    errorMessage = "Invalid email or password";
                    return false; // Login failed
                }
            } catch (Exception e) {
                errorMessage = "Error: " + e.getMessage();
                return false; // Exception occurred
            }

        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Save login session details in SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("studentNumber", studentNumber);
                editor.putString("role", role); // Save selected role
                editor.apply();

                Toast.makeText(getActivity(), "Login Successful as " + role, Toast.LENGTH_SHORT).show();

                // Redirect to MainActivity
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
