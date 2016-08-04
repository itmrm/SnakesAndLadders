package com.eyalin.snakes.BL;

import android.util.Log;

import com.eyalin.snakes.Listeners.GameListener;

import java.util.ArrayList;

public class GameFollower implements AbsGame {

    final static String tag = "GameFollower";

    private ArrayList<GameListener> listeners;
    private Board board;
    private Player[] mPlayers;
    private int turn;

    public GameFollower(Player[] players) {
        mPlayers = players;
        board = new Board();
        turn = 1;
        listeners = new ArrayList<>();
    }

    @Override
    public void play(int steps) {
        Log.i(tag, "Start play turn of " + turn + ", Num of steps: " + steps);
        int place = mPlayers[turn].getPlace() + steps;
        Log.i(tag, "Place: " + place);
        if (place >= 99) {
            updateSteps(99);
            updateWin();
            return;
        } else {
            updateSteps(place);
            mPlayers[turn].setPlace(place);
            Shortcut shortcut = board.getTile(place).getShortcut();
            while (shortcut != null) {
                place = shortcut.getExit().getNum();
                mPlayers[turn].setPlace(place);
                fireShortcut(shortcut);
                shortcut = board.getTile(place).getShortcut();
            }
            if (turn >= mPlayers.length - 1)
                turn = 0;
            else
                ++turn;
            changeTurn();
        }
    }

    public void updateSteps(int place) {
        int lastPlace = mPlayers[turn].getPlace();
        place = place - lastPlace;
        for (GameListener l : listeners)
            l.makeSteps(place);
    }

    public void changeTurn() {
        for (GameListener l : listeners)
            l.turnChanged();
    }

    public void updateWin() {
        for (GameListener l : listeners)
            l.gameOver(mPlayers[turn]);
    }

    public void fireShortcut(Shortcut shortcut) {
        for (GameListener l : listeners)
            l.makeShortcut(shortcut.getExit().getNum());
    }

    public void fireShortcutChanged(int index) {

    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public void setShortcut(Shortcut shortcut, int index) {
        Shortcut[] shortcuts = board.getShortcuts();
        shortcuts[index] = shortcut;
        fireShortcutChanged(index);
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
