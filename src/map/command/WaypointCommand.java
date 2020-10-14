package com.map.command;

import com.map.WaypointList;

import java.awt.Component;

import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Abstract class defining the base structure of a
 * WaypointCommand. New commands should use this parent structure
 * as the baseline requirement for insuring system compatability and
 * functionality.
 */
public abstract class WaypointCommand {

	public enum CommandType {ADD, REMOVE, MOVE, EDIT, CLEAR, TARGET};
	
	protected WaypointList waypoints;
	protected CommandType type;
	
	// Add/Remove Vars
	protected Dot point;
	protected int index;
	
	// Move Vars
	protected Dot startPoint;
	protected Dot endPoint;
	
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

	public void finalize(Dot ep) {
		this.endPoint = ep; 
	}
}