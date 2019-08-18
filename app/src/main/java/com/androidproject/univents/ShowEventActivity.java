package com.androidproject.univents;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.androidproject.univents.customviews.EventItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class ShowEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String eventid;
    private EventItem eventItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);

        initFireBase();
        readIntentCreateItem();

    }

    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * reads the intent extra (EventID) and gets the data of the event from
     * Firebase.
     */
    private void readIntentCreateItem() {
        eventid = getIntent().getStringExtra(getString(R.string.event_id));
        db.collection(getString(R.string.KEY_FB_EVENTS)).document(eventid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventItem = documentSnapshot.toObject(EventItem.class);
                Toast.makeText(getApplicationContext(), eventItem.getTitle(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
