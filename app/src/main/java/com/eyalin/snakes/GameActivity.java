package com.eyalin.snakes;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eyalin.snakes.BL.AbsGame;
import com.eyalin.snakes.BL.Game;
import com.eyalin.snakes.BL.GameFollower;
import com.eyalin.snakes.BL.Player;
import com.eyalin.snakes.Listeners.GameListener;
import com.eyalin.snakes.Listeners.PawnListener;
import com.eyalin.snakes.Listeners.ShortListener;
import com.eyalin.snakes.Server.RoomPlayModel;
import com.eyalin.snakes.UI.BoardAdapter;
import com.eyalin.snakes.UI.PawnManager;
import com.eyalin.snakes.UI.ShortcutManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
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

public class GameActivity extends AppCompatActivity implements GameListener,
        PawnListener, ShortListener,RealTimeMultiplayer.ReliableMessageSentCallback,
        View.OnClickListener,OnInvitationReceivedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoomUpdateListener,RoomStatusUpdateListener,
        RealTimeMessageReceivedListener {

    final static String tag = "GameActivity";

    private int mode;

    private AbsGame game;
    private Player[] players;
    private GridView boardGrid;
    private BoardAdapter boardAdapter;
    private ImageView pawn1;
    private ImageView pawn2;
    private PawnManager pManager1;
    private PawnManager pManager2;
    private ImageView dice;
    private ImageView fakeDice;
    private MediaPlayer diceSound;
    private AnimationDrawable rollAnimation;
    private int player;
    private ShortcutManager shortcuts;
    private ImageView[] shortcutImages;
    private boolean gameStarted = false;
    private boolean shortsInplace;
    private boolean pawnInPlace;
    private TextView playerTxt;
    private TextView phoneTxt;
    //private RoomPlayModel roomPlayModel;


    //Multi
    static final String MULTI_KEY = "Multiplayer";
    static final String ROOM = "room";

    boolean mBound = false;

    //Buttons
    private SignInButton btn_SignIn;
    private Button btn_SignOut;
    private Button btn_Invite;
    private Button btn_SeeInventations;
    private boolean mSignInClicked = false;

    private RoomPlayModel roomPlayModel;
    final static int RC_WAITING_ROOM = 10002;
    int REQUEST_CODE_RESOLVE_ERR = 1000;
    private String mRoomId = "2";
    final static int RC_INVITATION_INBOX = 10001;
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;
    // private  Player currentPlayer;
    final static int RC_SELECT_PLAYERS = 10000;
    private  String mIncomingInvitationId;
    private   Button button;
    private EditText editText;
    //private   Room roomPlay;
    // are we already playing?
    boolean mPlaying = false;
    // at least 2 players required for our game
    final static int MIN_PLAYERS = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(tag, "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Log.i(tag, "Layout set.");

        mode = 0;
        String pName;
        String eName;

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(WelcomeActivity.BUNDLE_KEY);
        if (bundle != null) {
            mode = bundle.getInt(WelcomeActivity.MODE_KEY);
        }
        pName = "Player";
        eName = "Phone";

        players = new Player[]{new Player(pName), new Player(eName)};
        playerTxt = (TextView) findViewById(R.id.player_name);
        playerTxt.setText(pName);
        phoneTxt = (TextView) findViewById(R.id.friend_name);
        phoneTxt.setText(eName);
        Log.i(tag, "Players set.");
        if (mode != 2) {
            game = new Game(players);
            player = 0;
        }
        else {
            game = new GameFollower(players);
            player = 1;
        }

        Log.i(tag, "Game generated.");

        pawn1 = (ImageView) findViewById(R.id.pawn1);
        pawn2 = (ImageView) findViewById(R.id.pawn2);
        Log.i(tag, "Game generated.");

        boardAdapter = new BoardAdapter(this, game.getBoard());
        boardGrid = (GridView) findViewById(R.id.board_grid);
        boardGrid.setAdapter(boardAdapter);
        Log.i(tag, "Board generated.");

        initDice();

        setShortcuts();
        shortcuts = new ShortcutManager(boardGrid, shortcutImages, game.getBoard());
        shortsInplace = true;
        pawnInPlace = false;

        //Multi
        if(mode != 0) {
            RoomPlayModel.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        initialActivity();

    }

    private void initDice() {
        dice = (ImageView) findViewById(R.id.dice);
        dice.setBackgroundResource(R.drawable.dice_animation);
        rollAnimation = (AnimationDrawable) dice.getBackground();
        fakeDice = (ImageView) findViewById(R.id.fake_dice);
        fakeDice.setVisibility(View.INVISIBLE);
        diceSound = MediaPlayer.create(this, R.raw.roll_dice);

    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && dice.isClickable()) {
            fakeDice.setVisibility(View.INVISIBLE);
            rollAnimation.stop();
            rollAnimation.start();
            diceSound.start();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP && dice.isClickable()) {
            rollAnimation.stop();
            dice.setClickable(false);
            gameStarted = true;
            int steps = (int)(1 + Math.random() * 6);
            setFakeDice(steps);
            fakeDice.setVisibility(View.VISIBLE);
            Log.i(tag, "Player steps: " + steps);
            game.play(steps);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void setFakeDice(int steps) {
        switch (steps) {
            case 1: fakeDice.setImageResource(R.drawable.dice1);
                break;
            case 2: fakeDice.setImageResource(R.drawable.dice2);
                break;
            case 3: fakeDice.setImageResource(R.drawable.dice3);
                break;
            case 4: fakeDice.setImageResource(R.drawable.dice4);
                break;
            case 5: fakeDice.setImageResource(R.drawable.dice5);
                break;
            default: fakeDice.setImageResource(R.drawable.dice6);
        }
    }

    private void setShortcuts() {
        shortcutImages = new ImageView[16];
        shortcutImages[0] = (ImageView) findViewById(R.id.shortcut0);
        shortcutImages[1] = (ImageView) findViewById(R.id.shortcut1);
        shortcutImages[2] = (ImageView) findViewById(R.id.shortcut2);
        shortcutImages[3] = (ImageView) findViewById(R.id.shortcut3);
        shortcutImages[4] = (ImageView) findViewById(R.id.shortcut4);
        shortcutImages[5] = (ImageView) findViewById(R.id.shortcut5);
        shortcutImages[6] = (ImageView) findViewById(R.id.shortcut6);
        shortcutImages[7] = (ImageView) findViewById(R.id.shortcut7);
        shortcutImages[8] = (ImageView) findViewById(R.id.shortcut8);
        shortcutImages[9] = (ImageView) findViewById(R.id.shortcut9);
        shortcutImages[10] = (ImageView) findViewById(R.id.shortcut10);
        shortcutImages[11] = (ImageView) findViewById(R.id.shortcut11);
        shortcutImages[12] = (ImageView) findViewById(R.id.shortcut12);
        shortcutImages[13] = (ImageView) findViewById(R.id.shortcut13);
        shortcutImages[14] = (ImageView) findViewById(R.id.shortcut14);
        shortcutImages[15] = (ImageView) findViewById(R.id.shortcut15);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        shortcuts.initShortcuts();
        shortcuts.addListener(GameActivity.this);
        pManager1 = new PawnManager(pawn1, boardGrid, players[0], 0, dice);
        pManager2 = new PawnManager(pawn2, boardGrid, players[1], 20, dice);
        pManager1.addListener(GameActivity.this);
        pManager2.addListener(GameActivity.this);
        game.addListener(GameActivity.this);
        if (player == 0)
            dice.setClickable(true);
        else
            dice.setClickable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast t = Toast.makeText(this, R.string.directions,
                Toast.LENGTH_LONG);
        t.show();
        //Multi
        if (mode != 0) {
            RoomPlayModel.mGoogleApiClient.connect();
        }
    }

    private void play() {
        if (player == 0) {
            playerTxt.setBackgroundColor(Color.RED);

            phoneTxt.setBackgroundColor(Color.WHITE);
        }
        else {
            playerTxt.setBackgroundColor(Color.WHITE);
            phoneTxt.setBackgroundColor(Color.RED);
        }
        if (!gameStarted)
            return;
        shortsInplace = true;
        pawnInPlace = false;
        Log.i(tag, "Play: " + player);
        if (player == 1) {
            if (mode == 0) {
                fakeDice.setVisibility(View.INVISIBLE);
                diceSound.start();
                rollAnimation.stop();
                rollAnimation.start();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int steps = players[1].makeMove();
                        stopPhoneDice(steps);
                    }
                });
                t.start();
            }
        } else {
            dice.setClickable(true);
        }
    }

    private void stopPhoneDice(final int steps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setFakeDice(steps);
                rollAnimation.stop();
                fakeDice.setVisibility(View.VISIBLE);
                game.play(steps);
            }
        });
    }

    @Override
    public void updateEndOfMovment() {
        Log.i(tag, "End of pawn movment.");
        pawnInPlace = true;
        if (shortsInplace)
            play();
    }

    @Override
    public void makeSteps(int steps) {
        Log.i(tag, "Player: " + player + "; Steps: " + steps);
        if (player == 0)
            if (steps > 0)
                pManager1.makeStep(steps);
            else
                pManager1.makeReverseStep(steps);
        else
        if (steps > 0)
            pManager2.makeStep(steps);
        else
            pManager2.makeReverseStep(steps);

    }

    @Override
    public void turnChanged() {
        if (player == 0) {
            ++player;
        }
        else {
            --player;
        }
    }

    @Override
    public void makeShortcut(int surtcut) {
        if (player == 0)
            pManager1.makeShortcut(surtcut);
        else
            pManager2.makeShortcut(surtcut);
    }

    @Override
    public void gameOver(Player winner) {
        Log.i(tag, "The winner:" + winner.getName());
        dice.setClickable(false);
        gameStarted = false;
        if (player == 0)
            playerTxt.setText(R.string.is_winner);
        else
            phoneTxt.setText(R.string.is_winner);
    }

    @Override
    public void shortcutChange(final int index) {
        if (!gameStarted)
            return;
        Log.i(tag, "Shortcut changed.");
        shortsInplace = false;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!pawnInPlace) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                GameActivity.this.setShortcut(index);
            }
        });
        thread.start();
    }

    private void setShortcut(final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shortcuts.updateShortcut(index);
            }
        });
    }

    @Override
    public void shortcutMoving() {

    }

    @Override
    public void shortcutInPlace() {
        Log.i(tag, "New shortcut is in place.");
        if(gameStarted)
            play();
    }



    //#################################################################################
    //                MultiplayRoom
    //#################################################################################

    private void initialActivity() {
        btn_SignIn = (SignInButton) findViewById(R.id.sign_in_button);
        setGooglePlusButtonText(btn_SignIn,"Sign in to play online");

        btn_SignOut = (Button)findViewById(R.id.sign_out_button);
        btn_Invite = (Button)findViewById(R.id.invite_button);
        btn_SeeInventations = (Button)findViewById(R.id.onInvitationReceived);

        btn_SignIn.setOnClickListener(this);
        btn_SignOut.setOnClickListener(this);
        btn_Invite.setOnClickListener(this);
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
        else if(view.getId() == R.id.invite_button) {
            Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(RoomPlayModel.mGoogleApiClient, 1, 3);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        }

        else if(view.getId() == R.id.onInvitationReceived) {
            seeInventations();
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
        if(mode!=0) {
            RoomPlayModel.mGoogleApiClient.disconnect();
            ((TextView) findViewById(R.id.lbHeader)).setText("not connected");
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        setOnlineButtonVisibility(true);
        // Show sign-out button on main menu
//        roomPlayModel = RoomPlayModel.getInstance(this);
        // show sign-out button, hide the sign-in button
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        // Set the greeting appropriately on main menu
        com.google.android.gms.games.Player p = Games.Players.getCurrentPlayer(RoomPlayModel.mGoogleApiClient);
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

    private void seeInventations() {
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
        else if (request == 10001) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            //unbindService(mConnection);
            mBound = false;
        }
    }


}
