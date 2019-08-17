package com.androidproject.univents.main_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.androidproject.univents.R;
import com.androidproject.univents.ShowEventActivity;
import com.androidproject.univents.customviews.EventItem;
import com.androidproject.univents.customviews.EventItemGridAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db;

    private FloatingActionButton fabNewEvent;
    private EditText txtCurrentLocation;

    private GridView gridViewHomeEvents;
    private EventItemGridAdapter adapter;
    private List<EventItem> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFireBase();
        initViews(view);
        getData();
    }

    /**
     * initializes fireBase tools
     */
    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
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

        txtCurrentLocation = view.findViewById(R.id.txt_current_location);

        gridViewHomeEvents = view.findViewById(R.id.grid_view_home_events);
        adapter = new EventItemGridAdapter(getActivity(),  items);
        gridViewHomeEvents.setAdapter(adapter);
        gridViewHomeEvents.setSelector(android.R.color.transparent);
        gridViewHomeEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventItem item = (EventItem) parent.getItemAtPosition(position);
                goToShowEventActivity(item);
            }
        });
    }

    /**
     * retrieves the data from firebase and creates a "EventItem" for every document
     * and adds it ot the item-list.
     */
    private void getData() {
        String city = txtCurrentLocation.getText().toString();
        db.collection(getString(R.string.KEY_FB_EVENTS)).whereEqualTo("city", city)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                items.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    EventItem item = document.toObject(EventItem.class);
                    items.add(item);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * opens the ShowEventActivity with the event-item as intent-extra
     * @param item selected event
     */
    private void goToShowEventActivity(EventItem item) {
        Intent showEventIntent = new Intent(getActivity(), ShowEventActivity.class);
        showEventIntent.putExtra(getString(R.string.event_id), item.getEventID());
        startActivity(showEventIntent);
    }

    //TODO: Add Intent to Activity
    private void goToNewEventActivity() {

    }
}
