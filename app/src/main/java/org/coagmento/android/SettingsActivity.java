package org.coagmento.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPreferencesFromResource(R.xml.preferences);
        setupAbout();
    }

    // Set up the About section
    private void setupAbout() {
        // Set up version info
        Preference versionPref = findPreference(getString(R.string.version_key));
        versionPref.setTitle("Coagmento for Android v" + BuildConfig.VERSION_NAME);

        // Set up About Coagmento
        Preference aboutCoagmentoPref = findPreference(getString(R.string.pref_key_about));
        aboutCoagmentoPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle(getString(R.string.title_about_coagmento))
                        .setMessage(getString(R.string.description_about_coagmento))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) { /* Do nothing */ }
                        }).create().show();
                return true;
            }
        });

        // Set up developers and contributors info
        Preference teamPref = findPreference(getString(R.string.pref_key_team));
        teamPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Developers and Contributors")
                        .setMessage(getString(R.string.about_team))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) { /* Do nothing */ }
                        }).create().show();
                return true;
            }
        });

        // Set up open source info
        Preference openSourcePref = findPreference(getString(R.string.open_source_key));
        openSourcePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/InfoSeeking/CoagmentoAndroid"));
                startActivity(browserIntent);
                return true;
            }
        });

        // Set up attributions
        Preference attributionsPref = findPreference(getString(R.string.attributions_key));
        attributionsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //Intent intent = new Intent(SettingsActivity.this, OpenSourceLicenses.class);
                return true;
            }
        });
    }
}
