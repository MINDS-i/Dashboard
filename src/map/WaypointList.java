package com.map;

import com.Dashboard;
import com.xml;
import com.map.*;
import com.serial.*;
import com.Context;
import com.serial.*;
import com.serial.Messages.*;

import java.util.Vector;
import java.awt.Point;
import java.util.Iterator;

public class WaypointList{
	private Vector<Dot> waypoints = new Vector<Dot>();
	private Context context;
	private boolean isLooped;
	private int targetIndex;
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
		sendWaypoint((byte)(index&0xff), Serial.ADD_SUBTYPE);
		context.waypointUpdated();
	}
	public void set(int index, Point.Double newPosition){
		set(index, newPosition, waypoints.get(index).getAltitude());
	}
	public void set(int index, Point.Double newPosition, Short alt){
		if(index < 0 || index >= waypoints.size()) return;
		waypoints.get(index).setLocation(newPosition, alt);
		sendWaypoint((byte)(index&0xff), Serial.ALTER_SUBTYPE);
		context.waypointUpdated();
	}
	public void remove(int index){
		if(index < 0 || index >= waypoints.size()) return;
		sendWaypoint((byte)(index&0xff), Serial.DELETE_SUBTYPE);
		waypoints.remove(index);
		context.waypointUpdated();
	}
	public Dot get(int index){
		return waypoints.get(index);
	}
	public Dot get(byte index){
		return waypoints.get((index&0xff));
	}
	public int size(){
		return waypoints.size();
	}
	public Iterator iterator(){
		return waypoints.iterator();
	}
	public boolean isLooped(){
		return isLooped;
	}
	public void setLooped(boolean loop){
		isLooped = loop;
		Message msg = new StandardMessage(Serial.LOOPING_CMD, (byte) ((loop)?1:0) );
        context.sender.sendMessage(msg);
		context.waypointUpdated();
	}
	public int getTarget(){
		return targetIndex;
	}
	public Dot getTargetWaypoint(){
		return waypoints.get(getTarget());
	}
	public void setTarget(int target){ //updates and transmits the target waypoint
		updateTarget(target);
		Message msg = new StandardMessage(Serial.TARGET_CMD, (byte) target);
        context.sender.sendMessage(msg);
	}
	public void updateTarget(int target){ //sets the target waypoint
		targetIndex = target;
		context.waypointUpdated();
	}
	public void swap(Vector<Dot> newList){
		waypoints = newList;
	}
	private void sendDataMsg(int index){
        if(context.sender != null){
            Message msg = new StandardMessage((byte)index,
            								context.upstreamSettings[index]);
            context.sender.sendMessage(msg);
        }
	}
	public void sendWaypoint(byte index, int type){
		if(context.connected){
		    Message msg = new WaypointMessage(
		    			type, (byte)(index&0xff), waypoints.get(index));
		    context.sender.sendMessage(msg);
		}
	}
}
