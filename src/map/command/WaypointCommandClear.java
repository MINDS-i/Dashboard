package com.map.command;

import com.Context;
import com.map.WaypointList.*;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.WaypointList;
import com.map.Dot;
import com.serial.SerialSendManager;
import com.util.SwathProperties;

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
		CommandManager commandManager = CommandManager.getInstance();

		//Event is labeled as coming from the rover to avoid sending
		//waypoint update messages for every point. An explicit clear message
		//is sent to the rover afterwards.
		waypoints.clear(WaypointListener.Source.REMOTE);
		commandManager.getGeofence().setIsEnabled(false);
		
		//Send the empty list
		SerialSendManager.getInstance().sendWaypointList(waypoints);
		SerialSendManager.getInstance().changeMovement(false);
		
		//TODO - CP - Check for swath type and set appropriate state here

		return true;
	}
	
	/**
	 * Restores the waypoint list to its previous state before being cleared.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		CommandManager manager = CommandManager.getInstance();
		
		//Add each waypoint from the backup one by one
		for(int i = 0; i < waypointsBackup.size(); i++) {
			
			//If this is the origin point for the geofence
			if(i == 0) {				
				manager.getGeofence().setOriginLatLng(
						waypointsBackup.get(i).dot().getLatitude(),
						waypointsBackup.get(i).dot().getLongitude());
				manager.getGeofence().setIsEnabled(true);
			}
			
			waypoints.add(waypointsBackup.get(i).dot(), i);
		}
		
		//TODO - CP - Check for swath type and set appropriate state here
		
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
