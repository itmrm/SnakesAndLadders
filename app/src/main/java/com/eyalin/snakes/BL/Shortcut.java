package com.eyalin.snakes.BL;

public class Shortcut {

    private Tile mEnter;
    private Tile mExit;
    private ShortType mType;
    private int mLength;

    public Shortcut(Tile enter, Tile exit, ShortType type, int length) {
        mEnter = enter;
        mExit = exit;
        mType = type;
        mLength = length;
    }

    public void moveShortcut(Tile enter, Tile exit) {
        if(mEnter != null)
            mEnter.setShortcut(null);
        mEnter = enter;
        mExit = exit;
        mEnter.setShortcut(this);
    }

    public Tile getmEnter() {
        return mEnter;
    }

    public Tile getExit() {
        return mExit;
    }

    public ShortType getType() {
        return mType;
    }

    public int getLength() {
        return mLength;
    }

    protected void setLength(int length) {
        mLength = length;
    }

}
