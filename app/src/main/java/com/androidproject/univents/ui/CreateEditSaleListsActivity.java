package com.androidproject.univents.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.controller.EventSaleAddListAdapter;
import com.androidproject.univents.models.ExpandedGridView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateEditSaleListsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText txtSaleCategory, txtSaleIdentifier, txtSalePrice;
    private ImageButton btnAddToList;

    private Toolbar toolbar;

    private ExpandedGridView listViewSaleList;
    private EventSaleAddListAdapter adapter;
    private ArrayList<String> identifier = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();

    private String saleCategoryFromIntent;
    private String eventId;
    private boolean isNewList = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_sale_lists);

        getIntents();
        initFirebase();
        initToolbar();
        initViews();
        setClickListener();
        if (!isNewList) {
            getData();
        }

    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setClickListener() {
        btnAddToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtSaleIdentifier.getText().toString().isEmpty()
                        && !txtSalePrice.getText().toString().isEmpty()) {
                    identifier.add(txtSaleIdentifier.getText().toString());
                    prices.add(Double.parseDouble(txtSalePrice.getText().toString()));
                    adapter.notifyDataSetChanged();
                    txtSaleIdentifier.setText("");
                    txtSalePrice.setText("");
                }
            }
        });
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_SALE))
                .document(saleCategoryFromIntent).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> map = documentSnapshot.getData();
                identifier.clear();
                prices.clear();
                for (String string : map.keySet()) {
                    identifier.add(string);
                    prices.add(Double.parseDouble(String.valueOf(map.get(string))));
                }
                txtSaleCategory.setText(documentSnapshot.getId());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void getIntents() {
        if (getIntent().getStringExtra(getString(R.string.KEY_INTENT_SALE_CATEGORY)) != null) {
            saleCategoryFromIntent = getIntent().getStringExtra(getString(R.string.KEY_INTENT_SALE_CATEGORY));
            eventId = getIntent().getStringExtra(getString(R.string.KEY_FIREBASE_EVENT_ID));
            isNewList = false;
        } else {
            isNewList = true;
        }
    }

    private void initViews() {
        txtSaleCategory = findViewById(R.id.txt_create_edit_event_sale_category);
        txtSaleIdentifier = findViewById(R.id.txt_create_edit_event_sale_piece);
        txtSalePrice = findViewById(R.id.txt_create_edit_event_sale_price);

        btnAddToList = findViewById(R.id.btn_add_new_list_item);

        listViewSaleList = findViewById(R.id.list_view_new_list);
        adapter = new EventSaleAddListAdapter(this, identifier, prices);
        listViewSaleList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.profile_edit_save:
                if (!txtSaleCategory.getText().toString().isEmpty()) {
                    HashMap<String, Object> map = new HashMap<>();
                    int i = 0;
                    for (String str : identifier) {
                        map.put(str, prices.get(i));
                        i++;
                    }
                    Intent intent = new Intent(CreateEditSaleListsActivity.this
                            , CreateEditEventActivity.class);
                    intent.putExtra(getString(R.string.KEY_INTENT_NEW_SALE_ITEM_MAP), map);
                    intent.putExtra(getString(R.string.KEY_INTENT_SALE_CATEGORY), txtSaleCategory.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Du musst eine Kategorie hinzufügen"
                            ,Toast.LENGTH_LONG ).show();
                }
                break;
            case android.R.id.home:
                onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * checks if dark theme is on
     */
    public void checkTheme() {
        if (isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkTheme);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme);
        }
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }

}
