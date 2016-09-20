package com.map;

import static java.lang.Math.*;

public class Waypoint{
    /** Earth's radius in miles */
    private static final double EARTH_RAD = 3958.761;
    /** Earth's circumference in miles */
    private static final double EARTH_CIRC = EARTH_RAD*Math.PI*2.0;
    /** Convert degrees to radians */
    private static double toRad(double degrees){
        return degrees*Math.PI/180.0;
    }
    /** Convert radians to degrees */
    private static double toDeg(double radians){
        return radians*180.0/Math.PI;
    }
    /**
     * The waypoint's latitude and longitude in decimal degrees
     * northern and eastern hemisphere are positive lat, long respectively
     */
    private final double latitude, longitude;
    /**
     * Construct a new waypoint; arguments are in decimal degrees
     */
    public Waypoint(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    /** Return this Waypoint's latitude in decimal degrees */
    public double getLatitude(){
        return latitude;
    }
    /** Return this Waypoint's longitude in decimal degrees */
    public double getLongitude(){
        return longitude;
    }
    /**
     * Return the heading in degrees, north = 0, CW positive around UP from
     * this waypoint to target
     */
    public double headingTo(Waypoint target){
        double aRlat = toRad(getLatitude());
        double aRlng = toRad(getLongitude());
        double bRlat = toRad(target.getLatitude());
        double bRlng = toRad(target.getLongitude());
        double y = sin(bRlng - aRlng) * cos(bRlat);
        double x = cos(aRlat)*sin(bRlat)
                   - sin(aRlat)*cos(bRlat)*cos(bRlng - aRlng);
        return toDeg( atan2(y,x) );
    }
    /**
     * Return the distance in miles between this waypoint and the target
     */
    public double distanceTo(Waypoint target){
        double aRlat  = toRad(getLatitude());
        double aRlng  = toRad(getLongitude());
        double bRlat  = toRad(target.getLatitude());
        double bRlng  = toRad(target.getLongitude());
        double sinlat = sin((aRlat - bRlat)/2.);
        double sinlng = sin((aRlng - bRlng)/2.);
        double chord  = sinlat*sinlat + sinlng*sinlng*cos(aRlat)*cos(bRlat);
        return 2.0 * EARTH_RAD * atan2( sqrt(chord), sqrt(1.-chord) );
    }
    /**
     * Return a waypoint that is `distance` miles away in the `heading`
     * direction from this waypoint's location.
     * heading is in degrees, north = 0, CW positive around UP
     */
    public Waypoint extrapolate(double heading, double distance){
        double rlat, rlng;
        double pRlat = toRad(getLatitude());
        double pRlng = toRad(getLongitude());
        rlat = asin(
                   sin(pRlat)*cos(distance/EARTH_RAD) +
                   cos(pRlat)*sin(distance/EARTH_RAD) * cos(toRad(heading))
               );
        rlng = pRlng+atan2(
                   (sin(toRad(heading))*sin(distance/EARTH_RAD)*cos(pRlat)),
                   (cos(distance/EARTH_RAD)-sin(pRlat)*sin(rlat))
               );
        return new Waypoint(toDeg(rlat), toDeg(rlng));
    }
}
