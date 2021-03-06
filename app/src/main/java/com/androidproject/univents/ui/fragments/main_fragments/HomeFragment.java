package com.androidproject.univents.ui.fragments.main_fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidproject.univents.ui.MainActivity;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.ui.ShowEventActivity;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.controller.EventItemGridAdapter;
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

/**
 * Fragment for the Home-Page of the app, where events of the selected city are displayed
 */
public class HomeFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_LOCATION = 122;
    private FirebaseFirestore db;

    private EditText txtCurrentLocation;
    private ImageButton btnSearchLocation;
    private ScrollView scrollView;

    private GridView gridViewHomeEvents;
    private EventItemGridAdapter adapter;
    private List<EventItem> items = new ArrayList<>();

    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private SwipeRefreshLayout swipeRefreshLayout;

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
     * initializes FireBase tools
     */
    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initFlProviderClient() {
        flProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    /**
     * initializes the shared-preferences and the editor
     */
    private void initPreferences() {
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    /**
     * Initializes all views from the layout
     * @param view includes the layout from the fragment.
     */
    private void initViews(View view) {
        initLocationEditText(view);
        initGridView(view);
        initGetLocationButton(view);
        initSwipeRefresh(view);
        scrollView = view.findViewById(R.id.scroll_view);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == 0) {
                    ((MainActivity)getActivity()).setToolbarElevation(0.0f);
                } else {
                    ((MainActivity)getActivity()).setToolbarElevation(7.0f);
                }
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
     * gets all necessary permissions
     */
    private void getPermissions() {
        if (!checkLocationPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
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
                        } else {
                            showToast("Es konnte kein Standort abgerufen werden .Gib deinen Standort manuell ein.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Es konnte kein Standort abgerufen werden .Gib deinen Standort manuell ein.");
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
    /**
     * opens the ShowEventActivity with the event-item as intent-extra
     * @param item selected event
     */
    private void goToShowEventActivity(EventItem item) {
        Intent showEventIntent = new Intent(getActivity(), ShowEventActivity.class);
        showEventIntent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), item.getEventId());
        startActivity(showEventIntent);
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
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast(getString(R.string.permssion_granted));
                receiveLocation();
            } else {
                showToast(getString(R.string.permssion_granted));
            }
        }
    }

    private void showToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
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
