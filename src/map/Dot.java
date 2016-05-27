package com.map;

import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.Point;

public class Dot {
    Point2D location;
    short altitude;
    int status;
    public Dot() {
        location = new Point.Double(0,0);
        altitude = 0;
    }
    public Dot(Point2D l, short alt) {
        location = l;
        altitude = alt;
    }
    public Dot(Point2D l) {
        location = l;
        altitude = 0;
    }
    public Dot(Dot dot) {
        location = dot.getLocation();
        altitude = dot.getAltitude();
    }
    public Dot(double lat, double lng, short alt) {
        location = new Point.Double(lng,lat);
        altitude = alt;
    }
    public void setLocation(Point2D l) {
        location = l;
    }
    public void setLocation(Point2D l, short alt) {
        location = l;
        altitude = alt;
    }
    public void setLatitude(double lat) {
        location = new Point.Double(location.getX(), lat);
    }
    public void setLongitude(double lng) {
        location = new Point.Double(lng, location.getY());
    }
    public void setAltitude(short alt) {
        altitude = alt;
    }
    public Point2D getLocation() {
        return location;
    }
    public double getLongitude() {
        return location.getX();
    }
    public double getLatitude() {
        return location.getY();
    }
    public short getAltitude() {
        return altitude;
    }
}
