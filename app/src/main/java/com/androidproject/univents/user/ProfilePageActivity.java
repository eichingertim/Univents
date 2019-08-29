package com.androidproject.univents.user;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.customviews.EventItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import javax.annotation.Nullable;

public class ProfilePageActivity extends AppCompatActivity {

    private ImageButton phoneNumberButton;
    private ImageButton emailButton;
    private TextView profileName;
    private Toolbar toolbar;

    private User user;

    private FirebaseFirestore db;
    private DocumentReference refUser;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        initFireBase();
        initToolbar();
        initUI();
        readSharedPreferences();
        readIntentCreateItem();
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
                initUsername();
                initButtons();
            }
        });
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
        String username = user.getFirstName() + " " + user.getLastName();
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
        startActivity(new Intent(this, EditProfilePage.class));
    }

    private void readSharedPreferences(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("EmailPreference", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("email", true)){
            showEmail();
        }else{
            dontShowEmail();
        }
    }

    public void dontShowEmail(){
        emailButton.setVisibility(View.INVISIBLE);
    }
    public void showEmail(){
        emailButton.setVisibility(View.VISIBLE);
    }
    public void dontShowPhoneNumber(){
        phoneNumberButton.setVisibility(View.INVISIBLE);
    }
    public void showPhoneNumber(){
        phoneNumberButton.setVisibility(View.VISIBLE);
    }
}
