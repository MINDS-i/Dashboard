package com.map.command;

import java.util.*;
import com.map.command.WaypointCommand;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-2020
 * Description: Singleton class used to process and track Waypoint commands. This
 * system allows for Undo/Redo support.
 */
public class CommandManager {
	private static CommandManager cmInstance = null;
	
	private LinkedList<WaypointCommand> processedCommands;
	private LinkedList<WaypointCommand> revertedCommands;
	
	private CommandManager() {
		processedCommands = new LinkedList<WaypointCommand>();
		revertedCommands  = new LinkedList<WaypointCommand>();		
	}
	
	/**
	 * Returns an instance of this singleton class. This is
	 * the intended form of retrieval and instantiation.
	 */
	public static CommandManager getInstance() {
		if(cmInstance == null) {
			cmInstance = new CommandManager();
		}
		
		return cmInstance;
	}
	
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
	
	public void clearTrackedCommands() {
		processedCommands.clear();
		revertedCommands.clear();
	}
	
}
