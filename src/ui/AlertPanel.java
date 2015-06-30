package com.ui;
import com.Dashboard;
import com.serial.SerialSender;
import com.serial.*;
import com.serial.Messages.*;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.font.*;


public class AlertPanel extends JPanel {
	private final static int NUM_LINES = 8;
	private Theme theme;
	private int lineHeight;
	private FontMetrics metrics;
	private String[] messages = new String[NUM_LINES];

	public AlertPanel(){
		this.setPreferredSize(new Dimension(4000,200)); //cheap fix for autoscaling
		setOpaque(false);
		for(int i=0; i<NUM_LINES; i++) messages[i]="";
		displayMessage("Welcome!");
	}

	public AlertPanel(Theme theme){
		this();
		this.theme = theme;
	}

	public void setTheme(Theme theme){
		this.theme = theme;
	}

	private Font getMessageFont(){
		if(theme != null && theme.alertFont != null)
			return theme.alertFont;
		else
			return new Font(Font.MONOSPACED,Font.BOLD,16);
	}

	private Color getMessageColor(){
		if (theme!=null)
			return theme.textColor;
		else
			return Color.WHITE;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(getMessageFont());

		metrics = g.getFontMetrics();
		final int rowHeight = metrics.getHeight();

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    		                 RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g2d.getFontRenderContext();
		for(int i=0; i<NUM_LINES; i++){
			//Draw string centered on the line
			Graphics2D g2 = (Graphics2D) g2d.create();

			String message = messages[i];
			GlyphVector gv = g2.getFont().createGlyphVector(frc, message);
    		Shape shape = gv.getOutline();

			final Rectangle bounds = shape.getBounds();
			final int xPos = (getWidth() - bounds.width) /2;
			final int yPos = getHeight() - (bounds.height/2) - rowHeight*i;

			g2.translate(xPos, yPos);

			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(6,
							 BasicStroke.JOIN_ROUND,
							 BasicStroke.CAP_ROUND));
			g2.draw(shape);
			g2.setColor(getMessageColor());
			g2.fill(shape);

			g2.dispose();
		}
	}

	//private void paintString(Graphics g, String msg, int x, int y){}

	public void displayMessage(String msg){
		for(int i=NUM_LINES-1; i>0; i--){
			messages[i] = messages[i-1];
		}
		messages[0] = msg;
		repaint();
	}

}
