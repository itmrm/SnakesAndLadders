package com.eyalin.snakes.UI;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.eyalin.snakes.BL.Board;

public class BoardAdapter extends BaseAdapter {

    static final String tag = "BoardAdapter";

    private Context mContext;
    private Board mBoard;

    public BoardAdapter(Context context, Board board) {
        mContext = context;
        mBoard = board;
        Log.i(tag, "set BoardAdapter.");
    }

    @Override
    public int getCount() {
        return 100;
    }

    @Override
    public Object getItem(int i) {
        return mBoard.getTile(i);
    }

    @Override
    public long getItemId(int i) {
        int tile = i / 10;
        if(tile % 2 != 0)
            tile = 99 - i;
        else
            tile = 90 - 10 * tile + (i % 10);
        return tile;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View tileView;
        Log.i(tag, "Setting pos no." + i);
        if (view == null) {
            int position = translatePosition(i);
            tileView = new TileView(mContext, mBoard.getTile(position));
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
            tileView.setLayoutParams(new GridView.LayoutParams(dm.widthPixels/8,dm.widthPixels/10));
        }
        else
            tileView = (TileView) view;
        return tileView;
    }

    public int translatePosition(int position) {
        int tile = position / 10;
        if(tile % 2 == 0)
            tile = 99 - position;
        else
            tile = 90 - 10 * tile + (position % 10);
        return tile;
    }

}
