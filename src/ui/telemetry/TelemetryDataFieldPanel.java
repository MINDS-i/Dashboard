package com.ui.telemetry;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.remote.Setting;
import com.ui.FloatJSlider;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-3-21
 * Description: Custom JPanel designed to be used within a table view, allowing for control
 * of configurable telemetry settings using JSliders.
 */
public class TelemetryDataFieldPanel extends JPanel {

	//Visual Components
	protected JPanel 		panel;
//	protected JLabel 		telemName;
//	protected JTextField 	telemValField;
	protected FloatJSlider 	telemValSlider;

	protected Setting		setting;
	protected int			sigFigs;
	protected int			conversionVal;
	
	public TelemetryDataFieldPanel(Setting setting) {
		this.setting = setting;

		//Creat slider and map values to it.
		//Parse out the decimal places for JSlider mapping (Jslider uses ints for range).
		String str = String.valueOf(setting.getDefault());
		String[] parsed = str.split(".");
		
		//NOTE: length vs length()... length returns the length of the array
		//where as length() returns the number of characters in a string.
		sigFigs = (parsed.length > 2) ? parsed[1].length() : 0;
		conversionVal = (sigFigs < 0) ? (int) Math.pow(10, sigFigs) : 1;

		int min = (int) (setting.getMin() * conversionVal);
		int max = (int) (setting.getMax() * conversionVal);
		telemValSlider = new FloatJSlider(
				min, max, (int) Math.floor(setting.getDefault()), conversionVal);
		telemValSlider.setOpaque(false);
		telemValSlider.setFocusable(false);
		
		telemValSlider.getModel().addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent event) {
				BoundedRangeModel model = (BoundedRangeModel) event.getSource();
				float updateVal = ((float) model.getValue() / (float) conversionVal);

				//Update text field value
//				telemValField.setText(Objects.toString(updateVal));
			}
		});
	}
	
	//TODO - CP - Update/push value change here. (this should fire from editor)
	public void updateValue(Setting setting) {
		
	}
}
