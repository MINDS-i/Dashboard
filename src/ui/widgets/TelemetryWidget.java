package com.ui.widgets;

import com.Context;
import com.telemetry.TelemetryListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.FileReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

//TODO - CP - DEPRECATED - This class has been ported to the UIWidget Heirarchy, and is no longer
//used. Once any necessary information that remains has been gleaned out it should
//be removed. See the TelemetryDataWidget class instead.

public class TelemetryWidget extends JPanel {
    // The top,left,bottom,right border widths of the nine patch image rendered
    // around the internal text
    private final static Border insets = new EmptyBorder(9, 10, 40, 10);
    private final static Border padding = new EmptyBorder(0, 4, 0, 4);

    // Maximum number of chars wide to make the lines
    private final int lineWidth;

    // Collection of line instances being rendered in the telemetry widget
    private final Collection<Line> lines = new ArrayList<>();

//    private final NinePatch np;

    //Private Constructor
    private TelemetryWidget(Context ctx, int lineWidth, float fontSize,
                            Color textColor, Collection<LineItem> items) {

        this.lineWidth = lineWidth;

//        np = ctx.theme.screenPatch;
        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(insets);
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
        dataPanel.setOpaque(false);

        Font font = ctx.theme.text.deriveFont(fontSize);

        for (LineItem i : items) {
            Line l = new Line(ctx, i.getFmt());
            l.setFont(font);
            l.setForeground(textColor);
            l.setBackground(i.getBgColor());
            l.setOpaque(true);
            l.setBorder(padding);
            if (i.getTelemetryId() != -1) {
                ctx.telemetry.registerListener(i.getTelemetryId(), l);
            }

            lines.add(l);
            dataPanel.add(l);
        }

        this.setOpaque(false);
        this.add(dataPanel);
    }

    /**
     * Create a TelemetryWidget.
     * Each line will be `lineWidth` characters
     * Each line is adapted from a LineItem instance in the `items` collection
     * that specifies the label string and what telemetry index to display
     */
    public static TelemetryWidget create(
            Context ctx,
            int lineWidth,
            float fontSize,
            Collection<LineItem> items) {
        return new TelemetryWidget(ctx, lineWidth, fontSize, ctx.theme.textColor, items);
    }

    public static TelemetryWidget fromXML(Context ctx, String resourceKey)
            throws ParseException {
        int width = 0;
        float fontSize = 0f;
        String defaultFormat = "% f";
        Color textColor = ctx.theme.textColor;
        Collection<LineItem> items = new LinkedList<>();

        try (Reader source = new FileReader(ctx.getResource(resourceKey))) {
            XMLStreamReader r =
                    XMLInputFactory.newInstance().createXMLStreamReader(source);

            while (r.hasNext()) {

                if (r.next() == XMLStreamConstants.START_ELEMENT) {
                    if (r.getLocalName().equals("telemetryWidget")) {
                        width = Integer.parseInt(r.getAttributeValue(null, "width"));
                        fontSize = Float.parseFloat(r.getAttributeValue(null, "fontsize"));
                        defaultFormat = String.format(" %% %df", width - 1);

                        String color = r.getAttributeValue(null, "color");
                        if (color != null) {
                            textColor = Color.decode(color);
                        }
                    }
                    else if (r.getLocalName().equals("line")) {
                        String fmt = r.getAttributeValue(null, "fmt");
                        String idx = r.getAttributeValue(null, "telem");

                        String bgString = r.getAttributeValue(null, "bg");
                        Color bg = (bgString != null)
                                ? Color.decode(bgString)
                                : Color.WHITE;

                        String format = (fmt == null) ? defaultFormat : fmt;
                        int index = (idx == null) ? -1 : Integer.parseInt(idx);

                        items.add(new LineItem(format, index, bg));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ParseException("Failed to parse XML from the stream" + e, 0);
        }

        return new TelemetryWidget(ctx, width, fontSize, textColor, items);
    }

    /**
     * Reset all the data lines to 0.0
     */
    public void reset() {
        for (Line l : lines) {
            l.update(0.0);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
//        np.paintIn(g, getWidth(), getHeight());
    }

    public static class LineItem {
        private final String fmt;
        private final int telemetryID;
        private final Color bgcolor;

        public LineItem(String fmt, int telemetryID, Color bgcolor) {
            this.fmt = fmt;
            this.telemetryID = telemetryID;
            this.bgcolor = bgcolor;
        }

        public String getFmt() {
            return fmt;
        }

        public int getTelemetryId() {
            return telemetryID;
        }

        public Color getBgColor() {
            return bgcolor;
        }
    }

    private class Line extends JLabel implements TelemetryListener {
        private final String fmtStr;

        public Line(Context ctx, String fmtStr) {
            this.fmtStr = fmtStr;
            update(0.0);
        }

        public void update(double data) {
            String fmt = String.format(fmtStr, data);
            int finalWidth = Math.min(fmt.length(), lineWidth);
            setText(fmt.substring(0, finalWidth));
        }

        @Override
        public void repaint() {
            super.repaint();
            // Since the parent component (lower in the component tree) could
            //   render a transparent border on top of it's Lines
            //   (higher in the view window), it must be repainted after
            //   this component or the layers will change order depending on
            //   who gets repainted
            TelemetryWidget.this.repaint(getX(), getY(), getWidth(), getHeight());
        }
    }
}
