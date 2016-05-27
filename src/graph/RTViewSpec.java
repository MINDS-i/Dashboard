package com.graph;

/**
 * RTViewSpec implements a ViewSpec optimized for Real Time data
 *    - the data on screen is independent of height/width
 *    - The newest data (x = 1.0) is always on screen
 *    - Vertical panning should be a simple pixel accurate shift
 *    - Horizonal panning should shift the leftmost X value in view
 *    - Zooming effects only the Y axis
 */

public class RTViewSpec implements ViewSpec {
    private final static float XCENTER_MIN = 0.50f;
    private final static float XCENTER_MAX = 0.95f;
    private final static float ZOOM_MIN = Float.MIN_NORMAL;
    private final static float ZOOM_MAX = Float.MAX_VALUE;

    private float xCenter;//data
    private float yScale; //data
    private float yCenter;//pixels

    public RTViewSpec() {
        this(40.0f, 0.0f, 1.0f);
    }
    public RTViewSpec(float yScale, float yCenter) {
        this(yScale, yCenter, 1.0f);
    }
    public RTViewSpec(float yScale, float yCenter, float xScale) {
        this.xCenter = Math.min(XCENTER_MAX, Math.max(XCENTER_MIN, xScale/2.0f));
        this.yScale  = yScale;
        this.yCenter = yCenter;
    }
    public float maxY() {
        return yCenter + yScale/2.0f;
    }
    public float minY() {
        return yCenter - yScale/2.0f;
    }
    public void zoom(float fac) {
        yScale = Math.min(ZOOM_MAX,Math.max(ZOOM_MIN,yScale*fac));
    }
    public void panY(int pix, int height) {
        yCenter -= (pix/(float)height)*yScale;
    }
    public void panX(int pix, int width) {
        float nScale = xCenter - pix/(2.0f*(float)width);
        xCenter = Math.min(XCENTER_MAX, Math.max(XCENTER_MIN, nScale));
    }
    public View at(int height, int width) {
        float xDist  = (1.0f-xCenter)*2.0f;

        View view = new View( height, width,
                              yScale, yCenter,
                              xDist , xCenter);
        return view;
    }
}
