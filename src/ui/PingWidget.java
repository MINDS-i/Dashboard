package com.ui;

import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import com.Context;

/** 
 * @author Chris Park @ Infinetix Corp.
 * Date: 12-2-20
 * Description: Dashboard Widget child class used to display a units
 * ping sensor data.
 */
public class PingWidget extends UIWidget {
	
	//Constants
	protected static final int NUM_SENSORS 	  = 5;
	protected static final int[] WARN_LEVELS  = {1000, 1600, 3000, 1600, 1000};
	protected static final int[] BLOCK_LEVELS = {1500, 2400, 4500, 2400, 1500};
	
	//Ping Sensor Indicators
	protected Hashtable<Integer, ArrayList<JLabel>> sensors;
	
	
	//Sensor Values
	protected int[] curSensorVals;
	
	//Sensor Image Panel
	protected JPanel sensorOuterPanel;
	protected JPanel sensorPanelA;
	protected JPanel sensorPanelB;
	protected JPanel sensorPanelC;
	protected JPanel sensorPanelD;
	protected JPanel sensorPanelE;
	
	/**
	 * Class Constructor
	 * Generates the initial layout for the widget, loading graphics and placing
	 * them within a defined JPanel layout
	 * @param ctx - The application context
	 */
	public PingWidget(Context ctx) {
		super(ctx, "U-Sound Ping");
		
		ArrayList<JLabel> temp;
		Component widthSpacer = Box.createHorizontalStrut(20);
		
		sensors = new Hashtable<Integer, ArrayList<JLabel>>();
		curSensorVals = new int[5];

		for(int i = 0; i < NUM_SENSORS; i++) {
			temp = new ArrayList<JLabel>();
			temp.add(new JLabel(new ImageIcon(ctx.theme.pingRed)));
			temp.add(new JLabel(new ImageIcon(ctx.theme.pingYellow)));
			temp.add(new JLabel(new ImageIcon(ctx.theme.pingGreen)));
			sensors.put(i, temp);
		}
		
		sensorOuterPanel = new JPanel();
		sensorOuterPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 0, 2, 0);
		constraints.anchor = GridBagConstraints.CENTER;
		
		
		sensorPanelA = new JPanel();
		sensorPanelA.setLayout(new BoxLayout(sensorPanelA, BoxLayout.Y_AXIS));
		sensorPanelA.add(widthSpacer);
		sensorPanelA.add(sensors.get(0).get(0));
		sensorPanelA.add(sensors.get(0).get(1));
		sensorPanelA.add(sensors.get(0).get(2));
		constraints.gridx = 0;
		constraints.gridy = 0;
		sensorOuterPanel.add(sensorPanelA, constraints);

		sensorPanelB = new JPanel();
		sensorPanelB.setLayout(new BoxLayout(sensorPanelB, BoxLayout.Y_AXIS));
		sensorPanelB.add(widthSpacer);
		sensorPanelB.add(sensors.get(1).get(0));
		sensorPanelB.add(sensors.get(1).get(1));
		sensorPanelB.add(sensors.get(1).get(2));
		constraints.gridx = 1;
		constraints.gridy = 0;
		sensorOuterPanel.add(sensorPanelB, constraints);
		
		sensorPanelC = new JPanel();
		sensorPanelC.setLayout(new BoxLayout(sensorPanelC, BoxLayout.Y_AXIS));
		sensorPanelC.add(widthSpacer);
		sensorPanelC.add(sensors.get(2).get(0));
		sensorPanelC.add(sensors.get(2).get(1));
		sensorPanelC.add(sensors.get(2).get(2));
		constraints.gridx = 2;
		constraints.gridy = 0;
		sensorOuterPanel.add(sensorPanelC, constraints);

		sensorPanelD = new JPanel();
		sensorPanelD.setLayout(new BoxLayout(sensorPanelD, BoxLayout.Y_AXIS));
		sensorPanelD.add(widthSpacer);
		sensorPanelD.add(sensors.get(3).get(0));
		sensorPanelD.add(sensors.get(3).get(1));
		sensorPanelD.add(sensors.get(3).get(2));
		constraints.gridx = 3;
		constraints.gridy = 0;
		sensorOuterPanel.add(sensorPanelD, constraints);

		sensorPanelE = new JPanel();
		sensorPanelE.setLayout(new BoxLayout(sensorPanelE, BoxLayout.Y_AXIS));
		sensorPanelE.add(widthSpacer);
		sensorPanelE.add(sensors.get(4).get(0));
		sensorPanelE.add(sensors.get(4).get(1));
		sensorPanelE.add(sensors.get(4).get(2));
		constraints.gridx = 4;
		constraints.gridy = 0;
		sensorOuterPanel.add(sensorPanelE, constraints);

		this.add(sensorOuterPanel);
		
		
		//Test show/hide
//		updateSensorBar(3);
	}
	
	/**
	 * Updates the tracked value and visual display for the indexed sensor.
	 * @param index - The sensor number
	 * @param data - The current value from this sensor.
	 */
	public void update(int index, int data) {

		//Return on out of bounds
		if(index > (NUM_SENSORS - 1) || index < 0) {
			return;
		}

		curSensorVals[index] = data;
		updateSensorBar(index);
	}
	
	/**
	 * Updates the visual display for a sensor based on the current
	 * value.
	 * @param index - The index of the sensor
	 */
	protected void updateSensorBar(int index) {
	
		if(index >= NUM_SENSORS) {
			System.err.println("Error - Invalid PingSensor range access attempt.");
			return;
		}
		
		ArrayList<JLabel> temp = sensors.get(index);
		for (JLabel meter : temp) {
			meter.setVisible(false);
		}
		
		//method A
		//hide all
		//if < warn /2 and greater than 0
			//show green
		
		//if >= warn level
			//show yellow
		
		//if >= block level
			//show red
		
		
		//method B
		//if less than warn level / 2 hide green
		//else show green
		
		//if >= warn level show yellow
		//else hide yellow
		
		//if >= block, show red
		//else hide red
		
	}
	
	/**
	 * Gets the current value for a sensor.
	 * @param index - The index of the sensor
	 * @return - the current value stored for that sensor
	 */
	public int getSensorValue(int index) {
		return curSensorVals[index];
	}
	
	/**
	 * Gets the number of sensors traked by this widget.
	 * @return - the number of sensors
	 */
	public int getNumSensors() {
		return NUM_SENSORS;
	}
}
