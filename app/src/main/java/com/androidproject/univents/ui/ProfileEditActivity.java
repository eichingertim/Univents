package com.androidproject.univents.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ProfileEditActivity extends AppCompatActivity {


    private EditText editFirstName, editLastname, editDescription
            , editEmail, editPhone, editOrga;
    private ImageButton btnEditPassword;
    private View containerOrga;
    private Button btnDeleteAccount;

    private Switch email, phone;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private User user;

    private SharedPreferences sharedPreferences;

    private boolean saved = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        initToolbar();
        initUI();
        initCurrentUser();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initCurrentUser() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS))
                .document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(User.class);
                        fillDataToFields();
                    }
                });
    }

    private void fillDataToFields() {
        editFirstName.setText(user.getFirstName());
        editLastname.setText(user.getLastName());
        editEmail.setText(user.getEmail());
        editPhone.setText(user.getPhoneNumber());
        editDescription.setText(user.getDescription());
        if (user.getOrga()) {
            containerOrga.setVisibility(View.VISIBLE);
            editOrga.setText(user.getOrgaName());
        }
    }

    private void initUI(){

        btnDeleteAccount = findViewById(R.id.btn_delete_account);
        setDeleteClickListener();

        editFirstName = findViewById(R.id.txt_edit_first_name);
        editLastname = findViewById(R.id.txt_edit_last_name);
        editDescription = findViewById(R.id.txt_edit_description);
        editEmail = findViewById(R.id.txt_edit_email);
        editPhone = findViewById(R.id.txt_edit_phone);
        editOrga = findViewById(R.id.txt_edit_orga_name);

        btnEditPassword = findViewById(R.id.btn_edit_password);
        setEditPasswordClickListener();

        containerOrga = findViewById(R.id.container_profile_orga);

        email = findViewById(R.id.profile_edit_email_switch);
        phone = findViewById(R.id.profile_edit_phone_switch);

        sharedPreferences = this.getSharedPreferences(getString(R.string.PREF_KEY_EMAIL_PHONE)
                , Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean("email", true))email.setChecked(true);
        else email.setChecked(false);

        if (sharedPreferences.getBoolean("phone", true))phone.setChecked(true);
        else phone.setChecked(false);


    }

    private void setDeleteClickListener() {
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
                builder.setTitle("Konto löschen");
                builder.setMessage("Willst du dein Konto wirklich löschen? Es werden alle " +
                        "deine Daten und alle deine Events gelöscht.");
                builder.setPositiveButton("Konto löschen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserDatabaseData(dialog);
                    }
                });
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void deleteUserDatabaseData(final DialogInterface dialog) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS))
                .document(auth.getCurrentUser().getUid()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteEventDatabaseData(dialog);
                    }
                });
    }

    private void deleteEventDatabaseData(final DialogInterface dialog) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                .whereEqualTo(getString(R.string.KEY_FIREBASE_EVENT_ORGANIZER), auth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS))
                            .document(doc.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
                deleteAccount(dialog);
            }
        });
    }

    private void deleteAccount(final DialogInterface dialog) {
        FirebaseUser user = auth.getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(ProfileEditActivity.this
                                    , LogRegChooserActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Konto erfolgreich" +
                                    " gelöscht", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            finish();
                        }
                    }
                });
    }

    private void setEditPasswordClickListener() {
        btnEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (saved) {
                    Intent i = getBaseContext().getPackageManager().
                            getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    showShouldSaveDialog();
                }
                break;
            case R.id.profile_edit_save:
                saveProfileInfos();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showShouldSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
        builder.setTitle("Speichern");
        builder.setMessage("Willst du deine Änderungen speichern?");
        builder.setCancelable(false);
        builder.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveProfileInfos();
            }
        });
        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void saveProfileInfos() {

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.PREF_KEY_EMAIL_PHONE)
                , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("email", email.isChecked());
        editor.putBoolean("phone", phone.isChecked());
        editor.commit();

        Map<String, Object> update = getUserData();

        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS))
                .document(auth.getCurrentUser().getUid()).update(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Daten gespeichert", Toast.LENGTH_LONG)
                        .show();
                saved = true;
            }
        });

    }

    private Map<String, Object> getUserData() {
        Map<String, Object> update = new HashMap<>();
        update.put(getString(R.string.KEY_FIREBASE_USER_FIRSTNAME)
                , editFirstName.getText().toString());
        update.put(getString(R.string.KEY_FIREBASE_USER_LASTNAME)
                , editLastname.getText().toString());
        update.put(getString(R.string.KEY_FIREBASE_USER_DESCRIPTION)
                , editDescription.getText().toString());
        update.put(getString(R.string.KEY_FIREBASE_USER_PHONE_NUMBER)
                , editPhone.getText().toString());
        if (user.getOrga()) {
            update.put(getString(R.string.KEY_FIREBASE_USER_ORGA_NAME)
                    , editOrga.getText().toString());
        }
        return update;
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
