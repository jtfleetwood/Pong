package com.gamecodeschool.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PongGame extends SurfaceView implements Runnable {
    private final boolean DEBUGGING = true;
    private SurfaceHolder mOurHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    private long mFPS;
    private final int MILLIS_IN_SECOND = 1000;
    private int mScreenX;
    private int mScreenY;
    private int mFontSize;
    private int mFontMargin;
    private Bat mBat;
    private Ball mBall;
    private int mScore;
    private int mLives;
    private Thread mGameThread = null;
    // Volatile makes it safe to access variable from inside and outside of the thread.
    private volatile boolean mPlaying;
    private boolean mPaused = true;

    public PongGame(Context context, int x, int y){

        // of type view class that takes in type context class within activity class. Gives surface view information relating to object state.
        super(context);
        mScreenX = x;
        mScreenY = y;

        mFontSize = mScreenY / 20;
        mFontMargin = mScreenX / 75;

        mOurHolder = getHolder();
        mPaint = new Paint();

        startNewGame();

    }

    public void startNewGame() {
        mScore = 0;
        mLives = 3;

    }

    private void draw() {
        // Validates that area of memory that we want to manipulate to represent our frame of drawing is available.
        // Drawing/processing takes place asynchronously with the code that detects player input and OS messages.
        // Code executing 60 times a second, and we need to confirm that we have access to the memory before we access.
        if (mOurHolder.getSurface().isValid()) {
            // Lock the canvas (graphics memory) ready to draw.
            // Ensures that while we are accessing the memory here, no other code can access it.
            mCanvas = mOurHolder.lockCanvas();
            mCanvas.drawColor(Color.argb(255, 26, 128, 182));

            mPaint.setTextSize(mFontSize);

            mPaint.setColor(Color.argb(255, 255, 255, 255));

            mCanvas.drawText("Score: " + mScore + "   Lives: " + mLives, mFontMargin, mFontSize, mPaint);

            if (DEBUGGING){
                printDebuggingText();
            }


            // Frees up the memory to be accessed again, and posts the new canvas.
            // Happens every single frame of animation.
            mOurHolder.unlockCanvasAndPost(mCanvas);

        }
    }

    private void printDebuggingText() {
        int debugSize = mFontSize / 2;
        int debugStart = 150;

        mPaint.setTextSize(debugSize);

        mCanvas.drawText("FPS: " + mFPS, 25, debugStart - 50, mPaint);
    }

    // Game loop is implemented below.
    // What thread runs.
    // Is the parameter passed into the thread variable.
    @Override
    public void run() {
        while (mPlaying) {
            long frameStartTime = System.currentTimeMillis();

            if (!mPaused){
                update();
                detectCollisions();
            }

            draw();

            long timeThisFrame = System.currentTimeMillis() - frameStartTime;

            if (timeThisFrame > 0) {
                mFPS = MILLIS_IN_SECOND / timeThisFrame;
            }
        }

    }

    private void update() {
        // updating object data.
    }

    private void detectCollisions() {

    }

    // Called when player quits the game.
    public void pause() {
        mPlaying = false;

        try {
            // stopping the thread.
            mGameThread.join();
        }

        catch (InterruptedException e){
            Log.e("Error:", "joining thread");
        }
    }

    // Called when player starts game.
    public void resume() {
        mPlaying = true;

        mGameThread = new Thread(this);

        mGameThread.start();
    }
}
