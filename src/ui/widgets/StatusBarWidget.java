package com.ui.widgets;

import com.Context;

import javax.swing.*;
import java.awt.*;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 1-26-21
 * Description: Dashboard Widget class used to display contextual vehicle
 * information in an easy to interpret visual format. Example use cases would be
 * displaying a units current decision state, or perhaps a visual warning level
 * indication related to a vehicle sensor or other data processing part over
 * serial communication.
 */
public class StatusBarWidget extends JPanel {

    //Constants
    protected static final float FONT_SIZE = 12.0f;
    protected static final int MIN_BAR_WIDTH = 100;
    protected static final int BAR_HEIGHT = 20;

    //Standard Variables
    protected Context context;

    //Bar Display Label
    protected JLabel statusLabel;

    protected StatusType currentType;

    /**
     * Class Constructor
     *
     * @param ctx - The application context
     */
    public StatusBarWidget(Context ctx) {
        context = ctx;

        //configure initial label dimensions, font, and alignment
        statusLabel = new JLabel();
        statusLabel.setOpaque(true);
        statusLabel.setFont(context.theme.text.deriveFont(FONT_SIZE));
        statusLabel.setMinimumSize(new Dimension(MIN_BAR_WIDTH, BAR_HEIGHT));
        statusLabel.setPreferredSize(new Dimension(MIN_BAR_WIDTH, BAR_HEIGHT));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        update(StatusType.NORMAL);

        this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(statusLabel);
    }

    /**
     * Updates the text, foreground, and background color of this StatusBarWidget
     * using the parameters defined in the enum type.
     *
     * @param type - the enum type to pull data from.
     */
    public void update(StatusType type) {
        statusLabel.setText(type.text);
        statusLabel.setForeground(type.fgColor);
        statusLabel.setBackground(type.bgColor);
        currentType = type;
    }

    /**
     * Returns the current Status displayed by the bar.
     *
     * @return - StatusType
     */
    public StatusType getState() {
        return currentType;
    }

    /**
     * Pre-defined status type enums used to more concisely track
     * status bar parameters by type and allow for easier updating.
     */
    public enum StatusType {

        //General State Tracking
        NORMAL("Normal", Color.black, Color.white),
        PROCESSING("Processing", Color.white, Color.blue),
        CAUTION("Caution", Color.black, Color.yellow),
        TURNAROUND("Turn", Color.white, Color.blue),
        ERROR("Error", Color.black, Color.red),
        UNKNOWN("Unknown", Color.black, Color.white),

        //Bumper Specific State Tracking
        DEFER("Defer to US", Color.black, Color.yellow),
        ACTIVATED("Activated", Color.black, Color.yellow),
        CLEAR("Clear", Color.black, Color.white);

        private final String text;
        private final Color fgColor;
        private final Color bgColor;

        StatusType(String text, Color fgColor, Color bgColor) {
            this.text = text;
            this.fgColor = fgColor;
            this.bgColor = bgColor;
        }
    }
}
