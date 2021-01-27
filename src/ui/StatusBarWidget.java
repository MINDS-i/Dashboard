package com.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.Context;
//import com.serial.Serial;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-26-21
 * Description: Dashboard Widget child class used to display contextual vehicle 
 * information in an easy to interpret visual format. Example use cases would be
 * displaying a units current decision state, or perhaps a visual warning level
 * indication related to a vehicle sensor or other data processing part over
 * serial communication.
 */
public class StatusBarWidget extends JPanel {
	
	//Constants
	protected static final int MIN_BAR_WIDTH	= 100;
	protected static final int BAR_HEIGHT 		= 25;
	
	//Bar Display Label
	protected JLabel statusLabel;
	
	/**
	 * Pre-define status type enums used to more concisely track
	 * status bar parameters by type and allow for easier updating.
	 */
	public enum StatusType {
		NORMAL		("Normal", 	Color.white), 
		PROCESSING	("Process", Color.blue), 
		CAUTION		("Caution", Color.yellow),
		ERROR		("Error", 	Color.red);
		
		private final String text;
		private final Color color;
		
		StatusType(String text, Color color) {
			this.text = text;
			this.color = color;
		}
	};
	
	//Class Constructor
	public StatusBarWidget(Context ctx, String type) {
		statusLabel.setText("Undefined");
		statusLabel.setBackground(Color.white);
		
		statusLabel.setMinimumSize(new Dimension(MIN_BAR_WIDTH, BAR_HEIGHT));
		statusLabel.setPreferredSize(new Dimension(MIN_BAR_WIDTH, BAR_HEIGHT));
	}
	
	public void update(StatusType type) {
		statusLabel.setText(type.text);
		statusLabel.setBackground(type.color);
	}
}
