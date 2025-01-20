package com.map;

import com.map.WaypointType;

import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.*;


//NOTES TO SELF (CPark):
//- Are we actually utilizing all the overriden constructors?
//	Can some be removed?
//- Does the altitude attribute actually get used by the Rover/UAV code?

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 1/6/2023
 * Description: Base class used to define a waypoint location and it's common 
 * operability functions. Based on an original class created by 
 * Brett Folkins (formerly Brett Menzies) prior to Infinetix's involvement. 
 * (approx 2020)
 */
public class Dot {
	
	//Standard Vars
	protected Point2D location;
    protected short altitude;
    protected WaypointType waypointType;
    
    /*----------------------------Constructors--------------------------------*/
    /**
     * Default Class Constructor
     */
    public Dot() {
        this.location = new Point.Double(0,0);
        this.altitude = 0;
    }
    
    /**
     * Class Constructor
     * @param location - The Lat/Lng location of the waypoint dot.
     * @param altitude - The altitude of the waypoint dot.
     */
    public Dot(Point2D location, short altitude) {
        this.location = location;
        this.altitude = altitude;
    }
    
    /**
     * Class Constructor
     * @param location - The Lat/Lng location of the waypoint dot.
     */
    public Dot(Point2D location) {
        this.location = location;
        this.altitude = 0;
    }
    
    /**
     * Class Constructor
     * @param latitude - The latitude of the waypoint dot.
     * @param longitude - The longitude of the waypoint dot.
     * @param altitude - The altitude of the waypoint dot.
     */
    public Dot(double latitude, double longitude, short altitude) {
        this.location = new Point.Double(longitude, latitude);
        this.altitude = altitude;
    }
    
    /**
     * Class Copy Constructor
     * @param dot - The waypoint dot to copy.
     */
    public Dot(Dot dot) {
        this.location = dot.getLocation();
        this.altitude = dot.getAltitude();
        this.waypointType = dot.getWaypointType();
    }

    
    /*-----------------------Getters and Setters------------------------------*/
    /**
     * Sets the Point2D location of this waypoint dot.
     * @param location - The point location to set.
     */
    public void setLocation(Point2D location) {
        this.location = location;
    }
    
    /**
     * Sets the Point2D location and altitud of this waypoint dot.
     * @param location - The point location to set
     * @param altitude - The altitude to set.
     */
    public void setLocation(Point2D location, short altitude) {
        this.location = location;
        this.altitude = altitude;
    }
    
    /**
     * Sets the latitude of this waypoint dot.
     * @param latitude - the latitude to set this waypoint do to.
     */
    public void setLatitude(double latitude) {
        this.location = new Point.Double(location.getX(), latitude);
    }
    
    /**
     * Sets the longitude of this waypoint dot.
     * @param longitude - The longitude to set this waypoint dot to.
     */
    public void setLongitude(double longitude) {
        this.location = new Point.Double(longitude, location.getY());
    }
    
    /**
     * Sets the altitude of this waypoint dot.
     * @param altitude - the altitude to set this waypoint do to.
     */
    public void setAltitude(short altitude) {
        this.altitude = altitude;
    }
    
    /**
     * Returns the Point2D location (Longitude, Latitude) of this waypoint dot.
     * @return - Point2D
     */
    public Point2D getLocation() {
        return this.location;
    }
    
    /**
     * Returns the longitude of this waypoint type.
     * @return - double
     */
    public double getLongitude() {
        return this.location.getX();
    }
    
    /**
     * Returns the latitude of this waypoint dot.
     * @return - double
     */
    public double getLatitude() {
        return this.location.getY();
    }
    
    /**
     * Returns the altitude of this waypoint dot.
     * @return - short
     */
    public short getAltitude() {
        return this.altitude;
    }
    
    /**
     * Returns the type of this waypoint dot.
     * @return - WaypointType
     */
    public WaypointType getWaypointType() {
    	return this.waypointType;
    }
    
    /*--------------------------Type Functions--------------------------------*/
    
    //TODO - CP - Type behavior defined here.
    public void executeTypeProcesses() {
    	//To be overriden in child classes for specific functionality
    	//as required.
    }
}
