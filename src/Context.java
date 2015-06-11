package com;

import com.ContextViewer;
import com.Dashboard;
import com.map.*;
import com.serial.*;
import com.serial.Messages.*;
import com.ui.*;
import com.xml;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
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

	//public boolean		isLogged[]	= new boolean[Serial.MAX_TELEMETRY];
	//public float		telemetry[]	= new float  [Serial.MAX_TELEMETRY];

	private SerialPort				port;
	private Vector<ContextViewer> 	toUpdate;

	public Context(){
		connected = false;
		toUpdate = new Vector<ContextViewer>();
	}
	public void give( 	Dashboard    dashboard,
						AlertPanel   alertPanel,
						SerialSender serialSender,
						SerialParser serialParser,
						WaypointList waypointList,
						SerialPort   serialPort,
						Locale 		 loc){
		dash      = dashboard;
		alert     = alertPanel;
		sender    = serialSender;
		parser    = serialParser;
		waypoint  = waypointList;
		port      = serialPort;
		locale	  = loc;
		theme     = new Theme(locale);
		telemetry = new TelemetryManager();
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
		Message msg = new SettingsMessage((byte)index, upstreamSettings[index]);
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
	public void onConnection(){
		sender.sendWaypointList();
	}
}
