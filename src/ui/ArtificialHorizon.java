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
        paintNumberIndicator(g, size);
    }











    private interface Marker{
        void mark(double percent, double width, String label);
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
      double center,
      double scale,
      int tickCount,
      int majorRate,
      Marker output){
        double firstLowerMarker = Math.floor(center/scale)*scale;
        double normalizedOffset = (center - firstLowerMarker)/(scale*(double)tickCount);
        double tickHeight = 1.0/(double)tickCount;

        for(int i=1; i<tickCount+1; i++){
            double pos = tickHeight*i - normalizedOffset;
            double value = (i - tickCount/2)*scale + firstLowerMarker;

            if( (int)(value / scale) % majorRate == 0 )
                output.mark(pos, 0.9, ""+value);
            else
                output.mark(pos, 0.5, "");
        }
    }


    private void paintNumberIndicator(Graphics2D g, int size){
        final int STROKE = 3;
        final double indicatorHeight = (14.0*(double)size)/20.0;
        final double indicatorWidth  = ( 1.0*(double)size)/20.0;
        final int indicatorBottom = (17*size)/20;
        final int indicatorLeft   = (17*size)/20;
        final int indicatorTop    = ( 3*size)/20;

        g.setColor(Color.GREEN);
        g.fillRect(indicatorLeft, indicatorTop, STROKE, (int)indicatorHeight);

        markAxis(altitude, 10.0, 20, 4,
          (double percent, double width, String label) -> {
            int height =
                indicatorBottom - (int)Math.round(
                    percent*indicatorHeight
                );
            int markWidth =
                (int) Math.round(
                    width*indicatorWidth
                );
            g.fillRect(
                indicatorLeft,
                height,
                markWidth,
                STROKE
                );
            drawCenteredString(
                g,
                label,
                indicatorLeft+(int)indicatorWidth,
                height
            );
        });
    }

    void drawCenteredString(Graphics2D g, String s, int x, int y){
        FontMetrics m = g.getFontMetrics();
        g.drawString(s, x, y + (m.getHeight()-m.getDescent())/2);
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
                    wave*40.0);
                Thread.sleep(20);
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
