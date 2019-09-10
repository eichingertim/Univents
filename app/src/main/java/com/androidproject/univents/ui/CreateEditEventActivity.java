package com.androidproject.univents.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.models.FabClickListener;
import com.androidproject.univents.ui.fragments.create_edit_event_fragments.CreateEditAddressFragment;
import com.androidproject.univents.ui.fragments.create_edit_event_fragments.CreateEditDetailsFragment;
import com.androidproject.univents.ui.fragments.create_edit_event_fragments.CreateEditFinishFragment;
import com.androidproject.univents.ui.fragments.create_edit_event_fragments.CreateEditSaleFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CreateEditEventActivity extends AppCompatActivity implements FabClickListener {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Toolbar toolbar;
    private ActionBar actionBar;

    private ViewPager pager;

    private String eventId;
    private boolean isNewEvent = true;

    private Map<String, Object> mapDetails = new HashMap<>();
    private Map<String, Object> mapAdress = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_event);

        getEventIdFromIntent();
        initFirebase();
        initToolbar();
        initViewPager();

    }

    private void getEventIdFromIntent() {
        if (getIntent().getStringExtra(getString(R.string.KEY_FIREBASE_EVENT_ID)) != null) {
            eventId = getIntent().getStringExtra(getString(R.string.KEY_FIREBASE_EVENT_ID));
            isNewEvent = false;
        } else {
            isNewEvent = true;
        }

    }

    //TODO: Finish Create Edit Process
    private void finishCreateEdit() {
        Map<String, Object> newOrEditedEvent = new HashMap<>();

        if (isNewEvent) {
            newOrEditedEvent.put(getString(R.string.KEY_FIREBASE_EVENT_ID), generateEventId());
        } else {
            newOrEditedEvent.put(getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
        }

        //Details
        newOrEditedEvent.putAll(getConfiguredDetailsMap());

        //Address
        newOrEditedEvent.putAll(mapAdress);

        if (mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_PATH)) == null) {
            addOrUpdateToFirebase(newOrEditedEvent, "");
        } else {
            addOrUpdateToFirebase(newOrEditedEvent
                    , mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_PATH)).toString());
        }



    }

    private Map<? extends String,?> getConfiguredDetailsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(getString(R.string.KEY_FIREBASE_EVENT_TITLE)
                , mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_TITLE)));
        map.put(getString(R.string.KEY_FIREBASE_EVENT_DESCRIPTION)
                , mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_DESCRIPTION)));
        map.put(getString(R.string.KEY_FIREBASE_EVENT_BEGIN)
                , mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_BEGIN)));
        map.put(getString(R.string.KEY_FIREBASE_EVENT_END)
                , mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_END)));
        if (mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_PATH)) == null) {
            map.put(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_URL)
                    , mapDetails.get(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_URL)));
        }
        return map;
    }

    private void addOrUpdateToFirebase(Map<String, Object> newOrEditedEvent, String path) {
        if (isNewEvent) {
            uploadTitlePicture(newOrEditedEvent, path);
        } else if (!path.equals("")) {
            uploadTitlePicture(newOrEditedEvent, path);
        } else {
            updateFirebaseEventDocument(newOrEditedEvent);
        }
    }

    private void uploadTitlePicture(final Map<String, Object> newOrEditedEvent, String path) {
        Uri file = Uri.fromFile(new File(path));
        final StorageReference ref = storage.getReference().child("event_title_pictures/"
                + file.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(file);
        Task<Uri> urlTask = (uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        })).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    newOrEditedEvent.put(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_URL)
                            , downloadUri.toString());
                    if (isNewEvent) {
                        addNewFirebaseEventDocument(newOrEditedEvent);
                    } else {
                        updateFirebaseEventDocument(newOrEditedEvent);
                    }
                }
            }
        });
    }

    private void addNewFirebaseEventDocument(Map<String, Object> newEvent) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                .document(newEvent.get(getString(R.string.KEY_FIREBASE_EVENT_ID)).toString())
                .set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Neues Event erfolgreich erstellt"
                        , Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateFirebaseEventDocument(Map<String, Object> editedEvent) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                .document(editedEvent.get(getString(R.string.KEY_FIREBASE_EVENT_ID)).toString())
                .update(editedEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Event erfolgreich aktualisiert"
                        , Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
    }

    private void initViewPager() {
        pager = findViewById(R.id.pager_create_edit_event);
        pager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));
        actionBar.setTitle("Schritt 1 - Details");
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        updateToolbar("Schritt 1 - Details", false);
                        break;
                    case 1:
                        updateToolbar("Schritt 2 - Location", true);
                        break;
                    case 2:
                        updateToolbar("Schritt 3 - Verkauf", true);
                        break;
                    case 3:
                        updateToolbar("Schritt 4 - Bestätigung", true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_edit_event, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_close_create_edit_event:
                finish();
                break;
            case android.R.id.home:
                pager.setCurrentItem(pager.getCurrentItem()-1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateToolbar(String toolbarTitle, boolean backIconEnabled) {
        actionBar.setTitle(toolbarTitle);
        actionBar.setDisplayHomeAsUpEnabled(backIconEnabled);
    }

    @Override
    public void onFabClick(Map<String, Object> data, int fragmentPosition) {

        switch (fragmentPosition) {
            case 0:
                mapDetails = data;
                pager.setCurrentItem(pager.getCurrentItem()+1);
                break;
            case 1:
                mapAdress = data;
                pager.setCurrentItem(pager.getCurrentItem()+1);
                break;
            case 2:
                pager.setCurrentItem(pager.getCurrentItem()+1);
                break;
            case 3:
                finishCreateEdit();
                break;
        }
    }

    private String generateEventId() {
        String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(20);

        for (int i = 0; i < 20; i++) {
            int index = (int)(availableChars.length()
                    * Math.random());
            sb.append(availableChars
                    .charAt(index));
        }
        return sb.toString();
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

    public class CustomPagerAdapter extends FragmentStatePagerAdapter {


        public CustomPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    CreateEditDetailsFragment createEditDetailsFragment = new CreateEditDetailsFragment();
                    if (!isNewEvent) {
                        createEditDetailsFragment.setArguments(getDataBundle());
                    }
                    createEditDetailsFragment.setFabClickListener(CreateEditEventActivity.this);
                    return createEditDetailsFragment;
                case 1:
                    CreateEditAddressFragment createEditAddressFragment = new CreateEditAddressFragment();
                    if (!isNewEvent) {
                        createEditAddressFragment.setArguments(getDataBundle());
                    }
                    createEditAddressFragment.setFabClickListener(CreateEditEventActivity.this);
                    return createEditAddressFragment;
                case 2:
                    CreateEditSaleFragment createEditSaleFragment = new CreateEditSaleFragment();
                    return createEditSaleFragment;
                case 3:
                    CreateEditFinishFragment createEditFinishFragment = new CreateEditFinishFragment();
                    return createEditFinishFragment;
            }

            return null;
        }

        private Bundle getDataBundle() {
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.KEY_FIREBASE_EVENT_ID), eventId);
            return bundle;
        }

    }

}
