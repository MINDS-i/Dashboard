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
	
	
	private JLabel apmLabel;
	private JLabel driveLabel;
	private JLabel autoLabel;
	private JLabel flagLabel;
	
	private String apmStateStr;
	private String driveStateStr;
	private String autoStateStr;
	private String flagStateStr;
	
	public StateWidget(Context ctx) {
		context = ctx;
		
		apmStateStr 	= "APM - Uninit";
		driveStateStr 	= "DRIVE - Uninit";
		autoStateStr 	= "AUTO - Uninit";
		flagStateStr 	= "FLAGS - None";
		
		apmLabel 	= new JLabel(apmStateStr);
		driveLabel 	= new JLabel(driveStateStr);
		autoLabel 	= new JLabel(autoStateStr);
		flagLabel 	= new JLabel(flagStateStr);
		
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(apmLabel);
		this.add(driveLabel);
		this.add(autoLabel);
		this.add(flagLabel);
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
				break;
			default:
				System.err.println("Error - Unrecognized State");
		}
	}
	
	private void setAPMState(byte substate) {
		System.err.println("StateWidget - Updating APM State");
		
		switch(substate) {
			case Serial.APM_STATE_INIT:
				apmLabel.setText("APM - Initializing");
				break;
			case Serial.APM_STATE_SELF_TEST:
				apmLabel.setText("APM - Performing Self Test");
				break;
			case Serial.APM_STATE_DRIVE:
				apmLabel.setText("APM - Driving");
				break;
			default:
				apmLabel.setText("APM - Unknown State");
		}
	}
	
	private void setDriveState(byte substate) {
		System.err.println("StateWidget - Updating Drive State");
		
		switch(substate) {
			case Serial.DRIVE_STATE_STOP:
				driveLabel.setText("DRIVE - Stopped");
				break;
			case Serial.DRIVE_STATE_AUTO:
				driveLabel.setText("DRIVE - Auto Mode");
				break;
			case Serial.DRIVE_STATE_RADIO:
				driveLabel.setText("DRIVE - Radio Manual Mode");
				break;
			default:
				driveLabel.setText("DRIVE - Unknown State");
		}
	}
	
	private void setAutoState(byte substate) {
		System.err.println("StateWidget - Updating Auto State");
		
		switch(substate) {
			case Serial.AUTO_STATE_FULL:
				autoLabel.setText("AUTO - Full");
				break;
			case Serial.AUTO_STATE_CAUTION:
				autoLabel.setText("AUTO - Caution");
				break;
			case Serial.AUTO_STATE_STALLED:
				autoLabel.setText("AUTO - Stalled");
				break;
			default:
				autoLabel.setText("AUTO - Unknown State");
		}
	}
	
	private void setFlagState(byte substate) {
		boolean avoid    = ((substate & Serial.AUTO_STATE_FLAGS_AVOID) 	  > 0 ) ? true : false;
		boolean approach = ((substate & Serial.AUTO_STATE_FLAGS_APPROACH) > 0 ) ? true : false;
		
		System.out.println("StateWidget - Updating Flag State");
		
		if(avoid && approach) {
			flagLabel.setText("FLAG - Approach & Avoid");
			//Severity High. Approaching a clear obstable? 
		}
		else if(approach) {
			flagLabel.setText("FLAG - Approach");
			//Severity Medium. Approaching an obstacle, slowing down?
		}
		else if(avoid) {
			flagLabel.setText("FLAG - Avoid");
			//Severity Medium. Avoiding an obstacle? 
		}
		else {
			flagLabel.setText("FLAG - None");
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
