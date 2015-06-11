package com.ui;

import com.Dashboard;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import javax.imageio.*;

//POD class for theme elements
public class Theme{
	public BufferedImage gaugeBackground;
	public BufferedImage gaugeRed;
	public BufferedImage gaugeSquare;
	public BufferedImage gaugeGlare;
	public BufferedImage roverSide;
	public BufferedImage roverTop;
	public BufferedImage roverFront;
	public BufferedImage refreshImage;
	public BufferedImage waypointSelected;
	public BufferedImage waypointImage;
	public BufferedImage lineSegment;
	public BufferedImage roverImage;
	public Font number;
  	public Font text;
	public Theme(Locale locale){
		ResourceBundle res = ResourceBundle.getBundle("resources", locale);
        try{
        	String url = res.getString("resource_folder");
			roverImage       = ImageIO.read(new File(url+res.getString("map_icon")));
			waypointImage    = ImageIO.read(new File(url+res.getString("waypoint_icon")));
			waypointSelected = ImageIO.read(new File(url+res.getString("waypoint_selected")));
			lineSegment      = ImageIO.read(new File(url+res.getString("line_segment")));
			refreshImage 	 = ImageIO.read(new File(url+res.getString("refresh_icon")));
			gaugeBackground  = ImageIO.read(new File(url+res.getString("gauge_background")));
			roverSide 		 = ImageIO.read(new File(url+res.getString("drone_side")));
			roverTop 		 = ImageIO.read(new File(url+res.getString("drone_top")));
			roverFront 		 = ImageIO.read(new File(url+res.getString("drone_front")));
			gaugeRed 		 = ImageIO.read(new File(url+res.getString("gauge_arrow")));
			gaugeSquare 	 = ImageIO.read(new File(url+res.getString("gauge_screen")));
			gaugeGlare 		 = ImageIO.read(new File(url+res.getString("gauge_glare")));
			text 	= Font.createFont(Font.TRUETYPE_FONT, new File(url+res.getString("text_font")));
			number = Font.createFont(Font.TRUETYPE_FONT, new File(url+res.getString("number_font")));
			text = text.deriveFont(13f);
      		number = number.deriveFont(36f);
        } catch(IOException|FontFormatException e){
            Dashboard.DisplayError(e);
        }
	}
}
