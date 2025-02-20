package com.map.command;

import com.map.WaypointList;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Command responsible for removing a waypoint from the
 * active sessions list.
 */
public class WaypointCommandRemove extends WaypointCommand {

    /**
     * Constructor
     *
     * @param waypoints - List of current navigational waypoints.
     * @param index     - Index of the waypoint effected by this command.
     */
    public WaypointCommandRemove(WaypointList waypoints, int index) {
        super(waypoints, CommandType.REMOVE);

        this.index = index;
        this.point = waypoints.get(index).dot();
    }

    /**
     * Removes the waypoint at the selected index from the waypoint list.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean execute() {
        CommandManager manager = CommandManager.getInstance();

        //If this is the initial waypoint, remove the geofence
        if (index == 0) {
            manager.getGeofence().setIsEnabled(false);
        }

        waypoints.remove(index);

        return true;
    }

    /**
     * Adds the waypoint selected by this removal back into the waypoint list
     * at its original location.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean undo() {
        CommandManager manager = CommandManager.getInstance();

        waypoints.add(point, index);
        waypoints.setSelected(index);

        if (index == 0) {
            //Update the geofence coordinates at the first index.
            manager.getGeofence().setOriginLatLng(
                    point.getLatitude(), point.getLongitude());
            manager.getGeofence().setIsEnabled(true);

            waypoints.setTarget(index);
        }

        return true;
    }

    /**
     * Re-executes the previous removal operation after an undo.
     *
     * @return Boolean - Whether or not the operation was successful.
     */
    @Override
    public boolean redo() {
        return execute();
    }
}
