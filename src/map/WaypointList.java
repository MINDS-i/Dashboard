package com.map;

import com.Dashboard;
import com.xml;
import com.map.*;
import com.serial.*;
import com.Context;

import java.util.Vector;
import java.awt.Point;
import java.util.Iterator;

public class WaypointList{
	private Vector<Dot> waypoints = new Vector<Dot>();
	private Context context;
	public WaypointList(Context cxt){
		context = cxt;
	}
	public void changeContext(Context cxt){
		context = cxt;
	}
	public void add(Dot newDot){
		add(newDot, waypoints.size());
	}
	public void add(double longitude, double latitude, int index){
		Dot tmp = new Dot(new Point.Double(longitude, latitude));
		add(tmp, index);
	}
	public void add(double longitude, double latitude, short altitude, int index){
		Dot tmp = new Dot(new Point.Double(longitude, latitude), altitude);
		add(tmp, index);
	}
	public void add(Dot newDot, int index){
        if(waypoints.size()>= Serial.MAX_WAYPOINTS) return;
        if(newDot.getAltitude()==0 && index > 0) {
            newDot.setAltitude(context.waypoint.get(index-1).getAltitude());
        }
        waypoints.insertElementAt(newDot, index);
        sendWaypoint((byte)index, Serial.ADD_WAYPOINT_MSG);
        context.waypointUpdated();
	}
	public void set(int index, Point.Double newPosition){
		set(index, newPosition, waypoints.get(index).getAltitude());
	}
	public void set(int index, Point.Double newPosition, short alt){
		if(index < 0 || index >= waypoints.size()) return;
		waypoints.get(index).setLocation(newPosition, alt);
		sendWaypoint((byte)index, Serial.CHANGE_WAYPOINT_MSG);
		context.waypointUpdated();
	}
	public void remove(int index){
		if(index < 0 || index >= waypoints.size()) return;
		waypoints.remove(index);
		sendWaypoint((byte)index, Serial.DELETE_WAYPOINT_MSG);
		context.waypointUpdated();
	}
	public Dot get(int index){
		return waypoints.get(index);
	}
	public int size(){
		return waypoints.size();
	}
	public Iterator iterator(){
		return waypoints.iterator();
	}
	public boolean isLooped(){
		return (context.data[Serial.LOOPING_TOGGLE]==0)?(false):(true);
	}
	public void setLooped(boolean loop){
		context.data[Serial.LOOPING_TOGGLE] = (loop)?(1.f):(0.f);
		sendDataMsg(Serial.LOOPING_TOGGLE);
		context.waypointUpdated();
	}
	public int getTarget(){
		return (int) context.data[Serial.TARGET_INDEX];
	}
	public Dot getTargetWaypoint(){
		return waypoints.get((int)context.data[Serial.TARGET_INDEX]);
	}
	public void setTarget(int target){
		context.data[Serial.TARGET_INDEX] = (float) target;
		sendDataMsg(Serial.TARGET_INDEX);
		context.waypointUpdated();
	}
	public void swap(Vector<Dot> newList){
		waypoints = newList;
	}
	private void sendDataMsg(int index){
        if(context.sender != null){
            Message msg = new Message((byte)index, context.data[index] );
            context.sender.sendMessage(msg);
        }
	}
	private void sendWaypoint(int index, byte label){
		if(context.connected){
		    Message msg = new Message(label, waypoints.get(index), (byte)index);
		    context.sender.sendMessage(msg);
		}
	}
}
