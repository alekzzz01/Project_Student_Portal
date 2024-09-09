package com.example.studentportal;

import android.content.Intent;

import android.os.Bundle;

import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class fragment_dashboard extends Fragment {

    TextView name_header;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firebase Auth and Database reference

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        name_header = rootView.findViewById(R.id.et_Name);


        // Retrieve the current logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            loadUserName(uid);  // Load the name from Firebase Database
        }

        ImageView kebabMenu = rootView.findViewById(R.id.kebab_menu);
        kebabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        return rootView;
    }




    // Method to load the user's name from Firebase Database
    private void loadUserName(String uid) {
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);

                    // Set the name in the header TextView
                    if (firstName != null && lastName != null) {
                        name_header.setText(firstName + " " + lastName);
                    } else {
                        Toast.makeText(getActivity(), "Failed to load user name", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to show PopupMenu for announcement actions
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.kebab_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_view_details) {
                // View Announcement Details
                Toast.makeText(getActivity(), "View Announcement Details", Toast.LENGTH_SHORT).show();
                return true;
            } else if (menuItem.getItemId() == R.id.action_delete) {
                // Delete Announcement
                Toast.makeText(getActivity(), "Announcement Deleted", Toast.LENGTH_SHORT).show();
                // Implement announcement deletion logic here
                return true;
            } else if (menuItem.getItemId() == R.id.action_share) {
                // Share Announcement
                Toast.makeText(getActivity(), "Share Announcement", Toast.LENGTH_SHORT).show();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this announcement!");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

}
