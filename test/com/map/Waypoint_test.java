package com.map;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Waypoint_test {
    static final double EPSILON = 0.0001d;
    static class RefPath{
        Waypoint start, end;
        double heading, distance;
        RefPath(Waypoint s, Waypoint e, double h, double d){
            start = s;
            end = e;
            heading = h;
            distance = d;
        }
    }
    static final Waypoint origin    = new Waypoint( 0, 0);
    static final Waypoint north     = new Waypoint( 1, 0);
    static final Waypoint northEast = new Waypoint( 1, 1);
    static final Waypoint east      = new Waypoint( 0, 1);
    static final Waypoint southEast = new Waypoint(-1, 1);
    static final Waypoint south     = new Waypoint(-1, 0);
    static final Waypoint southWest = new Waypoint(-1,-1);
    static final Waypoint west      = new Waypoint( 0,-1);
    static final Waypoint northWest = new Waypoint( 1,-1);
    static final RefPath[] paths = {
        new RefPath(origin,     north,      0.0, 69.0934),
        new RefPath(origin, northEast,  44.9956, 97.7103),
        new RefPath(origin,      east,     90.0, 69.0934),
        new RefPath(origin, southEast, 135.0043, 97.7103),
        new RefPath(origin,     south,    180.0, 69.0934),
        new RefPath(origin, southWest,-135.0043, 97.7103),
        new RefPath(origin,      west,    -90.0, 69.0934),
        new RefPath(origin, northWest, -44.9956, 97.7103),
    };

    @Test public void distanceTo(){
        for(RefPath r : paths){
            assertEquals(r.distance, r.start.distanceTo(r.end), EPSILON);
        }
    }

    @Test public void headingTo(){
        for(RefPath r : paths){
            assertEquals(r.heading, r.start.headingTo(r.end), EPSILON);
        }
    }

    @Test public void extrapolate(){
        for(RefPath r : paths){
            Waypoint result = r.start.extrapolate(r.heading, r.distance);
            assertEquals(r.end.getLatitude(), result.getLatitude(), EPSILON);
            assertEquals(r.end.getLongitude(), result.getLongitude(), EPSILON);
        }
    }
}
