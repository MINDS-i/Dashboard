package com.map;

import static com.map.WaypointList.*;
import com.Context;
import com.Dashboard;
import com.graph.Graph;
import com.map.coordinateListener;
import com.map.Dot;
import com.serial.*;
import com.ui.DataWindow;
import com.ui.LogViewer;
import com.ui.ninePatch.NinePatchPanel;
import com.ui.SystemConfigWindow;
import com.ui.Theme;
import com.xml;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.xml.stream.XMLStreamException;

class WaypointPanel extends NinePatchPanel {
    protected static final int MOVE_STEP = 32;
    protected static final int BDR_SIZE = 25;
    protected static final String NO_WAYPOINT_MSG = "N / A";
    private Context context;
    private MapPanel map;
    private WaypointList waypoints;
    TelemField latitude;
    TelemField longitude;
    TelemField altitude;
    JLabel waypointIndexDisplay;

    private class TelemField extends JTextField{
        float lastSetValue = Float.NaN;
        @Override
        public void setText(String newString){
            super.setText(newString);
            lastSetValue = Float.NaN;
        }
        void update(float newValue){
            /**
             * editable float fields will get their cursor reset unless
             * updates from possible waypointlistener events only change
             * the text when the dot moves
             */
            if(newValue != lastSetValue){
                setText(Float.toString(newValue));
                lastSetValue = newValue;
            }
        }
    }

