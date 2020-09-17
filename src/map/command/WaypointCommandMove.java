package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description:
 *
 */
public class WaypointCommandMove extends WaypointCommand {
	
	public WaypointCommandMove(WaypointList waypoints, int index) {
		super(waypoints, CommandType.MOVE);
		
		this.startPoint = new Dot(waypoints.get(index).dot()); //startPoint;
		this.endPoint = null;
		this.painter = null;
		this.index = index;
	}
	
	/**
	 * @return Whether or not the operation was successful. In the
	 * case of a failure, ensure that the end point was set before
	 * attempting execution.
	 */
	public boolean execute() {
		if(endPoint == null) {
			//TODO - CP - Throw Log here no endpoint here
			return false;
		}
		
		waypoints.set(endPoint, index);
		
		return true;
	}
	
	@Override
	public boolean undo() {
		waypoints.set(startPoint, index);
		return repaint();
	}
	
	@Override
	public boolean redo() {
		return execute();
	}
}