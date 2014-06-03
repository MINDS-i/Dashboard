package com.map;

import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.Point;

public class Dot{
	Point.Double location;
	public Dot(){
		location = new Point.Double(0,0);
	}
	public Dot(Point.Double l){
		location = l;
	}
	public Dot(Dot dot){
		location = dot.getLocation();
	}
	public void setLocation(Point.Double l){
		location = l;
	}
	public void setLatitude(double lat){
		location = new Point.Double(location.getX(), lat);
	}
	public void setLongitude(double lng){
		location = new Point.Double(lng, location.getY());
	}
	public Point.Double getLocation(){
		return location;
	}
	public double getLongitude(){
		return location.x;
	}
	public double getLatitude(){
		return location.y;
	}
}
