/*
 * Copyright (c) 2013 held jointly by the individual authors.
 *
 * Jitter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jitter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jitter.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openleap.jitter;

import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.ScreenTapGesture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Second layer to Jitter providing higher level functionality based on receiving processed input from JitterListener.
 * Buffers the input read at "Leap FPS" for easy consumption at a lower "Application FPS" without missing frames.
 * Application-specific implementations can simply request batched data through very exact method calls.
 * Some methods additionally support filtering the batched data further.
 *
 * General design notes:
 * Leap gestures come in three stages - started, updated, stopped. Discrete gestures only have the stopped state.
 * - Started: first frame making up a continuous gesture. Add it to the buffer since we know it is brand new.
 * - Updated: later frame in a continuous gesture. Replace any existing buffer entry with the latest frame.
 *      If a gesture can be "consumed" by use in an implementation then it gets removed and is ignored if seen again.
 * - Stopped: final/only gesture frame. Unless already consumed add/overwrite in buffer and remove from "consumed" list.
 *
 * Buffers are filled by calls coming from JitterListener and are consumed by calls to the batch return methods.
 * Those methods may be picky and not accept all buffered gestures and should remove "stopped" gestures from the buffer.
 *
 * To hide more technical Leap details this class could offer "user friendly" gesture enabling methods that include
 * details on the minimum sensitivity of gestures as well as whether said gestures are "consumed" when returned.
 *
 * Based on gesture_recognition.pde by Marcel Schwittlick for LeapMotionP5 - https://github.com/mrzl/LeapMotionP5
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class BufferedJitterSystem implements JitterListener {

    //Circle gesture buffers
    private ConcurrentSkipListMap<Integer, CircleGesture> circleGestureBuffer = new ConcurrentSkipListMap<Integer, CircleGesture>();
    private ConcurrentSkipListSet<Integer> consumedCircleGestureBuffer = new ConcurrentSkipListSet<Integer>();
    //Swipe gesture buffers
    private ConcurrentSkipListMap<Integer, SwipeGesture> swipeGestureBuffer = new ConcurrentSkipListMap<Integer, SwipeGesture>();
    private ConcurrentSkipListSet<Integer> consumedSwipeGestureBuffer = new ConcurrentSkipListSet<Integer>();
    //Screen tap gesture buffers
    private ConcurrentSkipListMap<Integer, ScreenTapGesture> screenTapGestureBuffer = new ConcurrentSkipListMap<Integer, ScreenTapGesture>();
    private ConcurrentSkipListSet<Integer> consumedScreenTapGestureBuffer = new ConcurrentSkipListSet<Integer>();
    //Key tap gesture buffers
    private ConcurrentSkipListMap<Integer, KeyTapGesture> keyTapGestureBuffer = new ConcurrentSkipListMap<Integer, KeyTapGesture>();
    private ConcurrentSkipListSet<Integer> consumedKeyTapGestureBuffer = new ConcurrentSkipListSet<Integer>();

    private static final Logger logger = LoggerFactory.getLogger(BufferedJitterSystem.class);

    @Override
    public void circleGestureRecognized(CircleGesture detectedGesture) {
        if (detectedGesture.state() == Gesture.State.STATE_STOP) {
            //This may have been already consumed, we are not sure.
            if (!consumedCircleGestureBuffer.contains(detectedGesture.id()))
                circleGestureBuffer.put(detectedGesture.id(), detectedGesture);

            logger.debug("//////////////////////////////////////");
            logger.debug("Gesture type: " + detectedGesture.type().toString());
            logger.debug("ID: " + detectedGesture.id());
            logger.debug("Radius: " + detectedGesture.radius());
            logger.debug("Normal: " + detectedGesture.normal());
            logger.debug("Clockwise: " + JitterSystem.isClockwise(detectedGesture));
            logger.debug("Turns: " + detectedGesture.progress());
            logger.debug("Center: " + detectedGesture.center());
            logger.debug("Duration: " + detectedGesture.durationSeconds() + "s");
            logger.debug("//////////////////////////////////////");
        } else if (detectedGesture.state() == Gesture.State.STATE_START) {
            circleGestureBuffer.put(detectedGesture.id(), detectedGesture);
        } else if (detectedGesture.state() == Gesture.State.STATE_UPDATE) {
            if (!consumedCircleGestureBuffer.contains(detectedGesture.id()))
                circleGestureBuffer.put(detectedGesture.id(), detectedGesture);
        }
    }

    @Override
    public void swipeGestureRecognized(SwipeGesture detectedGesture) {
        logger.info("Swipe gesture recognizeD.");
        if (detectedGesture.state() == Gesture.State.STATE_STOP) {
            if(!consumedSwipeGestureBuffer.contains(detectedGesture.id()))
                swipeGestureBuffer.put(detectedGesture.id(), detectedGesture);

            logger.debug("//////////////////////////////////////");
            logger.debug("Gesture type: " + detectedGesture.type());
            logger.debug("ID: " + detectedGesture.id());
            logger.debug("Position: " + detectedGesture.position());
            logger.debug("Direction: " + detectedGesture.direction());
            logger.debug("Duration: " + detectedGesture.durationSeconds() + "s");
            logger.debug("Speed: " + detectedGesture.speed());
            logger.debug("//////////////////////////////////////");
        } else if (detectedGesture.state() == Gesture.State.STATE_START) {
            logger.info("Swipe gesture recognized.");
            swipeGestureBuffer.put(detectedGesture.id(), detectedGesture);
        } else if (detectedGesture.state() == Gesture.State.STATE_UPDATE) {
            logger.info("Swipe gesture recognizEd.");
            if(!consumedSwipeGestureBuffer.contains(detectedGesture.id()))
                swipeGestureBuffer.put(detectedGesture.id(), detectedGesture);
        }
    }

    @Override
    public void screenTapGestureRecognized(ScreenTapGesture detectedGesture) {
        if (detectedGesture.state() == Gesture.State.STATE_STOP) {
            if(!consumedScreenTapGestureBuffer.contains(detectedGesture.id()))
                screenTapGestureBuffer.put(detectedGesture.id(), detectedGesture);

            logger.debug("//////////////////////////////////////");
            logger.debug("Gesture type: " + detectedGesture.type());
            logger.debug("ID: " + detectedGesture.id());
            logger.debug("Position: " + detectedGesture.position());
            logger.debug("Direction: " + detectedGesture.direction());
            logger.debug("Duration: " + detectedGesture.durationSeconds() + "s");
            logger.debug("//////////////////////////////////////");
        } else if (detectedGesture.state() == Gesture.State.STATE_START) {
            screenTapGestureBuffer.put(detectedGesture.id(), detectedGesture);
        } else if (detectedGesture.state() == Gesture.State.STATE_UPDATE) {
            if(!consumedScreenTapGestureBuffer.contains(detectedGesture.id()))
                screenTapGestureBuffer.put(detectedGesture.id(), detectedGesture);
        }
    }

    //TODO: Refactor to follow a similar approach as circle gestures
    @Override
    public void keyTapGestureRecognized(KeyTapGesture detectedGesture) {
        if (detectedGesture.state() == Gesture.State.STATE_STOP) {
            if(!consumedKeyTapGestureBuffer.contains(detectedGesture.id()))
                keyTapGestureBuffer.put(detectedGesture.id(), detectedGesture);

            logger.debug("//////////////////////////////////////");
            logger.debug("Gesture type: " + detectedGesture.type());
            logger.debug("ID: " + detectedGesture.id());
            logger.debug("Position: " + detectedGesture.position());
            logger.debug("Direction: " + detectedGesture.direction());
            logger.debug("Duration: " + detectedGesture.durationSeconds() + "s");
            logger.debug("//////////////////////////////////////");
        } else if (detectedGesture.state() == Gesture.State.STATE_START) {
            keyTapGestureBuffer.put(detectedGesture.id(), detectedGesture);
        } else if (detectedGesture.state() == Gesture.State.STATE_UPDATE) {
            if(!consumedKeyTapGestureBuffer.contains(detectedGesture.id()))
                keyTapGestureBuffer.put(detectedGesture.id(), detectedGesture);
        }
    }

    // CIRCLE NOTE: on enabling circle gestures should indicate whether they should be "consumed" on use
    // Usage of *more than one* variant of nextWhateverBatch at the same time may be bad and cause unexpected results

    //TODO: Make implementer set it instead. Enable gestures through here (through JitterSystem) including consumption?
    boolean consumptionEnabled = true;

    //TODO: Support filtering gestures by hand? But it would have to be a persistent hand ID or we'd lose buffers ...

    /**
     * Returns the next CircleGesture in buffer, if any.
     * Note that consuming buffered gestures does not respect different ways to use the gesture.
     * If it is returned for use just once it is considered spent and will not be returned again.
     * @return a CircleGesture or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch() {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        for (CircleGesture circleGesture : circleGestureBuffer.values()) {

            // Automatically add every circle to the return batch since we have no constraints to test here
            circleBatch.add(circleGesture);

            // Consume (if that's enabled)
            consumeCircle(circleGesture);

            // Remove stopped circles from the buffer as they won't be seen again.
            removeStoppedCircles(circleGesture);
        }

        return circleBatch;
    }

    /**
     * Returns the next CircleGesture in buffer that has made progress to at least the supplied parameter (inclusive)
     * @param progress a float describing the number of circles (fractional or not) the gesture has completed
     * @return a CircleGesture matching the request or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch(float progress) {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        // System.out.println("nextCircleBatch started with " + circleGestureBuffer.size() + " entries in the buffer");

        for (CircleGesture circleGesture : circleGestureBuffer.values()) {
            if (circleGesture.progress() >= progress) {
                logger.debug("Circle gesture has progressed sufficiently, allowing it to be processed");
                circleBatch.add(circleGesture);
                consumeCircle(circleGesture);
            }
            removeStoppedCircles(circleGesture);
        }
        return circleBatch;
    }

    /**
     * Returns the next CircleGesture in buffer that has made progress to at least the supplied parameters (inclusive)
     * @param progress a float describing the number of circles (fractional or not) the gesture has completed
     * @param radius a float for the minimum radius circles to consider (pass '0' progress to solely consider radius)
     * @return a CircleGesture matching the request or null if none are available
     */
    public Set<CircleGesture> nextCircleBatch(float progress, float radius) {
        Set<CircleGesture> circleBatch = new HashSet<CircleGesture>();

        for (CircleGesture circleGesture : circleGestureBuffer.values()) {

            // Test against constraints here and add only if the gesture passes muster
            if (circleGesture.progress() >= progress && circleGesture.radius() >= radius) {
                logger.debug("Circle gesture has progressed sufficiently, allowing it to be processed");
                circleBatch.add(circleGesture);

                // Consume (if that's enabled)
                consumeCircle(circleGesture);

            }// else {
                //System.out.println("Circle gesture hasn't progressed enough to be considered yet");
            //}

            // Remove stopped circles (won't be seen again). With constraints some circles may never have been used
            removeStoppedCircles(circleGesture);
        }

        return circleBatch;
    }

    /**
     * @return the next swipe gesture in the respective buffer.
     */
    public Set<SwipeGesture> getNextSwipeGestureFromBuffer() {
        Set<SwipeGesture> swipeBatch = new HashSet<SwipeGesture>();
        for (SwipeGesture swipeGesture : swipeGestureBuffer.values()) {
            swipeBatch.add(swipeGesture);
            consumeSwipe(swipeGesture);
            removeStoppedSwipes(swipeGesture);
        }
        return swipeBatch;
    }

    public Set<ScreenTapGesture> getNextScreenTapGestureFromBuffer() {
        Set<ScreenTapGesture> swipeBatch = new HashSet<ScreenTapGesture>();
        for (ScreenTapGesture screenTapGesture : screenTapGestureBuffer.values()) {
            swipeBatch.add(screenTapGesture);
            consumeScreenTap(screenTapGesture);
            removeStoppedScreenTaps(screenTapGesture);
        }
        return swipeBatch;
    }

    public Set<KeyTapGesture> getNextKeyTapGestureFromBuffer() {
        Set<KeyTapGesture> swipeBatch = new HashSet<KeyTapGesture>();
        for (KeyTapGesture keyTapGesture : keyTapGestureBuffer.values()) {
            swipeBatch.add(keyTapGesture);
            consumeKeyTap(keyTapGesture);
            removeStoppedKeyTaps(keyTapGesture);
        }
        return swipeBatch;
    }

    private void consumeCircle(CircleGesture circleGesture) {
        // If gestures of this type are considered consumed when returned for processing then flag & remove
        if (consumptionEnabled) {
            logger.debug("Consuming circle gesture with id: " + circleGesture.id());
            consumedCircleGestureBuffer.add(circleGesture.id());    // Mark circle as consumed so it won't get re-added
            circleGestureBuffer.remove(circleGesture.id());  // Remove it from the buffer so it won't be tested again
        }
    }

    private void removeStoppedCircles(CircleGesture circleGesture) {
        if (circleGesture.state() == Gesture.State.STATE_STOP) {
            circleGestureBuffer.remove(circleGesture.id());
            if (consumptionEnabled) {
                consumedCircleGestureBuffer.remove(circleGesture.id());
                logger.debug("Just removed gesture with id " + circleGesture.id() + " from the 'consumed' list");
            }
        }
    }

    private void consumeSwipe(SwipeGesture swipeGesture) {
        if(consumptionEnabled) {
            logger.debug("Consuming swipe gesture with id {}", swipeGesture.id());
            consumedSwipeGestureBuffer.add(swipeGesture.id());
            swipeGestureBuffer.remove(swipeGesture.id());
        }
    }

    private void removeStoppedSwipes(SwipeGesture swipeGesture) {
        if(swipeGesture.state() == Gesture.State.STATE_STOP) {
            swipeGestureBuffer.remove(swipeGesture.id());
            if(consumptionEnabled) {
                consumedSwipeGestureBuffer.remove(swipeGesture.id());
                logger.debug("Removed the swipe gesture from the consumed buffer with the id {}", swipeGesture.id());
            }
        }
    }

    private void consumeScreenTap(ScreenTapGesture screenTapGesture) {
        if(consumptionEnabled) {
            logger.debug("Consuming swipe gesture with id {}", screenTapGesture.id());
            consumedScreenTapGestureBuffer.add(screenTapGesture.id());
            screenTapGestureBuffer.remove(screenTapGesture.id());
        }
    }

    private void removeStoppedScreenTaps(ScreenTapGesture screenTapGesture) {
        if(screenTapGesture.state() == Gesture.State.STATE_STOP) {
            swipeGestureBuffer.remove(screenTapGesture.id());
            if(consumptionEnabled) {
                consumedScreenTapGestureBuffer.remove(screenTapGesture.id());
                logger.debug("Removed the swipe gesture from the consumed buffer with the id {}", screenTapGesture.id());
            }
        }
    }

    private void consumeKeyTap(KeyTapGesture keyTapGesture) {
        if(consumptionEnabled) {
            logger.debug("Consuming swipe gesture with id {}", keyTapGesture.id());
            consumedKeyTapGestureBuffer.add(keyTapGesture.id());
            keyTapGestureBuffer.remove(keyTapGesture.id());
        }
    }

    private void removeStoppedKeyTaps(KeyTapGesture keyTapGesture) {
        if(keyTapGesture.state() == Gesture.State.STATE_STOP) {
            keyTapGestureBuffer.remove(keyTapGesture.id());
            if(consumptionEnabled) {
                consumedKeyTapGestureBuffer.remove(keyTapGesture.id());
                logger.debug("Removed the swipe gesture from the consumed buffer with the id {}", keyTapGesture.id());
            }
        }
    }
}
