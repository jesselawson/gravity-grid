package com.turkey.gravitygrid;

/**
 * Created by lawsonje on 12/2/2016.
 */

public final class GameOptions {

    private int playSound;

    GameOptions() {
        playSound = 1;
    }

    public boolean playSounds() {
        return (playSound == 1 ? true : false);
    }

    public void togglePlaySounds() {
        if(playSound == 1) {
            playSound = 0;
        } else {
            playSound = 1;
        }

    }
}
