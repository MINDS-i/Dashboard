package com.map.command;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.*;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;
import com.map.WaypointType;

//TODO - CP - SWATH_MOVE - See below edge cases/issues to work determine.
	//How do we pinpoint the start point index?
	//Should the swath path be passed into the constructor?
		//For all swath commands?

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Command responsible for moving the position of a swath
 * pattern.
 */
public class WaypointCommandMoveSwath extends WaypointCommand {

	//Member Vars
	protected List<Dot> swathStartPoints;
	protected List<Dot> swathEndPoints;
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigation waypoints
	 * @param index - Insertion index of the beginning of the swathPoint list.
	 */
	public WaypointCommandMoveSwath(WaypointList waypoints, int index) {
		super(waypoints, CommandType.MOVE_SWATH);
		
		this.index = index;
		buildSwathStartList();
		this.swathEndPoints = null;
	}
	
	/**
	 * Moves the swath path to the new position detailed by the finalize
	 * method.
	 * @return Boolean - Whether or not the operation was successful. In the
	 * case of a failure, ensure that the end point was set before attempting
	 * execution, and that no individual point in the swath path exceeds the
	 * geofence.
	 */
	@Override
	public boolean execute() {
		CommandManager manager = CommandManager.getInstance();
		
		
		//TODO - CP - SWATH_MOVE See following for swath path movement:
			//make global copy of swath list (swathEndPoints)
				//Move all swath points by supplied distance.
					//Add lat diff to lat, and lng diff to lng;
			//Make sure no point exceeds the geofence.
			//Make sure the end point exists (not null) before moving.
		
		//If endpoints have not been set
		if(swathEndPoints == null) {
			serialLog.warning(WARN_ENDPOINT_NOT_SET);
			return false;
		}
		
		if(manager.getGeofence().getIsEnabled()) {
			//For each swath end point
				//Check for intersect
					//If no intersect, abort
//			if(!manager.getGeofence().doesLocationIntersect(pointhere)) {
//				
//			}
		}
		
		
		return true;
	}
	
	/**
	 * Returns the swath path outlined by this command to its original position.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		//TODO - CP - SWATH_MOVE move everything back to the start point.
		return true;
	}
	
	/**
	 * Move the swath path back to its modified postion again.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
	
	/**
	 * Parses over the waypoint list and retrieves the points that make up
	 * this commands related swath pattern in their original positions.
	 */
	protected void buildSwathStartList() {
		int currIndex = this.index;
		boolean isSwathPoint = true;
		Dot tempDot;
		
		this.swathStartPoints = new ArrayList<Dot>();
		
		//Starting at the insertion index, interate over the swath points
		//and add them until the end point is found by type check.
		while(isSwathPoint) {
			tempDot = this.waypoints.get(currIndex).dot();
			this.swathStartPoints.add(tempDot);
			
			if(tempDot.getWaypointType() == WaypointType.SWATH_END) {
				isSwathPoint = false;
			}
			
			currIndex++;
		}
	}
	
	/**
	 * Parses over the waypoint list and retrieves the points that make up
	 * this commands related swath pattern in their altered positions.
	 */
	protected void buildSwathEndList() {
		
	}
	
	//TODO - CP - SWATH_MOVE Create finalize overload function for all points.
	public void finalize(double latOffset, double lngOffset) {
		//Apply the offset to each swathPoint, copying it to the endpoint
		//list.
	}
}
