package com.example.batcap;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;

public class LightFlasher {

//    public LightFlasher(Context context){
//        this.context = context;
//    }

    static public void flashLight(final Context cn) {
        int flashDurationMillis = 100;
        int pauseDurationMillis = 100;
        int numFlashes = 3;

//        On(cn);
        int offset = 0;

        for(int i = 0; i < numFlashes; i++) {
            offset = i * (flashDurationMillis + pauseDurationMillis);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    On(cn);
                }
            }, pauseDurationMillis + offset);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Off(cn);
                }
            }, flashDurationMillis + pauseDurationMillis + offset);
        }
    }

    static private void On(Context cn) {
        CameraManager cameraManager = (CameraManager) cn.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
        }
    }

    static private void Off(Context cn) {
        CameraManager cameraManager = (CameraManager) cn.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
        }
    }
}
