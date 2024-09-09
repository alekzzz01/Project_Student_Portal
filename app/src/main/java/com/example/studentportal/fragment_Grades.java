package com.example.studentportal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class fragment_Grades extends Fragment {

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    private TextView etName;
    private TextView studentNumber;

    String[] item = {"2023-2024(3rd Year - Second Sem)", "2024-2025(4th Year - First Sem)"};

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__grades, container, false);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        autoCompleteTextView = rootView.findViewById(R.id.auto_complete_text);
        etName = rootView.findViewById(R.id.et_Name);
        studentNumber = rootView.findViewById(R.id.studentnumber);

        // Set up AutoCompleteTextView
        adapterItems = new ArrayAdapter<>(getContext(), R.layout.list_item, item);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getContext(), "Item: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch and set user data
        fetchUserData();

        return rootView;
    }

    private void fetchUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get current user's ID
            DatabaseReference userRef = dbReference.child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String studentNumberStr = dataSnapshot.child("studentNumber").getValue(String.class); // Ensure this field exists

                        // Update TextViews with user data
                        etName.setText(firstName + " " + lastName);
                        studentNumber.setText(studentNumberStr);
                    } else {
                        // Handle case where user data is not found
                        Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors
                    Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle case where the user is not logged in
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
