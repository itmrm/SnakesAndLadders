package com.eyalin.snakes.BL;


public class Player {

    private String mName;
    private int mPlace;

    public Player (String name) {
        mName = name;
        mPlace = 0;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getPlace() {
        return mPlace;
    }

    public void setPlace(int place) {
        mPlace = place;
    }

    public int makeMove() {
        return (int) (1 + Math.random() * 6);
    }
}
