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

	public enum CommandType {ADD, REMOVE, MOVE, CLEAR}
	
	protected WaypointList waypoints;
	protected CommandType type;
	
	// Add/Remove Vars
	protected Dot point;
	protected boolean isTarget;
	protected int index;
	
	// Move Vars
	protected Dot startPoint;
	protected Dot endPoint;
	protected Component painter;
	
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
	
	public void finalize(Dot ep, Component painter) {
		this.endPoint = ep; 
		this.painter = painter;
	}
	
	protected boolean repaint() {
		
		if(painter == null) {
			//TODO - CP - throw log here, not being used by a 
			//paintable command
			return false;
		}
		
		painter.repaint();
		return true;
	}
}