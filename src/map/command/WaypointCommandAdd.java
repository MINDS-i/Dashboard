package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for adding a waypoint to the
 * active sessions list.
 */
public class WaypointCommandAdd extends WaypointCommand {
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints.
	 * @param point - Waypoint to be manually edited by this command
	 * @param index - index in the waypoint list of the waypoint being added.
	 */
	public WaypointCommandAdd(WaypointList waypoints, Dot point, int index) {
		super(waypoints, CommandType.ADD);
		
		this.point = point;
		this.index = index;
	}
	
	/**
	 * Adds a new waypoint at the specified location. 
	 * @return Boolean - Whether or not the command was executed successfully.
	 */
	@Override
	public boolean execute() {
		waypoints.add(point, index);
		
		// If this point was added to an existing line
		// at index 0, make it the new target of the rover. 
		if(index == 0) {
			waypoints.setTarget(index);
		}
		
		waypoints.setSelected(index);	
		return true;
	}
	
	/**
	 * Removes the added waypoint described by this command.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		waypoints.remove(index);
		
		return true;
	}
	
	/**
	 * Re-adds the detailed waypoint to the list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
}
