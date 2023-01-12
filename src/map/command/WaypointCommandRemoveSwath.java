package com.map.command;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.*;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Command responsible for removing a swath path from the active
 * session list.
 */
public class WaypointCommandRemoveSwath extends WaypointCommand {

	//Constants
	//Warning Strings (If Any)
	
	//Member vars
	List<Dot> swathPoints;
	
	/**
	 * Constructor
	 * @param waypoints - The master list of waypoints
	 * @param index - The index to begin removal at.
	 */
	public WaypointCommandRemoveSwath(WaypointList waypoints, int index) {
		super(waypoints, CommandType.REMOVE_SWATH);
		
		//TODO - CP - SWATH_REMOVE Load all points into list by iterating 
		//until start/end is found (depending on where we start).
		this.index = index;
	}
	
	/**
	 * Removes all points in the swath path from the waypoint list.
	 * @return - Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean execute() {
		//TODO - CP - SWATH_REMOVE - Iterate over swath points and remove
		//them from the waypoint list
		
		return true;
	}
	
	/**
	 * Re-Adds the swath path points into the waypoint list.
	 * @return - Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		//TODO - CP - SWATH_REMOVE - Re-add swath path at insertion index.
		
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
	 * @return - List - The list of points for the swath pattern.
	 */
	protected List<Dot> getSwathList() {
		//TODO - CP - SWATH_REMOVE Iterate over swath points here.
		return null;
	}
}
