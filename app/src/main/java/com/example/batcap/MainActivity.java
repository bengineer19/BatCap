package com.example.batcap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private boolean cutoffArmed = true;
    private int resetHysteresisPct = 5;
    private BroadcastReceiver batteryLevelReceiver;
    private boolean receiversRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions for camera, to use flash
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA}, 1);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        cutoffArmed = sharedPref.getBoolean("cutoffArmed", true);
        if(cutoffArmed){
            armCutoff();
        }else{
            disarmCutoff();
        }
        Log.d("cutoff", "Recovered value of cutoffArmed: " + String.valueOf(cutoffArmed));

        final Switch enableSwitch = findViewById(R.id.enableFunction);
        Button armBtn = findViewById(R.id.armBtn);

        // Setup listener for battery status change
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                Log.d("batt", "Percent:  " + String.valueOf(level));

                // Get value from UI
                EditText levelInput = findViewById(R.id.levelInput);
                int cutoff = Integer.parseInt(levelInput.getText().toString());

                if (level >= cutoff
                        && status == BatteryManager.BATTERY_STATUS_CHARGING
                        && enableSwitch.isChecked()
                        && cutoffArmed) {
                    Log.d("action", "Cutting power");
                    Log.d("arming", "DISARMING cutoff");
                    disarmCutoff();
                    LightFlasher.flashLight(context);
                }

                else if (level < (cutoff - resetHysteresisPct)) {
                    Log.d("arming", "ARMING cutoff");
                    armCutoff();
                }
            }
        };

//        final IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceivers();
//        registerReceiver(batteryLevelReceiver, batteryLevelFilter);

        armBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cutoffArmed) {
                    disarmCutoff();
                }
                else {
                    armCutoff();
                }
            }
        });
    }

    private void armCutoff() {
        cutoffArmed = true;
        Button armBtn = findViewById(R.id.armBtn);
        armBtn.setEnabled(false);
        armBtn.setText("Armed");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("cutoffArmed", true);
        prefEditor.commit();
    }

    private void disarmCutoff() {
        cutoffArmed = false;
        Button armBtn = findViewById(R.id.armBtn);
        armBtn.setEnabled(true);
        armBtn.setText("Arm");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("cutoffArmed", false);
        prefEditor.commit();
    }

    public void registerReceivers() {
        // Only register if not already registered
        if (!receiversRegistered) {
            registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            receiversRegistered = true;
        }
    }

    @Override
    protected void onResume() {
        registerReceivers();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        registerReceivers();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (receiversRegistered) {
            unregisterReceiver(batteryLevelReceiver);
            receiversRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiversRegistered) {
            unregisterReceiver(batteryLevelReceiver);
        }
    }
}
