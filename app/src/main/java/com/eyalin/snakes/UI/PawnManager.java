package com.eyalin.snakes.UI;


import android.animation.Animator;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.eyalin.snakes.BL.Player;
import com.eyalin.snakes.Listeners.PawnListener;

import java.util.ArrayList;

public class PawnManager implements Animator.AnimatorListener {

    final static String tag = "PawnMovment";

    private GridView mGrid;
    private ImageView mPawn;
    private ArrayList<PawnListener> listeners;

    private final int DIV;
    private float xPos = 0;
    private float yPos = 0;
    private int location;
    private int time = 800;
    private boolean ready = true;
    private Player mPlayer;
    private ArrayList<Integer> nextSeps;
    private boolean win;
    private View mDice;

    public PawnManager(ImageView pawn, GridView board,Player player, int div, View dice) {
        mPawn = pawn;
        mGrid = board;
        location = 0;
        mPlayer = player;
        DIV = div;
        win = false;
        mDice = dice;
        listeners = new ArrayList<>();
        mPawn.animate().setListener(this);
        nextSeps = new ArrayList<>();
        mPawn.animate().setDuration(time);
        goTo(location);
    }

    private void goTo(int newLocation) {
        int pos = (int) mGrid.getAdapter().getItemId(newLocation);
        View view = mGrid.getChildAt(pos);
        Log.i(tag, "Go to: " + newLocation + ", Chiled at: " + pos);
        xPos = view.getX() + 10 + DIV;
        yPos = view.getY() + 10;
        Log.i(tag, "------" + xPos + " , " + yPos + "------");
        mPawn.animate().x(xPos).y(yPos);
        mPawn.animate().start();
        if(pos == 0)
            win = true;
    }

    public void makeStep(int steps) {
        for (int i = 0; i < steps; i++) {
            ++location;
            nextSeps.add(location);
        }
        if (ready)
            playNext();
    }

    public void makeReverseStep(int steps) {
        for (int i = 0; i < steps; i++) {
            --location;
            nextSeps.add(location);
        }
        if (ready)
            playNext();
    }

    public void makeShortcut(int shortcut) {
        location = shortcut;
        nextSeps.add(location);
        if (ready)
            playNext();
    }

    private void winDance() {
        xPos = mDice.getLeft() + 30;
        yPos = mDice.getTop() - 50;
        mPawn.setMinimumHeight(100);
        mPawn.setMinimumWidth(100);
        mPawn.invalidate();
        mPawn.animate().x(xPos).y(yPos);
        mPawn.animate().rotationY(700);
        mPawn.animate().setDuration(time * 20);
        mPawn.animate().start();
    }


    @Override
    public void onAnimationStart(Animator animator) {
        ready = false;
    }

    @Override
    public void onAnimationEnd(Animator animator) {
        if (!win)
            playNext();
        else {
            winDance();
        }
    }

    @Override
    public void onAnimationCancel(Animator animator) {
        playNext();
    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    private void playNext() {
        if(nextSeps.isEmpty()) {
            ready = true;
            for (PawnListener l : listeners)
                l.updateEndOfMovment();
        }
        else {
            Integer next = nextSeps.get(0);
            nextSeps.remove(0);
            goTo(next);
        }
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public void addListener(PawnListener l) {
        listeners.add(l);
    }

    public void removeListener(PawnListener l) {
        listeners.remove(l);
    }

}
