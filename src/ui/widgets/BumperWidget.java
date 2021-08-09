package com.ui.widgets;

import com.Context;
import com.serial.Serial;

import java.awt.*;
import javax.swing.*;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 8-2-2021
 * Description: Dashboard Widget child class used to display the state of
 * the push button bumbper. Left and right buttons are each displayed, 
 * along with a status bar the tracks the corresponding state and decision
 * making in response to button events.
 */
public class BumperWidget extends UIWidget {

	//Constants
	protected final static int LEFT_BUMPER 		= 0;
	protected final static int RIGHT_BUMPER 	= 1;
	
	//Color Defaults
	protected static final Color DEF_INACTIVE_COLOR = Color.decode("0x2CBC00");
	protected static final Color DEF_ACTIVE_COLOR 	= Color.decode("0xD70514");
	
	//Visual Components
	protected JPanel outerPanel;
	protected JPanel lowerPanel;
	protected JPanel leftPanel;
	protected JLabel leftLabel;
	protected JPanel rightPanel;
	protected JLabel rightLabel;
	protected StatusBarWidget statusBar;
	
	//State Tracking Variables
	protected BumperStatus bumperStateLeft;
	protected BumperStatus bumperStateRight;
	
	/**
	 * Pre-defined state enums used to keep track of and
	 * update the currently displayed status label based
	 * on bumper state.
	 */
	protected enum BumperStatus {
		UNKOWN		(0),
		ACTIVATED	(1),
		CLEAR		(2),
		DEFER		(3);
		
		private final int state;
		
		BumperStatus(int state) {
			this.state = state;
		}
		
		public int getValue() {
			return this.state;
		}
		
	};
		
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 */
	public BumperWidget(Context ctx) {
		super(ctx, "Bumper");
		
		Font font = context.theme.text.deriveFont(FONT_SIZE);
		Dimension labelSize = new Dimension(50, 20);
		
		//Set initial bumper states
		bumperStateLeft 	= BumperStatus.CLEAR;
		bumperStateRight 	= BumperStatus.CLEAR;
		
		//Set up outer panel
		outerPanel = new JPanel();
		outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
		
		//Set up lower panel (Houses Left/Right Displays)
		lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
		
		//Set up Status Bar
		statusBar = new StatusBarWidget(context);
		statusBar.update(StatusBarWidget.StatusType.CLEAR);
		outerPanel.add(statusBar);
		
		//Set up left button label/panel
		leftLabel 	= new JLabel("Left", SwingConstants.CENTER);
		leftLabel.setBackground(DEF_INACTIVE_COLOR);
		leftLabel.setOpaque(true);
		leftLabel.setMaximumSize(labelSize);
		
		leftPanel = new JPanel();
		leftPanel.setBorder(insets);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
		leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		leftPanel.setPreferredSize(labelSize);
		leftPanel.add(leftLabel);
		lowerPanel.add(leftPanel);
	
		//Set up right button label/panel
		rightLabel 	= new JLabel("Right", SwingConstants.CENTER);
		rightLabel.setBackground(DEF_INACTIVE_COLOR);
		rightLabel.setOpaque(true);
		rightLabel.setMaximumSize(labelSize);
	
		rightPanel = new JPanel();
		rightPanel.setBorder(insets);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
		rightPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		rightPanel.setPreferredSize(labelSize);
		rightPanel.add(rightLabel);
		lowerPanel.add(rightPanel);
		
		outerPanel.add(lowerPanel);
		this.add(outerPanel);
	}
	
	/**
	 * Starts updates of the state, and color of the received target bumper. Once 
	 * this operation is completed, the widget status label is evaluated for needed 
	 * updates also.
	 * @param bumper - The bumper to be updated
	 * @param state - the received state of the bumper
	 */
	public void update(int bumper, int state) {
		BumperStatus newState;
		
		//Determine incoming state
		switch(state) {
			case 0:
				newState = BumperStatus.CLEAR;
				break;
			case 1:
				newState = BumperStatus.ACTIVATED;
				break;
			default:
				newState = BumperStatus.UNKOWN;
				serialLog.warning("BUMPER: Unrecognized bumper state, cannot update.");
		}
		
		//Update target bumper
		switch(bumper) {
			case LEFT_BUMPER:
				bumperStateLeft = newState;
				break;
			case RIGHT_BUMPER:
				bumperStateRight = newState;
				break;
			default:
				serialLog.warning("BUMPER: Unkown bumper INDEX received on state update.");
		}
		
		//Update Colors & Label
		updateBumperColors();
		updateStatusLabel();
	}
	
	/**
	 * Updates the bumper button widget colors based on the
	 * current states of each button.
	 */
	protected void updateBumperColors() {
		
		//Update Left Bumper
		switch(bumperStateLeft) {
			case CLEAR:
				leftLabel.setBackground(DEF_INACTIVE_COLOR);
				break;
			case ACTIVATED:
				leftLabel.setBackground(DEF_ACTIVE_COLOR);
				break;
			default:
				serialLog.warning("BUMPER: Unknown left bumper STATE received on color update.");
		}
		
		//Update Right Bumper
		switch(bumperStateRight) {
			case CLEAR:
				rightLabel.setBackground(DEF_INACTIVE_COLOR);
				break;
			case ACTIVATED:
				rightLabel.setBackground(DEF_ACTIVE_COLOR);
				break;
			default:
				serialLog.warning("BUMPER: Unknown right bumper STATE received on color update.");
		}
	}
	
	/**
	 * Updates the status label message with respect to
	 * the currently held bumper button states.
	 */
	protected void updateStatusLabel() {
		//If either bumper has been triggered, update the label to reflect this.
		if((bumperStateLeft == BumperStatus.ACTIVATED)
		||(bumperStateRight == BumperStatus.ACTIVATED)) {
			statusBar.update(StatusBarWidget.StatusType.ACTIVATED);
			return;
		}
		
		//Otherwise assume the bumper is clear
		statusBar.update(StatusBarWidget.StatusType.CLEAR);
	}
	
}
