package com.eyalin.snakes.BL;


import android.util.Log;

import com.eyalin.snakes.Listeners.GameListener;

import java.util.ArrayList;

public class Game {

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

    public void play(int steps) {
        Log.i(tag, "Start play turn of " + turn + ", Num of steps: " + steps);
        int place = mPlayers[turn].getPlace() + steps;
        Log.i(tag, "Place: " + place);
        if(place == 99) {
            updateSteps(place);
            updateWin();
            return;
        }
        else {
            if (place > 99)
                place = 198 - place;
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

    private void changeTurn() {
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

    public Board getBoard() {
        return board;
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

}
