package org.openleap.jitter;

import com.leapmotion.leap.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nh_99
 */
public class Motions {
    private JitterSystem jitter;
    private BufferedJitterSystem jitterBuffer;
    private boolean gestureFinished = false;
    private static final Logger logger = LoggerFactory.getLogger(Motions.class);

    @Before
    public void before() {
        jitterBuffer = new BufferedJitterSystem();
        jitter = new JitterSystem(jitterBuffer);
    }

    @Test
    public void circleGesture() {
        jitter.enableGesture(Gesture.Type.TYPE_CIRCLE);

        while(!gestureFinished) {
            for (CircleGesture circleGesture : jitterBuffer.nextCircleBatch(2)) {
                if (jitter.isClockwise(circleGesture)) {
                    System.out.println("Processing a clockwise circle gesture");
                    gestureFinished = true;
                } else {
                    System.out.println("Processing a counter-clockwise circle gesture");
                    gestureFinished = true;
                }
            }
        }

        jitter.disableGesture(Gesture.Type.TYPE_CIRCLE);
    }

    @Test
    public void swipeGesture() {
        jitter.enableGesture(Gesture.Type.TYPE_SWIPE);
        gestureFinished = false;

        while(!gestureFinished) {
            for(SwipeGesture swipeGesture : jitterBuffer.getNextSwipeGestureFromBuffer()) {
                logger.debug("Processing a swipe gesture.");
                gestureFinished = true;
            }
        }

        jitter.disableGesture(Gesture.Type.TYPE_SWIPE);
    }

    @Test
    public void screenTap() {
        jitter.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        gestureFinished = false;

        while(!gestureFinished) {
            for(ScreenTapGesture screenTapGesture : jitterBuffer.getNextScreenTapGestureFromBuffer()) {
                logger.debug("Processing a screen tap gesture.");
                gestureFinished = true;
            }
        }

        jitter.disableGesture(Gesture.Type.TYPE_SCREEN_TAP);
    }

    @Test
    public void keyTap() {
        jitter.enableGesture(Gesture.Type.TYPE_KEY_TAP);
        gestureFinished = false;

        while(!gestureFinished) {
            for(KeyTapGesture keyTapGesture : jitterBuffer.getNextKeyTapGestureFromBuffer()) {
                logger.debug("Processing a key tap gesture.");
                gestureFinished = true;
            }
        }
    }

}
