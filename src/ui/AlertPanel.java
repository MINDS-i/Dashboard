package com.ui;
import com.logging.*;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.font.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;
import java.io.*;

public class AlertPanel extends JPanel {
	private final static int REC_LINE_LEN = 80;
	private final static int NUM_LINES = 8;
	private Font messageFont = new Font(Font.MONOSPACED,Font.BOLD,16);
	private Color messageColor = Color.WHITE;
	private List<String> messages = new ArrayList<String>(NUM_LINES);
	private List<Object> gcHold = new ArrayList<Object>();
	private boolean fontUpdated = true;
	private int lineHeight;
	private FontMetrics metrics;

	public static AlertPanel createLogDisplay(String logNS, Level level){
		AlertPanel ap = new AlertPanel();
		Logger log = Logger.getLogger(logNS);
		// if `log` gets garbage collected ap will stop getting updates
		ap.addGcHold(log);
		Handler handler = new SimpleHandler((String s) -> ap.addMessage(s));
		handler.setFormatter(new EllipsisFormatter(REC_LINE_LEN));
		log.addHandler(handler);
		return ap;
	}

	public AlertPanel(){
		setOpaque(false);
		for(int i=0; i<NUM_LINES; i++) addMessage("");
		addMessage("Welcome!");
		updateDim();
	}

	public void addMessage(String msg){
		while(messages.size() >= NUM_LINES)
			messages.remove(0);
		messages.add(msg);
		repaint();
	}

	public void setFont(Font f){
		messageFont = f;
	}

	public void setColor(Color c){
		messageColor = c;
	}

	private void addGcHold(Object o){
		gcHold.add(o);
	}

	private void updateDim(){
		fontUpdated = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(messageFont);

		metrics = g.getFontMetrics();
		final int rowHeight = metrics.getHeight();

		//use number of lines and 80 char length to calculate preferred size
		if(fontUpdated){
			setPreferredSize(new Dimension(REC_LINE_LEN*metrics.charWidth('W'),
										   NUM_LINES*metrics.getHeight()));
			fontUpdated = false;
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    		                 RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g2d.getFontRenderContext();

		for(int i=0; i<NUM_LINES; i++){
			//Draw string centered on the line
			Graphics2D g2 = (Graphics2D) g2d.create();

			String message = messages.get(NUM_LINES-1-i);
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
			g2.setColor(messageColor);
			g2.fill(shape);

			g2.dispose();
		}
	}
}
