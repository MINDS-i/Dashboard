package com;

import com.ContextViewer;
import com.Dashboard;
import com.map.*;
import com.serial.*;
import com.serial.Messages.*;
import com.ui.*;
import com.xml;
import java.io.*;
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
	public float		upstreamSettings[] = new float[Serial.MAX_SETTINGS];


	public TelemetryManager telemetry;

	private SerialPort				port;
	private Vector<ContextViewer> 	toUpdate;

	private static String propertiesFile = "./data/persist.properties";
	private Properties props;

	public Context(){
		connected = false;
		toUpdate  = new Vector<ContextViewer>();

		props = new Properties();
		FileInputStream file = null;
		try{
			file = new FileInputStream(propertiesFile);
			props.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file != null) file.close();
			} catch (Exception e) {}
		}
		loadLocale();
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
		FileOutputStream file = null;
		try{
			file = new FileOutputStream(propertiesFile);
			props.store(file, " no comment ");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file != null) file.close();
			} catch (Exception e) {}
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
		dash      = dashboard;
		alert     = alertPanel;
		sender    = serialSender;
		parser    = serialParser;
		waypoint  = waypointList;
		port      = serialPort;
		theme     = new Theme(locale);
		telemetry = new TelemetryManager(this);
		if(alertPanel != null) alertPanel.setTheme(theme);
	}
	public void updatePort(SerialPort newPort) throws SerialPortException{
		closePort();
		port = newPort;
		sender.start();
		parser.updatePort();
		connected = true;
	}
	public void closePort() throws SerialPortException{
		sender.stop();
		final SerialPort portToClose = port;
		Runnable close = new Runnable(){
			public void run(){
				try{
					portToClose.closePort();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		if(port != null)
			(new Thread(close)).start();

		port = null;
		connected = false;
	}
	public SerialPort port(){
		return port;
	}
	public void waypointUpdated(){
		Iterator it = toUpdate.iterator();
		while(it.hasNext()) ((ContextViewer)it.next()).waypointUpdate();
	}
	public void registerViewer(ContextViewer viewer){
		toUpdate.add(viewer);
	}
	public void removeViewer(ContextViewer viewer){
		toUpdate.remove(viewer);
	}
	public void sendSetting(int index){
		Message msg = Message.setSetting((byte)index, upstreamSettings[index]);
		sender.sendMessage(msg);
	}
	public void setSetting(int index, float value){
		if(index < 0 || index >= upstreamSettings.length) return;
		upstreamSettings[index] = value;
		sendSetting(index);
	}
	public void inputSetting(int index, float value){
		if(index < 0 || index >= upstreamSettings.length) return;
		upstreamSettings[index] = value;
	}
	public void setSettingQuiet(int index, float value){
		if(index < 0 || index >= upstreamSettings.length) return;
		upstreamSettings[index] = value;
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
