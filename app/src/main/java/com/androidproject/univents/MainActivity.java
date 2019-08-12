package com.androidproject.univents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproject.univents.customviews.NoSwipeViewPager;
import com.androidproject.univents.logreg.LogRegChooserActivity;
import com.androidproject.univents.main_fragments.HomeFragment;
import com.androidproject.univents.main_fragments.MapFragment;
import com.androidproject.univents.main_fragments.MyEventsFragment;
import com.androidproject.univents.main_fragments.SearchFragment;
import com.androidproject.univents.settings.SettingsActivity;
import com.androidproject.univents.user.User;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        , BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int NUM_VIEW_PAGES = 4;
    private static final int HOME = 0;
    private static final int SEARCH = 1;
    private static final int MAP = 2;
    private static final int MY_EVENTS = 3;

    private Toolbar toolbar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference refUser;

    private DrawerLayout mainDrawer;
    private ActionBarDrawerToggle mainDrawerToggle;
    private NavigationView mainDrawerNavView;

    private BottomNavigationView mainBottomNav;
    private NoSwipeViewPager mainViewPager;
    private PagerAdapter viewPagerAdapter;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initFireBase();
        initNavigationDrawer();
        refUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                initNavDrawerHeader();
                initContent();
            }
        });


    }

    private void initNavDrawerHeader() {
        TextView tvHeaderUserName = findViewById(R.id.tv_header_name);
        TextView tvHeaderUserEmail = findViewById(R.id.tv_header_email);
        tvHeaderUserName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
        tvHeaderUserEmail.setText(user.getEmail());

        ImageView ivHeaderProfilePic = findViewById(R.id.iv_header_profile_pic);
        try {
            Picasso.get().load(auth.getCurrentUser().getPhotoUrl()).into(ivHeaderProfilePic);
        } catch (Exception e) {
            Log.e("PHOTO", e.getMessage());
        }

    }

    /**
     * initializes all content-views
     */
    private void initContent() {
        mainBottomNav = findViewById(R.id.bottom_nav_view);
        mainViewPager = findViewById(R.id.main_view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(viewPagerAdapter);
        mainBottomNav.setOnNavigationItemSelectedListener(this);
    }

    /**
     * initializes the toolbar as actionbar
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Initializes the complete NavigationDrawer with toggle and view
     */
    private void initNavigationDrawer() {
        mainDrawer = findViewById(R.id.main_drawer_layout);
        mainDrawerToggle = new ActionBarDrawerToggle(this, mainDrawer, R.string.OPEN
                , R.string.CLOSE);
        mainDrawer.addDrawerListener(mainDrawerToggle);
        mainDrawerToggle.syncState();
        mainDrawerNavView = findViewById(R.id.drawer_nav_view);
        mainDrawerNavView.setNavigationItemSelectedListener(this);
    }

    /**
     * initializes FireBase
     */
    private void initFireBase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        refUser = db.collection(getString(R.string.KEY_FB_USERS))
                .document(auth.getCurrentUser().getUid());

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.drawer_action_profile:
                goToProfilePage();
                break;
            case R.id.drawer_action_logout:
                signOut();
                break;
            case R.id.drawer_action_settings:
                goToSettings();
                break;
            case R.id.bottom_action_home:
                handleHomeClick();
                break;
            case R.id.bottom_action_search:
                handleSearchClick();
                break;
            case R.id.bottom_action_map:
                handleMapClick();
                break;
            case R.id.bottom_action_my_events:
                handleMyEventsClick();
                break;
        }
        return true;
    }

    private void handleSearchClick() {
        mainViewPager.setCurrentItem(SEARCH);
    }

    private void handleHomeClick() {
        mainViewPager.setCurrentItem(HOME);
    }

    private void handleMapClick() {
        mainViewPager.setCurrentItem(MAP);
    }

    private void handleMyEventsClick() {
        mainViewPager.setCurrentItem(MY_EVENTS);
    }

    private void goToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    //TODO: Intent to profile page
    private void goToProfilePage() {

    }

    /**
     * signs the user out from fireBase and facebook
     */
    private void signOut() {
        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e) {
            Log.e("FACEBOOK_SIGNOUT", e.getMessage());
        }
        auth.signOut();
        startActivity(new Intent(MainActivity.this, LogRegChooserActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mainDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case HOME:
                    return new HomeFragment();
                case SEARCH:
                    return new SearchFragment();
                case MAP:
                    return new MapFragment();
                case MY_EVENTS:
                    return new MyEventsFragment();
            }
            return new HomeFragment();
        }

        @Override
        public int getCount() {
            return NUM_VIEW_PAGES;
        }
    }

}
