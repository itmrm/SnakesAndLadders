package com.eyalin.snakes.UI;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import com.eyalin.snakes.BL.Board;
import com.eyalin.snakes.BL.ShortType;
import com.eyalin.snakes.BL.Shortcut;
import com.eyalin.snakes.Listeners.ShortListener;

import java.util.ArrayList;

public class ShortcutManager implements Animator.AnimatorListener {

    final static String tag = "ShortcutManager";

    private Shortcut[] mShortcuts;
    private ShortcutView[] mImages;
    private Board mBoard;
    private GridView mGrid;
    private ArrayList<ShortListener> listeners;

    public ShortcutManager(GridView grid, ShortcutView[] images, Board board) {
        mBoard = board;
        mGrid = grid;
        mImages = images;
        mShortcuts = mBoard.getShortcuts();
        listeners = new ArrayList<>();
    }

    public void initShortcuts() {
        for (int i = 0; i < 8; i++) {
            int num = mShortcuts[i].getmEnter().getNum();
            View view = mGrid.getChildAt(0);
            int x = view.getWidth();
            int y = view.getHeight();
            Log.i(tag, "Setting Image no." + i + ", Width: " + x + "Heigth: " + y + ".");
            if (i < 4)
                mImages[i].setParameters(x, y, ShortType.LADDER, num);
            else
                mImages[i].setParameters(x, y, ShortType.SNAKE, num);
            placeShortcut(i);
        }
    }

    private void placeShortcut(int i) {
        int place = mShortcuts[i].getmEnter().getNum();
        place = (int) mGrid.getAdapter().getItemId(place);
        View view = mGrid.getChildAt(place);
        int target = mShortcuts[i].getExit().getNum();
        mImages[i].setNumber(target);
        mImages[i].invalidate();
        mImages[i].animate().x(view.getLeft() + 30).y(view.getTop() + 30);
        mImages[i].animate().setDuration(1000).setListener(this);
        mImages[i].animate().start();
    }

    public void updateShortcut(int num) {
        placeShortcut(num);
    }

    public void addListener(ShortListener l) {
        listeners.add(l);
    }

    public void removeListener(ShortListener l) {
        listeners.remove(l);
    }


    @Override
    public void onAnimationCancel(Animator animator) {
        for (ShortListener l : listeners)
            l.shortcutInPlace();
    }

    @Override
    public void onAnimationStart(Animator animator) {
        for (ShortListener l : listeners)
            l.shortcutMoving();
    }

    @Override
    public void onAnimationEnd(Animator animator) {
        Log.i(tag, "Animation End.");
        for (ShortListener l : listeners)
            l.shortcutInPlace();
    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

}
