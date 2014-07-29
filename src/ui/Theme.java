package com.ui;

import com.Dashboard;
import java.awt.image.*;
import java.io.*;
import java.awt.*;
import javax.imageio.*;

public class Theme{
	public BufferedImage gaugeBackground;
	public BufferedImage gaugeRed;
	public BufferedImage gaugeSquare;
	public BufferedImage gaugeGlare;
	public BufferedImage roverSide;
	public BufferedImage roverTop;
	public BufferedImage roverFront;
	public BufferedImage droneSide;
	public BufferedImage droneTop;
	public BufferedImage droneFront;
	public BufferedImage refreshImage;
	public BufferedImage waypointSelected;
	public BufferedImage waypointImage;
	public BufferedImage lineSegment;
	public BufferedImage roverImage;
	public Font digital;
  	public Font ocr;
	public Theme(String folderURL){
        try{
			roverImage       = ImageIO.read(new File(folderURL+"auv-icon.png"));
			waypointImage    = ImageIO.read(new File(folderURL+"waypoint.png"));
			waypointSelected = ImageIO.read(new File(folderURL+"waypoint-green.png"));
			lineSegment      = ImageIO.read(new File(folderURL+"line.png"));
			refreshImage 	 = ImageIO.read(new File(folderURL+"refresh.png"));
			gaugeBackground  = ImageIO.read(new File(folderURL+"Gauge.png"));
			roverSide 		 = ImageIO.read(new File(folderURL+"6x6-Side.png"));
			roverTop 		 = ImageIO.read(new File(folderURL+"6x6-Top.png"));
			roverFront 		 = ImageIO.read(new File(folderURL+"6x6-Front.png"));
/*			droneSide 		 = ImageIO.read(new File(folderURL+"6x6-Side.png"));
			droneTop 		 = ImageIO.read(new File(folderURL+"6x6-Top.png"));
			droneFront 		 = ImageIO.read(new File(folderURL+"6x6-Front.png"));*/
			gaugeRed 		 = ImageIO.read(new File(folderURL+"direction-arrow.png"));
			gaugeSquare 	 = ImageIO.read(new File(folderURL+"screenWithGlare.png"));
			gaugeGlare 		 = ImageIO.read(new File(folderURL+"gaugeGlare.png"));
			ocr 	= Font.createFont(Font.TRUETYPE_FONT, new File(folderURL+"ocr.ttf"));
			digital = Font.createFont(Font.TRUETYPE_FONT, new File(folderURL+"digital.ttf"));
			ocr = ocr.deriveFont(13f);
      		digital = digital.deriveFont(36f);
        } catch(IOException|FontFormatException e){
            Dashboard.DisplayError(e);
        }
	}
}
