package com.map.command;

import java.util.logging.Logger;

import com.map.WaypointList;
import com.map.Dot;
import com.map.geofence.WaypointGeofence;

import java.awt.Component;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Abstract class defining the base structure of a
 * WaypointCommand. New commands should use this parent structure
 * as the baseline requirement for insuring system compatability and
 * functionality.
 */
public abstract class WaypointCommand {

	protected final Logger serialLog = Logger.getLogger("d.serial");
	
	//Constants
	protected static final double 	MIN_DISTANCE_FT = 45.00;
	public static final int 		MAX_WAYPOINTS 	= 63;

	
	//Warning Strings
	protected static final String WARN_NO_GEOFENCE_INTERSECT = 
			  "Add - Waypoint placement"
			+ " exceeds geofence. Canceling placement.";
	
	protected static final String WARN_MAX_WAYPOINTS_REACHED =
			  "Maximum waypoints reached.";
	
	protected static final String WARN_GEOFENCE_ALREADY_PLACED =
			  "Geofence already in place."
			+ " Cannot add waypoint at index 0.";
	
	protected static final String WARN_ENDPOINT_NOT_SET =
			  "Move - Execution failure."
			+ " Endpoint was not set.";
	
	protected static final String WARN_MIN_DISTANCE_DIALOG = 
			  "For optimal results, a minimum" 
			+ " distance of " + MIN_DISTANCE_FT + " feet between waypoints" 
			+ " is recommended.";
	
	//Command Types
	public enum CommandType {
		ADD, REMOVE, MOVE, EDIT, CLEAR, TARGET, PARSE, ADD_SWATH, REMOVE_SWATH,
		MOVE_SWATH};
	
	//Add/Remove Vars
	protected Dot point;
	protected int index;
	
	//Move Vars
	protected Dot startPoint;
	protected Dot endPoint;
	
	//Geofencing Vars
	protected WaypointGeofence geofence;
	
	protected WaypointList waypoints;
	protected CommandType type;
	
	/**
	 * Constructor
	 * @param waypoints - The current list of navigational waypoints
	 * @param type - The Type of command being created
	 */
	public WaypointCommand(WaypointList waypoints, CommandType type) {
		this.waypoints = waypoints;
		this.type = type;
	}
	
	//Abstract functions to be overriden in concrete classes.
	public abstract boolean execute();
	public abstract boolean undo();
	public abstract boolean redo();
	
	/**
	 * 
	 * @return CommandType - The command type of this command instance.
	 */
	public CommandType getType() {
		return type;
	}

	/**
	 * Sets the final point for a movement command, making it ready to be executed.
	 * If this point is not set, then the corresponding move command will not execute.
	 * @param ep - The ending point to be moved to.
	 */
	public void finalize(Dot endPoint) {
		this.endPoint = endPoint;
	}
}