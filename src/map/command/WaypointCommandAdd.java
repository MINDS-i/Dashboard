package com.map.command;

import com.map.Dot;
import com.map.WaypointList;
import com.util.UtilHelper;

import javax.swing.*;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for adding a waypoint to the
 * active sessions list.
 */
public class WaypointCommandAdd extends WaypointCommand {
    //Constants
    private static final double MIN_DISTANCE_FT = 45.00;
    private static final int MAX_WAYPOINTS = 63;

    //Warning Strings
    private static final String WARN_MIN_DISTANCE_DIALOG =
            "For optimal results, a minimum"
                    + " distance of " + MIN_DISTANCE_FT + " feet between waypoints"
                    + " is recommended.";

    private static final String WARN_GEOFENCE_ALREADY_PLACED =
            "WP Add - Geofence already in place."
                    + " Cannot add waypoint at index 0.";

    private static final String WARN_NO_GEOFENCE_INTERSECT =
            "WP Add - Waypoint placement"
                    + " exceeds geofence. Canceling placement.";

    private static final String WARN_MAX_WAYPONTS_REACHED =
            "WP Add - Maximum waypoints reached.";

    /**
     * Constructor
     *
     * @param waypoints - List of current navigational waypoints.
     * @param point     - Waypoint to be manually edited by this command
     * @param index     - index in the waypoint list of the waypoint being added.
     */
    public WaypointCommandAdd(WaypointList waypoints, Dot point, int index) {
        super(waypoints, CommandType.ADD);

        this.point = point;
        this.index = index;
    }

    /**
     * Adds a new waypoint at the specified location.
     *
     * @return Boolean - Whether or not the command was executed successfully.
     */
    @Override
    public boolean execute() {
        CommandManager manager = CommandManager.getInstance();

        //If the maximum waypoints has been reached, do nothing
        if (waypoints.size() == MAX_WAYPOINTS) {
            serialLog.warn(WARN_MAX_WAYPONTS_REACHED);
            return false;
        }

        //If a geofence exists
        if (manager.getGeofence().getIsEnabled()) {
            //Check for and refuse a second index 0 placement
            if (index == 0) {
                serialLog.warn(WARN_GEOFENCE_ALREADY_PLACED);
                return false;
            }

            //Check for waypoint intersection
            if (!manager.getGeofence().doesLocationIntersect(point)) {
                serialLog.warn(WARN_NO_GEOFENCE_INTERSECT);
                return false;
            }
        }

        waypoints.add(point, index);

        if (index == 0) {
            //create the geofence at the first index.
            manager.getGeofence().setOriginLatLng(point.getLatitude(),
                    point.getLongitude());
            manager.getGeofence().setIsEnabled(true);

            waypoints.setTarget(index);
        }

        waypoints.setSelected(index);
        warnMinimumDistance(index);

        return true;
    }

    /**
     * Removes the added waypoint described by this command.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean undo() {
        CommandManager manager = CommandManager.getInstance();

        //If this is the initial waypoint. Remove the geofence
        if (index == 0) {
            manager.getGeofence().setIsEnabled(false);
        }

        waypoints.remove(index);

        return true;
    }

    /**
     * Re-adds the detailed waypoint to the list.
     *
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
     *
     * @param index - the index of the most recent waypoint.
     */
    public void warnMinimumDistance(int index) {
        Dot waypointA;
        Dot waypointB;
        double distance;

        if (index > 0) {
            waypointA = waypoints.get(index - 1).dot();
            waypointB = waypoints.get(index).dot();

            distance = UtilHelper.getInstance().haversine(waypointA, waypointB);
            distance = UtilHelper.getInstance().kmToFeet(distance);

            if (distance < MIN_DISTANCE_FT) {
                serialLog.debug(
                        "WaypointCommand Add - Waypoint placement of "
                                + distance
                                + " feet is less than recommended minimum of "
                                + MIN_DISTANCE_FT + " feet.");
                JOptionPane.showMessageDialog(null, WARN_MIN_DISTANCE_DIALOG);

            }
        }
    }
}
