package com.example.android.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Mohamed Yasser on 9/21/2015.
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private MyThread thread;

    public MySurfaceView(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        thread = new MyThread(surfaceHolder,this);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
            thread.setRunning(true);
            thread.start();
    }



    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        while(retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.setWillNotDraw(false);

        Paint black = new Paint();
        black.setStyle(Paint.Style.STROKE);
        black.setColor(Color.BLACK);
        black.setAntiAlias(true);

        canvas.drawColor(Color.BLUE);
        canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2
                , this.getWidth() / 2, black);

        this.getHolder().unlockCanvasAndPost(canvas);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}
