package com.gamecodeschool.pong;

import android.graphics.RectF;

/**
 * The Ball class is used within our PongGame class. Technically, it's existence would be composition since the PongGame
 * class controls the lifetime of the Ball object. This object is the ball that floats around the screen and determines
 * player score. (If ball hits bottom of screen -> lose a life, if ball hits bat -> player gains a point)
 */
public class Ball {
    private RectF mRect;
    private float mXVelocity;
    private float mYVelocity;
    private float mBallWidth;
    private float mBallHeight;

    /**
     * Parametrized constructor below.
     * @param screenX Number of horizontal pixels on user's screen. (Used to assign ball width/height)
     * The above is important so the ball will be the same size on every player's screen relative to their resolution/screen size.
     */
    public Ball(int screenX) {
        mBallWidth = screenX / 100;
        mBallHeight = screenX / 100;

        mRect = new RectF();
    }

    /** Accessor method below.
     * @see Bat#getRect()
     * @return Returns the ball (x, y coordinates).
     */
    public RectF getRect() {
        return mRect;
    }

    /** Method used to change y direction of the ball movement */
    void reverseYVelocity(){
        mYVelocity = -mYVelocity;
    }

    /** Method used to change x direction of the ball movement */
    void reverseXVelocity(){
        mXVelocity = -mXVelocity;
    }

    /** Method below used to update the position of the ball based off x/y velocity */
    void update(long fps) {
        mRect.left = mRect.left + (mXVelocity / fps);
        mRect.top = mRect.top + (mYVelocity / fps);
        mRect.right = mRect.left + mBallWidth;
        mRect.bottom = mRect.top + mBallHeight;

    }

    /** Resets the position of the ball upon a new game starting, starting position all relative to screen resolution */
    void reset(int x, int y) {
        mRect.left = x / 2;
        mRect.top = 0;
        mRect.right = (x / 2) + mBallWidth;
        mRect.bottom = mBallHeight;

        mYVelocity = -(y / 3);
        mXVelocity = (x / 2);
    }

    /** Increases the ball velocity whenever it collides with the user's bat (point gained -> increased difficulty)
     *
     */
    void increaseVelocity(){
        mXVelocity = mXVelocity * 1.1f;

        mYVelocity = mYVelocity * 1.1f;

    }

    /** Method that designates the x/y velocity whenever a bat/ball collision occurs
     * if the ball collides with the left side of the bat -> bounce left and up, etc. */
    void batBounce(RectF batPosition) {
        float batCenter = batPosition.left + (batPosition.width() / 2);
        float ballCenter = mRect.left + (mBallWidth / 2);

        float relativeIntersect = (batCenter - ballCenter);

        if (relativeIntersect < 0) {
            mXVelocity = Math.abs(mXVelocity);
        }

        else {
            mXVelocity = -Math.abs(mXVelocity);
        }

        reverseYVelocity();
    }

}
