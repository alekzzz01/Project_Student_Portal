package com.example.studentportal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpTabFragment extends Fragment  {

    EditText studentnumber;
    EditText fname;
    EditText mname;
    EditText lname;
    EditText email;
    EditText password;
    Button signupbutton;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        studentnumber = root.findViewById(R.id.studentnumber_et);
        fname = root.findViewById(R.id.firstname_et);
        mname = root.findViewById(R.id.middlename_et);
        lname = root.findViewById(R.id.lastname_et);
        email = root.findViewById(R.id.email_et);
        password = root.findViewById(R.id.password_et);
        signupbutton = root.findViewById(R.id.button);

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentNum = studentnumber.getText().toString().trim();
                String firstName = fname.getText().toString().trim();
                String middleName = mname.getText().toString().trim();
                String lastName = lname.getText().toString().trim();
                String emailAddress = email.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (studentNum.isEmpty() || firstName.isEmpty() || middleName.isEmpty() || lastName.isEmpty() || emailAddress.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(studentNum, firstName, middleName, lastName, emailAddress, pass);
                }
            }
        });

        return root;

    }


    private void registerUser(String studentNum, String firstName, String middleName, String lastName, String emailAddress, String password) {
        mAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), studentNum, firstName, middleName, lastName, emailAddress);
                            sendVerificationEmail(user);
                        }
                        Toast.makeText(getActivity(), "Registration Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String studentNum, String firstName, String middleName, String lastName, String emailAddress) {
        Map<String, String> user = new HashMap<>();
        user.put("studentNumber", studentNum);
        user.put("firstName", firstName);
        user.put("middleName", middleName);
        user.put("lastName", lastName);
        user.put("email", emailAddress);

        mDatabase.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       Toast.makeText(getActivity(), "User data saved successfully", Toast.LENGTH_SHORT).show();
                   } else {
                       Toast.makeText(getActivity(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                   }
                });


    }

    private void sendVerificationEmail(FirebaseUser user){
        user.sendEmailVerification().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               Toast.makeText(getActivity(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
           } else {
               Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
           }
        });
    }
}



