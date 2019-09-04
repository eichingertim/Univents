package com.androidproject.univents.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
     * reads the intent extra (UserID) and gets the data of the event from
     * Firebase.
     */
    private void readIntentCreateItem() {
        eventid = getIntent().getStringExtra(getString(R.string.KEY_FIREBASE_EVENT_ID));
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventItem = documentSnapshot.toObject(EventItem.class);
                Toast.makeText(getApplicationContext(), eventItem.getEventTitle(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
