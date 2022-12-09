package com.map.geofence;

import com.map.CoordinateTransform;
import com.map.RoverPath;
import com.map.Dot;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 11/15/2021
 * Description: Provides an interface to create, manage, and manipulate a
 * geofence of variable size around an initial waypoint. Once placed, only
 * waypoints which fall within the bounds of the fence will be considered valid
 * and added to the waypoint mission list. In order to create a new fenced area,
 * the current waypoint mission must be cleared.
 */
public class WaypointGeofence {
	
	//Constants
	public static final double MIN_RADIUS_FT = 100.0;

	/**
	 * Fence Type Enum
	 * Pre-defined fence types.
	 */
	public enum FenceType {
		CIRCLE		(0),
		SQUARE		(1);
		
		private final int type;
		
		FenceType(int type) {
			this.type = type;
		}
		
		public int getValue() {
			return this.type;
		}
	};
	
	//Vars
	protected FenceType fenceType;
	protected GeofenceType fence;
	protected CoordinateTransform mapTransform;
	protected double radius_ft;
	protected boolean isEnabled;
	
	/**
	 * Class constructor. Creates a basic inactive geofence of the supplied
	 * type with default coordinates.
	 * @param radius 	- The radius of the fence from is origin.
	 * @param type 	 	- The shape type of the fence
	 * @param transform - The map transform used for drawing.
	 */
	public WaypointGeofence(double radius, FenceType type,
			CoordinateTransform transform) {
		fenceType = type;
		//Take the larger radius between the provided and default
		radius_ft = (radius > MIN_RADIUS_FT) ? radius : MIN_RADIUS_FT;
		mapTransform = transform;
		
		switch(fenceType) {
			case CIRCLE:
				fence = new GeofenceTypeCircle(radius_ft);
				break;
			case SQUARE:
				fence = new GeofenceTypeSquare(radius_ft);
				break;
			default:
				break;
		};
		
		isEnabled = false;
	}

	/**
	 * Get the point used for the origin of this Geofence.
	 * @return - The point
	 */
	public Dot getOriginLatLng() {
		return fence.getOriginLatLng();
	}
	
	/**
	 * Set the point used for the origin of this Geofence.
	 */
	public void setOriginLatLng(double lat, double lng) { 
		fence.setOriginLatLng(lat, lng);
	}
	
	/**
	 * Uses the maps coordinate transform to return the geofence's 
	 * origin latitude/longitude position as pixel screen coordinates.
	 * @return - Point2D - The pixel space representation of the
	 * 					   origins Lat/Lng.
	 */
	public Point2D getOriginPixels() {
		return mapTransform.screenPosition(
				fence.getOriginLatLng().getLocation());
	}
	
	/**
	 * Checks to see if the given coordinate location interesects
	 * within the bounds of the geofence. Returns true if it does.
	 * @param location - The location in question.
	 * @return - boolean - Whether or not the location intersects the fence.
	 */
	public boolean doesLocationIntersect(Dot location) {
		return fence.doesIntersect(location);
	}
	
	/**
	 * Draws the Geofence on the map.
	 * @param g - the Graphics context used for painting
	 */
	public void paintFence(Graphics g) {
		fence.paint(g, mapTransform);
	}
	
	/**
	 * Returns the enabled/disabled state of the fence.
	 * @return - boolean - Whether or not the fence is currently enabled.
	 */
	public boolean getIsEnabled() {
		return isEnabled;
	}
	
	/**
	 * Toggle the enabled/disabled state of the geofence to determine
	 * whether it should be considered or drawn for waypoint placements.
	 * @param enable - Whether or not to enable or disable the fence. 
	 */
	public void setIsEnabled(boolean enable) {
		isEnabled = enable;
	}

	/**
	 * Returns the geofence radius in feet.
	 * @return - the radius of the geofence.
	 */
	public double getRadius() {
		return radius_ft;
	}
	
	/**
	 * Updates the radius of the geofence and forces the generation of a
	 * new radiusPoint by calling setOriginLatLng(). (See setRadiusLng()
	 * function in the specific fence implementation for details)
	 * @param new_radius_ft - the radius value to update to.
	 */
	public void updateRadius(double new_radius_ft) {
		Dot origin = getOriginLatLng();
		
		radius_ft = new_radius_ft;
		fence.updateRadiusFeet(radius_ft);
		fence.setOriginLatLng(origin.getLatitude(), origin.getLongitude());
	}
}
