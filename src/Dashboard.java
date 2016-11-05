package com;

import com.logging.*;
import com.map.MapPanel;
import com.map.WaypointList;
import com.serial.Serial;
import com.serial.SerialConnectPanel;
import com.serial.SerialEventListener;
import com.serial.SerialParser;
import com.serial.SerialSender;
import com.ui.*;
import com.ui.ninePatch.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.geom.AffineTransform;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Dashboard implements Runnable {
    private static final int START_WIDTH  = 820; //default window width
    private static final int START_HEIGHT = 820; //default window height
    private Context context;

    private TelemetryWidget dataWidget;

    private final Logger seriallog = Logger.getLogger("d.serial");
    private final Logger iolog = Logger.getLogger("d.io");
    private final Logger rootlog = Logger.getLogger("d");

    @Override
    public void run() {
        try {
            //setup a loading frame
            BufferedImage logo = ImageIO.read(new File("./resources/images/startup-logo.png"));
            JFrame loading = new JFrame("MINDS-i Loading Box");
            loading.setUndecorated(true);
            loading.setBackground(new Color(0,0,0,0));
            loading.add(new JLabel(new ImageIcon(logo)));
            loading.pack();
            loading.setVisible(true);
            //initialize the major classes into the context
            createLogDirectory();
            context = new Context(this);
            initLogging();
            initUI();
            loading.dispose();
        } catch (IOException e) {
            rootlog.severe("Dashboard startup failure: "+e.toString());
            displayErrorPopup((Exception)e);
        }
    }

    private void createLogDirectory(){
        File logDir = new File("log");
        try{
            logDir.mkdir();
        } catch (Exception e){
            System.err.println("Cannot create log directory");
            e.printStackTrace();
        }
    }

    private void initLogging() {
        Logger root = rootlog;
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
            Handler file = new FileHandler("log/"+context.getInstanceLogName()+".log");
            file.setFormatter(new SimpleFormatter());
            String fileLevel = context.getResource("file_log_level", "OFF");
            file.setLevel(Level.parse(fileLevel));
            root.addHandler(file);
        } catch (Exception e) {
            iolog.severe("Log File Write Failed "+e.toString());
            e.printStackTrace();
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
                context.sender.sendSync();
            }
            public void disconnectRequest() {
                context.closePort();
                seriallog.info("Serial Port Closed");
                resetData();
            }
        };
        SerialConnectPanel serialPanel = new SerialConnectPanel(connectActions);

        JPanel messageBox = createAlertBox();

        MapPanel mapPanel = new MapPanel(context,
                                         new Point(0,0),
                                         4, // default zoom level
                                         serialPanel,
                                         createRightPanel(),
                                         messageBox);

        f.add(mapPanel);
        f.pack();
        f.setSize(START_WIDTH, START_HEIGHT);
        f.setVisible(true);
    }

    private JPanel createAlertBox() {
        AlertPanel ap = new AlertPanel(context.theme.alertFont, 8, 80);
        ap.setColor(context.theme.textColor);

        Handler handler = new SimpleHandler((LogRecord l, String s) ->
                                            ap.addMessage(s));
        handler.setLevel(Level.INFO);
        rootlog.addHandler(handler);
        return ap;
    }

    private JPanel createRightPanel() {
        try {
            dataWidget = TelemetryWidget.fromXML(context, "telemetryWidetSpec");
        } catch(Exception e) {
            iolog.severe("Failed to load telemetry widget line spec "+e);
            e.printStackTrace();
        }

        JPanel dashPanel = new JPanel();
        dashPanel.setOpaque(false);
        dashPanel.setLayout(new BoxLayout(dashPanel, BoxLayout.PAGE_AXIS));

        dashPanel.add(dataWidget);
        System.out.println(context.getResource("widget_type", "Angles"));
        dashPanel.add(
            AngleWidget.createDial(
                context, Serial.HEADING, context.theme.roverTop));
        if(context.getResource("widget_type", "Angles").equals("Horizon")){
            dashPanel.add(
                HorizonWidgets.makeHorizonWidget(context, 140));
        } else {
            dashPanel.add(
                AngleWidget.createDial(
                    context, Serial.PITCH, context.theme.roverSide));
            dashPanel.add(
                AngleWidget.createDial(
                    context, Serial.ROLL, context.theme.roverFront));
        }

        return dashPanel;
    }

    private void resetData() {
        dataWidget.reset();
    }

    public static void displayErrorPopup(Exception e) {
        final JFrame errorFrame = new JFrame("Data");
        errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        errorFrame.setLayout(new FlowLayout());
        errorFrame.setVisible(true);
        JPanel panel = new JPanel();
        JLabel text = new JLabel("Error: \n"+e.getMessage());
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
        } catch (Exception e){
            e.printStackTrace();
        }

        System.setProperty("sun.java2d.opengl", openglProperty);
        System.out.println("Launching with opengl="+openglProperty);

        Dashboard se = new Dashboard();
        SwingUtilities.invokeLater(se);
    }
}
