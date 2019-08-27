package com.androidproject.univents.user;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.androidproject.univents.R;


import javax.annotation.Nullable;

public class ProfilePageActivity extends AppCompatActivity {

    private ImageButton phoneNumberButton;
    private ImageButton emailButton;
    private TextView profileName;
    private Toolbar toolbar;

    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        initToolbar();
        initUI();
        initUsername();
        initButtons();
    }

    /**
     * initializes the UI
     */
    private void initUI(){
        emailButton = findViewById(R.id.btn_profile_email);
        phoneNumberButton = findViewById(R.id.btn_phone_number);
        profileName = findViewById(R.id.profile_name);
        user = new User();
    }

    /**
     * initializes the Username
     */
    private void initUsername(){
        String username = user.getFirstName() + user.getLastName();
        profileName.setText(username);
    }

    /**
     * initializes OnClickListeners for the buttons
     */
    private void initButtons(){
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
        phoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhoneCall();
            }
        });
    }

    /**
     * initializes send email intent
     */
    private void sendMail(){
        String email = user.getEmail();

        Intent sendMail = new Intent(Intent.ACTION_SENDTO);
        //sendMail.putExtra(Intent.EXTRA_EMAIL, email);
        //sendMail.setType("message/rfc822");
        sendMail.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(sendMail, "Choose an email client"));
    }

    private void openPhoneCall(){
        Integer number = 12345;
        String phoneNumber = number.toString();
        Intent phoneCall = new Intent(Intent.ACTION_DIAL);
        phoneCall.setData(Uri.fromParts("tel", phoneNumber, null));
        startActivity(phoneCall);

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
        Intent editProfile = new Intent(ProfilePageActivity.this, EditProfilePage.class);


    }
}
