package com.map;

import static com.map.WaypointList.*;
import com.Context;
import com.serial.Serial;
import com.telemetry.TelemetryListener;
import com.layer.Layer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;


class RoverPath implements Layer {
    private static final Color ACTIVE_LINE_FILL = new Color(1.f,1.f,0.f,1f);
    private static final Color PATH_LINE_FILL   = new Color(0f,0f,0f, 1f);
    private static final Color LINE_BORDER      = new Color(.5f,.5f,.5f,0f);
    private static final int   LINE_WIDTH       = 10;

    private Context context;
    private CoordinateTransform mapTransform;
    private WaypointList waypoints;
    private Dot rover = new Dot();
    private Component painter;
    private boolean waypointsDisabled = false;

    RoverPath(Context c, CoordinateTransform cT, WaypointList waypoints, Component painter) {
        context = c;
        mapTransform = cT;
        this.waypoints = waypoints;
        this.painter = painter;

        waypointsDisabled = !Boolean.valueOf(
            context.getResource("waypoints_enabled", "true")
        );
    }

    public int getZ() {
        return 1;
    }

    /**
     * Enable creating and modifying the waypoint list. Defaults to true.
     */
    public void setWaypointsEnabled(boolean value){
        waypointsDisabled = !value;
    }

    public void paint(Graphics g) {
        paintDots(g);
    }

    public boolean onClick(MouseEvent e) {
        if(waypointsDisabled) return false;

        Point2D pixel = toP2D(e.getPoint());
        int underneith = isOverDot(pixel, context.theme.waypointImage);

        if((e.getButton() == MouseEvent.BUTTON1) && (underneith == Integer.MAX_VALUE)) {
            //left click thats not over a dot
            Point2D point = mapTransform.mapPosition(pixel);

            int line = isOverLine(e.getPoint());
            if (line==Integer.MAX_VALUE) {
                //click is not over an existing line
                waypoints.add(new Dot(point), waypoints.size());
                waypoints.setSelected(waypoints.size() - 1);
            } else {
                //click is over an existing line
                waypoints.add(new Dot(point), line);
                waypoints.setSelected(line);
                if(line == 0){
                    // When the rover is targeting point 0, an additional line=0
                    // can occur representing the line from the rover to the
                    // first waypoint. If a new dot is added in this location,
                    // the new dot should make itself the new target of the
                    // rover so the user can quickly add a new waypoint to
                    // the front of the line.
                    waypoints.setTarget(line);
                }
            }
            return true;
        } else if ((e.getButton() == MouseEvent.BUTTON3) && (underneith != Integer.MAX_VALUE)) {
            //right click on top of a point
            waypoints.remove(underneith);
            return true;
        }
        return false;
    }

    private Dot draggedDot = null;
    private int draggedDotIdx = Integer.MAX_VALUE;
    private int downDot = Integer.MAX_VALUE;

    public boolean onPress(MouseEvent e) {
        Point pixel = e.getPoint();
        downDot = isOverDot(pixel, context.theme.waypointImage);
        if(downDot != Integer.MAX_VALUE) {
            waypoints.setSelected(downDot);
            if(downDot < 0 || waypointsDisabled) {
                // Disable dragging for non-waypoint line dots
                downDot = Integer.MAX_VALUE;
            } else {
                draggedDot = waypoints.get(downDot).dot();
            }
            return true;
        }
        return false;
    }

    public void onDrag(MouseEvent e) {
        Point2D pixel = toP2D(e.getPoint());
        if (downDot != Integer.MAX_VALUE) {
            Point2D finalLoc = mapTransform.mapPosition(pixel);
            draggedDotIdx = downDot;
            draggedDot.setLocation(finalLoc);
            painter.repaint();
        }
    }

    public void onRelease(MouseEvent e) {
        Point pixel = e.getPoint();
        if(draggedDotIdx != Integer.MAX_VALUE) {
            waypoints.set(draggedDot, draggedDotIdx);
            draggedDotIdx = Integer.MAX_VALUE;
        }
    }

    public void paintLine(Graphics g, Point2D pointA, Point2D pointB, Color fill) {
        Graphics2D g2d = (Graphics2D) g.create();
        RenderingHints hints = g2d.getRenderingHints();
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(hints);
        try {
            final double dx = pointA.getX() - pointB.getX();
            final double dy = pointA.getY() - pointB.getY();
            final int length = (int) Math.sqrt(dx*dx + dy*dy);

            g2d.translate((int)pointB.getX(), (int)pointB.getY());
            g2d.rotate(Math.atan2(dy, dx));
            g2d.setPaint(new GradientPaint(0, 0, fill,
                                           0,
                                           -(LINE_WIDTH/2),
                                           LINE_BORDER,
                                           true));
            g2d.fillRect(0, -(LINE_WIDTH/2), length, LINE_WIDTH);
        } finally {
            g2d.dispose();
        }
    }

