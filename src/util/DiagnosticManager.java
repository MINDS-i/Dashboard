package com.util;

import java.util.*;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 9-12-22
 * Description: Singleston class used to track and display system running data
 * and diagnostics for use in observing typical system and unit run behavior 
 * as well as diagnoses and troubleshooting of potential issues.
 */
public class DiagnosticManager {	
	private static DiagnosticManager managerInstance = null;

	//------------------------------------------------------------------------//
	//General Serial Statistic Indexes
	public static final int CONNECTED					= 0;
	public static final int CONNECT_FAILED				= 1;
	public static final int DISCONNECTED				= 2;
	public static final int DISCONNECT_FAILED			= 3;
	public static final int LAST_BAUD_USED				= 4;
	public static final int SEND						= 5;
	public static final int SEND_FAIL					= 6;
	public static final int RESEND						= 7;
	public static final int RESEND_FAIL					= 8;
	public static final int SEND_TIMEOUT				= 9;
	public static final int SEND_CONFIRM				= 10;
	
	//Total number of general Serial statistics
	public static final int NUM_SERIAL_STATS			= 11;			
	
	//Formats/Converts raw values to their string representations
	public class SerialItem {
		
		//String conversions
		protected String itemName;
		protected String itemValue;
		
		public SerialItem(int nameIndex, int value) {
			itemName = nameIndexToString(nameIndex);
			itemValue = Integer.toString(value);
		}
		
		/**
		 * Converts the serial stat index to a name string
		 * @param index - The serial data name index
		 * @return - A string representation of the index
		 */
		public String nameIndexToString(int index) {
			
			switch(index) {
			case CONNECTED:
				return "Connected";
			case CONNECT_FAILED:
				return "Connect Failed";
			case DISCONNECTED:
				return "Disconnected";
			case DISCONNECT_FAILED:
				return "Disconnect Failed";
			case LAST_BAUD_USED:
				return "Last Baud Used";
			case SEND:
				return "Send";
			case SEND_FAIL:
				return "Send Fail";
			case RESEND:
				return "Resend";
			case RESEND_FAIL:
				return "Resend Fail";
			case SEND_TIMEOUT:
				return "Send Timeout";
			case SEND_CONFIRM:
				return "Send Confirm";
			default:
				return "Unknown Serial Statistic";
			}
		}
		
		/**
		 * Return name string
		 * @return - The name string for this item
		 */
		public String getNameString() {
			return itemName;
			
		}
		
		/**
		 * Return value string
		 * @return - The value string for this item
		 */
		public String getValueString() {
			return itemValue;
		}
	} //End SerialItem Inner Class
	
	//------------------------------------------------------------------------//
	
	//------------------------------------------------------------------------//
	//Telemetry Statistic Indexes
	public static final int LINE_GRAVITY	 			= 0;	
	public static final int STEER_THROW 				= 1;	
	public static final int STEER_STYLE 				= 2;	
	public static final int STEER_SCALAR 				= 3;	
	public static final int MIN_FWD_SPEED 				= 4;	
	public static final int MAX_FWD_SPEED 				= 5;	
	public static final int REV_STEER_THROW 			= 6;	
	public static final int REV_SPEED 					= 7;	
	public static final int PING_FACTOR 				= 8;
	public static final int COAST_TIME 					= 9;
	public static final int MIN_REV_TIME 				= 10;
	public static final int CRUISE_P 					= 11;
	public static final int CRUISE_I 					= 12;
	public static final int CRUISE_D 					= 13;
	public static final int TIRE_DIAMETER 				= 14;
	public static final int STEER_CENTER 				= 15;
	public static final int WAYPOINT_ACHIEVED_RADIUS 	= 16;
	public static final int APPROACH_RADIUS 			= 17;
	public static final int UNUNSED_HOLDER_A			= 18; //Unused Index
	public static final int UNUNSED_HOLDER_B			= 19; //Unused Index
	public static final int STEER_SKEW 					= 20;
	public static final int AVOID_PING_EDGES 			= 21;
	public static final int AVOID_PING_MIDDLE 			= 22;
	public static final int AVOID_PING_CENTER 			= 23;
	public static final int WARN_PING_EDGES 			= 24;
	public static final int WARN_PING_MIDDLE 			= 25;
	public static final int WARN_PING_CENTER 			= 26;
	public static final int RADIO_FAILSAFE 				= 27;
	public static final int RADIO_CONTROLLER 			= 28;
	
