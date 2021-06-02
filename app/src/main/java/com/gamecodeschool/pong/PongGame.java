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

/**
 * This class extends the SurfaceView class and implements the Runnable interface. Allowing us to provide a view to the
 * user and provide some context as to how we want our thread (game loop) to execute. PongGame pretty much runs the all of the
 * mechanics that comes with user game play (view, drawing, collision detection, game loop implementation, etc.) As you can see
 * below this class is composed of many different objects that contribute to the above discussed functionality.
 */

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
    private Obstacle mObstacle;
    private Obstacle mAddObstacle;
    private Ball mBall;
    private SoundPool mSP;
    private int mBeepID = -1;
    private int mBoopID = -1;
    private int mBopID = -1;
    private int mMissID = -1;

    /**
     * Below we have our parametrized constructor.
     * @param context Passes in information regarding our user's current device state and display information. Is actually
     * passed into our Parent class's constructor so that our PongGame object (which is of type view) knows what to display
     * and how to display (resolution) if any previous activity was unfinished.
     * @param x Contains resolution x-size (# of pixels).
     * @param y Contains resolution y-size (# of pixels).
     * Both above parameters are used for formatting drawing activities.
     *
     * This parametrized constructor essentially accomplishes setting the user's view based off their existing context,
     * and properly initializes game objects/sounds based off screen resolution and device OS version. Once, all of
     * the above is properly set up we call to start a new game.
     * @exception IOException relates to attempting to load sound files through our asset manager (manages audio files).
     * @see PongGame#startNewGame()
     */
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
        mObstacle = new Obstacle(mScreenX, mScreenY);
        mAddObstacle = new Obstacle(mScreenX, mScreenY);

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

    /**
     * Below method starts a new game. Which requires resetting user score and number of lives. Also resetting the
     * positions of our objects in view (ball, and two obstacles used).
     */

    public void startNewGame() {

        mScore = 0;
        mLives = 3;

        mBall.reset(mScreenX, mScreenY);
        mObstacle.reset(0, 0);
        mAddObstacle.reset(mScreenX - 225, (float) (mScreenY / 2.5));

    }

    /**
     * Below draw() method controls all of our drawing operations and is called within our game loop.
     * @see PongGame#run()
     * Quick notes:
     * @see PongGame#mOurHolder Pretty much provides another layer to our user view which allows us to draw surfaces
     * from separate threads.
     * @see PongGame#mOurHolder#getSurface()#isValid() Since asynchronous execution of game loop and OS interaction is
     * occuring, we need to ensure the memory associated with our user's view or surface is not currently being manipulated before
     * we begin to draw on it.
     * @see PongGame#mOurHolder#lockCanvas() Locks surface to ensure no other component of our program tries to access
     * that memory while we are manipulating it.
     * @see PongGame#mOurHolder#unlockCanvasAndPost() Unlocks the memory associated with the surface and posts changes or
     * new drawings to the overall user view.
     *
     * This method is drawing the current positions of our ball, bat, and obstacles.
     */

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

            mPaint.setColor(Color.argb(255, 0, 0, 0));
            mCanvas.drawRect(mObstacle.getObstacle(), mPaint);
            mCanvas.drawRect(mAddObstacle.getObstacle(), mPaint);
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

    /**
     * Method used for debugging purposes. Displays our user's FPS to the screen as well.
     * @see PongGame#mFPS
     */

    private void printDebuggingText() {
        int debugSize = mFontSize / 2;
        int debugStart = 150;

        mPaint.setTextSize(debugSize);

        mCanvas.drawText("FPS: " + mFPS, 25, debugStart - 50, mPaint);

    }

    /**
     * Below is our game loop that is overridden through implementing the Runnable interface.
     * Our game loop consists of:
     * 1. Updating objects (moving them, collision detection)
     * 2. Drawing updated position of objects
     * @see PongGame#draw()
     * 3. Responding to any touches from the user.
     *
     */
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

    /**
     * This method will be called within our game loop.
     * @see PongGame#run()
     *
     * These update methods are individual to each type of object and pretty much move our objects' coordinates based
     * off movement velocity and FPS. Notice how an FPS value is passed into each update method.
     */
    private void update() {
        mBall.update(mFPS);
        mBat.update(mFPS);
        mObstacle.update(mFPS);
        mAddObstacle.update(mFPS);
    }

    /**
     * This method controls all collision detection and further needed actions upon a collision. Such as a ball hitting
     * the left boundary of the surface, and then reversing it's x velocity to keep it within the screen view. We are
     * also updating score and number of lives for the user based off collisions occurring (if ball hits bottom of screen,
     * then the player loses a life, etc.)
     * @see Ball#batBounce(RectF)
     * @see Ball#increaseVelocity()
     * @see Ball#reverseXVelocity()
     * @see Ball#reverseYVelocity()
     * @see Obstacle#reverseVelocity()
     */
    private void detectCollisions() {

        if(RectF.intersects(mBat.getRect(), mBall.getRect())) {
            mBall.batBounce(mBat.getRect());
            mBall.increaseVelocity();
            mScore++;
            mSP.play(mBeepID, 1, 1, 0, 0, 1);
        }

        if (RectF.intersects(mObstacle.getObstacle(), mBall.getRect())) {
            mBall.batBounce(mObstacle.getObstacle());
        }

        if (RectF.intersects(mAddObstacle.getObstacle(), mBall.getRect())) {
            mBall.batBounce(mAddObstacle.getObstacle());
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

        if (mObstacle.getObstacle().right > mScreenX) {
            mObstacle.reverseVelocity();
        }

        if (mObstacle.getObstacle().left < 0) {
            mObstacle.reverseVelocity();
        }

        if (mAddObstacle.getObstacle().right > mScreenX) {
            mAddObstacle.reverseVelocity();
        }

        if (mAddObstacle.getObstacle().left < 0) {
            mAddObstacle.reverseVelocity();
        }
    }

    /**
     * Pause method that is called within:
     * @see PongActivity#onPause()
     *
     * This method pauses or tries to pause the game loop if the user exits the app.
     * @exception InterruptedException in case error occurs with stopping thread or game loop.
     */
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

    /**
     * Just like the above method:
     * @see PongGame#pause()
     *
     * We are implementing functionality to start execution of our thread (game loop) when the game is resumed or started.
     * @see PongActivity#onResume()
     */
    public void resume() {
        mPlaying = true;

        mGameThread = new Thread(this);

        mGameThread.start();
    }

    /**
     * Overridden method within the view class that detects user interactions with our surface (current view).
     *
     * This method, based off the location and nature in which the user touches the screen, indicates whether the bat
     * needs to be updated left/right or not moved.
     * @see Bat#SetMovementState(int)
     * @param motionEvent Is the actual event of the user touching the screen. Contains information about the touch, such
     * as where it happened. We can filter the information in this variable through bitwise comparison to get the
     * information we want.
     * @return We return true to indicate that a touch occurred.
     */
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
