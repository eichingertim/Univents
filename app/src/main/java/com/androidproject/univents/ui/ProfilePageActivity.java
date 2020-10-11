package com.androidproject.univents.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This activity displays and handles a users profile page
 */
public class ProfilePageActivity extends AppCompatActivity {

    private ImageButton phoneNumberButton;
    private ImageButton emailButton;
    private CircleImageView profilePicture, imgEditPic;
    private TextView profileName, profileDescription;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;

    private User user;
    private String userId;

    private FirebaseFirestore db;
    private DocumentReference refUser;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        initFireBase();
        getIntentExtras();
        initSharedPreferences();
        initToolbar();
        initUI();
        readIntentCreateItem();
    }

    private void getIntentExtras() {
        userId = getIntent().getStringExtra(getString(R.string.KEY_FIREBASE_USER_ID));
    }

    private void initSharedPreferences() {
        sharedPreferences = this.getSharedPreferences(getString(R.string.PREF_KEY_EMAIL_PHONE)
                , Context.MODE_PRIVATE);
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
                .document(userId);
        refUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                fillUserName();
                setButtonFunctions();
                setProfilePicture();
                fillUserDescription();
                profilePicEditHandler();
            }
        });
    }

    private void profilePicEditHandler() {
        if (auth.getCurrentUser().getUid().equals(userId)) {
            imgEditPic.setVisibility(View.VISIBLE);
        }
        imgEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser().getUid().equals(userId)) {
                    startActivity(new Intent(ProfilePageActivity.this
                            , ProfilePicChangeActivity.class));
                }
            }
        });
    }

    private void fillUserDescription() {
        profileDescription.setText(user.getDescription());
    }

    private void setProfilePicture() {
        if (!user.getPictureURL().equals("")) {
            Picasso.get().load(user.getPictureURL()).noFade().into(profilePicture);
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
        imgEditPic = findViewById(R.id.img_edit_pic);
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
                    Toast.makeText(getApplicationContext(), getString(R.string.user_not_receive_emails)
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
                    Toast.makeText(getApplicationContext(), getString(R.string.user_not_receive_calls)
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
        startActivity(Intent.createChooser(sendMail, "Wähle eine E-Mail App"));
    }

    private void openPhoneCall(){
        String phoneNumber = user.getPhoneNumber();
        if (!phoneNumber.isEmpty()) {
            Intent phoneCall = new Intent(Intent.ACTION_DIAL);
            phoneCall.setData(Uri.fromParts("tel", phoneNumber, null));
            startActivity(phoneCall);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.user_has_no_phone_number)
                    , Toast.LENGTH_LONG).show();
        }

    }

    /**
     * initializes the toolbar
     */
    private void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.profile));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userId.equals(auth.getCurrentUser().getUid())) {
            getMenuInflater().inflate(R.menu.menu_profile, menu);
        }
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
        startActivity(new Intent(this, ProfileEditActivity.class));
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setProfilePicture();
        } catch (Exception ignore) {

        }
    }
}
