package com.androidproject.univents.logreg;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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

import java.util.ArrayList;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int POS_DATA_ENTRY = 0;
    private static final int POS_DATA_PROTECT = 1;

    private ActionBar actionBar;

    private ViewPager layout_pager;
    private View layoutDataEntry, layoutDataProtection;
    private ArrayList<View> registerLayouts = new ArrayList<>();

    private TextView tvdataProtectDecl;
    private CheckBox checkDataProtection;
    private ScrollView scrollView;

    private FloatingActionButton fab, fab_scroll;

    private EditText txtFirstName, txtLastname, txtOrgaName, txtEmail, txtEmailConfirm
            , txtPassword, txtPasswordConfirm;

    private ImageView imgConfirmEmail, imgConfirmPassword, imgMinLetter
            , imgMinNumber, imgMinChars;

    boolean isOrga = false;


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
    }

    /**
     * initializes all views and setup viewpager with layout-views
     */
    private void initViews() {
        layout_pager = findViewById(R.id.register_process_layout_container);

        fab = findViewById(R.id.fab_register);
        fab.setOnClickListener(this);

        LayoutInflater inflater = RegisterActivity.this.getLayoutInflater();
        if (isOrga) {
            layoutDataEntry = inflater.inflate(R.layout.layout_register_data_entry_orga, null);
        } else {
            layoutDataEntry = inflater.inflate(R.layout.layout_register_data_entry_private, null);
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
        tvdataProtectDecl = layoutDataProtection.findViewById(R.id.tv_data_protection_declaration);
        tvdataProtectDecl.setText(Html.fromHtml(getString(R.string.data_protection)));

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
        txtLastname = layoutDataEntry.findViewById(R.id.txt_last_name);
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

    private void handleFabRegisterClick() {

        String firstName = txtFirstName.getText().toString();
        String lastName = txtLastname.getText().toString();
        String email = txtEmail.getText().toString();
        String emailConfirm = txtEmailConfirm.getText().toString();
        String password = txtPassword.getText().toString();
        String passwordConfirm = txtPasswordConfirm.getText().toString();
        String orgaName = null;
        if (isOrga) orgaName = txtOrgaName.getText().toString();

        if (layout_pager.getCurrentItem() == POS_DATA_ENTRY) {
            if (checkCorrectFilled(firstName, lastName, orgaName, email, emailConfirm, password
                    , passwordConfirm)) {
                layout_pager.setCurrentItem(POS_DATA_PROTECT);
            }
        } else {
            if (checkDataProtection.isChecked()) {
                signIn(firstName, lastName, orgaName, email, password);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.confirm_data_protection)
                        , Toast.LENGTH_LONG).show();
            }
        }
    }

    //TODO check all edittext if they are valid
    private boolean checkCorrectFilled(String firstName, String lastName, String orgaName
            , String email, String emailConfirm, String password, String passwordConfirm) {
        return true;
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

        public ViewPagerAdapter(ArrayList<View> views) {
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

}
