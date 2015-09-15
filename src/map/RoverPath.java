package com.map;

import com.Context;
import com.ContextViewer;
import com.serial.Serial;
import com.ui.TelemetryListener;
import com.layer.Layer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

class RoverPath implements Layer, ContextViewer {
    private static final Color ACTIVE_LINE_FILL = new Color(1.f,1.f,0.f,1f);
    private static final Color PATH_LINE_FILL   = new Color(0f,0f,0f, 1f);
    private static final Color LINE_BORDER      = new Color(.5f,.5f,.5f,0f);
    private static final int   LINE_WIDTH       = 10;

    private int downDot = -1;
    private Context context;
    private CoordinateTransform mapTransform;
    private WaypointPanel waypointPanel;
    private Dot rover = new Dot();

    //do the thing to update rover position
    //decouple from waypointPanel
    //clean up point drag code
    //gut mapPanel
    //refresh map

    RoverPath(Context c, CoordinateTransform cT, WaypointPanel wP) {
        context = c;
        mapTransform = cT;
        waypointPanel = wP;

        context.registerViewer(this);
        context.telemetry.registerListener(Serial.LATITUDE, new TelemetryListener(){
            public void update(double data){
                rover.setLatitude( data );
            }
        });
        context.telemetry.registerListener(Serial.LONGITUDE, new TelemetryListener(){
            public void update(double data){
                rover.setLongitude( data );
            }
        });
    }

    public int getZ() {
        return 1;
    }

    public void paint(Graphics g){
        paintDots(g);
    }

    public void waypointUpdate(){
        //direct parent to repaint?
    }

    public boolean onClick(MouseEvent e){
        Point2D pixel = toP2D(e.getPoint());
        int underneith = isOverDot(pixel, context.theme.waypointImage);

        if((e.getButton() == MouseEvent.BUTTON1) && (underneith == -1)){
            //left click thats not over a dot
            Point2D point = mapTransform.mapPosition(pixel);

            int line = isOverLine(e.getPoint());
            if (line==-1) {
                //click is not over an existing line
                context.waypoint.add(new Dot(point));
                waypointPanel.setSelectedWaypoint(context.waypoint.size()-1);
            } else {
                //click is over an existing line
                context.waypoint.add(new Dot(point), line);
                waypointPanel.setSelectedWaypoint(line);
            }
            return true;
        } else if ((e.getButton() == MouseEvent.BUTTON3) && (underneith != -1)) {
            //right click on top of a point
            context.waypoint.remove(underneith);
            return true;
        }
        return false;
    }

    public boolean onPress(MouseEvent e){
        Point pixel = e.getPoint();
        downDot = isOverDot(pixel, context.theme.waypointImage);
        if(downDot != -1) {
            waypointPanel.setSelectedWaypoint(downDot);
            context.waypointUpdated();
            return true;
        }
        return false;
    }

    public void onDrag(MouseEvent e){
        Point2D pixel = toP2D(e.getPoint());
        if (downDot != -1){
            Point2D finalLoc = mapTransform.mapPosition(pixel);
            context.waypoint.get(downDot).setLocation(finalLoc);
            context.waypointUpdated();
        }
    }

    public void onRelease(MouseEvent e){
        Point pixel = e.getPoint();
        if(downDot != -1){
            context.waypoint.sendUpdatedPosition(downDot);
        }
    }

    public void paintLine(Graphics g, Point2D pointA, Point2D pointB, Color fill){
        Graphics2D g2d = (Graphics2D) g.create();
        RenderingHints hints = g2d.getRenderingHints();
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(hints);
        try{
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
            n = toPoint(mapTransform.screenPosition( ((Dot)itr.next()).getLocation()));
            if(l!=null) paintLine(g, n, l, PATH_LINE_FILL);
            l = n;
        }
        if(context.waypoint.isLooped()){
            n = toPoint(mapTransform.screenPosition( context.waypoint.get(0).getLocation()));
            paintLine(g, n, l, PATH_LINE_FILL);
        }
    }

    private void drawPoints(Graphics g){
        Point2D tmp;
        Iterator itr = context.waypoint.iterator();
        int i=0;
        while(itr.hasNext()){
            tmp = mapTransform.screenPosition( ((Dot)itr.next()).getLocation() );
            if(i==waypointPanel.getSelectedWaypoint())
                drawImg(g, context.theme.waypointSelected, tmp);
            else
                drawImg(g, context.theme.waypointImage, tmp);
            i++;
        }
    }

    private void drawRover(Graphics g){
        Point2D roverPoint = mapTransform.screenPosition(rover.getLocation());
        drawImg(g, context.theme.roverImage, roverPoint);
    }

    private void drowRoverLine(Graphics g){
        if(context.waypoint.getTarget() >= context.waypoint.size()) {
            System.err.println("roverTarget out of Bounds");
            return;
        }
        Point n = toPoint(mapTransform.screenPosition( rover.getLocation() ));
        Point l = toPoint(mapTransform.screenPosition( context.waypoint.getTargetWaypoint().getLocation() ));
        paintLine(g, n, l, ACTIVE_LINE_FILL);
    }

    private void drawImg(Graphics g, BufferedImage img, Point2D loc){
        g.translate(-img.getWidth()/2, -img.getHeight()/2);
        g.drawImage( img , (int)loc.getX(), (int)loc.getY(), null);
        g.translate( img.getWidth()/2, img.getHeight()/2);
    }

    public int isOverDot(Point2D click, BufferedImage image){
        for(int i=0; i<context.waypoint.size(); i++){
            Dot d = context.waypoint.get(i);
            Point2D loc = mapTransform.screenPosition(d.getLocation());
            if(Math.abs(click.getX()-loc.getX()-1) > image.getWidth() /2) continue;
            if(Math.abs(click.getY()-loc.getY()-1) > image.getHeight()/2) continue;
            return i;
        }
        return -1;
    }

    public int isOverLine(Point p){
        if(context.waypoint.size()<2) return -1;
        Point prevPoint = toPoint(mapTransform.screenPosition(rover.getLocation()));
        for(int i=0; i<context.waypoint.size(); i++){
            Point thisPoint = toPoint(mapTransform.screenPosition(
                                context.waypoint.get(i).getLocation()));
            if( distFromLine(prevPoint, thisPoint, p) < (LINE_WIDTH/2) ){
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

    Point2D toP2D(Point p){
        return new Point2D.Double(p.x, p.y);
    }

    Point toPoint(Point2D p){
        return new Point((int) p.getX(), (int) p.getY());
    }

}
