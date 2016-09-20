package com.map;

public class Waypoint{
    // Earth's radius in miles
    private static final double EARTH_RAD = 3958.761;
    // Earth's circumference in miles
    private static final double EARTH_CIRC = EARTH_RAD*Math.PI*2.0;

    private static double toRad(double degrees){
        return degrees*Math.PI/180.0;
    }

    private static double toDeg(double radians){
        return radians*180.0/Math.PI;
    }

    double latitude, longitude; // stored in decimal degrees
    /**
     * Construct a new waypoint; arguments are in decimal degrees
     */
    Waypoint(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    /** Return this Waypoint's latitude in decimal degrees */
    double getLatitude(){
        return latitude;
    }
    /** Return this Waypoint's longitude in decimal degrees */
    double getLongitude(){
        return longitude;
    }
    /**
     * Return the heading in degrees, north = 0, CW positive around UP from
     * this waypoint to target
     */
    double headingTo(Waypoint target){
        double aRlat = toRad(getLatitude());
        double aRlng = toRad(getLongitude());
        double bRlat = toRad(target.getLatitude());
        double bRlng = toRad(target.getLongitude());
        double y = Math.sin(bRlng - aRlng) * Math.cos(bRlat);
        double x = Math.cos(aRlat)*Math.sin(bRlat)
                    - Math.sin(aRlat)*Math.cos(bRlat)*Math.cos(bRlng - aRlng);
        return toDeg( Math.atan2(y,x) );
    }
    /**
     * Return the distance in miles between this waypoint and the target
     */
    double distanceTo(Waypoint target){
        double aRlat  = toRad(getLatitude());
        double aRlng  = toRad(getLongitude());
        double bRlat  = toRad(target.getLatitude());
        double bRlng  = toRad(target.getLongitude());
        double sinlat = Math.sin((aRlat - bRlat)/2.);
        double sinlng = Math.sin((aRlng - bRlng)/2.);
        double chord  = sinlat*sinlat + sinlng*sinlng*Math.cos(aRlat)*Math.cos(bRlat);
        return 2.0 * EARTH_RAD * Math.atan2( Math.sqrt(chord), Math.sqrt(1.-chord) );
    }
    /**
     * Return a waypoint that is `distance` miles away in the `heading`
     * direction from this waypoint's location.
     * heading is in degrees, north = 0, CW positive around UP
     */
    Waypoint extrapolate(double heading, double distance){
        double rlat, rlng;
        double pRlat = toRad(getLatitude());
        double pRlng = toRad(getLongitude());
        rlat = Math.asin(Math.sin(pRlat)*Math.cos(distance/EARTH_RAD) +
                 Math.cos(pRlat)*Math.sin(distance/EARTH_RAD) * Math.cos(toRad(heading)));
        rlng = pRlng+Math.atan2(
                  (Math.sin(toRad(heading))*Math.sin(distance/EARTH_RAD)*Math.cos(pRlat)),
                  (Math.cos(distance/EARTH_RAD)-Math.sin(pRlat)*Math.sin(rlat))
               );
        return new Waypoint(toDeg(rlat), toDeg(rlng));
    }
}
