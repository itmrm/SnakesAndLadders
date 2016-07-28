package com.eyalin.snakes.Listeners;

import com.eyalin.snakes.BL.Player;

public interface GameListener {

    void makeSteps(int steps);
    void turnChanged();
    void makeShortcut(int surtcut);
    void gameOver(Player winner);
    void shortcutChange(int index);

}
