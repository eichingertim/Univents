package com.androidproject.univents.logreg;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int POS_DATA_ENTRY = 0;
    private static final int POS_DATA_PROTECT = 1;
    private static final String REGEX_PASSWORD_NUMBERS = ".*[0-9].*";
    private static final String REGEX_PASSWORD_SMALL_LETTER = ".*[a-zäöüß].*";
    private static final String REGEX_PASSWORD_BIG_LETTER = ".*[A-ZÄÖÜ].*";
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:" +
            "[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    private ActionBar actionBar;

    private NoSwipeViewPager layout_pager;
    private View layoutDataEntry, layoutDataProtection;
    private ArrayList<View> registerLayouts = new ArrayList<>();

    private CheckBox checkDataProtection;
    private ScrollView scrollView;

    private FloatingActionButton fab, fab_scroll;

    private EditText txtFirstName, txtLastName, txtOrgaName, txtEmail, txtEmailConfirm
            , txtPassword, txtPasswordConfirm;

    private boolean isEmailConfirm, isPwNumbers, isPwLetters, isPwNumCount, isPwConfirm;

    private ImageView imgConfirmEmail, imgConfirmPassword, imgMinLetter
            , imgMinNumber, imgMinChars;

    boolean isOrga = false;

    int[][] states = new int[][] {
            new int[] { android.R.attr.state_enabled}  // pressed
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        isOrga = Objects.requireNonNull(getIntent().getExtras())
                .getBoolean(getString(R.string.BOOLEAN_ORGA));

        initToolbar();
        initViews();

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
                signIn(firstName, lastName, orgaName, email, password);
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

    //TODO sign in via firebase and finish activity with result ok
    private void signIn(String firstName, String lastName, String orgaName, String email
            , String password) {

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