	//Total number of Telemetry statistics
	public static final int NUM_TELEMETRY_STATS			= 29;
	
	//Formats/Converts raw values to their string representations
	public class TelemetryItem {

		//String Conversions
		protected String itemName;
		protected String itemValue;
		
		public TelemetryItem(int nameIndex, double value) {
			itemName = nameIndexToString(nameIndex);
			itemValue = Double.toString(value);
		}
		
		/**
		 * Converts the telemetry stat index to a name string
		 * @param index - The telemetry data name index
		 * @return - A string representation of the index
		 */
		public String nameIndexToString(int index) {
			switch(index) {
			case LINE_GRAVITY:
				return "Line Gravity";
			case STEER_THROW:
				return "Steer Throw";
			case STEER_STYLE:
				return "Steer Style";
			case STEER_SCALAR:
				return "Steer Scalar";
			case MIN_FWD_SPEED:
				return "Min Forward Speed";
			case MAX_FWD_SPEED:
				return "Max Forward Speed";
			case REV_STEER_THROW:
				return "Reverse Steer Throw";
			case REV_SPEED:
				return "Reverse Speed";
			case PING_FACTOR:
				return "Ping Factor";
			case COAST_TIME:
				return "Coast Time";
			case MIN_REV_TIME:
				return "Min Reverse Time";
			case CRUISE_P:
				return "Cruise P";
			case CRUISE_I:
				return "Cruise I";
			case CRUISE_D:
				return "Cruise D";
			case TIRE_DIAMETER:
				return "Tire Diameter";
			case STEER_CENTER:
				return "Steer Center";
			case WAYPOINT_ACHIEVED_RADIUS:
				return "Waypoint Achieved Radius";
			case APPROACH_RADIUS:
				return "Approach Radius";
			case UNUNSED_HOLDER_A:
				return "Unused A";
			case UNUNSED_HOLDER_B:
				return "Unused B";
			case STEER_SKEW:
				return "Steer Skew";
			case AVOID_PING_EDGES:
				return "Avoid Ping Edges";
			case AVOID_PING_MIDDLE:
				return "Avoid Ping Middle";
			case AVOID_PING_CENTER:
				return "Avoid Ping Center";
			case WARN_PING_EDGES:
				return "Warn Ping Edges";
			case WARN_PING_MIDDLE:
				return "Warn Ping Middle";
			case WARN_PING_CENTER:
				return "Warn Ping Center";
			case RADIO_FAILSAFE:
				return "Radio Failsafe";
			case RADIO_CONTROLLER:
				return "Radio Controller";
				
			default:
				return "Unknown Telemetry Statistic";
			}
		}
		
		/**
		 * Return name string
		 * @return - The name string for this item
		 */
		public String getNameString() {
			return itemName;
			
		}
		
		/**
		 * Return value string
		 * @return - The value string for this item
		 */
		public String getValueString() {
			return itemValue;
		}
	} //End TelemetryItem Inner Class
	//------------------------------------------------------------------------//
	
	//Diagnostic Arrays
	private static int[] serialStatsArray;
	private static double[] telemetryStatsArray;
	
	//Diagnostic Mode Toggle
	private boolean isDiagnosticModeEnabled;
	
	/**
	 * Constructor (Private, accessed by getInstance)
	 */
	private DiagnosticManager() {
		//Arrays are pre zero'd by Java spec here so no need to do it manually.
		serialStatsArray = new int[NUM_SERIAL_STATS];
		telemetryStatsArray = new double[NUM_TELEMETRY_STATS];
		
		isDiagnosticModeEnabled = true;
	}

