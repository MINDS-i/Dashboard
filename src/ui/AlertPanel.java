package com.ui;
import com.Dashboard;
import com.serial.SerialSender;
import com.serial.Message;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;


public class AlertPanel extends JPanel {
	private static AlertPanel instance = null;
	static int NUM_LINES = 8;
	Font font;
	int lineHeight;
	FontMetrics metrics;
	String[] messages = new String[NUM_LINES];

	public AlertPanel(Font inFont){
		instance = this;
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

	public static void displayMessage(String msg){
		if(instance != null){
			for(int i=NUM_LINES-1; i>0; i--){
				instance.messages[i] = instance.messages[i-1];
			}
			instance.messages[0] = msg;
			instance.repaint();
		}
	}

}
