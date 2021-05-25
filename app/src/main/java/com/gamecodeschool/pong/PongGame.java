package com.gamecodeschool.pong;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class PongGame extends SurfaceView implements Runnable {
    private final boolean DEBUGGING = true;
    private final SurfaceHolder mOurHolder;
    private Canvas mCanvas;
    private final Paint mPaint;
    private long mFPS;
    private final int MILLIS_IN_SECOND = 1000;
    private final int mScreenX;
    private final int mScreenY;
    private final int mFontSize;
    private final int mFontMargin;
    private int mScore;
    private int mLives;
    private Thread mGameThread = null;
    // Volatile makes it safe to access variable from inside and outside of the thread.
    private volatile boolean mPlaying;
    private boolean mPaused = true;
    private Bat mBat;
    private Ball mBall;
    private SoundPool mSP;
    private int mBeepID = -1;
    private int mBoopID = -1;
    private int mBopID = -1;
    private int mMissID = -1;

    public PongGame(Context context, int x, int y){

        // of type view class that takes in type context class within activity class. Gives surface view information relating to object state.
        super(context);
        mScreenX = x;
        mScreenY = y;

        mFontSize = mScreenY / 20;
        mFontMargin = mScreenX / 75;

        mOurHolder = getHolder();
        mPaint = new Paint();
        mBall = new Ball(mScreenX);
        mBat = new Bat(mScreenX, mScreenY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            mSP = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();

        }

        else {
            Log.d("Error:", "Did not have updated version.\n");
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("beep.ogg");
            mBeepID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("boop.ogg");
            mBoopID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("bop.ogg");
            mBopID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("miss.ogg");
            mMissID = mSP.load(descriptor, 0);

        }

        catch (IOException e) {
            Log.d("Error", "Failed to load sound files.");
        }

        startNewGame();

    }

    public void startNewGame() {

        mScore = 0;
        mLives = 3;

        mBall.reset(mScreenX, mScreenY);

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
            mCanvas.drawRect(mBall.getRect(), mPaint);
            mCanvas.drawRect(mBat.getRect(), mPaint);

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
        mBall.update(mFPS);
        mBat.update(mFPS);
    }

    private void detectCollisions() {
        if(RectF.intersects(mBat.getRect(), mBall.getRect())) {
            mBall.batBounce(mBat.getRect());
            mBall.increaseVelocity();
            mScore ++;
            mSP.play(mBeepID, 1, 1, 0, 0, 1);
        }

        if (mBall.getRect().bottom > mScreenY) {
            mBall.reverseYVelocity();
            mLives--;
            mSP.play(mMissID, 1, 1, 0, 0, 1);

            if (mLives == 0) {
                mPaused = true;

                startNewGame();
            }
        }

        if (mBall.getRect().top < 0) {
            mBall.reverseYVelocity();
            mSP.play(mBoopID, 1, 1, 0, 0, 1);
        }

        if (mBall.getRect().left < 0) {
            mBall.reverseXVelocity();
            mSP.play(mBopID, 1, 1, 0, 0, 1);
        }

        if (mBall.getRect().right > mScreenX) {
            mBall.reverseXVelocity();
            mSP.play(mBopID, 1, 1, 0, 0, 1);
        }
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

    @Override

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch(motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPaused = false;

                if (motionEvent.getX() > mScreenX / 2) {
                    mBat.SetMovementState(mBat.RIGHT);
                }

                else {
                    mBat.SetMovementState(mBat.LEFT);
                }

                break;

            case MotionEvent.ACTION_UP:
                mBat.SetMovementState(mBat.STOPPED);

                break;
        }

        return true;
    }
}
