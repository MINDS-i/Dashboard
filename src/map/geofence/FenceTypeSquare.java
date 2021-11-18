package com.map.geofence;

import com.map.Dot;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 11/17/2021
 * Description: Concrete FenceType class defining the dimensions and
 * drawing behavior for a square fence.
 */
public class FenceTypeSquare extends FenceType {

	/**
	 * Constructor 
	 * @param origin - Central origin point (waypoint) of the fence
	 * @param radius_ft - 
	 */
	public FenceTypeSquare(Dot origin, int radius_ft) {
		super(origin, radius_ft);
	}
	
	/**
	 * Draws the outline of the fence centered on the
	 * origin waypoint's position.
	 * @param graphics - The graphics context used for drawing.
	 */
	@Override
	public void paint(Graphics graphics) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.drawRect(
				(int)origin.getLatitude(), (int)origin.getLongitude(),
				radius_ft, radius_ft);
	}
	
	//TODO - CP - Define this override for collisions
	@Override
	public boolean doesIntersect(Dot coordinate) {
		return true;
	}
}