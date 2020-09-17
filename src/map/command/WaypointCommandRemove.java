package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description:
 *
 */
public class WaypointCommandRemove extends WaypointCommand {
	public WaypointCommandRemove(WaypointList waypoints, int index) {
		super(waypoints, CommandType.REMOVE);
		this.index = index;
	}
	
	@Override
	public boolean execute() {
		waypoints.remove(index);
		
		return true;
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
