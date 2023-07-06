package com.map;

import java.awt.Color;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Pre-defined enums used to communicate details about a
 * waypoints behavior and appearance.
 */
public enum WaypointType {
	
	STANDARD 		(0, "Standard",		Color.red),
	HOME			(1, "Home", 		Color.green),
	SWATH_START		(2, "Swath Start", 	Color.yellow),
	SWATH_END		(3, "Swath End", 	Color.yellow),
	SWATH			(4, "Swath Point",	Color.blue);
	
	private final int index;
	private final String text;
	private final Color pointColor;
	
	WaypointType(int index, String text, Color pointColor) {
		this.index = index;
		this.text = text;
		this.pointColor = pointColor;
	}    	
};
