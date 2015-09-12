package com.map;

public interface MapLayer {
    /**
     * Returns the Z index - or height - of this layer
     * Lower indecies are lower, "closer" to the map
     */
    public int getZ();
    /**
     * Responds to clicks within this layer
     * return true if the click is responded to
     * passed the point of the click in screen location and map location points
     */
    public boolean onClick(Point pixel, Point map);
    /**
     * Paints a layer
     * g - the graphics object to paint with
     * t - the function transforming lat/lon coordinates to pixel positions
     */
    public void paint(Graphics g, AffineTransform t);
}
