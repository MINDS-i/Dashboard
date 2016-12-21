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
        int[] channels = {RDPITCH, RDROLL, RDYAW, RDTHROTTLE, RDGEAR};
        RDisp b = new RDisp(ctx.theme);

        for(int i=0; i<channels.length; i++){
            final Integer idx = i;
            ctx.telemetry.registerListener(channels[i],
                (double d) -> { b.data[idx]=(float)d/180f; });
        }

        JPanel container = new TransparentPanel(ctx, size);
        container.add(b);
        return container;
    }

    private static class RDisp extends JPanel{
        float[] data = new float[5];
        private static final int MARGIN = 6;
        private static final Color background = new Color(0xDFDFDF);
        private static final Color emptyGraph = new Color(0xBABABA);
        private static final Color guideColor = Color.GRAY;
        private static final Stroke guideStroke =
            new BasicStroke(
                1.0f, //weight
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER,
                1, //miter
                new float[] {5f, 5f}, // dash
                0.0f); // dash phase
        private static final Stroke borderStroke = new BasicStroke(1.5f);
        private static final Color borderColor = Color.BLACK;
        private Color iconColor;
        private Font font;

        RDisp(Theme theme){
            this.font = theme.text.deriveFont(Font.BOLD, 14.0f);
            this.iconColor  = theme.textColor;
            for(int i=0; i<5; i++) data[i] = 0.5f;
        }

        @Override
        public void paintComponent(Graphics g){
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setStroke(borderStroke);
            g2d.setColor(background);
            g2d.fillRect(0,0,getWidth(), getHeight());

            float wUnit = ((float)getWidth());
            float hUnit = ((float)getHeight());

            g2d.setFont(font);
            g2d.setColor(borderColor);
            drawString(
                DrawPoint.TopCenter,
                g2d,
                "Radio Status",
                (int) (0.5*wUnit),
                (int) (0.025*hUnit));

            // box sizes as ratios of height/width
            int leftBoxX = (int) (0.05f*wUnit);
            int rightBoxX = (int) (0.525f*wUnit);
            int radioBoxY = (int) (0.50f*hUnit); // .35
            int switchBoxY = (int) (0.25f*hUnit); // .2
            int boxLength = (int) (0.425f*wUnit);
            int switchBoxHeight = (int) (0.15f*hUnit);

            // Pick which switch box to fill
            boolean switchDown = data[4] <= 0.5;
            Color leftSwitchBoxFill, rightSwitchBoxFill;
            if(switchDown){
                leftSwitchBoxFill = iconColor;
                rightSwitchBoxFill = emptyGraph;
            } else {
                leftSwitchBoxFill = emptyGraph;
                rightSwitchBoxFill = iconColor;
            }

            // switch box left bg and fill
            g2d.setColor(leftSwitchBoxFill);
            g2d.fillRect(
                leftBoxX,
                switchBoxY,
                boxLength,
                switchBoxHeight);
            g2d.setColor(borderColor);
            g2d.drawRect(
                leftBoxX,
                switchBoxY,
                boxLength,
                switchBoxHeight);

            // switch box right bg and fill
            g2d.setColor(rightSwitchBoxFill);
            g2d.fillRect(
                rightBoxX,
                switchBoxY,
                boxLength,
                switchBoxHeight);
            g2d.setColor(borderColor);
            g2d.drawRect(
                rightBoxX,
                switchBoxY,
                boxLength,
                switchBoxHeight);

            // left and right radio boxes
            drawRadioBox(g2d,
                leftBoxX,
                radioBoxY,
                boxLength,
                boxLength,
                data[2],
                data[3]);
            drawRadioBox(g2d,
                rightBoxX,
                radioBoxY,
                boxLength,
                boxLength,
                data[1],
                data[0]);
        }

        private void drawRadioBox(
          Graphics g,
          int xpos,
          int ypos,
          int width,
          int height,
          float iconX,
          float iconY){
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(xpos, ypos);

            int iconCenterX = width- (int)(((float)width) *iconX);
            int iconCenterY = height-(int)(((float)height)*iconY);
            int iconRadius  = 4;

            // draw the box background
            g2d.setColor(emptyGraph);
            g2d.fillRect(0, 0, width, height);

            // draw the center guide lines
            g2d.setColor(guideColor);
            g2d.setStroke(guideStroke);
            g2d.drawLine(0, height/2, width, height/2);
            g2d.drawLine(width/2, 0, width/2, height);

            // draw the border around the radio box
            g2d.setColor(borderColor);
            g2d.setStroke(borderStroke);
            g2d.drawRect(0, 0, width, height);

            // draw the radio stick icon
            g2d.setColor(iconColor);
            g2d.fillOval(iconCenterX-iconRadius, iconCenterY-iconRadius,
                        2*iconRadius, 2*iconRadius);
            g2d.setColor(borderColor);
            g2d.drawOval(iconCenterX-iconRadius, iconCenterY-iconRadius,
                        2*iconRadius, 2*iconRadius);

            g2d.dispose();
        }
    }
}
