package com.ui;


import com.Context;
import com.map.*;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.text.Format;
import javax.swing.*;

public class UIConfigPanel extends JPanel {
	
	private static final int DEF_TEXT_FIELD_WIDTH 	= 6;
	private static final int DEF_BUTTON_WIDTH 		= 200;
	private static final int DEF_BUTTON_HEIGHT 		= 30;
	private static final String DEF_HOME_COORD		= "0.0";
	
	private Context 	context;
	
	private JPanel 		buttonPanel;
	private JButton		toggleButton;
	private JButton		driverButton;
	private JPanel 		homePanel;
	private JPanel		lngPanel;
	private JLabel 		lngLabel;
	private JTextField 	lngField;
	private JPanel		latPanel;
	private JLabel 		latLabel;
	private JTextField 	latField;
	private JButton 	setHomeButton;
	
	public UIConfigPanel(Context cxt, boolean isWindows) {
		this.context = cxt;
		
		this.setLayout(new GridBagLayout());
		//Constraints persist between component applications, so any properties we
		//don't want more than one component to share need to be explicitly defined
		//before being applied to that a compoenent.
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0,5,0,5);
		
		toggleButton = new JButton(toggleLocaleAction);
		toggleButton.setPreferredSize(
				new Dimension(DEF_BUTTON_WIDTH,DEF_BUTTON_HEIGHT));
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.add(toggleButton, constraints);
		
		if(isWindows) {
			driverButton = new JButton(driverExecAction);
			driverButton.setPreferredSize(
					new Dimension(DEF_BUTTON_WIDTH,DEF_BUTTON_HEIGHT));
			constraints.gridx = 0;
			constraints.gridy = 1;
			this.add(driverButton, constraints);	
		}
		
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
	}
	
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
    
    private Action driverExecAction = new AbstractAction() {
        {
            String text = "Launch driver installer";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            String[] cmd = { "RadioDiversv2.12.06WHQL_Centified.exe" };
            try {
                Process p = Runtime.getRuntime().exec(cmd);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    
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
