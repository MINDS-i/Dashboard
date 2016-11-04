package com.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;

//for testing purposes
import java.io.File;

public class ArtificialHorizon extends JPanel {
    /**
     * Ratio of display size to the distance between the inner bar of an
     *   axis indicator and the edge of the display
     */
    private static final float WDIST  = 3.0f/20.0f;
    /**
     * Ratio of display size to the length of a full tick mark on an axis
     *   indicator
     */
    private static final float TICKH  = 1.0f/20.0f;
    /**
     * Ratio of display size to the width of the indicator axis lines
     */
    private static final float STROKE = 1.0f/300.0f;
    /**
     * Magnitude in abstract units between marks on the indicator axis
     */
    private static final float TICK_SCALE = 10f;
    /**
     * Number of ticks to display on each indicator axis
     */
    private static final int TICK_COUNT = 20;
    /**
     * Ratio of normal small indicator ticks to large labeled ticks
     */
    private static final int MAJOR_TICK_RATE = 4;
    private static final Color groundColor = new Color(0x7D5233);
    private static final Color airColor = new Color(0x5B93C5);
    private static final Color wingColor = new Color(0xEA8300);
    private static final Color barColor = new Color(0xBBBBBB);
    enum DataAxis {
        TOP(new IndicatorBarSpecs(
            WDIST, WDIST, STROKE, 1f-2f*WDIST, Axis.X_AXIS, -TICKH,
            DrawPoint.BottomCenter
        )),
        LEFT(new IndicatorBarSpecs(
            WDIST, WDIST, STROKE, 1f-2f*WDIST, Axis.Y_AXIS, -TICKH,
            DrawPoint.RightCenter
        )),
        RIGHT(new IndicatorBarSpecs(
            WDIST, 1f-WDIST, STROKE, 1f-2f*WDIST, Axis.Y_AXIS, TICKH,
            DrawPoint.LeftCenter
        )),
        BOTTOM(new IndicatorBarSpecs(
            1f-WDIST, WDIST, STROKE, 1f-2f*WDIST, Axis.X_AXIS, TICKH,
            DrawPoint.TopCenter
        ));
        final IndicatorBarSpecs ibs;
        DataAxis(IndicatorBarSpecs ibs){ this.ibs = ibs; }
    }
    private Map<DataAxis, Float> values = new EnumMap<DataAxis, Float>(DataAxis.class);
    private Map<DataAxis, Boolean> enabled = new EnumMap<DataAxis, Boolean>(DataAxis.class);
    {
        for(DataAxis a : DataAxis.values()){
            enabled.put(a, false);
            values.put(a, 0.0f);
        }
    }
    private float pitch, roll;
    /**
     * Construct an artificial Horizon instance
     */
    public ArtificialHorizon(){
    }
    /**
     * Set the main display angles for the artificial horizon and redraw
     */
    public void setAngles(float pitch, float roll){
        synchronized(this){
            this.pitch = pitch;
            this.roll = roll;
            this.repaint();
            Toolkit.getDefaultToolkit().sync();
        }
    }
    /**
     * Set the value centered on a particular data indicator axis
     */
    public void set(DataAxis dv, float value){
        values.put(dv, value);
    }
    /**
     * Enable the drawing of a particular data indicator axis
     */
    public void setEnabled(DataAxis dv, boolean value){
        enabled.put(dv, value);
    }
    /**
     * Paint this Artificial Horizon in a Graphics context
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int actualSize = Math.min(getHeight(), getWidth());
        Graphics2D g2d = (Graphics2D)g.create();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        /*
        int artificialSize = 2048;
        double scale = (double)actualSize / (double)artificialSize;
        g2d.scale(scale,scale);
        render(g2d, artificialSize);
        */
        render(g2d, actualSize);
        g2d.dispose();

