package com.eyalin.snakes.Server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.eyalin.snakes.Listeners.ServerListener;

import java.util.ArrayList;

public class Communicator extends Service {

    final static String tag = "Communicator";

    private final IBinder mBinder = new LocalBinder();
    private ArrayList<ServerListener> mListeners;
    private RoomPlayModel roomPlayModel;

    @Override
    public void onCreate() {
        super.onCreate();

        mListeners = new ArrayList<>();
        Log.e(tag, "onCreate.");
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(tag, "Activity bind");
        return mBinder;
    }

    public void setRoomPlayModel(RoomPlayModel room) {
        roomPlayModel = room;
    }

    public RoomPlayModel getRoomPlayModel() {
        return roomPlayModel;
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

        public Communicator getService() {
            return Communicator.this;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "Service destroyed.");
    }


    public void setMessageRecieved() {

    }

}
