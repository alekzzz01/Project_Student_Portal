package com.example.studentportal;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
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
    private ImageView[] dots;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ImageView calendar, grades, schedule;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize Firebase Auth and Database reference

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        name_header = rootView.findViewById(R.id.et_Name);
        calendar = rootView.findViewById(R.id.calendar);
        grades = rootView.findViewById(R.id.grades);
        schedule = rootView.findViewById(R.id.schedule);

        // Retrieve the current logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            loadUserName(uid);  // Load the name from Firebase Database
        }

        // Initialize the HorizontalScrollView and its child LinearLayout
        HorizontalScrollView horizontalScrollView = rootView.findViewById(R.id.horizontalScrollView2);
        LinearLayout cardContainer = rootView.findViewById(R.id.cardContainer);

        // Get the number of CardViews in the container
        int numberOfDots = cardContainer.getChildCount(); // Automatically counts the number of CardView children

        // Initialize the dots layout
        LinearLayout dotsLayout = rootView.findViewById(R.id.dotsLayout);
        dots = new ImageView[numberOfDots];

        // Add the dots to the layout
        for (int i = 0; i < numberOfDots; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inactive_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dots[i], params);
        }

        // Set the first dot as active
        if (dots.length > 0) {
            dots[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
        }

        // Set the scroll listener to update dots based on scroll position
        horizontalScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // Calculate the current position
                int totalScrollRange = horizontalScrollView.getChildAt(0).getWidth() - horizontalScrollView.getWidth();
                int currentPosition = (int) (((float) scrollX / totalScrollRange) * (numberOfDots - 1));

                // Update dots
                updateDots(currentPosition);
            }
        });




        return rootView;
    }


    private void updateDots(int currentPosition) {
        for (int i = 0; i < dots.length; i++) {
            if (i == currentPosition) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inactive_dot));
            }
        }
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
}
