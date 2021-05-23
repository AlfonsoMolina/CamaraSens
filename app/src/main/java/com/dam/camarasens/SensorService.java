package com.dam.camarasens;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;

public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mCompass;
    private float lastOrientation;

    private static final String TAG = "SensorService";

    public SensorService(){

    }

    public SensorService(SensorManager manager) {
        Log.d(TAG, "Creando servicio");

        lastOrientation = 0;

        mSensorManager = manager;
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

    }


    public void startListening() {
        Log.d(TAG, "Escuchando sensores");
        mSensorManager.registerListener(this, mCompass,
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void stopListening() {
        Log.d(TAG, "Fin de la escucha de sensores");
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopListening();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
            lastOrientation = Math.round(event.values[0]);
    }


    public float getLastOrientation(){
        return lastOrientation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
