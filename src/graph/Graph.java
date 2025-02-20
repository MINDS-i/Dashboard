package com.graph;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Graph extends JPanel {
    private final static float ZOOM_FACTOR = 0.05f;
    private final static int RULER_XOFF = 10;
    private final static int RULER_YOFF = 2;
    private final static int REPAINT_INTERVAL = 50; //milliseconds
    private final static int NUM_HORZ_RULES = 4; //creates 2^NUM_VERT_RULES horizontal rulers
    private final static int NUM_VERT_RULES = 16; //creates x evenly spaces vertical rulers
    /**
     * Graph lines are initial colored by their index mod the number of default
     * paints with these colors. Asside from the initial high contrast colors,
     * the rest are taken from "http://colorbrewer2.org"s "8-class Dark2"
     * palette (licensed under apache 2.0).
     */
    private final static Paint[] DEFAULT_PAINTS = {
            Color.BLACK, Color.BLUE, Color.RED,
            new Color(0x1b9e77), new Color(0xd95f02),
            new Color(0x7570b3), new Color(0xe7298a),
            new Color(0x66a61e), new Color(0xe6ab02),
            new Color(0xa6761d), new Color(0x666666),
    };
    private final List<DataConfig> sources;
    private Timer refreshTimer;
    private GraphConfigWindow config;
    private final Action configPopupAction = new AbstractAction() {
        {
            String text = "Configure";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            if (config == null) {
                config = new GraphConfigWindow(Graph.this);
            }
            config.show();
        }
    };
    private ViewSpec viewSpec = new RTViewSpec();
    private boolean antiAlias = true; //anti-aliasing render hint

    public Graph(List<DataSource> inputSources, boolean defaultState) {
        sources = new ArrayList<>();
        int paintCount = 0;
        for (DataSource source : inputSources) {
            DataConfig dc = new DataConfig(source, defaultState);
            dc.setPaint(DEFAULT_PAINTS[(paintCount++) % DEFAULT_PAINTS.length]);
            sources.add(dc);
        }

        //place configuration button
        this.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.add(new JButton(configPopupAction));

        //call back when the window is closed
        this.addHierarchyListener(e -> {
            boolean showingChanged = (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;
            if (!showingChanged) {
                return;
            }
            if (isShowing()) {
                onShow();
            }
            else {
                onClose();
            }
        });

        //turn on graph mouse listener
        MouseAdapter mouseAdapter = new GraphMouseHandler();
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseWheelListener(mouseAdapter);
    }

    //main method for quicker manual testing
    public static void main(String[] args) {
        List<DataSource> trialSources = new ArrayList<>();
        DataSource sin = new DataSource() {
            public double get(double x) {
                return 20.0 * Math.sin(x * 3.0f * Math.PI);
            }

            public String getName() {
                return "sine";
            }
        };
        DataSource cos = new DataSource() {
            public double get(double x) {
                return 20.0 * Math.cos(x * 3.0f * Math.PI);
            }

            public String getName() {
                return "cosine";
            }
        };
        trialSources.add(sin);
        trialSources.add(cos);

        Graph g = new Graph(trialSources, true);
        //g.setViewSpec(new RTViewSpec(50.0f, 10.0f));

        try {
            BufferedImage img = g.render(400, 800);
            File out = new File("test.png");
            ImageIO.write(img, "png", out);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        JFrame f = new JFrame("graphTest");
        f.add(g);
        f.pack();
        f.setVisible(true);
        while (f.isShowing()) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    List<DataConfig> getSources() {
        return sources;
    }

    public ViewSpec getViewSpec() {
        return viewSpec;
    }

    public void setViewSpec(ViewSpec vs) {
        viewSpec = vs;
    }

    public boolean getAntiAliasing() {
        return antiAlias;
    }

    public void setAntiAliasing(boolean on) {
        antiAlias = on;
    }

    private void onShow() {
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new RepaintTask(), 0, REPAINT_INTERVAL);
    }

    private void onClose() {
        if (config != null) {
            config.close();
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        render((Graphics2D) g, getHeight(), getWidth());
        //Foreground draw by swing calling PaintComponents
    }

    public BufferedImage render(int height, int width) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        {
            Graphics g = g2d.create();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();
        }
        render(g2d, height, width);
        g2d.dispose();
        return img;
    }

    private void render(Graphics2D g2d, int height, int width) {
        final View view = viewSpec.at(height, width);

        //Draw background
        g2d.setPaint(Color.BLACK);
        drawGrid(g2d, view);

        //Draw graph elements
        for (DataConfig data : sources) {
            if (data.getDrawn()) {
                drawData(g2d, data, view);
            }
        }
        drawLabels(g2d, sources, view);
    }

    private void drawGrid(Graphics2D g2d, View v) {
        Graphics2D g = (Graphics2D) g2d.create();

        final int horzRuleScale = Math.getExponent(v.yRange()) - NUM_HORZ_RULES;
        final float horzRuleDelta = (float) Math.pow(2.0, horzRuleScale);
        final float maxDataVal = v.yPixToData(0);
        final float minDataVal = v.yPixToData(v.height());
        final float minRule = minDataVal - (minDataVal % horzRuleDelta);

        if (horzRuleDelta <= 0.0f) {
            throw new RuntimeException("Horz rule spacing must be positive");
        }

        //horizontal rules
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.LIGHT_GRAY);
        for (float r = minRule; r < maxDataVal; r += horzRuleDelta) {
            final int pY = v.yDataToPix(r);
            g.drawLine(0, pY, v.width(), pY);
            g.drawString("" + r, RULER_XOFF, pY - RULER_YOFF);
        }

        //vertical rules
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.LIGHT_GRAY);
        final int dx = v.width() / NUM_VERT_RULES;
        for (int x = 0; x < v.width(); x += dx) {
            g.drawLine(x, 0, x, v.height());
        }

        //bold 0 line
        g.setStroke(new BasicStroke(3));
        g.setColor(Color.BLACK);
        g.drawLine(0, v.yDataToPix(0), v.width(), v.yDataToPix(0)); //bold 0 line

        g.dispose();
    }

    private void drawData(Graphics2D g2d, DataConfig data, View v) {
        Graphics2D g = (Graphics2D) g2d.create();
        if (antiAlias) {
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHints(rh);
        }
        g.setPaint(data.getPaint());

        final DataSource source = data.getSource();
        GeneralPath dataShape = new GeneralPath();
        dataShape.moveTo(0, 0);

        for (int x = 0; x < v.width(); x++) {
            final float xData = v.xPixToData(x);
            final float y = v.yDataToPix((float) source.get(xData));
            dataShape.lineTo(x, y);
        }

        g.draw(dataShape);

        g.dispose();
    }

    private void drawLabels(Graphics2D g2d, List<DataConfig> dcs, View v) {
        g2d = (Graphics2D) g2d.create();
        FontMetrics metrics = g2d.getFontMetrics();
        final int dropPoint = metrics.getDescent();
        final int rowHeight = metrics.getHeight() + dropPoint;

        final int RECT_BUFF = 2;
        final int WALL_BUFF = 10;
        final int TEXT_RISE = RECT_BUFF + 3;

        final int startX = WALL_BUFF;
        final int startY = v.height() - WALL_BUFF;

        int dCount = 0;
        for (final DataConfig dc : dcs) {
            if (!dc.getDrawn()) {
                continue;
            }
            final int left = startX;
            final int bot = startY - rowHeight * dCount;
            dCount++;
            g2d.setPaint(dc.getPaint());

            g2d.fillRect(left,
                    bot - rowHeight - RECT_BUFF,
                    rowHeight - RECT_BUFF,
                    rowHeight - RECT_BUFF * 2);

            g2d.drawString(dc.getName(),
                    left + rowHeight, bot - dropPoint - TEXT_RISE);
        }
        g2d.dispose();
    }

    static class DataConfig {
        DataSource source;
        Paint paint;
        boolean drawn;

        public DataConfig(DataSource source, Boolean drawn) {
            this.source = source;
            this.drawn = drawn;
            this.paint = Color.BLACK;
        }

        public DataConfig(DataSource s) {
            this(s, false);
        }

        public Paint getPaint() {
            return this.paint;
        }

        public void setPaint(Paint p) {
            this.paint = p;
        }

        public boolean getDrawn() {
            return this.drawn;
        }

        public void setDrawn(Boolean draw) {
            this.drawn = draw;
        }

        public DataSource getSource() {
            return source;
        }

        public String getName() {
            return source.getName();
        }
    }

    private class RepaintTask extends TimerTask {
        public void run() {
            repaint();
        }
    }

    class GraphMouseHandler extends MouseAdapter {
        private Point dragPoint = null;

        @Override
        public void mousePressed(MouseEvent e) {
            switch (e.getButton()) {
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
        public void mouseDragged(MouseEvent e) {
            if (dragPoint != null) {
                final double dx = e.getPoint().x - dragPoint.x;
                final double dy = e.getPoint().y - dragPoint.y;
                viewSpec.panY((int) -dy, getHeight());
                viewSpec.panX((int) dx, getWidth());
                //save current point for delta calculations
                dragPoint = e.getPoint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragPoint = null;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            float d = (float) e.getPreciseWheelRotation();
            viewSpec.zoom(1.0f + d * ZOOM_FACTOR);
        }
    }
}
