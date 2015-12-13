package com.graph;

/**
 * An instance of View defines a mapping from pixels to data coordinates
 *    and vice versa to be used when rendering a graph
 */

class View{
    private int h,w;
    private float yScale, yMin;
    private float xScale, xMin;
    View(int h, int w, float yScale, float yMin, float xScale, float xMin){
        this.h = h;
        this.w = w;
        this.yScale = yScale;
        this.yMin   = yMin;
        this.xScale = xScale;
        this.xMin   = xMin;
    }
    int height() { return h; }
    int width()  { return w; }
    float yRange() { return (float)h / yScale; }
    float xRange() { return (float)w / xScale; }
    float yPixToData(int y) { return ((float)y-yMin) / yScale; }
    float xPixToData(int x) { return ((float)x-xMin) / xScale; }
    int yDataToPix(float y) { return (int)(y*yScale + yMin);   }
    int xDataToPix(float x) { return (int)(x*xScale + xMin);   }
}
