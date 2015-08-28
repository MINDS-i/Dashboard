package com.ui;

import com.Dashboard;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import javax.imageio.*;
import javax.swing.*;
import com.ui.ninePatch.NinePatchButton;
import com.ui.ninePatch.NinePatch;

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
  	public NinePatch panelPatch;
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

			appIcon = ImageIO.read(new File("./data/app-icon.png"));

			BufferedImage buttonCenter = ImageIO.read(new File("./data/nP/button/center.png"));
            BufferedImage[] buttonWalls = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/button/top.png")),
                ImageIO.read(new File("./data/nP/button/left.png")),
                ImageIO.read(new File("./data/nP/button/right.png")),
                ImageIO.read(new File("./data/nP/button/bottom.png")) };
            BufferedImage[] buttonJoints = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/button/topLeft.png")),
                ImageIO.read(new File("./data/nP/button/topRight.png")),
                ImageIO.read(new File("./data/nP/button/lowerLeft.png")),
                ImageIO.read(new File("./data/nP/button/lowerRight.png")) };
            buttonPatch = new NinePatch(buttonCenter, buttonWalls, buttonJoints);

            BufferedImage panelCenter = ImageIO.read(new File("./data/nP/display/Middle.png"));
            BufferedImage[] panelWalls = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/display/TopBorder.png")),
                ImageIO.read(new File("./data/nP/display/LeftBorder.png")),
                ImageIO.read(new File("./data/nP/display/RightBorder.png")),
                ImageIO.read(new File("./data/nP/display/BottomBorder.png")) };
            BufferedImage[] panelJoints = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/display/TopLeft.png")),
                ImageIO.read(new File("./data/nP/display/TopRight.png")),
                ImageIO.read(new File("./data/nP/display/BottomLeft.png")),
                ImageIO.read(new File("./data/nP/display/BottomRight.png")) };
            panelPatch = new NinePatch(panelCenter, panelWalls, panelJoints);

        } catch(IOException|FontFormatException e){
            Dashboard.displayErrorPopup(e);
        }
	}
	public JButton makeButton(){
		NinePatchButton bt = new NinePatchButton(buttonPatch);
		//bt.setHoverPatch(panelPatch);
		return bt;
	}
	public JButton makeButton(Action a){
		NinePatchButton bt = (NinePatchButton) makeButton();
		bt.setAction(a);
		return bt;
	}
}