    public WaypointPanel(Context cxt, MapPanel mapPanel) {
        super(cxt.theme.panelPatch);
        map = mapPanel;
        context = cxt;
        makeActions();
        waypoints = context.getWaypointList();
        setOpaque(false);
        LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(BDR_SIZE,BDR_SIZE,BDR_SIZE,BDR_SIZE));
        buildPanel();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                //do nothing here to prevent clicks from falling through
                //to the map panel in the background
            }
        });

        waypoints.addListener(new WaypointListener(){
            @Override public void unusedEvent() { updateDisplay(); }
        });
        updateDisplay();

    }

    private void makeActions(){
        nextTileServer = new AbstractAction() {
            java.util.List<String> serverLabels
                = new LinkedList<String>(map.tileServerNames());
            { updateLabel();
            }
            private void updateLabel(){
                putValue(Action.NAME, serverLabels.get(0));
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

        String indexField = (selectedWaypoint+1) + " / " + waypoints.size();
        // special dots that don't represent waypoints show names instead of index
        switch(w.type()){
            case ROVER: indexField = "Robot"; break;
            case HOME: indexField = "Home "; break;
        }
        waypointIndexDisplay.setText(indexField);

        latitude.update(  (float)dot.getLatitude() );
        longitude.update( (float)dot.getLongitude() );
        altitude.update(  (float)fixedToDouble(dot.getAltitude()) );

        latitude.setForeground(Color.BLACK);
        longitude.setForeground(Color.BLACK);
        altitude.setForeground(Color.BLACK);
        if(selectedWaypoint < 0){
            latitude.setEditable(false);
            longitude.setEditable(false);
            altitude.setEditable(false);
        } else {
            latitude.setEditable(true);
            longitude.setEditable(true);
            altitude.setEditable(true);
        }

    }

    private void buildPanel() {
        final Theme theme = context.theme;
        final Dimension space = new Dimension(0,5);
        final Dimension buttonSize = new Dimension(140, 25);

        //make all the buttons
        JButton tileButton 	= theme.makeButton(nextTileServer);
        JButton dataPanel 	= theme.makeButton(openDataPanel);
        JButton graphButton = theme.makeButton(buildGraph);
        JButton reTarget 	= theme.makeButton(reTargetRover);
        JButton looping 	= theme.makeButton(toggleLooping);
        JButton config      = theme.makeButton(openConfigWindow);
        JButton logPanelButton = theme.makeButton(logPanelAction);
        JComponent[] format = new JComponent[] {
            tileButton, dataPanel, graphButton,
            reTarget, looping, config, logPanelButton
        };
        for(JComponent jc : format) {
            jc.setAlignmentX(Component.CENTER_ALIGNMENT);
            jc.setMaximumSize(buttonSize);
        }

        //make zoom button panel
        JPanel zoom = new JPanel(new FlowLayout());
        zoom.setOpaque(false);
        zoom.add(theme.makeButton(zoomInAction));
        zoom.add(theme.makeButton(zoomOutAction));

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
        ArrayList<EditBoxSpec> editorBoxes = new ArrayList<EditBoxSpec>();
        latitude  = new TelemField();
        longitude = new TelemField();
        altitude  = new TelemField();
        editorBoxes.add(new EditBoxSpec(latitude , "Lat: "));
        editorBoxes.add(new EditBoxSpec(longitude, "Lng: "));
        editorBoxes.add(new EditBoxSpec(altitude , context.getResource("waypointExtra")+" "));
        ArrayList<JPanel> editorPanels = new ArrayList<JPanel>();
        for(EditBoxSpec box : editorBoxes) {
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
            coordinateListener listener = new coordinateListener(tf, this);
            tf.getDocument().addDocumentListener(listener);
            tf.addActionListener(listener);
            //add to layout
            editorPanels.add(panel);
        }

        //rows, cols, hgap, vgap
        JPanel waypointOptions = new JPanel(new GridLayout(2,2,5,5));
        waypointOptions.setOpaque(false);
        waypointOptions.add(theme.makeButton(newWaypoint));
        waypointOptions.add(theme.makeButton(interpretLocationAction));
        waypointOptions.add(theme.makeButton(saveWaypoints));
        waypointOptions.add(theme.makeButton(loadWaypoints));

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
        add(selector);
        add(Box.createRigidArea(space));
        for(JPanel panel : editorPanels) {
            add(panel);
        }
        add(Box.createRigidArea(space));
        add(waypointOptions);
        add(Box.createRigidArea(space));
        add(reTarget);
        add(Box.createRigidArea(space));
        add(looping);
    }

    private double fixedToDouble(int i) {
        return ((double)(i&0xffff))/((double)Serial.U16_FIXED_POINT);
    }
    private int    doubleToFixed(double i) {
        return (int)(i*Serial.U16_FIXED_POINT);
    }

    public void interpretLocationEntry() {
        try {
            int selectedWaypoint = waypoints.getSelected();

            if(selectedWaypoint < 0) // rover or home location selected
                return;

            Double newLatitude  = Double.parseDouble(latitude.getText());
            Double newLongitude = Double.parseDouble(longitude.getText());
            Double tmpAltitude  = Double.parseDouble(altitude.getText());

            int newAltitude = doubleToFixed(tmpAltitude);
            Point.Double newPosition = new Point.Double(newLongitude, newLatitude);

            if((newAltitude&0xffff) == newAltitude) {
                waypoints.set(new Dot(newPosition, (short)newAltitude), selectedWaypoint);
                //set to display reconverted value
                altitude.setText(fixedToDouble(newAltitude)+"");
            } else {
                Dot newloc = waypoints.get(selectedWaypoint).dot();
                newloc.setLocation(newPosition);
                waypoints.set(newloc, selectedWaypoint);
            }
        } catch (NumberFormatException e) {}
    }
    private Action logPanelAction = new AbstractAction() {
        LogViewer lv;
        Logger log;
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
    private Action zoomInAction = new AbstractAction() {
        {
            String text = "+";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            map.zoomIn(new Point(map.getWidth() / 2, map.getHeight() / 2));
        }
    };
    private Action zoomOutAction = new AbstractAction() {
        {
            String text = "-";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            map.zoomOut(new Point(map.getWidth() / 2, map.getHeight() / 2));
        }
    };
    private Action nextTileServer;
    private Action toggleLooping = new AbstractAction() {
        {
            String text = "Looping On";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            waypoints.setLooped(!waypoints.getLooped());
            putValue(Action.NAME,
                     (waypoints.getLooped())? "Looping Off" : "Looping On");
        }
    };
    private Action previousWaypoint = new AbstractAction() {
        {
            String text = "<";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            waypoints.setSelected(waypoints.getSelected()-1);
            updateDisplay();
            map.repaint();
        }
    };
    private Action nextWaypoint = new AbstractAction() {
        {
            String text = ">";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            waypoints.setSelected(waypoints.getSelected()+1);
            updateDisplay();
            map.repaint();
        }
    };
    private Action saveWaypoints = new AbstractAction() {

        {
            String text = "Save";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            try {
                xml.writeXML(context);
            } catch (XMLStreamException ex) {
                System.err.println(ex.getMessage());
            }
        }
    };
    private Action loadWaypoints = new AbstractAction() {
        {
            String text = "Load";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            try {
                xml.readXML(context);
            } catch (XMLStreamException ex) {
                System.err.println(ex.getMessage());
            }
        }
    };
    private Action interpretLocationAction = new AbstractAction() {
        {
            String text = "Enter";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            interpretLocationEntry();
        }
    };
    private Action reTargetRover = new AbstractAction() {
        {
            String text = "Set Target";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            waypoints.setTarget(waypoints.getSelected());
        }
    };
    private Action newWaypoint = new AbstractAction() {
        {
            String text = "New";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            int selectedWaypoint = waypoints.getSelected();
            waypoints.add(
            	new Dot(waypoints.get(selectedWaypoint).dot()), selectedWaypoint);
        }
    };
    private Action openDataPanel = new AbstractAction() {
        {
            String text = "Telemetry";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            final DataWindow window = new DataWindow(context);
        }
    };
    private Action openConfigWindow = new AbstractAction() {
        {
            String text = "Configuration";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            final SystemConfigWindow window = new SystemConfigWindow(context);
        }
    };
    private Action buildGraph = new AbstractAction() {
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
}

