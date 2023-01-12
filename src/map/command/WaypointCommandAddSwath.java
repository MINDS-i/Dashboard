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
 * Description: Command responsible for adding a swath path to the
 * active session list.
 */
public class WaypointCommandAddSwath extends WaypointCommand {

	//Constants
	//Warning Strings (If Any)
	
	//Member Vars
	protected List<Dot> swathPoints;
	
	/**
	 * Constructor
	 * @param waypoints = List of current navigational waypoints
	 * @param swathPoints - Waypoint list to be manually edited by this command
	 * @param index - Index in the waypoint list to insert the swathPoints list
	 */
	public WaypointCommandAddSwath(
			WaypointList waypoints, List<Dot> swathPoints, int index) {
		super(waypoints, CommandType.ADD_SWATH);
		
		//TODO - CP - SWATH_ADD Determine if the single point from inheritence should
		//be used in some way...(perhaps as the leading point in the point
		//list?) Or is it safe to leave it as null?
		this.swathPoints = swathPoints;
		this.index = index; //Where the start of the swathPoints list is.
	}
	
	/**
	 * Adds the entire list of swathPoints for the related swath pattern
	 * to the waypoint list at the specified location.
	 * @return Boolean - Whether or not the command was executed successfully.
	 */
	@Override
	public boolean execute() {
		//TODO - CP - SWATH_ADD Do swath pattern here.
		
		//(Start at/track from insertion index)
		//For each point in swathPoints List
		//If the waypoint max hasn't been reached yet
		//add current point to the master waypoint list
		
		return true;
	}
	
	/**
	 * Removes the related swathPoints list from the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		//TODO - CP - SWATH_ADD Undo swath pattern here.
		
		//(Start at insertion index + swathPoints length)
		//Remove the point at index
		//Repeat swathPoint length times. OR until the type of the removed
		//point is a START type.
		return true;
	}
	
	/**
	 * Re-adds the swathPoints list to the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	public boolean redo() {
		return execute();
	}
	
	
	//TODO - CP - SWATH_ADD Add additional functionality here if needed.
}
