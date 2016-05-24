package com;

import com.ContextViewer;
import com.Dashboard;
import com.map.*;
import com.remote.SettingList;
import com.serial.*;
import com.serial.Messages.*;
import com.ui.*;
import com.xml;
import java.io.*;
import java.lang.ClassLoader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import jssc.SerialPort;
import jssc.SerialPortException;

public class Context{
	public AlertPanel	alert;
	public boolean		connected;
	public Dashboard	dash;
	public Locale		locale;
	public SerialParser	parser;
	public SerialSender	sender;
	public Theme		theme;
	public WaypointList	waypoint;
	public PrintStream  log;
	public SettingList  settingList;

	public TelemetryManager telemetry;

	private SerialPort				port;
	private Vector<ContextViewer> 	waypointViewers;

	private ResourceBundle resources;
	private static String propertiesFile = "./resources/persist/persist.properties";
	private Properties persist;

	// this classloader will be setup to load .properties files from the
	// resources directory
	private static final ClassLoader resourceLoader; static {
		ClassLoader rL = null;
		try{
			File loc = new File("./resources/");
			URL[] urls = {loc.toURI().toURL()};
			rL = new URLClassLoader(urls);
		} catch (Exception e) {
			Exception error = new Exception("Can't reference resource directory", e);
			error.printStackTrace();
			Dashboard.displayErrorPopup(error);
		}
		resourceLoader = rL;
	}

	public Context(){
		connected = false;
		waypointViewers  = new Vector<ContextViewer>();

		persist = new Properties();
		try(FileInputStream file = new FileInputStream(propertiesFile)){
			persist.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		loadLocale();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm_MM-dd_yyyyGG");
        String time = sdf.format(cal.getTime());
        try {
        	log = new PrintStream("log/"+time+".log");
        } catch (Exception e){
        	System.err.println("Cannot save log file");
        	e.printStackTrace();
        }
	}
	public void toggleLocale(){
		String current = (String) persist.get("subject");
		if(current.equals("air")){
			persist.put("subject","ground");
		} else {
			persist.put("subject","air");
		}
		saveProps();
		//loadLocale();
	}
	private void saveProps(){
		try(FileOutputStream file = new FileOutputStream(propertiesFile)){
			persist.store(file, "no comment");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void loadLocale(){
		if (!persist.containsKey("subject")) {
			persist.put("subject","air");
		}
		String sub = (String) persist.get("subject");
		locale = new Locale("en","US",sub);
		resources = loadResourceBundle("resources");
	}

	public void give( 	Dashboard    dashboard,
						AlertPanel   alertPanel,
						SerialSender serialSender,
						SerialParser serialParser,
						WaypointList waypointList,
						SerialPort   serialPort){
		dash        = dashboard;
		alert       = alertPanel;
		sender      = serialSender;
		parser      = serialParser;
		waypoint    = waypointList;
		port        = serialPort;
		theme       = new Theme(this);
		telemetry   = new TelemetryManager(this);
		settingList = new SettingList(this);
		if(alertPanel != null) {
			alertPanel.setTheme(theme);
			alertPanel.logTo(log);
		}
	}
	public void updatePort(SerialPort newPort){
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
	public SerialPort port(){
		return port;
	}

	public ResourceBundle loadResourceBundle(String name){
		return ResourceBundle.getBundle("resources", locale, resourceLoader);
	}

	public String getResource(String name){
		return resources.getString(name);
	}

	public String getResource(String name, String otherwise){
		String rtn = getResource(name);
		if(rtn == null) return otherwise;
		return rtn;
	}

	public void waypointUpdated(){
		Iterator it = waypointViewers.iterator();
		while(it.hasNext()) ((ContextViewer)it.next()).waypointUpdate();
	}
	//register viewer of waypoint list
	public void registerViewer(ContextViewer viewer){
		waypointViewers.add(viewer);
	}
	//remove viewer of waypoint list
	public void removeViewer(ContextViewer viewer){
		waypointViewers.remove(viewer);
	}

	public void sendSetting(int index){
		settingList.pushSetting(index);
	}
	public void setSetting(int index, float value){
		settingList.pushSetting(index, value);
	}
	public void setSettingQuiet(int index, float value){
		settingList.updateSettingVal(index, value);
	}

	public void setTelemetry(int id, float value){
		telemetry.updateTelemetry(id, (double)value);
	}
	public float getTelemetry(int id){
		return (float) telemetry.getTelemetry(id);
	}
	public String getTelemetryName(int id){
		return telemetry.getTelemetryName(id);
	}
	public int getTelemetryCount(){
		return telemetry.telemetryCount();
	}
	public void onConnection(){
		sender.sendWaypointList();
	}
}
