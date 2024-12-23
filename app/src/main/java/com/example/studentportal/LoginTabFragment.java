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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginTabFragment extends Fragment {

    EditText studentemail, password;
    Button login;
    ConnectionClass connectionClass;
    AutoCompleteTextView role;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_tab_fragment, container, false);

        studentemail = root.findViewById(R.id.studentemail_et);
        password = root.findViewById(R.id.password_et);
        login = root.findViewById(R.id.login_btn);
        role = root.findViewById(R.id.role);

        String[] roles = getResources().getStringArray(R.array.roles_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, roles);
        role.setAdapter(adapter);

        role.setText(roles[0], false);

        connectionClass = new ConnectionClass();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = studentemail.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String selectedRole = role.getText().toString().trim();

                if (emailAddress.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    new LoginTask(selectedRole).execute(emailAddress, pass);
                }
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
                    query = "SELECT username FROM enrollvisitortbl WHERE username = ? AND password = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, email);
                    stmt.setString(2, password);
                } else {
                    query = "SELECT studentnumber FROM enrollpswdstudtbl WHERE studentnumber = ? AND secretdoor = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, email);
                    stmt.setString(2, password);
                }

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    if (role.equalsIgnoreCase("Visitor")) {
                        studentNumber = rs.getString("username"); // Save username for visitors
                    } else {
                        studentNumber = rs.getString("studentnumber"); // Save student number for students
                    }
                    return true;
                } else {
                    errorMessage = "Invalid email or password";
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
