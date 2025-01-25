package com.example.studentportal;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpTabFragment extends Fragment {

    EditText studentnumber;
    EditText password;
    EditText retypePassword;
    Button signupbutton;

    ConnectionClass connectionClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_fragment, container, false);

        connectionClass = new ConnectionClass();

        studentnumber = root.findViewById(R.id.studentnumber_et);
        password = root.findViewById(R.id.password_et);
        retypePassword = root.findViewById(R.id.confirmpassword_et);
        signupbutton = root.findViewById(R.id.button);

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentNum = studentnumber.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String rePass = retypePassword.getText().toString().trim();

                boolean hasError = false;

                if (studentNum.isEmpty() && pass.isEmpty()) {
                    studentnumber.setBackgroundResource(R.drawable.red_border);
                    password.setBackgroundResource(R.drawable.red_border);
                    Toast.makeText(getActivity(), "Student Number and Password is required", Toast.LENGTH_SHORT).show();
                    hasError = true;
                } else {
                    if (studentNum.isEmpty()) {
                        studentnumber.setBackgroundResource(R.drawable.red_border);
                        Toast.makeText(getActivity(), "Student Number is required", Toast.LENGTH_SHORT).show();
                        hasError = true;
                    } else {
                        studentnumber.setBackgroundResource(0); // Reset background
                    }

                    if (pass.isEmpty()) {
                        password.setBackgroundResource(R.drawable.red_border);
                        Toast.makeText(getActivity(), "Password is required", Toast.LENGTH_SHORT).show();
                        hasError = true;
                    } else {
                        password.setBackgroundResource(0); // Reset background
                    }
                }

                if (rePass.isEmpty()) {
                    retypePassword.setBackgroundResource(R.drawable.red_border);
                    Toast.makeText(getActivity(), "Retype Password is required", Toast.LENGTH_SHORT).show();
                    hasError = true;
                } else {
                    retypePassword.setBackgroundResource(0); // Reset background
                }

                if (!pass.equals(rePass)) {
                    retypePassword.setBackgroundResource(R.drawable.red_border);
                    Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    hasError = true;
                } else if (!hasError) {
                    retypePassword.setBackgroundResource(0); // Reset background
                }

                if (!hasError) {
                    verifyAndRegisterStudent(studentNum, pass);
                }
            }
        });

        return root;
    }

    private void verifyAndRegisterStudent(final String studentNum, final String password) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                Connection conn = connectionClass.CONN();
                if (conn == null) {
                    return "Connection Error";
                }

                try {
                    // Check if student number exists in enrollstudentinformation
                    String checkQuery = "SELECT * FROM enrollstudentinformation WHERE studentNumber = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setString(1, studentNum);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        // Insert into enrollpswdstudtbl
                        String insertQuery = "INSERT INTO enrollpswdstudtbl (studentnumber, secretdoor, registerdate) " +
                                "VALUES (?, ?, ?)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                        insertStmt.setString(1, studentNum);
                        insertStmt.setString(2, password);

                        // Set current date for registerdate
                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        insertStmt.setString(3, currentDateTime);

                        insertStmt.executeUpdate();

                        insertStmt.close();
                        rs.close();
                        checkStmt.close();
                        conn.close();




                        return "Registration Successful";


                    } else {
                        rs.close();
                        checkStmt.close();
                        conn.close();
                        return "Student Number not found";
                    }
                } catch (Exception e) {
                    Log.e("Database", "Error: " + e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
