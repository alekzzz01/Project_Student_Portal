package com.example.studentportal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import androidx.fragment.app.Fragment;





public class fragment_schedule extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        LinearLayout fishery, it, managementdep, teacher;
        fishery = rootView.findViewById(R.id.fisheries);
        it = rootView.findViewById(R.id.informationtech);
        managementdep = rootView.findViewById(R.id.management);
        teacher = rootView.findViewById(R.id.teachereducation);


        fishery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/143URic-hrfG0yJxcXmEhqEWvHkuGYyke"));
                startActivity(browserIntent);
            }
        });

        it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1WtW17hLRsur7PEK40RXv5SdPnpLxIXO-"));
                startActivity(browserIntent);
            }
        });

        managementdep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1H3YVphCNUNhg5lbGV9pIQmDXQz9_L82f"));
                startActivity(browserIntent);
            }
        });

        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1lA8ibcEyqk9GtE9JQUKrj8jNxkQyt0DO"));
                startActivity(browserIntent);
            }
        });

        return rootView;
    }
}
