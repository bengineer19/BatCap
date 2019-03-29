package com.example.batcap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                Log.d("batt", "Percent:  " + String.valueOf(level));

                // Get value from UI
                EditText levelInput = (EditText) findViewById(R.id.levelInput);
                int cutoff = Integer.parseInt(levelInput.getText().toString());

                if(status == BatteryManager.BATTERY_STATUS_CHARGING && level >= cutoff) {
                    Log.d("action", "Cutting power");
                }
            }
        };

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);

        /*final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MonitorWorker.class).build();

        Switch enableSwitch = (Switch) findViewById(R.id.enableFunction);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Log.d("enableSwitch","Enabled");
                    WorkManager.getInstance().enqueue(workRequest);
                }
                else
                {
                    Log.d("enableSwitch","Disabled");
                }

            }
        });*/
    }

}
