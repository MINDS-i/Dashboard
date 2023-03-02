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
	
	public static final int DEFAULT_ALERT_LINE_COUNT = 8;
	public static final int DEFAULT_ALERT_LINE_LENGTH = 80;
	
    private List<String> messages = new ArrayList<String>();
    private Color messageColor = Color.WHITE;
    private int lineLength;
    private int lineCount;
    private Font messageFont;

    public AlertPanel(Font messageFont, int lineCount, int lineLength) {
        this.messageFont = messageFont;
        this.lineCount = lineCount;
        this.lineLength = lineLength;

        // Set preferred size for the container assuming W is the widest char
        FontMetrics metrics = this.getFontMetrics(messageFont);
        setPreferredSize(new Dimension(lineLength*metrics.charWidth('W'),
                                       lineCount*metrics.getHeight()));

        setOpaque(false);
        for(int i=0; i<lineCount; i++) addMessage("");
        addMessage("Welcome!");
    }

    public void addMessage(String msg) {
        while(messages.size() >= lineCount)
            messages.remove(0);
        messages.add( EllipsisFormatter.ellipsize(msg,lineLength) );
        repaint();
    }

    public void setColor(Color c) {
        messageColor = c;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(messageFont);

        FontMetrics metrics = this.getFontMetrics(messageFont);
        final int rowHeight = metrics.getHeight();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext frc = g2d.getFontRenderContext();

        for(int i=0; i<lineCount; i++) {
            //Draw string centered on the line
            Graphics2D g2 = (Graphics2D) g2d.create();

            String message = messages.get(lineCount-1-i);
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
