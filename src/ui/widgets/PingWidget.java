package com.ui.widgets;

import com.Context;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 12-2-20
 * Description: Dashboard Widget child class used to display a units
 * ping sensor data.
 */
public class PingWidget extends UIWidget {

    //Constants
    protected static final int NUM_SENSORS = 5;
    protected static final int UPDATE_DELAY_MS = 500;
    protected static final int[] WARN_LEVELS = {2000, 3200, 6000, 3200, 2000};
    protected static final int[] BLOCK_LEVELS = {1000, 1600, 3000, 1600, 1000};

    //Ping Sensor Meters
    protected HashMap<Integer, ArrayList<JPanel>> sensorMeters;

    //Sensor Values
    protected int[] curSensorVals;

    //Meter Update Frequency Timer
    protected javax.swing.Timer meterUpdateTimer;

    //Sensor Image Panel
    protected JPanel sensorOuterPanel;
    /**
     * Timer event responsible for controlling the rate at which the sensor
     * meters update. see constant value UPDATE_DELAY_MS for the currently
     * configured timer delay.
     */
    ActionListener meterUpdateAction = event -> updateMeters();

    /**
     * Class Constructor
     * Generates the initial layout for the widget, loading graphics and placing
     * them within a defined GridBagLayout.
     *
     * @param ctx - The application context
     */
    public PingWidget(Context ctx) {
        super(ctx, "Ultrasound");

        curSensorVals = new int[]{0, 0, 0, 0, 0};

        sensorOuterPanel = new JPanel();
        sensorOuterPanel.setMinimumSize(new Dimension(100, 60));
        sensorOuterPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 0, 2, 0);
        constraints.anchor = GridBagConstraints.CENTER;

        sensorMeters = new HashMap<>();
        for (int i = 0; i < NUM_SENSORS; i++) {
            sensorMeters.put(i, buildMeterSet());
        }

        //Zero out meters on init
        constraints.gridx = 0;
        constraints.gridy = 0;
        sensorOuterPanel.add(sensorMeters.get(0).get(0), constraints);
        constraints.gridx = 1;
        sensorOuterPanel.add(sensorMeters.get(1).get(0), constraints);
        constraints.gridx = 2;
        sensorOuterPanel.add(sensorMeters.get(2).get(0), constraints);
        constraints.gridx = 3;
        sensorOuterPanel.add(sensorMeters.get(3).get(0), constraints);
        constraints.gridx = 4;
        sensorOuterPanel.add(sensorMeters.get(4).get(0), constraints);

        this.add(sensorOuterPanel);

        meterUpdateTimer = new javax.swing.Timer(UPDATE_DELAY_MS, meterUpdateAction);
        meterUpdateTimer.start();
    }

    /**
     * Constructs a complete set of meter graphics to be used for a
     * ping sensor.
     *
     * @return - The JPanel containing the meter set
     */
    public ArrayList<JPanel> buildMeterSet() {
        ArrayList<JPanel> meterSet;
        JPanel panel;

        meterSet = new ArrayList<>();

        //Level: None
        panel = new JPanel();
        panel.add(new JLabel(new ImageIcon(context.theme.pingSpacer)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingSpacer)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingSpacer)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        meterSet.add(panel);

        //Level: Low
        panel = new JPanel();
        panel.add(new JLabel(new ImageIcon(context.theme.pingSpacer)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingSpacer)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingGreen)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        meterSet.add(panel);

        //Level: Medium
        panel = new JPanel();
        panel.add(new JLabel(new ImageIcon(context.theme.pingSpacer)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingYellow)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingGreen)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        meterSet.add(panel);

        //Level: High
        panel = new JPanel();
        panel.add(new JLabel(new ImageIcon(context.theme.pingRed)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingYellow)));
        panel.add(new JLabel(new ImageIcon(context.theme.pingGreen)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        meterSet.add(panel);

        return meterSet;
    }

    /**
     * Updates a tracked sensor value and calls for a new visual meter display for
     * all sensors.
     *
     * @param index - The sensor number
     * @param data  - The current value from this sensor.
     */
    public void update(int index, int data) {

        //Return on out of bounds
        if (index > (NUM_SENSORS - 1) || index < 0) {
            System.err.println("Sensor index out of bounds");
            return;
        }

        curSensorVals[index] = data;
    }

    /**
     * Updates the visual meter display for all sensor meters based on the
     * currently detected values as they compare against pre-calibrated
     * warning levels.
     */
    protected void updateMeters() {
        GridBagConstraints constraints = new GridBagConstraints();

        sensorOuterPanel.removeAll();

        //Meter One
        constraints.gridy = 0;
        constraints.gridx = 0;
        //Warning High
        if (curSensorVals[0] <= BLOCK_LEVELS[0]) {
            sensorOuterPanel.add(sensorMeters.get(0).get(3), constraints);
        } //Warning Medium
        else if (curSensorVals[0] <= WARN_LEVELS[0]) {
            sensorOuterPanel.add(sensorMeters.get(0).get(2), constraints);
        } //Warning Low
        else {
            sensorOuterPanel.add(sensorMeters.get(0).get(1), constraints);
        }

        //Meter Two
        constraints.gridx = 1;
        //Warning High
        if (curSensorVals[1] <= BLOCK_LEVELS[1]) {
            sensorOuterPanel.add(sensorMeters.get(1).get(3), constraints);
        } //Warning Medium
        else if (curSensorVals[1] <= WARN_LEVELS[1]) {
            sensorOuterPanel.add(sensorMeters.get(1).get(2), constraints);
        } //Warning Low
        else {
            sensorOuterPanel.add(sensorMeters.get(1).get(1), constraints);
        }

        //Meter Three
        constraints.gridx = 2;
        //Warning High
        if (curSensorVals[2] <= BLOCK_LEVELS[2]) {
            sensorOuterPanel.add(sensorMeters.get(2).get(3), constraints);
        } //Warning Medium
        else if (curSensorVals[2] <= WARN_LEVELS[2]) {
            sensorOuterPanel.add(sensorMeters.get(2).get(2), constraints);
        } //Warning Low
        else {
            sensorOuterPanel.add(sensorMeters.get(2).get(1), constraints);
        }

        //Meter Four
        constraints.gridx = 3;
        //Warning High
        if (curSensorVals[3] <= BLOCK_LEVELS[3]) {
            sensorOuterPanel.add(sensorMeters.get(3).get(3), constraints);
        } //Warning Medium
        else if (curSensorVals[3] <= WARN_LEVELS[3]) {
            sensorOuterPanel.add(sensorMeters.get(3).get(2), constraints);
        } //Warning Low
        else {
            sensorOuterPanel.add(sensorMeters.get(3).get(1), constraints);
        }

        //Meter Five
        constraints.gridx = 4;
        //Warning High
        if (curSensorVals[4] <= BLOCK_LEVELS[4]) {
            sensorOuterPanel.add(sensorMeters.get(4).get(3), constraints);
        } //Warning Medium
        else if (curSensorVals[4] <= WARN_LEVELS[4]) {
            sensorOuterPanel.add(sensorMeters.get(4).get(2), constraints);
        } //Warning Low
        else {
            sensorOuterPanel.add(sensorMeters.get(4).get(1), constraints);
        } //Warning None
    }

    /**
     * Gets the current value for a sensor by index.
     *
     * @param index - The index of the sensor
     * @return - the current value stored for that sensor
     */
    public int getSensorValue(int index) {
        return curSensorVals[index];
    }

    /**
     * Gets the number of sensors tracked by this widget.
     *
     * @return - the number of sensors
     */
    public int getNumSensors() {
        return NUM_SENSORS;
    }
}
