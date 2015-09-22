package com.map;

import java.awt.geom.Point2D;
import java.awt.Graphics2D;

interface MapSource {
    /**
     * Instructs the map source to clear any cache it map have, to free up
     * any non necessary memory
     */
    void clear();
    /**
     * Point a specified view to a minimum height and width
     *     may draw outside the minumum height and width
     *     Will draw with the gps coordinate defined by "center" at 0,0
     *     "scale" defines the number of pixels wide/tall the earth is
     *     will try to approximate scale as best as possible
     */
    void paint(Graphics2D g, Point2D center, int scale, int width, int height);
}
