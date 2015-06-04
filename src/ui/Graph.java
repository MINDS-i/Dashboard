package com.ui;

import com.ui.DataSource;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.ArrayList;

public class Graph extends JPanel{
    private Collection<DataSource> sources;

    public Graph(){
        sources = new ArrayList<DataSource>();
    }

    public void addSource(DataSource ds){
        sources.add(ds);
    }

    private void drawGrid(Graphics2D g2d, int width,  int height,
                                          int wlines, int hlines){
        Graphics2D g = (Graphics2D) g2d.create();
        final int dx = width/wlines;
        for(int x=0; x<width; x+=(width/wlines)){
            g.drawLine(x,0,x,height);
        }

        final int hh = height/2;
        final int dy = height/hlines;

        for(int y=dy; y<height/2; y+=dy){
            g.drawLine(0,hh+y,width,hh+y);
            g.drawLine(0,hh-y,width,hh-y);
        }

        g.setStroke(new BasicStroke(3));
        g.drawLine(0, hh, width, hh); //bold 0 line
    }

    private void drawData(Graphics2D g2d, DataSource data){
        Graphics2D g = (Graphics2D) g2d.create();
        g.setPaint(data.getPaint());
        final double width = this.getWidth();
        final double hh  = this.getHeight()/2;
        for(int x=0; x<width; x++){
            final double xPos = ((double)x) / width;
            final int d = (int) (data.get( xPos )*hh + hh);

            //TODO draw line from last point to here
            g.drawRect(x, d, 1, 1);
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        final int width  = this.getWidth();
        final int height = this.getHeight();

        g2d.setPaint(Color.BLACK);
        drawGrid(g2d, width, height, 16, 10);

        for(DataSource data : sources){
            drawData(g2d, data);
        }
    }

    public static void main(String[] args) {
        Graph g = new Graph();
        JFrame f = new JFrame("graphTest");
        f.add(g);
        f.pack();
        f.setVisible(true);

        DataSource sin = new DataSource(){
            public double get(double x){
                return Math.sin(x*3.0f*Math.PI);
            }
            public Paint getPaint(){
                return (Paint) Color.BLUE;
            }
        };
        DataSource cos = new DataSource(){
            public double get(double x){
                return Math.cos(x*3.0f*Math.PI);
            }
            public Paint getPaint(){
                return (Paint) Color.RED;
            }
        };
        g.addSource(sin);
        g.addSource(cos);

        while(true){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
