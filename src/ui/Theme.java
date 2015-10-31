package com.ui;

import com.Dashboard;
import com.ui.ninePatch.NinePatch;
import com.ui.ninePatch.NinePatchButton;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.imageio.*;
import javax.swing.*;

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
	public BufferedImage appIcon;
	public Font number;
  	public Font text;
  	public Font alertFont;
  	public NinePatch buttonPatch;
    public NinePatch buttonHover;
    public NinePatch buttonPress;
  	public NinePatch panelPatch;
    public NinePatch screenPatch;
  	public Color textColor;
	public Theme(Locale locale){
		ResourceBundle res = ResourceBundle.getBundle("resources", locale);
        try{
        	String url = res.getString("resource_folder");
			roverImage       = ImageIO.read(new File(url+res.getString("map_icon")));
			waypointImage    = ImageIO.read(new File(url+res.getString("waypoint_icon")));
			waypointSelected = ImageIO.read(new File(url+res.getString("waypoint_selected")));
			lineSegment      = ImageIO.read(new File(url+res.getString("line_segment")));
			refreshImage     = ImageIO.read(new File(url+res.getString("refresh_icon")));
			gaugeBackground  = ImageIO.read(new File(url+res.getString("gauge_background")));
			roverSide        = ImageIO.read(new File(url+res.getString("drone_side")));
			roverTop         = ImageIO.read(new File(url+res.getString("drone_top")));
			roverFront       = ImageIO.read(new File(url+res.getString("drone_front")));
			gaugeRed         = ImageIO.read(new File(url+res.getString("gauge_arrow")));
			gaugeSquare      = ImageIO.read(new File(url+res.getString("gauge_screen")));
			gaugeGlare       = ImageIO.read(new File(url+res.getString("gauge_glare")));
			text             = Font.createFont(Font.TRUETYPE_FONT, new File(url+res.getString("text_font")));
			number           = Font.createFont(Font.TRUETYPE_FONT, new File(url+res.getString("number_font")));
			text             = text.deriveFont(13f);
			number           = number.deriveFont(36f);
			textColor        = new Color(255,155,30);
			alertFont        = null;
			appIcon          = ImageIO.read(new File("./data/app-icon.png"));
            buttonPatch      = NinePatch.loadFrom(Paths.get("./data/nP/button"));
            buttonHover      = NinePatch.loadFrom(Paths.get("./data/nP/buttonHovered"));
            buttonPress      = NinePatch.loadFrom(Paths.get("./data/nP/buttonPressed"));
            panelPatch       = NinePatch.loadFrom(Paths.get("./data/nP/display"));
            screenPatch      = NinePatch.loadFrom(Paths.get("./data/nP/screen"));
        } catch(IOException|FontFormatException e){
            Dashboard.displayErrorPopup(e);
        }
	}
	public JButton makeButton(){
		NinePatchButton bt = new NinePatchButton(buttonPatch);
		bt.setHoverPatch(buttonHover);
        bt.setPressedPatch(buttonPress);
		return bt;
	}
	public JButton makeButton(Action a){
		JButton bt = makeButton();
		bt.setAction(a);
		return bt;
	}
}
