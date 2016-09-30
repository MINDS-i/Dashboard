package com;

import com.Dashboard;
import com.map.*;
import static com.map.WaypointList.*;
import com.remote.SettingList;
import com.serial.*;
import com.serial.Messages.*;
import com.ui.*;
import com.xml;
import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import jssc.SerialPort;
import jssc.SerialPortException;

public class Context {
    public boolean connected;
    public Dashboard dash;
    public Locale locale;
    public SerialParser parser;
    public SerialSender sender;
    public Theme theme;
    public SettingList settingList;
    public TelemetryManager telemetry;

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
        theme       = new Theme(this);
        sender      = new SerialSender(this);
        parser      = new SerialParser(this, waypoint);
        settingList = new SettingList(this);
        telemetry   = new TelemetryManager(this);
        waypoint    = new WaypointList();

        // Patch the waypoint list into the serial sender
        waypoint.addListener(new WaypointListener(){
            @Override
            public void changed(Dot point, int index, Action action) {
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
            public void targetChanged(int targetIndex) {
                sender.sendMessage(Message.setTarget((byte) targetIndex));
            }
            @Override
            public void loopModeSet(boolean isLooped) {
                sender.sendMessage(Message.setLooping((byte) ((isLooped)?1:0) ));
            }
        });

        final double lastLatitude[] = new double[1];
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
        String rtn = getResource(name);
        if(rtn == null) return otherwise;
        return rtn;
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
        return telemetry.telemetryCount();
    }
    public void onConnection() {
        sender.sendWaypointList();
    }
}
