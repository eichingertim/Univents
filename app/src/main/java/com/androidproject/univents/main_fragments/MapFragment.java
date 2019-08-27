package com.androidproject.univents.main_fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidproject.univents.R;
import com.androidproject.univents.ShowEventActivity;
import com.androidproject.univents.customviews.EventItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnInfoWindowClickListener {

    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";

    private MapView mapView;

    private FirebaseFirestore db;

    private List<EventItem> allEvents = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getActivity(), "pk.eyJ1IjoidHRqYXBwcHJvamVjdCIsImEiOiJjano1c2NnOGIwNXU3M2RuMGZ3ZXJ0cWJvIn0.dSrsfqwzCei9HvcRDZxwiA");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initMap(view, savedInstanceState);
        initFireBase();
        
    }

    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * initializes the mapView and adds a callback
     * @param view fragment layout
     * @param savedInstanceState saved state
     */
    private void initMap(View view, Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(getStyle(), new Style.OnStyleLoaded() {

            @Override
            public void onStyleLoaded(@NonNull Style style) {
                getDataAndAddMarkers(mapboxMap);
            }
        });
    }

    private void getDataAndAddMarkers(final MapboxMap mapboxMap) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    EventItem item = doc.toObject(EventItem.class);
                    allEvents.add(item);
                    LatLng position = new LatLng(item.getEventExactLocation().getLatitude()
                            , item.getEventExactLocation().getLongitude());
                    mapboxMap.addMarker(new MarkerOptions().position(position)
                            .setSnippet(item.getEventId())
                            .setTitle(item.getEventTitle()));
                }
            }
        });
        mapboxMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public boolean onInfoWindowClick(@NonNull Marker marker) {
        Intent showEventIntent = new Intent(getActivity(), ShowEventActivity.class);
        showEventIntent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), marker.getSnippet());
        startActivity(showEventIntent);
        return true;
    }

    private String getStyle() {
        if (isDarkTheme()) return Style.DARK;
        else return Style.LIGHT;
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}
