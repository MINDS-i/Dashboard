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
	
	/**
	 * Constructor
	 * @param waypoints - The current list of navigational waypoints
	 * @param type - The Type of command being created
	 */
	public WaypointCommand(WaypointList waypoints, CommandType type) {
		this.waypoints = waypoints;
		this.type = type;
	}
	
	//Abstract functions to be overriden in concrete classes.
	public abstract boolean execute();
	public abstract boolean undo();
	public abstract boolean redo();
	
	/**
	 * 
	 * @return CommandType - The command type of this command instance.
	 */
	public CommandType getType() {
		return type;
	}

	/**
	 * Sets the final point for a movement command, making it ready to be executed.
	 * If this point is not set, then the corresponding move command will not execute.
	 * @param ep - The ending point to be moved to.
	 */
	public void finalize(Dot ep) {
		this.endPoint = ep; 
	}
}