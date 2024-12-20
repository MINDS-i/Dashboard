package com.layer;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface Layer {
    /**
     * Returns the Z index - or height - of this layer
     * Lower indecies are lower, "closer" to the map
     */
    int getZ();

    /**
     * Methods for responding to new mouse events in this layer
     * return true if the click is responded to
     * passed the point of the click in screen location and map location points
     */
    boolean onClick(MouseEvent e);

    boolean onPress(MouseEvent e);

    /**
     * If the "onPress" call is responded to, subsequent drag and release
     * events from the mouse will be forwarded to this map layer
     */
    void onDrag(MouseEvent e);

    void onRelease(MouseEvent e);

    /**
     * Paints a layer
     * g - the graphics object to paint with
     * t - the function transforming lat/lon coordinates to pixel positions
     */
    void paint(Graphics g);
}
