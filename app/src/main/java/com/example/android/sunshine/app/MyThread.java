package com.example.android.sunshine.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Mohamed Yasser on 9/21/2015.
 */
public class MyThread extends Thread {
    private boolean running;
    SurfaceHolder surfaceHolder;
    SurfaceView surfaceView;

    public MyThread(SurfaceHolder surfaceHolder,SurfaceView surfaceView){
        this.surfaceHolder = surfaceHolder;
        this.surfaceView = surfaceView;
    }

    public void setRunning(boolean running){
        this.running = running;
    }



    @Override
    public void run() {
        while(running){

            Canvas canvas = null;
            try{
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder){
                    surfaceView.draw(canvas);
                }
            }finally {
                if(canvas!=null){
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
//                if(currentThread().isInterrupted() && !surfaceHolder.getSurface().isValid()) {
//                    continue;
//                }
//            surfaceView.draw(surfaceHolder.lockCanvas());

        }


            // update game state
            // render state to the screen

    }

}
