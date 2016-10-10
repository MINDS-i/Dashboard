package com.map;

import com.map.Waypoint;
import com.map.Dot;

import java.util.*;

public class WaypointList {
    public static class WaypointListener{
        public enum Action { ADD, SET, DELETE }
        /**
         * Override unusedEvent to have a method called from all ather handlers
         * that were not overriden
         */
        public void unusedEvent() {}
        public void roverMoved(Dot p) { unusedEvent(); }
        public void changed(Dot p, int index, Action a) { unusedEvent(); }
        public void targetChanged(int target) { unusedEvent(); }
        public void selectionChanged(int selection) { unusedEvent(); }
        public void loopModeSet(boolean isLooped) { unusedEvent(); }
    }

    private final List<Dot> waypoints = new LinkedList<Dot>();
    private final List<WaypointListener> listeners = new LinkedList<WaypointListener>();
    private Dot roverLocation = new Dot();
    private boolean isLooped = false;
    private int targetIndex = 0;
    private int selectedIndex = -1;

    public WaypointList() {}

    public enum Type{
        ROVER, HOME, WAYPOINT, SELECTED
    }
    public interface ExtendedWaypoint{
        Dot dot();
        Type type();
    }

    /**
     * The index location to start calling index at so that extended waypoints
     * like the rover's location and home position get added to the list
     */
    public int extendedIndexStart() { return -1; }
    /**
     * Return the waypoint at location `index`
     * Throws IndexOutOfBoundsException if there is no waypoint at that index
     */
    public ExtendedWaypoint get(int index) {
        switch(index){
            case -1: //home
                return new ExtendedWaypoint(){
                    public Dot dot(){ return new Dot(roverLocation); }
                    public Type type(){ return Type.ROVER; }
                };
            default:
                return new ExtendedWaypoint(){
                    public Dot dot(){ return new Dot(waypoints.get(index)); }
                    public Type type(){
                        return (index == selectedIndex)
                                    ? Type.SELECTED
                                    : Type.WAYPOINT;
                    }
                };
        }
    }
    /**
     * Return the current number of waypoints
     */
    public int size() {
        return waypoints.size();
    }
    /**
     * Set the rover's location
     */
    public void setRover(Dot p){
        roverLocation = p;
        listeners.stream().forEach(l -> l.roverMoved(p));
    }
    /**
     * Get the rover's location
     */
    public Dot getRover(){
        return new Dot(roverLocation);
    }
    /**
     * Add a waypoint so it occures at position `index` in the Waypoint List
     * if index is greater than the current length of the list, an
     * IndexOutOfBoundsException is thrown.
     */
    public void add(Dot p, int index){
        waypoints.add(index, p);
        if(index <= selectedIndex) setSelected(selectedIndex + 1);
        if(index <= targetIndex) setTarget(targetIndex + 1);
        listeners.stream().forEach(l -> l.changed(p, index, WaypointListener.Action.ADD));
    }
    /**
     *
     * Throws IndexOutOfBoundsException if there is no waypoint at that index
     */
    public void set(Dot p, int index){
        waypoints.set(index, p);
        listeners.stream().forEach(l -> l.changed(p, index, WaypointListener.Action.SET));
    }
    /**
     * Return the waypoint at location `index`
     * Throws IndexOutOfBoundsException if there is no waypoint at that index
     */
    public void remove(int index){
        Dot p = waypoints.get(index);
        waypoints.remove(index);
        if(index <= selectedIndex) setSelected(selectedIndex - 1);
        if(index <= targetIndex) setTarget(targetIndex - 1);
        listeners.stream().forEach(l -> l.changed(p, index, WaypointListener.Action.DELETE));
    }
    /**
     * Set the waypoint looped status and inform listeners if it changed
     */
    public void setLooped(boolean state){
        if(isLooped == state) return;
        isLooped = state;
        listeners.stream().forEach(l -> l.loopModeSet(state));
    }
    /**
     * Return if waypoints are currently looped
     */
    public boolean getLooped(){
        return isLooped;
    }
    /**
     * Set the target index and inform listeners if the target changed
     */
    public void setTarget(int index){
        if(index == targetIndex || // only fire events when actually changed
           index <  0 ||
           index >= waypoints.size()) return;
        targetIndex = index;
        listeners.stream().forEach(l -> l.targetChanged(index));
    }
    /**
     * Return the current target index
     */
    public int getTarget(){
        return targetIndex;
    }
    /**
     * Set the Selected Waypoint index and inform listeners if the target changed
     */
    public void setSelected(int index){
        if(index == selectedIndex || // only fire events when actually changed
           index <  extendedIndexStart() ||
           index >= waypoints.size()) return;
        selectedIndex = index;
        listeners.stream().forEach(l -> l.selectionChanged(index));
    }
    /**
     * Return the current target index
     */
    public int getSelected(){
        return selectedIndex;
    }
    /**
     * Register a new WaypointListener
     */
    public void addListener(WaypointListener l){
        listeners.add(l);
    }
    /**
     * Removes the first occurance of a listener .equals to `l`
     */
    public void removeListener(WaypointListener l){
        listeners.remove(l);
    }
}
