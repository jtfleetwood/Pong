package com.gamecodeschool.pong;

import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * This class controls movement and the instantiation/creation of the Bat object used in our:
 * @see PongGame class -> example of composition - lifetime of bat controlled by PongGame.
 */

public class Bat {

    private RectF mRect;
    private float mLength;
    private float mXCoord;
    private float mBatSpeed;
    private int mScreenX;

    final int STOPPED = 0;
    final int LEFT = 1;
    final int RIGHT = 2;

    private int mBatMoving = STOPPED;

    /**
     * Parametrized constructor below.
     * @param sx Used to assign bat length based off width of screen.
     * @param sy Used to assign bat height based off height of screen.
     */
    public Bat(int sx, int sy) {
        mScreenX = sx;

        mLength = mScreenX / 8;

        float height = sy / 40;

        mXCoord = mScreenX / 2;

        float mYCoord = sy - height;

        mRect = new RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + height);

        mBatSpeed = mScreenX;

    }

    /**
     * Accessor used to return our mRect instance variable which is the bat itself, used in drawing/collision detection.
     * @return returns the rect (x, y coordinates)
     */
    public RectF getRect() {
        return mRect;
    }

    /**
     * Method that sets movement state based off touch event realized in:
     * @see PongGame#onTouchEvent(MotionEvent)
     * @param state is passed in as an integer that corresponds with our instance variables that communicates state of the bat.
     * @see Bat#mBatMoving stores the current state of the bat and is used in:
     * @see Bat#update(long)
     * @see Bat#LEFT
     * @see Bat#RIGHT
     * @see Bat#STOPPED
     */
    public void SetMovementState(int state) {
        mBatMoving = state;
    }

    /**
     * Below method controls moving operations for the bat by changing the x coordinate locations of it.
     * Coordinates are changed based off the mBatMoving instance variable that is set through:
     * @see Bat#SetMovementState(int) 
     * @see PongGame#onTouchEvent(MotionEvent)
     * @param fps Important in designating how many pixels need to be moved per animation based off bat speed and fps.
     */
    public void update(long fps) {
        if (mBatMoving == LEFT) {
            mXCoord = mXCoord - (mBatSpeed / fps);
        }

        if (mBatMoving == RIGHT) {
            mXCoord = mXCoord + (mBatSpeed / fps);
        }

        if (mXCoord < 0) {
            mXCoord = 0;
        }

        else if (mXCoord + mLength > mScreenX) {
            mXCoord = mScreenX - mLength;
        }

        mRect.left = mXCoord;
        mRect.right = mXCoord + mLength;
    }
}
