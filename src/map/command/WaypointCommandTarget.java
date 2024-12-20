package com.map.command;

import com.map.WaypointList;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date 10-13-20
 * Description: Command responsible for setting a rover's target waypoint.
 */
public class WaypointCommandTarget extends WaypointCommand {
    protected int newTarget;
    protected int prevTarget;

    /**
     * Constructor
     *
     * @param waypoints - The list of current navigational waypoints.
     */
    public WaypointCommandTarget(WaypointList waypoints) {
        super(waypoints, CommandType.TARGET);

        this.prevTarget = waypoints.getTarget();
        this.newTarget = waypoints.getSelected();
    }

    /**
     * Sets the waypoint target to the currently selected one.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean execute() {
        waypoints.setTarget(newTarget);
        return true;
    }

    /**
     * Sets the waypoint target to where it was previous to this command.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean undo() {
        waypoints.setTarget(prevTarget);
        return true;
    }

    /**
     * See execute.
     *
     * @return - Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean redo() {
        return execute();
    }
}
