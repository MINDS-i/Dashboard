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
public class StatusBarWidget extends UIWidget {
	
	//Constants
	protected static final int MIN_BAR_WIDTH	= 100;
	protected static final int BAR_HEIGHT 		= 25;
	
	protected JLabel statusLabel;
	
	//Class Constructor
	public StatusBarWidget(Context ctx, String barType) {
		super(ctx, barType + "StatusBar");
		
		statusLabel.setText("Undefined");
		statusLabel.setBackground(Color.white);
		
		statusLabel.setMinimumSize(new Dimension(MIN_BAR_WIDTH, BAR_HEIGHT));
		statusLabel.setPreferredSize(new Dimension(MIN_BAR_WIDTH, BAR_HEIGHT));
	}
	
	/**
	 * Updates the status bar text display to the given string parameter.
	 * @param text - The text to update the label to.
	 */
	public void setLabel(String text) {
		statusLabel.setText(text);
	}
	
	/**
	 * Updates the status bar background color to the given Color parameter
	 * @param color - The color to set the background do
	 */
	public void setColor(Color color) {
		statusLabel.setBackground(color);
	}
	
	/**
	 * Updates the preferred size of the status bar
	 * @param size - the Dimensional size (Width, Height) to set the bar to.
	 */
	public void setSize(Dimension size) {
		statusLabel.setPreferredSize(size);
	}
}
