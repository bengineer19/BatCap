package com.example.batcap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private boolean cutoffArmed = true;
    private int defaultCutoffLevel = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions for camera, to use flash
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA}, 1);

        SharedPreferences sharedPref = getSharedPrefs();
        cutoffArmed = sharedPref.getBoolean("cutoffArmed", true);
        if(cutoffArmed){
            armCutoff();
        }else{
            disarmCutoff();
        }
        // Update UI with sharedPrefs
        EditText levelInput = findViewById(R.id.levelInput);
        Switch enableSwitch = findViewById(R.id.enableFunction);
        levelInput.setText(String.valueOf(sharedPref.getInt("cutoffLevel", defaultCutoffLevel)));
        enableSwitch.setChecked(sharedPref.getBoolean("enabled", true));

        // If we should be monitoring, start backend service
        if(enabled() && cutoffArmed){
            startBattCheckerService();
        }

        setUIListeners();

        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    // Listen for any updates to the `cutoffArmed` preference, which the `BattCheckerService` might
    // have made
    SharedPreferences.OnSharedPreferenceChangeListener listener = new
SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals("cutoffArmed")) {
                if(prefs.getBoolean("cutoffArmed", true)){
                    armCutoff();
                }
                else {
                    disarmCutoff();
                }
            }
        }
    };

    private SharedPreferences getSharedPrefs(){
        return getApplicationContext().getSharedPreferences(getString(R.string.pref_file_name),
                        Context.MODE_PRIVATE);
    }

    private void startBattCheckerService(){
        // Start background service which watches the battery level
        Intent intent = new Intent(this, BattCheckerService.class);
        startService(intent);
        Toast.makeText(getApplication(), "Starting monitoring service",
                Toast.LENGTH_LONG).show();
    }

    private void armCutoff() {
        cutoffArmed = true;
        Button armBtn = findViewById(R.id.armBtn);
        armBtn.setEnabled(false);
        armBtn.setText("Armed");
        SharedPreferences sharedPref = getSharedPrefs();
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("cutoffArmed", true);
        prefEditor.commit();
    }


    private void disarmCutoff() {
        cutoffArmed = false;
        Button armBtn = findViewById(R.id.armBtn);
        armBtn.setEnabled(true);
        armBtn.setText("Arm");
        SharedPreferences sharedPref = getSharedPrefs();
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("cutoffArmed", false);
        prefEditor.commit();
    }

    private void updateCutoffLevel(String level) {
        SharedPreferences sharedPref = getSharedPrefs();
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        try {
            prefEditor.putInt("cutoffLevel", Integer.parseInt(level));
        }
        catch (NumberFormatException e) {
            Log.e("error", "Could not convert '" + level + "' to Int");
        }
        prefEditor.commit();
    }

    private boolean enabled(){
        Switch enableSwitch = findViewById(R.id.enableFunction);
        return enableSwitch.isChecked();
    }

    private void setUIListeners(){
        Button armBtn = findViewById(R.id.armBtn);
        EditText levelInput = findViewById(R.id.levelInput);
        Switch enableSwitch = findViewById(R.id.enableFunction);

        armBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cutoffArmed) {
                    disarmCutoff();
                }
                else {
                    armCutoff();
                    if(enabled()){
                        startBattCheckerService();
                    }
                }
            }
        });

        levelInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateCutoffLevel(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = getSharedPrefs();
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putBoolean("enabled", isChecked);
                prefEditor.commit();
                if(cutoffArmed){
                    startBattCheckerService();
                }
            }
        });
    }
}
