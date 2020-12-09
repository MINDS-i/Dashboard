package com.ui;

import java.util.*;
import java.awt.*;
import java.swing.*;

import com.Context;

/** 
 * @author Chris Park @ Infinetix Corp.
 * Date: 12-2-20
 * Description: Dashboard Widget child class used to display a units
 * ping sensor data.
 */
public class PingWidget extends UIWidget {
	
	//Constants
	protected static final int NUM_SENSORS = 3;
	protected static final int MIN_SENSOR_LEVEL = 0;
	protected static final int MAX_SENSOR_LEVEL = 3;
	
	//Ping Sensor Indicators
	protected Collection<BufferedImage> sensorA;
	protected Collection<BufferedImage> sensorB;
	protected Collection<BufferedImage> sensorC;
	protected Collection<BufferedImage> sensorD;
	
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 */
	public PingWidget(Context ctx) {
		super(ctx);
		
		sensorA.add(new BufferedImage(ctx.theme.pingGreen));
		sensorA.add(new BufferedImage(ctx.theme.pingYellow));
		sensorA.add(new BufferedImage(ctx.theme.pingRed));
		
		sensorB.add(new BufferedImage(ctx.theme.pingGreen));
		sensorB.add(new BufferedImage(ctx.theme.pingYellow));
		sensorB.add(new BufferedImage(ctx.theme.pingRed));
		
		sensorC.add(new BufferedImage(ctx.theme.pingGreen));
		sensorC.add(new BufferedImage(ctx.theme.pingYellow));
		sensorC.add(new BufferedImage(ctx.theme.pingRed));
		
		sensorD.add(new BufferedImage(ctx.theme.pingGreen));
		sensorD.add(new BufferedImage(ctx.theme.pingYellow));
		sensorD.add(new BufferedImage(ctx.theme.pingRed));
		
		
	}
	
	public void update(int sensorIndex, int level) {
		
		//If not a valid sensor index, return
		if(sensorIndex > (NUM_SENSORS - 1) || sensorIndex < 0) {
			return;
		}
		
		//If the level provided exceeds the allowed range then do nothing.
		if(level > MAX_SENSOR_LEVEL || level < MIN_SENSOR_LEVEL) {
			return;
		}
		
		

		//cases (level)
			// case 0
				//show image at index 0, hide index 1, 2
			// case 1
				//Show image at index 0, 1, hide 2
			// case 2
				//show all
		
	}
	
}
