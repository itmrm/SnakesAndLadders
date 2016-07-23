package com.eyalin.snakes;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



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

public class MainActivity extends AppCompatActivity implements RealTimeMultiplayer.ReliableMessageSentCallback, View.OnClickListener,OnInvitationReceivedListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,RoomUpdateListener,RoomStatusUpdateListener, RealTimeMessageReceivedListener {


    //Buttons
    private SignInButton btn_SignIn;
    private Button btn_SignOut;
    private Button btn_Invite;
    private Button btn_SeeInventations;



    // request code for the "select players" UI
    // can be any number as long as it's unique
    // are we already playing?
    boolean mExplicitSignOut = false;
    int REQUEST_CODE_RESOLVE_ERR = 1000;
    boolean mInSignInFlow = false; // set to true when you're in the middle of the
    // sign in flow, to know you should not attempt
    // to connect in onStart()
    // at least 2 players required for our game
    // arbitrary request code for the waiting room UI.
// This can be any integer that's unique in your Activity.
    final static int RC_WAITING_ROOM = 10002;
    private String mRoomId = "2";
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    // request code (can be any number, as long as it's unique)
    final static int RC_INVITATION_INBOX = 10001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;
    private ConnectionResult mConnectionResult;
    // How long to show toasts.
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;
    private static final long ROLE_1 = 0x1; // 001 in binary
    private static final long ROLE_2 = 0x2; // 010 in binary
    private static final long ROLE_WIZARD = 0x4; // 100 in binary
    private  Player currentPlayer;
    // request code for the "select players" UI
// can be any number as long as it's unique
    final static int RC_SELECT_PLAYERS = 10000;
    private  String mIncomingInvitationId;
private   Button button;
    private   EditText editText;
    private   Room roomPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // launch the player selection screen

        //  Games.GamesOptions gamesOptions = Games.GamesOptions.builder().setRequireGooglePlus(true).build();
        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        btn_SignIn = (SignInButton) findViewById(R.id.sign_in_button);
        setGooglePlusButtonText(btn_SignIn,"Sign in to play online");


        btn_SignOut = (Button)findViewById(R.id.sign_out_button);
        btn_Invite = (Button)findViewById(R.id.invite_button);
        btn_SeeInventations = (Button)findViewById(R.id.onInvitationReceived);

        btn_SignIn.setOnClickListener(this);
        btn_SignOut.setOnClickListener(this);
        btn_Invite.setOnClickListener(this);
        btn_SeeInventations.setOnClickListener(this);

        setOnlineButtonVisablity(false);

        findViewById(R.id.startQuickGame).setOnClickListener(this);
        findViewById(R.id.SendMessage).setOnClickListener(this);

         button = (Button)findViewById(R.id.SendMessage);
        button.setEnabled(false);
         editText = (EditText)findViewById(R.id.edit_message);
        editText.setInputType(InputType.TYPE_NULL);

    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            // Check to see the developer who's running this sample code read the instructions :-)
            // NOTE: this check is here only because this is a sample! Don't include this
            // check in your actual production app.


            // start the sign-in flow
            Log.d("Game", "Sign-in button8 clicked");
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
        else if (view.getId() == R.id.sign_out_button) {
            // sign out.
            mSignInClicked = false;
            try {
                boolean test = mGoogleApiClient.isConnected();
                Games.signOut(mGoogleApiClient);
// show sign-out button, hide the sign-in button
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
        else if(view.getId() == R.id.invite_button)
        {
            Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        }
        else if(view.getId() == R.id.startQuickGame)
        {
            startQuickGame();
        }
        else if(view.getId() == R.id.onInvitationReceived)
        {
            seeInventations();
        }
        else if(view.getId() == R.id.SendMessage)
        {

            byte[] message = editText.getText().toString().getBytes();
            for (Participant p : roomPlay.getParticipants()) {
                if (!p.getPlayer().getPlayerId().equals(currentPlayer.getPlayerId())) {
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, this, message,
                            roomPlay.getRoomId(), p.getParticipantId());
                }
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();
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
        mIncomingInvitationId = invitation.getInvitationId();
    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        ((TextView)findViewById(R.id.lbHeader)).setText("not connected" );
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        setOnlineButtonVisablity(true);
        // Show sign-out button on main menu
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
        // show sign-out button, hide the sign-in button
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        currentPlayer = p;
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
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

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
        Intent intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
        this.startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        int errorCode = result.getErrorCode();


        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }
//        // Save the result and resolve the connection failure upon a user click.
//        mConnectionResult = result;

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
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if (request == 10001)
        {
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept(mIncomingInvitationId);
            Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

// prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

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
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
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
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
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
        roomPlay = room;
        // show error message, return to main screen.
    }
    // are we already playing?
    boolean mPlaying = false;

    // at least 2 players required for our game
    final static int MIN_PLAYERS = 2;

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
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
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
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
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
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

        boolean test = true;
        test = false;
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
        button.setEnabled(false);
        editText.setEnabled(false);
        editText.setInputType(InputType.TYPE_NULL);
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

    private void setOnlineButtonVisablity(boolean isOnline)
    {
        if (  isOnline  )
        {
            btn_SignOut.setVisibility(View.VISIBLE);
            btn_SignIn.setVisibility(View.GONE);
            btn_Invite.setVisibility(View.VISIBLE);
            btn_SeeInventations.setVisibility(View.VISIBLE);
        }
        else
        {
            btn_SignOut.setVisibility(View.GONE);
            btn_SignIn.setVisibility(View.VISIBLE);
            btn_Invite.setVisibility(View.GONE);
            btn_SeeInventations.setVisibility(View.GONE);
        }

    }
}
