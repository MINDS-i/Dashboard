package com.ui;

import com.Context;
import com.ui.UIWidget;

import com.telemetry.TelemetryListener;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;

import javax.xml.stream.*;
import java.text.ParseException;

import javax.swing.*;
import java.awt.*;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 11-25-2020
 * Description: Widget child class use for displaying Telemetry data.
 */
public class TelemetryDataWidget extends UIWidget {
	private int lineWidth;
	private Collection<Line> lines = new ArrayList<Line>();
	
	/**
	 * @author Chris Park @ Infinetix Corp.
	 * Date: 11-25-2020
	 * Description: Nested static internal class used to display telemetry data.
	 */
	public static class LineItem {
		private String formatStr;
		private int telemetryID;
		private Color bgColor;
		
		/**
		 * Class Constructor
		 * @param format - the format string for this LineItem
		 * @param id  - The Telemetry ID for this LineItem
		 * @param bgc - The Background color for this LineItem
		 */
		public LineItem(String format, int id, Color bgc) {
			formatStr 	= format;
			telemetryID = id;
			bgColor 	= bgc;
		}
		
		/**
		 * Returns the format string
		 * @return - The current format string;
		 */
		public String getFormatString() {
			return formatStr;
		}
		
		/**
		 * Returns the telemtry ID
		 * @return - the current telemetry ID
		 */
		public int getTelemetryID() {
			return telemetryID;
		}
		
		/**
		 * Returns the background color
		 * @return - The current background color
		 */
		public Color getBackgroundColor() {
			return bgColor;
		}
	}
	
	/**
	 * @author Chris Park @ Infinetix Corp.
	 * Date: 11-25-2020
	 * Description: Nested internal class used to display telemetry data.
	 */
	private class Line extends JLabel implements TelemetryListener {
        private String formatStr;
        
        /**
         * Class Constructor
         * @param ctx - The application Context
         * @param format - format String for the line text.
         */
        public Line(Context ctx, String format) {
            formatStr = format;
            update(0.0);
        }
        
        /**
         * Updates the text and formatting help by this line
         */
        public void update(double data) {
            String format = String.format(formatStr, data);
            int finalWidth = Math.min(format.length(), lineWidth);
            setText(format.substring(0, finalWidth));
        }
        
        /**
         * Repaints the parent widget to avoid a change in order layering
         */
        @Override 
        public void repaint() {
            super.repaint();
            //Repaint the parent to ensure no change in layer order
            TelemetryDataWidget.this.repaint(getX(), getY(), 
            		getWidth(), getHeight());
        }
	}
	
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 * @param lineWidth - Maximum Line width for a line of text
	 * @param fontSize  - Font size of a line of 
	 * @param textColor - Color of the displayed text
	 * @param items - Collection of LineItems to be displayed
	 */
	public TelemetryDataWidget(Context ctx, int lineWidth, float fontSize,
			Color textColor, Collection<LineItem> items) {
		super(ctx);
		
		this.lineWidth = lineWidth;
		
        JPanel panel = new JPanel();
        panel.setBorder(insets);
        panel.setPreferredSize(new Dimension(100, (20 * items.size())));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setOpaque(false);

        Font font = ctx.theme.text.deriveFont(fontSize);

        for(LineItem i : items) {
            Line l = new Line(ctx, i.getFormatString());
            l.setFont(font);
            l.setForeground(textColor);
            l.setBackground(i.getBackgroundColor());
            l.setOpaque(true);
            if(i.getTelemetryID() != -1) {
                ctx.telemetry.registerListener(i.getTelemetryID(), l);
            }

            lines.add(l);
            panel.add(l);
        }

        this.setOpaque(false);
        this.add(panel);
	}
	
	/**
	 * Resets all data lines to 0.0
	 */
	public void reset() {
		for(Line l : lines) {
			l.update(0.0);
		}
	}

	/**
	 * Generates a data widget from an xml resource file.
	 * @param ctx - The application context
	 * @param resourceKey - The XML Resource key
	 * @return - Telemetry Data Widget
	 * @throws ParseException
	 */
	public static TelemetryDataWidget fromXML(Context ctx, String resourceKey)
		throws ParseException {
		int width = 0;
        float fontSize = 0f;
        String defaultFormat = "% f";
        Color textColor = ctx.theme.textColor;
        Collection<LineItem> items = new LinkedList<LineItem>();
        
        try (Reader source = new FileReader(ctx.getResource(resourceKey))) {
            XMLStreamReader r = 
            		XMLInputFactory.newInstance().createXMLStreamReader(source);
            
            while(r.hasNext()) {
                switch(r.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if(r.getLocalName().equals("telemetryWidget")) {
                            width = Integer.valueOf(r.getAttributeValue(null, "width"));
                            fontSize = Float.valueOf(r.getAttributeValue(null, "fontsize"));
                            defaultFormat = String.format(" %% %df", (width - 1));

                            String color = r.getAttributeValue(null, "color");
                            if(color != null) {
                                textColor = Color.decode(color);
                            }
                        } 
                        else if(r.getLocalName().equals("line")) {
                            String fmt = r.getAttributeValue(null, "fmt");
                            String idx = r.getAttributeValue(null, "telem");

                            String bgString = r.getAttributeValue(null,"bg");
                            Color bg = (bgString != null)
                                        ? Color.decode(bgString)
                                        : Color.WHITE;

                            String format = (fmt == null) ? defaultFormat : fmt;
                            int index = (idx == null) ? -1 : Integer.valueOf(idx);

                            items.add(new LineItem(format,index, bg));
                        }
                        break;
                }
            }
        } 
        catch (Exception e) {
            throw new ParseException("Failed to parse XML from the stream"+ e, 0);
        }

        return new TelemetryDataWidget(ctx, width, fontSize, textColor, items);
	}
	
	
}
