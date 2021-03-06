package com.layer;

import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Point;

public interface Layer {
    /**
     * Returns the Z index - or height - of this layer
     * Lower indecies are lower, "closer" to the map
     */
    public int getZ();
    /**
     * Methods for responding to new mouse events in this layer
     * return true if the click is responded to
     * passed the point of the click in screen location and map location points
     */
    public boolean onClick(MouseEvent e);
    public boolean onPress(MouseEvent e);
    /**
     * If the "onPress" call is responded to, subsequent drag and release
     * events from the mouse will be forwarded to this map layer
     */
    public void onDrag(MouseEvent e);
    public void onRelease(MouseEvent e);
    /**
     * Paints a layer
     * g - the graphics object to paint with
     * t - the function transforming lat/lon coordinates to pixel positions
     */
    public void paint(Graphics g);
}
