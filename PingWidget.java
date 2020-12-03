package com.ui;

import java.util.*;
import java.awt.*;
import java.swing.*;

import com.Context;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 12-2-20
 * Description: Dashboard Widget child class used to display a units
 * ping sensor data.
 *
 */
public class PingWidget extends UIWidget {
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
		
	}
	
	
}
