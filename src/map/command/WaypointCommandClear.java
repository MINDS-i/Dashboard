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
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints.
	 * @param context - Application context
	 */
	public WaypointCommandClear(WaypointList waypoints, Context context) {
		super(waypoints, CommandType.CLEAR);
		this.context = context;
		waypointsBackup = new WaypointList();
		
		//Backup the list for later redo
		for(int i = 0; i < waypoints.size(); i++) {
			waypointsBackup.add(waypoints.get(i).dot(), i);
		}
	}
	
	/**
	 * Clears the waypoint list.
	 * @return Boolean - Whether or not the command was successful.
	 */
	@Override
	public boolean execute() {
		//Event is labeled as coming from the rover to avoid sending
		//waypoint update messages for every point. An explicit clear message
		//is sent to the rover afterwards.
		waypoints.clear(WaypointListener.Source.REMOTE);
		
		if(context.sender != null) {
			context.sender.sendWaypointList();
			context.sender.changeMovement(false);
		}
		
		return true;
	}
	
	/**
	 * Restores the waypoint list to its previous state before being cleared.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		//TODO - CP - GEOFENCE - The Geofence will need to be re-added here also.
		
		//Add each waypoint from the backup one by one?
		for(int i = 0; i < waypointsBackup.size(); i++) {
			waypoints.add(waypointsBackup.get(i).dot(), i);
		}
		
		return true;
	}
	
	/**
	 * Re-clears the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
}
