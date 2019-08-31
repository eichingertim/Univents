package com.androidproject.univents.user;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePageActivity extends AppCompatActivity {

    private ImageButton phoneNumberButton;
    private ImageButton emailButton;
    private CircleImageView profilePicture;
    private TextView profileName, profileDescription;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;

    private User user;

    private FirebaseFirestore db;
    private DocumentReference refUser;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        initFireBase();
        initSharedPreferences();
        initToolbar();
        initUI();
        readIntentCreateItem();
    }

    private void initSharedPreferences() {
        sharedPreferences = this.getSharedPreferences("EmailPreference", Context.MODE_PRIVATE);
    }

    /**
     * initializes FireBase
     */
    private void initFireBase(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * reads the intent extra (EventID) and gets the data of the user from
     * Firebase.
     */
    private void readIntentCreateItem() {
        refUser = db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS))
                .document(auth.getCurrentUser().getUid());
        refUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                fillUserName();
                setButtonFunctions();
                setProfilePicture();
                fillUserDescription();
            }
        });
    }

    private void fillUserDescription() {
        profileDescription.setText(user.getDescription());
    }

    private void setProfilePicture() {
        Uri photoUrl = auth.getCurrentUser().getPhotoUrl();
        if (photoUrl != null) {
            Picasso.get().load(photoUrl).noFade().into(profilePicture);
        } else {
            profilePicture.setImageResource(R.color.colorAccent);
        }

    }

    /**
     * initializes the UI
     */
    private void initUI(){
        emailButton = findViewById(R.id.btn_profile_email);
        phoneNumberButton = findViewById(R.id.btn_phone_number);
        profileName = findViewById(R.id.profile_name);
        profileDescription = findViewById(R.id.profile_description);
        profilePicture = findViewById(R.id.profile_picture);
    }

    /**
     * initializes the Username
     */
    private void fillUserName(){

        String username;
        if (user.getOrga()) {
            username = user.getOrgaName();
        } else {
            username = user.getFirstName() + " " + user.getLastName();
        }
        profileName.setText(username);
    }

    /**
     * initializes OnClickListeners for the buttons
     */
    private void setButtonFunctions(){
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean("email", true)) {
                    sendMail();
                } else {
                    Toast.makeText(getApplicationContext(), "Der Nutzer will keine E-Mails bekommen."
                            , Toast.LENGTH_LONG).show();
                }
            }
        });
        phoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean("phone", true)) {
                    openPhoneCall();
                } else {
                    Toast.makeText(getApplicationContext(), "Der Nutzer will keine Anrufe bekommen."
                            , Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    /**
     * initializes send email intent
     */
    private void sendMail(){
        String email = user.getEmail();

        Intent sendMail = new Intent(Intent.ACTION_SENDTO);
        sendMail.setData(Uri.parse("mailto:" + email));
        startActivity(Intent.createChooser(sendMail, "Choose an email client"));
    }

    private void openPhoneCall(){
        String phoneNumber = user.getPhoneNumber();
        if (!phoneNumber.isEmpty()) {
            Intent phoneCall = new Intent(Intent.ACTION_DIAL);
            phoneCall.setData(Uri.fromParts("tel", phoneNumber, null));
            startActivity(phoneCall);
        } else {
            Toast.makeText(getApplicationContext(), "Der Nutzer hat keine Telefonnummer."
                    , Toast.LENGTH_LONG).show();
        }

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
        startActivity(new Intent(this, EditProfilePage.class));
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
