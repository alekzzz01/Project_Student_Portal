package com.example.studentportal;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class fragment_campus_calendar extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
  

        View rootView = inflater.inflate(R.layout.fragment_campus_calendar, container, false);


        calendarView = rootView.findViewById(R.id.calendarView);
        recyclerView = rootView.findViewById(R.id.recyclerViewEvents);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Sample data for RecyclerView
        eventList = new ArrayList<>();
        eventList.add(new Event("New Year's Day", "January 1"));
        eventList.add(new Event("Bonifacio Day", "December 30"));
        eventList.add(new Event("Assumption of Mary", "August 15"));

        EventAdapter eventAdapter = new EventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);

        // Setting up a day click listener
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();  // Get Calendar object from EventDay
            Toast.makeText(getActivity(), "Selected Date: " + clickedDay.getTime().toString(), Toast.LENGTH_SHORT).show();
        });

        return rootView;
    }
}