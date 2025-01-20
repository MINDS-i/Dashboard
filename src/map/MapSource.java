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

    /**
     * Pre-loads every map tile within a certain distance from a point into the disk cache.
     * Loading may take a long time and should run in a separate thread, so a callback can
     * be provided to receive updates.
     * @param center The center point of tiles to load.
     * @param distanceKm The distance from the center within which tiles should be loaded.
     * @param callback A callback that will be called as tiles are loaded.
     */
    void preloadTiles(Point2D center, double distanceKm, TileLoadingCallback callback);

    /**
     * Interrupts a preload operation that is currently in progress.
     */
    void stopPreloading();
}
