package com.androidproject.univents.ui.fragments.create_edit_event_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.models.FabClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;

public class CreateEditFinishFragment extends Fragment {

    private FabClickListener listener;
    private FloatingActionButton fabFinish, fabDeleteEvent;

    public void setFabClickListener(FabClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_edit_finish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setClickListener();

    }

    private void setClickListener() {
        fabFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFabClick(null, 3);
            }
        });
        fabDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFabClick(new HashMap<String, Object>(), 3);
            }
        });
    }

    private void initViews(View view) {
        fabFinish = view.findViewById(R.id.fab_controller_create_edit_event);
        fabDeleteEvent = view.findViewById(R.id.fab_delete_event);
    }
}
