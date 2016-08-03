package com.eyalin.snakes.Server;


import com.eyalin.snakes.BL.Shortcut;

import java.io.Serializable;

public class GameStatus implements Serializable {

    int steps = 0, index = -1;
    Shortcut shortcut;
    Shortcut[] shortcuts = null;

}
