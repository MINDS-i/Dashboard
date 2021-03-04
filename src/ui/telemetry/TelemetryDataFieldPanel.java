package com.ui.telemetry;

import javax.swing.*;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-3-21
 * Description: Custom JPanel designed to be used within a table view, allowing for control
 * of configurable telemetry settings using JSliders.
 */
public class TelemetryDataFieldPanel extends JPanel {

	//Visual Components
	public JPanel 		panel;
	public JLabel 		telemName;
	public JTextField 	telemValField;
	public JSlider 		telemValSlider;
	
	public TelemetryDataFieldPanel() {
		 
	}
}
