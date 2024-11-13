package com.example.studentportal;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginTabFragment extends Fragment {

    EditText studentemail, password;
    Button login;
    ConnectionClass connectionClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_tab_fragment, container, false);

        // Initialize UI components
        studentemail = root.findViewById(R.id.studentemail_et);
        password = root.findViewById(R.id.password_et);
        login = root.findViewById(R.id.login_btn);

        // Initialize ConnectionClass instance
        connectionClass = new ConnectionClass();

        // Set OnClickListener for login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered email and password
                String emailAddress = studentemail.getText().toString().trim();
                String pass = password.getText().toString().trim();

                // Check if fields are not empty
                if (emailAddress.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Execute the login task if fields are filled
                    new LoginTask().execute(emailAddress, pass);
                }
            }
        });

        return root;
    }

    // AsyncTask to handle database login on a background thread
    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        String email, password;
        String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            email = params[0];
            password = params[1];

            Log.d("LoginTask", "Attempting to connect to database");
            Connection conn = connectionClass.CONN();
            if (conn == null) {
                errorMessage = "Error in connection with SQL server";
                Log.e("LoginTask", errorMessage);
                return false;
            }

            try {
                Log.d("LoginTask", "Connected to database. Verifying credentials...");
                String query = "SELECT * FROM enrollpswdstudtbl WHERE emailaddress = ? AND secretdoor = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Log.d("LoginTask", "Login successful");
                    return true;
                } else {
                    errorMessage = "Invalid email or password";
                    Log.e("LoginTask", errorMessage);
                    return false;
                }
            } catch (Exception e) {
                errorMessage = "Error: " + e.getMessage();
                Log.e("LoginTask", errorMessage);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_SHORT).show();
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
