package com.ui;

import com.ui.TelemetryListener;
import com.ui.ninePatch.*;
import com.Context;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;
import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.FontMetrics;

import javax.xml.stream.*;
import java.text.ParseException;


public class TelemetryWidget extends JPanel{
    // The top,left,bottom,right border widths of the nine patch image rendered
    // around the internal text
    private final static Border insets = new EmptyBorder(15,18,46,18);
    // Maximum number of chars wide to make the lines
    private final int lineWidth;
    // Collection of line instances being rendered in the telemetry widget
    private final Collection<Line> lines = new ArrayList<Line>();

    public static class LineItem {
        private String label;
        private int telemetryID;
        public LineItem(String label, int telemetryID){
            this.label = label;
            this.telemetryID = telemetryID;
        }
        public String getLabel(){ return label; }
        public int getTelemetryId(){ return telemetryID; }
    }

    /**
     * Create a TelemetryWidget.
     * Each line will be `lineWidth` characters
     * Each line is adapted from a LineItem instance in the `items` collection
     *    that specifies the label string and what telemetry index to display
     */
    public static TelemetryWidget create(
      Context ctx,
      int lineWidth,
      Collection<LineItem> items){
        return new TelemetryWidget(ctx, lineWidth, items);
    }

    public static TelemetryWidget fromXML(Context ctx, String resourceKey)
      throws ParseException {
        int width = 0;
        Collection<LineItem> items = new LinkedList<LineItem>();
        try (Reader source = new FileReader(ctx.getResource(resourceKey))){
            XMLStreamReader r = XMLInputFactory.newInstance().
                                        createXMLStreamReader(source);
            while(r.hasNext()) {
                switch(r.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if(r.getLocalName().equals("telemetryWidget")) {
                            width = Integer.valueOf(r.getAttributeValue(null, "width"));
                        } else if(r.getLocalName().equals("line")) {
                            String label = r.getAttributeValue(null,"label");
                            int index = Integer.valueOf(r.getAttributeValue(null,"index"));
                            items.add(new LineItem(label,index));
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new ParseException("Failed to parse XML from the stream"+e,0);
        }

        return new TelemetryWidget(ctx, width, items);
    }

    /**
     * Reset all the data lines to 0.0
     */
    public void reset(){
        for(Line l : lines){
            l.update(0.0);
        }
    }

    private TelemetryWidget(
      Context ctx,
      int lineWidth,
      Collection<LineItem> items){
        this.lineWidth = lineWidth;

        NinePatchPanel dataPanel = new NinePatchPanel(ctx.theme.screenPatch);
        dataPanel.setBorder(insets);
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
        dataPanel.setOpaque(false);

        for(LineItem i : items){
            Line l = new Line(ctx, i.getLabel());
            ctx.telemetry.registerListener(i.getTelemetryId(), l);
            lines.add(l);
            dataPanel.add(l);
        }

        this.setOpaque(false);
        this.add(dataPanel);
    }

    private class Line extends JLabel implements TelemetryListener {
        private String label;
        public Line(Context ctx, String prefix) {
            setForeground(ctx.theme.textColor);
            setFont(ctx.theme.text);
            label = prefix;
            update(0.0);
        }
        public void update(double data) {
            String fmt = String.format("%s%c%f",
                            label, (data >= 0)? ' ' : '-', Math.abs(data));
            int finalWidth = Math.min(fmt.length(), lineWidth);
            setText(fmt.substring(0,finalWidth));
        }
    }

}
