package com.map;

import java.awt.*;

/**
 * Pre-defined enums used to communicate details about a
 * waypoints behavior and appearance.
 */
public enum WaypointType {

    STANDARD(0, "Standard", Color.red),
    LEFT_APPROACH(1, "Left Approach", Color.blue),
    RIGHT_APPROACH(2, "Right Approach", Color.orange);

    private final int index;
    private final String text;
    private final Color pointColor;

    WaypointType(int index, String text, Color pointColor) {
        this.index = index;
        this.text = text;
        this.pointColor = pointColor;
    }
}
