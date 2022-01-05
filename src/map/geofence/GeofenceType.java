package com.map.geofence;

import com.map.Dot;
import com.map.CoordinateTransform;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Chris Park @ Infinetix Corp
 * Date 11/17/2021
 * Description: Abstract base class defining the basic shared charicteristics
 * and functions of a geofence type. 
 */
public abstract class GeofenceType {
	
	Dot origin;
	double radius_ft;
	
	/**
	 * Constructor
	 * @param origin - Center origin for the geofence
	 * @param radius_ft - radius from the origin to the geofence wall
	 */
	public GeofenceType(Dot origin, double radius_ft) {
		this.origin = origin;
		this.radius_ft = radius_ft;
	}
	
	//Abstract functions to be overriden in concrete classes.
	public abstract void paint(Graphics graphics, CoordinateTransform transform);
	public abstract boolean doesIntersect(Dot coordinate);
	public abstract Dot getOriginLatLng();
	public abstract void setOriginLatLng(double lat, double lng);
}
