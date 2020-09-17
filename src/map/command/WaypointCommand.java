package com.map.command;

import com.map.WaypointList;
import com.map.Dot;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description:
 *
 */
public abstract class WaypointCommand {

	public enum CommandType {ADD, REMOVE, MOVE, CLEAR}
	
	protected WaypointList waypoints;
	protected CommandType type;
	protected Dot point;
	protected int index;
	protected boolean isTarget;

	public WaypointCommand(WaypointList waypoints, CommandType type) {
		this.waypoints = waypoints;
		this.type = type;
	}
		
	public abstract boolean execute();
	public abstract boolean undo();
	public abstract boolean redo();
	
	public CommandType getType() {
		return type;
	}
	
}