package com.androidproject.univents.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.androidproject.univents.R;

import javax.annotation.Nullable;

public class EditProfilePage extends AppCompatActivity {



    private Switch email, phone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        initUI();
        checkSwitches();
    }

    private void initUI(){
        email = findViewById(R.id.profile_edit_email_switch);
        phone = findViewById(R.id.profile_edit_phone_switch);

    }

    private void checkSwitches(){
        email.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sharedPreferences(R.string.switch_email_true);
                }else{
                    sharedPreferences(R.string.switch_email_false);
                }
            }
        });

        phone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sharedPreferences(R.string.switch_phone_true);
                }else{
                    sharedPreferences(R.string.switch_phone_false);
                }
            }
        });
    }

    private void sharedPreferences(Integer state){
        SharedPreferences sharedPreferences = this.getSharedPreferences("EmailPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (state){
            case R.string.switch_email_true:
                editor.putBoolean("email", true);
                editor.commit();
                break;
            case R.string.switch_email_false:
                editor.putBoolean("email", false);
                editor.commit();
                break;
        }
    }
}
