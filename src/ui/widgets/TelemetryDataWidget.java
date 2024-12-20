package com.ui.widgets;

import com.Context;
import com.telemetry.IMonitorListener;
import com.telemetry.TelemetryListener;
import com.telemetry.TelemetryMonitor;
import com.telemetry.TelemetryMonitor.TelemetryDataType;

import javax.swing.*;
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

/**
 * Adapted from source originally written by Brett Menzies (See deprecated
 * TelemetryWidget class)
 *
 * @author Chris Park @ Infinetix Corp.
 * Date: 11-25-2020
 * Description: Widget child class used for displaying Telemetry data.
 */
public class TelemetryDataWidget extends UIWidget {
    //Constants
    protected static final double BATTERY_LOW_WARNING_THRESHOLD = 6.5;
    private final int lineWidth;
    private final Collection<Line> lines = new ArrayList<>();
    //Vars
    protected JPanel panel;
    protected TelemetryMonitor telemetryMonitor;


    /**
     * Class Constructor
     *
     * @param ctx       - The application context
     * @param lineWidth - Maximum Line width for a line of text
     * @param fontSize  - Font size of a line of text
     * @param textColor - Color of the displayed text
     * @param items     - Collection of LineItems to be displayed
     */
    public TelemetryDataWidget(Context ctx, int lineWidth, float fontSize,
                               Color textColor, Collection<LineItem> items) {
        super(ctx, "Telemetry");

        telemetryMonitor = new TelemetryMonitor(ctx);
        this.lineWidth = lineWidth;

        panel = new JPanel();
        panel.setBorder(insets);
        panel.setPreferredSize(new Dimension(115, (25 * items.size())));
        panel.setOpaque(false);

        Font font = ctx.theme.text.deriveFont(fontSize);

        for (LineItem i : items) {
            Line l = new Line(ctx, i.getFormatString());
            l.setFont(font);
            l.setForeground(textColor);
            l.setBackground(i.getBackgroundColor());
            l.setOpaque(true);
            if (i.getTelemetryID() != -1) {
                ctx.telemetry.registerListener(i.getTelemetryID(), l);
            }

            lines.add(l);
            panel.add(l);
        }

        this.setOpaque(false);
        this.add(panel);
    }

    /**
     * Generates a data widget from an xml resource file.
     *
     * @param ctx         - The application context
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
        Collection<LineItem> items = new LinkedList<>();

        try (Reader source = new FileReader(ctx.getResource(resourceKey))) {
            XMLStreamReader r =
                    XMLInputFactory.newInstance().createXMLStreamReader(source);

            while (r.hasNext()) {
                if (r.next() == XMLStreamConstants.START_ELEMENT) {
                    if (r.getLocalName().equals("telemetryWidget")) {
                        width = Integer.parseInt(r.getAttributeValue(null, "width"));
                        fontSize = Float.parseFloat(r.getAttributeValue(null, "fontsize"));
                        defaultFormat = String.format(" %% %df", (width - 1));

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
        return new TelemetryDataWidget(ctx, width, fontSize, textColor, items);
    }

    /**
     * Resets all data lines to 0.0
     */
    public void reset() {
        for (Line l : lines) {
            l.update(0.0);
            l.resetNotificationFlag();
        }
    }

    /**
     * @author Chris Park @ Infinetix Corp.
     * Date: 11-25-2020
     * Description: Nested static class used to display telemetry data.
     */
    public static class LineItem {
        private final String formatStr;
        private final int telemetryID;
        private final Color bgColor;

        /**
         * Class Constructor
         *
         * @param format - the format string for this LineItem
         * @param id     - The Telemetry ID for this LineItem
         * @param bgc    - The Background color for this LineItem
         */
        public LineItem(String format, int id, Color bgc) {
            formatStr = format;
            telemetryID = id;
            bgColor = bgc;
        }

        /**
         * Returns the format string
         *
         * @return - The current format string;
         */
        public String getFormatString() {
            return formatStr;
        }

        /**
         * Returns the telemtry ID
         *
         * @return - the current telemetry ID
         */
        public int getTelemetryID() {
            return telemetryID;
        }

        /**
         * Returns the background color
         *
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
    private class Line extends JLabel implements TelemetryListener, IMonitorListener {
        private final String formatStr;
        private double currData;
        private boolean shouldNotifyUser;


        /**
         * Class Constructor
         *
         * @param ctx    - The application Context
         * @param format - format String for the line text.
         */
        public Line(Context ctx, String format) {
            formatStr = format;
            setPreferredSize(new Dimension(100, 20));
            update(0.0);

            telemetryMonitor.register(this);
            telemetryMonitor.start();

            shouldNotifyUser = true;
        }

        /**
         * Updates the value and text formatting for this line
         *
         * @param data - the value to update the line text to.
         */
        public void update(double data) {
            currData = data;
            String format = String.format(formatStr, data);

            if (format.contains("Vcc")) {
                if (data <= BATTERY_LOW_WARNING_THRESHOLD) {
                    this.setForeground(Color.red);
                }
                else {
                    this.setForeground(Color.decode("0xEA8300"));
                }
            }

            int finalWidth = Math.min(format.length(), lineWidth);
            setText(format.substring(0, finalWidth));
        }

        /**
         * Sends the last received data value to the telemetry manager along
         * with an identifier so that it can process the data appropriately.
         *
         * @param monitor - The reference to the telemetry monitor this object
         *                is registered/subscribed to.
         */
        @Override
        public void updateMonitor(TelemetryMonitor monitor) {
            if (formatStr.contains("Vcc")) {
                monitor.storeData(currData, TelemetryDataType.VOLTAGE);


                //TODO - CP - Rework low voltage warning popup here.
                //Move to TelemetryMonitor evaluateVoltage function to
                //average out stray values and settling time.

                //If below the warning threshold, and this is
                //not a reset value (0.0), and we have not yet
                //warned the user, Do so now.
//        		if((currData < BATTERY_LOW_WARNING_THRESHOLD)
//				&& (currData > 0.0)
//				&& (shouldNotifyUser)) {
//        			JFrame messageFrame = new JFrame("message");
//        			JOptionPane.showMessageDialog(messageFrame,
//        					"Unit voltage is low and will soon shut down. "
//        				  + "Replace or recharge batteries.");
//        			shouldNotifyUser = false;
//        		}
            }
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

        /**
         * Resets the boolean used to determine if this line should
         * give a user facing notification when triggered to do so by
         * some pre-defined, telemetry type specific event.
         */
        public void resetNotificationFlag() {
            shouldNotifyUser = true;
        }
    }


}
