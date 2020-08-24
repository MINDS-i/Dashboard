package com;

import com.Dashboard;
import com.map.*;
import static com.map.WaypointList.*;
import com.remote.SettingList;
import com.serial.*;
import com.serial.Messages.*;
import com.ui.*;
import com.telemetry.*;
import com.xml;
import com.graph.DataSource;
import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import jssc.SerialPort;
import jssc.SerialPortException;
import java.awt.geom.Point2D;

public class Context {
    public boolean connected;
    public Dashboard dash;
    public Locale locale;
    public SerialParser parser;
    public SerialSender sender;
    public Theme theme;
    public SettingList settingList;
    public TelemetryManager telemetry;
    public TelemetryLogger telemLog;

    private WaypointList waypoint;
    private SerialPort port;
    private ResourceBundle resources;
    private Properties persist;

    private static final File persistanceFile =
        new File("./resources/persist/persist.properties");
    private final String instanceLogName;

    private final Logger ioerr = Logger.getLogger("d.io");

    public Context(Dashboard dashboard) {
        dash        = dashboard;
        port        = null;
        connected   = false;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd_HHmm_yyyyGG");
        instanceLogName = sdf.format(cal.getTime());

        persist = new Properties();
        try {
            if(!persistanceFile.exists()) {
                persistanceFile.getParentFile().mkdirs();
                persistanceFile.createNewFile();
            }
            InputStream is =new FileInputStream(persistanceFile);
            persist.load(is);
            is.close();
        } catch (Exception e) {
            ioerr.severe("Failed to open and read persistance file");
            e.printStackTrace();
        } finally {
            loadLocale(); // defaults to "air" mode
        }

        // Instance these classes after the resources have been loaded
        waypoint    = new WaypointList();
        theme       = new Theme(this);
        sender      = new SerialSender(this);
        parser      = new SerialParser(this, waypoint);
        settingList = new SettingList(this);
        telemetry   = new TelemetryManager(this);
        telemLog    = new TelemetryLogger(this, telemetry);

        loadHomeLocation();
        
        // Patch the waypoint list into the serial sender
        waypoint.addListener(new WaypointListener(){
            @Override
            public void changed(Source s, Dot point, int index, Action action) {
                if(s == Source.REMOTE) return;
                Message tosend;
                switch(action){
                    case ADD:
                        tosend = Message.addWaypoint((byte)(index&0xff), point);
                        break;
                    case SET:
                        tosend = Message.setWaypoint((byte)(index&0xff), point);
                        break;
                    case DELETE:
                        tosend = Message.deleteWaypoint((byte)(index&0xff));
                        break;
                    default:
                        return;
                }
                sender.sendMessage(tosend);
            }
            @Override
            public void targetChanged(Source s, int targetIndex) {
                if(s == Source.REMOTE) return;
                sender.sendMessage(Message.setTarget((byte) targetIndex));
            }
            @Override
            public void loopModeSet(Source s, boolean isLooped) {
                if(s == Source.REMOTE) return;
                sender.sendMessage(Message.setLooping((byte) ((isLooped)?1:0) ));
            }
        });

        final double lastLatitude[] = new double[2];
        telemetry.registerListener(Serial.LATITUDE, new TelemetryListener() {
            public void update(double data) {
                lastLatitude[0] = data;
            }
        });
        telemetry.registerListener(Serial.LONGITUDE, new TelemetryListener() {
            public void update(double data) {
                Dot location = waypoint.getRover();
                location.setLatitude(lastLatitude[0]);
                location.setLongitude(data);
                waypoint.setRover(location);
            }
        });
        telemetry.registerListener(Serial.HOMELATITUDE, new TelemetryListener() {
            public void update(double data) {
                lastLatitude[1] = data;
            }
        });
        telemetry.registerListener(Serial.HOMELONGITUDE, new TelemetryListener() {
            public void update(double data) {
                Dot location = waypoint.getHome();
                location.setLatitude(lastLatitude[1]);
                location.setLongitude(data);
                waypoint.setHome(location);
            }
        });
        telemetry.registerListener(Serial.ALTITUDE, new TelemetryListener() {
            public void update(double altitude) {
                double homeAlt = telemetry.getTelemetry(Serial.HOMEALTITUDE);
                if(homeAlt != 0.0d){
                    telemetry.updateTelemetry(Serial.DELTAALTITUDE,
                        altitude - homeAlt
                        );
                }
            }
        });
    }
    public void toggleLocale() {
        String current = (String) persist.get("subject");
        if(current.equals("air")) {
            persist.put("subject","ground");
        } else {
            persist.put("subject","air");
        }
        saveProps();
    }
    
    public void setHomeProp(String lat, String lng) {
    	persist.setProperty("homeLat", lat);
    	persist.setProperty("homeLng", lng);
    	saveProps();
    }
    
    public Point2D getHomeProp() {
    	String lat = (String) persist.getProperty("homeLat", "0.0");
    	String lng = (String) persist.getProperty("homeLng", "0.0");
    	
    	return new Point2D.Double(Double.parseDouble(lat),
    			Double.parseDouble(lng));
    }
    
    private void saveProps() {
        try(FileOutputStream file = new FileOutputStream(persistanceFile)) {
            persist.store(file, "");
        } catch (Exception e) {
            ioerr.severe("Can't save persist props "+e);
        }
    }
    private void loadLocale() {
        if (!persist.containsKey("subject")) {
            persist.put("subject","air");
        }
        String sub = (String) persist.get("subject");
        locale = new Locale("en","US",sub);
        resources = loadResourceBundle("resources");
    }

    public void loadHomeLocation() {
    	Dot location = waypoint.getHome();
    	Point2D home = getHomeProp();
    	location.setLatitude(home.getX());
    	location.setLongitude(home.getY());
    	waypoint.setHome(location);
    }
    
    public void updatePort(SerialPort newPort) {
        closePort();
        port = newPort;
        sender.start();
        parser.updatePort();
        connected = true;
    }
    public void closePort() {
        sender.stop();
        port = null;
        connected = false;
    }
    public SerialPort port() {
        return port;
    }
    public String getInstanceLogName() {
        return instanceLogName;
    }
    public ResourceBundle loadResourceBundle(String name) {
        return ResourceBundle.getBundle(name, locale);
    }
    public String getResource(String name) {
        return resources.getString(name);
    }
    public String getResource(String name, String otherwise) {
        try {
            String rtn = getResource(name);
            if(rtn == null) return otherwise;
            return rtn;
        } catch (Exception e){
            return otherwise;
        }
    }
    //register viewer of waypoint list
    public void addWaypointListener(WaypointListener l) {
        waypoint.addListener(l);
    }
    //remove viewer of waypoint list
    public void removeWaypointListener(WaypointListener l) {
        waypoint.removeListener(l);
    }
    public WaypointList getWaypointList(){
        return waypoint;
    }
    public void sendSetting(int index) {
        settingList.pushSetting(index);
    }
    public void setSetting(int index, float value) {
        settingList.pushSetting(index, value);
    }
    public void setSettingQuiet(int index, float value) {
        settingList.updateSettingVal(index, value);
    }
    public void setTelemetry(int id, float value) {
        telemetry.updateTelemetry(id, (double)value);
    }
    public float getTelemetry(int id) {
        return (float) telemetry.getTelemetry(id);
    }
    public String getTelemetryName(int id) {
        return telemetry.getTelemetryName(id);
    }
    public int getTelemetryCount() {
        return telemetry.maxIndex();
    }
    public List<DataSource> getTelemetryDataSources() {
        return telemetry.getDataSources();
    }
    public void onConnection() {
        sender.sendWaypointList();
    }
}
