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

/**
 * Fragment where the address things are handled from the create/edit event process
 */
public class CreateEditAddressFragment extends Fragment {

    private static int REQUEST_LOCATION_CODE = 901;

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

        //when the arguments are not null, it means there is already an event
        //that wants to be edited.
        if (getArguments() != null) {
            eventId = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
            getData();
        }

    }

    /**
     * initializes necessary Firebase-Tools;
     */
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * initializes all views from the layout
     * @param view layout belonging to this fragment
     */
    private void initViews(View view) {
        fabContinue = view.findViewById(R.id.fab_controller_create_edit_event);

        txtExactLocation = view.findViewById(R.id.txt_create_edit_event_coordinates);
        txtCity = view.findViewById(R.id.txt_create_edit_event_city);
        txtLocationDetails = view.findViewById(R.id.txt_create_edit_event_location_details);

        btnSelectLocation = view.findViewById(R.id.btnSelectLocation);

    }

    /**
     * sets the onClickListener to the 2 necessary views
     */
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

    /**
     * retrieves the event data from FireBase and saves the data in an EventItem-Object
     */
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

    /**
     * fills the data into the initialized views
     */
    private void fillDataToViews() {
        String exactLocation = event.getEventExactLocation().getLatitude() + ","
                + event.getEventExactLocation().getLongitude();
        txtExactLocation.setText(exactLocation);
        txtCity.setText(event.getEventCity());
        txtLocationDetails.setText(event.getEventDetailLocation());
    }

    /**
     * Creates a key-value-map with all data that the user has entered/selected.
     * The keys of the map matches with the firebase keys
     * @return returns a map with the data
     */
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

    /**
     * checks if all inputs are valid and if not toasts with error-messages are displayed
     * @return if all is valid or not
     */
    private boolean checkValidInput() {

        if (txtCity.getText().toString().equals("")
                || txtExactLocation.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.please_select_location)
                    , Toast.LENGTH_LONG).show();
            return false;
        } else if (txtLocationDetails.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.describe_your_location)
                    , Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * starts the SelectLocationActivity, to select a location for the event
     */
    private void selectLocation() {
        Intent intent = new Intent(getActivity(), SelectLocationActivity.class);
        startActivityForResult(intent, REQUEST_LOCATION_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = (double) data.getExtras().get(getString(R.string.KEY_LATITUDE));
            double longitude = (double) data.getExtras().get(getString(R.string.KEY_LONGITUDE));
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
