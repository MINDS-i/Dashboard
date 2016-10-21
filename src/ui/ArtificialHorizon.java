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
    private double pitch, roll;
    private Color groundColor = new Color(0x7D5233);
    private Color airColor = new Color(0x5B93C5);
    private Color wingColor = new Color(0xEA8300);
    private Color barColor = new Color(0xBBBBBB);

    public ArtificialHorizon(){
    }

    public void set(double pitch, double roll){
        synchronized(this){
            this.pitch = pitch;
            this.roll = roll;
            this.repaint();
            Toolkit.getDefaultToolkit().sync();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int size = Math.min(getHeight(), getWidth());
        Graphics2D g2d = (Graphics2D)g.create();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
        render(g2d, size);
        g2d.dispose();

        g.setColor(Color.BLACK);
        int box = Math.max(getHeight(), getWidth());
        g.fillRect(0, size, box, box);
        g.fillRect(size, 0, box, box);
    }

    private void render(Graphics2D g, int size){
        Graphics2D background = (Graphics2D)g.create();
        background.translate(size/2, size/2);
        background.rotate(roll);
        background.translate(-size, -size);
        renderBackground(background, size*2);
        background.dispose();

        paintPlaneIndicator(g, size);
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

    //main method for quicker manual testing
    public static void main(String[] args) {
        ArtificialHorizon ah = new ArtificialHorizon();

        JFrame f = new JFrame("ArtificialHorizon");
        f.add(ah);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        double wave = 0.0;
        while(f.isShowing()) {
            try {
                wave += 0.01;
                ah.set(Math.cos(wave)/3.0,Math.sin(wave*(3.0/5.0))/10.0);
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
