/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stepan Rutz - initial implementation
 *    Brett Menzies - MINDS-i adaptation
 ******************************************************************************/

package com.map;
import com.Dashboard;
import com.map.Dot;
import com.map.TileServer;
import com.map.WaypointPanel;
import com.serial.*;
import com.serial.Messages.*;
import com.ContextViewer;
import com.Context;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;
import java.util.Iterator;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

public class MapPanel extends JPanel implements ContextViewer {
    private static final TileServer[] TILESERVERS = {
        new TileServer("http://otile1.mqcdn.com/tiles/1.0.0/sat/", 18),
        new TileServer("http://otile1.mqcdn.com/tiles/1.0.0/map/", 18),
    };

    private static final int PREFERRED_WIDTH = 320;
    private static final int PREFERRED_HEIGHT = 200;

    private static final int ANIMATION_FPS = 15, ANIMATION_DURARTION_MS = 500;

    private static final int TILE_SIZE = 256;
    private static final int CACHE_SIZE = 64;
    private static final int MAGNIFIER_SIZE = 100;

    private static final Color ACTIVE_LINE_FILL =  new Color(1.f,1.f,0.f,1f);
    private static final Color PATH_LINE_FILL   =  new Color(0f,0f,0f, 1f);
    private static final Color LINE_BORDER      =  new Color(.5f,.5f,.5f,0f);

    private Dimension mapSize = new Dimension(0, 0);
    private Point mapPosition = new Point(0, 0);
    private int zoom;
    private DragListener mouseListener = new DragListener();
    private Animation animation;
    private Rectangle magnifyRegion;

    private TileServer tileServer = TILESERVERS[0];

    private WaypointPanel waypointPanel;
    private BorderLayout border = new BorderLayout();
    private TileCache cache = new TileCache();
    private Stats stats = new Stats();
    private JPanel centerPanel = new JPanel();

    protected double smoothScale = 1.0D;
    private boolean useAnimations = false;
    private int smoothOffset = 0;
    private Point smoothPosition, smoothPivot;

    private Dot rover = new Dot();
    private Context context;

    public MapPanel(Context cxt) {
        this(cxt, new Point(8282, 5179), 6, null, null, null);
    }

    public MapPanel(Context cxt, Point mapPosition, int zoom){
        this(cxt, mapPosition, zoom, null, null, null);
    }

    public MapPanel(Context cxt, Point mapPosition, int zoom, JPanel north,
                                                              JPanel east,
                                                              JPanel south) {
        context = cxt;
        context.registerViewer(this);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);

        border.setVgap(-20);
        setOpaque(true);
        setBackground(new Color(0xc0, 0xc0, 0xc0));
        setLayout(border);

        waypointPanel = new WaypointPanel(context, this);
        centerPanel.setLayout(new BorderLayout());
        if(north != null) centerPanel.add(Contain(north),BorderLayout.NORTH);
        if(south != null) centerPanel.add(Contain(south),BorderLayout.SOUTH);
        centerPanel.setOpaque(false);

        add(centerPanel, BorderLayout.CENTER);
        add(Contain(waypointPanel),BorderLayout.WEST);
        if(east != null)
            add(Contain(east),BorderLayout.EAST);

