package com.ui;
import com.Dashboard;
import com.serial.SerialSender;
import com.serial.*;
import com.serial.Messages.*;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;


public class AlertPanel extends JPanel {
	final static int NUM_LINES = 8;
	Font font;
	int lineHeight;
	FontMetrics metrics;
	String[] messages = new String[NUM_LINES];

	public AlertPanel(Font inFont){
		font = inFont;
		this.setPreferredSize(new Dimension(4000,200)); //cheap fix for autoscaling
		setOpaque(false);
		for(int i=0; i<NUM_LINES; i++) messages[i]=" ";
		displayMessage("Welcome!");
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(font);
		metrics = g.getFontMetrics();

		Color color;
		float alpha = 1;
		for(int i=0; i<NUM_LINES; i++){
			color = new Color(0f, 0f, 0f, 1f-((float)i/(NUM_LINES)) );
			g.setColor(color);
			g.drawString(messages[i], (getWidth()-metrics.stringWidth(messages[i]))/2 , getHeight()-metrics.getMaxDescent()-i*metrics.getHeight());
		}
	}

	public void displayMessage(String msg){
		for(int i=NUM_LINES-1; i>0; i--){
			messages[i] = messages[i-1];
		}
		messages[0] = msg;
		repaint();
	}

}
