package com.ui;

import java.util.*;
//import java.util.Hashtable;
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
	protected Hashtable<Integer, ArrayList<BufferedImage>> sensors;
	
	//Sensor Values
	protected int[] curSensorVals;
	
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 */
	public PingWidget(Context ctx) {
		super(ctx, "U-Sound Ping");
		
		ArrayList<BufferedImage> temp;
		
		sensors = new Hashtable<Integer, ArrayList<BufferedImage>>();		
		curSensorVals = new int[5];

		for(int i = 0; i < NUM_SENSORS; i++) {
			temp = new ArrayList<BufferedImage>();
			temp.add(ctx.theme.pingRed);
			temp.add(ctx.theme.pingYellow);
			temp.add(ctx.theme.pingGreen);
			sensors.put(i, temp);
		}
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
		
		//
		
	}
	
	/**
	 * Gets the current value for a sensor.
	 * @param index - The index of the sensor
	 * @return - the current value stored for that sensor
	 */
	public int getSensorValue(int index) {
		return curSensorVals[index];
	}
	
	public int getNumSensors() {
		return NUM_SENSORS;
	}
	
}
