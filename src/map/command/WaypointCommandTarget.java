package com.map.command;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date 10-13-20
 * Description: Command responsible for setting a rover's target waypoint.
 *
 */
public class WaypointCommandTarget extends WaypointCommand {
	protected int newTarget;
	protected int prevTarget;

	public WaypointCommandTarget(WaypointList waypoints) {
		super(waypoints, CommandType.TARGET);

		this.prevTarget = waypoints.getTarget();
		this.newTarget = waypoints.getSelected();
	}


	@Override
	public boolean execute() {
		waypoints.setTarget(newTarget);
		return true;
	}

	@Override
	public boolean undo() {
		waypoints.setTarget(prevTarget);
		return true;
	}

	@Override
	public boolean redo() {
		return execute();
	}
}
