package com.map;

import com.Context;

import com.map.RoverPath;
import com.map.Dot;

//TODO - CP - Continue fleshing out constructor. Make sure configurable size plays nice.

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 11/15/2021
 * Description: Provides an interface to create, manage, and manipulate a
 * geofence of variable size around an initial waypoint. Once placed, only
 * waypoints which fall within the bounds of the fence will be considered valid
 * and added to the waypoint mission list.
 */
public class WaypointGeofence {
	
	//Constants
	protected static final int MIN_RADIUS_FT = 150;
	
	
	//Vars
	protected int xPos;
	protected int yPos;
	protected int radius_ft;
	
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 */
	public WaypointGeofence(Context ctx, Dot origin, int radius) {
		//Calc positions from origin and radius
		//check radius against default and take the larger of two.
	}
	
	/**
	 * Get the point used for the origin of this Geofence.
	 * @return - The point
	 */
	public Dot getOriginPoint() {
		//TODO - CP - Implement origin retrieval here
		return null;
	}
	
	/**
	 * Checks to see if the given coordinate location interesects
	 * within the bounds of the geofence. Returns true if it does.
	 * @param location - The location in question.
	 * @return - boolean - Whether or not the location intersects the fence.
	 */
	public boolean doesLocationIntersect(Dot location) {
		//TODO - CP - Implement point intersection check
		return true;
	}
	
	
	//TODO - CP - Continue to flesh out paint function requirements here.
	//Will need:
	//	- Type of fence (Circle/Square)
	//	- Draw X dist from origin (dashed lines pref.)
	//	- Fence should be thinner, but still easily visible
	//	- Use RoverPath.java's paintLine function as a base.
	//	- 
	public void paintFence() {
		
	}
	
	
}
