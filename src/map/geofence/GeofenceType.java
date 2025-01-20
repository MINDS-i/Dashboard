package com.map.geofence;

import com.map.CoordinateTransform;
import com.map.Dot;

import java.awt.*;

/**
 * @author Chris Park @ Infinetix Corp
 * Date 11/17/2021
 * Description: Abstract base class defining the basic shared charicteristics
 * and functions of a geofence type.
 */
public abstract class GeofenceType {

    Dot origin;
    Dot radiusPoint;
    double radius_ft;

    /**
     * Constructor
     *
     * @param radius_ft - radius from the origin to the geofence wall
     */
    public GeofenceType(double radius_ft) {
        //This constructor is only called for initial setup,
        //so the origin and radius points are defaulted to 0,0.
        this.origin = new Dot();
        this.radiusPoint = new Dot();
        this.radius_ft = radius_ft;
    }

    //Abstract functions to be overriden in concrete classes.
    public abstract void paint(Graphics graphics, CoordinateTransform transform);

    public abstract boolean doesIntersect(Dot coordinate);

    public abstract Dot getOriginLatLng();

    public abstract void setOriginLatLng(double lat, double lng);

    public abstract void updateRadiusFeet(double new_radius_ft);

    protected abstract void setRadiusLng();

    protected abstract double applyDrawOffset(double fenceDiameter);

}
