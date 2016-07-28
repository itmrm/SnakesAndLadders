package com.eyalin.snakes.UI;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

import com.eyalin.snakes.BL.Tile;

public class TileView extends View {

    private ShapeDrawable smallDrawable;
    private ShapeDrawable bigDrawable;
    private ShapeDrawable player;
    private int mWidth;
    private int mHeight;
    int x;
    int y;
    Tile mTile;


    public TileView(Context context, Tile tile) {
        super(context);
        mTile = tile;

        x = 10;
        y = 10;
        mWidth = 100 + x;
        mHeight = 100 + y;

        smallDrawable = new ShapeDrawable(new RectShape());
        smallDrawable.getPaint().setColor(setColor());
        smallDrawable.setBounds(x, y, mWidth - x, mHeight - y);

        bigDrawable = new ShapeDrawable(new RectShape());
        bigDrawable.getPaint().setColor(0xff000000);
        bigDrawable.setBounds(0, 0, mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        bigDrawable.draw(canvas);
        smallDrawable.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(25);
        canvas.drawText("" + (mTile.getNum() + 1), 20, 35, paint);
    }

    private int setColor() {
//        int num = mTile.getNum() % 10;
//        num %= 7;
        int num = (int) (Math.random() * 7);
        int color;
        switch (num) {
            case 0: color = Color.GRAY;
                break;
            case 1: color = Color.RED;
                break;
            case 2: color = Color.GREEN;
                break;
            case 3: color = Color.YELLOW;
                break;
            case 4: color = Color.BLUE;
                break;
            case 5: color = Color.MAGENTA;
                break;
            default: color = Color.CYAN;
        }
        return color;
    }

    public Tile getTile() {
        return mTile;
    }

}
