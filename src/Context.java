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
	private Vector<ContextViewer> 	toUpdate;

	private static String propertiesFile = "./data/persist.properties";
	private Properties props;

	public Context(){
		connected = false;
		toUpdate  = new Vector<ContextViewer>();

		props = new Properties();
		try(FileInputStream file = new FileInputStream(propertiesFile)){
			props.load(file);
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
		String current = (String) props.get("subject");
		if(current.equals("air")){
			props.put("subject","ground");
		} else {
			props.put("subject","air");
		}
		saveProps();
	}
	private void saveProps(){
		try(FileOutputStream file = new FileOutputStream(propertiesFile)){
			props.store(file, "no comment");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void loadLocale(){
		if (!props.containsKey("subject")) {
			props.put("subject","air");
		}
		String sub = (String) props.get("subject");
		locale = new Locale("en","US",sub);
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
		theme       = new Theme(locale);
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
	public Path getSettingsDescriptionFile() throws MissingResourceException {
		ResourceBundle res = ResourceBundle.getBundle("resources", locale);
		return Paths.get(res.getString("settings_spec"));
	}

	public void waypointUpdated(){
		Iterator it = toUpdate.iterator();
		while(it.hasNext()) ((ContextViewer)it.next()).waypointUpdate();
	}
	//register viewer of waypoint list
	public void registerViewer(ContextViewer viewer){
		toUpdate.add(viewer);
	}
	//remove viewer of waypoint list
	public void removeViewer(ContextViewer viewer){
		toUpdate.remove(viewer);
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
