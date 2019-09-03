package com.androidproject.univents.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.ui.ShowEventActivity;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.models.EventItemGridAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
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
    private ImageButton btnSearchLocation;

    private GridView gridViewHomeEvents;
    private EventItemGridAdapter adapter;
    private List<EventItem> items = new ArrayList<>();

    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private SwipeRefreshLayout swipeRefreshLayout;

    //Location things
    private FusedLocationProviderClient flProviderClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFireBase();
        initFlProviderClient();
        initPreferences();
        initViews(view);
        getPermissions();
        if (!checkIfLocationSaved()) {
            receiveLocation();
        }
    }

    /**
     * initializes the preferences and the editor
     */
    private void initPreferences() {
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
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
     * checks whether the user has typed in a location before
     * @return true or false
     */
    private boolean checkIfLocationSaved() {
        String location = sharedPref.getString("currentLocation", null);
        if (location == null) {
            return false;
        } else {
            txtCurrentLocation.setText(location);
            getData();
            return true;
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
                                editor.putString("currentLocation", txtCurrentLocation.getText().toString());
                                editor.commit();
                            } catch (IOException e) {
                                e.printStackTrace();
                                showToast(getString(R.string.cannot_encode_address));
                            }
                            getData();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Es konnte kein Standort abgerufen werden");
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
        initFloatingActionButton(view);
        initLocationEditText(view);
        initGridView(view);
        initGetLocationButton(view);
        initSwipeRefresh(view);
    }

    /**
     * initializes the floating action button and sets an onClickListener
     * @param view current fragment-layout
     */
    private void initFloatingActionButton(View view) {
        fabNewEvent = view.findViewById(R.id.fab_home_new_event);
        fabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNewEventActivity();
            }
        });
    }

    /**
     * initializes the editText where the current location is displayed and sets
     * a listener for detecting when the user presses ENTER or DONE on the keyboard.
     * @param view current fragment-layout
     */
    private void initLocationEditText(View view) {
        txtCurrentLocation = view.findViewById(R.id.txt_current_location);
        txtCurrentLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    editor.putString("currentLocation", txtCurrentLocation.getText().toString());
                    editor.commit();
                    getData();
                }
                return false;
            }
        });
    }

    /**
     * initializes the getLocation-button and sets an onclickListener
     * @param view current fragment-layout
     */
    private void initGetLocationButton(View view) {
        btnSearchLocation = view.findViewById(R.id.btn_edit_location);
        btnSearchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveLocation();
            }
        });
    }

    /**
     * initializes the grid-view and its adapter and sets specific configurations
     * and an onItemClickListener.
     * @param view current fragment-layout
     */
    private void initGridView(View view) {
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
     * initializes the swipe refresh layout and sets a refresh listener
     * @param view current fragment-layout
     */
    private void initSwipeRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.home_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
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
                swipeRefreshLayout.setRefreshing(false);
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

    /**
     * checks if the Permission is already accepted.
     * @return true or false
     */
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    receiveLocation();
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_home_refresh:
                swipeRefreshLayout.setRefreshing(true);
                getData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
