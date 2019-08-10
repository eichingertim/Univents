package com.androidproject.univents.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.androidproject.univents.R;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private SwitchPreferenceCompat prefDarkTheme;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences, s);
        initPreferences();
    }

    private void initPreferences() {
        prefDarkTheme = (SwitchPreferenceCompat) findPreference(getString(R.string.PREF_KEY_THEME));
        prefDarkTheme.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        if (preference.equals(prefDarkTheme)) {
            handleThemeChange(preference);
        }
        return true;
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
