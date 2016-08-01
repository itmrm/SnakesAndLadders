package com.eyalin.snakes.BL;

import com.eyalin.snakes.Listeners.GameListener;

public interface AbsGame {

    void play(int steps);
    Board getBoard();
    void setShortcut(Shortcut shortcut ,int index);
    void addListener(GameListener listener);
    void removeListener(GameListener listener);

}
