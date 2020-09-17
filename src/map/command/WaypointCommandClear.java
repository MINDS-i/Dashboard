package com.map.command;

import com.map.WaypointList;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for moving a waypoint from one position
 * to another.
 */
public class WaypointCommandClear extends WaypointCommand {
	public WaypointCommandClear(WaypointList waypoints, CommandType type) {
		super(waypoints, type);
	}
	
	@Override
	public boolean execute() {
		return false;
	}
	
	@Override
	public boolean undo() {
		return false;
	}
	
	@Override
	public boolean redo() {
		return false;
	}
}
