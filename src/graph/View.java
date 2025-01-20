package com.graph;

/**
 * An instance of View defines a mapping from pixels to data coordinates
 * and vice versa to be used when rendering a graph
 */

class View {
    private final int h;
    private final int w;
    private final float yScale;
    private final float yMin;
    private final float xScale;
    private final float xMin;

    /**
     * h = height of final view
     * w = width of final view
     * yScale  = height in y data of the view
     * yCenter = location in data to be centered vertically in the view
     * xScale  = width in x data of the view
     * xCenter = location in data to be centered horizonally in the view
     */
    View(int h, int w, float yScale, float yCenter, float xScale, float xCenter) {
        this.h = h;
        this.w = w;
        this.yScale = -h / yScale;
        this.yMin = yCenter + yScale / 2;
        this.xScale = w / xScale;
        this.xMin = xCenter - xScale / 2;
    }

    int height() {
        return h;
    }

    int width() {
        return w;
    }

    float yRange() {
        return -h / yScale;
    }

    float xRange() {
        return w / xScale;
    }

    float yPixToData(int y) {
        return (y / yScale) + yMin;
    }

    float xPixToData(int x) {
        return (x / xScale) + xMin;
    }

    int yDataToPix(float y) {
        return (int) ((y - yMin) * yScale);
    }

    int xDataToPix(float x) {
        return (int) ((x - xMin) * xScale);
    }
}
