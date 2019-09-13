package com.androidproject.univents.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.models.NoSwipeViewPager;
import com.androidproject.univents.ui.fragments.main_fragments.HomeFragment;
import com.androidproject.univents.ui.fragments.main_fragments.MapFragment;
import com.androidproject.univents.ui.fragments.main_fragments.MyEventsFragment;
import com.androidproject.univents.ui.fragments.main_fragments.SearchFragment;
import com.androidproject.univents.models.User;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

/**
 * This activity the 4 main fragments are initialized and the bottom-nav-view and
 * navigation drawer are handled.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        , BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int NUM_VIEW_PAGES = 4;
    private static final int HOME = 0;
    private static final int SEARCH = 1;
    private static final int MAP = 2;
    private static final int MY_EVENTS = 3;

    private boolean backClickedOnce = false;

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

    private FloatingActionButton fabNewEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDynamicLinks();
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

    /**
     * Initializes all views of the navigation-drawer-header and
     * fills relevant views with data
     */
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
        initFloatingActionButton();
    }

    /**
     * initializes the floating action button and sets an onClickListener
     */
    private void initFloatingActionButton() {
        fabNewEvent = findViewById(R.id.fab_home_new_event);
        fabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNewEventActivity();
            }
        });
    }

    //TODO: Create New Event
    private void goToNewEventActivity() {
        startActivity(new Intent(this, CreateEditEventActivity.class));
    }

    /**
     * initializes the toolbar as actionbar
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void setToolbarElevation(float width) {
        toolbar.setElevation(width);
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
        refUser = db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS))
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
        toolbar.setTitle("Suchen");
        mainViewPager.setCurrentItem(SEARCH);
    }

    private void handleHomeClick() {
        toolbar.setTitle("Home");
        mainViewPager.setCurrentItem(HOME);
    }

    private void handleMapClick() {
        toolbar.setTitle("Karte");
        mainViewPager.setCurrentItem(MAP);
    }

    private void handleMyEventsClick() {
        toolbar.setTitle("Meine Events");
        mainViewPager.setCurrentItem(MY_EVENTS);
    }

    private void goToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void goToProfilePage() {
        Intent profileIntent = new Intent(MainActivity.this, ProfilePageActivity.class);
        profileIntent.putExtra(getString(R.string.KEY_FIREBASE_USER_ID), user.getUserId());
        startActivity(profileIntent);
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

    /**
     * receive dynamic links and send the eventID from URL to ShowEventActivity
     */
    private void initDynamicLinks() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            String eventID = deepLink.getPath();
                            Intent intent = new Intent(MainActivity.this, ShowEventActivity.class);
                            intent.putExtra(getString(R.string.KEY_FIREBASE_EVENT_ID), eventID);
                            startActivity(intent);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEEP_LINK_FAIL", "getDynamicLink:onFailure", e);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (backClickedOnce) {
            Intent homeScreenIntent = new Intent(Intent.ACTION_MAIN);
            homeScreenIntent.addCategory(Intent.CATEGORY_HOME);
            homeScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeScreenIntent);
            return;
        }

        this.backClickedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                backClickedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            initNavDrawerHeader();
        } catch (Exception ignore){

        }
    }
}
