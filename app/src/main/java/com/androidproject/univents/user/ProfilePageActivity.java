package com.androidproject.univents.user;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.androidproject.univents.R;
import com.androidproject.univents.settings.SettingsFragment;

import javax.annotation.Nullable;

public class ProfilePageActivity extends AppCompatActivity {

    private Button phoneNumberButton;
    private ImageButton emailButton;
    private TextView profileName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        initToolbar();
        initUI();
    }

    /**
     * initializes the UI
     */
    private void initUI(){
        emailButton = findViewById(R.id.btn_profile_email);
        phoneNumberButton = findViewById(R.id.btn_phone_number);
        profileName = findViewById(R.id.profile_name);
    }

    /**
     * initializes the toolbar
     */
    private void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.profile_edit:
                editProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editProfile(){

    }
}
