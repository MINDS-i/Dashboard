package com.map;

import com.map.Waypoint;
import com.map.Dot;

import java.util.*;

public class WaypointList {
	
	/**
	 * Listener class for waypoint events.
	 */
    public static class WaypointListener {
        public enum Source { LOCAL, REMOTE }
        public enum Action { ADD, SET, DELETE }
        
        /**
         * Override unusedEvent to have a method called from all ather handlers
         * that were not overriden
         */
        public void unusedEvent() {}
        public void selectionChanged(int selection) { unusedEvent(); }
        public void roverMoved(WaypointListener.Source s, Dot p) { unusedEvent(); }
        public void homeMoved(WaypointListener.Source s, Dot p) { unusedEvent(); }
        public void changed(WaypointListener.Source s, Dot p, int index, Action a) { unusedEvent(); }
        public void targetChanged(WaypointListener.Source s, int target) { unusedEvent(); }
        public void loopModeSet(WaypointListener.Source s, boolean isLooped) { unusedEvent(); }
    }

    //Constants
    private final static int EXTENDED_INDEX_START = -2;
    
    //Vars
    private final List<Dot> waypoints = new LinkedList<Dot>();
    private final List<WaypointListener> listeners = new LinkedList<WaypointListener>();
    private Dot roverLocation = new Dot();
    private Dot homeLocation = new Dot();
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
    public int getExtendedIndexStart() { 
    	return EXTENDED_INDEX_START; 
    }
    
    /**
     * Return the waypoint at location `index`
     * Throws IndexOutOfBoundsException if there is no waypoint at that index
     */
    public ExtendedWaypoint get(int index) {
        switch(index){
            case -2: //home
                return new ExtendedWaypoint(){
                    public Dot dot(){ return new Dot(homeLocation); }
                    public Type type(){ return Type.HOME; }
                };
            case -1: //rover
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
        setRover(p, WaypointListener.Source.LOCAL);
    }
    public void setRover(Dot p, WaypointListener.Source s){
        roverLocation = p;
        listeners.stream().forEach(l -> l.roverMoved(s, p));
    }
    
    /**
     * Get the rover's location
     */
    public Dot getRover(){
        return new Dot(roverLocation);
    }
    
    /**
     * Set the home's location
     */
    public void setHome(Dot p){
        setHome(p, WaypointListener.Source.LOCAL);
    }
    public void setHome(Dot p, WaypointListener.Source s){
        homeLocation = p;
        listeners.stream().forEach(l -> l.homeMoved(s, p));
    }
    
    /**
     * Get the home's location
     */
    public Dot getHome(){
        return new Dot(homeLocation);
    }
    
    /**
     * Add a waypoint so it occures at position `index` in the Waypoint List
     * if index is greater than the current length of the list, an
     * IndexOutOfBoundsException is thrown.
     */
    public void add(Dot p, int index) {
        add(p, index, WaypointListener.Source.LOCAL);
    }
    public void add(Dot p, int index, WaypointListener.Source s){
        waypoints.add(index, p);
        if(index <= selectedIndex) setSelected(selectedIndex + 1);
        if(index <= targetIndex) setTarget(targetIndex + 1);
        listeners.stream().forEach(l -> l.changed(s, p, index, WaypointListener.Action.ADD));
    }
    
    /**
     *
     * Throws IndexOutOfBoundsException if there is no waypoint at that index
     */
    public void set(Dot p, int index){
        set(p, index, WaypointListener.Source.LOCAL);
    }
    public void set(Dot p, int index, WaypointListener.Source s){
        waypoints.set(index, p);
        listeners.stream().forEach(l -> l.changed(s, p, index, WaypointListener.Action.SET));
    }
    
    /**
     * Return the waypoint at location `index`
     * Throws IndexOutOfBoundsException if there is no waypoint at that index
     */
    public void remove(int index){
        remove(index, WaypointListener.Source.LOCAL);
    }
    public void remove(int index, WaypointListener.Source s){
        Dot p = waypoints.get(index);
        waypoints.remove(index);
        if(index <= selectedIndex) setSelected(selectedIndex - 1);
        if(index <= targetIndex) setTarget(targetIndex - 1);
        listeners.stream().forEach(l -> l.changed(s, p, index, WaypointListener.Action.DELETE));
    }
    
    /**
     * Set the waypoint looped status and inform listeners if it changed
     */
    public void setLooped(boolean state){
        setLooped(state, WaypointListener.Source.LOCAL);
    }
    public void setLooped(boolean state, WaypointListener.Source s){
        if(isLooped == state) return;
        isLooped = state;
        listeners.stream().forEach(l -> l.loopModeSet(s, state));
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
        setTarget(index, WaypointListener.Source.LOCAL);
    }
    public void setTarget(int index, WaypointListener.Source s){
        if(index == targetIndex || // only fire events when actually changed
           index <  0 ||
           index >= waypoints.size()) return;
        targetIndex = index;
        listeners.stream().forEach(l -> l.targetChanged(s, index));
    }
    
    /**
     * Return the current target index
     */
    public int getTarget(){
        return targetIndex;
    }
    
    /**
     * Set the Selected Waypoint index and inform listeners if the target changed.
     * If the end of the list is reached from either side, the selected index wraps
     * around to the other end of the list.
     */
    public void setSelected(int index) {
    	
    	//If this index is already selected, return
        if(index == selectedIndex) {
        	return;
        }
        
          //Case 1: If at start of list, wrap to end
        if(index < EXTENDED_INDEX_START) {
        	selectedIndex = (waypoints.size() -1);
        } //Case 2: If at end of index list, wrap to beginning
        else if(index >= waypoints.size()) {
        	selectedIndex = EXTENDED_INDEX_START;
        } //Case 3: Set the index normally
        else {
        	selectedIndex = index;
        }
        
        //Update listeners
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
    
    /**
     * Cleares the waypoint list, passing source s through to listeners
     */
    public void clear(WaypointListener.Source s){
        while (!waypoints.isEmpty()) {
            remove(0, s);
        }
    }
}
