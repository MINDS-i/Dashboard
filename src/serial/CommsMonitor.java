package com.serial;

import com.Context;
import com.serial.Messages.*;

import java.util.logging.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 10-18-21
 * Description: Singleton class designed to monitor the serial communications 
 * channel between the dashboard and a vehicle to help ensure unbroken operation 
 * and expected information are present.
 * 
 * 10-18-21 - Currently supported features -
 * 		- Heartbeat poll monitoring. 
 * 
 * Additional functionality to be added as required.
 */
public class CommsMonitor {
	private static CommsMonitor cManInstance = null;
	
	//Heartbeat Constants
	private static final int HEARTBEAT_GOOD				= 0;
	private static final int MAX_HEARTBEAT_CHECK_COUNT 	= 10;
	private static final int HEARTBEAT_POLL_RATE_MS 	= 500;
	
	//Heartbeat Vars
	private javax.swing.Timer heartbeatCheckTimer;
	private int heartbeatCheckCount;
	private boolean awaitingInitialHeartbeat;
	private boolean heartbeatConnectionLost;
	
	//Logging Support
	protected final Logger serialLog = Logger.getLogger("d.serial");

	/**
	 * Private Class Constructor instantiated through getInstance
	 */
	private CommsMonitor() {
		heartbeatCheckCount = 0;
		awaitingInitialHeartbeat = true;
		heartbeatConnectionLost = false;
		heartbeatCheckTimer = new javax.swing.Timer(HEARTBEAT_POLL_RATE_MS,
				heartbeatCheckAction);
	}
	
	/**
	 * Returns an instance of this singleton class, This is
	 * the intended form of retrieval and instantiation.
	 * @return - the CommsMonitor instance
	 */
	public static CommsMonitor getInstance() {
		if(cManInstance == null) {
			cManInstance = new CommsMonitor();
		}
		
		return cManInstance;
	}
	
	/**
	 * Monitor Type - Heartbeat
	 * Timer event action that initiates a heartbeat pualse check 
	 * when triggered.
	 */
	private ActionListener heartbeatCheckAction = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			updateHeartbeatCheck();
		}
	};
	
	/**
	 * Monitor Type - Heartbeat
	 * Increments the heartbeat count check. If the heartbeat poll count 
	 * has exceeded the maximum, an error is logged.
	 */
	private void updateHeartbeatCheck() {
		if(heartbeatCheckCount == MAX_HEARTBEAT_CHECK_COUNT) {
			if(!heartbeatConnectionLost) {
				serialLog.severe(
						"CommsMonitor - No serial heartbeat response from unit. " 
					  + "Please check that radio connection is established.");
				heartbeatConnectionLost = true;
			}
			
			return;
		}
		
		heartbeatCheckCount++;
	}
	
	/**
	 * Monitor Type - Heartbeat
	 * Receives a serial response heartbeat pulse from the attached vehicle,
	 * resetting the pulse attempt timeout count. If this is an initial pulse
	 * or a pulse that occurs after connection was previously lost, this is 
	 * logged appropriately.
	 * @param pulse - The received pulse data
	 */
	public void receiveHeartbeatPulse(int pulse) {
		
		if(awaitingInitialHeartbeat) {
			serialLog.warning(
					"CommsMonitor - Initial serial heartbeat received.");
			awaitingInitialHeartbeat = false;
		}
		
		if(heartbeatConnectionLost) {
			serialLog.warning(
					"CommsMonitor - Serial Heartbeat connection re-established");
			heartbeatConnectionLost = false;
		}
		
		if(pulse == HEARTBEAT_GOOD) {
			System.err.println(
					"Received heartbeat check in, resetting timeout count");
			heartbeatCheckCount = 0;
		}
		else {
			System.err.println("Unknown heartbeat respone type received");
			serialLog.warning(
					"CommsMonitor - Received unknown heartbeat status. Ignoring");
		}
	}
	
	/**
	 * Monitor Type - Heartbeat
	 * Starts the periodic timer for a heartbeat pulse, resets the pulse count,
	 * and flags the monitor to expect an intial heartbeat message from the
	 * connected vehicle.
	 */
	public void startHeartbeatTimer() {
		System.err.println("Starting heartbeat timer");
		heartbeatCheckCount = 0;
		awaitingInitialHeartbeat = true;
		heartbeatCheckTimer.start();
	}
	
	/**
	 * Monitor Type - Heartbeat
	 * Stops the periodic timer for a heartbeat pulse check.
	 */
	public void stopHeartbeatTimer() {
		System.err.println("Stopping heartbeat timer");
		heartbeatCheckTimer.stop();
	}
}
