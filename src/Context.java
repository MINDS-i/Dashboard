package com;

import com.Dashboard;
import com.Logger;
import com.map.*;
import com.serial.*;
import com.serial.Messages.*;
import com.ui.*;
import com.xml;
import com.ContextViewer;
import jssc.SerialPort;
import jssc.SerialPortException;
import java.util.Vector;
import java.util.Iterator;

public class Context{
	public Dashboard	dash;
	public AlertPanel	alert;
	public SerialSender	sender;
	public SerialParser	parser;
	public Logger		log;
	public WaypointList	waypoint;
	public boolean		connected;
	public Theme		theme;
	public boolean		isLogged[]	= new boolean[Serial.MAX_TELEMETRY];
	public float		telemetry[]	= new float  [Serial.MAX_TELEMETRY];
	public float		upstreamSettings[] = new float[Serial.MAX_SETTINGS];

	private SerialPort				port;
	private Vector<ContextViewer> 	toUpdate;

	public Context(){
		connected = false;
		for(int i=0; i<8; i++) isLogged[i] = true;
		toUpdate = new Vector<ContextViewer>();
	}
	public void give( 	Dashboard    dashboard,
						AlertPanel   alertPanel,
						SerialSender serialSender,
						SerialParser serialParser,
						WaypointList waypointList,
						Logger       logger,
						SerialPort   serialPort,
						Theme 		 thm){
		dash     = dashboard;
		alert    = alertPanel;
		sender   = serialSender;
		parser   = serialParser;
		waypoint = waypointList;
		log      = logger;
		port     = serialPort;
		theme    = thm;
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
		if(port != null){
			port.closePort();
		}
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
	public void sendSetting(int index, float value){
		if(index < 0 || index >= upstreamSettings.length) return;
		upstreamSettings[index] = value;
		Message msg = new SettingsMessage((byte)index, value);
		sender.sendMessage(msg);
	}
	public void sendSetting(int index){
		Message msg = new SettingsMessage((byte)index, upstreamSettings[index]);
		sender.sendMessage(msg);
	}
	public void setSetting(int index, float value){
		if(index < 0 || index >= upstreamSettings.length) return;
		upstreamSettings[index] = value;
	}
	public void setTelemetry(int index, float value){
		if(index < 0 || index >= telemetry.length) return;
		telemetry[index] = value;
		dash.updateDash(index);
	}
	public float getTelemetry(int id){
		return telemetry[id];
	}
	public void onConnection(){
		//do something on connection
	}
}
