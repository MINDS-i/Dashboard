package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;

import java.awt.Component;

import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-25-2020
 * Description: Command responsible for editing waypoint properties from the
 * Waypoint Panel editable telemtry fields.
 *
 */
public class WaypointCommandEdit extends WaypointCommand {
	
	public WaypointCommandEdit(WaypointList waypoints, int index) {
		super(waypoints, CommandType.EDIT);
		
		this.startPoint = new Dot(
				waypoints.get(index).dot().getLatitude(),
				waypoints.get(index).dot().getLongitude(),
				waypoints.get(index).dot().getAltitude());
		
		this.endPoint = null;
		this.index = index;
	} 
	
	@Override
	public boolean execute() {
		if(endPoint == null) {
			System.err.print("WaypointCommandMove - Execution failure. ");
			System.err.println("Endpoint was not set.");
			return false;
		}
		
		waypoints.set(endPoint, index);
		return true;
	}
	
	@Override
	public boolean undo() {
		waypoints.set(startPoint, index);
		return true;
	}
	
	@Override
	public boolean redo() {
		return execute();
	}	
}
