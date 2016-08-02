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
    private ImageView[] mImages;
    private final int LENGTH;
    private Board mBoard;
    private GridView mGrid;
    private ArrayList<ShortListener> listeners;

    public ShortcutManager(GridView grid, ImageView[] images, Board board) {
        mBoard = board;
        mGrid = grid;
        mImages = images;
        LENGTH = mImages.length / 2;
        mShortcuts = mBoard.getShortcuts();
        listeners = new ArrayList<>();
    }

    public void initShortcuts() {
        for (int i = 0; i < 8; i++) {
            placeShortcut(i);
        }
    }

    private void placeShortcut(int i) {
        int place = mShortcuts[i].getmEnter().getNum();
        place = (int) mGrid.getAdapter().getItemId(place);
        View enter = mGrid.getChildAt(place);
        int x = enter.getWidth();
        int y = enter.getHeight();
        mImages[i].getLayoutParams().width = x;
        mImages[i].getLayoutParams().height = y;
        mImages[i].requestLayout();
        int target = mShortcuts[i].getExit().getNum();
        target = (int) mGrid.getAdapter().getItemId(target);
        View exit = mGrid.getChildAt(target);
        mImages[i + LENGTH].getLayoutParams().width = x;
        mImages[i + LENGTH].getLayoutParams().height = y;
        mImages[i + LENGTH].requestLayout();
        mImages[i].animate().x(enter.getLeft()).y(enter.getTop());
        mImages[i].animate().setDuration(1000);
        mImages[i].animate().start();

        mImages[i + LENGTH].requestLayout();
        mImages[i + LENGTH].animate().x(exit.getLeft()).y(exit.getTop());
        mImages[i + LENGTH].animate().setDuration(1000).setListener(this);
        mImages[i + LENGTH].animate().start();
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
