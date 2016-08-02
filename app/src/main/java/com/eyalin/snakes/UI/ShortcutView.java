package com.eyalin.snakes.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.eyalin.snakes.BL.ShortType;

public class ShortcutView extends View {

    final static String tag = "ShortcutView";

    private ShapeDrawable circle;
    private int x;
    private int y;
    private int width;
    private int height;
    private ShortType mType;
    private Paint numberPaint;
    private int number;

    public ShortcutView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.i(tag, "Shortcut created");
        x = 20;
        y = 20;
        width = (int) 0.8 * x;
        height = (int) 0.8 * x;
        mType = ShortType.LADDER;

        circle = new ShapeDrawable(new OvalShape());
        if (mType == ShortType.LADDER)
            circle.getPaint().setColor(Color.WHITE);
        else
            circle.getPaint().setColor(Color.RED);

        circle.setBounds(x, y, x + width, y + height);

        number = 0;
        numberPaint = new TextPaint();
        numberPaint.setColor(Color.BLUE);
        numberPaint.setTextSize(5);
        numberPaint.setFakeBoldText(true);
    }

    public void setParameters(int sizeX, int sizeY, ShortType type, int num) {
        width = sizeX/2;
        height = width;
        x = (int) 0.9 * width;
        y = x;
        mType = type;

        circle = new ShapeDrawable(new OvalShape());
        if (mType == ShortType.LADDER)
            circle.getPaint().setColor(Color.WHITE);
        else
            circle.getPaint().setColor(Color.RED);

        circle.setBounds(x, y, x + width, y + height);

        number = num + 1;
        numberPaint = new Paint();
        numberPaint.setColor(Color.BLUE);
        numberPaint.setTextSize(30);
        numberPaint.setFakeBoldText(true);
    }

    public void setNumber(int num) {
        number = num + 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        circle.draw(canvas);
        canvas.drawText("" + number, width/3, height/2, numberPaint);
    }
}
