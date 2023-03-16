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
	
	//TODO - CP - Determine if these enums should be moved to own class
	//Swath Property Enums
	public enum SwathType {
		//TODO - CP - Add Image Paths here (from Theme class?)
		NONE		("A"),
		HORIZONTAL	("B"),
		VERITCAL	("C");
		
		private final String path;
		
		SwathType(String path) {
			this.path = path;
		}
		
		public String getImagePath() {
			return this.path;
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
	protected BufferedImage backgroundImg;
	protected SwathType currSwathType;
	protected SwathRotation currSwathRotation;
	protected boolean isVisible;
	


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
		//set background image
		//Set preferred size
		//Set rotation angle to 0/none
		//set opaque to false
		
		
	}

	//(F) Init()
	//-Create blank image surface (or show blank image?)
	//-Set Minimum Size
	//-Set Default Images
	//

	//(F) UpdatePreview(enum swathType, enum rotationType)
	//Updates the preview image and its rotation to match the currently
	//selected swath pattern.
	//-Should probably always be updated on a users swath selection on the map
	//-Make sure to repaint the component if there is a change	
	protected void updatePreview(SwathType type) {
		updatePreview(type, SwathRotation.NONE);
	}
	
	protected void updatePreview(SwathType type, SwathRotation rotation) {
		//TODO - CP - calibrate image and repaint here.
		
		switch(type) {
			case HORIZONTAL:
				switch(rotation) {
					case NONE:
						//Load horizontal standard
						break;
					case CLOCKWISE:
						break;
					case COUNTER_CLOCKWISE:
						break;
				};
				break;
				
			case VERITCAL:
				switch(rotation) {
					case NONE:
						//Load vertical standard
						break;
					case CLOCKWISE:
						break;
					case COUNTER_CLOCKWISE:
						break;
				};
				break;
				
			case NONE:
				//Load blank image
				break;
		};
		
		//Repaint here?
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
		
		Graphics g2d = (Graphics2D) g.create();
		
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