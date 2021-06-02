package com.gamecodeschool.pong;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.Window;
import android.os.Bundle;
import android.graphics.Point;
import android.view.Display;

/**
 * This class serves as the main tool in how we interact and gain information from our user's Android device
 * (OS-Game interaction). This is made possible through inheritance used with Android's Activity class. More particularly,
 * this inheritance allows us to interact with the Android OS lifecycle and make needed adjustments to operate our game
 * properly.
 */
public class PongActivity extends Activity {
    /**
     * You can see below that this class is composed of a PongGame object which contains our userView and game mechanics.
     */
    private PongGame mPongGame;

    /**
     * Implementation of polymorphism below here as we override an onCreate method which is the first method called within
     * the Android OS lifecycle (when the user opens the app).
     * @param savedInstanceState contains information about our user's current device state. (Was there an ongoing game
     * that the user had and is now resuming?) This method firstly calls Activity's onCreate method and assigns our user
     * view to contain no title, so we have more space for game visuals. Additionally, we get display information and
     * store those values in a point variable which is then passed into our PongGame constructor.
     * @see PongGame#PongGame(Context, int, int)
     * Once our PongGame object has information regarding the current state of the user's OS and device, we then set
     * content view (user view) to our PongGame object.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();

        display.getSize(size);

        mPongGame = new PongGame(this, size.x, size.y);

        setContentView(mPongGame);
    }

    /**
     * Another implementation of polymorphism below as we are overriding this stage within our Android lifecycle to also
     * call our user-defined resume method that controls thread stop-start.
     * @see PongGame#resume()
     */

    @Override
    protected void onResume() {
        super.onResume();

        // Starting threads.
        mPongGame.resume();
    }

    /**
     * Same idea as above. We are overriding our onPause method to allow for additional functionality in relation to
     * starting or stopping or thread.
     * @see PongGame#pause()
     */

    @Override
    protected void onPause() {
        super.onPause();
        // Pausing threads.
        mPongGame.pause();
    }

}
