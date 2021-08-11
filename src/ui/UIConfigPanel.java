package com.ui;

import com.Context;
import com.map.*;

import java.io.*;
import java.text.Format;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.util.ACLIManager;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 09-2020
 * Description: UI Panel responsible for providing UI focused options to the user.
 * Allows for toggling air/ground mode, and saving the current Home location to persistent
 * settings. 
 */
public class UIConfigPanel extends JPanel {
	
	//Constants
	private static final int 	DEF_TEXT_FIELD_WIDTH 	= 6;
	private static final int 	DEF_BUTTON_WIDTH 		= 200;
	private static final int 	DEF_BUTTON_HEIGHT 		= 30;
	private static final String DEF_HOME_COORD			= "0.0";
	
	//UI Componenets
	private JPanel 		buttonPanel;
	private JButton		toggleButton;
	private JButton		driverButton;
	private JButton		sketchUploadButton;
	private JPanel 		homePanel;
	private JPanel		lngPanel;
	private JLabel 		lngLabel;
	private JTextField 	lngField;
	private JPanel		latPanel;
	private JLabel 		latLabel;
	private JTextField 	latField;
	private JButton 	setHomeButton;
	
	private JCheckBox	bumperCheckBox;
	private JButton		applySettingsButton;
	
	//State and Reference vars
	private Context 	context;
	private boolean		bumperIsEnabled;
	
	/**
	 * Class constructor
	 * @param cxt - the application context
	 * @param isWindows - boolean check to see if the application is running in windows
	 */
	public UIConfigPanel(Context cxt, boolean isWindows) {
		this.context = cxt;
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0,5,0,5);
		
		//Unit type toggle button(Rover/Copter)
		toggleButton = new JButton(toggleLocaleAction);
		toggleButton.setPreferredSize(
				new Dimension(DEF_BUTTON_WIDTH, DEF_BUTTON_HEIGHT));
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.add(toggleButton, constraints);
		
		//Driver installation button (Deprecated - Windows Only)
		//Note - CP - 8-10-21: There is an updated version of the driver, 
		//and also an automatic installation of this driver from the 
		//installer executable now. This should be removed at some point.
		if(isWindows) {
			driverButton = new JButton(driverExecAction);
			driverButton.setPreferredSize(
					new Dimension(DEF_BUTTON_WIDTH, DEF_BUTTON_HEIGHT));
			constraints.gridx = 0;
			constraints.gridy = 1;
			this.add(driverButton, constraints);	
		}
		
		//Sketch upload button
		sketchUploadButton = new JButton(uploadSketchAction);
		sketchUploadButton.setPreferredSize(
				new Dimension(DEF_BUTTON_WIDTH, DEF_BUTTON_HEIGHT));
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.add(sketchUploadButton, constraints);
		
		//Home coordinates fields and set button
		Point2D homeCoords = context.getHomeProp();
		
		latLabel = new JLabel("Latitiude:");
		constraints.gridx = 1;
		constraints.gridy = 0;
		this.add(latLabel, constraints);
		
		latField = new JTextField(DEF_TEXT_FIELD_WIDTH);
		constraints.gridx = 2;
		constraints.gridy = 0;
		this.add(latField, constraints);
		latField.setText(String.valueOf(homeCoords.getX()));
		
		lngLabel = new JLabel("Longitude:");
		constraints.gridx = 1;
		constraints.gridy = 1;
		this.add(lngLabel, constraints);
		
		lngField = new JTextField(DEF_TEXT_FIELD_WIDTH);
		constraints.gridx = 2;
		constraints.gridy = 1;
		this.add(lngField, constraints);
		lngField.setText(String.valueOf(homeCoords.getY()));
		
		setHomeButton = new JButton(setHomeAction);
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		this.add(setHomeButton, constraints);
		
		//Bumper toggle checkbox
		bumperCheckBox = new JCheckBox("Enable Bumper");
		bumperCheckBox.setSelected(true);
		constraints.gridx = 3;
		constraints.gridy = 1;
		this.add(bumperCheckBox, constraints);
		this.bumperIsEnabled = true;
		
