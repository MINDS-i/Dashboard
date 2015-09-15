/*******************************************************************************
 * The source code for this class
 * is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This code is based on the MapPanel source code written by Stepan Rutz
 * found at: http://mappanel.sourceforge.net/
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
import com.ui.TelemetryListener;
import com.layer.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;
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

public class MapPanel extends JPanel implements ContextViewer, CoordinateTransform {
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
    private JPanel centerPanel = new JPanel();

    protected double smoothScale = 1.0D;
    private boolean useAnimations = false;
    private int smoothOffset = 0;
    private Point smoothPosition, smoothPivot;

    private LayerManager mll = new LayerManager();

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

        border.setVgap(-20);
        setOpaque(true);
        setBackground(new Color(0xc0, 0xc0, 0xc0));
        setLayout(border);

        waypointPanel = new WaypointPanel(context, this);
        JPanel west = contain(waypointPanel);
        east = contain(east);

        add(south);
        south.setLayout(new BorderLayout());
        south.add(west,  BorderLayout.WEST);
        south.add(east,  BorderLayout.EAST);
        south.add(north, BorderLayout.CENTER);

        setZoom(zoom);
        setMapPosition(mapPosition);

        mll.add(new RoverPath(context, this, waypointPanel));
        mll.add(mouseListener);
        addMouseWheelListener(mouseListener);
        addMouseListener(mll);
        addMouseMotionListener(mll);
    }

    //Code for CoordinateTransform interface
    /**
     * Transforms a (lonitude,latitude) point to absolute (x,y) pixels
     * Will return an instance of the same class as the argument p
     */
    public Point2D toPixels(Point2D p){
        Point2D f = (Point2D) p.clone();
        double scale = TILE_SIZE * (1 << (getZoom()-1));
        double lon   = Math.toRadians(p.getX());
        double lat   = Math.toRadians(p.getY());
        double x = ((lon + Math.PI) / Math.PI) * scale;
        double y = (1 -
                       Math.log(
                           Math.tan(lat) + 1 / Math.cos(lat)
                       ) / Math.PI
                   ) * scale;
        f.setLocation(x,y);
        return f;
    }
    /**
     * Transforms absolute (x,y) pixels to (lonitude,latitude)
     * Will return an instance of the same class as the argument p
     */
    public Point2D toCoordinates(Point2D p){
        Point2D f = (Point2D) p.clone();
        double scale = TILE_SIZE * (1 << (getZoom()-1));
        double x     = p.getX() / scale;
        double y     = p.getY() / scale;
        double lon   = x * 180 - 180;
        double lat   = Math.toDegrees(
                           Math.atan(
                               Math.sinh(
                                   Math.PI * (1 - y)
                               )
                           )
                       );
        f.setLocation(lon,lat);
        return f;
    }
    /**
     * Transforms absolute (lon,lat) to the pixel position in the current screen
     */
    public Point2D screenPosition(Point2D p){
        Point2D absPix = toPixels(p);
        Point2D f = (Point2D) p.clone();
        f.setLocation(absPix.getX() - mapPosition.x, absPix.getY() - mapPosition.y);
        return f;
    }
    /**
     * Transforms pixel position relative current screen to absolute (lon,lat)
     */
    public Point2D mapPosition(Point2D p){
        Point2D f = (Point2D) p.clone();
        f.setLocation(p.getX() + mapPosition.x, p.getY() + mapPosition.y);
        return toCoordinates(f);
    }
    //End Code for CoordinateTransform interface

    //Code for ContextViewer interface
    public void waypointUpdate(){
        repaint();
    }
    //End code for ContextViewep interface

//TileServer code
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
    public TileCache getCache() {
        return cache;
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

    public static String getTileString(TileServer tileServer, int xtile, int ytile, int zoom) {
        String number = ("" + zoom + "/" + xtile + "/" + ytile);
        String url = tileServer.getURL() + number + ".png";
        return url;
    }

    public static String getTileNumber(TileServer tileServer, double lat, double lon, int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        return getTileString(tileServer, xtile, ytile, zoom);
    }
//end tileserver code

    public void setUseAnimations(boolean useAnimations) {
        this.useAnimations = useAnimations;
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

    public Point getTile(Point position) {
        return new Point((int) Math.floor(((double) position.x) / TILE_SIZE),(int) Math.floor(((double) position.y) / TILE_SIZE));
    }

    public Point getCenterPosition() {
        return new Point(mapPosition.x + getWidth() / 2, mapPosition.y + getHeight() / 2);
    }

    public void setCenterPosition(Point p) {
        setMapPosition(p.x - getWidth() / 2, p.y - getHeight() / 2);
    }

    //--------------------------------------------------------------------------
    //Painting functions

    private void paintInternal(Graphics2D g) {
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

        long t1 = System.currentTimeMillis();
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

        //TEMPORARY
        g = (Graphics2D) gOrig.create();
        //g.translate(-mapPosition.x, -mapPosition.y);
        mll.draw(g);
        g.dispose();
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

    private class DragListener implements Layer, MouseWheelListener {
        private Point downCoords = null;
        private Point downPosition = null;

        public int getZ(){
            return -1;
        }

        public boolean onClick(MouseEvent e){
            return false;
        }

        public boolean onPress(MouseEvent e) {
            downCoords = e.getPoint();
            downPosition = getMapPosition();
            return true;
        }

        public void onDrag(MouseEvent e) {
            handleDrag(e);
        }

        private void handleDrag(MouseEvent e) {
            if (downCoords != null) {
                int tx = downCoords.x - e.getX();
                int ty = downCoords.y - e.getY();
                setMapPosition(downPosition.x + tx, downPosition.y + ty);
                repaint();
            }
        }

        public void onRelease(MouseEvent e) {
            handleDrag(e);
            downCoords = null;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotation = e.getWheelRotation();
            Point mouseCoords = e.getPoint();
            if (rotation < 0)
                zoomInAnimated(new Point(mouseCoords.x, mouseCoords.y));
            else
                zoomOutAnimated(new Point(mouseCoords.x, mouseCoords.y));
        }

        public void paint(Graphics g){

        }
    }

    public static JPanel contain(JPanel input){ //total hack
        JPanel tmp = new JPanel();
        tmp.add(input);
        tmp.setOpaque(false);
        return tmp;
    }
}


