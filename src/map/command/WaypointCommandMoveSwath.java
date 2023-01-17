package com.map.command;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.*;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;

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
	//Constants
	//Warning Strings if any
	
	//Member Vars
	protected List<Dot> swathPoints;
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigation waypoints
	 * @param index - Insertion index of the beginning of the swathPoint list.
	 */
	public WaypointCommandMoveSwath(WaypointList waypoints, int index) {
		super(waypoints, CommandType.MOVE_SWATH);
		
		//TODO - CP - SWATH_MOVE - Load all swath points into the local../
		//list for tracking here
		
		this.index = index;
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
		//TODO - CP - SWATH_MOVE See following for swath path movement:
			//Move all swath points by supplied distance.
			//Make sure no point exceeds the geofence.
			//Make sure the end point exists (not null) before moving.
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
	
	//TODO - CP - SWATH_MOVE Determine if finalize needs to be overridden here.
}
