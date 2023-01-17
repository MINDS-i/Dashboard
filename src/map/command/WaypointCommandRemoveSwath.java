package com.map.command;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.*;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;
import com.map.WaypointType;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Command responsible for removing a swath path from the active
 * session list.
 */
public class WaypointCommandRemoveSwath extends WaypointCommand {
	
	//Member vars
	List<Dot> swathPoints;
	
	/**
	 * Constructor
	 * @param waypoints - The master list of waypoints
	 * @param index - The insertion index to begin removal at.
	 */
	public WaypointCommandRemoveSwath(WaypointList waypoints, int index) {
		super(waypoints, CommandType.REMOVE_SWATH);
		
		this.index = index;
		buildSwathList();
	}
	
	/**
	 * Removes all points in the swath path from the waypoint list.
	 * @return - Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean execute() {
		int currIndex = this.index;
		boolean isSwathPoint = true;
		Dot tempDot;
		
		while(isSwathPoint) {
			tempDot = this.waypoints.get(currIndex).dot();
			this.waypoints.remove(currIndex);
			
			if(tempDot.getWaypointType() == WaypointType.SWATH_END) {
				isSwathPoint = false;
			}
			
			currIndex++;
		}
		
		return true;
	}
	
	/**
	 * Re-Adds the swath path points into the waypoint list.
	 * @return - Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		int currIndex = this.index;
		CommandManager manager = CommandManager.getInstance();

		//Check for any waypoint intersections first
		for(Dot point : swathPoints) {
			if(!manager.getGeofence().doesLocationIntersect(point)) {
				serialLog.warning(WARN_NO_GEOFENCE_INTERSECT);
				return false;
			}
		}
		
		//For each point in swathPoints List
		for(Dot point : swathPoints) {
			//If the waypoint max hasn't been reached yet
			if(waypoints.size() < MAX_WAYPOINTS) {
				waypoints.add(point, currIndex);
				currIndex++;
			}
		}
		
		return true;
	}
	
	/**
	 * Executes the removal of the swath path from the waypoint list again.
	 * @return - Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
	
	/**
	 * Parses over the waypoint list and retrieves the points that make up
	 * this commands related swath pattern.
	 */
	protected void buildSwathList() {
		int currIndex = this.index;
		boolean isSwathPoint = true;
		Dot tempDot;
		
		this.swathPoints = new ArrayList<Dot>();
		
		//Starting at the insertion index, interate over the swath points
		//and add them until the end point is found by type check.
		while(isSwathPoint) {
			tempDot = this.waypoints.get(currIndex).dot();
			this.swathPoints.add(tempDot);
			
			if(tempDot.getWaypointType() == WaypointType.SWATH_END) {
				isSwathPoint = false;
			}
			
			currIndex++;
		}
	}
}
