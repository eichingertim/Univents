package com.androidproject.univents.ui.fragments.show_event_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.controller.EventQandAListAdapter;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.models.EventQandA;
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

/**
 * Fragment that shows questions and answers that are asked and answered about an event
 */
public class QandAFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private String eventId;
    private EventItem eventItem;

    private ArrayList<EventQandA> qandAs = new ArrayList<>();
    private ListView listViewQandAs;
    private EventQandAListAdapter adapter;
    private TextView emptyListView;

    private FloatingActionButton fabaddNewQuestion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_q_and_a, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        eventId = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
        initViews(view);
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
     * initializes all views from the layout
     * @param view layout belonging to the fragment
     */
    private void initViews(View view) {
        emptyListView = view.findViewById(R.id.tv_empty_q_and_as);
        listViewQandAs = view.findViewById(R.id.list_view_event_q_and_as);
        adapter = new EventQandAListAdapter(getActivity(), qandAs);
        listViewQandAs.setAdapter(adapter);
        listViewQandAs.setEmptyView(emptyListView);

    }

    /**
     * retrieves the data for an event and saves it in an eventItem object.
     * afterwards, the listclicklistener is set and the floatingActionButton is
     * initialized
     * @param view layout belonging to the fragment
     */
    private void getEventData(final View view) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventItem = documentSnapshot.toObject(EventItem.class);
                setListClickListener();
                initFab(view);
            }
        });
    }

    /**
     * sets the itemClickListener to the listView
     * onClick, the NewQandADialogFragment is opened.
     */
    private void setListClickListener() {
        if (eventItem.getEventOrganizer().equals(firebaseUser.getUid())) {
            listViewQandAs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EventQandA item = (EventQandA) parent.getItemAtPosition(position);
                    DialogFragment dialog = NewQandADialogFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
                    bundle.putBoolean(getString(R.string.KEY_INTENT_IS_ANSWER), true);
                    bundle.putString("eventQandAId", item.getEventQandAId());
                    dialog.setArguments(bundle);
                    dialog.show(getActivity().getSupportFragmentManager(), "tag");
                }
            });
        }
    }

    /**
     * initializes the FloatingActionButton and sets an onClickListener
     * @param view
     */
    private void initFab(View view) {
        fabaddNewQuestion = view.findViewById(R.id.fab_add_question);
        fabaddNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = NewQandADialogFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
                bundle.putBoolean(getString(R.string.KEY_INTENT_IS_ANSWER), false);
                dialog.setArguments(bundle);
                dialog.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });
    }

    /**
     * retrieves all the answers and question data and saves it in a list of QandA-items
     */
    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                qandAs.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    EventQandA qandA = doc.toObject(EventQandA.class);
                    qandA.setEventQandAId(doc.getId());
                    qandAs.add(qandA);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