    private void paintDots(Graphics g) {
        drawLines(g);
        drowRoverLine(g);
        drawPoints(g);
    }

    private Point2D drawnLocation(int index){
        Dot d = null;
        if(index == draggedDotIdx){
            d = draggedDot;
        } else {
            d = waypoints.get(index).dot();
        }
        return mapTransform.screenPosition(d.getLocation());
    }

    private Point2D roverLocation(){
        return mapTransform.screenPosition(waypoints.getRover().getLocation());
    }

    private void drawLines(Graphics g) {
        if(waypoints.size() < 2) return;

        Point n = null;
        Point l = null;
        for(int i=0; i<waypoints.size(); i++){
            n = toPoint(drawnLocation(i));
            if(l!=null) paintLine(g, n, l, PATH_LINE_FILL);
            l = n;
        }

        if(waypoints.getLooped()) {
            n = toPoint(drawnLocation(0));
            paintLine(g, n, l, PATH_LINE_FILL);
        }
    }

    private void drawPoints(Graphics g) {
        Point tmp;

        for(int i=waypoints.extendedIndexStart(); i<waypoints.size(); i++){
            tmp = toPoint(drawnLocation(i));
            ExtendedWaypoint w = waypoints.get(i);
            BufferedImage img = context.theme.waypointImage;
            switch(w.type()){
                case HOME: img = context.theme.homeIcon; break;
                case ROVER: img = context.theme.roverImage; break;
                case SELECTED: img = context.theme.waypointSelected; break;
            }
            drawImg(g, img, tmp);
        }
    }

    private void drowRoverLine(Graphics g) {
        if(waypoints.getTarget() >= waypoints.size()) {
            return;
        }
        Point n = toPoint(roverLocation());
        Point l = toPoint(drawnLocation(waypoints.getTarget()));
        paintLine(g, n, l, ACTIVE_LINE_FILL);
    }

    private void drawImg(Graphics g, BufferedImage img, Point2D loc) {
        g.translate(-img.getWidth()/2, -img.getHeight()/2);
        g.drawImage( img , (int)loc.getX(), (int)loc.getY(), null);
        g.translate( img.getWidth()/2, img.getHeight()/2);
    }

    public int isOverDot(Point2D click, BufferedImage image) {
        for(int i=waypoints.extendedIndexStart(); i<waypoints.size(); i++) {
            Dot d = waypoints.get(i).dot();
            Point2D loc = mapTransform.screenPosition(d.getLocation());
            if(Math.abs(click.getX()-loc.getX()-1) > image.getWidth() /2.0) continue;
            if(Math.abs(click.getY()-loc.getY()-1) > image.getHeight()/2.0) continue;
            return i;
        }
        return Integer.MAX_VALUE;
    }

    public int isOverLine(Point p) {
        if(waypoints.size() <= 0) return Integer.MAX_VALUE;

        int index;
        Point prevPoint;

        if(waypoints.getTarget() == 0){
            index = 0;
            prevPoint = toPoint(roverLocation());
        } else {
            index = 1;
            prevPoint = toPoint(drawnLocation(0));
        }

        for(; index<waypoints.size(); index++) {
            Point thisPoint = toPoint(drawnLocation(index));
            if( distFromLine(prevPoint, thisPoint, p) < (LINE_WIDTH/2) ) {
                return index;
            }
            prevPoint = thisPoint;
        }

        return Integer.MAX_VALUE;
    }

    public int distFromLine(Point a, Point b, Point idp) {
        float abSlope = (a.y-b.y)/(a.x-b.x+.0000001f);
        float abYCept = a.y - abSlope*a.x;
        float perpSlope = (a.x-b.x)/(b.y-a.y+.0000001f);
        float perpYCept = idp.y - perpSlope*idp.x;
        float interceptX = (abYCept - perpYCept) / (perpSlope-abSlope+.0000001f);
        float interceptY = perpSlope*interceptX + perpYCept;
        if( a.x > b.x && a.x > interceptX && interceptX > b.x) {
            return (int) Math.floor(
                       Math.sqrt( (interceptX-idp.x)*(interceptX-idp.x) +
                                  (interceptY-idp.y)*(interceptY-idp.y) ));
        } else if ( b.x > a.x && b.x > interceptX && interceptX > a.x) {
            return (int) Math.floor(
                       Math.sqrt( (interceptX-idp.x)*(interceptX-idp.x) +
                                  (interceptY-idp.y)*(interceptY-idp.y) ));
        } else {
            return Integer.MAX_VALUE;
        }
    }

    Point2D toP2D(Point p) {
        return new Point2D.Double(p.x, p.y);
    }

    Point toPoint(Point2D p) {
        return new Point((int) p.getX(), (int) p.getY());
    }

}
