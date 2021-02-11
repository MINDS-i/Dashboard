package com.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 2-3-2021
 * Description: Singleton class used to configure the arduino-cli and upload
 * APM sketches from the dashboard.
 */
public class ACLIManager {
	private static ACLIManager aclimInstance = null;
	
	private static final String CMD_EXEC  			= "cmd /c";
	private static final String ACLI_EXEC 			= "arduino-cli";
	private static final String ACLI_PATH 			= "";
	private static final String BOARD_LIST_FILE 	= "boardlist.txt";
	private static final String SKETCH_CONFIG_FILE 	= "sketch.json";
	
	private ProcessBuilder processBuilder;
	private List<String> params;
	private String port;
	private String core;
	private String errorString;
	
	/**
	 * Description: Enum used to create, package, and contain all necessary information
	 * to run an Arduino CLI command through the ACLIManager (Arduino CLI Manager).
	 */	
	public enum ACLICommand {
		//Installs required core for APM
		INSTALL_AVR_CORE 	(Arrays.asList("core", "install", "arduino:avr")),
		//Generates a list of attached Arduino Boards
		GENERATE_BOARD_LIST (Arrays.asList("board", "list", ">", BOARD_LIST_FILE)),
		//Parses the port and core of the APM as it appears in the board list file
		PARSE_BOARD_INFO	(Arrays.asList(BOARD_LIST_FILE)),
		//Compiles and prepares a sketch for upload
		COMPILE_SKETCH		(Arrays.asList("compile")),
		//Uploads a sketch to the APM
		UPLOAD_SKETCH		(Arrays.asList("upload"));
		
		public final List<String> params;
		
		ACLICommand(List<String> params) {
			this.params = params;
		}
	};
	
	/**
	 * Constructor (Private, accessed by getInstance)
	 */
	private ACLIManager() {
		port = "";
		core = "";
		errorString = "No error.";
	}
	
	/**
	 * Returns the singleton instance of this class to be used system wide.
	 * @return The Arduino CLI Command manager instance.
	 */
	public static ACLIManager getInstance() {
		if(aclimInstance == null) {
			aclimInstance = new ACLIManager();
		}
		
		return aclimInstance;
	}
	
	/**
	 * Executes the given ACLICommand object. as a command line operation using
	 * the Java ProcessBuilder class.
	 * @param command - The action/command to execute
	 */
	public boolean execute(ACLICommand command) {
		params = new ArrayList<String>();
		params.addAll(0, Arrays.asList(CMD_EXEC, ACLI_EXEC, ACLI_PATH));
		params.addAll(command.params);
		
		switch(command) {
			case INSTALL_AVR_CORE:
				//Go straight to exectuion
				break;
			case GENERATE_BOARD_LIST:
				//Go straight to execution
				break;
			case PARSE_BOARD_INFO:
				if(parseInfo()) {
					params.add(port);
				}
				else {
					errorString = "Could not obtain port and core information.";
					System.err.println("ACLI Manager parse error: "
							+ "parsing operation unsuccessful");
					return false;
				}
				break;
			default:
		}
		
		processBuilder = new ProcessBuilder();
		processBuilder.command(params);
		
		try {
			Process process = processBuilder.start();
			if(process.waitFor() == 0) {
				errorString = "No error.";
			}
		}
		catch (Exception e) {
			switch(command) {
				case INSTALL_AVR_CORE:
					errorString = "Unable to install avr core.";
					break;
				case GENERATE_BOARD_LIST:
					errorString = "Unable to generate board list.";
					break;
				case PARSE_BOARD_INFO:
					errorString = "Unable to parse board info.";
					break;
				default:
					errorString = "Unknown error";
			}
			System.err.println("ACLI Manager exec error: " + e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Executes the given ACLICommand object. as a command line operation using
	 * the Java ProcessBuilder class. Overloaded from previous function to accept a
	 * sketch path.
	 * @param command - The action/command to execute
	 * @param sketchPath - The user selected path to the sketch being used.
	 */
	public boolean execute(ACLICommand command, String sketchPath) {
		params = new ArrayList<String>();
		params.addAll(0, Arrays.asList(CMD_EXEC, ACLI_EXEC, ACLI_PATH));
		params.addAll(command.params);
		
		switch(command) {
			case COMPILE_SKETCH:
				if(core.isEmpty() || port.isEmpty()) {
					errorString = "Necessary board information (port or core) missing. "
							+ "Unable to compile.";
					return false;
				}
				params.add("-b " + core);
				params.add("-p " + port);
				params.add(sketchPath);
				break;
			case UPLOAD_SKETCH:
				if(port.isEmpty()) {
					errorString = "port information missing. "
							+ "Unable to upload sketch.";
					return false;
				}
				
				params.add("-p " + port);
				params.add(sketchPath);
				break;
			default:
		}
		
		processBuilder = new ProcessBuilder();
		processBuilder.command(params);
		
		try {
			Process process = processBuilder.start();
			if(process.waitFor() == 0) {
				errorString = "No error.";
			}
		}
		catch (Exception e) {
			switch(command) {
			case COMPILE_SKETCH:
				errorString = "Unable to compile sketch.";
				break;
			case UPLOAD_SKETCH:
				errorString = "Unable to upload sketch.";
				break;
			default:
				errorString = "Unknown error";
			}
			System.err.println("ACLI Manager exec error: " + e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reads the board list file of attached arduino devices and parses
	 * out the required port name and core type for an APM module. These are
	 * requirements needed for arduino-cli to compile and upload a sketch.
	 * @return - Whether or not the operation was successful.
	 */
	public boolean parseInfo() {
		//The APM shows up as an avr core. Here is a Regex target line example: 
		//"COM4 Serial Port (USB) Arduino Mega or Mega 2560 arduino:avr:mega arduino:avr"
		Pattern pattern = Pattern.compile(
				"^COM\\d+\\s\\w{0,6}\\s\\w{0,4}\\s\\(?\\w{0,5}\\)?\\sArduino");
		Matcher matcher;
		File file;
		boolean matchFound = false;
		
		//Checks for board list
		file = new File(BOARD_LIST_FILE);
		if(!file.exists()) {
			System.err.println(
					"ACLI Manager file error: Board list not found.");
			return false;
		}
		
		try {
			FileReader fileReader = new FileReader(BOARD_LIST_FILE);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String temp;
			
			while((temp = bufferedReader.readLine()) != null) {
				matcher = pattern.matcher(temp);
				
				//If a match was found
				matchFound = matcher.find();
				if(matchFound) {
					String[] substrings = temp.split(" ");
					port = "serial://" + substrings[0];
					core = substrings[(substrings.length - 1)];
					break;
				}
			}	
			bufferedReader.close();

			//If no match was found
			if(!matchFound) {
				System.err.println(
						"ACLI Manager file error: Failed to find port.");
				return false;
			}
		}
		catch(Exception e) {
			System.err.println("ACLI Manager file error: " + e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets the most recent error string resulting from command executions.
	 * @return - The currently set error string or "No error" if there isn't one.
	 */
	public String getErrorStr() {
		return errorString;
	}
}
