package com.eyalin.snakes;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import com.eyalin.snakes.UI.ShortcutView;

import org.w3c.dom.Text;

public class GameActivity extends AppCompatActivity implements GameListener,
        PawnListener, ShortListener {

    final static String tag = "GameActivity";

    private RoomPlayModel room;
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
        Bundle bundle = intent.getBundleExtra(LoginActivity.MULTI_KEY);
        if (bundle != null) {
            mode = bundle.getInt(LoginActivity.MODE_KEY);
            pName = bundle.getString(LoginActivity.PLAYER_NAME);
            eName = bundle.getString(LoginActivity.FRIEND_NAME);
        }
        else {
            pName = "Player";
            eName = "Phone";
        }
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

}
