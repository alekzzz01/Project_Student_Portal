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

        forgotpass.setOnClickListener(v ->
                Toast.makeText(getContext(), "Please proceed to MIS for assistance", Toast.LENGTH_SHORT).show()
        );

        login.setOnClickListener(v -> {
            String emailAddress = studentemail.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (emailAddress.isEmpty() || pass.isEmpty()) {
                studentemail.setBackgroundResource(emailAddress.isEmpty() ? R.drawable.red_border : 0);
                password.setBackgroundResource(pass.isEmpty() ? R.drawable.red_border : 0);
                Toast.makeText(getContext(), "Both fields are required", Toast.LENGTH_SHORT).show();
            } else {
                new LoginTask("Student").execute(emailAddress, pass);
            }
        });

        visitorButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("role", "Visitor");
            editor.putBoolean("isLoggedIn", true);
            editor.apply();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return root;
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        String email, password, studentNumber, role;
        String errorMessage = "";

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
                String query = "SELECT studentnumber FROM enrollpswdstudtbl WHERE studentnumber = ? AND secretdoor = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    studentNumber = rs.getString("studentnumber");
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
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("studentNumber", studentNumber);
                editor.putString("role", role);
                editor.putBoolean("isLoggedIn", true);
                editor.apply();

                Toast.makeText(getActivity(), "Login Successful as " + role, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
