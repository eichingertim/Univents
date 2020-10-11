package com.androidproject.univents.ui.fragments.main_fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.ui.ShowEventActivity;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.controller.EventItemRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Fragment where events are separately displayed as
 * items where the current user is part of or has it created.
 */
public class MyEventsFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecyclerView rvEventsOrga;
    private EventItemRecyclerAdapter adapterOrga;
    private ArrayList<EventItem> itemsOrga = new ArrayList<>();

    private RecyclerView rvEventsPart;
    private EventItemRecyclerAdapter adapterPart;
    private ArrayList<EventItem> itemsPart = new ArrayList<>();

    private TextView tvEmptyRvOrga, tvEmptyRvPart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFireBase();

        initRvOrgaAndAdapter(view);
        initRvPartAndAdapter(view);

        getDataOrga();
        getDataPart();



    }

    /**
     * initializes necessary firebase-tools
     */
    private void initFireBase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * initializes the recyclerView for the events the user has created
     * @param view layout belonging to the fragment
     */
    private void initRvOrgaAndAdapter(View view) {
        rvEventsOrga = view.findViewById(R.id.rv_my_events_orga);
        tvEmptyRvOrga = view.findViewById(R.id.tv_empty_rv_orga);
        rvEventsOrga.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvEventsOrga.setLayoutManager(MyLayoutManager);
        adapterOrga = new EventItemRecyclerAdapter(getActivity(), itemsOrga
                , new EventItemRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(EventItem item) {
                startShowEventActivity(item);
            }
        });
        rvEventsOrga.setAdapter(adapterOrga);
    }

    /**
     * initializes the recyclerView for the events the user participate
     * @param view layout belonging to the fragment
     */
    private void initRvPartAndAdapter(View view) {
        rvEventsPart = view.findViewById(R.id.rv_my_events_participate);
        tvEmptyRvPart = view.findViewById(R.id.tv_empty_rv_part);
        rvEventsPart.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvEventsPart.setLayoutManager(MyLayoutManager);
        adapterPart = new EventItemRecyclerAdapter(getActivity(), itemsPart
                , new EventItemRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(EventItem item) {
                startShowEventActivity(item);
            }
        });
        rvEventsPart.setAdapter(adapterPart);
    }

    /**
     * starts the ShowEventActivity and hands over the eventid of the selected eventitem
     * @param item selected EventItem
     */
    private void startShowEventActivity(EventItem item) {
        Intent intent = new Intent(getActivity(), ShowEventActivity.class);
        intent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), item.getEventId());
        startActivity(intent);
    }

    /**
     * retrieves the data from firebase and fills it to the list
     * where the user created events are stored
     */
    private void getDataOrga() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                .whereEqualTo(getString(R.string.KEY_FIREBASE_EVENT_ORGANIZER)
                        , Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                itemsOrga.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    EventItem item = doc.toObject(EventItem.class);
                    itemsOrga.add(item);
                }
                adapterOrga.notifyDataSetChanged();
                setVisibility(itemsOrga, rvEventsOrga, tvEmptyRvOrga);
            }
        });

    }

    /**
     * retrieves the data from firebase and fills it to the list
     * where the user participate events are stored
     */
    private void getDataPart() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                itemsPart.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    EventItem item = doc.toObject(EventItem.class);
                    if (item.getEventParticipants().contains(auth.getCurrentUser().getUid())) {
                        itemsPart.add(item);
                    }
                }
                adapterPart.notifyDataSetChanged();
                setVisibility(itemsPart, rvEventsPart, tvEmptyRvPart);
            }
        });
    }

    /**
     * handles the visibility of the recyclerView and its emptyView
     * @param items List of eventItems
     * @param recyclerView current recyclerView
     * @param emptyView recyclerView's emptyView
     */
    private void setVisibility(ArrayList<EventItem> items, RecyclerView recyclerView
            , TextView emptyView) {

        if (items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

}
