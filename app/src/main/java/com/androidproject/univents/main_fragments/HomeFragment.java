package com.androidproject.univents.main_fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidproject.univents.R;

public class HomeFragment extends Fragment {

    private FloatingActionButton fabNewEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    /**
     * Initializes all views from the layout
     * @param view includes the layout from the fragment.
     */
    private void initViews(View view) {
        fabNewEvent = view.findViewById(R.id.fab_home_new_event);
        fabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNewEventActivity();
            }
        });
    }

    //TODO: Add Intent to Activity
    private void goToNewEventActivity() {

    }
}
