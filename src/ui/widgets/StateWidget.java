package com.ui.widgets;

import com.Context;
import com.serial.Serial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 10-28-20
 * Description: Dashboard Widget child class used to display the
 * current state of a connected unit as described over serial communication.
 */
public class StateWidget extends UIWidget {
    //Constants
    protected static final int LINE_WIDTH = 14;
    protected static final String FAILSAFE_MSG = "A radio failsafe has occured."
            + "Please check unit power levels/replace batteries.";

    //Color Defaults
    protected static final Color DEF_FONT_COLOR = Color.decode("0xEA8300");
    protected static final Color DEF_BACK_COLOR_A = Color.decode("0xDFDFDF");
    protected static final Color DEF_BACK_COLOR_B = Color.decode("0xEEEEEE");
    protected StatusBarWidget statusBar;
    //State Tracking Variables
    protected byte lastDriveState = 0xF;
    private JFrame infoFrame;
    /**
     * Generates an information panel on click describing any warnings, errors,
     * and details of the current state.
     */
    private final MouseAdapter stateDetailsMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent me) {

            if (infoFrame != null && infoFrame.isVisible()) {
                infoFrame.toFront();
            }
            else {
                infoFrame = new JFrame("state info");
                JOptionPane.showMessageDialog(infoFrame, "Click Info String Here");
            }
        }
    };
    //State Readouts Labels
    private JPanel apmPanel;
    private JLabel apmLabel;
    private JPanel drivePanel;
    private JLabel driveLabel;
    private JPanel autoPanel;
    private JLabel autoLabel;
    private JPanel flagPanel;
    private JLabel flagLabel;

    /**
     * Class constructor
     *
     * @param ctx - The application context
     */
    public StateWidget(Context ctx) {
        super(ctx, "States");
        initPanel();
    }

    /**
     * Construct and place the visual layout elements of the widget. Sets
     * properties of the elements such as size and format restrictions,
     * dimensional spacing, and border insets.
     */
    private void initPanel() {

        statusBar = new StatusBarWidget(context);
        this.add(statusBar);

        Font font = context.theme.text.deriveFont(FONT_SIZE);
        Dimension labelSize = new Dimension(100, 20);

        //Configure state labels
        apmLabel = new JLabel("Apm:--");
        apmLabel.setFont(font);
        apmLabel.setForeground(DEF_FONT_COLOR);
        apmLabel.setBackground(DEF_BACK_COLOR_A);
        apmLabel.setOpaque(true);

        driveLabel = new JLabel("Drv:--");
        driveLabel.setFont(font);
        driveLabel.setForeground(DEF_FONT_COLOR);
        driveLabel.setBackground(DEF_BACK_COLOR_B);
        driveLabel.setOpaque(true);

        autoLabel = new JLabel("Aut:--");
        autoLabel.setFont(font);
        autoLabel.setForeground(DEF_FONT_COLOR);
        autoLabel.setBackground(DEF_BACK_COLOR_A);
        autoLabel.setOpaque(true);

        flagLabel = new JLabel("Flg:None");
        flagLabel.setFont(font);
        flagLabel.setForeground(DEF_FONT_COLOR);
        flagLabel.setBackground(DEF_BACK_COLOR_B);
        flagLabel.setOpaque(true);

        JComponent[] labelList = new JComponent[]{
                apmLabel, driveLabel, autoLabel, flagLabel //, gpsLabel
        };

        for (JComponent jc : labelList) {
            jc.setMaximumSize(labelSize);
        }

        //Configure state panels
        ArrayList<JPanel> statePanels = new ArrayList<JPanel>();
        apmPanel = new JPanel();
        apmPanel.setBorder(insets);
        apmPanel.setLayout(new BoxLayout(apmPanel, BoxLayout.LINE_AXIS));
        apmPanel.setPreferredSize(labelSize);
        apmPanel.setOpaque(true);
        apmPanel.add(apmLabel);
        statePanels.add(apmPanel);

        drivePanel = new JPanel();
        drivePanel.setBorder(insets);
        drivePanel.setLayout(new BoxLayout(drivePanel, BoxLayout.LINE_AXIS));
        drivePanel.setPreferredSize(labelSize);
        drivePanel.setOpaque(true);
        drivePanel.add(driveLabel);
        statePanels.add(drivePanel);

        autoPanel = new JPanel();
        autoPanel.setBorder(insets);
        autoPanel.setLayout(new BoxLayout(autoPanel, BoxLayout.LINE_AXIS));
        autoPanel.setPreferredSize(labelSize);
        autoPanel.setOpaque(true);
        autoPanel.add(autoLabel);
        statePanels.add(autoPanel);

        flagPanel = new JPanel();
        flagPanel.setBorder(insets);
        flagPanel.setLayout(new BoxLayout(flagPanel, BoxLayout.LINE_AXIS));
        flagPanel.setPreferredSize(labelSize);
        flagPanel.setOpaque(true);
        flagPanel.add(flagLabel);
        statePanels.add(flagPanel);

        //Add panels to widget
        for (JPanel panel : statePanels) {
            this.add(panel);
        }
    }

    /**
     * Updates the internal state of the widget using byte data received from
     * serial communications with a device.
     *
     * @param state    - The main state type being updated
     * @param substate - The sub state variation to be updated to
     */
    public void update(byte state, byte substate) {
        switch (state) {
            case Serial.APM_STATE:
                setAPMState(substate);
                break;
            case Serial.DRIVE_STATE:
                setDriveState(substate);
                break;
            case Serial.AUTO_STATE:
                setAutoState(substate);
                break;
            case Serial.AUTO_FLAGS:
                setFlagState(substate);
                break;
            case Serial.GPS_STATE:
                //(CP - Depricated as of 7-15-21, May be utilized for
                //other functionality at another time so this case is left
                //in place for now. If this turns out to not be the case,
                //then this should be removed.
                break;
            default:
                serialLog.warning("State Widget: Unrecognized incoming State");
        }
    }

    /**
     * Sets the current APM state.
     *
     * @param substate - The sub state variation to be set
     */
    private void setAPMState(byte substate) {
        int finalWidth;
        String fmt;
        String fmtStr = "Apm:%s";

        switch (substate) {
            case Serial.APM_STATE_INIT:
                fmt = String.format(fmtStr, "Init");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
                break;
            case Serial.APM_STATE_SELF_TEST:
                fmt = String.format(fmtStr, "Self Test");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
                break;
            case Serial.APM_STATE_DRIVE:
                fmt = String.format(fmtStr, "Driving");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
                break;
            default:
                fmt = String.format(fmtStr, "Unknown");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
        }
        apmLabel.setText(fmt.substring(0, finalWidth));
    }

    /**
     * Sets the current Drive state.
     *
     * @param substate - The sub state variation to be set
     */
    private void setDriveState(byte substate) {
        int finalWidth;
        String fmt;
        String fmtStr = "Drv:%s";

        switch (substate) {
            case Serial.DRIVE_STATE_STOP:
                fmt = String.format(fmtStr, "Stopped");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);

                //If the mission finished, update the Start/Stop Mission button.
                if (context.dash.mapPanel.waypointPanel.getIsMoving()
                        && lastDriveState != Serial.DRIVE_STATE_STOP) {
                    context.dash.mapPanel.waypointPanel.missionButton.doClick();
                }

                break;
            case Serial.DRIVE_STATE_AUTO:
                fmt = String.format(fmtStr, "Auto");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);

                break;
            case Serial.DRIVE_STATE_RADIO:
                fmt = String.format(fmtStr, "Manual");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);

                break;
            case Serial.DRIVE_STATE_LOW_VOLTAGE_STOP:
            case Serial.DRIVE_STATE_LOW_VOLTAGE_RESTART:
                fmt = String.format(fmtStr, "Low Vol.");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);

                break;

            case Serial.DRIVE_STATE_RADIO_FAILSAFE:
                fmt = String.format(fmtStr, "Failsafe");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);

                break;

            default:
                fmt = String.format(fmtStr, "Unknown");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
        }
        driveLabel.setText(fmt.substring(0, finalWidth));
        lastDriveState = substate;
    }

    /**
     * Sets the current Auto state.
     *
     * @param substate - The sub state variation to be set
     */
    private void setAutoState(byte substate) {
        int finalWidth;
        String fmt;
        String fmtStr = "Aut:%s";

        switch (substate) {
            case Serial.AUTO_STATE_FULL:
                fmt = String.format(fmtStr, "Full");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
                break;
            case Serial.AUTO_STATE_AVOID:
                fmt = String.format(fmtStr, "Avoid");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);

                if (statusBar.getState() != StatusBarWidget.StatusType.CAUTION) {
                    statusBar.update(StatusBarWidget.StatusType.CAUTION);
                }

                break;
            case Serial.AUTO_STATE_STALLED:
                fmt = String.format(fmtStr, "Stalled");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
                break;
            default:
                fmt = String.format(fmtStr, "Unknown");
                finalWidth = Math.min(fmt.length(), LINE_WIDTH);
        }
        autoLabel.setText(fmt.substring(0, finalWidth));
    }

    /**
     * Sets the current state flag if any.
     *
     * @param substate - The flag type to be set
     */
    private void setFlagState(byte substate) {
        int finalWidth;
        String fmt;
        String fmtStr = "Flg:%s";

        boolean caution = (substate & Serial.AUTO_STATE_FLAGS_CAUTION) > 0;
        boolean approach = (substate & Serial.AUTO_STATE_FLAGS_APPROACH) > 0;
        boolean turn = (substate & Serial.AUTO_STATE_FLAGS_TURNAROUND) > 0;

        if (caution && approach) {
            fmt = String.format(fmtStr, "App. & Caut.");
            finalWidth = Math.min(fmt.length(), LINE_WIDTH);
            statusBar.update(StatusBarWidget.StatusType.CAUTION);
        }
        else if (approach) {
            fmt = String.format(fmtStr, "Approach");
            finalWidth = Math.min(fmt.length(), LINE_WIDTH);
            statusBar.update(StatusBarWidget.StatusType.PROCESSING);
        }
        else if (caution) {
            fmt = String.format(fmtStr, "Caution");
            finalWidth = Math.min(fmt.length(), LINE_WIDTH);
            statusBar.update(StatusBarWidget.StatusType.CAUTION);
        }
        else if (turn) {
            fmt = String.format(fmtStr, "Turn");
            finalWidth = Math.min(fmt.length(), LINE_WIDTH);
            statusBar.update(StatusBarWidget.StatusType.TURNAROUND);
        }
        else {
            fmt = String.format(fmtStr, "None");
            finalWidth = Math.min(fmt.length(), LINE_WIDTH);
            statusBar.update(StatusBarWidget.StatusType.NORMAL);
        }
        flagLabel.setText(fmt.substring(0, finalWidth));
    }
}
