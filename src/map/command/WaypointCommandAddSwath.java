package com.map.command;

import com.ui.widgets.SwathPreviewWidget.SwathType;
import com.ui.widgets.SwathPreviewWidget.SwathRotation;
import com.ui.widgets.SwathPreviewWidget.SwathInversion;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.*;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Command responsible for adding a swath path to the
 * active session list.
 */
public class WaypointCommandAddSwath extends WaypointCommand {

	//Member Vars
	protected List<Dot> swathPoints;

	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints
	 * @param point - Starting point for the swath to be built off of.
	 * @param index - Index in the waypoint list to insert the swathPoints list
	 * @param type - The orientation type (Horizontal/Vertical) of the swath
	 * @param rotation - The rotation of the swath (None/Rotation (90 degrees))
	 * @param inversion - The inversion of the swath (Inverted/Not Inverted)
	 */
	public WaypointCommandAddSwath(
			WaypointList waypoints, Dot point, int index,
			SwathType type, SwathRotation rotation, SwathInversion inversion) {
		super(waypoints, CommandType.ADD_SWATH);
		
		//Starting index of the list of swath points.
		this.index = index;
		
		//TODO - CP - Generate list of swath points with given info here.
		
	}
	
	/**
	 * Adds the entire list of swathPoints for the related swath pattern
	 * to the waypoint list at the specified location.
	 * @return Boolean - Whether or not the command was executed successfully.
	 */
	@Override
	public boolean execute() {
		int currIndex = this.index;
		CommandManager manager = CommandManager.getInstance();

		//Check for any waypoint intersections first
		for(Dot point : swathPoints) {
			if(!manager.getGeofence().doesLocationIntersect(point)) {
				serialLog.warning(WARN_NO_GEOFENCE_INTERSECT);
				return false;
			}
		}
		
		//For each point in swathPoints List
		for(Dot point : swathPoints) {
			//If the waypoint max hasn't been reached yet
			if(waypoints.size() < MAX_WAYPOINTS) {
				waypoints.add(point, currIndex);
				currIndex++;
			}
		}
		
		return true;
	}
	
	/**
	 * Removes the related swathPoints list from the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		int currIndex = (this.index + swathPoints.size());
		CommandManager manager = CommandManager.getInstance();
		
		//For each index starting at the end of the swath path
		//and continuing until the insertion index is reached...
		for(int i = currIndex; i < this.index; i--) {
			waypoints.remove(i);
		}
		
		return true;
	}
	
	/**
	 * Re-adds the swathPoints list to the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	public boolean redo() {
		return execute();
	}
}

//TODO - CP - Create Swath patterns routines to execute from the AddSwath
//command.
	//Horizontal
	//Horizontal rotate 90
	//More after proofing that out
