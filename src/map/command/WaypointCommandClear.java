package com.map.command;

import com.Context;
import com.map.WaypointList.*;
import com.map.command.WaypointCommand.CommandType;
import com.map.WaypointList;
import com.map.Dot;

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
		waypointsBackup = new WaypointList();
		
		//Backup the list for later redo
		for(int i = 0; i < waypoints.size(); i++) {
			waypointsBackup.add(waypoints.get(i).dot(), i);
		}
	}
	
	@Override
	public boolean execute() {
		//Event is labeled as coming from the rover to avoid sending
		//waypoint update messages for every point. An explicit clear message
		//is sent to the rover afterwards.
		waypoints.clear(WaypointListener.Source.REMOTE);
		
		if(context.sender != null) {
			context.sender.sendWaypointList();
		}
		
		return true;
	}
	
	@Override
	public boolean undo() {
		
		//Add each waypoint from the backup one by one?
		for(int i = 0; i < waypointsBackup.size(); i++) {
			waypoints.add(waypointsBackup.get(i).dot(), i);
		}
		
		return true;
	}
	
	@Override
	public boolean redo() {
		return execute();
	}
}
