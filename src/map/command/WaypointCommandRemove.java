package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for removing a waypoint from the
 * active sessions list.
 */
public class WaypointCommandRemove extends WaypointCommand {
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints.
	 * @param index - Index of the waypoint effected by this command.
	 */
	public WaypointCommandRemove(WaypointList waypoints, int index) {
		super(waypoints, CommandType.REMOVE);
		
		this.index = index;
		this.point = waypoints.get(index).dot();
	}
	
	/**
	 * Removes the waypoint at the selected index from the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean execute() {
		waypoints.remove(index);
		
		return true;
	}
	
	/**
	 * Adds the waypoint selected by this removal back into the waypoint list at its original
	 * location.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		waypoints.add(point, index);
		waypoints.setSelected(index);
		
		// If this point was added to an existing line
		// at index 0, make it the new target of the rover. 
		if(index == 0) {
			waypoints.setTarget(index);
		}
		
		return true;
	}
	
	/**
	 * Re-executes the previous removal operation after an undo.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
}
