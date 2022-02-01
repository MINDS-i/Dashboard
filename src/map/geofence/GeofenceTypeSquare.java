package com.map.geofence;

import com.map.Dot;
import com.map.CoordinateTransform;
import com.util.UtilHelper;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 11/17/2021
 * Description: Concrete FenceType class defining the dimensions and
 * drawing behavior for a square fence.
 */
public class GeofenceTypeSquare extends GeofenceType {

	/**
	 * Constructor 
	 * @param radius_ft - the radius to the edge of the fence from the origin
	 */
	public GeofenceTypeSquare(double radius_ft) {
		super(radius_ft);
		
	}
	
	/**
	 * Draws the outline of the fence centered on the
	 * origin waypoint's position.
	 * @param graphics - The graphics context used for drawing.
	 */
	@Override
	public void paint(Graphics graphics, CoordinateTransform transform) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		Point2D center = transform.screenPosition(origin.getLocation());
		Point2D end = transform.screenPosition(radiusPoint.getLocation());
		
		graphics2d.drawOval(
				(int)center.getX(), (int)center.getY(),
				(int)(center.getX() + end.getX()), 
				(int)(center.getY() + end.getY()));

	}
	
	/**
	 * Determines if a given coordinate lands within the geofence.
	 * @param coordinate - The coordinate to check for inside the fence.
	 * @return - True if the coordinate lands within the fence.
	 */
	@Override
	public boolean doesIntersect(Dot coordinate) {
		boolean boundedByX = false;
		boolean boundedByY = false;
		
//		double minXWall = origin.getLatitude() - radius_ft;
//		double maxXWall = origin.getLatitude() + radius_ft;
//		double minYWall = origin.getLongitude() - radius_ft;
//		double maxYWall = origin.getLongitude() + radius_ft;
//		
//		//Check if within X dimension of fence
//		if((coordinate.getLatitude() < maxXWall)
//		&& (coordinate.getLatitude() > minXWall)) {
//			boundedByX = true;
//		}
//		
//		//Check if within Y dimension of fence
//		if((coordinate.getLongitude() < maxYWall)
//		&& (coordinate.getLongitude() > minYWall)) {
//			boundedByY = true;
//		}

		return (boundedByX && boundedByY);
	}
	
	/**
	 * Returns this fences origin (center) point in map coordinates
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
	
	@Override
	protected void setRadiusLng() {
		double radiusKm = UtilHelper.getInstance().feetToKm(radius_ft);
		double degreesLng = UtilHelper.getInstance().kmToDegLng(radiusKm);
		
		radiusPoint = new Dot(origin.getLatitude(), 
				origin.getLongitude() + degreesLng, origin.getAltitude());
	}
}