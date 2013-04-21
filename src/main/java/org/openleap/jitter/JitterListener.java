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

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;

/**
 * "External" listener for Leap Motion Controller input provided by the Leap software.
 * Used by Jitter to forward gesture calls from to an internal listener to an implementer.
 *
 * Based on gesture_recognition.pde by Marcel Schwittlick for LeapMotionP5 - https://github.com/mrzl/LeapMotionP5
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public interface JitterListener {

    /**
     * Called when the Leap Listener detects a circle gesture.
     * @param gesture the CircleGesture detected
     */
    void circleGestureRecognized(CircleGesture gesture);

    /**
     * Called when the Leap Listener detects a swipe gesture.
     * @param gesture the SwipeGesture detected
     */
    void swipeGestureRecognized(SwipeGesture gesture);

    /**
     * Called when the Leap Listener detects a screen tap gesture (a finger tap directly toward the screen).
     * @param gesture the ScreenTapGesture detected
     */
    void screenTapGestureRecognized(ScreenTapGesture gesture);

    /**
     * Called when the Leap Listener detects a key tap gesture (a finger tap downwards as if hitting a key).
     * @param gesture the KeyTapGesture detected
     */
    void keyTapGestureRecognized(KeyTapGesture gesture);
}
