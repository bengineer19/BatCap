package com.example.batcap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class BattCheckerService extends IntentService {

    private int resetHysteresisPct = 5;
    private int checkDelayMillis = 1000;

    public BattCheckerService() {
        super("BattCheckerService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service", "Handling intent");

        startMyOwnForeground();

        try {
            while (true) {
                Thread.sleep(checkDelayMillis);
//                Log.d("action", "Checking Battery...");
                checkBatt();
            }
        }catch (InterruptedException e) {}

    }

    private void checkBatt(){
        Intent intent = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

//        Log.d("batt", "Percent:  " + String.valueOf(level));

        SharedPreferences sharedPref = getSharedPrefs();
        int cutoffLevel = sharedPref.getInt("cutoffLevel", 802);
//        Log.d("batt", "cutoffLevel:  " + String.valueOf(cutoffLevel));

        boolean enabled = sharedPref.getBoolean("enabled", true);
        boolean cutoffArmed = sharedPref.getBoolean("cutoffArmed", true);

        if (level >= cutoffLevel
                && status == BatteryManager.BATTERY_STATUS_CHARGING
                && enabled
                && cutoffArmed) {

            Log.d("action", "Flashing light");
            // LightFlasher currently failing :( Probs a thread/handler thing
            LightFlasher.flashLight(this);
            CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);
            } catch (CameraAccessException e) {
            }



            Log.d("arming", "DISARMING cutoff");
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putBoolean("cutoffArmed", false);
            prefEditor.commit();

            stopForeground(true);
            stopSelf();
        }

        else if (level < (cutoffLevel - resetHysteresisPct) && !cutoffArmed) {
            Log.d("arming", "ARMING cutoff");
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putBoolean("cutoffArmed", true);
            prefEditor.commit();
        }
    }

    private SharedPreferences getSharedPrefs(){
        return getApplicationContext().getSharedPreferences(getString(R.string.pref_file_name),
                Context.MODE_PRIVATE);
    }

    private void startMyOwnForeground(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                getNotifBuilder(this, "BatCap.Checker", NotificationManager.IMPORTANCE_LOW);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_batt_charging)
                .setContentTitle("BatCap")
                .setContentText("Monitoring Battery level")
                .setContentIntent(pendingIntent)
                .setTicker("ticker")
                .build();

        Log.d("Service", "Starting foreground service...");
        startForeground(2, notification);
    }


    public static NotificationCompat.Builder getNotifBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        prepareChannel(context, channelId, importance);
        builder = new NotificationCompat.Builder(context, channelId);
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = "ok!";
        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nm.createNotificationChannel(nChannel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "BattCheckerService exiting", Toast.LENGTH_SHORT).show();
    }
}
