package com.map.command;

import com.map.Dot;
import com.map.WaypointList;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for moving a waypoints location.
 */
public class WaypointCommandMove extends WaypointCommand {
    //Warning Strings
    private static final String WARN_NO_GEOFENCE_INTERSECT =
            "WP Move - Waypoint placement"
                    + " exceeds geofence. Canceling movement.";

    private static final String WARN_ENDPOINT_NOT_SET =
            "WP Move - Execution failure."
                    + " Endpoint was not set.";

    private static final String WARN_ORIGIN_CANNOT_MOVE =
            "WP Move - Cannot move origin point."
                    + " Waypoints must first be cleared.";

    /**
     * Constructor
     *
     * @param waypoints - list of current navigation waypoints
     * @param index     -index in the waypoint list of the waypoint being moved.
     */
    public WaypointCommandMove(WaypointList waypoints, int index) {
        super(waypoints, CommandType.MOVE);

        this.startPoint = new Dot(waypoints.get(index).dot());
        this.endPoint = null;
        this.index = index;
    }

    /**
     * Moves the point to the endpoint detailed by the finalize method
     * (See WaypointCommand Class for definition)
     *
     * @return Whether or not the operation was successful. In the
     * case of a failure, ensure that the end point was set before
     * attempting execution.
     */
    @Override
    public boolean execute() {
        CommandManager manager = CommandManager.getInstance();

        //If no endpoint set
        if (endPoint == null) {
            System.err.println(WARN_ENDPOINT_NOT_SET);
            return false;
        }

        if (manager.getGeofence().getIsEnabled()) {
            //If an attempt is made to move the origin, abort the move.
            if (index == 0) {
                serialLog.warning(WARN_ORIGIN_CANNOT_MOVE);
                return false;
            }

            //If not the geofence origin and the endpoint is outside of
            //the current fence, abort the move.
            if (!manager.getGeofence().doesLocationIntersect(endPoint)) {
                serialLog.warning(WARN_NO_GEOFENCE_INTERSECT);
                return false;
            }
        }

        waypoints.set(endPoint, index);
        return true;
    }

    /**
     * Returns the waypoint outlined by this command to its original position
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean undo() {
        waypoints.set(startPoint, index);
        return true;
    }

    /**
     * Moves the waypoint back to its modified position again.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean redo() {
        return execute();
    }
}
