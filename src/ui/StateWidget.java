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
	private byte currentState;
	private byte currentSubState;
	
	private JFrame infoFrame;
	private String stateInfoStr;
	
	public StateWidget(Context ctx) {
		context = ctx;
		//Default state and substate to something inactive here?
	}

	
	public void update(byte state, byte substate) {
		currentState = state;
		currentSubState = substate;
		updateStateInfo();
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
				JOptionPane.showMessageDialog(infoFrame, stateInfoStr);
			}
		}
	};
	
	private void updateStateInfo() {
		switch(currentState) {
			case Serial.APM_STATE:
				setAPMStateInfo();
				//Update Color A
				break;
			case Serial.DRIVE_STATE:
				setDriveStateInfo();
				//Update Color B
				break;
			case Serial.AUTO_STATE:
				setAutoStateInfo();
				//Update Color C
				break;
			default:
				stateInfoStr = "No details or unrecognized state.";
				//Update Color Default for primary and secondary
		}
	}
	
	private void setAPMStateInfo() {
		switch(currentSubState) {
			case Serial.APM_STATE_INIT:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.APM_STATE_SELF_TEST:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.APM_STATE_DRIVE:
				//Update Secondary Color
				stateInfoStr = "";
				break;
			default:
				stateInfoStr = "No details or unrecognized state.";
				//Update Secondary Color
		}
	}
	
	private void setDriveStateInfo() {
		switch(currentSubState) {
			case Serial.DRIVE_STATE_STOP:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.DRIVE_STATE_AUTO:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.DRIVE_STATE_RADIO:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			default:
				stateInfoStr = "No details or unrecognized state.";
				//Update Secondary Color
		}
	}
	
	private void setAutoStateInfo() {
		switch(currentSubState) {
			case Serial.AUTO_STATE_FULL:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.AUTO_STATE_CAUTION:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.AUTO_STATE_AVOID:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.AUTO_STATE_APPROACH:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			case Serial.AUTO_STATE_STALLED:
				stateInfoStr = "";
				//Update Secondary Color
				break;
			default:
				stateInfoStr = "No details or unrecognized state.";
				//Update Secondary Color
		}
	}
}
