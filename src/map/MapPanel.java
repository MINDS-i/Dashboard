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
import com.ContextViewer;
import com.Context;
import com.layer.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javax.imageio.*;
import javax.swing.*;

public class MapPanel extends JPanel implements ContextViewer, CoordinateTransform {
    private static final int TILE_SIZE = 256;

    private MapSource mapSource = new TileServer("http://otile1.mqcdn.com/tiles/1.0.0/sat");

    private int zoom;
    private Point2D mapPosition = new Point2D.Double(0, 0);

    private Context       context;
    private BorderLayout  border = new BorderLayout();
    private DragListener  mouseListener = new DragListener();
    private LayerManager  mll = new LayerManager();
    private WaypointPanel waypointPanel;

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

        mapSource.addRepaintListener(this);

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
        //setMapPosition(mapPosition);

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
        double scale = zoom;
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
        double scale = zoom;
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
        Point2D pPix = toPixels(p);
        Point2D cPix = toPixels(mapPosition);
        Point2D f = (Point2D) p.clone();
        f.setLocation(pPix.getX() - cPix.getX() +  getWidth()/2,
                      pPix.getY() - cPix.getY() + getHeight()/2 );
        return f;
    }
    /**
     * Transforms pixel position relative current screen to absolute (lon,lat)
     */
    public Point2D mapPosition(Point2D p){
        Point2D f = (Point2D) p.clone();
        //f.setLocation(p.getX() + mapPosition.x, p.getY() + mapPosition.y);
        return toCoordinates(f);
    }
    //End Code for CoordinateTransform interface

    //Code for ContextViewer interface
    public void waypointUpdate(){
        repaint();
    }
    //End code for ContextViewep interface

    //TileServer code

    public void nextTileServer() {
    }

    public Point2D getMapPosition() {
        return (Point2D) mapPosition.clone();
    }

    public void setMapPosition(Point2D mapPosition) {
        this.mapPosition = mapPosition;
    }

    public void setMapPosition(double x, double y) {
        setMapPosition(new Point2D.Double(x,y));
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (zoom == this.zoom)
            return;
        int oldZoom = this.zoom;
        this.zoom = zoom;
        System.out.println("Zoom set to "+zoom);
        //this.zoom = Math.min(getTileServer().getMaxZoom(), zoom);
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

    public void zoomIn(Point pivot) {
        /*if (getZoom() >= getTileServer().getMaxZoom())
            return;*/
        int dx = pivot.x;
        int dy = pivot.y;
        setZoom((getZoom()*12)/11);
        //Point mapPosition = getMapPosition();
        //setMapPosition(mapPosition.x * 2 + dx, mapPosition.y * 2 + dy);
        repaint();
    }

    public void zoomOut(Point pivot) {
        if (getZoom() <= 2*TILE_SIZE)//screen width
            return;
        int dx = pivot.x;
        int dy = pivot.y;
        setZoom((getZoom()*11)/12);
        //Point mapPosition = getMapPosition();
        //setMapPosition((mapPosition.x - dx) / 2, (mapPosition.y - dy) / 2);
        repaint();
    }

    public int getXTileCount() {
        return zoom/TILE_SIZE;
    }

    public int getYTileCount() {
        return zoom/TILE_SIZE;
    }

    public int getXMax() {
        return zoom;
    }

    public int getYMax() {
        return zoom;
    }

    public Point getTile(Point position) {
        return new Point((int) Math.floor(((double) position.x) / TILE_SIZE),(int) Math.floor(((double) position.y) / TILE_SIZE));
    }

    @Override
    public void paintComponent(Graphics gOrig) {
        super.paintComponent(gOrig);
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
            mapSource.paint(g,
                            mapPosition,
                            zoom,
                            getWidth(),
                            getHeight() );
            mll.draw(g);
        } finally {
            g.dispose();
        }
    }

    private class DragListener implements Layer, MouseWheelListener {
        private Point downCoords = null;
        private Point2D downPosition = null;

        public int getZ(){
            return -1;
        }

        public boolean onClick(MouseEvent e){
            return false;
        }

        public boolean onPress(MouseEvent e) {
            downCoords = e.getPoint();
            downPosition = toPixels(getMapPosition());
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
                double ny = downPosition.getY() - dy;
                Point2D l = toCoordinates(new Point2D.Double(nx, ny));
                setMapPosition(l);
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

        public void paint(Graphics g){

        }
    }

    public static JPanel contain(JPanel input){
        JPanel tmp = new JPanel();
        tmp.add(input);
        tmp.setOpaque(false);
        return tmp;
    }
}


