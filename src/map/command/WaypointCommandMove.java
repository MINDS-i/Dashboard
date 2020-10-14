package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for moving a waypoints location.
 *
 */
public class WaypointCommandMove extends WaypointCommand {
	
	public WaypointCommandMove(WaypointList waypoints, int index) {
		super(waypoints, CommandType.MOVE);
		
		this.startPoint = new Dot(waypoints.get(index).dot());
		this.endPoint = null;
		this.index = index;
	}
	
	/**
	 * Moves the point to the 
	 * @return Whether or not the operation was successful. In the
	 * case of a failure, ensure that the end point was set before
	 * attempting execution.
	 */
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
