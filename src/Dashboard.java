package com;

import com.logging.SimpleHandler;
import com.map.MapPanel;
import com.serial.*;
import com.ui.AlertPanel;
import com.ui.ArtificialHorizon;
import com.ui.ArtificialHorizon.DataAxis;
import com.ui.RadioWidget;
import com.ui.WidgetPanel;
import com.ui.widgets.*;
import jssc.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.*;

public class Dashboard implements Runnable {
    //Static Values
    private static final int DEF_WINDOW_WIDTH = 1200;
    private static final int DEF_WINDOW_HEIGHT = 900;
    private static final int HORIZON_WIDGET_SIZE = 145;
    private static final int DEFAULT_ZOOM_LEVEL = 4;
    //Logging
    private final Logger seriallog = LoggerFactory.getLogger("d.serial");
    private final Logger iolog = LoggerFactory.getLogger("d.io");
    private final Logger rootlog = LoggerFactory.getLogger("d");
    public StateWidget stateWidget;
    public PingWidget pingWidget;
    public GPSWidget gpsWidget;
    public BumperWidget bumperWidget;
    public MapPanel mapPanel;
    public SerialConnectPanel serialPanel;
    //UI Widget Frame
    WidgetPanel widgetPanel;
    //Standard References
    private Context context;
    //UI Widgets
    private TelemetryDataWidget dataWidget;

    public static void displayErrorPopup(Exception e) {
        final JFrame errorFrame = new JFrame("Data");
        errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        errorFrame.setLayout(new FlowLayout());
        errorFrame.setVisible(true);
        JPanel panel = new JPanel();
        JLabel text = new JLabel("Error: \n" + e.getMessage());
        panel.add(text);
        errorFrame.add(panel);
        errorFrame.pack();
    }

    public static void main(String[] args) {
        String openglProperty = "false";

        try (Reader optFile = new FileReader("./resources/system.properties")) {
            Properties launchOptions = new Properties();
            launchOptions.load(optFile);
            openglProperty =
                    launchOptions.getProperty("opengl", openglProperty);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.setProperty("sun.java2d.opengl", openglProperty);
        System.out.println("Launching with opengl=" + openglProperty);

        Dashboard se = new Dashboard();
        SwingUtilities.invokeLater(se);
    }

    @Override
    public void run() {
        try {
            //setup a loading frame
            BufferedImage logo = ImageIO.read(new File("./resources/images/startup-logo.png"));
            JFrame loading = new JFrame("MINDS-i Loading Box");
            loading.setUndecorated(true);
            loading.setBackground(new Color(0, 0, 0, 0));
            loading.add(new JLabel(new ImageIcon(logo)));
            loading.pack();
            loading.setVisible(true);
            //initialize the major classes into the context
            createLogDirectory();
            context = new Context(this);
            initLogging();
            initUI();
            loading.dispose();
        }
        catch (IOException e) {
            rootlog.error("Dashboard startup failure: ", e);
            displayErrorPopup(e);
        }
    }

    private void createLogDirectory() {
        File logDir = new File("log");
        try {
            //noinspection ResultOfMethodCallIgnored
            logDir.mkdir();
        }
        catch (Exception e) {
            rootlog.error("Cannot create log directory", e);
        }
    }

    private void initLogging() {
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("d");
        root.setUseParentHandlers(false);
        root.setLevel(Level.ALL);

        java.util.logging.Formatter simpleForm = new java.util.logging.Formatter() {
            public String format(LogRecord rec) {
                return String.format("%s: %s%n", rec.getLevel(), rec.getMessage());
            }
        };
        Handler console = new ConsoleHandler();
        console.setFormatter(simpleForm);
        String consoleLevel = context.getResource("console_log_level", "OFF");
        console.setLevel(Level.parse(consoleLevel));
        root.addHandler(console);

        try {
            Handler file = new FileHandler("log/" + context.getInstanceLogName() + ".log");
            file.setFormatter(new SimpleFormatter());
            String fileLevel = context.getResource("file_log_level", "OFF");
            file.setLevel(Level.parse(fileLevel));
            root.addHandler(file);
        }
        catch (Exception e) {
            iolog.error("Log File Write Failed ", e);
        }
    }

    private void initUI() {
        JFrame f = new JFrame("MINDS-i Dashboard");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(false);
        f.setIconImage(context.theme.appIcon);//roverTop);
        f.setTitle("MINDS-i dashboard");

        SerialEventListener connectActions = new SerialEventListener() {
            public void connectionEstablished(SerialPort port) {
                context.updatePort(port);
                seriallog.info("Port opened");
                SerialSendManager.getInstance().sendSync();
                CommsMonitor.getInstance().startHeartbeatTimer();
            }

            public void disconnectRequest() {
                context.closePort();
                seriallog.info("Serial Port Closed");
                resetData();
                CommsMonitor.getInstance().stopHeartbeatTimer();
            }
        };

        serialPanel = new SerialConnectPanel(connectActions);
        serialPanel.showBaudSelector(true);

        JPanel messageBox = createAlertBox();

        mapPanel = new MapPanel(context,
                new Point2D.Double(context.getHomeProp().getY(),
                        context.getHomeProp().getX()),
                DEFAULT_ZOOM_LEVEL,
                serialPanel,
                createRightPanel(),
                messageBox);

        f.add(mapPanel);
        f.pack();
        f.setSize(DEF_WINDOW_WIDTH, DEF_WINDOW_HEIGHT);
        f.setVisible(true);
    }

    private JPanel createAlertBox() {
        AlertPanel ap = new AlertPanel(context.theme.alertFont, 8, 80);
        ap.setColor(context.theme.textColor);

        Handler handler = new SimpleHandler((LogRecord l, String s) ->
                ap.addMessage(s));
        handler.setLevel(Level.INFO);
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("d");
        root.addHandler(handler);
        return ap;
    }

    private void registerHorizonListeners(ArtificialHorizon ah, boolean sideBars) {
        final float[] lastPitch = new float[1];
        context.telemetry.registerListener(Serial.PITCH, pitch -> lastPitch[0] = (float) Math.toRadians(pitch));
        context.telemetry.registerListener(Serial.ROLL, roll -> ah.setAngles(lastPitch[0], (float) Math.toRadians(-roll)));

        if (sideBars) {
            ah.setEnabled(DataAxis.TOP, true);
            context.telemetry.registerListener(Serial.HEADING, yaw -> ah.set(DataAxis.TOP, (float) yaw));

            ah.setEnabled(DataAxis.RIGHT, true);
            context.telemetry.registerListener(Serial.DELTAALTITUDE, alt -> ah.set(DataAxis.RIGHT, (float) alt));
        }
    }

    /**
     * Creates the right panel display for the dashboard. This display is populated
     * with with preconfigured UIWidgets that plug in to a larger WidgetPanel.
     *
     * @return - The right panel
     */
    private JPanel createRightPanel() {
        JPanel outerPanel = new JPanel();
        outerPanel.setOpaque(false);
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.PAGE_AXIS));
        widgetPanel = new WidgetPanel(context);
        widgetPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        //Telemetry Data Widget
        try {
            dataWidget = createTelemetryWidget();
            widgetPanel.addWidget(dataWidget);
        }
        catch (Exception e) {
            iolog.error("Failed to load telemetry widget line spec ", e);
        }

