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

//for testing purposes
import java.io.File;

public class ArtificialHorizon extends JPanel {
    private double pitch, roll, altitude;
    private Color groundColor = new Color(0x7D5233);
    private Color airColor = new Color(0x5B93C5);
    private Color wingColor = new Color(0xEA8300);
    private Color barColor = new Color(0xBBBBBB);

    public ArtificialHorizon(){
    }

    public void set(double pitch, double roll, double altitude){
        synchronized(this){
            this.pitch = pitch;
            this.roll = roll;
            this.altitude = altitude;
            this.repaint();
            Toolkit.getDefaultToolkit().sync();
        }
    }

    /**
     * The artificial horizon is rendered in an artificial 512,512 square
     *   which is scaled to fit the current actual window size
     */

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int actualSize = Math.min(getHeight(), getWidth());
        Graphics2D g2d = (Graphics2D)g.create();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        /*int artificialSize = 2048;
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
        final float wDist   = 3.0f/20.0f;
        final float mHeight = 1.0f/20.0f;
        final float stroke  = 1.0f/300.0f;
        IndicatorBarSpecs top = new IndicatorBarSpecs(
            wDist, wDist, stroke, 1f-2f*wDist, Axis.X_AXIS, -mHeight,
            DrawPoint.BottomCenter
        );
        IndicatorBarSpecs right = new IndicatorBarSpecs(
            wDist, 1f-wDist, stroke, 1f-2f*wDist, Axis.Y_AXIS, mHeight,
            DrawPoint.LeftCenter
        );
        IndicatorBarSpecs bottom = new IndicatorBarSpecs(
            1f-wDist, wDist, stroke, 1f-2f*wDist, Axis.X_AXIS, mHeight,
            DrawPoint.TopCenter
        );
        IndicatorBarSpecs left = new IndicatorBarSpecs(
            wDist, wDist, stroke, 1f-2f*wDist, Axis.Y_AXIS, -mHeight,
            DrawPoint.RightCenter
        );
        g.setColor(Color.GREEN);
        paintIndicator(top, g, size);
        paintIndicator(right, g, size);
        paintIndicator(bottom, g, size);
        paintIndicator(left, g, size);
    }

    private int scaleTo(float scale, int size){
        return (int) Math.round(scale*(float)size);
    }

    private void fillRect(Graphics2D g, int x, int y, int width, int height){
        if(width  < 0) x += width;
        if(height < 0) y += height;
        g.fillRect(x, y, Math.abs(width), Math.abs(height));
    }

    private void paintIndicator(IndicatorBarSpecs ibs, Graphics2D g, int size){
        float widthScalar = (ibs.markAxis == Axis.X_AXIS)?
            ibs.length + ibs.stroke : ibs.stroke;
        float heightScalar = (ibs.markAxis == Axis.Y_AXIS)?
            ibs.length + ibs.stroke : ibs.stroke;
        g.fillRect(
            scaleTo(ibs.left, size),
            scaleTo(ibs.upper, size),
            scaleTo(widthScalar, size),
            scaleTo(heightScalar, size)
            );
        markAxis((float)altitude, 10.0f, 20, 4,
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
    }

    enum DrawPoint{ LeftCenter, RightCenter, TopCenter, BottomCenter }
    void drawString(DrawPoint dp, Graphics2D g, String s, int x, int y){
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

    Font setFontHeight(Font f, int newHeight){
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

    int angleOffset(double theta, int size){
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
        double wave = 0.0;
        while(f.isShowing()) {
            try {
                wave += 0.005;
                ah.set(
                    0,//Math.cos(wave)/3.0,
                    0,//Math.sin(wave*(3.0/5.0))/10,
                    wave*15.0);
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
