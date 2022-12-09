package com.map.command;

import java.util.logging.Logger;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;

import java.awt.Component;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-25-2020
 * Description: Command responsible for editing waypoint properties from the
 * Waypoint Panel editable telemtry fields.
 *
 */
public class WaypointCommandEdit extends WaypointCommand {
	//Warning Strings
	private static final String WARN_GEOFENCE_MOVE =
			  "WaypointCommand Edit - Geofence cannot be"
			+ " moved while other waypoints are defined. The list must"
			+ " first be cleared.";
	
	private static final String WARN_NO_GEOFENCE_INTERSECT = 
			  "WaypointCommand Move - Waypoint placement"
			+ " exceeds geofence. Canceling movement.";
	
	private static final String WARN_ENDPOINT_NOT_SET =
			  "WaypointCommand Edit - Execution failure."
			+ " Endpoint was not set.";
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints.
	 * @param index - index in the waypoint list of the waypoint being modified.
	 */
	public WaypointCommandEdit(WaypointList waypoints, int index) {
		super(waypoints, CommandType.EDIT);
		
		this.startPoint = new Dot(
				waypoints.get(index).dot().getLatitude(),
				waypoints.get(index).dot().getLongitude(),
				waypoints.get(index).dot().getAltitude());
		
		this.endPoint = null;
		this.index = index;
	} 
	
	/**
	 * Edits the manual details of the selected point.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean execute() {
		CommandManager manager = CommandManager.getInstance();
		
		if(endPoint == null) {
			System.err.println(WARN_ENDPOINT_NOT_SET);
			return false;
		}
		
		//Geofence checks (On enabled only)
		if(manager.getGeofence().getIsEnabled()) {
			//If moving the fence origin and there are other waypoints,
			//abort the move operation and alert the user.
			if((index == 0) && waypoints.size() > 1) {
				serialLog.warning(WARN_GEOFENCE_MOVE);
				return false;
			}
			
			//If not the geofence origin and the endpoint is outside of 
			//the current fence, abort the move.
			if(!manager.getGeofence().doesLocationIntersect(endPoint)) {
				serialLog.warning(WARN_NO_GEOFENCE_INTERSECT);
				return false;
			}
		}
		
		
		waypoints.set(endPoint, index);
		return true;
	}
	
	/**
	 * Reverts the manual changes made to a waypoint with this command.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		waypoints.set(startPoint, index);
		return true;
	}
	
	/**
	 * Re executes the manual changes to the waypoint specified by this command.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}	
}
