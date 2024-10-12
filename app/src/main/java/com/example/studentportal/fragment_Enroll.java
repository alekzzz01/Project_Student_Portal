package com.example.studentportal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;


public class fragment_Enroll extends Fragment {

    // initial state of enrollment
    private boolean isEnrollmentOpen = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__enroll, container, false);


        // Enrollment form for showing of subject per section when click

        RadioGroup radioGroup = rootView.findViewById(R.id.radioGroup);
        TableLayout tableLayout1 = rootView.findViewById(R.id.tableLayout1);
        TableLayout tableLayout2 = rootView.findViewById(R.id.tableLayout2);


        tableLayout1.setVisibility(View.GONE);
        tableLayout2.setVisibility(View.GONE);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                tableLayout1.setVisibility(View.GONE);
                tableLayout2.setVisibility(View.GONE);


                // Show the selected TableLayout
                if (checkedId == R.id.radioButton) {
                    tableLayout1.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radioButton1) {
                    tableLayout2.setVisibility(View.VISIBLE);
                }
            }
        });


        //


        LinearLayout layoutSubjectSelection = rootView.findViewById(R.id.EnrollmentForm);
        LinearLayout layoutEnrollmentAlert = rootView.findViewById(R.id.NotYetEnrollment);

        // Check if enrollment is open and show the appropriate layout
        if (isEnrollmentOpen) {
            layoutSubjectSelection.setVisibility(View.VISIBLE);  // Show subject selection
            layoutEnrollmentAlert.setVisibility(View.GONE);       // Hide alert
        } else {
            layoutSubjectSelection.setVisibility(View.GONE);      // Hide subject selection
            layoutEnrollmentAlert.setVisibility(View.VISIBLE);    // Show alert
        }








        return rootView;




    }
}