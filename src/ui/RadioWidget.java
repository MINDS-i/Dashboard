package com.ui;

import com.telemetry.TelemetryListener;
import com.ui.ninePatch.*;
import com.ui.ArtificialHorizon;
import com.Context;
import static com.ui.FontUtils.*;
import static com.serial.Serial.*;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.FontMetrics;

import javax.xml.stream.*;
import java.text.ParseException;

public class RadioWidget{
    /**
     * Create a square radio widget displaying the five radio telemetry
     *   lines as bar graphs in a `size`^2 region
     */
    public static JPanel create(Context ctx, int size){
        int num_channels = 5;
        int[] telemetryChannels = {RDPITCH, RDROLL, RDYAW, RDTHROTTLE, RDGEAR};
        String[] labels = {"P", "R", "Y", "T", "S"};

        Bars b = new Bars(ctx.theme.text, num_channels, labels, ctx.theme.textColor);

        for(int i=0; i<num_channels; i++){
            final Integer idx = i;
            ctx.telemetry.registerListener(telemetryChannels[i],
                (double d) -> { b.data[idx]=(float)d/180f; });
        }

        JPanel container = new TransparentPanel(ctx, size);
        container.add(b);
        return container;
    }

    private static class Bars extends JPanel{
        int numBars;
        float[] data;
        String[] labels;
        private static final Color background = new Color(0xDFDFDF);
        private static final Color emptyBar = new Color(0xBABABA);
        private static final Color borderColor = Color.BLACK;
        private static final Stroke borderStroke = new BasicStroke(1.5f);
        private static final int MARGIN = 6;
        private Color fillColor;
        private Font font;
        Bars(Font font, int numBars, String[] labels, Color fillColor){
            this.font = font;
            this.labels = labels;
            this.numBars = numBars;
            this.fillColor = fillColor;
            this.data = new float[numBars];
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setFont(setFontHeight(font, g2d, getHeight()/5));

            g2d.setColor(background);
            g2d.fillRect(0,0,getWidth(),getHeight());

            int spacing = getWidth()/numBars;
            for(int i=0; i<numBars; i++){
                fillBar(g2d, i*spacing, spacing, data[i], labels[i]);
            }
        }

        private void fillBar(Graphics2D g, int xpos, int width, float mark, String label){
            int textHeight = getHeight()/5;
            int barTop = textHeight;
            int barWidth = width-2*MARGIN;
            int barHeight = getHeight()-barTop-MARGIN;
            int barCenter = barTop+barHeight/2;
            int barRightPos = xpos+MARGIN+barWidth;
            int fillHeight = (int) (((float)barHeight)*(1.0-mark));

            // Label String
            g.setColor(borderColor);
            drawString(DrawPoint.TopCenter, g, label, xpos+width/2, 0);
            // Empty part of the bar fill
            g.setColor(emptyBar);
            g.fillRect(xpos+MARGIN, barTop, barWidth, fillHeight);
            // Fill part of the bar fill
            g.setColor(fillColor);
            g.fillRect(xpos+MARGIN, barTop+fillHeight, barWidth, barHeight-fillHeight);
            // Bar Outline
            g.setColor(borderColor);
            g.setStroke(borderStroke);
            g.drawRect(xpos+MARGIN, barTop, barWidth, barHeight);
            // Center Line
            g.drawLine(xpos+MARGIN, barCenter, barRightPos, barCenter);
        }
    }
}
