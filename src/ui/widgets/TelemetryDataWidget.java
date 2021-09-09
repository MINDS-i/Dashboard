package com.ui.widgets;

import com.Context;
import com.ui.widgets.UIWidget;
import com.util.UtilHelper;

import com.telemetry.TelemetryListener;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;

import javax.xml.stream.*;
import java.text.ParseException;

import javax.swing.*;
import java.awt.*;

/**
 * Adapted from source originally written by Brett Menzies (See deprecated 
 * TelemetryWidget class)
 * @author Chris Park @ Infinetix Corp.
 * Date: 11-25-2020
 * Description: Widget child class used for displaying Telemetry data.
 */
public class TelemetryDataWidget extends UIWidget {
	protected static final double BATTERY_LOW_WARNING_THRESHOLD = 6.5;
	protected static final double BATTERY_LOW_CUTOFF_THRESHOLD = 6.0;
	protected static final int VOLT_AVERAGING_SIZE = 20;
	
	protected JPanel panel;
	
	private int lineWidth;
	private double[] voltArray;
	private int voltArrayCount;
	
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
            setPreferredSize(new Dimension(100, 20));
            update(0.0);
        }
        
        /**
         * Updates the value and text formatting for this line
         * @param data - the value to update the line text to.
         */
        public void update(double data) {        	
        	String format = String.format(formatStr, data);
            
            if(format.contains("Vcc")) {        		
            	if(data <= BATTERY_LOW_WARNING_THRESHOLD) {
            		this.setForeground(Color.red);
            	}
            	else {
            		this.setForeground(Color.decode("0xEA8300"));
            	}
            	
        		voltArray[voltArrayCount] = data;
        		voltArrayCount++;
        		
        		if(voltArrayCount == VOLT_AVERAGING_SIZE) {
        			evaluateVoltage();
        		}
            }

            int finalWidth = Math.min(format.length(), lineWidth);
            setText(format.substring(0, finalWidth));
        }
        
        /**
         * Repaints the parent widget to avoid a change in order layering
         */
        @Override 
        public void repaint() {
            super.repaint();
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
		super(ctx, "Telemetry");
		this.lineWidth = lineWidth;
		
		voltArray = new double[VOLT_AVERAGING_SIZE];
		initVoltAveraging();
		
        panel = new JPanel();
        panel.setBorder(insets);
        panel.setPreferredSize(new Dimension(115, (25 * items.size())));
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
	 * Initializes the voltage averaing array to a zero value
	 * and resets the count.
	 */
	int DEBUG_RESET_COUNT = 0;
	protected void initVoltAveraging() {
		for(int i = 0; i < VOLT_AVERAGING_SIZE; i++) {
			voltArray[i] = 0.0;
		}
		voltArrayCount = 0;
		
		DEBUG_RESET_COUNT++;
	}
	
	/**
	 * Checks the average voltage value across a range of VOLT_AVERAGE_SIZE. 
	 * If the average is below the BATTERY_LOW_CUTOFF_THRESHOLD, and the unit 
	 * is a ground vehicle, a warning is issued and the unit is stopped to 
	 * prevent abnormal running behavior.
	 * 
	 * NOTE FOR FUTURE IMPROVEMENT: Initialization calls to Line.update() 
	 * with a value of 0.0 will skew the average. This should be fine for
	 * a sufficiently large VOLT_AVERAGE_SIZE, but needs to be accounted
	 * for at some point. 
	 */
	protected void evaluateVoltage() {
		double average;
		
		average = UtilHelper.getInstance().average(
				voltArray, VOLT_AVERAGING_SIZE);
		
		//Reset averaging for the next pass
		initVoltAveraging();
		
		if(average <= BATTERY_LOW_CUTOFF_THRESHOLD) {
			//If this is not a ground vehicle, return. We don't want to crash.
			if(context.getCurrentLocale() != "ground") {
				return;
			}
			
			//Otherwise given that we're initialized and running,
			//Stop the ground vehicle.
			if((context.dash.mapPanel != null)
			&& (context.dash.mapPanel.waypointPanel.getIsMoving())) {
				serialLog.warning("Battery too low. Stopping");
				
				context.sender.changeMovement(false);
				System.err.println("Reset Count: " + DEBUG_RESET_COUNT);
			}
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
