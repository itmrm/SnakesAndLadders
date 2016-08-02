package com.eyalin.snakes.BL;


import android.util.Log;

import com.eyalin.snakes.Listeners.GameListener;

import java.util.ArrayList;

public class Game implements AbsGame {

    final static String tag = "Game";

    private ArrayList<GameListener> listeners;
    private Board board;
    private Player[] mPlayers;
    private int turn;
    private int movement;

    public Game(Player[] players) {
        Log.i(tag, "Setting game.");
        mPlayers = players;
        Log.i(tag, "Players set.");
        board = new Board();
        Log.i(tag, "Board set.");
        turn = 0;
        setMovement();
        Log.i(tag, "Movement set.");
        listeners = new ArrayList<>();
    }

    private void setMovement() {
        movement = (int) (1 + Math.random() * 5);
    }

    @Override
    public void play(int steps) {
        Log.i(tag, "Start play turn of " + turn + ", Num of steps: " + steps);
        int place = mPlayers[turn].getPlace() + steps;
        Log.i(tag, "Place: " + place);
        if(place >= 99) {
            updateSteps(99);
            updateWin();
            return;
        }
        else {
            updateSteps(place);
            mPlayers[turn].setPlace(place);
            Shortcut shortcut = board.getTile(place).getShortcut();
            while (shortcut != null) {
                place = shortcut.getExit().getNum();
                mPlayers[turn].setPlace(place);
                fireShortcut(shortcut);
                shortcut = board.getTile(place).getShortcut();
            }
            if (movement == 0) {
                moveShortcut();
                setMovement();
            }
            else
                movement--;
            if(turn >= mPlayers.length - 1)
                turn = 0;
            else
                ++turn;
            changeTurn();
        }
    }

    private void updateSteps(int place) {
        int lastPlace = mPlayers[turn].getPlace();
        place = place - lastPlace;
        for (GameListener l : listeners)
            l.makeSteps(place);
    }

    public void changeTurn() {
        for (GameListener l : listeners)
            l.turnChanged();
    }

    private void updateWin() {
        for (GameListener l : listeners)
            l.gameOver(mPlayers[turn]);
    }

    private void fireShortcut(Shortcut shortcut) {
        for (GameListener l : listeners)
            l.makeShortcut(shortcut.getExit().getNum());
    }

    private void fireShortcutChanged(int index) {
        for (GameListener l : listeners)
            l.shortcutChange(index);
    }

    private void moveShortcut() {
        Shortcut[] shortcuts = board.getShortcuts();
        int choose = (int) (Math.random() * shortcuts.length);
        board.replaceShortcut(shortcuts[choose]);
        fireShortcutChanged(choose);
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public void setShortcut(Shortcut shortcut ,int index) {

    }

    @Override
    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

}
