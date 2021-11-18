package com.map.geofence;

import com.map.Dot;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Chris Park @ Infinetix Corp
 * Date 11/17/2021
 * Description: Abstract base class defining the basic shared charicteristics
 * and functions of a geofence type. 
 */
public abstract class FenceType {
	
	Dot origin;
	int radius_ft;
	
	/**
	 * Constructor
	 * @param origin - Center origin for the geofence
	 * @param radius_ft - radius from the origin to the geofence wall
	 */
	public FenceType(Dot origin, int radius_ft) {
		this.origin = origin;
		this.radius_ft = radius_ft;
	}
	
	//Abstract functions to be overriden in concrete classes.
	public abstract void paint(Graphics graphics);
	public abstract boolean doesIntersect(Dot coordinate);
}
