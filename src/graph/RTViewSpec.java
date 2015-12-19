package com.graph;

/**
 * RTViewSpec implements a ViewSpec optimized for Real Time data
 *    - the data on screen is independent of height/width
 *    - The newest data (XCENTER_MAX) is always on screen
 *    - Vertical panning should be a simple pixel accurate shift
 *    - Horizonal panning should shift the leftmost X value in view
 *    - Zooming effects only the Y axis
 */

public class RTViewSpec implements ViewSpec{
    private final static float XCENTER_MIN = 0.50f;
    private final static float XCENTER_MAX = 0.95f;

    private float xCenter;//data
    private float yScale; //data
    private float yCenter;//pixels

    public RTViewSpec(){
        this(40.0f, 0.0f, XCENTER_MIN);
    }
    public RTViewSpec(float yScale, float yCenter){
        this(yScale, yCenter, XCENTER_MIN);
    }
    public RTViewSpec(float yScale, float yCenter, float xCenter){
        this.xCenter  = xCenter;
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
        yCenter -= (float) pix;
    }
    public void panX(int pix){
        float nScale = xCenter - (float)pix*0.005f;
        xCenter = Math.min(XCENTER_MAX, Math.max(XCENTER_MIN, nScale));
    }
    public View at(int height, int width){

        float yDataCenter = yCenter*(yScale/(float)height);
        float xDist  = (1.0f-xCenter)*2.0f;

        View view = new View( height, width,
                              yScale, yDataCenter,
                              xDist , xCenter);
        return view;
    }
}
