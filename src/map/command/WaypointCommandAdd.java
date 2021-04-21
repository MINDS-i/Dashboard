package com.map.command;

import javax.swing.*;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;
import com.util.UtilHelper;



/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for adding a waypoint to the
 * active sessions list.
 */
public class WaypointCommandAdd extends WaypointCommand {
	private static final double MIN_DISTANCE_FT = 45.00;
	private static final String WARN_STRING = "For optimal results, a minimum" 
			+ " distance of " + MIN_DISTANCE_FT + " feet between waypoints" 
			+ " is recommended.";
	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints.
	 * @param point - Waypoint to be manually edited by this command
	 * @param index - index in the waypoint list of the waypoint being added.
	 */
	public WaypointCommandAdd(WaypointList waypoints, Dot point, int index) {
		super(waypoints, CommandType.ADD);
		
		this.point = point;
		this.index = index;
	}
	
	/**
	 * Adds a new waypoint at the specified location. 
	 * @return Boolean - Whether or not the command was executed successfully.
	 */
	@Override
	public boolean execute() {
		waypoints.add(point, index);
		
		// If this point was added to an existing line
		// at index 0, make it the new target of the rover. 
		if(index == 0) {
			waypoints.setTarget(index);
		}
		
		waypoints.setSelected(index);			
		warnMinimumDistance(index);

		return true;
	}
	
	/**
	 * Removes the added waypoint described by this command.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		waypoints.remove(index);
		
		return true;
	}
	
	/**
	 * Re-adds the detailed waypoint to the list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
	
	/**
	 * Check if the distance between the waypoint at index and the previous
	 * waypoint (if there is one) are greater than a minimum recommended distance
	 * (MIN_DIST_FT). If they are not, then provide a dialog box informing the user
	 * that they should increase the distance for best performance.
	 * @param index - the index of the most recent waypoint.
	 */
	public void warnMinimumDistance(int index) {
		Dot waypointA;
		Dot waypointB;
		double distance;
		
		if(index > 0 ) {
			waypointA = waypoints.get(index - 1).dot();
			waypointB = waypoints.get(index).dot();
			
			distance = UtilHelper.getInstance().haversine(waypointA, waypointB);
			distance = UtilHelper.getInstance().kmToFeet(distance);
			
			if(distance < MIN_DISTANCE_FT) {
				JOptionPane.showMessageDialog(null, WARN_STRING);
			}
		}
	}
}
