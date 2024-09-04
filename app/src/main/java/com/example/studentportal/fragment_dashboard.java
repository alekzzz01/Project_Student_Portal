package com.example.studentportal;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;

public class fragment_dashboard extends Fragment {

    private ImageView[] dots;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

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
}
