package com.map.command;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.*;

import com.util.SwathProperties;
import com.util.UtilHelper;

import com.ui.widgets.SwathPreviewWidget.SwathType;
import com.ui.widgets.SwathPreviewWidget.SwathInversion;

import com.map.WaypointList;
import com.map.WaypointType;
import com.map.command.WaypointCommand.CommandType;
import com.map.geofence.WaypointGeofence;
import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Command responsible for adding a swath path to the
 * active session list.
 */
public class WaypointCommandAddSwath extends WaypointCommand {

	//Constants
	private static final double SWATH_LENGTH_FT = 200;
	private static final double SWATH_WIDTH_FT	= 32;
	
	//Member Vars
	protected SwathProperties swathProperties;
	protected UtilHelper utilHelper;
	
	protected List<Dot> swathPoints;
	protected SwathType type;
	protected SwathInversion inversion;
	protected double lengthOffset;
	protected double widthOffset;
	
	/**
	 * Constructor
	 * @param waypoints - List of current navigational waypoints
	 * @param point - Starting point for the swath to be built off of.
	 * @param index - Index in the waypoint list to insert the swathPoints list
	 * @param type - The orientation type (Horizontal/Vertical) of the swath
	 * @param inversion - The inversion of the swath (Inverted/Not Inverted)
	 */
	public WaypointCommandAddSwath(
			WaypointList waypoints, Dot point, int index, 
			SwathType type, SwathInversion inversion) {
		super(waypoints, CommandType.ADD_SWATH);
		
		utilHelper = UtilHelper.getInstance();
		swathProperties = SwathProperties.getInstance();
		
		swathPoints = new ArrayList<Dot>();
		//Starting index of the list of swath points.
		this.point = point;
		this.index = index;
		this.type = type;
		this.inversion = inversion;
		
		//TODO - CP - FIX - Make sure we convert using both LAT and LNG here
		//Converting height along lines of longitude causes distortion
		//because they are not paralell at any given y point.
		
		//Depending on orientation we will need different conversion factors.
		this.lengthOffset = utilHelper.kmToDegLng(utilHelper.feetToKm(
				SwathProperties.SWATH_LENGTH_FT));
		this.widthOffset = utilHelper.kmToDegLng(utilHelper.feetToKm(
				SwathProperties.SWATH_WIDTH_FT));
		
		generateSwathList();
	}
	
	//TODO - CP - Potential edge case with fence if user tries to insert at index 0
	
