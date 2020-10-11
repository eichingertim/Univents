package com.androidproject.univents.ui.fragments.show_event_fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Fragment that shows all updates from an event
 */
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

    /**
     * initializes necessary firebase-tools
     */
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    /**
     * initializes the listView and its adapter and emptyView
     * @param view layout belonging to the fragment
     */
    private void initListView(View view) {
        listViewUpdates = view.findViewById(R.id.list_view_updates);
        adapter = new EventUpdateListAdapter(getActivity(), updates);
        listViewUpdates.setAdapter(adapter);
        tvEmptyListView = view.findViewById(R.id.tv_empty_list_view);
        listViewUpdates.setEmptyView(tvEmptyListView);
    }

    /**
     * retrieves the data of the current event and saves it in an eventItem Object.
     * Afterwards the floatingActionButton is initialized and the clickListeners are set.
     * @param view layout belonging to the fragment
     */
    private void getEventData(final View view) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                event = documentSnapshot.toObject(EventItem.class);
                initFab(view);
                setClickListener();
            }
        });
    }

    /**
     * sets the onItemClickListener for the list
     */
    private void setClickListener() {
        listViewUpdates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (event.getEventOrganizer().equals(firebaseUser.getUid())) {
                    showDeleteDialog((EventUpdate) parent.getItemAtPosition(position));
                } else {
                    showLikeDialog((EventUpdate) parent.getItemAtPosition(position));
                }
            }
        });
    }

    /**
     * shows the dialog whether the user wants to like an update
     * @param itemAtPosition clicked EventUpdate-item
     */
    private void showLikeDialog(final EventUpdate itemAtPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format(getString(R.string.excited_of_title), itemAtPosition.getEventUpdateTitle()));
        builder.setMessage(getString(R.string.mark_update_with_like));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                likeUpdate(itemAtPosition.getEventUpdateId());
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    /**
     * updates the like counter for the selected update
     * @param eventUpdateId id of the selected update
     */
    private void likeUpdate(String eventUpdateId) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_UPDATES))
                .document(eventUpdateId).update(getString(R.string.KEY_FIRBASE_EVENT_UPDATE_LIKES)
                , FieldValue.increment(1)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), getString(R.string.you_like_this_update)
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * shows the dialog whether the organizer wants to delete an update
     * @param itemAtPosition clicked EventUpdate-item
     */
    private void showDeleteDialog(final EventUpdate itemAtPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format(getString(R.string.delete_title)
                , itemAtPosition.getEventUpdateTitle()));
        builder.setMessage(getString(R.string.want_to_delete_update));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUpdate(itemAtPosition.getEventUpdateId());
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    /**
     * deletes the selected update
     * @param eventUpdateId id of the selected update
     */
    private void deleteUpdate(String eventUpdateId) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_UPDATES))
                .document(eventUpdateId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), getString(R.string.updated_deleted)
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * initializes the floating action button and sets an onClickListener
     * @param view layout belonging to the fragment
     */
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

    /**
     * opens the NewUpdateDialogFragment to create a new update
     */
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

    /**
     * retrieves the update data and saves it in a list of EventUpdate-item objects
     */
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
}
