package com.gamecodeschool.pong;

import android.graphics.RectF;



public class Obstacle {
    private RectF mRect;
    private float mXCoord;
    private static int count;
    private float mYCoord;
    private float mOSpeed;
    private float mLength;

    public Obstacle(int sx, int sy) {

        float height = sy / 20;
        mLength = sx / 8;
        mOSpeed = (sx / 3);

        if (count >= 1) {
            mXCoord = sx - 225;
            mYCoord = (float) (sy / 2.5);
        }

        else {
            mXCoord = 0;
            mYCoord = 50;
        }

        mRect = new RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + height);
        count += 1;
    }

    public RectF getObstacle() {
        return mRect;
    }

    public void reverseVelocity() {
        mOSpeed = -mOSpeed;
    }

    public void reset(float sx, float sy) {

        mXCoord = sx;
        mYCoord = sy;
    }


    public void update(long fps) {
        mXCoord += (mOSpeed / fps);
        mRect.right = mXCoord + mLength;
        mRect.left = mXCoord;

    }

}
