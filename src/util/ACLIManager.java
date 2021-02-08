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
	
	/**
	 * Description: Enum used to create, package, and contain all necessary information
	 * to run an Arduino CLI command through the ACLIManager (Arduino CLI Manager).
	 */	
	public enum ACLICommand {
		//Installs required core for APM
		INSTALL_AVR_CORE 	(Arrays.asList("core", "install", "arduino:avr")),
		//Generates a list of attached Arduino Boards
		GENERATE_BOARD_LIST (Arrays.asList("board", "list", ">", BOARD_LIST_FILE)),
		//Parses port for an APM board and generates a config
		ATTACH_APM_BOARD 	(Arrays.asList("board", "attach")),
		//Compiles and uploads a sketch using generated config
		UPLOAD_SKETCH		(Arrays.asList("compile", "--upload"));
		
		public final List<String> params;
		
		ACLICommand(List<String> params) {
			this.params = params;
		}
	};
	
	/**
	 * Constructor (Private, accessed by getInstance)
	 */
	private ACLIManager() {
		
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
	 * @param command
	 */
	public void execute(ACLICommand command) {
		File file;
		
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
			case ATTACH_APM_BOARD:
				//Checks for board list
				file = new File(BOARD_LIST_FILE);
				if(!file.exists()) {
					//TODO - CP - Report this error to the UI level (Message popup)
					System.err.println(
							"ACLI Manager file error: Board list not found.");
					return;
				}
				
				Pattern pattern = Pattern.compile(
						"^COM\\d+\\s\\w{0,6}\\s\\w{0,4}\\s\\(?\\w{0,5}\\)?\\sArduino");
				Matcher matcher;
				boolean matchFound = false;
				
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
							params.add("serial://" + substrings[0]);
							break;
						}
					}	
					bufferedReader.close();

					//If no match was found
					if(!matchFound) {
						System.err.println(
								"ACLI Manager file error: Failed to find port.");
						return;
					}
				}
				catch(Exception e) {
					System.err.println("ACLI Manager file error: " + e);
				}
				break;
			case UPLOAD_SKETCH:
				//Checks for config
				file = new File(SKETCH_CONFIG_FILE);
				if(!file.exists()) {
					//TODO - CP - Report this error to the UI level (Message popup)
					System.err.println(
							"ACLI Manager file error: board config file not found.");
					return;
				}
				break;
			default:
		}
		
		processBuilder = new ProcessBuilder();
		processBuilder.command(params);
		
		try {
			processBuilder.start();
		}
		catch (Exception e) {
			System.err.println("ACLI Manager exec error: " + e);
		}
		
	}
}
