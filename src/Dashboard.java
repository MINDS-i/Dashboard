package com;

import com.logging.*;
import com.map.MapPanel;
import com.map.WaypointList;
import com.serial.Serial;
import com.serial.SerialConnectPanel;
import com.serial.SerialEventListener;
import com.serial.SerialParser;
import com.serial.SerialSender;
import com.telemetry.*;
import com.ui.*;
import com.ui.ArtificialHorizon.DataAxis;
import com.ui.ninePatch.*;
import com.ui.widgets.PingWidget;
import com.ui.telemetry.TelemetryDataWindow;
import com.ui.widgets.*;

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
    //Standard References
	private Context context;
    
	//Static Values
    private static final int DEF_WINDOW_WIDTH  		= 1200;
    private static final int DEF_WINDOW_HEIGHT 		= 900;
    private static final int HORIZON_WIDGET_SIZE 	= 145;
    private static final int DEFAULT_ZOOM_LEVEL 	= 4;

    //UI Widget Frame
    WidgetPanel widgetPanel;
    
    //UI Widgets
    private TelemetryDataWidget dataWidget;
    public StateWidget stateWidget;
    public PingWidget pingWidget;
    public GPSWidget gpsWidget;
    public BumperWidget bumperWidget;
    public MapPanel mapPanel;
    
    //Logging
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
        serialPanel.showBaudSelector(true);
        
        JPanel messageBox = createAlertBox();

        mapPanel = new MapPanel(context,
        								 new Point((int)context.getHomeProp().getY(),
        										   (int)context.getHomeProp().getX()),
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
        rootlog.addHandler(handler);
        return ap;
    }

    private void registerHorizonListeners(ArtificialHorizon ah, boolean sideBars){
        final float lastPitch[] = new float[1];
        context.telemetry.registerListener(Serial.PITCH, new TelemetryListener() {
            public void update(double pitch) {
                lastPitch[0] = (float)Math.toRadians(pitch);
            }
        });
        context.telemetry.registerListener(Serial.ROLL, new TelemetryListener() {
            public void update(double roll) {
                ah.setAngles(lastPitch[0], (float)Math.toRadians(-roll));
            }
        });

        if(sideBars){
            ah.setEnabled(DataAxis.TOP, true);
            context.telemetry.registerListener(Serial.HEADING, new TelemetryListener() {
                public void update(double yaw) {
                    ah.set(DataAxis.TOP, (float)yaw);
                }
            });

            ah.setEnabled(DataAxis.RIGHT, true);
            context.telemetry.registerListener(Serial.DELTAALTITUDE, new TelemetryListener() {
                public void update(double alt) {
                    ah.set(DataAxis.RIGHT, (float)alt);
                }
            });
        }
    }

    /**
     * Creates the right panel display for the dashboard. This display is populated
     * with with preconfigured UIWidgets that plug in to a larger WidgetPanel.
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
    	catch(Exception e) {
            iolog.severe("Failed to load telemetry widget line spec " + e);
            e.printStackTrace();
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
        
        //TODO - CP - Fix widget bug on the following two adds (horizon and radio widgets)
        if(context.getResource("widget_type", "Angles").equals("Horizon")) {
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
     * @return - The telemetry data widget.
     */
    private TelemetryDataWidget createTelemetryWidget() throws Exception {
    	if(context.getCurrentLocale() == "ground") {
			return TelemetryDataWidget.fromXML(context, "telemetryWidgetGnd");
		}
		else {
			return TelemetryDataWidget.fromXML(context, "telemetryWidgetAir");
		}
    } 
    
    /**
     * Creates a horizon widget with mouse event listener to pop out
     * a new horizon window when clicked.
     * @return - The horizon widget.
     */
    private JPanel createHorizonWidget() {
    	// Initialize the horizon widget
        JPanel horizon =
            HorizonWidgets.makeHorizonWidget(
            		context,
            		HORIZON_WIDGET_SIZE, 
            		(ArtificialHorizon ah)->{registerHorizonListeners(ah, false);
            });
        
        horizon.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                HorizonWidgets.makeHorizonWindow(context, (ArtificialHorizon ah)->{
                    registerHorizonListeners(ah, true);
                }).setVisible(true);
            }
        });
        
        return horizon;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.setProperty("sun.java2d.opengl", openglProperty);
        System.out.println("Launching with opengl="+openglProperty);

        Dashboard se = new Dashboard();
        SwingUtilities.invokeLater(se);
    }
}
