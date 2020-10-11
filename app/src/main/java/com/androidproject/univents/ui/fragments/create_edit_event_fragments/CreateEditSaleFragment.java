package com.androidproject.univents.ui.fragments.create_edit_event_fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.controller.EventSaleExpandableListAdapter;
import com.androidproject.univents.models.EventSale;
import com.androidproject.univents.models.FabClickListener;
import com.androidproject.univents.ui.CreateEditSaleListsActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment where the sale features are handled from the create/edit event process
 */
public class CreateEditSaleFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_CODE_NEW_LIST = 876;
    private static final int REQUEST_CODE_EDITED_LIST = 765;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private String eventId;

    private Button btnNewList;
    private ExpandableListView expandableListView;
    private EventSaleExpandableListAdapter adapter;
    private List<EventSale> items = new ArrayList<>();

    private FloatingActionButton fabContinue;
    private FabClickListener listener;


    public void setFabClickListener(FabClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_edit_sale, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
        }
        initFirebase();
        initViews(view);
        setClickListeners();
        if (getArguments() != null) {
            getData();
        }

    }

    /**
     * initializes necessary Firebase-Tools;
     */
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    /**
     * initializes all views from the layout
     * @param view layout belonging to this fragment
     */
    private void initViews(View view) {
        fabContinue = view.findViewById(R.id.fab_controller_create_edit_event);

        btnNewList = view.findViewById(R.id.btn_add_new_list);
        expandableListView = view.findViewById(R.id.expand_list_view_sale);
        expandableListView.setChildDivider(new ColorDrawable(Color.TRANSPARENT));
        adapter = new EventSaleExpandableListAdapter(getActivity(), items);
        expandableListView.setAdapter(adapter);
    }

    /**
     * sets the onClickListener to the 2 necessary views
     * and the onGroupClickListener to the expandableListView
     */
    private void setClickListeners() {
        fabContinue.setOnClickListener(this);
        btnNewList.setOnClickListener(this);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(final ExpandableListView parent, View v, final int groupPosition, long id) {

                //If the list is a new created list, the parent.getItemAtPosition(groupPosition)
                //is a string. So this can be used for deciding between created and edited.
                try {
                    String string = (String) parent.getItemAtPosition(groupPosition);
                    Toast.makeText(getActivity(), getString(R.string.cannot_edit_this_list), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    editSelectedSaleListCategory(groupPosition);
                }

                return true;
            }
        });
    }

    /**
     * starts the CreateEditSaleListsActivity, to edit the selected sale-list-category
     * @param groupPosition
     */
    private void editSelectedSaleListCategory(int groupPosition) {

        Intent intent = new Intent(getActivity(), CreateEditSaleListsActivity.class);
        intent.putExtra(getActivity().getString(R.string.KEY_INTENT_SALE_CATEGORY)
                , ((EventSale)adapter.getGroup(groupPosition)).getCategory());
        intent.putExtra(getActivity().getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
        startActivityForResult(intent, REQUEST_CODE_EDITED_LIST);
    }

    /**
     * retrieves all event-sale-items from FireBase and saves one object in an EventSale-Object
     * and all the created objects are saved in an ArrayList of EventSale-Items
     */
    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_SALE)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        items.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            EventSale sale = new EventSale(doc.getId(), doc.getData());
                            items.add(sale);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fab_controller_create_edit_event:
                listener.onFabClick(getMapData(), 2);
                break;
            case R.id.btn_add_new_list:
                Intent intent = new Intent(getActivity(), CreateEditSaleListsActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEW_LIST);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String category = "";
        HashMap<String, Object> map = new HashMap<>();

        if (requestCode == REQUEST_CODE_NEW_LIST && resultCode == RESULT_OK
                && data != null) {

            category = data.getStringExtra(getString(R.string.KEY_INTENT_SALE_CATEGORY));
            map = (HashMap<String, Object>) data.getExtras().get(getString(R.string.KEY_INTENT_NEW_SALE_ITEM_MAP));

            EventSale eventSale = new EventSale(category, map);
            items.add(eventSale);
            adapter.notifyDataSetChanged();

        } else if (requestCode == REQUEST_CODE_EDITED_LIST && resultCode == RESULT_OK
                && data != null) {

            category = data.getStringExtra(getString(R.string.KEY_INTENT_SALE_CATEGORY));
            map = (HashMap<String, Object>) data.getExtras().get(getString(R.string.KEY_INTENT_NEW_SALE_ITEM_MAP));

            for (EventSale sale : items) {
                if (sale.getCategory().equals(category)) {
                    items.remove(sale);
                    break;
                }
            }

            EventSale eventSale = new EventSale(category, map);
            items.add(eventSale);
            adapter.notifyDataSetChanged();

        }
    }

    /**
     * Creates a key-value-map with all data that the user has entered/selected.
     * The keys of the map matches with the firebase keys
     * @return returns a map with the data
     */
    private Map<String, Object> getMapData() {
        Map<String, Object> map = new HashMap<>();

        for (EventSale sale : items) {
            map.put(sale.getCategory(), sale.getItems());
        }

        return map;
    }
}
