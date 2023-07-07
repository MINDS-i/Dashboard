package com.ui;

import com.Dashboard;
import com.Context;
import com.ui.ninePatch.NinePatch;
import com.ui.ninePatch.NinePatchButton;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;
import javax.imageio.*;
import javax.swing.*;

//POD class for theme elements
public class Theme {
	//Image Assets
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
    public BufferedImage homeIcon;
    public BufferedImage logoWatermark;
    public BufferedImage pingRed;
    public BufferedImage pingYellow;
    public BufferedImage pingGreen;
    public BufferedImage pingSpacer;
    public BufferedImage verticalMeterRed;
    public BufferedImage verticalMeterYellow;
    public BufferedImage verticalMeterGreen;
    public BufferedImage verticalMeterSpacer;
    public BufferedImage swathHorizontal0;
    public BufferedImage swathVertical0;
    public BufferedImage swathNone;
    
    //Fonts & Coloring
    public Font number;
    public Font text;
    public Font alertFont;
    public Color textColor;
    
    //UI Panel Pieces
    public NinePatch buttonPatch;
    public NinePatch buttonHover;
    public NinePatch buttonPress;
    public NinePatch panelPatch;
    public NinePatch screenPatch;
    public NinePatch horizonBorder;
    
    
    
    public Theme(Context ctx) {
        try {
            String img = ctx.getResource("image_folder");
            String ttf = ctx.getResource("font_folder");
            String np = ctx.getResource("patch_folder");
            
            roverImage       = ImageIO.read(new File(img+ctx.getResource("map_icon")));
            waypointImage    = ImageIO.read(new File(img+ctx.getResource("waypoint_icon")));
            waypointSelected = ImageIO.read(new File(img+ctx.getResource("waypoint_selected")));
            lineSegment      = ImageIO.read(new File(img+ctx.getResource("line_segment")));
            refreshImage     = ImageIO.read(new File(img+ctx.getResource("refresh_icon")));
            gaugeBackground  = ImageIO.read(new File(img+ctx.getResource("gauge_background")));
            roverSide        = ImageIO.read(new File(img+ctx.getResource("drone_side")));
            roverTop         = ImageIO.read(new File(img+ctx.getResource("drone_top")));
            roverFront       = ImageIO.read(new File(img+ctx.getResource("drone_front")));
            gaugeRed         = ImageIO.read(new File(img+ctx.getResource("gauge_arrow")));
            gaugeSquare      = ImageIO.read(new File(img+ctx.getResource("gauge_screen")));
            gaugeGlare       = ImageIO.read(new File(img+ctx.getResource("gauge_glare")));
            appIcon          = ImageIO.read(new File(img+ctx.getResource("app_icon")));
            homeIcon         = ImageIO.read(new File(img+ctx.getResource("home_icon")));
            logoWatermark	 = ImageIO.read(new File(img+ctx.getResource("watermark")));
            pingRed			 = ImageIO.read(new File(img+ctx.getResource("ping_red")));
            pingYellow		 = ImageIO.read(new File(img+ctx.getResource("ping_yellow")));
            pingGreen		 = ImageIO.read(new File(img+ctx.getResource("ping_green")));
            pingSpacer		 = ImageIO.read(new File(img+ctx.getResource("ping_spacer")));
            verticalMeterRed = ImageIO.read(new File(img+ctx.getResource("gps_red")));
            verticalMeterYellow = ImageIO.read(new File(img+ctx.getResource("gps_yellow")));
            verticalMeterGreen = ImageIO.read(new File(img+ctx.getResource("gps_green")));
            verticalMeterSpacer = ImageIO.read(new File(img+ctx.getResource("gps_spacer")));
            swathHorizontal0 = ImageIO.read(new File(img+ctx.getResource("swath_horizontal_0")));
            swathVertical0	 = ImageIO.read(new File(img+ctx.getResource("swath_vertical_0")));
            swathNone	 	 = ImageIO.read(new File(img+ctx.getResource("swath_none")));
            buttonPatch      = NinePatch.loadFrom(Paths.get(np+ctx.getResource("button")));
            buttonHover      = NinePatch.loadFrom(Paths.get(np+ctx.getResource("button_hovered")));
            buttonPress      = NinePatch.loadFrom(Paths.get(np+ctx.getResource("button_pressed")));
            panelPatch       = NinePatch.loadFrom(Paths.get(np+ctx.getResource("display_panel")));
            screenPatch      = NinePatch.loadFrom(Paths.get(np+ctx.getResource("info_screen")));
            horizonBorder    = NinePatch.loadFrom(Paths.get(np+ctx.getResource("horizon_border")));
            text             = Font.createFont(Font.TRUETYPE_FONT,
                                               new File(ttf+ctx.getResource("text_font")));
            number           = Font.createFont(Font.TRUETYPE_FONT,
                                               new File(ttf+ctx.getResource("number_font")));
            text             = text.deriveFont(13f);
            number           = number.deriveFont(36f);
            textColor        = new Color(255, 155, 30); //Orange
            alertFont        = new Font(Font.MONOSPACED, Font.BOLD, 14);
        } catch(IOException|FontFormatException e) {
            Logger.getLogger("d.io").severe(e.toString());
            Dashboard.displayErrorPopup(e);
        }
    }
    public JButton makeButton() {
        NinePatchButton bt = new NinePatchButton(buttonPatch);
        bt.setHoverPatch(buttonHover);
        bt.setPressedPatch(buttonPress);
        return bt;
    }
    public JButton makeButton(Action a) {
        JButton bt = makeButton();
        bt.setAction(a);
        return bt;
    }
}
