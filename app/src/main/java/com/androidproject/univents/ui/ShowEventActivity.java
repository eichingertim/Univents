package com.androidproject.univents.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.ui.fragments.show_event_fragments.OverviewFragment;
import com.androidproject.univents.ui.fragments.show_event_fragments.SaleFragment;
import com.androidproject.univents.ui.fragments.show_event_fragments.QandAFragment;
import com.androidproject.univents.ui.fragments.show_event_fragments.UpdatesFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShowEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Toolbar toolbar;
    private ViewPager pager;
    private TabLayout tabs;

    private String eventid;
    private EventItem eventItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);

        initFireBase();
        readIntentCreateItem();
        initViews();
        setTabChangeListeners();

    }

    private void initViews() {
        pager = findViewById(R.id.pager_show_event);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager()
                , tabs.getTabCount());
        pager.setAdapter(tabsAdapter);
    }

    private void setTabChangeListeners() {
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(eventItem.getEventTitle());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                initToolbar();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (eventItem.getEventOrganizer().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            getMenuInflater().inflate(R.menu.menu_show_event, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_edit_event:
                Intent intent = new Intent(ShowEventActivity.this, CreateEditEventActivity.class);
                intent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), eventid);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class TabsAdapter extends FragmentStatePagerAdapter {

        private int mNumOfTabs;
        private String[] tabTitles = {"Übersicht", "Beiträge", "Q&A", "Verkauf"};


        public TabsAdapter(FragmentManager fm, int NoofTabs){
            super(fm);
            this.mNumOfTabs = NoofTabs;
        }
        @Override
        public int getCount() {
            return mNumOfTabs;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    OverviewFragment overviewFragment = new OverviewFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.KEY_FIREBASE_EVENT_ID),eventid);
                    overviewFragment.setArguments(bundle);
                    return overviewFragment;
                case 1:
                    UpdatesFragment updatesFragment = new UpdatesFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString(getString(R.string.KEY_FIREBASE_EVENT_ID),eventid);
                    updatesFragment.setArguments(bundle1);
                    return updatesFragment;
                case 2:
                    QandAFragment qandAFragment = new QandAFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString(getString(R.string.KEY_FIREBASE_EVENT_ID),eventid);
                    qandAFragment.setArguments(bundle2);
                    return qandAFragment;
                case 3:
                    SaleFragment saleFragment = new SaleFragment();
                    Bundle bundle3 = new Bundle();
                    bundle3.putString(getString(R.string.KEY_FIREBASE_EVENT_ID),eventid);
                    saleFragment.setArguments(bundle3);
                    return saleFragment;
                default:
                    return null;
            }
        }
    }

    /**
     * checks if dark theme is on
     */
    private void checkTheme() {
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