        setZoom(zoom);
        setMapPosition(mapPosition);
    }

    public void waypointUpdate(){
        repaint();
    }

    private void testTileServer(TileServer server){
        String urlstring = getTileString(tileServer, 1, 1, 1);
        try {
            URL url = new URL(urlstring);
            Object content = url.getContent();
        } catch (Exception e) {
            tileServer.setBroken(true);
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(MapPanel.this),
                "The tileserver \"" + getTileServer().getURL() + "\" could not be reached.\r\nCheck internet connection",
                "TileServer not reachable.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkTileServers() {
        for (TileServer server : TILESERVERS) {
            final TileServer s = server;
            Runnable tileTestRunner = new Runnable() {
                public void run() {
                    testTileServer(s);
                }
            };
            SwingUtilities.invokeLater(tileTestRunner);
        }
    }

    public void setTileServer(TileServer tileServer) {
        if(this.tileServer == tileServer)
            return;
        this.tileServer = tileServer;
        while (getZoom() > tileServer.getMaxZoom())
            zoomOut(new Point(getWidth() / 2, getHeight() / 2));
    }

    public void nextTileServer() {
        int index = Arrays.asList(TILESERVERS).indexOf(getTileServer());
        if (index == -1)
            return;
        setTileServer(TILESERVERS[(index + 1) % TILESERVERS.length]);
        repaint();
    }

    public TileServer getTileServer() {
        return tileServer;
    }


    public boolean isUseAnimations() {
        return useAnimations;
    }

    public void setUseAnimations(boolean useAnimations) {
        this.useAnimations = useAnimations;
    }

    public WaypointPanel getWaypointPanel() {
        return waypointPanel;
    }

    public TileCache getCache() {
        return cache;
    }

    public Stats getStats() {
        return stats;
    }

    public Point getMapPosition() {
        return new Point(mapPosition.x, mapPosition.y);
    }

    public void setMapPosition(Point mapPosition) {
        setMapPosition(mapPosition.x, mapPosition.y);
    }

    public void setMapPosition(int x, int y) {
        if (mapPosition.x == x && mapPosition.y == y)
            return;
        Point oldMapPosition = getMapPosition();
        mapPosition.x = x;
        mapPosition.y = y;
        firePropertyChange("mapPosition", oldMapPosition, getMapPosition());
    }

    public void translateMapPosition(int tx, int ty) {
        setMapPosition(mapPosition.x + tx, mapPosition.y + ty);
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (zoom == this.zoom)
            return;
        int oldZoom = this.zoom;
        this.zoom = Math.min(getTileServer().getMaxZoom(), zoom);
        mapSize.width = getXMax();
        mapSize.height = getYMax();
        firePropertyChange("zoom", oldZoom, zoom);
    }

    public void setVgap(int gap){
        border.setVgap(gap);
        repaint();
    }

    public void setHgap(int gap){
        border.setHgap(gap);
        repaint();
    }

    public void zoomInAnimated(Point pivot) {
        if (!useAnimations) {
            zoomIn(pivot);
            return;
        }
        if (animation != null)
            return;
        mouseListener.downCoords = null;
        animation = new Animation(AnimationType.ZOOM_IN, ANIMATION_FPS, ANIMATION_DURARTION_MS) {
            protected void onComplete() {
                smoothScale = 1.0d;
                smoothPosition = smoothPivot = null;
                smoothOffset = 0;
                animation = null;
                repaint();
            }
            protected void onFrame() {
                smoothScale = 1.0 + getFactor();
                repaint();
            }

        };
        smoothPosition = new Point(mapPosition.x, mapPosition.y);
        smoothPivot = new Point(pivot.x, pivot.y);
        smoothOffset = -1;
        zoomIn(pivot);
        animation.run();
    }

    public void zoomOutAnimated(Point pivot) {
        if (!useAnimations) {
            zoomOut(pivot);
            return;
        }
        if (animation != null)
            return;
        mouseListener.downCoords = null;
        animation = new Animation(AnimationType.ZOOM_OUT, ANIMATION_FPS, ANIMATION_DURARTION_MS) {
            protected void onComplete() {
                smoothScale = 1.0d;
                smoothPosition = smoothPivot = null;
                smoothOffset = 0;
                animation = null;
                repaint();
            }
            protected void onFrame() {
                smoothScale = 1 - .5 * getFactor();
                repaint();
            }

        };
        smoothPosition = new Point(mapPosition.x, mapPosition.y);
        smoothPivot = new Point(pivot.x, pivot.y);
        smoothOffset = 1;
        zoomOut(pivot);
        animation.run();
    }

    public void zoomIn(Point pivot) {
        if (getZoom() >= getTileServer().getMaxZoom())
            return;
        Point mapPosition = getMapPosition();
        int dx = pivot.x;
        int dy = pivot.y;
        setZoom(getZoom() + 1);
        setMapPosition(mapPosition.x * 2 + dx, mapPosition.y * 2 + dy);
        repaint();
    }

    public void zoomOut(Point pivot) {
        if (getZoom() <= 1)
            return;
        Point mapPosition = getMapPosition();
        int dx = pivot.x;
        int dy = pivot.y;
        setZoom(getZoom() - 1);
        setMapPosition((mapPosition.x - dx) / 2, (mapPosition.y - dy) / 2);
        repaint();
    }

    public int getXTileCount() {
        return (1 << zoom);
    }

    public int getYTileCount() {
        return (1 << zoom);
    }

    public int getXMax() {
        return TILE_SIZE * getXTileCount();
    }

    public int getYMax() {
        return TILE_SIZE * getYTileCount();
    }

    public Point getCursorPosition() {
        return new Point(mapPosition.x + mouseListener.mouseCoords.x, mapPosition.y + mouseListener.mouseCoords.y);
    }

    public Point getTile(Point position) {
        return new Point((int) Math.floor(((double) position.x) / TILE_SIZE),(int) Math.floor(((double) position.y) / TILE_SIZE));
    }

    public Point getCenterPosition() {
        return new Point(mapPosition.x + getWidth() / 2, mapPosition.y + getHeight() / 2);
    }

    public void setCenterPosition(Point p) {
        setMapPosition(p.x - getWidth() / 2, p.y - getHeight() / 2);
    }

    public Point.Double getLongitudeLatitude(Point position) {
        return new Point.Double(
                position2lon(position.x, getZoom()),
                position2lat(position.y, getZoom()));
    }

    public Point computePosition(Point.Double coords) {
        int x = lon2position(coords.x, getZoom());
        int y = lat2position(coords.y, getZoom());
        return new Point(x, y);
    }

    public Point computeScreenPosition(Point.Double coords){
        int x = lon2position(coords.x, getZoom()) - mapPosition.x;
        int y = lat2position(coords.y, getZoom()) - mapPosition.y;
        return new Point(x,y);
    }
    //--------------------------------------------------------------------------
    //MINDSi waypoint managing code
    public void updateRoverLatitude(double lat){
        rover.setLatitude(lat);
    }
    public void updateRoverLongitude(double lng){
        rover.setLongitude(lng);
    }
    public int isOverDot(Point clk){
        for(int i=0; i<context.waypoint.size(); i++){
/*            if(radialOverlap( computeScreenPosition(context.waypoint.get(i).getLocation()) , clk ,
                                                 context.theme.waypointImage.getWidth()/2 )){
                return i;
            }*/
            Point dot = computeScreenPosition(context.waypoint.get(i).getLocation());
            if (Math.abs(clk.x-dot.x-1) > context.theme.waypointImage.getWidth ()/2) continue;
            if (Math.abs(clk.y-dot.y-1) > context.theme.waypointImage.getHeight()/2) continue;
            return i;
        }
        return -1;
    }

    public int isOverLine(Point p){
        if(context.waypoint.size()<2) return -1;
        Point prevPoint = computeScreenPosition(rover.getLocation());
        for(int i=0; i<context.waypoint.size(); i++){
            Point thisPoint = computeScreenPosition(
                                context.waypoint.get(i).getLocation());
            if( distFromLine(prevPoint, thisPoint, p) < 8 ){
                return i;
            }
            prevPoint = thisPoint;
        }
        return -1;
    }

    public int distFromLine(Point a, Point b, Point idp){
        float abSlope = (a.y-b.y)/(a.x-b.x+.0000001f);
        float abYCept = a.y - abSlope*a.x;
        float perpSlope = (a.x-b.x)/(b.y-a.y+.0000001f);
        float perpYCept = idp.y - perpSlope*idp.x;
        float interceptX = (abYCept - perpYCept) / (perpSlope-abSlope+.0000001f);
        float interceptY = perpSlope*interceptX + perpYCept;
        if( a.x > b.x && a.x > interceptX && interceptX > b.x){
            return (int) Math.floor(
                            Math.sqrt( (interceptX-idp.x)*(interceptX-idp.x) +
                                       (interceptY-idp.y)*(interceptY-idp.y) ));
        } else if ( b.x > a.x && b.x > interceptX && interceptX > a.x){
            return (int) Math.floor(
                            Math.sqrt( (interceptX-idp.x)*(interceptX-idp.x) +
                                       (interceptY-idp.y)*(interceptY-idp.y) ));
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public boolean radialOverlap(Point a, Point b, int maxDist){
        return ( Math.sqrt( (a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) ) < maxDist);
    }
    //--------------------------------------------------------------------------
    //Painting functions
    public void paintLine(Graphics g, Point pointA, Point pointB, Color fill){
        final int WIDTH = 10;
        Graphics2D g2d = (Graphics2D) g.create();
        RenderingHints hints = g2d.getRenderingHints();
        hints.put(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(hints);
        try{
            g2d.translate(pointB.x, pointB.y);
            double angle = Math.atan2(pointA.y-pointB.y, pointA.x-pointB.x);
            g2d.rotate(angle);
            int w = (int)Math.sqrt((Math.abs(pointA.x-pointB.x)*Math.abs(pointA.x-pointB.x))+
                                    (Math.abs(pointA.y-pointB.y)*Math.abs(pointA.y-pointB.y)));
            g2d.setPaint(new GradientPaint(0, 0, fill,
                                                 0,
                                                 -(WIDTH/2),
                                                 LINE_BORDER,
                                                 true));
            g2d.fillRect(0, -(WIDTH/2), w, WIDTH);
        } finally {
            g2d.dispose();
        }
    }

    private void paintDots(Graphics g) {
        if(context.waypoint.size()!=0){
            drawLines(g);
            drowRoverLine(g);
            drawPoints(g);
        }
        drawRover(g);
    }

    private void drawLines(Graphics g){
        Point n = null;
        Point l = null;
        Iterator itr = context.waypoint.iterator();
        while(itr.hasNext()){
            n = computeScreenPosition( ((Dot)itr.next()).getLocation() );
            if(l!=null) paintLine(g, n, l, PATH_LINE_FILL);
            l = n;
        }
        if(context.waypoint.isLooped()){
            n = computeScreenPosition( context.waypoint.get(0).getLocation() );
            paintLine(g, n, l, PATH_LINE_FILL);
        }
    }

    private void drawPoints(Graphics g){
        Point tmp;
        Iterator itr = context.waypoint.iterator();
        int i=0;
        while(itr.hasNext()){
            tmp = computeScreenPosition( ((Dot)itr.next()).getLocation() );
            if(i++==waypointPanel.getSelectedWaypoint())
                drawImg(g, context.theme.waypointSelected, tmp);
            else
                drawImg(g, context.theme.waypointImage, tmp);
        }
    }

    private void drawRover(Graphics g){
        Point roverPoint = computeScreenPosition(rover.getLocation());
        drawImg(g, context.theme.roverImage, roverPoint);
    }

    private void drowRoverLine(Graphics g){
        if(context.waypoint.getTarget() >= context.waypoint.size()) {
            System.err.println("roverTarget out of Bounds");
            return;
        }
        Point n = computeScreenPosition( rover.getLocation() );
        Point l = computeScreenPosition( context.waypoint.getTargetWaypoint().getLocation() );
        paintLine(g, n, l, ACTIVE_LINE_FILL);
    }

    private void drawImg(Graphics g, BufferedImage img, Point loc){
        g.translate(-img.getWidth()/2, -img.getHeight()/2);
        g.drawImage( img , loc.x, loc.y, this);
        g.translate( img.getWidth()/2, img.getHeight()/2);
    }

    private void paintInternal(Graphics2D g) {
        stats.reset();
        long t0 = System.currentTimeMillis();

        if (smoothPosition != null) {
            {
                Point position = getMapPosition();
                Painter painter = new Painter(this, getZoom());
                painter.paint(g, position, null);
            }
            Point position = new Point(smoothPosition.x, smoothPosition.y);
            Painter painter = new Painter(this, getZoom() + smoothOffset);
            painter.setScale(smoothScale);

            float t = (float) (animation == null ? 1f : 1 - animation.getFactor());
            painter.setTransparency(t);
            painter.paint(g, position, smoothPivot);
            if (animation != null && animation.getType() == AnimationType.ZOOM_IN) {
                int cx = smoothPivot.x, cy = smoothPivot.y;
                drawScaledRect(g, cx, cy, animation.getFactor(), 1 + animation.getFactor());
            } else if (animation != null && animation.getType() == AnimationType.ZOOM_OUT) {
                int cx = smoothPivot.x, cy = smoothPivot.y;
                drawScaledRect(g, cx, cy, animation.getFactor(), 2 - animation.getFactor());
            }
        }

        if (smoothPosition == null) {
            Point position = getMapPosition();
            Painter painter = new Painter(this, getZoom());
            painter.paint(g, position, null);
        }

        paintDots(g);
        long t1 = System.currentTimeMillis();
        stats.dt = t1 - t0;
    }

    private void drawScaledRect(Graphics2D g, int cx, int cy, double f, double scale) {
        AffineTransform oldTransform = g.getTransform();
        g.translate(cx, cy);
        g.scale(scale, scale);
        g.translate(-cx, -cy);
        int c = 0x80 + (int) Math.floor(f * 0x60);
        if (c < 0) c = 0;
        else if (c > 255) c = 255;
        Color color = new Color(c, c, c);
        g.setColor(color);
        g.drawRect(cx - 40, cy - 30, 80, 60);
        g.setTransform(oldTransform);
    }

    protected void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
            paintInternal(g);
        } finally {
            g.dispose();
        }
    }
    //-------------------------------------------------------------------------
    // helpers

    private final static BufferedImage loadImg = new BufferedImage(1,1,
                                                    BufferedImage.TYPE_INT_ARGB);
    private void loadTile(TileCache c, TileServer ts, int x, int y, int zoom){
        c.put(ts, x, y, zoom, loadImg);
        Runnable load = new Runnable(){
            public void run(){
                final String url = getTileString(ts, x, y, zoom);
                try {
                    Image n = Toolkit.getDefaultToolkit().getImage(new URL(url));
                    //if n is null, painter will try again
                    c.put(ts, x, y, zoom, n);
                    MapPanel.this.repaint();
                } catch (Exception e) {
                    System.err.println("failed to load url \"" + url + "\"");
                }
            }
        };
        (new Thread(load)).start();
    }

    private static final class Painter {
        private final int zoom;
        private float transparency = 1F;
        private double scale = 1d;
        private final MapPanel mapPanel;

        private Painter(MapPanel mapPanel, int zoom) {
            this.mapPanel = mapPanel;
            this.zoom = zoom;
        }

        public float getTransparency() {
            return transparency;
        }

        public void setTransparency(float transparency) {
            this.transparency = transparency;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            this.scale = scale;
        }

        private void paint(Graphics2D gOrig, Point mapPosition, Point scalePosition) {
            Graphics2D g = (Graphics2D) gOrig.create();
            try {
                if (getTransparency() < 1f && getTransparency() >= 0f) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transparency));
                }

                if (getScale() != 1d) {
                    AffineTransform xform = new AffineTransform();
                    xform.translate(scalePosition.x, scalePosition.y);
                    xform.scale(scale, scale);
                    xform.translate(-scalePosition.x, -scalePosition.y);
                    g.transform(xform);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                }
                int width = mapPanel.getWidth();
                int height = mapPanel.getHeight();
                int x0 = (int) Math.floor(((double) mapPosition.x) / TILE_SIZE);
                int y0 = (int) Math.floor(((double) mapPosition.y) / TILE_SIZE);
                int x1 = (int) Math.ceil(((double) mapPosition.x + width) / TILE_SIZE);
                int y1 = (int) Math.ceil(((double) mapPosition.y + height) / TILE_SIZE);

                int nTx = (int) Math.ceil( ((double)x1-x0)/2 );
                int nTy = (int) Math.ceil( ((double)y1-y0)/2 );
                int  cx = x0 + nTx-1;
                int  cy = y0 + nTy-1;
                int  dx = x0 * TILE_SIZE - mapPosition.x + (nTx-1)*TILE_SIZE;
                int  dy = y0 * TILE_SIZE - mapPosition.y + (nTy-1)*TILE_SIZE;
                for(int i = 0; i <= nTx; i++){
                    int dI = i*TILE_SIZE;
                    for(int j = 0; j <= nTy; j++){
                        int dJ = j*TILE_SIZE;
                        paintTile(g, dx+dI, dy-dJ, cx+i, cy-j);
                        paintTile(g, dx+dI, dy+dJ, cx+i, cy+j);
                        paintTile(g, dx-dI, dy+dJ, cx-i, cy+j);
                        paintTile(g, dx-dI, dy-dJ, cx-i, cy-j);
                    }
                }

                if (getScale() == 1d && mapPanel.magnifyRegion != null) {
                    Rectangle magnifyRegion = new Rectangle(mapPanel.magnifyRegion);
                    magnifyRegion.translate(-mapPosition.x, -mapPosition.y);
                    g.setColor(Color.yellow);
                }
            } finally {
                g.dispose();
            }
        }

        private void paintTile(Graphics2D g, int dx, int dy, int x, int y) {
            boolean DEBUG = false;
            boolean DRAW_IMAGES = true;
            boolean DRAW_OUT_OF_BOUNDS = false;

            boolean imageDrawn = false;
            int xTileCount = 1 << zoom;
            int yTileCount = 1 << zoom;
            boolean tileInBounds = x >= 0 && x < xTileCount && y >= 0 && y < yTileCount;
            boolean drawImage = DRAW_IMAGES && tileInBounds;
            if (drawImage) {
                final TileCache cache = mapPanel.getCache();
                final TileServer tileServer = mapPanel.getTileServer();
                Image image = cache.get(tileServer, x, y, zoom);

                if (image == null) {
                    mapPanel.loadTile(cache, tileServer, x, y, zoom);
                } else {
                    g.drawImage(image, dx, dy, mapPanel);
                    imageDrawn = true;
                }
            }
            if (DEBUG && (!imageDrawn && (tileInBounds || DRAW_OUT_OF_BOUNDS))) {
                g.setColor(Color.blue);
                g.fillRect(dx + 4, dy + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                g.setColor(Color.gray);
                String s = "T " + x + ", " + y + (!tileInBounds ? " #" : "");
                g.drawString(s, dx + 4+ 8, dy + 4 + 12);
            }
        }
    }

    private enum AnimationType {
        ZOOM_IN, ZOOM_OUT
    }

    private static abstract class Animation implements ActionListener {

        private final AnimationType type;
        private final Timer timer;
        private long t0 = -1L;
        private long dt;
        private final long duration;

        public Animation(AnimationType type, int fps, long duration) {
            this.type = type;
            this.duration = duration;
            int delay = 1000 / fps;
            timer = new Timer(delay, this);
            timer.setCoalesce(true);
            timer.setInitialDelay(0);
        }

        public AnimationType getType() {
            return type;
        }

        protected abstract void onComplete();

        protected abstract void onFrame();

        public double getFactor() {
            return (double) getDt() / getDuration();
        }

        public void actionPerformed(ActionEvent e) {
            if (getDt() >= duration) {
                kill();
                onComplete();
                return;
            }
            onFrame();
        }

        public long getDuration() {
            return duration;
        }

        public long getDt() {
            if (!timer.isRunning())
                return dt;
            long now = System.currentTimeMillis();
            if (t0 < 0)
                t0 = now;
            return now - t0 + dt;
        }

        public void run() {
            if (timer.isRunning())
                return;
            timer.start();
        }

        public void kill() {
            if (!timer.isRunning())
                return;
            dt = getDt();
            timer.stop();
        }
    }

    private static class Tile {
        private final String key;
        public final int x, y, z;
        public Tile(String tileServer, int x, int y, int z) {
            this.key = tileServer;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + x;
            result = prime * result + y;
            result = prime * result + z;
            return result;
        }
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tile other = (Tile) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            if (z != other.z)
                return false;
            return true;
        }

    }

    private static class TileCache {
        private LinkedHashMap<Tile,Image> map = new LinkedHashMap<Tile,Image>(CACHE_SIZE, 0.75f, true) {
            protected boolean removeEldestEntry(java.util.Map.Entry<Tile,Image> eldest) {
                boolean remove = size() > CACHE_SIZE;
                return remove;
            }
        };
        public void put(TileServer tileServer, int x, int y, int z, Image image) {
            map.put(new Tile(tileServer.getURL(), x, y, z), image);
        }
        public Image get(TileServer tileServer, int x, int y, int z) {
            //return map.get(new Tile(x, y, z));
            Image image = map.get(new Tile(tileServer.getURL(), x, y, z));
            return image;
        }
        public int getSize() {
            return map.size();
        }
    }

    private static class Stats {
        private int tileCount;
        private long dt;
        private Stats() {
            reset();
        }
        private void reset() {
            tileCount = 0;
            dt = 0;
        }
    }

    public static class CustomSplitPane extends JComponent  {
        private static final int SPACER_SIZE = 4;
        private final boolean horizonal;
        private final JComponent spacer;

        private double split = 0.5;
        private int dx, dy;
        private Component componentOne, componentTwo;

        public CustomSplitPane(boolean horizonal) {
            this.spacer = new JPanel();
            this.spacer.setOpaque(false);
            this.spacer.setCursor(horizonal ? Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) : Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            this.dx = this.dy = -1;
            this.horizonal = horizonal;

            /* because of jdk1.5, javafx */
            class SpacerMouseAdapter extends MouseAdapter implements MouseMotionListener {
                public void mouseReleased(MouseEvent e) {
                    Insets insets = getInsets();
                    int width = getWidth();
                    int height = getHeight();
                    int availw = width - insets.left - insets.right;
                    int availh = height - insets.top - insets.bottom;
                    if (CustomSplitPane.this.horizonal && dy != -1) {
                        setSplit((double) dx / availw);
                    } else if (dx != -1) {
                        setSplit((double) dy / availh);
                    }
                    dx = dy = -1;
                    spacer.setOpaque(false);
                    repaint();
                }

                public void mouseDragged(MouseEvent e) {
                    dx = e.getX() + spacer.getX();
                    dy = e.getY() + spacer.getY();
                    spacer.setOpaque(true);
                    if (dx != -1 && CustomSplitPane.this.horizonal) {
                        spacer.setBounds(dx, 0, SPACER_SIZE, getHeight());
                    } else if (dy != -1 && !CustomSplitPane.this.horizonal) {
                        spacer.setBounds(0, dy, getWidth(), SPACER_SIZE);
                    }
                    repaint();
                }

                public void mouseMoved(MouseEvent e) {
                }
            };
            SpacerMouseAdapter mouseAdapter = new SpacerMouseAdapter();
            spacer.addMouseListener(mouseAdapter);
            spacer.addMouseMotionListener(mouseAdapter);

            setLayout(new LayoutManager() {
                public void addLayoutComponent(String name, Component comp) {
                }

                public void removeLayoutComponent(Component comp) {
                }

                public Dimension minimumLayoutSize(Container parent) {
                    return new Dimension(1, 1);
                }

                public Dimension preferredLayoutSize(Container parent) {
                    return new Dimension(128, 128);
                }

                public void layoutContainer(Container parent) {
                    Insets insets = parent.getInsets();
                    int width = parent.getWidth();
                    int height = parent.getHeight();
                    int availw = width - insets.left - insets.right;
                    int availh = height - insets.top - insets.bottom;

                    if (CustomSplitPane.this.horizonal) {
                        availw -= SPACER_SIZE;
                        int width1 = Math.max(0, (int) Math.floor(split * availw));
                        int width2 = Math.max(0, availw - width1);
                        if (componentOne.isVisible() && !componentTwo.isVisible()) {
                            spacer.setBounds(0, 0, 0, 0);
                            componentOne.setBounds(insets.left, insets.top, availw, availh);
                        } else if (!componentOne.isVisible() && componentTwo.isVisible()) {
                            spacer.setBounds(0, 0, 0, 0);
                            componentTwo.setBounds(insets.left, insets.top, availw, availh);
                        } else {
                            spacer.setBounds(insets.left + width1, insets.top, SPACER_SIZE, availh);
                            componentOne.setBounds(insets.left, insets.top, width1, availh);
                            componentTwo.setBounds(insets.left + width1 + SPACER_SIZE, insets.top, width2, availh);
                        }
                    } else {
                        availh -= SPACER_SIZE;
                        int height1 = Math.max(0, (int) Math.floor(split * availh));
                        int height2 = Math.max(0, availh - height1);
                        if (componentOne.isVisible() && !componentTwo.isVisible()) {
                            spacer.setBounds(0, 0, 0, 0);
                            componentOne.setBounds(insets.left, insets.top, availw, availh);
                        } else if (!componentOne.isVisible() && componentTwo.isVisible()) {
                            spacer.setBounds(0, 0, 0, 0);
                            componentTwo.setBounds(insets.left, insets.top, availw, availh);
                        } else {
                            spacer.setBounds(insets.left, insets.top + height1, availw, SPACER_SIZE);
                            componentOne.setBounds(insets.left, insets.top, availw, height1);
                            componentTwo.setBounds(insets.left, insets.top + height1 + SPACER_SIZE, availw, height2);
                        }
                    }
                }
            });
            add(spacer);
        }

        public double getSplit() {
            return split;
        }

        public void setSplit(double split) {
            if (split < 0)
                split = 0;
            else if (split > 1)
                split = 1;
            this.split = split;
            invalidate();
            validate();
        }

        public void setComponentOne(Component component) {
            this.componentOne = component;
            if (componentOne != null)
                add(componentOne);
        }

        public void setComponentTwo(Component component) {
            this.componentTwo = component;
            if (componentTwo != null)
                add(componentTwo);
        }
    }

    private class DragListener extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
        private Point mouseCoords;
        private Point downCoords;
        private Point downPosition;
        private int downDot;
        private boolean hasMoved;

        public DragListener() {
            mouseCoords = new Point();
            downCoords = null;
            downPosition = null;
            magnifyRegion = null;
            downDot = -1;
        }

        public void mousePressed(MouseEvent e) {
            downCoords = e.getPoint();
            downPosition = getMapPosition();
            downDot = isOverDot(new Point(mouseCoords.x, mouseCoords.y));
            hasMoved = false;
            if (e.getButton() == MouseEvent.BUTTON1) {
                magnifyRegion = null;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                int cx = getCursorPosition().x;
                int cy = getCursorPosition().y;
                magnifyRegion = new Rectangle(cx - MAGNIFIER_SIZE / 2, cy - MAGNIFIER_SIZE / 2, MAGNIFIER_SIZE, MAGNIFIER_SIZE);
                repaint();
            }
            if(downDot != -1) waypointPanel.setSelectedWaypoint(downDot);
        }

        public void mouseDragged(MouseEvent e) {
            handlePosition(e);
            handleDrag(e);
            hasMoved = true;
        }

        private void handleDrag(MouseEvent e) {
            if (downDot != -1){
                Point.Double point = new Point.Double(position2lon(getCursorPosition().x, getZoom()),
                                        position2lat(getCursorPosition().y, getZoom()));
                context.waypoint.get(downDot).setLocation(point);
                repaint();
                waypointPanel.updateDisplay();
            } else if (downCoords != null) {
                int tx = downCoords.x - e.getX();
                int ty = downCoords.y - e.getY();
                setMapPosition(downPosition.x + tx, downPosition.y + ty);
                repaint();
            } else if (magnifyRegion != null) {
                int cx = getCursorPosition().x;
                int cy = getCursorPosition().y;
                magnifyRegion = new Rectangle(cx - MAGNIFIER_SIZE / 2, cy - MAGNIFIER_SIZE / 2, MAGNIFIER_SIZE, MAGNIFIER_SIZE);
                repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            handleDrag(e);
            if(downDot != -1){
                //context.w
                //context.waypoint.sendWaypoint((byte)downDot, Serial.ALTER_SUBTYPE);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {

                if(downDot == -1){
                    Point.Double point = new Point.Double(position2lon(getCursorPosition().x, getZoom()), position2lat(getCursorPosition().y, getZoom()));
                    int tmp = isOverLine(new Point(mouseCoords.x, mouseCoords.y));
                    if(tmp==-1) {
                        context.waypoint.add(new Dot(point));
                        waypointPanel.setSelectedWaypoint(context.waypoint.size()-1);
                    }
                    else{
                        context.waypoint.add(new Dot(point), tmp);
                        waypointPanel.setSelectedWaypoint(tmp);
                    }
                }

            } else if (e.getButton() == MouseEvent.BUTTON3) {
                if( downDot != -1 ) context.waypoint.remove(downDot);

            } else if (e.getButton() == MouseEvent.BUTTON2) {
                setCenterPosition(getCursorPosition());
                repaint();
            }
        }

        public void mouseMoved(MouseEvent e) {
            handlePosition(e);
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent me) {
            super.mouseEntered(me);
        }

        private void handlePosition(MouseEvent e) {
            mouseCoords = e.getPoint();
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotation = e.getWheelRotation();
            if (rotation < 0)
                zoomInAnimated(new Point(mouseCoords.x, mouseCoords.y));
            else
                zoomOutAnimated(new Point(mouseCoords.x, mouseCoords.y));
        }
    }

    private final class MapLayout implements LayoutManager {

        public void addLayoutComponent(String name, Component comp) {
        }
        public void removeLayoutComponent(Component comp) {
        }
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(1, 1);
        }
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        }
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
        }
    }
    //-------------------------------------------------------------------------
    // utils
    public static JPanel Contain(JPanel input){ //total hack
        JPanel tmp = new JPanel();
        tmp.add(input);
        tmp.setOpaque(false);
        return tmp;
    }

    public static String getTileString(TileServer tileServer, int xtile, int ytile, int zoom) {
        String number = ("" + zoom + "/" + xtile + "/" + ytile);
        String url = tileServer.getURL() + number + ".png";
        return url;
    }

    public static String format(double d) {
        return String.format("%.5f", d);
    }

    public static double getN(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return n;
    }

    public static double position2lon(int x, int z) {
        double xmax = TILE_SIZE * (1 << z);
        return x / xmax * 360.0 - 180;
    }

    public static double position2lat(int y, int z) {
        double ymax = TILE_SIZE * (1 << z);
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / ymax)));
    }

    public static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double tile2lat(int y, int z) {
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z))));
    }

    public static int lon2position(double lon, int z) {
        double xmax = TILE_SIZE * (1 << z);
        return (int) Math.floor((lon + 180) / 360 * xmax);
    }

    public static int lat2position(double lat, int z) {
        double ymax = TILE_SIZE * (1 << z);
        return (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * ymax);
    }

    public static String getTileNumber(TileServer tileServer, double lat, double lon, int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        return getTileString(tileServer, xtile, ytile, zoom);
    }

    private static void drawBackground(Graphics2D g, int width, int height) {
        Color color1 = Color.black;
        Color color2 = new Color(0x30, 0x30, 0x30);
        color1 = new Color(0xc0, 0xc0, 0xc0);
        color2 = new Color(0xe0, 0xe0, 0xe0);
        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));
        g.setPaint(new GradientPaint(0, 0, color1, 0, height, color2));
        g.fillRoundRect(0, 0, width, height, 4, 4);
        g.setComposite(oldComposite);
    }


}


