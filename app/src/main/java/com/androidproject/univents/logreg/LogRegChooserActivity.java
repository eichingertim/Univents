package com.androidproject.univents.logreg;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidproject.univents.R;

public class LogRegChooserActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REGISTER_REQUEST_CODE = 201;

    //buttons the user can choose between register and login
    private Button btnChooseRegisterPrivate, btnChooseRegisterOrga, btnChooseLogIn;

    private View layout_welcome, layout_login;

    private ImageView btnCloseLogIn;
    private Button btnLogIn;
    private EditText txtEmail, txtPassword;

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

        layout_welcome = findViewById(R.id.layout_welcome);
        btnChooseRegisterPrivate = findViewById(R.id.btn_choose_register_private);
        btnChooseRegisterOrga = findViewById(R.id.btn_choose_register_orga);
        btnChooseLogIn = findViewById(R.id.btn_choose_log_in);

        //set OnClick Listeners
        btnChooseRegisterPrivate.setOnClickListener(this);
        btnChooseRegisterOrga.setOnClickListener(this);
        btnChooseLogIn.setOnClickListener(this);

        layout_login = findViewById(R.id.layout_login);
        btnCloseLogIn = findViewById(R.id.btn_close_log_in);
        btnLogIn = findViewById(R.id.btn_log_in);
        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);

        btnCloseLogIn.setOnClickListener(this);
        btnLogIn.setOnClickListener(this);


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
            case R.id.btn_log_in:
                logIn();
        }

    }

    //TODO: log in with firebase
    private void logIn() {
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
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
