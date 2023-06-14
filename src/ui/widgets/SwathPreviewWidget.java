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
 * Description: Widget used to track the currently selected swath pattern
 * orientation and display it in the FarmingPanel.
 */
public class SwathPreviewWidget extends JPanel {

	//Orientation Enums
	public enum SwathType {
		HORIZONTAL	("Horizontal"),
		VERTICAL	("Vertical");
		
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
		ROTATED 			(90.0);
		
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
	
	public enum SwathInversion {
		NONE		(0),
		FLIPPED		(1);
		
		private final int value;
		
		SwathInversion(int value) {
			this.value = value;
		}
		
		public int getInversionType() {
			return this.value;
		}
	};
	
	//Standard Variables
	protected Context context;
	protected ArrayList<BufferedImage> swathImages;
	protected BufferedImage activePatternImg;
	protected SwathType currSwathType;
	protected SwathType prevSwathType;
	protected SwathRotation currSwathRotation;
	protected SwathRotation prevSwathRotation;
	protected SwathInversion currSwathInversion;
	protected SwathInversion prevSwathInversion;
	protected boolean isVisible;
	
	//Stored Swath Pattern Images
	protected BufferedImage imgSwathNone;
	protected BufferedImage imgSwathHorizontal;
	protected BufferedImage imgSwathVertical;
	
	/**
	 * Class Constructor
	 * @param ctx - the application context
	 */
	public SwathPreviewWidget(Context ctx) {
		context = ctx;
		
		//Do any init here.
		isVisible = false;
		
		//Set all image variables
		imgSwathNone = 			ctx.theme.swathNone;
		imgSwathHorizontal =	ctx.theme.swathHorizontal; 
		imgSwathVertical =		ctx.theme.swathVertical;
		
		//Set default image and orientation
		activePatternImg = imgSwathHorizontal;
		currSwathType = SwathType.HORIZONTAL;
		prevSwathType = SwathType.HORIZONTAL;
		currSwathRotation = SwathRotation.NONE;
		prevSwathRotation = SwathRotation.NONE;
		currSwathInversion = SwathInversion.NONE;
		prevSwathInversion = SwathInversion.NONE;
		
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
	public void updatePreview(SwathType type) {
		updatePreview(type, SwathRotation.NONE, SwathInversion.NONE);
	}
	
	/**
	 * Updates the preview image and its rotation to match the currently
	 * selected swath pattern.
	 * @param type - The swath type to be drawn
	 * @param rotation - The orientation of the drawn swath type.
	 */
	public void updatePreview(SwathType type, SwathRotation rotation,
			SwathInversion inversion) {
		prevSwathType = currSwathType;
		prevSwathRotation = currSwathRotation;
		prevSwathInversion = currSwathInversion;
		
		switch(type) {
			case HORIZONTAL:
				activePatternImg = imgSwathHorizontal;
				break;
			case VERTICAL:
				activePatternImg = imgSwathVertical;
				break;
		};
		
		currSwathType = type;
		currSwathRotation = rotation;
		currSwathInversion = inversion;
		
		repaint();
	}
	
	/**
	 * Paints the currently selected swath preview at the orientation
	 * selected by the user.
	 * @param g - The graphics object reference
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g.create();
		int xOffset = (activePatternImg.getWidth() / 2);
		int yOffset = (activePatternImg.getHeight() / 2);
		
		g2d.translate(xOffset, yOffset);
		g2d.rotate(currSwathRotation.getRadians());
		g2d.translate(-xOffset, -yOffset);
		
		//If Inverted, Flip Along The Horizontal
		if(currSwathInversion == SwathInversion.FLIPPED) {
			g2d.drawImage(
					activePatternImg,				//Image 
					activePatternImg.getWidth(), 	//Pos X
					0,								//Pos Y
					-activePatternImg.getWidth(), 	//Invert the Width
					activePatternImg.getHeight(),	//Standard Height
					null);							//Observer (Unused)
		} //Else Draw As Standard Rotation
		else {
			g2d.drawImage(activePatternImg, 0, 0, null);	
		}
		
		g2d.dispose();
	}
	
	/**
	 * Shows the currently selected preview
	 */
	public void showPreview() {
		if(this.isVisible) {
			return;
		}
		
		this.isVisible = true;
	}
	
	/**
	 * Hides the currently selected preview.
	 */
	public void hidePreview() {
		if(!this.isVisible) {
			return;
		}
		
		this.isVisible = false;
	}

	/**
	 * Returns whether or not the preivew can currently be seen by the user.
	 * @return - Whether or not he preview is visible
	 */
	public boolean isPreviewVisible() {
		return isVisible;
	}
	
	/**
	 * Returns the type of swath currently selected
	 * @return - SwathType
	 */
	public SwathType getSelectedType() {
		return this.currSwathType;
	}
	
	/**
	 * Returns the type of rotation for the currentlys elected swath
	 * @return - SwathRotation
	 */
	public SwathRotation getSelectedRotation() {
		return this.currSwathRotation;
	}
	
	/**
	 * Returns whether or not the swath pattern is in inversion.
	 * @return - SwathInversion
	 */
	public SwathInversion getSelectedInversion() {
		return this.currSwathInversion;
	}
}