package com.ui;


import com.Context;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.Format;
import javax.swing.*;

public class UIConfigPanel extends JPanel {
	
	private static final int DEF_TEXT_FIELD_WIDTH 	= 6;
	private static final int DEF_BUTTON_WIDTH 		= 200;
	private static final int DEF_BUTTON_HEIGHT 		= 30;
	
	private Context 	context;
	
	private JPanel 		buttonPanel;
	private JButton		toggleButton;
	private JButton		driverButton;
	private JPanel 		homePanel;
	private JPanel		lonPanel;
	private JLabel 		lonLabel;
	private JTextField 	lonField;
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
		
		latLabel = new JLabel("Latitiude:");
		constraints.gridx = 1;
		constraints.gridy = 0;
		this.add(latLabel, constraints);
		
		latField = new JTextField(DEF_TEXT_FIELD_WIDTH);
		constraints.gridx = 2;
		constraints.gridy = 0;
		this.add(latField, constraints);
		
		lonLabel = new JLabel("Longitude:");
		constraints.gridx = 1;
		constraints.gridy = 1;
		this.add(lonLabel, constraints);
		
		lonField = new JTextField(DEF_TEXT_FIELD_WIDTH);
		constraints.gridx = 2;
		constraints.gridy = 1;
		this.add(lonField, constraints);
		
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
    		//TODO - CP - Add home point save logic here.
    			//Grab text field info
    			//Save config however possible.
    			//Inform user on success or failure
    	}
    };
}
