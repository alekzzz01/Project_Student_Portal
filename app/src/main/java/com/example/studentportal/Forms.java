package com.example.studentportal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dewakoding.androiddatatable.DataTableView;
import com.dewakoding.androiddatatable.data.OrderBy;
import com.dewakoding.androiddatatable.data.Column;
import com.dewakoding.androiddatatable.listener.OnWebViewComponentClickListener;

import java.util.ArrayList;

public class Forms extends Fragment {

    private DataTableView dtvTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forms, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find your DataTable view by its ID and cast it to DataTable
        dtvTable = view.findViewById(R.id.dtv_table); // Ensure your DataTable ID is "dtvTable"

        // Define the columns for the table
        ArrayList<Column> columns = new ArrayList<>();
        columns.add(new Column("formCode", "Form Code"));
        columns.add(new Column("formName", "Form Name"));
        columns.add(new Column("department", "Department"));

        // Populate the table with some data
        ArrayList<Form> listData = getFormData();

        // Use the library's OrderBy class for sorting
        OrderBy orderBy = new OrderBy(0, "DESC");

        // Set the table with configurations
        dtvTable.setTable(
                columns,
                listData,
                true, // Show action button
                orderBy, // Sort by first column in descending order
                50, // Pagination length
                true // Disable searching
        );

        // Handle row click events
        dtvTable.setOnClickListener(new OnWebViewComponentClickListener() {
            @Override
            public void onRowClicked(String dataStr) {
                // Display a Toast message with the form code
                Form userClicked = parseDataStr(dataStr);
                String formCode = userClicked.getFormCode();
                Toast.makeText(getContext(), formCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Static method to get form data
    private ArrayList<Form> getFormData() {
        ArrayList<Form> listData = new ArrayList<>();
        listData.add(new Form("F001", "Application Form", "HR"));
        listData.add(new Form("F002", "Leave Request", "Finance"));
        listData.add(new Form("F003", "Expense Report", "Admin"));
        listData.add(new Form("F004", "Travel Request", "IT"));
        listData.add(new Form("F005", "Purchase Order", "Procurement"));
        return listData;
    }

    // Parse the data string to a Form object
    private Form parseDataStr(String dataStr) {
        // Implement your own parsing logic here
        // For this example, we'll create a placeholder Form object
        // Adjust the parsing logic based on how dataStr is formatted
        return new Form("F001", "Application Form", "HR"); // Placeholder
    }

    // Static inner class for form data
    public static class Form {
        private String formCode;
        private String formName;
        private String department;

        public Form(String formCode, String formName, String department) {
            this.formCode = formCode;
            this.formName = formName;
            this.department = department;
        }

        public String getFormCode() {
            return formCode;
        }

        public String getFormName() {
            return formName;
        }

        public String getDepartment() {
            return department;
        }
    }
}
