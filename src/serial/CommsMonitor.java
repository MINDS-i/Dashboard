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
 * channel between the dashboard and vehicle to ensure unbroken operation 
 * and expected information are present.
 * 
 * 10-18-21 - Currently supported features -
 * 		- Communications Heartbeat pulse and response monitoring. 
 * 
 * Additional functionality to be added as required.
 */
public class CommsMonitor {
	private static CommsMonitor cManInstance = null;
	
	//Heartbeat Constants
	private static final int HEARTBEAT_GOOD				= 0;
	private static final int MAX_HEARTBEAT_ATTEMPTS 	= 10;
	private static final int HEARTBEAT_PULSE_MS 		= 500;
	
	//Heartbeat Vars
	private javax.swing.Timer heartbeatPulseTimer;
	private int pulseAttempts;

	//Standard Vars
	private Context context;
	
	//Logging Support
	protected final Logger serialLog = Logger.getLogger("d.serial");

	/**
	 * Private Class Constructor instantiated through getInstance
	 * @param ctx - The application context.
	 */
	private CommsMonitor(Context ctx) {
		context = ctx;
		
		pulseAttempts = 0;
		heartbeatPulseTimer = new javax.swing.Timer(HEARTBEAT_PULSE_MS,
				heartbeatPulseAction);
	}
	
	/**
	 * Returns an instance of this singleton class, This is
	 * the intended form of retrieval and instantiation.
	 * @param ctx - The application context
	 * @return - the CommsMonitor instance
	 */
	public static CommsMonitor getInstance(Context ctx) {
		if(cManInstance == null) {
			cManInstance = new CommsMonitor(ctx);
		}
		
		return cManInstance;
	}
	
	/**
	 * Heartbeat Monitor 
	 * Timer event action that initiates a heartbeat pulse when triggered.
	 */
	private ActionListener heartbeatPulseAction = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			sendHeartbeatPulse();
		}
	};
	
	/**
	 * Heartbeat Monitor
	 * Attempts to send a communciations heartbeat pulse to unit.
	 */
	private void sendHeartbeatPulse() {
		
		//If Maximum attempts exceeded report error and stop timer.
		if(pulseAttempts == MAX_HEARTBEAT_ATTEMPTS) {
			System.err.println("Maximum heartbeat count reached, no response");
			serialLog.severe(
					"CommsMonitor - No serial heartbeat " 
				  + "response from unit. Check radio connection.");
			stopHeartbeatTimer();
		}
		
		context.sender.sendMessage(Message.sendHeartbeatPulse());
		pulseAttempts++;
	}
	
	/**
	 * Heartbeat Monitor
	 * Receives a serial response heartbeat pulse from the attached vehicle,
	 * resetting the pulse attempt timeout count.
	 * @param response - The received response
	 */
	public void receiveHeartbeatResponse(int response) {
		if(response == HEARTBEAT_GOOD) {
			System.err.println("Received response, resetting timeout count");
			pulseAttempts = 0;
		}
		else {
			System.err.println("Unknown heartbeat respones value received");
			serialLog.warning("CommsMonitor - Received unknown heartbeat " 
						 	+ "status value. Ignoring");
		}
	}
	
	/**
	 * Heartbeat Monitor
	 * Starts the periodic timer for a heartbeat pulse.
	 */
	public void startHeartbeatTimer() {
		System.err.println("Starting heartbeat timer");
		heartbeatPulseTimer.start();
	}
	
	/**
	 * Heartbeat Monitor
	 * Stops the periodic timer for a heartbeat pulse.
	 */
	public void stopHeartbeatTimer() {
		System.err.println("Stopping heartbeat timer");
		heartbeatPulseTimer.stop();
	}
	
}
