package com.example.studentportal;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dewakoding.androiddatatable.DataTableView;
import com.dewakoding.androiddatatable.data.OrderBy;
import com.dewakoding.androiddatatable.data.Column;
import com.dewakoding.androiddatatable.listener.OnWebViewComponentClickListener;

import org.json.JSONException;
import org.json.JSONObject;

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
        columns.add(new Column("formName", "Form Name: "));
        columns.add(new Column("department", "Department: "));

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
// Handle row click events
        dtvTable.setOnClickListener(new OnWebViewComponentClickListener() {
            @Override
            public void onRowClicked(String dataStr) {
                Form userClicked = parseDataStr(dataStr);
                if (userClicked != null) {
                    String formUrl = userClicked.getFormUrl();
                    downloadForm(formUrl);
                } else {
                    Toast.makeText(getContext(), "Error retrieving form data", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Static method to get form data
    private ArrayList<Form> getFormData() {
        ArrayList<Form> listData = new ArrayList<>();
        listData.add(new Form("OSAS-QF-13", "Needs Assessment Form", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-13%20Needs%20Assessment%20Form.docx.pdf?alt=media&token=87692890-eb29-4a5b-8a9b-236506ef96e1"));
        listData.add(new Form("OSAS-QF-19", "Application Form for Recognition and Accreditation of Student Organization", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-19%20Application%20Form%20for%20Recognition%20and%20Accreditation%20of%20Student%20Organization.docx.pdf?alt=media&token=ae37d450-6815-4246-9cc4-d9f950b5ad07"));
        listData.add(new Form("OSAS-QF-20", "Checklist of Requirements", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-20%20Checklist%20of%20Requirements.docx.pdf?alt=media&token=ad2155be-f773-483d-a5b3-d5e8e4c97586"));
        listData.add(new Form("OSAS-QF-23", "Activity Proposal Format", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-23%20Activity%20Proposal%20Format.docx.pdf?alt=media&token=8fae4563-18ef-4edb-b59d-c1383db3d587"));
        listData.add(new Form("OSAS-QF-25-CAMPUS", "Parent Guardian/Permit Consent", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-25%20Parent_Guardian%20Permit_Consent%20(CAMPUS).docx.pdf?alt=media&token=40218e8e-57bc-48c7-8f44-fb5dbce269cd"));
        listData.add(new Form("OSAS-QF-25-OFF-CAMPUS", "Parent Guardian/Permit Consent", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-25%20Parent_Guardian%20Permit_Consent%20OFF%20CAMPUS.docx.pdf?alt=media&token=b44dc025-b00c-4876-a641-66f7450ec3aa"));
        listData.add(new Form("OSAS-QF-26", "Permit to Withdraw", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-26%20Permit%20to%20Withdraw.docx.pdf?alt=media&token=b85466db-0a84-4cf2-b4af-0585680eea37"));
        listData.add(new Form("OSAS-QF-27", "Application Form of Filling of Candidacy", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-27%20Application%20Form%20for%20Filing%20of%20Candidacy.docx.pdf?alt=media&token=b643369c-17a9-4224-9003-99059497ace6"));
        listData.add(new Form("OSAS-QF-40", "Survey Form on Adequacy and Efficiency of CvSU Student Services and Facilities", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-40%20Survey%20Form%20on%20Adequacy%20and%20Efficiency%20of%20CvSU%20Student%20Services%20and%20Facilities.docx.pdf?alt=media&token=b0415863-d56c-4006-aab1-7b87116d538d"));
        listData.add(new Form("OSAS-QF-41", "Request Form for Certificate of Good Moral Character", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-41%20Request%20Form%20for%20Certificate%20of%20Good%20Moral%20Character.docx.pdf?alt=media&token=b94e1ca5-e0ca-4fbf-aeac-e2694f3d13be"));
        listData.add(new Form("OSAS-QF-44", "Incident Report Form", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FOSAS-QF-44%20Incident%20Report%20Form.docx.pdf?alt=media&token=da23d281-f8ca-4f07-aa12-889ad331e29b"));
        listData.add(new Form("QF-9", "Adding, Changing and Dropping Form", "Registrar", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FQF-9-ADDING-CHANGING-AND-DROPPING-FORM.pdf?alt=media&token=00457292-3ccd-4eaf-a103-a5bfa2b04c6d"));
        listData.add(new Form("STUDENT-CF", "Student Clearance Form", "OSAS", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FStudent%20Clearance.docx.pdf?alt=media&token=d58bd8de-6a5a-4b39-bf19-c1bd303069b1"));
        listData.add(new Form("UREG-CF", "Completion Form", "Registrar", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FCOMPLETION-FORM.pdf?alt=media&token=26b945e0-fc01-4873-b3aa-55dd3ae7806f"));
        listData.add(new Form("UREG-QF-10", "Request for Withdrawal of Registration", "Registrar", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FUREG-QF-10-Request-for-Withdrawal-of-Registration.pdf?alt=media&token=24844f63-5cce-4afa-9b93-49476af1db85"));
        listData.add(new Form("UREG-QF-11", "Application Form for Leave of Absence", "Registrar", "https://firebasestorage.googleapis.com/v0/b/cvsu-de0cf.appspot.com/o/downloadable_forms%2FUREG-QF-11-Application-Form-for-Leave-of-Absence.pdf?alt=media&token=29dbd760-2393-4250-8a1c-febfae7d3ef6"));


        return listData;
    }

    // Parse the data string to a Form object
    private Form parseDataStr(String dataStr) {
        try {
            // Assuming dataStr is in JSON format
            JSONObject jsonObject = new JSONObject(dataStr);
            String formCode = jsonObject.getString("formCode");
            String formName = jsonObject.getString("formName");
            String department = jsonObject.getString("department");
            String formUrl = jsonObject.getString("formUrl");
            return new Form(formCode, formName, department, formUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // Handle this appropriately in your code
        }
    }


    private void downloadForm(String formUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(formUrl));
        request.setTitle("Downloading Form");
        request.setDescription("Downloading form from Firebase Storage");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "form.pdf"); // Change the file name as needed

        DownloadManager downloadManager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(getContext(), "Download started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Download manager not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Static inner class for form data
    public static class Form {
        private String formCode;
        private String formName;
        private String department;
        private String formUrl;

        public Form(String formCode, String formName, String department, String formUrl) {
            this.formCode = formCode;
            this.formName = formName;
            this.department = department;
            this.formUrl = formUrl;
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

        public String getFormUrl() { return formUrl; }
    }
}
