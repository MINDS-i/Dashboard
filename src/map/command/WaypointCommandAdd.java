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
	
	public WaypointCommandAdd(WaypointList waypoints, Dot point, 
			int index, boolean isTarget) {
		super(waypoints, CommandType.ADD);
		
		this.point = point;
		this.index = index;
		this.isTarget = isTarget;
	}
	
	@Override
	public boolean execute() {
		waypoints.add(point, index);
		
		// If this point was added to an existing line
		// at index 0, make it the new target of the rover. 
		if(isTarget == true) {
			waypoints.setSelected(index);
			waypoints.setTarget(index);
		}
		else {
			waypoints.setSelected(index - 1);
		}
		
		return true;
	}
	
	@Override
	public boolean undo() {
		waypoints.remove(index);
		
		return true;
	}
	
	@Override
	public boolean redo() {
		return execute();
	}
}
