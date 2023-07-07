package com.map.command;

import java.util.List;

import com.Context;
import com.map.WaypointList.*;
import com.map.WaypointType;
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
		SwathProperties swathProperties = SwathProperties.getInstance();
		List<Dot> pointList = waypoints.getPoints();
		
		//Loop through the list of points before clearing them
		//If a swath pattern was in the list, set the trackable
		//states in swath properties so that we can properly restore it
		//if an undo operation happens.
		for(Dot point : pointList) {
			if(point.getWaypointType() == WaypointType.SWATH) {
				swathProperties.setPreviousSwathPlacedState(true);
				swathProperties.setIsSwathPlaced(false);
			}
		}
		
		//Event is labeled as coming from the rover to avoid sending
		//waypoint update messages for every point. An explicit clear message
		//is sent to the rover afterwards.
		waypoints.clear(WaypointListener.Source.REMOTE);
		commandManager.getGeofence().setIsEnabled(false);
		
		//Send the empty list
		SerialSendManager.getInstance().sendWaypointList(waypoints);
		SerialSendManager.getInstance().changeMovement(false);
		
		return true;
	}
	
	/**
	 * Restores the waypoint list to its previous state before being cleared.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		CommandManager manager = CommandManager.getInstance();
		SwathProperties swathProperties = SwathProperties.getInstance();
	
		//If a swath pattern exists and was previously placed inside the
		//waypoint backup list. Set placement states to prevent additional
		//patterns from being placed after restoring the existing one.
		if(swathProperties.getPreviousSwathPlacedState()) {
			swathProperties.setIsSwathPlaced(true);
			swathProperties.setPreviousSwathPlacedState(false);
		}
		
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
