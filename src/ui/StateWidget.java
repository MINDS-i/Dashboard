package com.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.Context;
import com.serial.Serial;
import com.ui.ninePatch.NinePatchPanel;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 10-28-20
 * Description: Dashboard Widget used to display the current state of a connected unit as
 * described over serial communication.
 */
public class StateWidget extends NinePatchPanel {
	//Constants
	protected static final int BORDER_SIZE = 25;
	
	private Context context;
	private JFrame infoFrame;
	
	//State readouts
	private JPanel apmPanel;
	private JLabel apmLabel;	
	private JPanel drivePanel;
	private JLabel driveLabel;
	private JPanel autoPanel;
	private JLabel autoLabel;
	private JPanel flagPanel;
	private JLabel flagLabel;
	
	/**
	 * Class constructor
	 * @param ctx - The application context
	 */
	public StateWidget(Context ctx) {
		super(ctx.theme.panelPatch);
		context = ctx;
		initPanel();
	}

	/**
	 * Construct and place the visual elements of the widget
	 */
	private void initPanel() {
		Dimension spacer 	= new Dimension(0, 5);
		Dimension labelSize = new Dimension(90, 25);
		Dimension panelSize = new Dimension(90, 25);
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(
				BORDER_SIZE, BORDER_SIZE,BORDER_SIZE, BORDER_SIZE));

		//Configure state labels
		apmLabel 	= new JLabel("APM - ??");
		driveLabel 	= new JLabel("DRV - ??");
		autoLabel 	= new JLabel("AUT - ??");
		flagLabel 	= new JLabel("FLG - None");
		
		JComponent[] labelList = new JComponent[] {
			apmLabel, driveLabel, autoLabel, flagLabel	
		};
		
		for(JComponent jc : labelList) {
			jc.setAlignmentX(Component.CENTER_ALIGNMENT);
			jc.setMaximumSize(labelSize);
		}
		
		//Configure state panels
		ArrayList<JPanel> statePanels = new ArrayList<JPanel>();
		apmPanel = new JPanel();
		apmPanel.setLayout(new BoxLayout(apmPanel, BoxLayout.LINE_AXIS));
		apmPanel.setPreferredSize(panelSize);
		apmPanel.setOpaque(true);
		apmPanel.add(apmLabel);
		statePanels.add(apmPanel);
		
		drivePanel = new JPanel();
		drivePanel.setLayout(new BoxLayout(drivePanel, BoxLayout.LINE_AXIS));
		drivePanel.setPreferredSize(panelSize);
		drivePanel.setOpaque(true);
		drivePanel.add(driveLabel);
		statePanels.add(drivePanel);
		
		autoPanel = new JPanel();
		autoPanel.setLayout(new BoxLayout(autoPanel, BoxLayout.LINE_AXIS));
		autoPanel.setPreferredSize(panelSize);
		autoPanel.setOpaque(true);
		autoPanel.add(autoLabel);
		statePanels.add(autoPanel);
		
		flagPanel = new JPanel();
		flagPanel.setLayout(new BoxLayout(flagPanel, BoxLayout.LINE_AXIS));
		flagPanel.setPreferredSize(panelSize);
		flagPanel.setOpaque(true);
		flagPanel.add(flagLabel);
		statePanels.add(flagPanel);
		
		//Add panels to widget
		for(JPanel panel : statePanels) {
			this.add(panel);
			this.add(Box.createRigidArea(spacer));
		}
	}
	
	/**
	 * Updates the internal state of the widget using byte data received from
	 * serial communications with a device.
	 * @param state - The main state type being updated
	 * @param substate - The main state variation to be updated to
	 */
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
	
	/**
	 * Sets the current APM state.
	 * @param substate - The state variation to be set
	 */
	private void setAPMState(byte substate) {
//		System.err.println("StateWidget - Updating APM State");
		
		switch(substate) {
			case Serial.APM_STATE_INIT:
				apmLabel.setText("APM - Init");
				apmPanel.setBackground(Color.white);
				break;
			case Serial.APM_STATE_SELF_TEST:
				apmLabel.setText("APM - Self Test");
				apmPanel.setBackground(Color.white);
				break;
			case Serial.APM_STATE_DRIVE:
				apmLabel.setText("APM - Driving");
				apmPanel.setBackground(Color.green);
				break;
			default:
				apmLabel.setText("APM - Unknown");
				apmPanel.setBackground(Color.red);
		}
	}
	
	/**
	 * Sets the current Drive state.
	 * @param substate - The state variation to be set
	 */
	private void setDriveState(byte substate) {
//		System.err.println("StateWidget - Updating Drive State");
		
		switch(substate) {
			case Serial.DRIVE_STATE_STOP:
				driveLabel.setText("DRV - Stopped");
				drivePanel.setBackground(Color.white);
				break;
			case Serial.DRIVE_STATE_AUTO:
				driveLabel.setText("DRV - Auto");
				drivePanel.setBackground(Color.green);
				break;
			case Serial.DRIVE_STATE_RADIO:
				driveLabel.setText("DRV - Manual");
				drivePanel.setBackground(Color.green);
				break;
			default:
				driveLabel.setText("DRV - Unknown");
				drivePanel.setBackground(Color.red);
		}
	}
	
	/**
	 * Sets the current Auto state.
	 * @param substate - The state variation to be set
	 */
	private void setAutoState(byte substate) {
//		System.err.println("StateWidget - Updating Auto State");
		
		switch(substate) {
			case Serial.AUTO_STATE_FULL:
				autoLabel.setText("AUT - Full");
				autoPanel.setBackground(Color.green);
				break;
			case Serial.AUTO_STATE_AVOID:
				autoLabel.setText("AUT - Avoid");
				autoPanel.setBackground(Color.yellow);
				break;
			case Serial.AUTO_STATE_STALLED:
				autoLabel.setText("AUT - Stalled");
				autoPanel.setBackground(Color.red);
				break;
			default:
				autoLabel.setText("AUT - Unknown State");
				autoPanel.setBackground(Color.red);
		}
	}
	
	//TODO - CP - Update flag string AND set an icon indicating severity level
	
	/**
	 * Sets the current state flag if any.
	 * @param substate - The flag type to be set
	 */
	private void setFlagState(byte substate) {
		boolean caution  = ((substate & Serial.AUTO_STATE_FLAGS_CAUTION)  > 0 ) ? true : false;
		boolean approach = ((substate & Serial.AUTO_STATE_FLAGS_APPROACH) > 0 ) ? true : false;
		
//		System.out.println("StateWidget - Updating Flag State");
		
		if(caution && approach) {
			flagLabel.setText("FLG - App. & Caut.");
			flagPanel.setBackground(Color.yellow);
			//Severity High. Approaching a clear obstable? 
		}
		else if(approach) {
			flagLabel.setText("FLG - Approach");
			flagPanel.setBackground(Color.yellow);
			//Severity Medium. Approaching an obstacle, slowing down?
		}
		else if(caution) {
			flagLabel.setText("FLG - Caution");
			flagPanel.setBackground(Color.yellow);
			//Severity Medium. Avoiding an obstacle? 
		}
		else {
			flagLabel.setText("FLG - None");
			flagPanel.setBackground(Color.green);
			//Severity Low. No hazards detected?
		}
	}
	
	/**
	 * Generates an information panel on click describing the warnings, errors,
	 * and details of the current state.
	 */
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
