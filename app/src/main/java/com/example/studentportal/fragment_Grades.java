package com.example.studentportal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

public class fragment_Grades extends Fragment {

    String[] item = {"2023-2024(3rd Year - Second Sem)", "2024-2025(4th Year - First Sem)"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment__grades, container, false);

        autoCompleteTextView = rootView.findViewById(R.id.auto_complete_text);
        adapterItems = new ArrayAdapter<>(getContext(), R.layout.list_item, item);

        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getContext(), "Item: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
