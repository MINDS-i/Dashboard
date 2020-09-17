package com.map.command;

import com.map.WaypointList;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description:
 *
 */
public class WaypointCommandMove extends WaypointCommand {
	public WaypointCommandMove(WaypointList waypoints, CommandType type) {
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
