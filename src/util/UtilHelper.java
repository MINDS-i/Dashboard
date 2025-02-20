package com.util;

import com.map.Dot;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 4-21-21
 * Description: Singletone class containing useful utility functions for
 * Minds-i Dashboard specific functionality and calculations.
 */
public class UtilHelper {
    private static final double EARTH_RADIUS_KM = 6372.80;
    private static final double FEET_PER_KM = 3280.84;
    private static final double KM_PER_FEET = 0.0003048;
    private static final double KM_PER_LNG = 111;
    private static UtilHelper utilHelperInstance = null;

    /**
     * Constructor (Private, accessed by getInstance
     */
    private UtilHelper() {
    }

    /**
     * Returns the singleton instance of this class to be used system wide.
     *
     * @return The UtilHelper instance.
     */
    public static UtilHelper getInstance() {
        if (utilHelperInstance == null) {
            utilHelperInstance = new UtilHelper();
        }

        return utilHelperInstance;
    }

    /**
     * Computes the avarage of the provided array values.
     *
     * @param array  - The array to average
     * @param length - The length of the array
     * @return - the average of the array values.
     */
    public double average(double[] array, int length) {
        double sumOfData = 0.0;

        for (int i = 0; i < length; i++) {
            sumOfData += array[i];
        }

        return (sumOfData / length);
    }

    /**
     * Computes the distance in km between two points on the surface of a sphere.
     *
     * @param pointA - First Waypoint (Dot)
     * @param pointB - Second Waypoint (Dot)
     * @return - Distance between coordinates in km
     */
    public double haversine(Dot pointA, Dot pointB) {
        return haversine(pointA.getLatitude(), pointA.getLongitude(),
                pointB.getLatitude(), pointB.getLongitude());
    }

    /**
     * Computes the distance in km between two points on the surface of a sphere.
     *
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
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Converts kilometers to feet.
     *
     * @param km - distance in km to convert
     * @return - distance in feet.
     */
    public double kmToFeet(double km) {
        return (km * FEET_PER_KM);
    }

    /**
     * Converts feet to kilometers
     *
     * @param feet - distance in feet to convert
     * @return - distance in kilometers
     */
    public double feetToKm(double feet) {
        return (feet * KM_PER_FEET);
    }

    /**
     * Converts kilometers to degrees longitude
     *
     * @param km - distance in kilometers to convert
     * @return - distance in degrees latitude
     */
    public double kmToDegLng(double km) {
        return (km / KM_PER_LNG);
    }
}
