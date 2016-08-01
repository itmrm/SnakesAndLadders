package com.eyalin.snakes.Server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.eyalin.snakes.BL.AbsGame;
import com.eyalin.snakes.BL.Shortcut;
import com.eyalin.snakes.Listeners.GameListener;
import com.eyalin.snakes.LoginActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public  class RoomPlayModel extends AppCompatActivity implements RoomStatusUpdateListener,
        RoomUpdateListener,RealTimeMultiplayer.ReliableMessageSentCallback,
        View.OnClickListener,OnInvitationReceivedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RealTimeMessageReceivedListener, GameListener, Serializable {

    final static String tag = "RoomPlayModel";

    public static Room roomPlay;
    public static Player currentPlayer;
    public static boolean isCreator = true;
    public static GoogleApiClient mGoogleApiClient;
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;
    public static  String mIncomingInvitationId;
    public final static int RC_SELECT_PLAYERS = 10000;
    public final static int RC_WAITING_ROOM = 10002;
    public final static int MIN_PLAYERS = 2;
    public boolean mPlaying = false;
    public String mRoomId = "2";
    private  Context mContext;

    int GamePlayerStatus = 0; // 0 - single player, 1 - leader, 2 - follower.
    private AbsGame mGame;

    public RoomPlayModel(Context context) {
        this.mContext = context;
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
        initialVariables();
    }

    private void initialVariables() {
        isCreator = true;
    }

    //need to invoke in the end of player's turn
    private void makeMove(GameStatus gameStatus) throws IOException {
        //insert any object instead of string, and make sure you parse it in message recieved
        byte[] message =  convertToBytes(gameStatus);


        for (Participant p : RoomPlayModel.roomPlay.getParticipants()) {
            if (!p.getPlayer().getPlayerId().equals(RoomPlayModel.currentPlayer.getPlayerId())) {
                Games.RealTimeMultiplayer.sendReliableMessage(RoomPlayModel.mGoogleApiClient, this, message,
                        RoomPlayModel.roomPlay.getRoomId(), p.getParticipantId());
            }
        }
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Player p = Games.Players.getCurrentPlayer(RoomPlayModel.mGoogleApiClient);
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
        currentPlayer = p;
        if (bundle != null) {
            Invitation inv =
                    bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(RoomPlayModel.mGoogleApiClient, roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // go to game screen
            }
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        if (request == RC_SELECT_PLAYERS) {
            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            Bundle extras = data.getExtras();
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get auto-match criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(RoomPlayModel.mGoogleApiClient, roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if (request == 10001)
        {
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept(mIncomingInvitationId);
            Games.RealTimeMultiplayer.join(RoomPlayModel.mGoogleApiClient, roomConfigBuilder.build());

// prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    public RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        isCreator = false;
        // show in-game popup to let user know of pending invitation
        Toast.makeText(
                mContext,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), TOAST_DELAY)
                .show();
        // store invitation for use when player accepts this invitation
        RoomPlayModel.mIncomingInvitationId = invitation.getInvitationId();
    }



    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

        try {
            GameStatus gameStatus = (GameStatus)convertFromBytes(realTimeMessage.getMessageData());
            if (gameStatus.steps != 0)
                mGame.play(gameStatus.steps);
            else if (gameStatus.index != -1)
                mGame.setShortcut(gameStatus.shortcut, gameStatus.index);
            else {
                int length = gameStatus.shortcuts.length;
                for (int i = 0; i < length; i++)
                    mGame.setShortcut(gameStatus.shortcuts[i], i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onRealTimeMessageSent(int i, int i1, String s) {
        //sent callback
    }
    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }

        // get waiting room intent
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(RoomPlayModel.mGoogleApiClient, room, Integer.MAX_VALUE);
        ((LoginActivity)mContext).startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }
            roomPlay = room;
        // get waiting room intent
        if(mGoogleApiClient.isConnected() != true)
        {

        }
        else {

            Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient,
                    room, Integer.MAX_VALUE);
            ((LoginActivity)mContext).startActivityForResult(i, RC_WAITING_ROOM);
        }

    }

    @Override
    public void onLeftRoom(int i, String s) {

    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        }

        RoomPlayModel.roomPlay = room;
        // roomPlay = room;
        // show error message, return to main screen.
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
        // peer left -- see if game should be canceled
        try {
            if (!mPlaying && shouldCancelGame(room)) {
                Games.RealTimeMultiplayer.leave(RoomPlayModel.mGoogleApiClient, null, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
        catch (Exception ex)
        {

        }

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }
    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return true;
    }
    @Override
    public void onPeersConnected(Room room, List<String> list) {
        if (mPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            // start game!
            roomPlay = room;
            ((LoginActivity)mContext).startGame();
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        if (mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
        } else if (shouldCancelGame(room)) {
            // cancel the game
            Games.RealTimeMultiplayer.leave(RoomPlayModel.mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void makeSteps(int steps) {
        Log.d(tag, "Send steps");
        GameStatus gameStatus = new GameStatus();
        gameStatus.steps = steps;
        try {
            makeMove(gameStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void turnChanged() {

    }

    @Override
    public void makeShortcut(int shortcut) {

    }

    @Override
    public void gameOver(com.eyalin.snakes.BL.Player winner) {

    }

    @Override
    public void shortcutChange(int index) {
        GameStatus gameStatus = new GameStatus();
        gameStatus.index = index;
        gameStatus.shortcut = mGame.getBoard().getShortcuts()[index];
        try {
            makeMove(gameStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGame(AbsGame game) {
        mGame = game;
        mGame.addListener(this);
    }

    public void setShortcuts(Shortcut[] shortcuts) {
        GameStatus gameStatus = new GameStatus();
        gameStatus.shortcuts = shortcuts;
        try {
            makeMove(gameStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
