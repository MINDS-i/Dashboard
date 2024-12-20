package com.ui.widgets;

import com.Context;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Logger;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 11-20-2020
 * Description: Base class from which a UI widget derives its
 * general functionality.
 */
public class UIWidget extends JPanel {
    //Constants
    protected static final float FONT_SIZE = 14.0f;
    protected static final int BORDER_TOP = 0;
    protected static final int BORDER_BOT = 0;
    protected static final int BORDER_LFT = 0;
    protected static final int BORDER_RHT = 0;
    protected static final int LABEL_HEIGHT = 25;
    protected static final int LABEL_WIDTH = 115;
    //Logging support
    protected final Logger serialLog = Logger.getLogger("d.serial");
    //Standard Variables
    protected Context context;
    protected Border insets;
    //Title Panel and Label
    protected JPanel titlePanel;
    protected JLabel titleLabel;

    /**
     * Class constructor
     *
     * @param ctx   - The application context
     * @param title - the title string for the widget
     */
    public UIWidget(Context ctx, String title) {
        context = ctx;
        //Param order: Top, Left, Bottom, Right
        insets = new EmptyBorder(BORDER_TOP, BORDER_LFT, BORDER_BOT, BORDER_RHT);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setOpaque(false);

        Font font = context.theme.text.deriveFont(FONT_SIZE);
        Dimension labelSize = new Dimension(LABEL_WIDTH, LABEL_HEIGHT);

        titlePanel = new JPanel();
        titlePanel.setOpaque(true);
        titlePanel.setPreferredSize(labelSize);
        titlePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        titlePanel.setBackground(Color.decode("0xFAAC2D"));

        titleLabel = new JLabel(title);
        titleLabel.setOpaque(false);
        titleLabel.setFont(font);

        titlePanel.add(titleLabel);
        add(titlePanel);
    }

    /**
     * Updates the padding insets for the widget, overriding the default.
     *
     * @param top
     * @param left
     * @param bottom
     * @param right
     */
    public void setInsets(int top, int left, int bottom, int right) {
        insets = new EmptyBorder(top, left, bottom, right);
        this.setBorder(insets);
    }

    /**
     * Updates the widget title, overriding the default.
     *
     * @param title - The string to update the title to.
     */
    public void updateTitle(String title) {
        titleLabel.setText(title);
    }
}
