package com.androidproject.univents.main_fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.ShowEventActivity;
import com.androidproject.univents.customviews.EventItem;
import com.androidproject.univents.customviews.EventItemGridAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_LOCATION = 122;
    private FirebaseFirestore db;

    private FloatingActionButton fabNewEvent;
    private EditText txtCurrentLocation;

    private GridView gridViewHomeEvents;
    private EventItemGridAdapter adapter;
    private List<EventItem> items = new ArrayList<>();

    //Location things
    private FusedLocationProviderClient flProviderClient;

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
        initFlProviderClient();
        initViews(view);
        getPermissions();
        receiveLocation();
    }

    //requests permissions.
    private void getPermissions() {
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }

    /**
     * gets the current location and sets the current city name
     * to the specified editText Field.
     */
    private void receiveLocation() {
        if (!checkLocationPermission()) {
            return;
        }
        flProviderClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Geocoder geocoder = new Geocoder(getActivity());
                            try {
                                Address address = geocoder.getFromLocation(latitude, longitude, 1).get(0);
                                txtCurrentLocation.setText(address.getLocality());
                            } catch (IOException e) {
                                e.printStackTrace();
                                showToast(getString(R.string.cannot_encode_address));
                                txtCurrentLocation.setText(getString(R.string.regensburg));
                            }
                            getData();
                        }
                    }
                });
    }

    private void initFlProviderClient() {
        flProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
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
        //TODO Save entered city in preferences.
        txtCurrentLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    getData();
                }
                return false;
            }
        });

        gridViewHomeEvents = view.findViewById(R.id.grid_view_home_events);
        adapter = new EventItemGridAdapter(getActivity(),  items);
        gridViewHomeEvents.setAdapter(adapter);
        gridViewHomeEvents.setSelector(android.R.color.transparent);
        gridViewHomeEvents.setEmptyView(view.findViewById(R.id.tv_empty_list_view));
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
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).whereEqualTo(getString(R.string.KEY_FIREBASE_EVENT_CITY), city)
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

    private void showToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }

    /**
     * opens the ShowEventActivity with the event-item as intent-extra
     * @param item selected event
     */
    private void goToShowEventActivity(EventItem item) {
        Intent showEventIntent = new Intent(getActivity(), ShowEventActivity.class);
        showEventIntent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), item.getEventId());
        startActivity(showEventIntent);
    }

    //TODO: Add Intent to Activity
    private void goToNewEventActivity() {

    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    receiveLocation();
                } else {
                    txtCurrentLocation.setText(getString(R.string.regensburg));
                }
                return;
            }
        }
    }
}
