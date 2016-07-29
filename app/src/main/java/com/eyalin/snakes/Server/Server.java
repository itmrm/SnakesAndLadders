package com.eyalin.snakes.Server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.eyalin.snakes.Listeners.ServerListener;

import java.util.ArrayList;

public class Server extends Service {

    final static String tag = "Server";

    private final IBinder mBinder = new LocalBinder();
    private ArrayList<ServerListener> mListeners;

    @Override
    public void onCreate() {
        super.onCreate();
        // Get the ACCELEROMETER sensore.

        mListeners = new ArrayList<>();
        Log.e(tag, "onCreate.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(tag, "Activity bind");
        return mBinder;
    }

    public void addListener(ServerListener listener) {
        mListeners.add(listener);
        // set the device zero position.
        Log.e(tag, "Register listener.");
    }

    public void removeListener(ServerListener listener) {
        mListeners.remove(listener);
    }

    public class LocalBinder extends Binder {
        Server getService() {
            return Server.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "Service destroyed.");
    }

}