		//Settings apply button (for Checkboxes/Radio Buttons)
		applySettingsButton = new JButton(setSettingsAction);
		constraints.gridx = 3;
		constraints.gridy = 2;
		this.add(applySettingsButton, constraints);
	}
	
	/**
	 * Action used to update settings based on selectable radio buttons and
	 * check boxes.
	 */
	private Action setSettingsAction = new AbstractAction() {
		{
			String text = "Apply Settings";
			putValue(Action.NAME, text);
		}
		
		//NOTE - CP - Message should be ack'd by rover yes? Need to update this.
		public void actionPerformed(ActionEvent e) {
			
			//Bumper enable/disable toggle
			if(bumperIsEnabled && !bumperCheckBox.isSelected()) {
				bumperIsEnabled = false;
				context.sender.toggleBumper(bumperIsEnabled);
				
				//TODO - Gray out/disable widget
				
			}
			else if(!bumperIsEnabled && bumperCheckBox.isSelected()) {
				bumperIsEnabled = true;
				context.sender.toggleBumper(bumperIsEnabled);
				
				//TODO - Enable widget
			}
			
		} 
	};
	
	/**
	 * Action used to toggle the user interface between Air and Ground mode.
	 * Requires a program restart for the setting to take effect.
	 */
    private Action toggleLocaleAction = new AbstractAction() {
        {
            String text = "Toggle ground/air mode";
            putValue(Action.NAME, text);
        }
        
        public void actionPerformed(ActionEvent e) {
            context.toggleLocale();
            JFrame mf = new JFrame("message");
            JOptionPane.showMessageDialog(mf, 
            		"Changes will take effect next launch");
        }
    };
    
    /**
     * Action used to install the Required Radio Telemtry drivers.
     * (Deprecated) - This is now handled by the program installer. 
     */
    private Action driverExecAction = new AbstractAction() {
        {
            String text = "Launch driver installer";
            putValue(Action.NAME, text);
        }
        
        public void actionPerformed(ActionEvent e) {
            String[] cmd = { "RadioDiversv2.12.06WHQL_Centified.exe" };
            try {
                Process p = Runtime.getRuntime().exec(cmd);
            } 
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    
    /**
     * Action used to open a FileChooser dialog, and
     * select and upload an Arduino sketch to the APM.
     */
    private Action uploadSketchAction = new AbstractAction() {
    	{
    		String text = "Upload arduino sketch";
    		putValue(Action.NAME, text);
    	}
    	
    	public void actionPerformed(ActionEvent e) {
    		File selectedFile = null;
    		JFileChooser fileChooser = new JFileChooser();
    		FileFilter filter = new FileNameExtensionFilter(
    				"Arduino Sketch (*.ino)", "ino");
    		    		
    		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    		fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter());
    		fileChooser.addChoosableFileFilter(filter);
    		
    		int retVal = fileChooser.showOpenDialog(UIConfigPanel.this);
    		if(retVal == JFileChooser.APPROVE_OPTION) {
    			selectedFile = fileChooser.getSelectedFile();
    		}
    		
    		if (selectedFile == null) {
    			System.err.println(
    					"UIConfigPanel - Upload error: No file was selected.");
    			return;
    		}
    		
    		boolean result = false;
    		
			//Install avr core used by APM    		
    		result = ACLIManager.getInstance().execute(
    				ACLIManager.ACLICommand.INSTALL_AVR_CORE);
    		if (result == false) {
    			JOptionPane.showMessageDialog(UIConfigPanel.this, 
    					ACLIManager.getInstance().getErrorStr(),
    					"Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		System.err.println("Successful Core Install");
    		
    		
			//Build list of available/connected boards
    		result = ACLIManager.getInstance().execute(
					ACLIManager.ACLICommand.GENERATE_BOARD_LIST);
    		if (result == false) {
    			JOptionPane.showMessageDialog(UIConfigPanel.this, 
    					ACLIManager.getInstance().getErrorStr(),
    					"Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		System.err.println("Successful Board List Generation");
    		
			//Parse core and port info
    		result = ACLIManager.getInstance().execute(
					ACLIManager.ACLICommand.PARSE_BOARD_INFO);
    		if (result == false) {
    			JOptionPane.showMessageDialog(UIConfigPanel.this, 
    					ACLIManager.getInstance().getErrorStr(),
    					"Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		System.err.println("Successful Board List Parsing");
    		
			//Compile selected sketch
    		result = ACLIManager.getInstance().execute(
					ACLIManager.ACLICommand.COMPILE_SKETCH, 
					selectedFile.getAbsolutePath());
    		if (result == false) {
    			JOptionPane.showMessageDialog(UIConfigPanel.this,
    					ACLIManager.getInstance().getErrorStr(),
    					"Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		System.err.println("Successful Sketch Compile");
    		
			//Upload selected sketch
    		result = ACLIManager.getInstance().execute(
					ACLIManager.ACLICommand.UPLOAD_SKETCH,
					selectedFile.getAbsolutePath());
    		if (result == false) {
    			JOptionPane.showMessageDialog(UIConfigPanel.this,
    					ACLIManager.getInstance().getErrorStr(),
    					"Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		System.err.println("Successful Sketch Upload");
    		
    		JOptionPane.showMessageDialog(UIConfigPanel.this, 
    				"Sketch uploaded successfully.",
    				"Info", JOptionPane.INFORMATION_MESSAGE);
    	}
    };
    
    /**
     * Action used to set the home longitude and latitude and save it to persistent 
     * settings.
     */
    private Action setHomeAction = new AbstractAction() {
    	{
    		String text = "Set Home";
    		putValue(Action.NAME, text);
    		putValue(Action.SHORT_DESCRIPTION, text);
    	}
    	
    	public void actionPerformed(ActionEvent e) {
    		String lat = latField.getText();
    		String lng = lngField.getText();
    		
    		//If the input from the fields looks bad, use a default
    		if(lat.isEmpty() || lat == null) {
    			lat = DEF_HOME_COORD;
    		}
    		if(lng.isEmpty() || lng == null) {
    			lng = DEF_HOME_COORD;
    		}
    		
    		WaypointList list = context.getWaypointList();
    		Dot location = list.getHome();
    		location.setLatitude(Double.parseDouble(lat));
    		location.setLongitude(Double.parseDouble(lng));
    		list.setHome(location);
    		
    		context.setHomeProp(latField.getText(),
    				lngField.getText());
    		
    		JFrame mf = new JFrame("message");
            JOptionPane.showMessageDialog(mf, 
            		"Home location has been set.");
    	}
    };
}
