package com.map.geofence;

import com.map.Dot;

import java.util.*;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 11/17/2021
 * Description: Concrete FenceType class defining the dimensions and
 * drawing behavior for a circular fence.
 */
public class GeofenceTypeCircle extends GeofenceType {

	/**
	 * Constructor
	 * @param origin - the central origin point (waypoint) for the fence
	 * @param radius_ft - the radius to the edge of the fence from the origin
	 */
	public GeofenceTypeCircle(Dot origin, double radius_ft) {
		super(origin, radius_ft);
	}
	
	/**
	 * Draws the outline of the fence centered on the
	 * origin waypoint's position.
	 * @param graphics - graphics context used for drawing.
	 */
	@Override
	public void paint(Graphics graphics) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		
		graphics2d.drawOval(
				(int)origin.getLongitude(), (int)origin.getLatitude(),
				(int)radius_ft, (int)radius_ft);
	}
	
	/**
	 * Determines if a given coordinate lands within the geofence.
	 * @param coordinate - The coordinate to check for inside the fence.
	 * @return - True if the coordinate lands within the fence
	 */
	@Override
	public boolean doesIntersect(Dot coordinate) {
		double xDiff;
		double yDiff;
		double distanceSquared;
		
		xDiff = origin.getLatitude() - coordinate.getLatitude();
		yDiff = origin.getLongitude() - coordinate.getLongitude();
		distanceSquared = Math.pow(xDiff, 2)+ Math.pow(yDiff, 2);
		
		return (distanceSquared < ((radius_ft * 2) * (radius_ft * 2)));
	}
	
	/**
	 * Returns this fences origin (center) point
	 *  @return - The origin of the fence
	 */
	@Override
	public Dot getOrigin() {
		return origin; 
	}
}