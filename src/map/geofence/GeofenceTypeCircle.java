package com.map.geofence;

import com.map.Dot;
import com.map.CoordinateTransform;
import com.util.UtilHelper;

import java.util.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 11/17/2021
 * Description: Concrete FenceType class defining the dimensions and
 * drawing behavior for a circular fence.
 */
public class GeofenceTypeCircle extends GeofenceType {
	protected BasicStroke lineStroke;
	/**
	 * Constructor
	 * @param radius_ft - the radius to the edge of the fence from the origin
	 */
	public GeofenceTypeCircle(double radius_ft) {
		super(radius_ft);
		
		lineStroke = new BasicStroke(4.0f);
	}
	
	/**
	 * Draws the outline of the fence centered on the
	 * origin waypoint's position.
	 * @param graphics 	- graphics context used for drawing.
	 * @param transform - The map transform used for drawing
	 * 					  correct location conversions.
	 * @param zoom		- The zoom level of the map.
	 */
	@Override
	public void paint(Graphics graphics, CoordinateTransform transform) {
		Graphics2D graphics2d = (Graphics2D) graphics.create();
		
		//Get Drawn Locations of points
		Point2D center = transform.screenPosition(origin.getLocation());
		Point2D end = transform.screenPosition(radiusPoint.getLocation());

		//DEBUG
//		System.err.println(
//				"Center: X,Y: " + center.getX() + "," + center.getY());
//		System.err.println(
//				"End: X,Y: " + end.getX() + "," + end.getY());
		
		
		double dX = end.getX() - center.getX();
		double dY = end.getY() - center.getY();
		double length = Math.sqrt((dX * dX) + (dY * dY));
		
		//DEBUG
		System.err.println("Pixel Distance of radius: " + length);
//		System.err.println("DX: " + dX);
//		System.err.println("DY: " + dY);

		graphics2d.setStroke(lineStroke);
		graphics2d.drawOval(
				(int)(center.getX() - length), 
				(int)(center.getY() - length),
				(int)(2 * length), (int)(2 * length));
		
		graphics2d.drawRect(
				(int)(center.getX() - length),
				(int)(center.getY() - length),
				(int)(2 * length), (int)(2 * length));
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
		
		//TODO - CP - TEST - Remove me once Geofence testing completed.
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
		
		//DEBUG
		System.err.println("Setting origin, Lat: " 
		+ origin.getLatitude() + " Lng: " 
		+ origin.getLongitude());
		
		setRadiusLng();
	}
	
	/**
	 * Sets a map coordinate point that is the distance of the radius
	 * away from the origin point in degrees longitude. Primarily used to
	 * determine fence draw distance independant of map scaling factor.
	 */
	@Override
	protected void setRadiusLng() {
		double radiusKm = UtilHelper.getInstance().feetToKm(radius_ft);
		double degreesLng = UtilHelper.getInstance().kmToDegLng(radiusKm);
		
		radiusPoint = new Dot(origin.getLatitude(), 
				origin.getLongitude() + degreesLng, origin.getAltitude());
		
		//DEBUG
		System.err.println("Setting radiusPoint, Lat: " 
		+ radiusPoint.getLatitude() + " Lng: " 
		+ radiusPoint.getLongitude());
	}
}