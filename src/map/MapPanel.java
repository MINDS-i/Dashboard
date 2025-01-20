/*
 * The source code for this class
 * is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This code is based on the MapPanel source code written by Stepan Rutz
 * found at: http://mappanel.sourceforge.net/
 */

package com.map;

import com.Context;
import com.layer.Layer;
import com.layer.LayerManager;
import com.map.command.CommandManager;
import com.map.geofence.WaypointGeofence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;

import static com.map.WaypointList.WaypointListener;

public class MapPanel extends JPanel implements CoordinateTransform {
    private static final int TILE_SIZE = 256;
    private static final float ZOOM_FACTOR = 1.1f;
    public static final double SEED_CACHE_RADIUS_KM = 2.0;

    private final Map<String, MapSource> mapSources;
    private final Context context;
    private final LayerManager mll = new LayerManager();
    private final Logger iolog = LoggerFactory.getLogger("d.io");
    public WaypointPanel waypointPanel;
    public RoverPath roverPath;
    private String currentTileServerName = "";
    private MapSource currentTileServer;
    private int zoom;
    private Point2D mapPosition = new Point2D.Double(0, 0);

    public MapPanel(Context cxt) {
        this(cxt, new Point2D.Double(0.0, 0.0), 6, null, null, null);
    }

    public MapPanel(Context cxt, Point2D mapPosition, int zoom) {
        this(cxt, mapPosition, zoom, null, null, null);
    }