	/**
	 * Returns the singleton instance of this class to be used system wide.
	 * @return The DiagnosticManager instance
	 */
	public static DiagnosticManager getInstance() {
		if(managerInstance == null ) {
			managerInstance = new DiagnosticManager();
		}
		
		return managerInstance;
	}
	
	/**
	 * Get whether this diagnostic mode is enabled or disabled.
	 * @return - boolean - Whether the mode is enabled or not
	 */
	public boolean getIsModeEnabled() {
		return isDiagnosticModeEnabled;
	}
	
	/**
	 * Set whether this diagnostic mode is enabled or not.
	 * @param isEnabled  - ...
	 */
	public void setIsModeEnabled(boolean isEnabled) {
		isDiagnosticModeEnabled = isEnabled;
	}
	
	/**
	 * Returns all tracked stats to their default values (Typically 0)
	 */
	public void resetStats() {
		
		for(int i = 0; i < NUM_SERIAL_STATS; i++) {
			serialStatsArray[i] = 0;
		}
		
		for(int i = 0; i < NUM_TELEMETRY_STATS; i++) {
			telemetryStatsArray[i] = 0.0;
		}
	}

	/**
	 * Add or increment value (see switch cases) at index to Serial Array 
	 * @param index - The stats index
	 * @param value - The value of the stat to be logged.
	 */
	public void logSerialData(int index, int value) {
		//Only store data if this mode is enabled.
		if(!getIsModeEnabled()) {
			return;
		}
		
		switch(index) {
		case LAST_BAUD_USED:
			serialStatsArray[index] = value;
			break;
		default:
			serialStatsArray[index] += value;
		}
	}
	
	/**
	 * Returns a SerialItem object containing the string
	 * representations of the indexed value 
	 * @param index - The diagnostic stat index 
	 * @return - SerialItem object containing the strings for the
	 * given index.
	 */
	public SerialItem createSerialItem(int index) {
		return new SerialItem(index, serialStatsArray[index]);
	}
	
	/**
	 * Returns a List of SerialItem objects
	 * containing string values for all serial statistics.
	 * @return - the List of SerialItem.
	 */
	public List<SerialItem> getSerialItemList() {
		ArrayList<SerialItem> itemList = new ArrayList<SerialItem>();
		
		for(int i = 0; i < NUM_SERIAL_STATS; i++) {
			itemList.add(new SerialItem(i, serialStatsArray[i]));
		}
		
		return Collections.synchronizedList(itemList);
	}
	
	/**
	 * Add value at index to Telemetry Array 
	 * @param index - The stats index
	 * @param value - The value of the stat to be logged
	 */
	public void logTelemetryData(int index, int value) {
		//Only store data if this mode is enabled.
		if(!getIsModeEnabled()) {
			return;
		}
		
		telemetryStatsArray[index] = value;
	}
	
	/**
	 * Returns a TelemetryItem object containing the string
	 * representations of the indexed value. 
	 * @param index - The diagnostic stat index 
	 * @return - TelemetryItem object containing the strings for the
	 * given index.
	 */	
	public TelemetryItem createTelemetryItem(int index) {
		return new TelemetryItem(index, telemetryStatsArray[index]);
	}
	
	/**
	 * Returns a List of TelemetryItem objects
	 * containing string values for all serial statistics.
	 * @return - the List of TelemetryItem.
	 */
	public List<TelemetryItem> getTelemetryItemList() {
		ArrayList<TelemetryItem> itemList = new ArrayList<TelemetryItem>();
		
		for(int i = 0; i < NUM_TELEMETRY_STATS; i++) {
			itemList.add(new TelemetryItem(i, telemetryStatsArray[i]));
		}
		
		return Collections.synchronizedList(itemList);
	}
	
} //End Main DiagnosticManager Class
