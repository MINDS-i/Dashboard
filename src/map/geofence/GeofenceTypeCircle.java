package com.map.geofence;

import com.map.Dot;
import com.map.CoordinateTransform;
import com.util.UtilHelper;

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
	public void paint(Graphics graphics, CoordinateTransform transform) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		Point2D point = transform.screenPosition(origin.getLocation());

		graphics2d.drawOval(
				(int)point.getX() - ((int)radius_ft / 2), 
				(int)point.getY() - ((int)radius_ft / 2),
				(int)radius_ft, (int)radius_ft);
	}
	
	/**
	 * Determines if a given coordinate lands within the geofence.
	 * @param coordinate - The coordinate to check for inside the fence.
	 * @return - True if the coordinate lands within the fence
	 */
	@Override
	public boolean doesIntersect(Dot coordinate) {
		double distance;
		distance = UtilHelper.getInstance().haversine(origin, coordinate);
		distance = UtilHelper.getInstance().kmToFeet(distance);
		
		//TODO - CP - TEST - Remove Me once Geofence testing completed.
		System.err.println("Geofence Circle - Distance: " + distance + "ft");
		System.err.println("Geofence Circle -   Radius: " + radius_ft + "ft");
		
		return (distance < radius_ft);
	}
	
	/**
	 * Returns this fences origin (center) point
	 *  @return - The origin of the fence
	 */
	@Override
	public Dot getOriginLatLng() {
		return origin; 
	}
	
	/**
	 * Sets the latitude and longitude of this geofences origin.
	 */
	@Override
	public void setOriginLatLng(double lat, double lng) {
		origin.setLatitude(lat);
		origin.setLongitude(lng);
	}
}