    public MapPanel(Context cxt, Point2D mapPosition, int zoom, JPanel north,
                    JPanel east,
                    JPanel south) {
        context = cxt;
        currentTileServer = new TileServer(currentTileServerName, cxt);
        context.getWaypointList().addListener(new WaypointListener() {
            @Override
            public void unusedEvent() {
                repaint();
            }
        });

        mapSources = importMapSources(cxt);
        switchToServer(cxt.getResource("default_tile_server", "satellite"));

        for (MapSource ms : mapSources.values()) {
            ms.addRepaintListener(this);
        }

        BorderLayout border = new BorderLayout();
        border.setVgap(-20);
        setOpaque(true);
        setBackground(new Color(0xc0, 0xc0, 0xc0));
        setLayout(border);

        waypointPanel = new WaypointPanel(context, this);
        JPanel west = contain(waypointPanel);
        east = contain(east);

        if (south == null) {
            south = new JPanel();
        }
        add(south);
        south.setLayout(new BorderLayout());
        south.add(west, BorderLayout.WEST);
        south.add(east, BorderLayout.EAST);
        south.add(north, BorderLayout.CENTER);

        CommandManager.getInstance().initGeofence(
                WaypointGeofence.MIN_RADIUS_FT,
                WaypointGeofence.FenceType.CIRCLE, this);

        setZoom(TILE_SIZE * (1 << zoom));
        setMapPosCoords(mapPosition);

        roverPath = new RoverPath(context, this, context.getWaypointList(),
                this, this);
        mll.add(roverPath);
        DragListener mouseListener = new DragListener();
        mll.add(mouseListener);
        addMouseWheelListener(mouseListener);
        addMouseListener(mll);
        addMouseMotionListener(mll);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });

    }

    public static JPanel contain(JPanel input) {
        JPanel tmp = new JPanel();
        tmp.add(input);
        tmp.setOpaque(false);
        return tmp;
    }

    //Code for CoordinateTransform interface

    /**
     * enable/disable the user's ability to change or add waypoints using
     * the map's RoverPath
     */
    public void enablePathModifications(boolean value) {
        roverPath.setWaypointsEnabled(value);
    }

    /**
     * Transforms a (longitude, latitude) point to absolute (x, y) pixels at the current zoom level.
     * Will return an instance of the same class as the argument p
     */
    public Point2D toPixels(Point2D p) {
        return toPixels(p, zoom);
    }

    /**
     * Transforms a (longitude, latitude) point to absolute (x, y) pixels at a specific scale.
     * Will return an instance of the same class as the argument p
     */
    public static Point2D toPixels(Point2D p, double scale) {
        Point2D f = (Point2D) p.clone();
        double lon = p.getX();
        double lat = Math.toRadians(p.getY());
        double x = ((lon + 180.0) / 360.0) * scale;
        double y = ((1 - Math.log(Math.tan(lat) + 1 / Math.cos(lat)) / Math.PI) / 2) * scale;

        f.setLocation(x, y);
        return f;
    }

    /**
     * Transforms absolute (x,y) pixels to (longitude,latitude) at the current zoom level.
     * Will return an instance of the same class as the argument p
     */
    public Point2D toCoordinates(Point2D p) {
        return toCoordinates(p, zoom);
    }

    /**
     * Transforms absolute (x,y) pixels to (longitude,latitude) at a specific scale.
     * Will return an instance of the same class as the argument p
     */
    public static Point2D toCoordinates(Point2D p, double scale) {
        Point2D f = (Point2D) p.clone();
        double x = p.getX() / scale;
        double y = ((p.getY() / scale) * 2);
        double lon = x * 360.0 - 180.0;
        double lat = Math.toDegrees(
                Math.atan(
                        Math.sinh(
                                Math.PI * (1.0 - y)
                        )
                )
        );
        f.setLocation(lon, lat);
        return f;
    }

    /**
     * Transforms absolute (lon,lat) to the pixel position in the current screen
     */
    public Point2D screenPosition(Point2D p) {
        Point2D f = (Point2D) p.clone();
        Point2D click = toPixels(p);
        Point2D center = getMapPosPixels();
        f.setLocation(click.getX() - center.getX() + getWidth() / 2.0,
                click.getY() - center.getY() + getHeight() / 2.0);
        return f;
    }
    //End Code for CoordinateTransform interface

    /**
     * Transforms pixel position relative current screen to absolute (lon,lat)
     */
    public Point2D mapPosition(Point2D p) {
        Point2D f = (Point2D) p.clone();
        Point2D center = getMapPosPixels();
        f.setLocation(p.getX() + center.getX() - (getWidth() / 2.0),
                p.getY() + center.getY() - (getHeight() / 2.0));
        return toCoordinates(f);
    }

    public Point2D getMapPosPixels() {
        return (Point2D) mapPosition.clone();
    }

    public void setMapPosPixels(Point2D pos) {
        this.mapPosition = (Point2D) pos.clone();
    }

    public Point2D getMapPosCoords() {
        return toCoordinates(mapPosition);
    }

    public void setMapPosCoords(Point2D pos) {
        this.mapPosition = toPixels(pos);
    }

    public int getZoom() {
        return zoom;
    }

    public boolean setZoom(int zoom) {
        boolean valid = currentTileServer.isValidZoom(zoom);
        if (valid) {
            this.zoom = zoom;
        }

        return valid;
    }

    public boolean zoomIn(Point pivot) {
        Point2D startLoc = getMapPosPixels();
        boolean success = setZoom((int) (getZoom() * ZOOM_FACTOR));
        if (!success) {
            return false;
        }
        double dx = (pivot.x - getWidth() / 2.0);
        double dy = (pivot.y - getHeight() / 2.0);
        Point2D endLoc = new Point2D.Double(
                startLoc.getX() * ZOOM_FACTOR + dx * (ZOOM_FACTOR - 1.0),
                startLoc.getY() * ZOOM_FACTOR + dy * (ZOOM_FACTOR - 1.0));
        setMapPosPixels(endLoc);
        repaint();
        return true;
    }

    public void zoomOut(Point pivot) {
        Point2D startLoc = getMapPosPixels();
        boolean success = setZoom((int) (getZoom() / ZOOM_FACTOR));
        if (!success) {
            return;
        }
        double dx = (pivot.x - getWidth() / 2.0);
        double dy = (pivot.y - getHeight() / 2.0);
        Point2D endLoc = new Point2D.Double(
                startLoc.getX() / ZOOM_FACTOR + dx * (1.0 / ZOOM_FACTOR - 1.0),
                startLoc.getY() / ZOOM_FACTOR + dy * (1.0 / ZOOM_FACTOR - 1.0));
        setMapPosPixels(endLoc);
        repaint();
    }

    public void zoomFull(Point pivot) {

        while (zoomIn(new Point(getWidth() / 2, getHeight() / 2))) {
        }
        setMapPosCoords(new Point2D.Double(context.getHomeProp().getY(),
                context.getHomeProp().getX()));
        repaint();
    }

    /**
     * Return a list of tile server names with the active server listed last
     */
    public java.util.List<String> tileServerNames() {
        java.util.List<String> list = new LinkedList<>(mapSources.keySet());
        list.remove(currentTileServerName);
        list.add(currentTileServerName);
        return list;
    }

    public void switchToServer(String serverName) {
        if (mapSources.containsKey(serverName)) {
            currentTileServer.clear();
            currentTileServerName = serverName;
            currentTileServer = mapSources.get(serverName);
        }
        else {
            iolog.error("No Tile Server Sources found for name {}", serverName);
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
                    getHeight());
            mll.draw(g);
        }
        finally {
            g.dispose();
        }
    }

    // Load mapsources from property file specified
    private Map<String, MapSource> importMapSources(Context ctx) {
        String sourceFile = ctx.getResource("tile_server_list");
        if (sourceFile == null) {
            iolog.error("Couldn't find tile server list resource path");
        }

        Map<String, MapSource> sources = new HashMap<>();
        ResourceBundle rb = ctx.loadResourceBundle(sourceFile);
        for (String key : rb.keySet()) {
            sources.put(key, new TileServer(rb.getString(key), this.context));
        }

        return sources;
    }

    /**
     * Instructs all map sources to begin pre-loading tiles into the filesystem cache.
     * @param callback A callback that will be sent updates as the caching progresses.
     */
    public void seedTileCache(TileLoadingCallback callback) {
        mapSources.values().forEach(source -> source.preloadTiles(
                toCoordinates(mapPosition),
                SEED_CACHE_RADIUS_KM,
                callback
        ));
    }

    /**
     * If map sources are currently preloading tiles, this will stop them.
     */
    public void stopSeeding() {
        mapSources.values().forEach(MapSource::stopPreloading);
    }

    /**
     * Name: Class - DragListener
     * Desc: Handles mouse movement events (Click and Drag, Zoom by scrollwheel) for the map.
     */
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
            downCoords = e.getPoint();
            downPosition = getMapPosPixels();
            return true;
        }

        public void onDrag(MouseEvent e) {
            handleDrag(e);
        }

        private void handleDrag(MouseEvent e) {
            if (downCoords != null) {
                int dx = downCoords.x - e.getX();
                int dy = downCoords.y - e.getY();
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
            if (rotation < 0) {
                zoomIn(new Point(mouseCoords.x, mouseCoords.y));
            }
            else {
                zoomOut(new Point(mouseCoords.x, mouseCoords.y));
            }
        }

        public void paint(Graphics g) {
        }
    }
}


