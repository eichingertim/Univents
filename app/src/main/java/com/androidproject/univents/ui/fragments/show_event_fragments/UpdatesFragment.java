package com.androidproject.univents.ui.fragments.show_event_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.models.EventUpdate;
import com.androidproject.univents.controller.EventUpdateListAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class UpdatesFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private ArrayList<EventUpdate> updates = new ArrayList<>();
    private ListView listViewUpdates;
    private EventUpdateListAdapter adapter;
    private TextView tvEmptyListView;

    private String eventId;

    private EventItem event;

    private FloatingActionButton fabAddUpdate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_updates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventId = getEventId();
        initFirebase();
        initListView(view);
        getEventData(view);
        getData();

    }

    private void getEventData(final View view) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                event = documentSnapshot.toObject(EventItem.class);
                initFab(view);
            }
        });
    }

    private void initFab(View view) {
        fabAddUpdate = view.findViewById(R.id.fab_add_update);
        if (firebaseUser.getUid().equals(event.getEventOrganizer())) {
            fabAddUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNewUpdateDialog();
                }
            });
        } else {
            fabAddUpdate.hide();
        }
    }

    private void openNewUpdateDialog() {
        DialogFragment dialog = NewUpdateDialogFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
        dialog.setArguments(bundle);
        dialog.show(getActivity().getSupportFragmentManager(), "tag");

    }

    private String getEventId() {
        return getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
    }

    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_UPDATES)).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots
                    , @Nullable FirebaseFirestoreException e) {
                updates.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    EventUpdate eventUpdate = doc.toObject(EventUpdate.class);
                    updates.add(eventUpdate);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initListView(View view) {
        listViewUpdates = view.findViewById(R.id.list_view_updates);
        adapter = new EventUpdateListAdapter(getActivity(), updates);
        listViewUpdates.setAdapter(adapter);
        tvEmptyListView = view.findViewById(R.id.tv_empty_list_view);
        listViewUpdates.setEmptyView(tvEmptyListView);

    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }
}
