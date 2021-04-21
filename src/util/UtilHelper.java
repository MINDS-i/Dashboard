package com.util;

import java.util.*;


/**
 * @author Chris Park @ Infinetix Corp
 * Date: 4-21-21
 * Description: Singletone class containing useful utility functions for 
 * Minds-i Dashboard specific functionality and calculations.
 */
public class UtilHelper {
	private static UtilHelper utilHelperInstance = null;
	
	private static final double EARTH_RADIUS = 6371.00;
	private static final double FEET_PER_KM = 3280.84;
	
	/**
	 * Constructor (Private, accessed by getInstance
	 */
	private UtilHelper() {
		
	}
	
	/**
	 * Returns the singleton instance of this class to be used system wide.
	 * @return The UtilHelper instance.
	 */
	public static UtilHelper getInstance() {
		if(utilHelperInstance == null) {
			utilHelperInstance = new UtilHelper();
		}
		
		return utilHelperInstance;
	}
	
	/**
	 * Computes the distance in km between to points on the surface of a sphere.
	 * @param lat1 - First latitude coordinate
	 * @param lon1 - First longitude coordinate
	 * @param lat2 - Second latitude coordinate
	 * @param lon2 - Second longitude coordinate
	 * @return - Distance between coordinates in km
	 */
	public double haversine(double lat1, double lon1,
							double lat2, double lon2) {
		//Calculate distance between Lats and Longs
		double distLat = Math.toRadians(lat2 - lat1);
		double distLon = Math.toRadians(lon2 - lon1);
		
		//Convert to Radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		//Use Formula
		double a = (Math.pow(Math.sin(distLat / 2), 2) + 
				   (Math.pow(Math.sin(distLon / 2), 2) * 
				    Math.cos(lat1) * 
				    Math.cos(lat2)));
		double c = (2 * Math.asin(Math.sqrt(a)));
		return EARTH_RADIUS * c;
	}
	
	/**
	 * Converts kilometer to feet.
	 * @param km - distance in km to convert
	 * @return - distance in feet.
	 */
	public double kmToFeet(double km) {
		return (km * FEET_PER_KM);
	}
}
