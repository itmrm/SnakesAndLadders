package com.eyalin.snakes;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.eyalin.snakes.Server.Communicator;
import com.eyalin.snakes.Server.RoomPlayModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
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
import com.google.android.gms.plus.Plus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements
        RealTimeMultiplayer.ReliableMessageSentCallback,
        View.OnClickListener,OnInvitationReceivedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoomUpdateListener,RoomStatusUpdateListener,
        RealTimeMessageReceivedListener {

    static final String tag = "LoginActivity";
    static final String MULTI_KEY = "Multiplayer";
    static final String PLAYER_NAME = "player";
    static final String FRIEND_NAME = "friend";
    static final String MODE_KEY = "GameMode";

    private Communicator mService;
    private boolean mBound = false;

    //Buttons
    private SignInButton btn_SignIn;
    private Button btn_SignOut;
    private Button btn_Invite;
    private Button btn_StartGame;
    private Button btn_SeeInventations;

    boolean mExplicitSignOut = false;
    int REQUEST_CODE_RESOLVE_ERR = 1000;
    boolean mInSignInFlow = false; // set to true when you're in the middle of the
    private RoomPlayModel roomPlayModel;
    final static int RC_WAITING_ROOM = 10002;
    private String mRoomId = "2";
   // private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    final static int RC_INVITATION_INBOX = 10001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;
    private ConnectionResult mConnectionResult;
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;
    private static final long ROLE_1 = 0x1; // 001 in binary
    private static final long ROLE_2 = 0x2; // 010 in binary
    private static final long ROLE_WIZARD = 0x4; // 100 in binary
   // private  Player currentPlayer;
    final static int RC_SELECT_PLAYERS = 10000;
    private  String mIncomingInvitationId;
    private   Button button;
    private   EditText editText;
    //private   Room roomPlay;
    // are we already playing?
    boolean mPlaying = false;
    // at least 2 players required for our game
    final static int MIN_PLAYERS = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // launch the player selection screen

        // Create the Google Api Client with access to Plus and Games
        RoomPlayModel.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        initialActivity();

    }

    private void initialActivity()
    {
        btn_SignIn = (SignInButton) findViewById(R.id.sign_in_button);
        setGooglePlusButtonText(btn_SignIn,"Sign in to play online");


        btn_SignOut = (Button)findViewById(R.id.sign_out_button);
        btn_Invite = (Button)findViewById(R.id.invite_button);
        btn_SeeInventations = (Button)findViewById(R.id.onInvitationReceived);
        btn_StartGame = (Button)findViewById(R.id.btnStartGame);


        btn_SignIn.setOnClickListener(this);
        btn_SignOut.setOnClickListener(this);
        btn_Invite.setOnClickListener(this);
        btn_StartGame.setOnClickListener(this);
        btn_SeeInventations.setOnClickListener(this);

        setOnlineButtonVisibility(false);


        //button.setEnabled(false);
       // editText.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            // Check to see the developer who's running this sample code read the instructions :-)
            // NOTE: this check is here only because this is a sample! Don't include this
            // check in your actual production app.


            // start the sign-in flow
            Log.d(tag, "Sign-in button8 clicked");
            mSignInClicked = true;
            RoomPlayModel.mGoogleApiClient.connect();

        }
        else if (view.getId() == R.id.sign_out_button) {
        // sign out.
            mSignInClicked = false;
            try {
                boolean test =RoomPlayModel. mGoogleApiClient.isConnected();
                Games.signOut(RoomPlayModel.mGoogleApiClient);
            // show sign-out button, hide the sign-in button
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        else if(view.getId() == R.id.invite_button)
        {
            Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(RoomPlayModel.mGoogleApiClient, 1, 3);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        }

        else if(view.getId() == R.id.onInvitationReceived)
        {
            seeInventations();
        }
        else if(view.getId() == R.id.btnStartGame)
        {
            startGame();
        }

    }

    //need to invoke in the end of player's turn
   private void endTurn()
   {
       //insert any object instead of string, and make sure you parse it in message recieved
       byte[] message = editText.getText().toString().getBytes();


       for (Participant p : RoomPlayModel.roomPlay.getParticipants()) {
           if (!p.getPlayer().getPlayerId().equals(RoomPlayModel.currentPlayer.getPlayerId())) {
               Games.RealTimeMultiplayer.sendReliableMessage(RoomPlayModel.mGoogleApiClient, this, message,
                       RoomPlayModel.roomPlay.getRoomId(), p.getParticipantId());
           }
       }
   }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            RoomPlayModel.mGoogleApiClient.connect();
        }

    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        // show in-game popup to let user know of pending invitation
        Toast.makeText(
                this,
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
    protected void onStop() {
        super.onStop();
        RoomPlayModel.mGoogleApiClient.disconnect();
        ((TextView)findViewById(R.id.lbHeader)).setText("not connected" );
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        setOnlineButtonVisibility(true);
        // Show sign-out button on main menu
        roomPlayModel = new RoomPlayModel(this);
        // show sign-out button, hide the sign-in button
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(RoomPlayModel.mGoogleApiClient);
        RoomPlayModel.currentPlayer = p;
        String displayName;
        if (p == null) {
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        if (connectionHint != null) {
            Invitation inv =
                    connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder = roomPlayModel.makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(RoomPlayModel.mGoogleApiClient, roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // go to game screen
            }
        }
        ((TextView)findViewById(R.id.lbHeader)).setText("Hello " + displayName);



    }

    private void seeInventations()
    {
        // launch the intent to show the invitation inbox screen
        Intent intent = Games.Invitations.getInvitationInboxIntent(RoomPlayModel.mGoogleApiClient);
        this.startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        int errorCode = result.getErrorCode();


        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                RoomPlayModel.mGoogleApiClient.connect();
            }
        }
        Log.e(tag, "Connection Failed, error Code: " + errorCode);

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
            RoomConfig.Builder roomConfigBuilder = roomPlayModel.makeBasicRoomConfigBuilder();
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
            RoomConfig.Builder roomConfigBuilder = roomPlayModel.makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept(roomPlayModel.mIncomingInvitationId);
            Games.RealTimeMultiplayer.join(RoomPlayModel.mGoogleApiClient, roomConfigBuilder.build());

// prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }


    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        RoomPlayModel.mGoogleApiClient.connect();
    }

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = roomPlayModel.makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(RoomPlayModel.mGoogleApiClient, roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }

        // get waiting room intent
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(RoomPlayModel.mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }
    //    roomPlay = room;
        // get waiting room intent
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(RoomPlayModel.mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
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
        editText.setText("You Are Connected to Multi Player", TextView.BufferType.EDITABLE);
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        button.setEnabled(true);
        RoomPlayModel.roomPlay = room;
       // roomPlay = room;
        // show error message, return to main screen.
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
    public void onPeersConnected(Room room, List<String> peers) {
        if (mPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            startGame();
            // start game!
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
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
    public void onPeerLeft(Room room, List<String> peers) {
        // peer left -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(RoomPlayModel.mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

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
    public void onPeerDeclined(Room room, List<String> peers) {
        // peer declined invitation -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(RoomPlayModel.mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

        //someone joined room
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        button.setEnabled(true);
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        try {
            String string = new String(realTimeMessage.getMessageData(), "UTF-8");
            editText.setText(string);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRealTimeMessageSent(int i, int i1, String s) {
//        button.setEnabled(false);
//        editText.setEnabled(false);
//        editText.setInputType(InputType.TYPE_NULL);
    }

    //change Sign in Text
    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.f);

                return;
            }
        }
    }

    private void setOnlineButtonVisibility(boolean isOnline) {
        //here we handle the multi player.
        //if isOnline true, so this player is online

        if (  isOnline  ) {
            btn_SignOut.setVisibility(View.VISIBLE);
            btn_SignIn.setVisibility(View.GONE);
            btn_Invite.setVisibility(View.VISIBLE);
            btn_SeeInventations.setVisibility(View.VISIBLE);
        }
        else {
            btn_SignOut.setVisibility(View.GONE);
            btn_SignIn.setVisibility(View.VISIBLE);
            btn_Invite.setVisibility(View.GONE);
            btn_SeeInventations.setVisibility(View.GONE);
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Communicator.LocalBinder binder = (Communicator.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void startGame() {
        Intent intent = new Intent(this, Communicator.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mService.setRoomPlayModel(roomPlayModel);

        Intent gameIntent = new Intent(LoginActivity.this, GameActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(PLAYER_NAME, roomPlayModel.currentPlayer.getName());
        String name = "";
        for (Participant p : RoomPlayModel.roomPlay.getParticipants()) {
            if (!p.getPlayer().getPlayerId().equals(RoomPlayModel.currentPlayer.getPlayerId()))
                name = p.getPlayer().getName();
            }
        bundle.putString(FRIEND_NAME, name);
        if (roomPlayModel.isCreator)
            bundle.putInt(MODE_KEY, 1);
        else
            bundle.putInt(MODE_KEY, 2);

        gameIntent.putExtra(MULTI_KEY, bundle);

        startActivity(gameIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}
