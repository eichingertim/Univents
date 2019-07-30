package com.androidproject.univents.logreg;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.androidproject.univents.R;

public class LogRegChooserActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REGISTER_REQUEST_CODE = 201;

    //buttons the user can choose between register and login
    private Button btnChooseRegister, btnChooseLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_reg_chooser);

        initViews();

    }

    /**
     * function where all registerLayouts from the layout are initialized.
     */
    private void initViews() {
        btnChooseRegister = findViewById(R.id.btn_choose_register);
        btnChooseLogIn = findViewById(R.id.btn_choose_log_in);

        //set OnClick Listeners
        btnChooseRegister.setOnClickListener(this);
        btnChooseLogIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        switch (viewID) {
            case R.id.btn_choose_register:
                openRegisterActivity();
                break;
            case R.id.btn_choose_log_in:
                openLogInLayout();
                break;
        }

    }

    /**
     * makes the welcome screen invisible and opens login layout
     */
    private void openLogInLayout() {

    }

    /**
     * hands off to registration process {@link RegisterActivity} with result.
     */
    private void openRegisterActivity() {
        Intent regIntent = new Intent(this, RegisterActivity.class);
        startActivityForResult(regIntent, REGISTER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REGISTER_REQUEST_CODE) {
            showConfirmEmailDialog();
        }

    }

    /**
     * Creates and shows a dialog that notifies the user to confirm its email-address
     */
    private void showConfirmEmailDialog() {

    }
}
