package com.gamecodeschool.pong;

import android.graphics.RectF;

/**
 * Additional class to "increase" difficulty for the user. As there are two moving Obstacles that float near the top
 * of the screen to randomly hit the ball back to the user (not to be considered AI at all). The lowest obstacle effectively
 * reduces the response time needed for the user to hit the ball back up towards the top of the screen.
 *
 * Strongly relates to the bat class. Did not inherit from the class however as there were some differences in how movement
 * is determined. An interface used across all game objects probably would have been best, but currently short on time.
 */

public class Obstacle {
    private RectF mRect;
    private float mXCoord;
    private static int count = 0;
    private float mYCoord;
    private float mOSpeed;
    private float mLength;
    private float mHeight;
    private float mScreenX;

    /** Parametrized constructor used below
     * Assigns obstacle width/height based off of screen resolution.
     * The static variable "count" helps the class count how many Obstacles have been instantiated and thus gives different
     * initial starting positions. (Do not want them to overlap)
     * Could have added another parametrized constructor with starting positions to handle the above.
     * As you can see with set conditions, program only expects, at most, two total obstacles.
     * @param sx screen x size. (pixels)
     * @param sy screen y size. (pixels)
     */
    public Obstacle(int sx, int sy) {

        mLength = sx / 8;
        mOSpeed = (sx / 3);
        mHeight = sy / 20;
        // If an obstacle already exists, set the y coordinate to be lower towards the screen so they do not overlap.
        mXCoord = 0;
        mYCoord = 0;
        mScreenX = sx;
        mRect = new RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + mHeight);
    }

     /** Accessor method used below. Used in drawing/collision detection.
     * @return of type RectF, returns information on location of object.
     */
    public RectF getObstacle() {
        return mRect;
    }

    /** Reverses velocity (x velocity) - as the obstacles will not move up/down
     * In the case one of the obstacles hits left/right boundary of screen */
    public void reverseVelocity() {
        mOSpeed = -mOSpeed;
    }

    /** Method that resets obstacles based off passed in coordinates - same concept could have followed with approach to
     * use another parametrized constructor, or alter the existing to pass in coordinates for the starting position of
     * each obstacle.
     * @param sx Starting x location of obstacle.
     * @param sy Starting y location of obstacle.
     */
    public void reset(float sx, float sy) {

        if (sx == 0) {
            mRect.right = sx + mLength;
            mRect.left = sx;
        }

        else {
            reverseVelocity();
            mRect.right = sx;
            mRect.left = sx - mLength;
        }

        mRect.top = sy;
        mRect.bottom = sy + mHeight;


    }

    /**
     * Updates the position of the obstacles indefinitely (they never stop).
     * @param fps - taking in FPS to designate amount of pixels the obstacle needs to be moved per animation.
     */
    public void update(long fps) {
        // Notice how top/bottom of rect is not updated as the obstacles never move up/down.
        if (mRect.left < 0) {
            mRect.left = 0;
        }

        else if (mRect.right > mScreenX) {
            mRect.right = mScreenX;
        }

        mRect.right += (mOSpeed / fps);
        mRect.left = mRect.right - mLength;

    }

}
