package com.map;

import java.awt.*;
import java.awt.geom.Point2D;

interface MapSource {
    /**
     * Instructs the map source to clear any cache it map have, to free up
     * any non necessary memory
     */
    void clear();

    /**
     * Point a specified view to a minimum height and width
     * may draw outside the minumum height and width
     * Will draw with the gps coordinate defined by "center" at 0,0
     * "scale" defines the number of pixels wide/tall the earth is
     */
    void paint(Graphics2D g, Point2D center, int scale, int width, int height);

    /**
     * Add a component c to the list of components that should repaint
     * when this mapSource changes its contents
     */
    void addRepaintListener(Component c);

    /**
     * Remove a component c from the list of components that should repaint
     * when this mapSource changes its content
     */
    void removeRepaintListener(Component c);

    /**
     * Returns if a given zoom level (expressed in pixels/earth radius)
     * can be displayed by this MapSource
     */
    boolean isValidZoom(int zoomLevel);
}