        g.setColor(Color.BLACK);
        int box = Math.max(getHeight(), getWidth());
        g.fillRect(0, actualSize, box, box);
        g.fillRect(actualSize, 0, box, box);
    }

    private void render(Graphics2D g, int size){
        g.setFont( setFontHeight(g.getFont(), size/26) );

        Graphics2D background = (Graphics2D)g.create();
        background.translate(size/2, size/2);
        background.rotate(roll);
        background.translate(-size, -size);
        renderBackground(background, size*2);
        background.dispose();

        paintPlaneIndicator(g, size);
        paintIndicators(g, size);
    }

    private interface Marker{
        void mark(float percent, float width, String label);
    }

    /**
     * Call to generate a series of marks along an axis via the Marker interface
     *   `center`        - the value halfway up the axis
     *   `scale`         - units between each mark
     *   `tickCount`     - the number of ticks on the axis
     *   `majorRate`     - the number of total ticks per 1 major tick mark
     *   `output`        - the marker object responsible for output
     */
    private void markAxis(
      float center,
      float scale,
      int tickCount,
      int majorRate,
      Marker output){
        float firstLowerMarker = ((float)Math.floor(center/scale))*scale;
        float normalizedOffset = (center - firstLowerMarker)/(scale*(float)tickCount);
        float tickHeight = 1.0f/(float)tickCount;

        for(int i=1; i<tickCount+1; i++){
            float pos = tickHeight*i - normalizedOffset;
            float value = (i - tickCount/2)*scale + firstLowerMarker;

            if( (int)(value / scale) % majorRate == 0 )
                output.mark(pos, 0.9f, ""+value);
            else
                output.mark(pos, 0.5f, "");
        }
    }

    private enum Axis{ X_AXIS, Y_AXIS };
    private static class IndicatorBarSpecs{
        float upper;
        float left;
        float stroke;
        float length;
        Axis markAxis;
        float markHeight;
        DrawPoint textPos;
        IndicatorBarSpecs(float upper, float left, float stroke, float length,
                          Axis markAxis, float markHeight, DrawPoint textPos){
            this.upper = upper;
            this.left = left;
            this.stroke = stroke;
            this.length = length;
            this.markAxis = markAxis;
            this.markHeight = markHeight;
            this.textPos = textPos;
        }
    }

    private void paintIndicators(Graphics2D g, int size){
        for(DataAxis a : DataAxis.values()){
            if(enabled.get(a) == true)
                paintIndicator(a.ibs, values.get(a), g, size);
        }
    }

    private int scaleTo(float scale, int size){
        return (int) Math.round(scale*(float)size);
    }

    private void fillRect(Graphics2D g, int x, int y, int width, int height){
        if(width  < 0) x += width;
        if(height < 0) y += height;
        g.fillRect(x, y, Math.abs(width), Math.abs(height));
    }

    private void paintIndicator(IndicatorBarSpecs ibs, float value, Graphics2D g, int size){
        float widthScalar = (ibs.markAxis == Axis.X_AXIS)?
            ibs.length : 0f;
        float heightScalar = (ibs.markAxis == Axis.Y_AXIS)?
            ibs.length : 0f;

        // Draw the straight bar
        g.setColor(Color.GREEN);
        g.fillRect(
            scaleTo(ibs.left, size),
            scaleTo(ibs.upper, size),
            scaleTo(widthScalar+ibs.stroke, size),
            scaleTo(heightScalar+ibs.stroke, size)
            );

        // Draw the axis tick marks
        markAxis(value, TICK_SCALE, TICK_COUNT, MAJOR_TICK_RATE,
          (float percent, float magn, String label) -> {
            float displacement = percent*ibs.length;
            float x = ibs.left + ((ibs.markAxis == Axis.X_AXIS)? displacement : 0.0f);
            float y = ibs.upper+ ((ibs.markAxis == Axis.Y_AXIS)? ibs.length-displacement : 0.0f);
            float width = (ibs.markAxis == Axis.Y_AXIS)? magn*ibs.markHeight : ibs.stroke;
            float height= (ibs.markAxis == Axis.X_AXIS)? magn*ibs.markHeight : ibs.stroke;
            fillRect(g,
                scaleTo(x, size),
                scaleTo(y, size),
                scaleTo(width, size),
                scaleTo(height, size)
                );

            float textX = (ibs.markAxis == Axis.X_AXIS)? x : x + ibs.markHeight;
            float textY = (ibs.markAxis == Axis.Y_AXIS)? y : y + ibs.markHeight;
            drawString(ibs.textPos, g, label, scaleTo(textX, size), scaleTo(textY, size));
        });

        // Make the Center Indicator triangle
        float centerX = ibs.left+widthScalar/2.0f;
        float centerY = ibs.upper+heightScalar/2.0f;
        float magnitude = ibs.markHeight/4.0f;
        float flip = (ibs.markAxis == Axis.X_AXIS)? 1.0f : -1.0f;
        g.setColor(Color.YELLOW);
        g.fillPolygon(
            new int[]{
                scaleTo(centerX, size),
                scaleTo(centerX - magnitude, size),
                scaleTo(centerX + flip*magnitude, size),
                },
            new int[]{
                scaleTo(centerY, size),
                scaleTo(centerY - magnitude, size),
                scaleTo(centerY - flip*magnitude, size),
                },
            3);
    }

    private enum DrawPoint{ LeftCenter, RightCenter, TopCenter, BottomCenter }
    private void drawString(DrawPoint dp, Graphics2D g, String s, int x, int y){
        Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
        switch(dp){
            case BottomCenter:
                g.drawString(s, x - (int)r.getCenterX(), y);
                break;
            case TopCenter:
                g.drawString(s, x - (int)r.getCenterX(), y + (int)r.getHeight());
                break;
            case LeftCenter:
                g.drawString(s, x, y - (int)r.getCenterY());
                break;
            case RightCenter:
                g.drawString(s, x - (int)r.getWidth(), y - (int)r.getCenterY());
                break;
        }
    }

    private Font setFontHeight(Font f, int newHeight){
        FontMetrics m = this.getFontMetrics(f);
        double scale = (double)newHeight / (double)m.getHeight();
        AffineTransform transform = new AffineTransform();
        transform.setToScale(scale, scale);
        return f.deriveFont(transform);
    }

    private void paintPlaneIndicator(Graphics2D g, int size){
        int height = Math.max(size/40,1);
        int width = size*6/40;
        int seperation = width;

        g.setColor(wingColor);
        g.fillRoundRect(size/2 - width/2 - seperation, size/2 - height/2,
                        width, height,
                        height, height);

        g.fillOval(size/2 - height/2, size/2 - height/2, height, height);

        g.fillRoundRect(size/2 - width/2 + seperation, size/2 - height/2,
                        width, height,
                        height, height);
    }

    private int angleOffset(double theta, int size){
        return (int)((theta/2.0)*(double)size)+size/2;
    }

    private void renderBackground(Graphics2D g, int size){
        synchronized(this){
            int horizonPosition = angleOffset(pitch, size);

            g.setColor(airColor);
            g.fillRect(0, 0, size, horizonPosition);

            g.setColor(groundColor);
            g.fillRect(0, horizonPosition, size, size-horizonPosition);

            g.setColor(Color.BLACK);
            g.fillRect(0, horizonPosition-1, size, 2);

            renderAngleMarkings(g, size);
        }
    }

    private void renderAngleMarkings(Graphics2D g, int size){
        g.setColor(barColor);
        int lineHeight = Math.max(size/200, 1);
        int shortWidth = size/15;

        for(int i=-60; i<=60; i+=5){
            if(i == 0) continue;

            int effWidth = shortWidth;
            int position = angleOffset(((double)i)*Math.PI/180.0+pitch, size);

            if(Math.abs(i) % 10 == 0){
                effWidth = shortWidth*Math.min(Math.abs(i)/10 + 1, 3);
            }

            g.fillRoundRect(
                size/2-effWidth/2, position-lineHeight/2,
                effWidth, lineHeight,
                lineHeight, lineHeight);
        }
    }

    public static void makePopup(){
        ArtificialHorizon ah = new ArtificialHorizon();

        JFrame f = new JFrame("ArtificialHorizon");
        f.add(ah);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ah.setEnabled(DataAxis.TOP, true);
        ah.setEnabled(DataAxis.RIGHT, true);
        ah.setEnabled(DataAxis.BOTTOM, true);
        ah.setEnabled(DataAxis.LEFT, true);
        float wave = 0.0f;
        while(f.isShowing()) {
            try {
                wave += 0.005f;
                ah.set(DataAxis.TOP, wave*20f);
                ah.set(DataAxis.RIGHT, wave*5f+7.378f);
                ah.setAngles(
                    (float)Math.cos(wave)/3.0f,
                    (float)Math.sin(wave*(3.0f/5.0f))/10f);
                Thread.sleep(16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //main method for quicker manual testing
    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "True");
        makePopup();
    }
}
