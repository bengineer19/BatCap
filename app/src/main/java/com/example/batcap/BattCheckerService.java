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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class BattCheckerService extends IntentService {

    public BattCheckerService() {
        super("BattCheckerService");
    }

    Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            // Do the thing
            Log.d("action", "Been timed innit");

            timerHandler.postDelayed(this, 1500);
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service", "Handling intent");

        startMyOwnForeground();
        try {
            Thread.sleep(1000);
//            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }

        try {
            while (true) {
                Thread.sleep(1000);
                Log.d("action", "OK lol");
            }
        }catch (InterruptedException e) {}

        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void startMyOwnForeground(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                getNotifBuilder(this, "BatCap.Checker", NotificationManager.IMPORTANCE_LOW);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.tick)
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
        Toast.makeText(this, "BattCheckerService done", Toast.LENGTH_SHORT).show();
    }
}
