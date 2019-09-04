package com.androidproject.univents.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.androidproject.univents.R;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private SwitchPreferenceCompat prefDarkTheme;
    private SwitchPreferenceCompat prefNotifications;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences, s);
        initPreferences();
    }

    private void initPreferences() {
        prefDarkTheme = (SwitchPreferenceCompat) findPreference(getString(R.string.PREF_KEY_THEME));
        prefDarkTheme.setOnPreferenceChangeListener(this);

        prefNotifications = (SwitchPreferenceCompat) findPreference(getString(R.string.PREF_KEY_NOTIFICATIONS));
        prefNotifications.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        if (preference.equals(prefDarkTheme)) {
            handleThemeChange(preference);
        } else if (preference.equals(prefNotifications)) {
            handleNotificationChange(preference);
        }
        return true;
    }

    private void handleNotificationChange(Preference preference) {
        if (!((SwitchPreferenceCompat)preference).isChecked()) {
            ((SwitchPreferenceCompat)preference).setChecked(true);
        } else {
            ((SwitchPreferenceCompat)preference).setChecked(false);
        }
    }

    /**
     * changes the theme when the user wants to
     * @param preference includes the switch-preference to switch
     *                   between themes.
     */
    private void handleThemeChange(Preference preference) {
        if (!((SwitchPreferenceCompat)preference).isChecked()) {
            ((SwitchPreferenceCompat)preference).setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            ((SwitchPreferenceCompat)preference).setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Intent i = getActivity().getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        getActivity().finish();
    }
}
