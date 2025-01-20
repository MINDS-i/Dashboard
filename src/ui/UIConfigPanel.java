package com.ui;

import com.Context;
import com.map.Dot;
import com.map.MapPanel;
import com.map.WaypointList;
import com.map.command.CommandManager;
import com.map.geofence.WaypointGeofence;
import com.serial.SerialSendManager;
import com.util.ACLIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.File;


/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 09-2020
 * Description: UI Panel responsible for providing UI focused options to the user.
 * Allows for toggling air/ground mode, and saving the current Home location to persistent
 * settings.
 */
public class UIConfigPanel extends JPanel {

    //Constants
    private static final int DEF_TEXT_FIELD_WIDTH = 8;
    private static final int DEF_BUTTON_WIDTH = 200;
    private static final int DEF_BUTTON_HEIGHT = 30;
    private static final String DEF_HOME_COORD = "0.0";

    private final JTextField lngField;
    private final JTextField latField;
    private final JTextField fenceField;
    private final JCheckBox bumperCheckBox;
    //State and Reference vars
    private final Context context;
    private final MapPanel map;
    private final CommandManager commandManager;

    private boolean bumperIsEnabled;

    private static final Logger logger = LoggerFactory.getLogger(UIConfigPanel.class);

    /**
     * Class constructor
     *
     * @param cxt       - the application context
     * @param isWindows - boolean check to see if the application is running in windows
     */
    public UIConfigPanel(Context cxt, MapPanel mapPanel, boolean isWindows) {
        this.context = cxt;
        this.map = mapPanel;
        this.commandManager = CommandManager.getInstance();

        this.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 5, 0, 5);

        //Unit type toggle button(Rover/Copter)
        /*
         * Action used to toggle the user interface between Air and Ground mode.
         * Requires a program restart for the setting to take effect.
         */
        Action toggleLocaleAction = new AbstractAction() {
            {
                String text = "Toggle ground/air mode";
                putValue(Action.NAME, text);
            }

            public void actionPerformed(ActionEvent e) {
                context.toggleLocale();
                JFrame mf = new JFrame("message");
                JOptionPane.showMessageDialog(mf,
                        "Changes will take effect next launch");
            }
        };
        //UI Componenets
        JButton toggleButton = new JButton(toggleLocaleAction);
        toggleButton.setPreferredSize(
                new Dimension(DEF_BUTTON_WIDTH, DEF_BUTTON_HEIGHT));
        constraints.gridx = 0;
        constraints.gridy = 0;
        this.add(toggleButton, constraints);

        //Driver installation button (Deprecated - Windows Only)
        //Note - CP - 8-10-21: There is an updated version of the driver,
        //and also an automatic installation of this driver from the
        //installer executable now. This should be removed at some point.
        if (isWindows) {
            /*
             * Action used to install the Required Radio Telemtry drivers.
             * (Deprecated) - This is now handled by the program installer.
             */
            Action driverExecAction = new AbstractAction() {
                {
                    String text = "Launch driver installer";
                    putValue(Action.NAME, text);
                }

                public void actionPerformed(ActionEvent e) {
                    String[] cmd = {"RadioDiversv2.12.06WHQL_Centified.exe"};
                    try {
                        Process p = Runtime.getRuntime().exec(cmd);
                    }
                    catch (Exception ex) {
                        logger.error("Error installing radio drivers", ex);
                    }
                }
            };
            JButton driverButton = new JButton(driverExecAction);
            driverButton.setPreferredSize(
                    new Dimension(DEF_BUTTON_WIDTH, DEF_BUTTON_HEIGHT));
            constraints.gridx = 0;
            constraints.gridy = 1;
            this.add(driverButton, constraints);
        }

