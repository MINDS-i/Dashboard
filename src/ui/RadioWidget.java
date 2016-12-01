package com.ui;

import com.telemetry.TelemetryListener;
import com.ui.ninePatch.*;
import com.ui.ArtificialHorizon;
import com.Context;
import static com.ui.FontUtils.*;
import com.serial.Serial;

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
        JPanel container = new TransparentPanel(ctx, size);

        Bars b = new Bars(ctx.theme.text);
        container.add(b);

        ctx.telemetry.registerListener(Serial.RDPITCH,
            (double d) -> { b.data[0]=(float)d/180.0f; });
        ctx.telemetry.registerListener(Serial.RDROLL,
            (double d) -> { b.data[1]=(float)d/180.0f; });
        ctx.telemetry.registerListener(Serial.RDYAW,
            (double d) -> { b.data[2]=(float)d/180.0f; });
        ctx.telemetry.registerListener(Serial.RDTHROTTLE,
            (double d) -> { b.data[3]=(float)d/180.0f; });
        ctx.telemetry.registerListener(Serial.RDGEAR,
            (double d) -> { b.data[4]=(float)d/180.0f; });

        return container;
    }

    private static class Bars extends JPanel{
        private static final int NUM_BARS = 5;
        float[] data = {0.0f, 0.25f, 0.5f, 0.75f, 1.0f};
        String[] labels = {"P", "R", "Y", "T", "G"};
        private static final Color background = new Color(0xDFDFDF);
        private static final Color fillColor = new Color(0x5B93C5);
        private static final Color emptyBar = new Color(0xbababa);
        private static final Color borderColor = Color.BLACK;
        private static final Stroke borderStroke = new BasicStroke(1.5f);
        private Font font;
        Bars(Font font){
            this.font = font;
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setFont(setFontHeight(font, g2d, getHeight()/5));

            g2d.setColor(background);
            g2d.fillRect(0,0,getWidth(),getHeight());

            g2d.setColor(Color.BLACK);
            int spacing = getWidth()/NUM_BARS;
            for(int i=0; i<NUM_BARS; i++){
                fillBar(g2d, i*spacing, spacing, data[i], labels[i]);
            }
        }

        private void fillBar(Graphics2D g, int xpos, int width, float mark, String label){
            int margin = 6;
            int textHeight = getHeight()/5;
            int barTop = margin+textHeight;

            int barWidth = width-2*margin;
            int barHeight = getHeight()-barTop-margin;

            drawString(DrawPoint.TopCenter, g, label, xpos+width/2, margin);

            int fillHeight = (int) (((float)barHeight)*(1.0-mark));

            g.setColor(emptyBar);
            g.fillRect(xpos+margin, barTop, barWidth, fillHeight);
            g.setColor(fillColor);
            g.fillRect(xpos+margin, barTop+fillHeight, barWidth, barHeight-fillHeight);

            g.setColor(borderColor);
            g.setStroke(borderStroke);
            g.drawRect(xpos+margin, barTop, barWidth, barHeight);
            g.drawLine(xpos+margin, barTop+barHeight/2, xpos+margin+barWidth, barTop+barHeight/2);
        }
    }
}
