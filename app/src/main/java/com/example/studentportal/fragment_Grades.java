package com.example.studentportal;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class fragment_Grades extends Fragment {

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private TableLayout tableLayout;

    private TextView etName;
    private TextView studentNumber;

    String[] schoolYears = {"2023-2024(3rd Year - Second Sem)", "2024-2025(4th Year - First Sem)"};

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__grades, container, false);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference("users");
        insertSampleGrade();

        // Initialize views
        autoCompleteTextView = rootView.findViewById(R.id.auto_complete_text);
        etName = rootView.findViewById(R.id.et_Name);
        studentNumber = rootView.findViewById(R.id.studentnumber);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        // Set up AutoCompleteTextView
        adapterItems = new ArrayAdapter<>(getContext(), R.layout.list_item, schoolYears);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();

                // Fetch grades for the selected semester
                fetchGradesForSelectedSemester(selectedItem);
            }
        });

        // Fetch and set user data
        fetchUserData();

        // Populate table with initial grades
        fetchGradesForSelectedSemester(schoolYears[0]);

        return rootView;
    }

    private void fetchUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = dbReference.child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String studentNumberStr = dataSnapshot.child("studentNumber").getValue(String.class);

                        // Update TextViews with user data
                        etName.setText(firstName + " " + lastName);
                        studentNumber.setText(studentNumberStr);
                    } else {
                        Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchGradesForSelectedSemester(String semester) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference gradesRef = dbReference.child(userId).child("grades").child(semester);

            gradesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Clear existing rows in the table (except header)
                    tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subjectId = subjectSnapshot.getKey();
                        Double gradeValue = subjectSnapshot.getValue(Double.class);

                        // Add each subject and grade to the table
                        addRowToTable(subjectId, gradeValue);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error fetching grades", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRowToTable(String subjectId, Double gradeValue) {
        // Create a new table row
        TableRow row = new TableRow(getContext());

        // Set layout parameters for the row to make the cells fill the row properly
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutParams);

        // Create and set text views for subject and grade
        TextView subjectTextView = new TextView(getContext());
        subjectTextView.setText(subjectId);
        subjectTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium)); // Apply PoppinsMedium
        subjectTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        subjectTextView.setPadding(150, 16, 16, 16);
        subjectTextView.setTextColor(getResources().getColor(R.color.black));
        subjectTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START); // Align left

        TextView gradeTextView = new TextView(getContext());
        gradeTextView.setText(String.valueOf(gradeValue));
        gradeTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsmedium)); // Apply PoppinsMedium
        gradeTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        gradeTextView.setPadding(16, 16, 170, 16);
        gradeTextView.setTextColor(getResources().getColor(R.color.black));
        gradeTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END); // Align right

        // Add views to the row
        row.addView(subjectTextView);
        row.addView(gradeTextView);

        // Add row to the table layout
        tableLayout.addView(row);
    }


    private void insertSampleGrade() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference gradesRef = dbReference.child(userId).child("grades").child("2023-2024(3rd Year - Second Sem)");

            // Create a sample grade entry
            Map<String, Object> sampleGrade = new HashMap<>();
            sampleGrade.put("Elective 3", 1.75);
            sampleGrade.put("Networking", 2.25);
            sampleGrade.put("Data Algorithm", 1.50);
            sampleGrade.put("Programming", 1.20);
            sampleGrade.put("Database", 1.0);

            // Push the sample grade to the database
            gradesRef.updateChildren(sampleGrade).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Sample grades inserted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to insert sample grades", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }



}
