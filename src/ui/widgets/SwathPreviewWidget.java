package com.ui.widgets;

import com.Context;

import java.util.*;
import java.lang.Math;

import javax.imageio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;


/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-6-2023
 * Description: Widget class 
 *
 */
public class SwathPreviewWidget extends UIWidget {

	//Constants (If Any)
		//Preview Window Dimensions?	
	
	public enum SwathType {
		NONE		("None"),
		HORIZONTAL	("Horizontal"),
		VERITCAL	("Vertical");
		
		private final String name;
		
		SwathType(String name) {
			this.name = name;
		}
		
		public String getImageName() {
			return this.name;
		}
	};
	
	public enum SwathRotation {
		NONE				(0.0),
		CLOCKWISE 			(90.0),
		COUNTER_CLOCKWISE 	(270.0);
		
		private final double degrees;
		
		SwathRotation(double degrees) {
			this.degrees = degrees;
		}
		
		public double getDegrees() {
			return this.degrees;
		}
		
		public double getRadians() {
			return Math.toRadians(this.degrees);
		}
	};
	
	//Standard Variables
	protected ArrayList<BufferedImage> swathImages;
	protected BufferedImage activePatternImg;
	protected SwathType currSwathType;
	protected SwathType prevSwathType;
	protected SwathRotation currSwathRotation;
	protected SwathRotation prevSwathRotation;
	protected boolean isVisible;
	
	//Stored Swath Pattern Images
	protected BufferedImage imgSwathNone;
	protected BufferedImage imgSwathHorizontal;
	protected BufferedImage imgSwathVertical;

	//Create a square image surface
	//Have all preset swath types as available image resources (in theme.java?)
	//depending on the selection apply/draw the appropriate image.
	
	/**
	 * Class Constructor
	 * @param ctx - the application context
	 */
	public SwathPreviewWidget(Context ctx) {
		super(ctx, "Swath Preview");
		
		//Do any init here.
		isVisible = false;
		
		//Set all image variables
		imgSwathNone = 			ctx.theme.swathNone;
		imgSwathHorizontal =	ctx.theme.swathHorizontal; 
		imgSwathVertical =		null; //TODO - CP - ADD THIS IMAGE
		
		//Set default image and orientation
		activePatternImg = imgSwathNone;
		currSwathRotation = SwathRotation.NONE;
		prevSwathRotation = SwathRotation.NONE;
		currSwathType = SwathType.NONE;
		prevSwathType = SwathType.NONE;
		
		//Set size constraints
		this.setPreferredSize(
				new Dimension(imgSwathNone.getWidth(), imgSwathNone.getHeight()));
		this.setMaximumSize(
				new Dimension(imgSwathNone.getWidth(), imgSwathNone.getHeight()));
		setOpaque(false);
	}

	/**
	 * Updates the preview image to match the currently selected swath
	 * pattern. Assumes a standard orientation without rotation.
	 * @param type - The swath type to be drawn
	 */
	protected void updatePreview(SwathType type) {
		updatePreview(type, SwathRotation.NONE);
	}
	
	/**
	 * Updates the preview image and its rotation to match the currently
	 * selected swath pattern.
	 * @param type - The swath type to be drawn
	 * @param rotation - The orientation of the drawn swath type.
	 */
	protected void updatePreview(SwathType type, SwathRotation rotation) {
		
		switch(type) {
			case HORIZONTAL:
				activePatternImg = imgSwathHorizontal;
				break;
			case VERITCAL:
				activePatternImg = imgSwathVertical;
				break;
			case NONE:
				activePatternImg = imgSwathNone;
				break;
		};
		
		prevSwathType = currSwathType;
		prevSwathRotation = currSwathRotation;
		
		currSwathType = type;
		currSwathRotation = rotation;
		
		repaint();
	}
	
	//TODO - CP - paint swath preview here.
	/**
	 * Paints the currently selected swath preview at the angle selected
	 * by the user.
	 * @param g - The graphics reference
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//If there has been no change in preview...
		if((prevSwathRotation == currSwathRotation)
		&& (prevSwathType == currSwathType)) {
			return;
		}
		
		Graphics2D g2d = (Graphics2D) g.create();
		int xOffset = (activePatternImg.getWidth() / 2);
		int yOffset = (activePatternImg.getHeight() / 2);

		g2d.translate(xOffset, yOffset);
		g2d.rotate(currSwathRotation.getRadians());
		g2d.drawImage(activePatternImg, 0, 0, null);
		g2d.dispose();
	}
	
	/**
	 * Shows the currently selected preview
	 */
	public void showPreview() {
		if(this.isVisible) {
			return;
		}
		
		//TODO - CP - Show the preview here
		this.isVisible = true;
	}
	
	/**
	 * Hides the currently selected preview.
	 */
	public void hidePreview() {
		if(!this.isVisible) {
			return;
		}
		
		//TODO - CP - Hide the preview here
		this.isVisible = false;
	}

	/**
	 * Returns whether or not the preivew can currently be seen by the user.
	 * @return - Whether or not he preview is visible
	 */
	public boolean isPreviewVisible() {
		return isVisible;
	}
}