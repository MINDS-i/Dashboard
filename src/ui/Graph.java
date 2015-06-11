package com.ui;

import com.ui.DataSource;

import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

public class Graph extends JPanel{

    class DataConfig{
        DataSource  source;
        Paint       paint;
        boolean     drawn;
        public DataConfig(DataSource s){
            this.source = s;
            this.drawn = false;
            this.paint = (Paint) Color.BLACK;
        }
        public void setPaint(Paint p){
            this.paint = p;
        }
        public void setDrawn(Boolean draw){
            this.drawn = draw;
        }
        Paint getPaint(){
            return this.paint;
        }
        boolean getDrawn(){
            return this.drawn;
        }
        DataSource getSource(){
            return source;
        }
    }
    private List<DataConfig> sources;
    private Timer refreshTimer;
    private double xScale  =  1.0;
    private double yScale  = 20.0;
    private double yCenter =  0.0;
    public List<DataConfig> getSources(){ return sources; }
    double getXScale() { return xScale; }
    double getYScale() { return yScale; }
    double getYCenter(){ return yCenter;}
    void setXScale(double s) { xScale = s; }
    void setYScale(double s) { yScale = s; }
    void setYCenter(double s){ yCenter= s; }

    public Graph(List<DataSource> inputSources){
        sources = new ArrayList<DataConfig>();
        for(DataSource source : inputSources){
            sources.add(new DataConfig(source));
        }

        //repaint at regular interval
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new RefreshTimerTask(), 0, 50);

        //place configuration button
        this.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.add(new JButton(configPopupAction));
    }

    private class RefreshTimerTask extends TimerTask{
        public void run(){
            repaint();
        }
    }

    private Action configPopupAction = new AbstractAction(){
      {
        String text = "Configure";
        putValue(Action.NAME, text);
        putValue(Action.SHORT_DESCRIPTION, text);
      }
      public void actionPerformed(ActionEvent e){
        GraphConfigWindow config = new GraphConfigWindow(Graph.this);
      }
    };

    private void drawGrid(Graphics2D g2d, int width,  int height,
                                          int wlines, int hlines){
        Graphics2D g = (Graphics2D) g2d.create();
        final int dx = width/wlines;
        for(int x=0; x<width; x+=(width/wlines)){
            g.drawLine(x,0,x,height);
        }

        final int hh = height/2;
        final int dy = height/hlines;
        final int center = (int) (yCenter*(hh/yScale)) + hh;

        for(int y=dy; y<height/2; y+=dy){
            g.drawLine(0,center+y,width,center+y);
            g.drawLine(0,center-y,width,center-y);
        }

        g.setStroke(new BasicStroke(3));
        g.drawLine(0, center, width, center); //bold 0 line
    }

    private void drawData(Graphics2D g2d, DataConfig data){
        if(!data.getDrawn()) return;

        //TODO start only drawing in invalidated boxes

        Graphics2D g = (Graphics2D) g2d.create();

        RenderingHints rh = new RenderingHints(
             RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
        //g.setRenderingHints(rh);
        g.setPaint(data.getPaint());

        final DataSource source = data.getSource();
        final double width  = this.getWidth();
        final double hh     = this.getHeight()/2;
        final double scale  = hh/yScale;
        final double center = hh + yCenter*scale;

        int px = 0;
        int py = (int)hh;
        for(int x=0; x<width; x++){
            final double xPos = (1.0d-xScale) + xScale * (((double)x) / width);
            final int d = (int) (source.get(xPos)*scale + center);

            g.drawLine(x, d, px, py);
            px = x;
            py = d;
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        final int width  = this.getWidth();
        final int height = this.getHeight();

        //Draw background
        g2d.setPaint(Color.BLACK);
        drawGrid(g2d, width, height, 16, 10);

        //Draw graph elements
        for(DataConfig data : sources){
            drawData(g2d, data);
        }

        //Foreground draw by swing calling PaintComponents
    }

    public static void main(String[] args) {
        List<DataSource> trialSources = new ArrayList<DataSource>();
        DataSource sin = new DataSource(){
            public double get(double x){
                return Math.sin(x*3.0f*Math.PI);
            }
        };
        DataSource cos = new DataSource(){
            public double get(double x){
                return Math.cos(x*3.0f*Math.PI);
            }
        };
        trialSources.add(sin);
        trialSources.add(cos);

        Graph g = new Graph(trialSources);
        JFrame f = new JFrame("graphTest");
        f.add(g);
        f.pack();
        f.setVisible(true);

        while(true){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
