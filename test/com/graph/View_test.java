package com.graph;

import com.graph.View;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * h = height of final view
 * w = width of final view
 * yScale  = height in y data of the view
 * yCenter = location in data to be centered vertically in the view
 * xScale  = width in x data of the view
 * xCenter = location in data to be centered horizonally in the view
 */
public class View_test {
    private float DELTA = 0.000001f;
    private int height = 1000;
    private int width  = 2000;
    private float yScale  = 10.0f;
    private float yCenter = 5.0f;
    private float xScale  = 1.0f;
    private float xCenter = 1.0f;
    private View sub = new View(height, width, yScale, yCenter, xScale, xCenter);

    @Test
    public void height() {
        assertEquals(height, sub.height());
    }

    @Test
    public void width() {
        assertEquals(width, sub.width());
    }

    @Test
    public void yRange() {
        assertEquals(yScale, sub.yRange(), DELTA);
    }

    @Test
    public void xRange() {
        assertEquals(xScale, sub.xRange(), DELTA);
    }

    @Test
    public void yPixToData() {
        assertEquals("minimum",yCenter-yScale/2, sub.yPixToData(0), DELTA);
        assertEquals("center" ,yCenter         , sub.yPixToData(height/2), DELTA);
        assertEquals("maximum",yCenter+yScale/2, sub.yPixToData(height), DELTA);
    }

    @Test
    public void xPixToData() {
        assertEquals("minimum", xCenter-xScale/2, sub.xPixToData(0), DELTA);
        assertEquals("center" , xCenter         , sub.xPixToData(width/2), DELTA);
        assertEquals("maximum", xCenter+xScale/2, sub.xPixToData(width), DELTA);
    }

    @Test
    public void yDataToPix() {
        assertEquals("pixel 0"       , 0       , sub.yDataToPix(yCenter-yScale/2));
        assertEquals("pixel height/2", height/2, sub.yDataToPix(yCenter));
        assertEquals("pixel height"  , height  , sub.yDataToPix(yCenter+yScale/2));
    }

    @Test
    public void xDatatoPix() {
        assertEquals("pixel 0"      , 0      , sub.xDataToPix(xCenter-xScale/2));
        assertEquals("pixel width/2", width/2, sub.xDataToPix(xCenter         ));
        assertEquals("pixel width"  , width  , sub.xDataToPix(xCenter+xScale/2));
    }
}
