package com.androidproject.univents.ui.fragments.show_event_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.controller.EventSaleListAdapter;
import com.androidproject.univents.models.EventSale;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class SaleFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    private String eventId;

    private ArrayList<EventSale> sales = new ArrayList<>();
    private EventSaleListAdapter adapter;
    private ListView listViewSales;
    private TextView emptyListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        getEventId();
        initViews(view);
        getData();
    }

    private void initViews(View view) {
        emptyListView = view.findViewById(R.id.tv_no_sale_lists);
        listViewSales = view.findViewById(R.id.list_view_sale);
        adapter = new EventSaleListAdapter(getActivity(), sales);
        listViewSales.setAdapter(adapter);
        listViewSales.setEmptyView(emptyListView);

    }

    private void getEventId() {
        eventId = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
    }

    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_SALE))
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                sales.clear();
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    EventSale sale = new EventSale(documentSnapshot.getId(), documentSnapshot.getData());
                    sales.add(sale);
                }
                adapter.notifyDataSetChanged();

            }
        });
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }
}
