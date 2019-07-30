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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;

import java.util.ArrayList;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initToolbar();
        initViews();

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
    }

    private void initViews() {
        layout_pager = findViewById(R.id.register_process_layout_container);

        fab = findViewById(R.id.fab_register);
        fab.setOnClickListener(this);

        LayoutInflater inflater = RegisterActivity.this.getLayoutInflater();
        layoutDataEntry = inflater.inflate(R.layout.layout_register_data_entry, null);
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

    private void changeIcons(boolean setBackArrow, int resID) {
        actionBar.setDisplayHomeAsUpEnabled(setBackArrow);
        fab.setImageResource(resID);
    }

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

    //TODO implement editTexts for data entries
    private void initDataEntryViews() {


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
        if (layout_pager.getCurrentItem() == POS_DATA_ENTRY) {
            if (checkCorrectFilled()) {
                layout_pager.setCurrentItem(POS_DATA_PROTECT);
            }
        } else {
            if (checkDataProtection.isChecked()) {
                signIn();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.confirm_data_protection)
                        , Toast.LENGTH_LONG).show();
            }
        }
    }

    //TODO check all edittext if they are valid
    private boolean checkCorrectFilled() {
        return true;
    }

    //TODO sign in via firebase and finish activity with result ok
    private void signIn() {

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
