package com.graph;

/**
 * RTViewSpec implements a ViewSpec optimized for Real Time data
 *    - the data on screen is independent of height/width
 *    - The newest data (XSCALE_MAX) is always on screen
 *    - Vertical panning should be a simple pixel accurate shift
 *    - Horizonal panning should shift the leftmost X value in view
 *    - Zooming effects only the Y axis
 */

public class RTViewSpec implements ViewSpec{
    private final static float XSCALE_MIN = 0.05f;
    private final static float XSCALE_MAX = 1.00f;

    private float xScale;
    private float yScale;
    private float yCenter;

    public RTViewSpec(){
        this(40.0f, 0.0f, XSCALE_MAX);
    }
    public RTViewSpec(float yScale, float yCenter){
        this(yScale, yCenter, XSCALE_MAX);
    }
    public RTViewSpec(float yScale, float yCenter, float xScale){
        this.xScale  = xScale;
        this.yScale  = yScale;
        this.yCenter = yCenter;
    }

    public float maxY(){
        return yCenter/yScale + yScale/2.0f;
    }
    public float minY(){
        return yCenter/yScale - yScale/2.0f;
    }
    public void zoom(float fac){
        yScale *= fac;
    }
    public void panY(int pix){
        yCenter += (float) pix;
    }
    public void panX(int pix){
        float nScale = xScale + (float)pix*0.005f;
        xScale = Math.min(XSCALE_MAX, Math.max(XSCALE_MIN, nScale));
    }
    public View at(int height, int width){
        float scale = -height/yScale;
        float min = (height/2.0f)-yCenter;

        float xscale  = (width / xScale);
        float xminPix = width - xscale;
        View view = new View( height, width,
                              scale,  min,
                              xscale, xminPix );
        return view;
    }
}
