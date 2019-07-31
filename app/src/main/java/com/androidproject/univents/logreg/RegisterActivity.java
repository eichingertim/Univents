package com.androidproject.univents.logreg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.customviews.NoSwipeViewPager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    //Constants for viewpager positions
    private static final int POS_DATA_ENTRY = 0;
    private static final int POS_DATA_PROTECT = 1;

    //Regular Expressions for password- and email-checking
    private static final String REGEX_PASSWORD_NUMBERS = ".*[0-9].*";
    private static final String REGEX_PASSWORD_SMALL_LETTER = ".*[a-zäöüß].*";
    private static final String REGEX_PASSWORD_BIG_LETTER = ".*[A-ZÄÖÜ].*";
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:" +
            "[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    private ActionBar actionBar;

    //FireBase-tools
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference refUsers;

    //ViewPager and its views + array-list that contains the views
    private NoSwipeViewPager layout_pager;
    private View layoutDataEntry, layoutDataProtection;
    private ArrayList<View> registerLayouts = new ArrayList<>();

    private FloatingActionButton fab;

    //Views from the data-protection layout
    private CheckBox checkDataProtection;
    private ScrollView scrollView;
    private FloatingActionButton fab_scroll;

    //Views from data-entry layout
    private EditText txtFirstName, txtLastName, txtOrgaName, txtEmail, txtEmailConfirm
            , txtPassword, txtPasswordConfirm;
    private ImageView imgConfirmEmail, imgConfirmPassword, imgMinLetter
            , imgMinNumber, imgMinChars;

    //containing whether a condition of password/email is true or false
    private boolean isEmailConfirm, isPwNumbers, isPwLetters, isPwNumCount, isPwConfirm;

    //contains whether a organisation or a private-person wants to register
    boolean isOrga = false;

    //states for the color-state-list which changes the vector-asset color
    int[][] states = new int[][] {
            new int[] { android.R.attr.state_enabled}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        isOrga = Objects.requireNonNull(getIntent().getExtras())
                .getBoolean(getString(R.string.BOOLEAN_ORGA));

        initToolbar();
        initFireBase();
        initViews();

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
     * Initializes the toolbar/actionbar
     */
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(String.format(getString(R.string.toolbar_reg_steps), 1));
    }

    /**
     * initializes all views and setup viewpager with layout-views
     */
    @SuppressLint("InflateParams")
    private void initViews() {
        layout_pager = findViewById(R.id.register_process_layout_container);

        fab = findViewById(R.id.fab_register);
        fab.setOnClickListener(this);

        LayoutInflater inflater = RegisterActivity.this.getLayoutInflater();
        if (isOrga) {
            layoutDataEntry = inflater.inflate(R.layout.layout_register_data_entry_orga
                    , null);
        } else {
            layoutDataEntry = inflater.inflate(R.layout.layout_register_data_entry_private,
                    null);
        }
        layoutDataProtection = inflater.inflate(R.layout.layout_register_data_protection_declaration
                ,null);
        initDataEntryViews();
        initDataProtectionViews();

        registerLayouts.add(layoutDataEntry);
        registerLayouts.add(layoutDataProtection);

        ViewPagerAdapter adapter = new ViewPagerAdapter(registerLayouts);
        layout_pager.setAdapter(adapter);
        addPagerListener();

    }

    /**
     * adds a listener to the viewpager, that makes an action when the pages are changed
     */
    private void addPagerListener() {
        layout_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case POS_DATA_ENTRY:
                        changeIcons(false, R.drawable.ic_arrow_forward_24dp);
                        break;
                    case POS_DATA_PROTECT:
                        changeIcons(true, R.drawable.ic_check_24dp);
                        break;
                }
                actionBar.setTitle(String.format(getString(R.string.toolbar_reg_steps),(i + 1)));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    /**
     * changes the icons of the floating button and the action bar
     * @param setBackArrow boolean: if the back arrow should be visible
     * @param resID ID of the drawable for the floating button icon
     */
    private void changeIcons(boolean setBackArrow, int resID) {
        actionBar.setDisplayHomeAsUpEnabled(setBackArrow);
        fab.setImageResource(resID);
    }

    /**
     * initializes all views from the data-protection-declaration-layout
     * and adds functionality to the scroll-down-floating-button and the
     * scroll-view container.
     */
    private void initDataProtectionViews() {
        TextView tvDataProtectDecl = layoutDataProtection
                .findViewById(R.id.tv_data_protection_declaration);
        tvDataProtectDecl.setText(Html.fromHtml(getString(R.string.data_protection)));

        checkDataProtection = layoutDataProtection.findViewById(R.id.check_data_protection);

        fab_scroll = layoutDataProtection.findViewById(R.id.fab_scroll_bottom);
        fab_scroll.setAlpha(0.80f);
        fab_scroll.setOnClickListener(this);

        scrollView = layoutDataProtection.findViewById(R.id.scroll_view_data_protect);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int dy = scrollY - oldScrollY;
                if (dy > 0 && fab_scroll.getVisibility() == View.VISIBLE) {
                    fab_scroll.hide();
                } else if (dy <= 0 && fab_scroll.getVisibility() != View.VISIBLE) {
                    fab_scroll.show();
                }
            }
        });
    }

    /**
     * initializes all views from the layout data-entry
     */
    private void initDataEntryViews() {
        txtFirstName = layoutDataEntry.findViewById(R.id.txt_first_name);
        txtLastName = layoutDataEntry.findViewById(R.id.txt_last_name);
        txtEmail = layoutDataEntry.findViewById(R.id.txt_email);
        txtEmailConfirm = layoutDataEntry.findViewById(R.id.txt_email_confirm);
        txtPassword = layoutDataEntry.findViewById(R.id.txt_password);
        txtPasswordConfirm = layoutDataEntry.findViewById(R.id.txt_password_confirm);

        if (isOrga) {
            txtOrgaName = layoutDataEntry.findViewById(R.id.txt_orga_name);
        }

        imgConfirmEmail = layoutDataEntry.findViewById(R.id.img_email_confirmed);
        imgConfirmPassword = layoutDataEntry.findViewById(R.id.img_password_confirmed);
        imgMinChars = layoutDataEntry.findViewById(R.id.img_count_chars);
        imgMinLetter = layoutDataEntry.findViewById(R.id.img_passw_big_small_letters);
        imgMinNumber = layoutDataEntry.findViewById(R.id.img_passw_number);

        setEditTextWatcher();

    }

    /**
     * Sets TextWatchers to necessary edit-texts
     */
    private void setEditTextWatcher() {
        setEmailConfirmWatcher();
        setPasswordWatcher();
        setPasswordConfirmWatcher();
    }

    /**
     * Text-Watcher for EditText password confirm
     */
    private void setPasswordConfirmWatcher() {
        txtPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean conditionPwConfirm = s.toString().equals(txtPassword.getText().toString())
                        && !s.toString().isEmpty();

                isPwConfirm = checkRealTimeInputValid(conditionPwConfirm, imgConfirmPassword);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Text-Watcher for EditText password
     */
    private void setPasswordWatcher() {
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean conditionCountChars = s.toString().length() >= 8;
                boolean conditionNumbers = s.toString().matches(REGEX_PASSWORD_NUMBERS);
                boolean conditionLetters = s.toString().matches(REGEX_PASSWORD_SMALL_LETTER)
                        && s.toString().matches(REGEX_PASSWORD_BIG_LETTER);

                isPwNumCount = checkRealTimeInputValid(conditionCountChars, imgMinChars);
                isPwLetters = checkRealTimeInputValid(conditionLetters, imgMinLetter);
                isPwNumbers = checkRealTimeInputValid(conditionNumbers, imgMinNumber);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * TextWatcher for EditText Email-Confirm
     */
    private void setEmailConfirmWatcher() {
        txtEmailConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean conditionPwConfirm = s.toString().equals(txtEmail.getText()
                        .toString()) && !s.toString().isEmpty();

                isEmailConfirm = checkRealTimeInputValid(conditionPwConfirm, imgConfirmEmail);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * checks whether in realtime whether the input is valid or not
     * @param condition includes the condition for the input
     * @param icon includes the icon, that should be changed when the input vaidation
     *             changes
     * @return returns whether the input is valid or not
     */
    private boolean checkRealTimeInputValid(boolean condition, ImageView icon) {
        if (condition) {
            setIconTrue(icon);
            return true;
        } else {
            setIconFalse(icon);
            return false;
        }
    }

    private void setIconTrue(ImageView icon) {
        icon.setImageTintList(new ColorStateList(states
                , new int[]{getResources().getColor(R.color.colorAccent)}));
        icon.setImageResource(R.drawable.ic_check_24dp);
    }

    private void setIconFalse(ImageView icon) {
        icon.setImageTintList(new ColorStateList(states
                , new int[]{getResources().getColor(R.color.colorDarkText)}));
        icon.setImageResource(R.drawable.ic_close_24dp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_register:
                handleFabRegisterClick();
                break;
            case R.id.fab_scroll_bottom:
                scrollToBottom();
                break;
        }
    }

    private void scrollToBottom() {
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }

    /**
     * handles when the floating action button is clicked.
     * It decides whether to go on with step two or finish
     * sign in process.
     */
    private void handleFabRegisterClick() {

        String firstName = txtFirstName.getText().toString();
        String lastName = txtLastName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String orgaName = "";

        if (isOrga) orgaName = txtOrgaName.getText().toString();

        if (layout_pager.getCurrentItem() == POS_DATA_ENTRY) {
            if (checkCorrectFilled(firstName, lastName, orgaName, email)) {
                layout_pager.setCurrentItem(POS_DATA_PROTECT);
            }
        } else {
            if (checkDataProtection.isChecked()) {
                signUp(firstName, lastName, orgaName, email, password);
            } else {
                showToast(getString(R.string.confirm_data_protection));
            }
        }
    }

    /**
     * checks whether all inputs are valid when user wants to register
     * @param firstName text from editText txtFirstName
     * @param lastName text from editText txtLastName
     * @param orgaName text from editText txtOrgaName
     * @param email text from editText txtEmail
     * @return returns if all inputs are valid
     */
    private boolean checkCorrectFilled(String firstName, String lastName, String orgaName
            , String email) {

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || (orgaName.isEmpty() && isOrga)) {
            showToast(getString(R.string.fill_all_fields));
            return false;
        } else if (!email.matches(REGEX_EMAIL)) {
            showToast(getString(R.string.no_valid_email));
            return false;
        } else if (!isEmailConfirm) {
            showToast(getString(R.string.emails_are_not_same));
            return false;
        } else if (!(isPwLetters && isPwNumbers && isPwNumCount)) {
            showToast(getString(R.string.password_not_valid));
            return false;
        } else if (isPwConfirm){
            return true;
        } else {
            showToast(getString(R.string.passwords_not_same));
            return false;
        }
    }

    /**
     * Signs up the user with fireBase email-authentication
     * @param firstName user first-name
     * @param lastName user last-name
     * @param orgaName user orga-name
     * @param email user email
     * @param password user password
     */
    private void signUp(String firstName, String lastName, String orgaName
            , String email, String password) {

        final Map<String, Object> newUser = new HashMap<>();
        newUser.put(getString(R.string.KEY_FB_FIRST_NAME), firstName);
        newUser.put(getString(R.string.KEY_FB_LAST_NAME), lastName);
        newUser.put(getString(R.string.KEY_FB_EMAIL), email);
        if (isOrga) {
            newUser.put(getString(R.string.KEY_FB_ORGA_NAME), orgaName);
            newUser.put(getString(R.string.KEY_FB_IS_ORGA), true);
        } else newUser.put(getString(R.string.KEY_FB_IS_ORGA), false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                newUser.put(getString(R.string.KEY_FB_USER_ID), user.getUid());
                                uploadDataToFbAndFinish(newUser, user);
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
     * Uploads user-data to Firebase, sends verification-email
     * and finishes the activity with a result+intent
     * @param newUser Hashmap with user-data
     * @param user firebase-user
     */
    private void uploadDataToFbAndFinish(Map<String, Object> newUser, final FirebaseUser user) {
        refUsers.document(user.getUid()).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent finishRegister = new Intent();
                finishRegister.putExtra(getString(R.string.KEY_USER_EMAIL)
                        , user.getEmail());
                setResult(RESULT_OK, finishRegister);
                user.sendEmailVerification();
                auth.signOut();
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logreg, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            layout_pager.setCurrentItem(POS_DATA_ENTRY);
        } else if (item.getItemId() == R.id.close_log_reg_layout){
            setResult(RESULT_CANCELED, null);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Simple Custom-PagerAdapter that adds the views from an ArrayList
     * to the view-group and handles adapter-actions
     */
    private class ViewPagerAdapter extends PagerAdapter {

        private ArrayList<View> views;

        ViewPagerAdapter(ArrayList<View> views) {
            this.views = views;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            View view = views.get(position);
            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

}
