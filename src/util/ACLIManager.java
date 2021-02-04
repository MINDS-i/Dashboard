package com.util;

import java.util.*;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 2-3-2021
 * Description: Singleton class used to configure the arduino-cli and upload
 * APM sketches from the dashboard.
 */
public class ACLIManager {
	private static ACLIManager aclimInstance = null;
	
	private static final String CMD_EXEC  = "cmd /c";
	private static final String ACLI_EXEC = "arduino-cli";
	private static final String ACLI_PATH = "";
	
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
		GENERATE_BOARD_LIST (Arrays.asList("board", "list")),
		//Parses port for an APM board and generates a config
		ATTACH_APM_BOARD 	(Arrays.asList("board", "attach", "serial://")),
		//Compiles and uploads a sketch using generated config
		UPLOAD_SKETCH		(Arrays.asList("compile", "upload"));
		
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
		params = new ArrayList<String>();
		params.addAll(0, Arrays.asList(CMD_EXEC, ACLI_EXEC, ACLI_PATH));
		params.addAll(command.params);
		
		processBuilder = new ProcessBuilder();
		processBuilder.command(params);
		
		switch(command) {
			case INSTALL_AVR_CORE:
				//No stored information required
				break;
			case GENERATE_BOARD_LIST:
				//No stored information required
				break;
			case ATTACH_APM_BOARD:
				//Checks for board list
				//Parses port for board
				//Generates Config
				break;
			case UPLOAD_SKETCH:
				//Checks for config
				//Compiles Sketch
				//Uploads sketch
				break;
		}
	}
}
