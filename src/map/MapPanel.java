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
import com.Context;
import com.layer.*;

import static com.map.WaypointList.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;

public class MapPanel extends JPanel implements CoordinateTransform {
    private static final int TILE_SIZE = 256;
    private static final float ZOOM_FACTOR = 1.1f;

    private Map<String,MapSource> mapSources;
    private String currentTileServerName = "";
    private MapSource currentTileServer = new TileServer(currentTileServerName);

    private int zoom;
    private Point2D mapPosition = new Point2D.Double(0, 0);

    private Context       context;
    private BorderLayout  border = new BorderLayout();
    private DragListener  mouseListener = new DragListener();
    private LayerManager  mll = new LayerManager();
    private WaypointPanel waypointPanel;
    private RoverPath     roverPath;

    private final Logger iolog = Logger.getLogger("d.io");

    public MapPanel(Context cxt) {
        this(cxt, new Point(0, 0), 6, null, null, null);
    }

    public MapPanel(Context cxt, Point mapPosition, int zoom) {
        this(cxt, mapPosition, zoom, null, null, null);
    }

    public MapPanel(Context cxt, Point mapPosition, int zoom, JPanel north,
                    JPanel east,
                    JPanel south) {
        context = cxt;
        context.getWaypointList().addListener(new WaypointListener(){
            @Override public void unusedEvent() { repaint(); }
        });

        mapSources = importMapSources(cxt);
        switchToServer(cxt.getResource("default_tile_server","satellite"));

        for(MapSource ms : mapSources.values()) ms.addRepaintListener(this);

        border.setVgap(-20);
        setOpaque(true);
        setBackground(new Color(0xc0, 0xc0, 0xc0));
        setLayout(border);

        waypointPanel = new WaypointPanel(context, this);
        JPanel west = contain(waypointPanel);
        east = contain(east);

        if(south == null) south = new JPanel();
        add(south);
        south.setLayout(new BorderLayout());
        south.add(west,  BorderLayout.WEST);
        south.add(east,  BorderLayout.EAST);
        south.add(north, BorderLayout.CENTER);

        setZoom(TILE_SIZE * (1 << zoom));
        setMapPosCoords(mapPosition);

        roverPath = new RoverPath(context, this, context.getWaypointList(), this);
        mll.add(roverPath);
        mll.add(mouseListener);
        addMouseWheelListener(mouseListener);
        addMouseListener(mll);
        addMouseMotionListener(mll);

        this.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                repaint();
            }
        });
    }

    /**
     * enable/disable the user's ability to change or add waypoints using
     * the map's RoverPath
     */
    public void enablePathModifications(boolean value){
        roverPath.setWaypointsEnabled(value);
    }
    //Code for CoordinateTransform interface
    /**
     * Transforms a (lonitude,latitude) point to absolute (x,y) pixels
     * Will return an instance of the same class as the argument p
     */
    public Point2D toPixels(Point2D p) {
        Point2D f = (Point2D) p.clone();
        double scale = zoom;
        double lon   = p.getX();
        double lat   = Math.toRadians(p.getY());
        double x = (lon+180.0)/360.0 * scale;
        double y = ((1 -
                     Math.log(
                         Math.tan(lat) + 1 / Math.cos(lat)
                     ) / Math.PI
                    )/2) * scale;
        f.setLocation(x,y);
        return f;
    }
    /**
     * Transforms absolute (x,y) pixels to (lonitude,latitude)
     * Will return an instance of the same class as the argument p
     */
    public Point2D toCoordinates(Point2D p) {
        Point2D f = (Point2D) p.clone();
        double scale = zoom;
        double x     = p.getX() / scale;
        double y     = ((p.getY()/scale)*2);
        double lon   = x * 360 - 180;
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
    public Point2D screenPosition(Point2D p) {
        Point2D f = (Point2D) p.clone();
        Point2D click  = toPixels(p);
        Point2D center = getMapPosPixels();
        f.setLocation(click.getX() - center.getX() +  getWidth()/2.0,
                      click.getY() - center.getY() + getHeight()/2.0 );
        return f;
    }
    /**
     * Transforms pixel position relative current screen to absolute (lon,lat)
     */
    public Point2D mapPosition(Point2D p) {
        Point2D f = (Point2D) p.clone();
        Point2D center = getMapPosPixels();
        f.setLocation(p.getX() + center.getX() -  getWidth()/2.0,
                      p.getY() + center.getY() - getHeight()/2.0);
        return toCoordinates(f);
    }
    //End Code for CoordinateTransform interface

    public Point2D getMapPosPixels() {
        return (Point2D) mapPosition.clone();
    }

    public Point2D getMapPosCoords() {
        return (Point2D) toCoordinates(mapPosition);
    }

    public void setMapPosPixels(Point2D pos) {
        this.mapPosition = (Point2D) pos.clone();
    }

    public void setMapPosCoords(Point2D pos) {
        this.mapPosition = (Point2D) toPixels(pos);
    }

    public int getZoom() {
        return zoom;
    }
    /**
     * Returns true if the new zoom is valid and the view has been changed
     */
    public boolean setZoom(int zoom) {
        boolean valid = currentTileServer.isValidZoom(zoom);
        if(valid) this.zoom = zoom;
        return valid;
    }

    public void zoomIn(Point pivot) {
        Point2D startLoc = getMapPosPixels();
        boolean success = setZoom((int)(getZoom()*ZOOM_FACTOR));
        if(!success) return;
        double dx = (pivot.x- getWidth()/2);
        double dy = (pivot.y-getHeight()/2);
        Point2D endLoc = new Point2D.Double(
            startLoc.getX()*ZOOM_FACTOR + dx*(ZOOM_FACTOR-1.0),
            startLoc.getY()*ZOOM_FACTOR + dy*(ZOOM_FACTOR-1.0) );
        setMapPosPixels(endLoc);
        repaint();
    }

    public void zoomOut(Point pivot) {
        Point2D startLoc = getMapPosPixels();
        boolean success = setZoom((int)(getZoom()/ZOOM_FACTOR));
        if(!success) return;
        double dx = (pivot.x- getWidth()/2);
        double dy = (pivot.y-getHeight()/2);
        Point2D endLoc = new Point2D.Double(
            startLoc.getX()/ZOOM_FACTOR + dx*(1.0/ZOOM_FACTOR-1.0),
            startLoc.getY()/ZOOM_FACTOR + dy*(1.0/ZOOM_FACTOR-1.0) );
        setMapPosPixels(endLoc);
        repaint();
    }

    /** Return a list of tile server names with the active server listed last */
    public java.util.List<String> tileServerNames() {
        java.util.List<String> list = new LinkedList<String>(mapSources.keySet());
        list.remove(currentTileServerName);
        list.add(currentTileServerName);
        return list;
    }

    public void switchToServer(String serverName) {
        if(mapSources.containsKey(serverName)){
            currentTileServer.clear();
            currentTileServerName = serverName;
            currentTileServer = mapSources.get(serverName);
        } else {
            iolog.severe("No Tile Server Sources found for name "+serverName);
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
            currentTileServer.paint(g,
                            getMapPosPixels(),
                            zoom,
                            getWidth(),
                            getHeight() );
            mll.draw(g);
        } finally {
            g.dispose();
        }
    }

    // Load mapsources from property file specified
    private Map<String,MapSource> importMapSources(Context ctx){
        String sourceFile = ctx.getResource("tile_server_list");
        if(sourceFile == null){
            iolog.severe("Couldn't find tile server list resource path");
        }

        Map<String, MapSource> sources = new HashMap<String,MapSource>();
        ResourceBundle rb = ctx.loadResourceBundle(sourceFile);
        for(String key : rb.keySet()){
            sources.put(key, new TileServer(rb.getString(key)));
        }

        return sources;
    }

    private class DragListener implements Layer, MouseWheelListener {
        private Point downCoords = null;
        private Point2D downPosition = null;

        public int getZ() {
            return -1;
        }

        public boolean onClick(MouseEvent e) {
            return false;
        }

        public boolean onPress(MouseEvent e) {
            downCoords   = e.getPoint();
            downPosition = getMapPosPixels();
            return true;
        }

        public void onDrag(MouseEvent e) {
            handleDrag(e);
        }

        private void handleDrag(MouseEvent e) {
            if (downCoords != null) {
                int    dx = downCoords.x - e.getX();
                int    dy = downCoords.y - e.getY();
                double nx = downPosition.getX() + dx;
                double ny = downPosition.getY() + dy;
                setMapPosPixels(new Point2D.Double(nx, ny));
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
                zoomIn(new Point(mouseCoords.x, mouseCoords.y));
            else
                zoomOut(new Point(mouseCoords.x, mouseCoords.y));
        }

        public void paint(Graphics g) {
        }
    }

    public static JPanel contain(JPanel input) {
        JPanel tmp = new JPanel();
        tmp.add(input);
        tmp.setOpaque(false);
        return tmp;
    }
}


