package com.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.Context;
import com.serial.Serial;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 10-28-20
 * Description: Dashboard Widget used to display the current state of a connected unit as
 * described over serial communication.
 *
 */
public class StateWidget extends JPanel {
	private Context context;
	private JFrame infoFrame;
	
	private String apmStateStr;
	private String driveStateStr;
	private String autoStateStr;
	private String flagStateStr;
	
	public StateWidget(Context ctx) {
		context = ctx;
		
		apmStateStr 	= "Uninitialized";
		driveStateStr 	= "Uninitialized";
		autoStateStr 	= "Uninitialized";
		flagStateStr 	= "Uninitialized";
	}

	public void update(byte state, byte substate) {
		switch(state) {
			case Serial.APM_STATE:
				setAPMState(substate);
				break;
			case Serial.DRIVE_STATE:
				setDriveState(substate);
				break;
			case Serial.AUTO_STATE:
				setAutoState(substate);
				break;
			case Serial.AUTO_FLAGS:
				setFlagState(substate);
		}
	}
	
	private void setAPMState(byte substate) {
		switch(substate) {
			case Serial.APM_STATE_INIT:
				apmStateStr = "APM - Initializing";
				break;
			case Serial.APM_STATE_SELF_TEST:
				apmStateStr= "APM - Performing Self Test";
				break;
			case Serial.APM_STATE_DRIVE:
				apmStateStr = "APM - Driving";
				break;
			default:
				apmStateStr = "APM - No details or unrecognized state";
		}
	}
	
	private void setDriveState(byte substate) {
		switch(substate) {
			case Serial.DRIVE_STATE_STOP:
				driveStateStr = "Drive - Stopped";
				break;
			case Serial.DRIVE_STATE_AUTO:
				driveStateStr = "Drive - Auto Mode";
				break;
			case Serial.DRIVE_STATE_RADIO:
				driveStateStr = "Drive - Radio Manual Mode";
				break;
			default:
				driveStateStr = "Drive - No details or unrecognized state";
		}
	}
	
	private void setAutoState(byte substate) {
		switch(substate) {
			case Serial.AUTO_STATE_FULL:
				autoStateStr = "AUTO - Full";
				break;
			case Serial.AUTO_STATE_CAUTION:
				autoStateStr = "AUTO - Caution";
				break;
			case Serial.AUTO_STATE_STALLED:
				autoStateStr = "AUTO - Stalled";
				break;
			default:
				autoStateStr = "AUTO - No details or unrecognized state.";
		}
	}
	
	private void setFlagState(byte substate) {
		boolean avoid = ((substate & Serial.AUTO_STATE_FLAGS_AVOID) > 0 ) ? true : false;
		boolean approach = ((substate & Serial.AUTO_STATE_FLAGS_APPROACH) > 0 ) ? true : false;
		
		if(avoid && approach) {
			flagStateStr = "FLAG - Approach & Avoid";
			//Severity High. Approaching a clear obstable? 
		}
		else if(approach) {
			flagStateStr = "FLAG - Approach";
			//Severity Medium. Approaching an obstacle, slowing down?
		}
		else if(avoid) {
			flagStateStr = "FLAG - Avoid";
			//Severity Medium. Avoiding an obstacle? 
		}
		else {
			flagStateStr = "FLAG - None";
			//Severity Low. No hazards detected?
		}
	}
	
	private MouseAdapter stateDetailsMouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent me) {
			
			if(infoFrame != null && infoFrame.isVisible()) {
				infoFrame.toFront();
				return;
			}
			else {
				infoFrame = new JFrame("state info");
				JOptionPane.showMessageDialog(infoFrame, "Click Info String Here");
			}
		}
	};
	
}
