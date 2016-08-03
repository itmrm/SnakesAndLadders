package com.eyalin.snakes.Server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eyalin.snakes.Listeners.ServerListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.List;

public class Communicator extends Service implements
        RealTimeMultiplayer.ReliableMessageSentCallback,
        View.OnClickListener,OnInvitationReceivedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoomUpdateListener,RoomStatusUpdateListener,
        RealTimeMessageReceivedListener {

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

    public void setRoomPlayModel(GoogleApiClient.ConnectionCallbacks cBacks,
                                 GoogleApiClient.OnConnectionFailedListener fListener) {
        // Create the Google Api Client with access to Plus and Games
        RoomPlayModel.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(cBacks).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(fListener).addOnConnectionFailedListener(this)
                .build();
    }

    public RoomPlayModel getRoomPlayModel() {
        if (roomPlayModel == null)
            Log.e(tag, "Room is null.");
        return roomPlayModel;
    }

    public void addListener(ServerListener listener) {
        mListeners.add(listener);
        Log.e(tag, "Register listener.");
    }

    public void removeListener(ServerListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(tag, "Service destroyed.");
    }

    public class LocalBinder extends Binder {

        public Communicator getService() {
            return Communicator.this;
        }

    }

    //###########################################################################################
    //                       RommPlayModel Listeners
    //###########################################################################################

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInvitationReceived(Invitation invitation) {

    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

    }

    @Override
    public void onRealTimeMessageSent(int i, int i1, String s) {

    }

    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {

    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int i, Room room) {

    }

    @Override
    public void onJoinedRoom(int i, Room room) {

    }

    @Override
    public void onLeftRoom(int i, String s) {

    }

    @Override
    public void onRoomConnected(int i, Room room) {

    }

}
