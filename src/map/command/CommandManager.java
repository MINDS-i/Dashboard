package com.map.command;

import java.util.*;
import java.awt.Graphics;

import com.map.Dot;
import com.map.CoordinateTransform;
import com.map.command.WaypointCommand;
import com.map.geofence.WaypointGeofence;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Singleton class used to process and track Waypoint commands. This
 * system allows for Undo/Redo support.
 */
public class CommandManager {
	private static CommandManager cmInstance = null;
	
	//Tracked command lists
	private LinkedList<WaypointCommand> processedCommands;
	private LinkedList<WaypointCommand> revertedCommands;
	
	//Geofence
	private WaypointGeofence geofence;
	
	/**
	 * Constructor (Private, accessed by getInstance)
	 */
	private CommandManager() {
		processedCommands = new LinkedList<WaypointCommand>();
		revertedCommands  = new LinkedList<WaypointCommand>();
		geofence = null;
	}
	
	/**
	 * Returns an instance of this singleton class. This is
	 * the intended form of retrieval and instantiation.
	 * @return - The CommandManager instance
	 */
	public static CommandManager getInstance() {
		if(cmInstance == null) {
			cmInstance = new CommandManager();
		}
		
		return cmInstance;
	}
	
	/**************************************************************************/
	//Command Processing Functions
	/**************************************************************************/
	
	/**
	 * Executes a waypoint command, adds it to the processedCommands
	 * list, and resets the revertedCommands list to maintain command
	 * order continuity.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	public boolean process(WaypointCommand command) {
		boolean result;
		
		result = command.execute();
		
		if(result == false) {
			System.err.println("CommandManager - Process command failure.");
			return false;
		}
		
		processedCommands.push(command);
		revertedCommands.clear();
		
		return result;
	}
		
	/**
	 * Pulls the most recently executed waypoint command from the
	 * processedCommands list, tells it to undo itself, and adds that
	 * command to the revertedCommands list.
	 * @return Boolean - Whether or not the operation was successful.
	 */
	public boolean undo() {
		WaypointCommand command;
		boolean result;
		
		if(processedCommands.isEmpty()) {
			System.err.println(
					"CommandManager - Failed undo. processed queue is empty.");
			return false;
		}
		
		command = processedCommands.pop();
		result = command.undo();
		
		if(result == true) {
			revertedCommands.push(command);
		}
		
		return result;
	}
	
	/**
	 * Pulls the most recently undone waypoint command from the 
	 * revertedCommands list, tells it to re-execute itself, and 
	 * adds it to the processedCommands list
	 * @return Boolean - Whether or not the operation was successful.
	 */
	public boolean redo() {
		WaypointCommand command;
		boolean result;
		
		if(revertedCommands.isEmpty()) {
			System.err.println(
					"CommandManager - Failed redo. reverted queue is empty.");
			return false;
		}
		
		command = revertedCommands.pop();
		result = command.execute();
		
		if(result == true) {
			processedCommands.push(command);
		}
		
		return result;
	}
	
	/**
	 * Clears all tracked command lists.
	 */
	public void clearTrackedCommands() {
		processedCommands.clear();
		revertedCommands.clear();
	}
	
	/**************************************************************************/
	//Geofence Functions
	/**************************************************************************/
	
	/**
	 * Initializes a new geofence instance with the given parameters. This
	 * instance is disabled by default and must be enabled using the fence's
	 * setIsEnabled() call before being utilized by the UI.
	 * @param radius_ft - The radius to the wall of the geofence from it's 
	 * 					  origin in feet.
	 * @param type		- The shape of the fence.
	 * @param transform	- the map coordinate transform used to convert
	 * 					  coordinates from longitude and latitude to pixel
	 * 					  screen coordinates.
	 */
	public void initGeofence(double radius_ft,
			WaypointGeofence.FenceType type, CoordinateTransform transform) {
		geofence = new WaypointGeofence(radius_ft, type,
				transform);
	}

	/**
	 * Retrieves the currently set geofence instance or null if 
	 * one does not yet exist.
	 * @return - The geofence
	 */
	public WaypointGeofence getGeofence() {
		return geofence;
	}
}
