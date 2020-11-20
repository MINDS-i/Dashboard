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
public class StateWidget extends JPanel {
	//Constants
	protected static final int BORDER_SIZE = 0;
	//TODO - CP - Replace this with a settable line width for future xml integration
	protected static final int LINE_WIDTH = 8;
	
	
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
		context = ctx;
		initPanel();
	}

	/**
	 * Construct and place the visual layout elements of the widget. Sets
	 * properties of the elements such as size and format restrictions, 
	 * dimensional spacing, and border insets.
	 */
	private void initPanel() {
		float fontSize = 16.0f;
		Font font = context.theme.text.deriveFont(fontSize);
		
		
		
		Dimension spacer 	= new Dimension(0, 5);
		Dimension labelSize = new Dimension(90, 25);
		Dimension panelSize = new Dimension(90, 25);
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(
				BORDER_SIZE, BORDER_SIZE,BORDER_SIZE, BORDER_SIZE));

		//Configure state labels
		apmLabel 	= new JLabel("Apm:--");
		apmLabel.setFont(font);
		apmLabel.setForeground(Color.decode("0xEA8300"));
		apmLabel.setBackground(Color.decode("0xDFDFDF"));
		apmLabel.setOpaque(true);
		
		driveLabel 	= new JLabel("Drv:--");
		driveLabel.setFont(font);
		driveLabel.setForeground(Color.decode("0xEA8300"));
		driveLabel.setBackground(Color.decode("0xEEEEEE"));
		driveLabel.setOpaque(true);
		
		autoLabel 	= new JLabel("Aut:--");
		autoLabel.setFont(font);
		autoLabel.setForeground(Color.decode("0xEA8300"));
		autoLabel.setBackground(Color.decode("0xDFDFDF"));
		autoLabel.setOpaque(true);
		
		flagLabel 	= new JLabel("Flg:None");
		flagLabel.setFont(font);
		flagLabel.setForeground(Color.decode("0xEA8300"));
		flagLabel.setBackground(Color.decode("0xEEEEEE"));
		flagLabel.setOpaque(true);
		
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
	 * @param substate - The sub state variation to be updated to
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
	 * @param substate - The sub state variation to be set
	 */
	private void setAPMState(byte substate) {
		int finalWidth;
		String fmt;
		String fmtStr = "Apm:%s";
		
		System.err.println("StateWidget - Updating APM State");
		switch(substate) {
			case Serial.APM_STATE_INIT:
				fmt = String.format(fmtStr, "Init");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			case Serial.APM_STATE_SELF_TEST:
				
				fmt = String.format(fmtStr, "Self Test");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			case Serial.APM_STATE_DRIVE:
				fmt = String.format(fmtStr, "Driving");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			default:
				fmt = String.format(fmtStr, "Unknown");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
		}
		apmLabel.setText(fmt.substring(0, finalWidth));
	}
	
	/**
	 * Sets the current Drive state.
	 * @param substate - The sub state variation to be set
	 */
	private void setDriveState(byte substate) {
		int finalWidth;
		String fmt;
		String fmtStr = "Drv:%s";
		
//		System.err.println("StateWidget - Updating Drive State");
		switch(substate) {
			case Serial.DRIVE_STATE_STOP:
				fmt = String.format(fmtStr, "Stopped");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			case Serial.DRIVE_STATE_AUTO:
				fmt = String.format(fmtStr, "Auto");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			case Serial.DRIVE_STATE_RADIO:
				fmt = String.format(fmtStr, "Manual");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			default:
				fmt = String.format(fmtStr, "Unknown");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
		}
		driveLabel.setText(fmt.substring(0, finalWidth));
	}
	
	/**
	 * Sets the current Auto state.
	 * @param substate - The sub state variation to be set
	 */
	private void setAutoState(byte substate) {
		int finalWidth;
		String fmt;
		String fmtStr = "Aut:%s";
		
//		System.err.println("StateWidget - Updating Auto State");
		switch(substate) {
			case Serial.AUTO_STATE_FULL:
				fmt = String.format(fmtStr, "Full");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			case Serial.AUTO_STATE_AVOID:
				fmt = String.format(fmtStr, "Avoid");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			case Serial.AUTO_STATE_STALLED:
				fmt = String.format(fmtStr, "Stalled");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
				break;
			default:
				fmt = String.format(fmtStr, "Unknown");
				finalWidth = Math.min(fmt.length(), LINE_WIDTH);
		}
		autoLabel.setText(fmt.substring(0, finalWidth));
	}
	
	/**
	 * Sets the current state flag if any.
	 * @param substate - The flag type to be set
	 */
	private void setFlagState(byte substate) {
		int finalWidth;
		String fmt;
		String fmtStr = "Flg:%s";
		
		boolean caution  = ((substate & Serial.AUTO_STATE_FLAGS_CAUTION)  > 0 ) ? true : false;
		boolean approach = ((substate & Serial.AUTO_STATE_FLAGS_APPROACH) > 0 ) ? true : false;
		
//		System.out.println("StateWidget - Updating Flag State");
		if(caution && approach) {
			fmt = String.format(fmtStr, "App. & Caut.");
			finalWidth = Math.min(fmt.length(), LINE_WIDTH);
		}
		else if(approach) {
			fmt = String.format(fmtStr, "Approach");
			finalWidth = Math.min(fmt.length(), LINE_WIDTH);
		}
		else if(caution) {
			fmt = String.format(fmtStr, "Caution");
			finalWidth = Math.min(fmt.length(), LINE_WIDTH); 
		}
		else {
			fmt = String.format(fmtStr, "None");
			finalWidth = Math.min(fmt.length(), LINE_WIDTH);
		}
		flagLabel.setText(fmt.substring(0, finalWidth));
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
