package com.google.android.exoplayer.demo.player;

import java.util.TimerTask;

/**
 * Created by chavez on 7/19/15.
 */
public class FrameListener extends TimerTask {

    public DemoPlayer player;
    long cuePosition = 3000;
    int offset = 2000;



   public FrameListener(DemoPlayer _player){
        player = _player;
    }
    @Override
    public void run() {
        long playerFrame = player.getCurrentPosition();

        //System.out.println( String.valueOf( (int)playerFrame));
        //if ( (int)playerFrame > (cuePosition + offset) && (int)playerFrame < (cuePosition - offset))
        if (playerFrame > 1000 && playerFrame<10000)
        {
            System.out.println( String.valueOf(playerFrame));
        }

    }
}
