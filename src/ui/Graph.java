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
    private final static int NUM_HORZ_RULES = 3; //creates 2^NUM_VERT_RULES horizontal rulers
    private final static boolean AA_ON = false; //anti-aliasing render hint

    private List<DataConfig> sources;
    private Timer refreshTimer;
    private double xScale  =  1.0;
    private double yScale  = 20.0;
    private double yCenter =  0.0;
    private GraphConfigWindow config;

    List<DataConfig> getSources(){ return sources; }
    double getXScale() { return xScale; }
    double getYScale() { return yScale; }
    double getYCenter(){ return yCenter;}

    void setXScale(double s) {
        xScale = s;
        if(config != null) config.graphConfigsUpdated();
    }
    void setYScale(double s) {
        yScale = s;
        if(config != null) config.graphConfigsUpdated();
    }
    void setYCenter(double s){
        yCenter= s;
        if(config != null) config.graphConfigsUpdated();
    }

    /*
    clean up graph logic
    add data/pixel units to parameters
    */

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
        this.addHierarchyListener(new HierarchyListener(){
            public void hierarchyChanged(HierarchyEvent e){
                if(isShowing()) return;
                if(config != null){
                    config.close();
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseHandler();
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseWheelListener(mouseAdapter);
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
        if(config == null){
            config = new GraphConfigWindow(Graph.this);
        }
        config.show();
      }
    };

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
        drawLabels(g2d, sources);

        //Foreground draw by swing calling PaintComponents
    }

    private void drawGrid(Graphics2D g2d, int width,  int height,
                                          int wlines, int hlines){
        Graphics2D g = (Graphics2D) g2d.create();

        final double horzRuleScale = Math.getExponent(yScale) - NUM_HORZ_RULES;
        final double horzRuleDelta = Math.pow(2, horzRuleScale);
        final int    hh = height/2;
        final double scale  = hh/yScale;
        final double center = hh+yCenter;
        /**
         * data*scale + center = pixel
         */
        final double maxDataVal = (((double)hh) - yCenter) * yScale / ((double)hh);
        final double minDataVal = -center/scale;
        final double minRule = minDataVal - (minDataVal%horzRuleDelta);

        //horizontal rules
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.LIGHT_GRAY);
        for(double r = minRule; r< maxDataVal; r+=horzRuleDelta){
            final int pY = (int)(r * scale + center);
            g.drawLine(0,pY,width,pY);
            g.drawString(""+(-r+0.0), 10, pY-2); //adding zero prevents "-0.0"
        }

        //vertical rules
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.LIGHT_GRAY);
        final int dx = width/wlines;
        for(int x=0; x<width; x+=(width/wlines)){
            g.drawLine(x,0,x,height);
        }

        g.setStroke(new BasicStroke(3));
        g.setColor(Color.BLACK);
        g.drawLine(0, (int)center, width, (int)center); //bold 0 line

        g.dispose();
    }

    private void drawData(Graphics2D g2d, DataConfig data){
        if(!data.getDrawn()) return;

        //TODO start only drawing in invalidated boxes

        Graphics2D g = (Graphics2D) g2d.create();

        if(AA_ON){
            RenderingHints rh = new RenderingHints(
                 RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHints(rh);
        }

        g.setPaint(data.getPaint());

        final DataSource source = data.getSource();
        final double width      = this.getWidth();
        final double hh         = this.getHeight()/2;
        final double scale      = hh/yScale;
        final double center     = hh + yCenter;

        int px = 0;
        int py = (int)hh;
        for(int x=0; x<width; x++){
            final double xPos = (1.0d-xScale) + xScale * (((double)x) / width);
            final int y = (int) (-source.get(xPos)*scale + center);

            g.drawLine(x, y, px, py);
            px = x;
            py = y;
        }

        g.dispose();
    }

    private void drawLabels(Graphics2D g2d, List<DataConfig> dcs){
        g2d = (Graphics2D) g2d.create();
        FontMetrics metrics = g2d.getFontMetrics();
        final int dropPoint = metrics.getDescent();
        final int rowHeight = metrics.getHeight()+dropPoint;

        final int RECT_BUFF = 2;
        final int WALL_BUFF = 10;
        final int TEXT_RISE = RECT_BUFF + 3;

        final int startX = WALL_BUFF;
        final int startY = getHeight() - WALL_BUFF;

        int dCount = 0;
        for(int i = 0; i < dcs.size(); i++) {
            final DataConfig dc = dcs.get(i);
            if(dc.getDrawn() == false) continue;
            final int left = startX;
            final int bot  = startY - rowHeight * dCount;
            dCount ++;
            g2d.setPaint(dc.getPaint());

            g2d.fillRect(left,
                            bot-rowHeight - RECT_BUFF,
                            rowHeight - RECT_BUFF,
                            rowHeight - RECT_BUFF*2);

            g2d.drawString(dc.getName(),
                            left+rowHeight, bot-dropPoint-TEXT_RISE);
        }
        g2d.dispose();
    }

    public static void main(String[] args) {
        List<DataSource> trialSources = new ArrayList<DataSource>();
        DataSource sin = new DataSource(){
            public double get(double x){
                return 20.0*Math.sin(x*3.0f*Math.PI);
            }
            public String getName(){
                return "sine";
            }
        };
        DataSource cos = new DataSource(){
            public double get(double x){
                return 20.0*Math.cos(x*3.0f*Math.PI);
            }
            public String getName(){
                return "cosine";
            }
        };
        trialSources.add(sin);
        trialSources.add(cos);

        Graph g = new Graph(trialSources);
        JFrame f = new JFrame("graphTest");
        f.add(g);
        f.pack();
        f.setVisible(true);

        //turn on all the test data sources
        for(DataConfig source : g.getSources()){
            source.setDrawn(true);
        }

        while(f.isShowing()){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DataConfig{
        DataSource  source;
        Paint       paint;
        boolean     drawn;
        public DataConfig(DataSource source, Boolean drawn){
            this.source = source;
            this.drawn = drawn;
            this.paint = (Paint) Color.BLACK;
        }
        public DataConfig(DataSource s){
            this(s, false);
        }
        public void setPaint(Paint p){
            this.paint = p;
        }
        public void setDrawn(Boolean draw){
            this.drawn = draw;
        }
        public Paint getPaint(){
            return this.paint;
        }
        public boolean getDrawn(){
            return this.drawn;
        }
        public DataSource getSource(){
            return source;
        }
        public String getName(){
            return source.getName();
        }
    }

    class MouseHandler extends MouseAdapter{
        private Point dragPoint = null;
        @Override
        public void mousePressed(MouseEvent e){
            switch(e.getButton()){
                case MouseEvent.BUTTON1:
                    dragPoint = e.getPoint();
                    break;
                case MouseEvent.BUTTON2:
                    break;
                case MouseEvent.BUTTON3:
                    break;
                default:
                    break;
            }
        }
        @Override
        public void mouseDragged(MouseEvent e){
            if(dragPoint != null){
                final int dx = e.getPoint().x - dragPoint.x;
                final int dy = e.getPoint().y - dragPoint.y;
                Graph g = Graph.this;
                final double dv = ((double)dy);
                g.setYCenter( g.getYCenter() + dv );

                double xs = ((double)dx)/((double)Graph.this.getWidth());
                xs += g.getXScale();
                xs = Math.max( Math.min(xs, 1.0), 0.05);
                g.setXScale( xs );

                dragPoint = e.getPoint();
            }
        }
        @Override
        public void mouseReleased(MouseEvent e){
            dragPoint = null;
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e){
            final double ZOOM_FACTOR = 0.05;
            double d = e.getPreciseWheelRotation();
            Graph g = Graph.this;
            g.setYScale( d*ZOOM_FACTOR*g.getYScale() + g.getYScale() );
        }
    }

}
