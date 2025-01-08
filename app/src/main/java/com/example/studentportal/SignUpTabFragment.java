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
    EditText email;
    EditText phone;
    Button signupbutton;

    ConnectionClass connectionClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_fragment, container, false);

        connectionClass = new ConnectionClass();

        studentnumber = root.findViewById(R.id.studentnumber_et);
        email = root.findViewById(R.id.email_et);
        phone = root.findViewById(R.id.phone_et);
        password = root.findViewById(R.id.password_et);
        retypePassword = root.findViewById(R.id.confirmpassword_et);
        signupbutton = root.findViewById(R.id.button);

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentNum = studentnumber.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String rePass = retypePassword.getText().toString().trim();
                String email1 = email.getText().toString().trim();
                String phone1 = phone.getText().toString().trim();

                if (email1.isEmpty() || phone1.isEmpty() || pass.isEmpty() || rePass.isEmpty() || studentNum.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                } else if (!pass.equals(rePass)) {
                    Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    verifyAndRegisterStudent(studentNum, pass, email1, phone1);
                }
            }
        });

        return root;
    }

    private void verifyAndRegisterStudent(final String studentNum, final String password, final String email, final String phone) {
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
                        String insertQuery = "INSERT INTO enrollpswdstudtbl (studentnumber, secretdoor, emailaddress, phonenumber, registerdate) " +
                                "VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                        insertStmt.setString(1, studentNum);
                        insertStmt.setString(2, password);
                        insertStmt.setString(3, email);
                        insertStmt.setString(4, phone);

                        // Set current date for registerdate
                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        insertStmt.setString(5, currentDateTime);

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
