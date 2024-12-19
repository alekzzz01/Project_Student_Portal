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

                if (studentNum.isEmpty() || pass.isEmpty() || rePass.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                } else if (!pass.equals(rePass)) {
                    Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    checkStudentRegistration(studentNum, pass);
                }
            }
        });

        return root;
    }

    private void checkStudentRegistration(final String studentNum, final String password) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Connection conn = connectionClass.CONN();
                if (conn == null) {
                    Log.e("Database", "Error in connection with SQL server");
                    return false;
                }

                try {
                    String query = "SELECT * FROM enrollpswdstudtbl WHERE studentnumber = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, studentNum);

                    ResultSet rs = stmt.executeQuery();
                    boolean isRegistered = rs.next();

                    rs.close();
                    stmt.close();
                    conn.close();

                    return isRegistered;

                } catch (Exception e) {
                    Log.e("Database", "Error: " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean isRegistered) {
                if (isRegistered) {
                    Toast.makeText(getActivity(), "The student number is already registered", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "This student number is not yet registered", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}
