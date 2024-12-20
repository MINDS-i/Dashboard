package com.map;

import com.Context;
import com.graph.Graph;
import com.map.command.*;
import com.serial.Serial;
import com.serial.SerialSendManager;
import com.ui.LogViewer;
import com.ui.SystemConfigWindow;
import com.ui.Theme;
import com.ui.ninePatch.NinePatchPanel;
import com.ui.telemetry.TelemetryDataWindow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;

import static com.map.WaypointList.ExtendedWaypoint;
import static com.map.WaypointList.WaypointListener;

public class WaypointPanel extends NinePatchPanel {
    //Constants
    protected static final int MOVE_STEP = 32;
    protected static final int BDR_SIZE_TB = 20;
    protected static final int BDR_SIZE_LR = 15;
    protected static final String NO_WAYPOINT_MSG = "N / A";
    //Logging support
    protected final Logger serialLog = Logger.getLogger("d.serial");
    private final Context context;
    private final MapPanel map;
    private final WaypointList waypoints;
    private final Action logPanelAction = new AbstractAction() {
        final LogViewer lv;
        final Logger log;

        {
            lv = new LogViewer();
            log = Logger.getLogger("d");
            log.addHandler(lv.getHandler());
            putValue(Action.NAME, "Event Log");
        }

        public void actionPerformed(ActionEvent e) {
            lv.setVisible(true);
        }
    };
    private final Action zoomInAction = new AbstractAction() {
        {
            String text = "+";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            //Do nothing, input handled by zoomInMouseAdapter
        }
    };
    /**
     * The zoom action performed when a zoomIn Timer is triggered
     */
    private final Action zoomInTimerAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            map.zoomIn(new Point(map.getWidth() / 2, map.getHeight() / 2));
        }
    };
    private final Action zoomOutAction = new AbstractAction() {
        {
            String text = "-";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            //Do nothing, input handled by zoomOutMouseAdapter
        }
    };
    /**
     * The zoom action performed when a zoomOut Timer is triggered
     */
    private final Action zoomOutTimerAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            map.zoomOut(new Point(map.getWidth() / 2, map.getHeight() / 2));
        }
    };
    /**
     * Zooms the map to the closest level.
     */
    private final Action zoomFullAction = new AbstractAction() {
        {
            String text = "Full";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            map.zoomFull(new Point(map.getWidth() / 2, map.getHeight() / 2));
        }
    };
    private final Action toggleLooping = new AbstractAction() {
        {
            String text = "Looping On";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            waypoints.setLooped(!waypoints.getIsLooped());
            putValue(Action.NAME,
                    (waypoints.getIsLooped()) ? "Looping Off" : "Looping On");
        }
    };
    /**
     * Action used to write the current waypoint list out to a
     * GPX formatted xml file.
     */
    private final Action saveWaypoints = new AbstractAction() {
        {
            String text = "Save";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            WaypointCommand command;
            String fileName;

            fileName = getFileName("Choose file to save");
            command = new WaypointCommandParse(
                    waypoints, context, fileName,
                    WaypointCommandParse.ParseType.XML,
                    WaypointCommandParse.ParseMode.WRITE);

            CommandManager.getInstance().process(command);
        }
    };
    /**
     * Action used to read a waypoint list from a GPX
     * formatted xml file.
     */
    private final Action loadWaypoints = new AbstractAction() {
        {
            String text = "Load";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            WaypointCommand command;
            String fileName;

            fileName = getFileName("Choose file to open");
            command = new WaypointCommandParse(
                    waypoints, context, fileName,
                    WaypointCommandParse.ParseType.XML,
                    WaypointCommandParse.ParseMode.READ);

            CommandManager.getInstance().process(command);
        }
    };
    private final Action reTargetRover = new AbstractAction() {
        {
            String text = "Set Target";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            WaypointCommand command = new WaypointCommandTarget(waypoints);
            CommandManager.getInstance().process(command);
        }
    };
    /**
     * Action responsible for triggering an undo operation on the
     * last command performed.
     */
    private final Action undoCommandAction = new AbstractAction() {
        {
            String text = "Undo";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            CommandManager.getInstance().undo();
        }
    };
    /**
     * Action responsible for triggering a redo action on the
     * last command undone.
     */
    private final Action redoCommandAction = new AbstractAction() {
        {
            String text = "Redo";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            CommandManager.getInstance().redo();
        }
    };
    private final Action setHomeCommandAction = new AbstractAction() {
        {
            String text = "Set Home";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            map.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            map.roverPath.setOpMode(RoverPath.OpMode.SET_HOME);
            serialLog.warning("SET HOME - Please select a home point.");
        }
    };
    private final Action clearWaypointsAction = new AbstractAction() {
        {
            String text = "Clear Waypoints";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            WaypointCommand command = new WaypointCommandClear(waypoints, context);
            CommandManager.getInstance().process(command);
        }
    };
    private final Action buildGraph = new AbstractAction() {
        {
            String text = "Graph";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            Graph graph = new Graph(context.getTelemetryDataSources(), false);
            graph.setPreferredSize(new Dimension(500, 300));
            JFrame gFrame = new JFrame("Telemetry Graph");
            gFrame.add(graph);
            gFrame.pack();
            gFrame.setVisible(true);
        }
    };
    //Panel Buttons
    public JButton tileButton;
    public JButton dataPanel;
    public JButton graphButton;
    public JButton reTarget;
    public JButton looping;
    public JButton config;
    public JButton logPanelButton;
    //Map Zoom Options
    public JButton zoomInButton;
    public JButton zoomOutButton;
    public JButton zoomFullButton;
    public JButton setHomeButton;
    //Waypoint Options
    public JButton clearWaypoints;
    public JButton enterButton;
    public JButton undoButton;
    public JButton redoButton;
    public JButton saveButton;
    public JButton loadButton;
    public JButton missionButton;
    //Text Fields
    TelemField latitude;
    TelemField longitude;
    TelemField altitude;
    private final Action interpretLocationAction = new AbstractAction() {
        {
            String text = "Enter";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {
            interpretLocationEntry();
        }
    };
    JLabel waypointIndexDisplay;
    private javax.swing.Timer zoomTimer;
    /**
     * Starts a periodic timer while the mouse button is held which
     * triggers repeated zoom in actions. The timer is stopped on release.
     */
    private final MouseAdapter zoomInMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent me) {
            map.zoomIn(new Point(map.getWidth() / 2, map.getHeight() / 2));

            if (zoomTimer == null) {
                zoomTimer = new javax.swing.Timer(250, zoomInTimerAction);
                zoomTimer.start();
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (zoomTimer != null) {
                zoomTimer.stop();
                zoomTimer = null;
            }
        }
    };
    /**
     * Starts a periodic timer while the mouse button is held which
     * triggers repeated zoom out actions. The timer is stopped on release.
     */
    private final MouseAdapter zoomOutMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent me) {
            map.zoomOut(new Point(map.getWidth() / 2, map.getHeight() / 2));

            if (zoomTimer == null) {
                zoomTimer = new javax.swing.Timer(250, zoomOutTimerAction);
                zoomTimer.start();
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (zoomTimer != null) {
                zoomTimer.stop();
                zoomTimer = null;
            }
        }
    };
    private TelemetryDataWindow telemetryDataWindow;
    private final Action openDataPanel = new AbstractAction() {
        {
            String text = "Telemetry";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {

            if (telemetryDataWindow != null
                    && telemetryDataWindow.getVisible()) {
                telemetryDataWindow.toFront();
                return;
            }

            telemetryDataWindow = new TelemetryDataWindow(context);
        }
    };
    private Action nextTileServer;
    /**
     * Action that starts or stops a unit for the currently active mission.
     */
    private boolean isUnitMoving = false;
    private final Action toggleMovement = new AbstractAction() {
        {
            String text = "Start Mission";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, "Start/Stop Mission");
        }

        public void actionPerformed(ActionEvent e) {
            if (isUnitMoving) {
                unlockWaypoints();
                context.dash.enableSerialPanel(true);

                SerialSendManager.getInstance().changeMovement(false);
                putValue(Action.NAME, "Start Mission");
                isUnitMoving = false;
            }
            else {
                lockWaypoints();
                context.dash.enableSerialPanel(false);

                SerialSendManager.getInstance().changeMovement(true);
                putValue(Action.NAME, "Stop Mission");
                isUnitMoving = true;
            }
        }
    };
    private final Action previousWaypoint = new AbstractAction() {
        {
            String text = "<";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            waypoints.setSelected(waypoints.getSelected() - 1);
            updateDisplay();
            map.repaint();
        }
    };
    private final Action nextWaypoint = new AbstractAction() {
        {
            String text = ">";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            waypoints.setSelected(waypoints.getSelected() + 1);
            updateDisplay();
            map.repaint();
        }
    };
    private SystemConfigWindow configWindow;
    private final Action openConfigWindow = new AbstractAction() {
        {
            String text = "Configuration";
            putValue(Action.NAME, text);
        }

        public void actionPerformed(ActionEvent e) {

            //If the window already exits, don't make a new one,
            //just move it to the front.
            if (configWindow != null && configWindow.getVisible()) {
                configWindow.toFront();
                return;
            }

            configWindow = new SystemConfigWindow(context, map);
        }
    };

    public WaypointPanel(Context cxt, MapPanel mapPanel) {
        super(cxt.theme.panelPatch);
        map = mapPanel;
        context = cxt;

        makeActions();

        waypoints = context.getWaypointList();
        setOpaque(false);
        LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(BDR_SIZE_TB, BDR_SIZE_LR,
                BDR_SIZE_TB, BDR_SIZE_LR));

        buildPanel();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                //do nothing here to prevent clicks from falling through
                //to the map panel in the background
            }
        });

        waypoints.addListener(new WaypointListener() {
            @Override
            public void unusedEvent() {
                updateDisplay();
            }
        });

        updateDisplay();
    }

    private void makeActions() {
        nextTileServer = new AbstractAction() {
            final java.util.List<String> serverLabels
                    = new LinkedList<>(map.tileServerNames());

            {
                updateLabel();
            }

            private void updateLabel() {
                String text = serverLabels.get(0);
                text = text.substring(0, 1).toUpperCase() + text.substring(1);
                putValue(Action.NAME, text);
            }

            public void actionPerformed(ActionEvent e) {
                // cycle current server to the back of the list
                String server = serverLabels.get(0);
                serverLabels.remove(0);
                serverLabels.add(server);
                // switch the map's server
                map.switchToServer(server);

                updateLabel();
                map.repaint();
            }
        };
    }

    /**
     * Update the selected dot text field displays
     */
    public void updateDisplay() {
        int selectedWaypoint = waypoints.getSelected();
        ExtendedWaypoint w = waypoints.get(selectedWaypoint);
        Dot dot = w.dot();

        String indexField = (selectedWaypoint + 1) + " / " + waypoints.size();
        // special dots that don't represent waypoints show names instead of index
        switch (w.type()) {
            case ROVER:
                indexField = "Robot";
                break;
            case HOME:
                indexField = "Home ";
                break;
        }
        waypointIndexDisplay.setText(indexField);

        latitude.update((float) dot.getLatitude());
        longitude.update((float) dot.getLongitude());
        altitude.update((float) fixedToDouble(dot.getAltitude()));

        latitude.setForeground(Color.BLACK);
        longitude.setForeground(Color.BLACK);
        altitude.setForeground(Color.BLACK);

        if (selectedWaypoint < 0) {
            latitude.setEditable(false);
            longitude.setEditable(false);
            altitude.setEditable(false);
        }
        else {
            if (!isUnitMoving) {
                latitude.setEditable(true);
                longitude.setEditable(true);
                altitude.setEditable(true);
            }
        }
    }

    private void buildPanel() {
        final Theme theme = context.theme;
        final Dimension space = new Dimension(0, 5);
        final Dimension buttonSize = new Dimension(140, 25);

        //Make all buttons
        tileButton = theme.makeButton(nextTileServer);
        dataPanel = theme.makeButton(openDataPanel);
        graphButton = theme.makeButton(buildGraph);
        reTarget = theme.makeButton(reTargetRover);
        looping = theme.makeButton(toggleLooping);
        config = theme.makeButton(openConfigWindow);
        logPanelButton = theme.makeButton(logPanelAction);

        //Map Zoom Options
        zoomInButton = theme.makeButton(zoomInAction);
        zoomInButton.addMouseListener(zoomInMouseAdapter);
        zoomOutButton = theme.makeButton(zoomOutAction);
        zoomOutButton.addMouseListener(zoomOutMouseAdapter);
        zoomFullButton = theme.makeButton(zoomFullAction);

        setHomeButton = theme.makeButton(setHomeCommandAction);

        //Waypoint Options
        clearWaypoints = theme.makeButton(clearWaypointsAction);

        enterButton = theme.makeButton(interpretLocationAction);
        undoButton = theme.makeButton(undoCommandAction);
        redoButton = theme.makeButton(redoCommandAction);
        saveButton = theme.makeButton(saveWaypoints);
        loadButton = theme.makeButton(loadWaypoints);
        missionButton = theme.makeButton(toggleMovement);

        JComponent[] format = new JComponent[]{
                tileButton, dataPanel, graphButton, reTarget, looping,
                config, logPanelButton, setHomeButton, clearWaypoints,
                enterButton, missionButton
        };

        for (JComponent jc : format) {
            jc.setAlignmentX(Component.CENTER_ALIGNMENT);
            jc.setMaximumSize(buttonSize);
        }

        //make zoom button panel
        JPanel zoom = new JPanel(new FlowLayout());
        zoom.setOpaque(false);
        zoom.add(zoomInButton);
        zoom.add(zoomFullButton);
        zoom.add(zoomOutButton);

        //add selectedWaypoint flow layout
        JPanel selector = new JPanel(new BorderLayout());
        selector.setOpaque(false);
        waypointIndexDisplay = new JLabel(NO_WAYPOINT_MSG);
        waypointIndexDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        selector.add(theme.makeButton(previousWaypoint), BorderLayout.LINE_START);
        selector.add(waypointIndexDisplay, BorderLayout.CENTER);
        selector.add(theme.makeButton(nextWaypoint), BorderLayout.LINE_END);

        //Make edit boxes
        class EditBoxSpec {
            public final JTextField ref;
            public final String label;

            public EditBoxSpec(JTextField ref, String label) {
                this.ref = ref;
                this.label = label;
            }
        }

        ArrayList<EditBoxSpec> editorBoxes = new ArrayList<>();
        latitude = new TelemField();
        longitude = new TelemField();
        altitude = new TelemField();
        editorBoxes.add(new EditBoxSpec(latitude, "Lat: "));
        editorBoxes.add(new EditBoxSpec(longitude, "Lng: "));
        editorBoxes.add(new EditBoxSpec(altitude, context.getResource("waypointExtra") + " "));

        ArrayList<JPanel> editorPanels = new ArrayList<>();
        for (EditBoxSpec box : editorBoxes) {
            //construct panel
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            panel.setOpaque(false);
            //add label box
            JLabel label = new JLabel(box.label);
            label.setFont(context.theme.text);
            panel.add(label);
            //add textField
            JTextField tf = box.ref;
            tf.setBorder(BorderFactory.createLineBorder(Color.gray));
            panel.add(tf);
            //Setup text field listener
            CoordinateListener listener = new CoordinateListener(tf, this);
            tf.getDocument().addDocumentListener(listener);
            tf.addActionListener(listener);
            //add to layout
            editorPanels.add(panel);
        }

        int COLS = 2;
        int ROWS = 2;
        int PADDING = 5;
        JPanel waypointOptions = new JPanel(
                new GridLayout(ROWS, COLS, PADDING, PADDING));
        waypointOptions.setOpaque(false);
        waypointOptions.add(undoButton);
        waypointOptions.add(redoButton);
        waypointOptions.add(saveButton);
        waypointOptions.add(loadButton);

        add(config);
        add(Box.createRigidArea(space));
        add(tileButton);
        add(zoom);
        add(dataPanel);
        add(Box.createRigidArea(space));
        add(graphButton);
        add(Box.createRigidArea(space));
        add(logPanelButton);
        add(Box.createRigidArea(space));
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(Box.createRigidArea(space));
        add(setHomeButton);
        add(Box.createRigidArea(space));
        add(clearWaypoints);
        add(Box.createRigidArea(space));
        add(selector);
        add(Box.createRigidArea(space));
        for (JPanel panel : editorPanels) {
            add(panel);
        }
        add(Box.createRigidArea(space));
        add(enterButton);
        add(Box.createRigidArea(space));
        add(waypointOptions);
        add(Box.createRigidArea(space));
        add(reTarget);
        add(Box.createRigidArea(space));
        add(looping);
        add(Box.createRigidArea(space));
        add(missionButton);
    }

    private double fixedToDouble(int i) {
        return ((double) (i & 0xffff)) / ((double) Serial.U16_FIXED_POINT);
    }

    private int doubleToFixed(double i) {
        return (int) (i * Serial.U16_FIXED_POINT);
    }

    public void interpretLocationEntry() {
        WaypointCommand command;

        try {
            int selectedWaypoint = waypoints.getSelected();

            if (selectedWaypoint < 0) // rover or home location selected
            {
                return;
            }

            //Grab waypoint info and create move command
            command = new WaypointCommandEdit(waypoints, selectedWaypoint);

            double newLatitude = Double.parseDouble(latitude.getText());
            double newLongitude = Double.parseDouble(longitude.getText());
            double tmpAltitude = Double.parseDouble(altitude.getText());

            int newAltitude = doubleToFixed(tmpAltitude);
            Point.Double newPosition = new Point.Double(newLongitude, newLatitude);

            if ((newAltitude & 0xffff) == newAltitude) {


                command.finalize(new Dot(newPosition, (short) newAltitude));
                CommandManager.getInstance().process(command);

                //set to display reconverted value
                altitude.setText(fixedToDouble(newAltitude) + "");
            }
            else {
                Dot newloc = waypoints.get(selectedWaypoint).dot();
                newloc.setLocation(newPosition);

                command.finalize(newloc);
                CommandManager.getInstance().process(command);
            }
        }
        catch (NumberFormatException e) {
        }
    }

    /**
     * Disables all WaypointPanel buttons and mouse events related
     * to the manipulation of waypoints.
     */
    private void lockWaypoints() {
        //Buttons
        setHomeButton.setEnabled(false);
        enterButton.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        saveButton.setEnabled(false);
        loadButton.setEnabled(false);
        reTarget.setEnabled(false);
        looping.setEnabled(false);

        //Manual Fields
        latitude.setEditable(false);
        longitude.setEditable(false);
        altitude.setEditable(false);

        //Mouse events
        map.enablePathModifications(false);
    }

    /**
     * Enables all WaypointPanel buttons and mouse events related
     * to the manipulation of waypoints.
     */
    private void unlockWaypoints() {
        //Buttons
        setHomeButton.setEnabled(true);
        enterButton.setEnabled(true);
        undoButton.setEnabled(true);
        redoButton.setEnabled(true);
        saveButton.setEnabled(true);
        loadButton.setEnabled(true);
        reTarget.setEnabled(true);
        looping.setEnabled(true);

        //Manual Fields
        latitude.setEditable(true);
        longitude.setEditable(true);
        altitude.setEditable(true);

        //Mouse events
        map.enablePathModifications(true);
    }

    public boolean getIsMoving() {
        return isUnitMoving;
    }

    /**
     * Opens a dialog and gets a filename/path from the user to use for
     * either reading or writing a GPX formatted xml file.
     *
     * @param title - The file dialog's window title
     * @return - String - The target filename
     */
    private String getFileName(String title) {
        WaypointCommand command;
        JFileChooser chooser;
        FileNameExtensionFilter filter;

        chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        filter = new FileNameExtensionFilter("GPX files", "xml", "gpx");
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        }

        return "";
    }

    private class TelemField extends JTextField {
        float lastSetValue = Float.NaN;

        @Override
        public void setText(String newString) {
            super.setText(newString);
            lastSetValue = Float.NaN;
        }

        void update(float newValue) {
            /*
             * editable float fields will get their cursor reset unless
             * updates from possible waypointlistener events only change
             * the text when the dot moves
             */
            if (newValue != lastSetValue) {
                setText(Float.toString(newValue));
                lastSetValue = newValue;
            }
        }
    }
}

