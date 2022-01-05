package com.map;

import java.awt.geom.Point2D;

public interface CoordinateTransform {
    /**
     * Transforms a (lonitude,latitude) point to absolute (x,y) pixels
     * Will return an instance of the same class as the argument p
     */
    public Point2D toPixels(Point2D p);
    /**
     * Transforms absolute (x,y) pixels to (lonitude,latitude)
     * Will return an instance of the same class as the argument p
     */
    public Point2D toCoordinates(Point2D p);
    /**
     * Transforms absolute (lon,lat) to the pixel position in the current screen
     */
    public Point2D screenPosition(Point2D p);
    /**
     * Transforms pixel position relative current screen to absolute (lon,lat)
     */
    public Point2D mapPosition(Point2D p);
}
