package com.eyalin.snakes.BL;


public class Tile {

    private final int mNum;
    private Shortcut mShortcut = null;

    public Tile(int num) {
        mNum = num;
    }

    public int getNum() {
        return mNum;
    }

    public Shortcut getShortcut() {
        return mShortcut;
    }

    public void setShortcut(Shortcut shortcut) {
        mShortcut = shortcut;
    }
}
