package com.map.command;

import com.Context;
import com.map.WaypointList.*;
import com.map.command.WaypointCommand.CommandType;
import com.map.WaypointList;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for moving a waypoint from one position
 * to another.
 */
public class WaypointCommandClear extends WaypointCommand {
	protected WaypointList waypointsBackup;
	protected Context context;
	
	public WaypointCommandClear(WaypointList waypoints, Context context) {
		super(waypoints, CommandType.CLEAR);
		this.context = context;
	}
	
	@Override
	public boolean execute() {
		//TODO - CP - Back up everything wiped out in clear event for undo
			//Backup waypoints
		
		
		
		//Event is labeled as coming from the rover to avoid sending
		//waypoint update messages for every point. Explicit clear message
		//to the rover afterwards.
		waypoints.clear(WaypointListener.Source.REMOTE);
		
		if(context.sender != null) {
			context.sender.sendWaypointList();
		}
		
		return true;
	}
	
	@Override
	public boolean undo() {
		
		//Add each waypoint from the backup one by one?
		//Making sure that any coventions performed by an add are
		//correctly followed. (maybe just use the add command?)
		
		//swap the waypoints back in and possibly repaint?
		
		//TODO - CP - Remove these once undo for clear is implemnented fully.
		System.err.print("WaypointCommandClear - No implementation for ");
		System.err.println("undo exists.");
		return false;
	}
	
	@Override
	public boolean redo() {
		return execute();
	}
}
