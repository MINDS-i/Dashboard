package com.graph;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
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
    private final static int RULER_XOFF = 10;
    private final static int RULER_YOFF =  2;
    private final static int REPAINT_INTERVAL = 50; //milliseconds
    private final static int NUM_HORZ_RULES = 4; //creates 2^NUM_VERT_RULES horizontal rulers
    private final static int NUM_VERT_RULES = 16; //creates x evenly spaces vertical rulers
    private final static boolean AA_ON = false; //anti-aliasing render hint
    private final static double XSCALE_MAX = 1.00;
    private final static double XSCALE_MIN = 0.05;

    private List<DataConfig> sources;
    private Timer refreshTimer;
    private GraphConfigWindow config;
    private double xScale  = XSCALE_MAX; //how much of the data's x range to display
    private double yScale  = 40.0; //scale of data per half graph height
    private double yCenter =  0.0; //pixel offset for where 0 line should be

    List<DataConfig> getSources(){ return sources; }
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
    double getXScale() { return xScale;  }
    double getYScale() { return yScale;  }
    double getYCenter(){ return yCenter; }

    /*
    clean up graph logic
    add data/pixel units to parameters
    */
    public Graph(List<DataSource> inputSources, boolean defaultState){
        sources = new ArrayList<DataConfig>();
        for(DataSource source : inputSources){
            sources.add(new DataConfig(source, defaultState));
        }

        //place configuration button
        this.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.add(new JButton(configPopupAction));

        //call back when the window is closed
        this.addHierarchyListener(new HierarchyListener(){
            public void hierarchyChanged(HierarchyEvent e){
                boolean showing_changed = (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;
                if(showing_changed && !isShowing()){
                    onClose();
                }
            }
        });

        //repaint at regular interval
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new RefreshTimerTask(), 0, REPAINT_INTERVAL);

        //turn on graph mouse listener
        MouseAdapter mouseAdapter = new GraphMouseHandler();
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseWheelListener(mouseAdapter);
    }

    private void onClose(){
        if(config != null) config.close();
        if(refreshTimer != null) refreshTimer.cancel();
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

        //find data->pixel conversion constants
        final Graphics2D g2d = (Graphics2D) g;

        final int height    = getHeight();
        final int width     = getWidth();
        final double scale  = -height/yScale;
        final double center = (height/2.0)+scale*-yCenter;
        //viewSpec.at(getHeight(), getWidth());
        final View view = new View( getHeight(), getWidth(),
            (float)scale, (float)center,
            (float)getWidth() , 0.00f
            );


        //Draw background
        g2d.setPaint(Color.BLACK);
        drawGrid(g2d, view);

        //Draw graph elements
        for(DataConfig data : sources){
            if(data.getDrawn())
                drawData(g2d, data, view);
        }
        drawLabels(g2d, sources, view);

        //Foreground draw by swing calling PaintComponents
    }

    static class ViewSpec{
/*        View at(int height, int width){

        }*/
    }

    static class View{
        private int h,w;
        private float yScale, yCenter;
        private float xScale, xCenter;
        View(int h, int w, float yScale, float yCenter, float xScale, float xCenter){
            this.h = h;
            this.w = w;
            this.yScale  = yScale;
            this.yCenter = yCenter;
            this.xScale  = xScale;
            this.xCenter = xCenter;
        }
        int height() { return h; }
        int width()  { return w; }
        float yRange() { return (float)h / yScale; }
        float xRange() { return (float)w / xScale; }
        float yPixToData(int y) { return ((float)y-yCenter) / yScale; }
        float xPixToData(int x) { return ((float)x-xCenter) / xScale; }
        int yDataToPix(float y) { return (int)(y*yScale + yCenter);   }
        int xDataToPix(float x) { return (int)(x*xScale + xCenter);   }
    }

    private void drawGrid(Graphics2D g2d, View v){
        Graphics2D g = (Graphics2D) g2d.create();

        final int   horzRuleScale = Math.getExponent(v.yRange()) - NUM_HORZ_RULES;
        final float horzRuleDelta = pow2(horzRuleScale);
        final float maxDataVal = v.yPixToData(0);
        final float minDataVal = v.yPixToData(v.height());
        final float minRule    = minDataVal - (minDataVal%horzRuleDelta);

        //horizontal rules
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.LIGHT_GRAY);
        for(float r = minRule; r<maxDataVal; r+=horzRuleDelta){
            final int pY = v.yDataToPix(r);
            g.drawLine(0, pY, v.width(), pY);
            g.drawString(""+r, RULER_XOFF, pY-RULER_YOFF);
        }

        //vertical rules
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.LIGHT_GRAY);
        final int dx = v.width()/NUM_VERT_RULES;
        for(int x=0; x<v.width(); x+=dx){
            g.drawLine(x,0,x,v.height());
        }

        //bold 0 line
        g.setStroke(new BasicStroke(3));
        g.setColor(Color.BLACK);
        g.drawLine(0, v.yDataToPix(0), v.width(), v.yDataToPix(0)); //bold 0 line

        g.dispose();
    }

    private void drawData(Graphics2D g2d, DataConfig data, View v){
        Graphics2D g = (Graphics2D) g2d.create();
        if(AA_ON){
            RenderingHints rh = new RenderingHints(
                 RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHints(rh);
        }
        g.setPaint(data.getPaint());

        final DataSource source = data.getSource();
        GeneralPath dataShape  = new GeneralPath();
        dataShape.moveTo(0,0);

        for(int x=0; x<v.width(); x++){
            final float xData = v.xPixToData(x);
            final float y     = v.yDataToPix((float)source.get((double)xData));
            dataShape.lineTo(x, y);
        }

        g.draw(dataShape);

        g.dispose();
    }

    private void drawLabels(Graphics2D g2d, List<DataConfig> dcs, View v){
        g2d = (Graphics2D) g2d.create();
        FontMetrics metrics = g2d.getFontMetrics();
        final int dropPoint = metrics.getDescent();
        final int rowHeight = metrics.getHeight()+dropPoint;

        final int RECT_BUFF = 2;
        final int WALL_BUFF = 10;
        final int TEXT_RISE = RECT_BUFF + 3;

        final int startX = WALL_BUFF;
        final int startY = v.height() - WALL_BUFF;

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

    private float pow2(int exp){
        int abs = Math.abs(exp);
        float output = (float)(1 << abs);
        if(exp < 0) return 1.0f / output;
        return output;
    }

    static class DataConfig{
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

    class GraphMouseHandler extends MouseAdapter{
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
                final Graph   g = Graph.this;
                final double dx = e.getPoint().x - dragPoint.x;
                final double dy = e.getPoint().y - dragPoint.y;
                //scale x drag motion by width, use it to alter the graph x scale
                double xs = dx/((double)Graph.this.getWidth());
                xs += g.getXScale();
                xs = Math.max( Math.min(xs, XSCALE_MAX), XSCALE_MIN);
                g.setXScale( xs );
                //pan the y axis by delta y
                final double dyunit = (dy/g.getHeight()) * g.getYScale();
                g.setYCenter( g.getYCenter() + dyunit );
                //save current point for delta calculations
                dragPoint = e.getPoint();
            }
        }
        @Override
        public void mouseReleased(MouseEvent e){
            dragPoint = null;
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e){
            final double ZOOM_FACTOR = 0.025;
            double d = e.getPreciseWheelRotation();
            Graph g = Graph.this;
            g.setYScale( d*ZOOM_FACTOR*g.getYScale() + g.getYScale() );
        }
    }

    //main method for quicker manual testing
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

        Graph g = new Graph(trialSources, true);
        JFrame f = new JFrame("graphTest");
        f.add(g);
        f.pack();
        f.setVisible(true);

        while(f.isShowing()){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
