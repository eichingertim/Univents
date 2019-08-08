package com.androidproject.univents;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.androidproject.univents.logreg.LogRegChooserActivity;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;

    private FirebaseAuth auth;

    private DrawerLayout mainDrawer;
    private ActionBarDrawerToggle mainDrawerToggle;
    private NavigationView mainDrawerNavView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initFireBase();
        initNavigationDrawer();
        initContent();

    }

    /**
     * initializes all content-views
     */
    private void initContent() {
        Button btnchangeTheme = findViewById(R.id.btn_changeTheme);
        btnchangeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    restartApp();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restartApp();
                }
            }
        });
    }

    /**
     * initializes the toolbar as actionbar
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mainDrawerNavView = findViewById(R.id.drawer_nav_view);
        mainDrawerNavView.setNavigationItemSelectedListener(this);
    }

    /**
     * initializes FireBase
     */
    private void initFireBase() {
        auth = FirebaseAuth.getInstance();
    }

    private void restartApp() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void checkTheme() {
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else setTheme(R.style.AppTheme);
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
        }
        return true;
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
}
