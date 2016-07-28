package com.eyalin.snakes.BL;


import android.util.Log;

public class Board {

    static final String tag = "Board";

    private Tile[] tiles;
    private final int LENGTH = 100;
    private Shortcut[] shortcuts;
    private final int SHORTS_NUM = 4;

    public Board() {
        Log.i(tag, "Setting Board");
        tiles = new Tile[LENGTH];
        for (int i = 0; i < LENGTH; i++)
            tiles[i] = new Tile(i);
        Log.i(tag, "Tiles set.");
        shortcuts = new Shortcut[SHORTS_NUM * 2];
        Log.i(tag, "Shortcuts array created.");
        int length = 10;
        for (int i = 0; i < SHORTS_NUM; i++) {
            shortcuts[i] = new Shortcut(null, null, ShortType.LADDER, length);
            replaceShortcut(shortcuts[i]);
            length += 7;
        }
        length = -10;
        for (int i = SHORTS_NUM; i < SHORTS_NUM * 2; i++) {
            shortcuts[i] = new Shortcut(null, null, ShortType.SNAKE, length);
            replaceShortcut(shortcuts[i]);
            length -= 7;
        }
        Log.i(tag, "Shortcut, set.");
    }

    public void replaceShortcut(Shortcut shortcut) {
        Log.i(tag, "Replacing shortcut.");
        int min, max, enter;
        if (shortcut.getType() == ShortType.LADDER) {
            min = 1;
            max = 99 - shortcut.getLength();
        }
        else {
            min = 1 - shortcut.getLength();
            max = 98;
        }
        int exit;
        do {
            enter = (int) (min + Math.random() * (max - min + 1));
            exit = enter + shortcut.getLength();
            Log.i(tag, "Enter: " + enter);
        } while (tiles[enter].getShortcut() != null && exit != 99);
        shortcut.moveShortcut(tiles[enter], tiles[exit]);
    }

    public Tile getTile(int num) {
        if (tiles[num] == null)
            Log.e(tag, "Tile num " + num + " is null.");
        else
            Log.e(tag, "Tile num " + num + " is O.K.");
        return tiles[num];
    }

    public Shortcut[] getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(Shortcut[] shortcuts) {
        this.shortcuts = shortcuts;
    }

}
