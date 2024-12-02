package com.example.studentportal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        studentemail = root.findViewById(R.id.studentemail_et);
        password = root.findViewById(R.id.password_et);
        login = root.findViewById(R.id.login_btn);

        connectionClass = new ConnectionClass();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = studentemail.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (emailAddress.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    new LoginTask().execute(emailAddress, pass);
                }
            }
        });

        return root;
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        String email, password, studentNumber;
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
                String query = "SELECT studentnumber FROM enrollpswdstudtbl WHERE studentnumber = ? AND secretdoor = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    studentNumber = rs.getString("studentNumber");
                    Log.d("LoginTask", "Login successful. studentNumber: " + studentNumber);
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
                // Save studentNumber in SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("studentNumber", studentNumber);
                editor.apply();

                // Log the saved student number for debugging
                Log.d("LoginTask", "Student Number saved: " + studentNumber);

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
