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
	 */
	@Override
	public void paint(Graphics graphics, CoordinateTransform transform) {
		Graphics2D graphics2d = (Graphics2D) graphics.create();
		
		//Get Drawn Locations of points
		Point2D center = transform.screenPosition(origin.getLocation());
		Point2D end = transform.screenPosition(radiusPoint.getLocation());
		
		//Calculate the side of a square that would be inscribed inside
		//the geofence's circular perimeter.
		double dX = end.getX() - center.getX();
		double dY = end.getY() - center.getY();
		double length = Math.sqrt((dX * dX) + (dY * dY));
		
		//Find diameter of the fence (hypotenuse of the square) using 
		//the Pythagorean theorum again
		double a = Math.pow((2 * length), 2);;
		double b = Math.pow((2 * length), 2);;
		double fenceDiameter = Math.sqrt(a + b);
		
		//Draw the fence. The drawOval function assumes drawing starts
		//from a top left corner point much like a square would be drawn,
		//so X and Y coordinates need to be offset by half the fence
		//diameter to center the circle on the origin point.
		graphics2d.setStroke(lineStroke);
		graphics2d.drawOval(
				(int)(center.getX() - (fenceDiameter / 2)), 
				(int)(center.getY() - (fenceDiameter / 2)),
				(int)(fenceDiameter), (int)(fenceDiameter));
		
//		graphics2d.drawRect(
//				(int)(center.getX() - length),
//				(int)(center.getY() - length),
//				(int)(2 * length), (int)(2 * length));
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
	}
}