	/**
	 * Adds the entire list of swathPoints for the related swath pattern
	 * to the waypoint list at the specified location.
	 * @return Boolean - Whether or not the command was executed successfully.
	 */
	@Override
	public boolean execute() {
		CommandManager manager = CommandManager.getInstance();
		int currIndex = this.index;
		
		//For each point in the swath list BEFORE placement begins
		for(Dot sPoint : swathPoints) {
			if(manager.getGeofence().getIsEnabled()) {
				//Check for and refuse a second index 0 placement
				if(currIndex == 0) {
					serialLog.warning(WARN_GEOFENCE_ALREADY_PLACED);
					return false;
				}
				
				//Check for waypoint intersection
				if(!manager.getGeofence().doesLocationIntersect(sPoint)) {
					serialLog.warning(WARN_NO_GEOFENCE_INTERSECT);
					return false;
				}
			}
			
			currIndex++;
		}

		//Reset the index for the placement pass.
		currIndex = this.index;
		
		//For each point in swathPoints list during placement
		for(Dot sPoint : swathPoints) {
			//If the waypoint max hasn't been reached yet
			if(waypoints.size() < MAX_WAYPOINTS) {
				
				waypoints.add(sPoint, currIndex);
				
				if(currIndex == 0) {
					//create the geofence at the first index.
					manager.getGeofence().setOriginLatLng(point.getLatitude(), 
							point.getLongitude());
					manager.getGeofence().setIsEnabled(true);
					
					waypoints.setTarget(currIndex);
				}
				
				waypoints.setSelected(currIndex);
				
				currIndex++;
			}
		}
		
		//Set the placement flag and the previous state
		swathProperties.setPreviousSwathPlacedState(
				swathProperties.getIsSwathPlaced());
		swathProperties.setIsSwathPlaced(true);		
		
		//If we hit the maximum waypoints while looping,
		//warn and return false here after setting swath states
		if(waypoints.size() == MAX_WAYPOINTS) {
			serialLog.warning(WARN_MAX_WAYPOINTS_REACHED);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Removes the related swathPoints list from the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		CommandManager manager = CommandManager.getInstance();
		
		int endIndex = (this.index + (swathPoints.size() - 1));
		
		//Starting at the end of the swath list, remove each swath point
		for(int i = endIndex; i >= index; i--) {
			waypoints.remove(i);
			
			//If this is the initial waypoint. Remove the fence.
			if(index == 0) {
				manager.getGeofence().setIsEnabled(false);
			}
		}
		
		//Reset the placement flag and the previous state
		swathProperties.setPreviousSwathPlacedState(
				swathProperties.getIsSwathPlaced());
		swathProperties.setIsSwathPlaced(false);
		
		return true;
	}
	
	/**
	 * Re-adds the swathPoints list to the waypoint list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	public boolean redo() {
		return execute();
	}
	
	/**
	 * Uses the swath properties select a swath pattern to be generated
	 */
	public void generateSwathList() {

		//Determine the swath pattern to generate using orientation properties
		switch(type) {
			case HORIZONTAL:
				switch(inversion) {
					case NONE:
						swathCreateHorizontalNotInverted();
						break;
						
					case FLIPPED:
						swathCreateHorizontalInverted();
						break;
				}
				break;
				
			case VERTICAL:
				switch(inversion) {
					case NONE:
						swathCreateVerticalNotInverted();
						break;
						
					case FLIPPED:
						swathCreateVerticalInverted();
						break;
				}
				break;
		}		
	}
	
	/**
	 * Creates a horizontally oriented swath pattern with no inversion
	 * to be placed by the command when processed.
	 */
	private void swathCreateHorizontalNotInverted() {
		Dot currentPoint;
		
		//Initial Point
		currentPoint = point;
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(),
				currentPoint.getLongitude() + lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Left
		currentPoint = new Dot(
				currentPoint.getLatitude(),
				currentPoint.getLongitude() - lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude() - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(),
				currentPoint.getLongitude() + lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude() - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Left
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() - lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
	}
	
	/**
	 * Creates a horizontally oriented swath pattern with inversion
	 * to be placed by the command when processed.
	 */
	private void swathCreateHorizontalInverted() {
		Dot currentPoint;
		
		//Initial Point
		currentPoint = point;
		swathPoints.add(currentPoint);
		
		
		//Left
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() - lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude() - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude() - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Left
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() - lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude() - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude() - widthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Left
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() - lengthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
	}
	
	/**
	 * Creates a vertically oriented swath pattern with no inversion to
	 * be placed by the command when processed.
	 */
	private void swathCreateVerticalNotInverted() {
		Dot currentPoint;
		
		//Initial Point
		currentPoint = point;
		swathPoints.add(currentPoint);
		
		//Up
		currentPoint = new Dot(
				currentPoint.getLatitude()  + lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Up
		currentPoint = new Dot(
				currentPoint.getLatitude()  + lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Up
		currentPoint = new Dot(
				currentPoint.getLatitude()  + lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
	}
	
	/**
	 * Creates a vertically oriented swath pattern with inversion to
	 * be placed by the command when processed.
	 */
	private void swathCreateVerticalInverted() {
		Dot currentPoint;
		
		//Initial Point
		currentPoint = point;
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Up
		currentPoint = new Dot(
				currentPoint.getLatitude()  + lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Up
		currentPoint = new Dot(
				currentPoint.getLatitude()  + lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Right
		currentPoint = new Dot(
				currentPoint.getLatitude(), 
				currentPoint.getLongitude() + widthOffset, 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
		
		//Down
		currentPoint = new Dot(
				currentPoint.getLatitude()  - lengthOffset, 
				currentPoint.getLongitude(), 
				(short)0,
				WaypointType.SWATH);
		swathPoints.add(currentPoint);
	}
	
}
