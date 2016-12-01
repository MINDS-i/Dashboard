package com.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;

class FontUtils{
    public static enum DrawPoint{ LeftCenter, RightCenter, TopCenter, BottomCenter }
    public static void drawString(DrawPoint dp, Graphics2D g, String s, int x, int y){
        Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
        switch(dp){
            case BottomCenter:
                g.drawString(s, x - (int)r.getCenterX(), y);
                break;
            case TopCenter:
                g.drawString(s, x - (int)r.getCenterX(), y + (int)r.getHeight());
                break;
            case LeftCenter:
                g.drawString(s, x, y - (int)r.getCenterY());
                break;
            case RightCenter:
                g.drawString(s, x - (int)r.getWidth(), y - (int)r.getCenterY());
                break;
        }
    }

    public static Font setFontHeight(Font f, Graphics g, int newHeight){
        FontMetrics m = g.getFontMetrics(f);
        double scale = (double)newHeight / (double)m.getHeight();
        AffineTransform transform = new AffineTransform();
        transform.setToScale(scale, scale);
        return f.deriveFont(transform);
    }
}
