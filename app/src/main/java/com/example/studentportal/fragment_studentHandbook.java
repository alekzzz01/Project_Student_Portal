package com.example.studentportal;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class fragment_studentHandbook extends Fragment {

    Button button_pdf;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_studenthandbook, container, false);

        button_pdf = rootView.findViewById(R.id.button2);
        button_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PdfVIewer_Activity.class);
                // intent.putExtra("pdfFileName", "sample1.pdf"); // passing the pdffile to the pdfviewer
                startActivity(intent);

            }
        });

        return rootView;
    }
}