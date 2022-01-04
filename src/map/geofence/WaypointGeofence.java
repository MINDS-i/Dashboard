package com.map.geofence;

import com.Context;

import com.map.RoverPath;
import com.map.Dot;

import java.awt.Point;
import java.awt.Graphics;

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
	public static final double MIN_RADIUS_FT = 150.0;

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
	
	//Fence Specific Vars
	protected FenceType fenceType;
	protected GeofenceType fence;
	protected double radius_ft;
	
	/**
	 * Class Constructor
	 * 
	 */
	
	/**
	 * Class constructor
	 * @param origin - The origin of the fence
	 * @param radius - the radius of the fence from is origin.
	 * @param type - The shape type of the fence
	 */
	public WaypointGeofence(Dot origin, double radius, FenceType type) {
		fenceType = type;
		//Take the larger radius between the provided and default
		radius_ft = (radius > MIN_RADIUS_FT) ? radius : MIN_RADIUS_FT;
		
		switch(fenceType) {
			case CIRCLE:
				fence = new GeofenceTypeCircle(origin, radius_ft);
				break;
			case SQUARE:
				fence = new GeofenceTypeSquare(origin, radius_ft);
				break;
			default:
				break;
		};
	}
	
	/**
	 * Get the point used for the origin of this Geofence.
	 * @return - The point
	 */
	public Dot getOriginPoint() {
		return fence.getOrigin();
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
		fence.paint(g);
	}
	
	
}