        //Sketch upload button
        /*
         * Action used to open a FileChooser dialog, and
         * select and upload an Arduino sketch to the APM.
         */
        //Install avr core used by APM
        //Build list of available/connected boards
        //Parse core and port info
        //Compile selected sketch
        //Upload selected sketch
        Action uploadSketchAction = new AbstractAction() {
            {
                String text = "Upload arduino sketch";
                putValue(Action.NAME, text);
            }

            public void actionPerformed(ActionEvent e) {
                File selectedFile = null;
                JFileChooser fileChooser = new JFileChooser();
                FileFilter filter = new FileNameExtensionFilter(
                        "Arduino Sketch (*.ino)", "ino");

                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter());
                fileChooser.addChoosableFileFilter(filter);

                int retVal = fileChooser.showOpenDialog(UIConfigPanel.this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                }

                if (selectedFile == null) {
                    System.err.println(
                            "UIConfigPanel - Upload error: No file was selected.");
                    return;
                }

                boolean result = false;

                //Install avr core used by APM
                result = ACLIManager.getInstance().execute(
                        ACLIManager.ACLICommand.INSTALL_AVR_CORE);
                if (!result) {
                    JOptionPane.showMessageDialog(UIConfigPanel.this,
                            ACLIManager.getInstance().getErrorStr(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.err.println("Successful Core Install");


                //Build list of available/connected boards
                result = ACLIManager.getInstance().execute(
                        ACLIManager.ACLICommand.GENERATE_BOARD_LIST);
                if (!result) {
                    JOptionPane.showMessageDialog(UIConfigPanel.this,
                            ACLIManager.getInstance().getErrorStr(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.err.println("Successful Board List Generation");

                //Parse core and port info
                result = ACLIManager.getInstance().execute(
                        ACLIManager.ACLICommand.PARSE_BOARD_INFO);
                if (!result) {
                    JOptionPane.showMessageDialog(UIConfigPanel.this,
                            ACLIManager.getInstance().getErrorStr(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.err.println("Successful Board List Parsing");

                //Compile selected sketch
                result = ACLIManager.getInstance().execute(
                        ACLIManager.ACLICommand.COMPILE_SKETCH,
                        selectedFile.getAbsolutePath());
                if (!result) {
                    JOptionPane.showMessageDialog(UIConfigPanel.this,
                            ACLIManager.getInstance().getErrorStr(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.err.println("Successful Sketch Compile");

                //Upload selected sketch
                result = ACLIManager.getInstance().execute(
                        ACLIManager.ACLICommand.UPLOAD_SKETCH,
                        selectedFile.getAbsolutePath());
                if (!result) {
                    JOptionPane.showMessageDialog(UIConfigPanel.this,
                            ACLIManager.getInstance().getErrorStr(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.err.println("Successful Sketch Upload");

                JOptionPane.showMessageDialog(UIConfigPanel.this,
                        "Sketch uploaded successfully.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        JButton sketchUploadButton = new JButton(uploadSketchAction);
        sketchUploadButton.setPreferredSize(
                new Dimension(DEF_BUTTON_WIDTH, DEF_BUTTON_HEIGHT));
        constraints.gridx = 0;
        constraints.gridy = 2;
        this.add(sketchUploadButton, constraints);

        //Home coordinates fields and set button
        Point2D homeCoords = context.getHomeProp();

        JLabel latLabel = new JLabel("Latitude:");
        constraints.gridx = 1;
        constraints.gridy = 0;
        this.add(latLabel, constraints);

        latField = new JTextField(DEF_TEXT_FIELD_WIDTH);
        constraints.gridx = 2;
        constraints.gridy = 0;
        this.add(latField, constraints);
        latField.setText(String.valueOf(homeCoords.getX()));

        JLabel lngLabel = new JLabel("Longitude:");
        constraints.gridx = 1;
        constraints.gridy = 1;
        this.add(lngLabel, constraints);

        lngField = new JTextField(DEF_TEXT_FIELD_WIDTH);
        constraints.gridx = 2;
        constraints.gridy = 1;
        this.add(lngField, constraints);
        lngField.setText(String.valueOf(homeCoords.getY()));

        /*
         * Action used to set the home longitude and latitude and save it to persistent
         * settings.
         */
        //If the input from the fields looks bad, use a default
        Action setHomeAction = new AbstractAction() {
            {
                String text = "Set Home";
                putValue(Action.NAME, text);
                putValue(Action.SHORT_DESCRIPTION, text);
            }

            public void actionPerformed(ActionEvent e) {
                String lat = latField.getText();
                String lng = lngField.getText();

                //If the input from the fields looks bad, use a default
                if (lat.isEmpty()) {
                    lat = DEF_HOME_COORD;
                }
                if (lng.isEmpty()) {
                    lng = DEF_HOME_COORD;
                }

                WaypointList list = context.getWaypointList();
                Dot location = list.getHome();
                location.setLatitude(Double.parseDouble(lat));
                location.setLongitude(Double.parseDouble(lng));
                list.setHome(location);

                context.setHomeProp(latField.getText(),
                        lngField.getText());

                JFrame mf = new JFrame("message");
                JOptionPane.showMessageDialog(mf,
                        "Home location has been set.");
            }
        };
        JButton setHomeButton = new JButton(setHomeAction);
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.add(setHomeButton, constraints);

        //Bumper toggle checkbox
        bumperCheckBox = new JCheckBox("Enable Bumper");
        bumperCheckBox.setSelected(false);
        constraints.gridx = 3;
        constraints.gridy = 0;
        this.add(bumperCheckBox, constraints);
        toggleBumper(context.dash.bumperWidget.isEnabled());

        //Geofence label and radius input field
        JLabel fenceLabel = new JLabel("Geofence Radius (Ft):");
        constraints.gridx = 3;
        constraints.gridy = 1;
        this.add(fenceLabel, constraints);

        fenceField = new JTextField(DEF_TEXT_FIELD_WIDTH);
        constraints.gridx = 5;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        this.add(fenceField, constraints);
        fenceField.setText(String.valueOf(
                commandManager.getGeofence().getRadius()));

        //Settings apply button (for Checkboxes/Radio Buttons)
        /*
         * Action used to update settings based on selectable radio buttons and
         * check boxes.
         */
        //Bumper enable/disable toggle
        //Geofence radius update
        //Don't allow a radius smaller than the minimum default
        Action setSettingsAction = new AbstractAction() {
            {
                String text = "Apply Settings";
                putValue(Action.NAME, text);
            }

            public void actionPerformed(ActionEvent e) {

                //Bumper enable/disable toggle
                if (bumperIsEnabled && !bumperCheckBox.isSelected()) {
                    toggleBumper(false);
                }
                else if (!bumperIsEnabled && bumperCheckBox.isSelected()) {
                    toggleBumper(true);
                }

                //Geofence radius update
                double radius = Double.parseDouble(fenceField.getText());

                //Don't allow a radius smaller than the minimum default
                if (radius < WaypointGeofence.MIN_RADIUS_FT) {
                    radius = WaypointGeofence.MIN_RADIUS_FT;
                }

                commandManager.getGeofence().updateRadius(radius);
                map.repaint();
            }
        };
        JButton applySettingsButton = new JButton(setSettingsAction);
        constraints.gridx = 3;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.add(applySettingsButton, constraints);

    }

    private void toggleBumper(boolean isEnabled) {
        bumperIsEnabled = isEnabled;
        bumperCheckBox.setSelected(bumperIsEnabled);
        SerialSendManager.getInstance().toggleBumper(bumperIsEnabled);
        context.dash.bumperWidget.setEnabled(bumperIsEnabled);
    }
}
