package com.androidproject.univents.logreg;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.MainActivity;
import com.androidproject.univents.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LogRegChooserActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REGISTER_REQUEST_CODE = 201;

    //FireBase Tools
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference refUsers;

    //buttons the user can choose between register and login
    private Button btnChooseRegisterPrivate, btnChooseRegisterOrga, btnChooseLogIn;
    private LoginButton btnFbLogIn;
    private CallbackManager callbackManager;

    //Welcome-Layout and Login-Layout as views
    private View layout_welcome, layout_login;

    //Views from the login-layout
    private ImageView btnCloseLogIn;
    private Button btnLogIn;
    private EditText txtEmail, txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg_chooser);

        initFireBase();
        initFacebook();
        initViews();

    }

    /**
     * Initialize FacebookSDK and Login
     */
    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        btnFbLogIn = findViewById(R.id.facebook_login_button);
        callbackManager = CallbackManager.Factory.create();
        btnFbLogIn.setReadPermissions("email", "public_profile");
    }

    /**
     * Initializes FireBase-Tools
     */
    private void initFireBase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        refUsers = db.collection(getString(R.string.KEY_FB_USERS));
    }

    /**
     * function where all registerLayouts from the layout are initialized.
     */
    private void initViews() {

        //init welcome-layout views
        layout_welcome = findViewById(R.id.layout_welcome);
        btnChooseRegisterPrivate = findViewById(R.id.btn_choose_register_private);
        btnChooseRegisterOrga = findViewById(R.id.btn_choose_register_orga);
        btnChooseLogIn = findViewById(R.id.btn_choose_log_in);



        //set OnClick Listeners for welcome-screen views
        btnChooseRegisterPrivate.setOnClickListener(this);
        btnChooseRegisterOrga.setOnClickListener(this);
        btnChooseLogIn.setOnClickListener(this);

        //init login-layout views
        layout_login = findViewById(R.id.layout_login);
        btnCloseLogIn = findViewById(R.id.btn_close_log_in);
        btnLogIn = findViewById(R.id.btn_log_in);
        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);

        //set OnClick Listeners for login-screen views
        btnCloseLogIn.setOnClickListener(this);
        btnLogIn.setOnClickListener(this);


    }

    /**
     * Function that is called when the Facebook-Login-Button is clicked
     * @param v button
     */
    public void onFbButtonClick(View v) {
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        showToast("Anmeldung abgebrochen");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        showToast(exception.getMessage());
                    }
                });
    }

    /**
     * handles the connection between facebook-login and firebase authentication
     * @param accessToken facebook-access-token
     */
    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    uploadDataToFbAndFinish(firebaseUser);
                } else {
                    showToast(Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });

    }

    /**
     * Uploads data to Firebase Firestore database
     * @param firebaseUser current user
     */
    private void uploadDataToFbAndFinish(FirebaseUser firebaseUser) {
        String[] name = Objects.requireNonNull(firebaseUser.getDisplayName()).split(" ");

        Map<String, Object> newUser = new HashMap<>();
        if (name.length < 2) {
            newUser.put(getString(R.string.KEY_FB_LAST_NAME), "...");
        } else {
            newUser.put(getString(R.string.KEY_FB_LAST_NAME), name[1]);
        }
        newUser.put(getString(R.string.KEY_FB_FIRST_NAME), name[0]);
        newUser.put(getString(R.string.KEY_FB_USER_ID), firebaseUser.getUid());
        newUser.put(getString(R.string.KEY_FB_IS_ORGA), false);
        newUser.put(getString(R.string.KEY_FB_EMAIL), Objects.requireNonNull(firebaseUser.getEmail()));

        refUsers.document(firebaseUser.getUid()).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(LogRegChooserActivity.this
                        , MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        switch (viewID) {
            case R.id.btn_choose_register_private:
                openRegisterActivity(false);
                break;
            case R.id.btn_choose_register_orga:
                openRegisterActivity(true);
                break;
            case R.id.btn_choose_log_in:
                changeLayout();
                break;
            case R.id.btn_close_log_in:
                changeLayout();
                break;
            case R.id.btn_log_in:
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();
                if (email.isEmpty() || password.isEmpty())
                    showToast(getString(R.string.enter_log_in_data));
                else logIn(email, password);
                break;

        }

    }

    /**
     * logs in the user and handles possible errors
     * @param email user entered email
     * @param password user entered password
     */
    private void logIn(String email, String password) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            assert user != null;
                            if (user.isEmailVerified()) {
                                startActivity(new Intent(LogRegChooserActivity.this
                                        , MainActivity.class));
                                finish();
                            } else {
                                auth.signOut();
                                showToast(getString(R.string.email_not_yet_confirmed));
                            }
                        } else {
                            String exceptionMessage = Objects.requireNonNull(task.getException())
                                    .getMessage();
                            showToast(exceptionMessage);
                        }
                    }
                });

    }

    /**
     * changes layout between login- and welcome-layout
     */
    private void changeLayout() {
        if (layout_login.getVisibility() == View.VISIBLE) {
            layout_login.setVisibility(View.INVISIBLE);
            layout_welcome.setVisibility(View.VISIBLE);
        } else {
            layout_login.setVisibility(View.VISIBLE);
            layout_welcome.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * hands off to registration process {@link RegisterActivity} with result.
     * @param b
     */
    private void openRegisterActivity(boolean b) {
        Intent regIntent = new Intent(this, RegisterActivity.class);
        regIntent.putExtra(getString(R.string.BOOLEAN_ORGA), b);
        startActivityForResult(regIntent, REGISTER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REGISTER_REQUEST_CODE) {
            showConfirmEmailDialog(data.getStringExtra(getString(R.string.KEY_USER_EMAIL)));
        }

    }

    /**
     * Creates and shows a dialog that notifies the user to confirm its email-address
     * @param email
     */
    @SuppressLint("InflateParams")
    private void showConfirmEmailDialog(String email) {

        View dialogView = getLayoutInflater().inflate(R.layout.layout_email_verify_dialog, null);
        TextView emailSent = dialogView.findViewById(R.id.tv_email_sent);
        String emailSentString = String.format(getString(R.string.email_verification_sent), email);
        emailSent.setText(Html.fromHtml(emailSentString));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

}