        //State Widget
        stateWidget = new StateWidget(context);
        widgetPanel.addWidget(stateWidget);


        //Ping Widget
        pingWidget = new PingWidget(context);
        widgetPanel.add(pingWidget);


        //GPS Widget
        gpsWidget = new GPSWidget(context);
        widgetPanel.add(gpsWidget);

        //Bumper Widget
        bumperWidget = new BumperWidget(context);
        widgetPanel.add(bumperWidget);

        outerPanel.add(widgetPanel);


        //Round Widgets
        outerPanel.add(AngleWidget.createDial(
                context, Serial.HEADING, context.theme.roverTop));

        if (context.getResource("widget_type", "Angles").equals("Horizon")) {
            outerPanel.add(createHorizonWidget());
            outerPanel.add(RadioWidget.create(context, HORIZON_WIDGET_SIZE));
        }
        else {
            outerPanel.add(
                    AngleWidget.createDial(
                            context, Serial.PITCH, context.theme.roverSide));
            outerPanel.add(
                    AngleWidget.createDial(
                            context, Serial.ROLL, context.theme.roverFront));
        }

        //Watermark Image
        BufferedImage watermark = context.theme.logoWatermark;
        JLabel watermarkLabel = new JLabel(new ImageIcon(watermark));
        watermarkLabel.setOpaque(false);
        JPanel logo = new JPanel();
        logo.setOpaque(false);
        logo.add(watermarkLabel);
        outerPanel.add(logo);

        return outerPanel;
    }

    /**
     * Creates and returns a TelementryDataWidget from xml. The type of
     * telemetry in the widget is selected using the mode configuration
     * found in the users persistent settings.
     *
     * @return - The telemetry data widget.
     */
    private TelemetryDataWidget createTelemetryWidget() throws Exception {
        if (Objects.equals(context.getCurrentLocale(), "ground")) {
            return TelemetryDataWidget.fromXML(context, "telemetryWidgetGnd");
        }
        else {
            return TelemetryDataWidget.fromXML(context, "telemetryWidgetAir");
        }
    }

    /**
     * Creates a horizon widget with mouse event listener to pop out
     * a new horizon window when clicked.
     *
     * @return - The horizon widget.
     */
    private JPanel createHorizonWidget() {
        // Initialize the horizon widget
        JPanel horizon =
                HorizonWidgets.makeHorizonWidget(
                        context,
                        HORIZON_WIDGET_SIZE,
                        (ArtificialHorizon ah) -> registerHorizonListeners(ah, false));

        horizon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HorizonWidgets.makeHorizonWindow(context, (ArtificialHorizon ah) -> registerHorizonListeners(ah, true)).setVisible(true);
            }
        });

        return horizon;
    }

    private void resetData() {
        dataWidget.reset();
    }

    //Toggle the enabled/disabled state of the serial panel
    public void enableSerialPanel(boolean isActive) {
        serialPanel.setEnabled(isActive);
    }
}
