package com.serial;

import com.Context;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 10-18-21
 * Description:  
 */
public class CommsMonitor {
	
	//Heargbeat Constants
	private static final int HEARTBEAT_GOOD				= 1;
	private static final int MAX_HEARTBEAT_ATTEMPTS 	= 10;
	private static final int HEARTBEAT_PULSE_MS 		= 500;
	
	private Context context;
	
	private javax.swing.Timer heartbeatPulseTimer;
	private int pulseAttempts;
	
	public CommsMonitor(Context ctx) {
		context = ctx;
		pulseAttempts = 0;
		
		heartbeatPulseTimer = new javax.swing.Timer(HEARTBEAT_PULSE_MS,
				heartbeatPulseAction);
	}
	
	//Heartbeat Timer Action
	private ActionListener heartbeatPulseAction = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			
		}
		//TODO - CP - Context - > send hearbeat
	};
	
	//SendHeartbeatPulse
	private void sendHeartbeatPulse() {
		//TODO - CP - Send pulse to APM (Will need command in sender)
	}
	
	//Receive heartbeat response
	public void receiveHeartbeatResponse(int response) {
		if(response == HEARTBEAT_GOOD) {
			pulseAttempts = 0;
		}
		else {
			//TODO - CP - Add error handling here?
		}
	}
	
	//Heartbeat Timer Start
	public void startHeartbeatTimer() {
		//TODO - CP - Make sure this is a rover and not a copter for now
		//so that we don't break backwards compatability with copter operators.
		heartbeatPulseTimer.start();
	}
	
	//Heartbeat Timer Stop
	public void stopHearbeatTimer() {
		heartbeatPulseTimer.stop();
	}
	
}
