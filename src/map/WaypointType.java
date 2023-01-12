package com.map;

import java.awt.Color;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-11-2023
 * Description: Pre-defined enums used to communicate details about a
 * waypoints behavior and appearance.
 */
public enum WaypointType {
	
	STANDARD 		(0, "Standard", Color.red),
	LEFT_APPROACH	(1, "Left Approach", Color.blue),
	RIGHT_APPROACH	(2, "Right Approach", Color.orange),
	SWATH_START		(3, "Swath Start", Color.yellow),
	SWATH_END		(4, "Swath End", Color.yellow);
	
	private final int index;
	private final String text;
	private final Color pointColor;
	
	WaypointType(int index, String text, Color pointColor) {
		this.index = index;
		this.text = text;
		this.pointColor = pointColor;
	}    	
};
