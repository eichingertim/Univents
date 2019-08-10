package com.androidproject.univents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import com.androidproject.univents.logreg.LogRegChooserActivity;
import com.androidproject.univents.settings.SettingsActivity;
import com.androidproject.univents.user.ProfilePageActivity;
import com.androidproject.univents.user.User;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference refUser;

    private DrawerLayout mainDrawer;
    private ActionBarDrawerToggle mainDrawerToggle;
    private NavigationView mainDrawerNavView;

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
        }
        return true;
    }

    private void goToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    //TODO: Intent to profile page
    private void goToProfilePage() {
        Intent profilePageIntent = new Intent(MainActivity.this, ProfilePageActivity.class);
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

}
