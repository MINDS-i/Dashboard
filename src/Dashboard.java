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
            context = new Context();
            context.give(this,
                         new SerialSender(context),
                         new SerialParser(context),
                         new WaypointList(context),
                         null //serialPort
                        );
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
        JPanel sideGauge  = AngleWidget.createDial(context, Serial.HEADING, context.theme.roverSide);
        JPanel topGauge   = AngleWidget.createDial(context, Serial.PITCH, context.theme.roverTop);
        JPanel frontGauge = AngleWidget.createDial(context, Serial.ROLL, context.theme.roverFront);

        /*Collection<TelemetryWidget.LineItem> items = new ArrayList<TelemetryWidget.LineItem>();
        items.add(new TelemetryWidget.LineItem("Lat:",0));
        items.add(new TelemetryWidget.LineItem("Lon:",1));
        items.add(new TelemetryWidget.LineItem("Dir:",2));
        JPanel dataPanel = TelemetryWidget.create(context, 13, items);*/
        JPanel dataPanel = null;
        try {
            dataPanel = TelemetryWidget.fromXML(context, "telemetryWidetSpec");
        } catch(Exception e) {
            iolog.severe("Failed to load telemetry widget line spec "+e);
            e.printStackTrace();
        }


        /*final int[] dataBorderSize = {15,18,46,18};//top,left,bottom,right
        final String dataLabels[] = {
        "Lat:",
        "Lng:",
        "Dir:",
        "Ptc:",
        "Rol:",
        "MPH:",
        "Vcc:",
        "Amp:" };
        Collection<DataLabel> displays = new ArrayList<DataLabel>(dataLabels.length);

        NinePatchPanel dataPanel = new NinePatchPanel(context.theme.screenPatch);
        dataPanel.setBorder(new EmptyBorder(dataBorderSize[0],
                                            dataBorderSize[1],
                                            dataBorderSize[2],
                                            dataBorderSize[3]) );
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
        dataPanel.setOpaque(false);
        for(int i=0; i<dataLabels.length; i++) {
            DataLabel label = new DataLabel(dataLabels[i]);
            label.setMaxLength(13);
            context.telemetry.registerListener(i, label);
            label.setForeground(context.theme.textColor);
            label.setFont(context.theme.text);
            dataPanel.add(label);
        }*/





        JPanel dashPanel = new JPanel();
        dashPanel.setLayout(new GridBagLayout());
        dashPanel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 1;
        dashPanel.add(dataPanel,c);
        c.gridy = 2;
        dashPanel.add(frontGauge,c);
        c.gridy = 3;
        dashPanel.add(topGauge,c);
        c.gridy = 4;
        dashPanel.add(sideGauge,c);

        return dashPanel;
    }

    private void resetData() {

        /*for(DataLabel label : displays) {
            label.update(0);
        }*/
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
        Dashboard se = new Dashboard();
        SwingUtilities.invokeLater(se);
    }
}
