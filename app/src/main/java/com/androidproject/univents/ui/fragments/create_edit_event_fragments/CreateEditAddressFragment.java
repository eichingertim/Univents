package com.androidproject.univents.ui.fragments.create_edit_event_fragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.models.FabClickListener;
import com.androidproject.univents.ui.SelectLocationActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class CreateEditAddressFragment extends Fragment {

    private FirebaseFirestore db;

    private Button btnSelectLocation;
    private EditText txtExactLocation, txtCity, txtLocationDetails;
    private FloatingActionButton fabContinue;

    private FabClickListener listener;

    private EventItem event;
    private String eventId;

    public void setFabClickListener(FabClickListener listener) {
        this.listener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_edit_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        initViews(view);
        setClickListener();
        if (getArguments() != null) {
            eventId = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
            getData();
        }

    }

    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                event = documentSnapshot.toObject(EventItem.class);
                fillDataToViews();
            }
        });
    }

    private void fillDataToViews() {
        String exactLocation = event.getEventExactLocation().getLatitude() + ","
                + event.getEventExactLocation().getLongitude();
        txtExactLocation.setText(exactLocation);
        txtCity.setText(event.getEventCity());
        txtLocationDetails.setText(event.getEventDetailLocation());
    }

    private void setClickListener() {
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLocation();
            }
        });
        fabContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidInput()) {
                    listener.onFabClick(getDataMap(), 1);
                }
            }
        });
    }

    private Map<String, Object> getDataMap() {

        Map<String, Object> map = new HashMap<>();

        String[] coordinates = txtExactLocation.getText().toString().split(",");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);

        map.put(getString(R.string.KEY_FIREBASE_EVENT_EXACT_LOCATION)
                , new GeoPoint(latitude, longitude));
        map.put(getString(R.string.KEY_FIREBASE_EVENT_CITY)
                , txtCity.getText().toString());
        map.put(getString(R.string.KEY_FIREBASE_EVENT_DETAIL_LOCATION)
                , txtLocationDetails.getText().toString());

        return map;

    }

    private boolean checkValidInput() {

        if (txtCity.getText().toString().equals("")
                || txtExactLocation.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Du musst eine Location auswählen"
                    , Toast.LENGTH_LONG).show();
            return false;
        } else if (txtLocationDetails.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Du musst deine Location noch genauer beschreiben"
                    , Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void initViews(View view) {
        fabContinue = view.findViewById(R.id.fab_controller_create_edit_event);

        txtExactLocation = view.findViewById(R.id.txt_create_edit_event_coordinates);
        txtCity = view.findViewById(R.id.txt_create_edit_event_city);
        txtLocationDetails = view.findViewById(R.id.txt_create_edit_event_location_details);

        btnSelectLocation = view.findViewById(R.id.btnSelectLocation);

    }

    private void selectLocation() {
        Intent intent = new Intent(getActivity(), SelectLocationActivity.class);
        startActivityForResult(intent, 901);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 901 && resultCode == RESULT_OK && data != null) {
            double latitude = (double) data.getExtras().get("latitude");
            double longitude = (double) data.getExtras().get("longitude");
            String coordinates = latitude + "," + longitude;

            Geocoder geocoder = new Geocoder(getActivity());
            Address address = null;
            try {
                address = geocoder.getFromLocation(latitude, longitude, 1).get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            txtExactLocation.setText(coordinates);
            txtCity.setText(address.getLocality());
        }

    }
}
