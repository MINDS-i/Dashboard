package com.map;

import com.Context;
import com.layer.Layer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

class RoverPath implements Layer {
    private static final Color ACTIVE_LINE_FILL =  new Color(1.f,1.f,0.f,1f);
    private static final Color PATH_LINE_FILL   =  new Color(0f,0f,0f, 1f);
    private static final Color LINE_BORDER      =  new Color(.5f,.5f,.5f,0f);

    private int downDot = -1;
    private Context context;
    private CoordinateTransform mapTransform;
    private WaypointPanel waypointPanel;
    private Dot rover = new Dot();

    //do the thing to update rover position
    //decouple from waypoint panel

    //extract roverpath class
        //clean up point drag code
        //map position dependency
            //compute screen position
            //compute from screen position
    //gut this class

    RoverPath(Context c, CoordinateTransform cT, WaypointPanel wP) {
        context = c;
        mapTransform = cT;
        waypointPanel = wP;
    }

    public int getZ() {
        return 1;
    }
    public void paint(Graphics g){
        paintDots(g);
    }

    public boolean onClick(MouseEvent e){
        Point pixel = e.getPoint();
        int underneith = isOverDot(pixel, context.theme.waypointImage);

        if (e.getButton() == MouseEvent.BUTTON1) {
            if(underneith == -1){
                Point.Double point = (Point.Double) mapTransform.mapPosition(
                    new Point.Double(pixel.getX(), pixel.getY()));

                int tmp = isOverLine(pixel);
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
            if(underneith != -1 ) context.waypoint.remove(underneith);
        }

        return true;
    }

    public boolean onPress(MouseEvent e){
        Point pixel = e.getPoint();
        downDot = isOverDot(pixel, context.theme.waypointImage);
        if(downDot != -1) {
            waypointPanel.setSelectedWaypoint(downDot);
            return true;
        }
        return false;
    }

    public void onDrag(MouseEvent e){
        Point pixel = e.getPoint();
        if (downDot != -1){
            Point.Double finalLoc = (Point.Double) mapTransform.mapPosition(
                new Point.Double(pixel.getX(), pixel.getY()));
            context.waypoint.get(downDot).setLocation(finalLoc);
            //use waypoint updated method in context here
        }
    }

    public void onRelease(MouseEvent e){
        Point pixel = e.getPoint();
        if(downDot != -1){
            context.waypoint.sendUpdatedPosition(downDot);
        }
    }

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
            if(i++==waypointPanel.getSelectedWaypoint())
                drawImg(g, context.theme.waypointSelected, tmp);
            else
                drawImg(g, context.theme.waypointImage, tmp);
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

    public int isOverDot(Point click, BufferedImage image){
        for(int i=0; i<context.waypoint.size(); i++){
            Dot d = context.waypoint.get(i);
            Point2D loc = mapTransform.screenPosition(d.getLocation());
            if(Math.abs(click.x-loc.getX()-1) > image.getWidth() /2) continue;
            if(Math.abs(click.y-loc.getY()-1) > image.getHeight()/2) continue;
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

    Point2D toP2D(Point p){
        return new Point2D.Double(p.x, p.y);
    }

    Point toPoint(Point2D p){
        return new Point((int) p.getX(), (int) p.getY());
    }

}
