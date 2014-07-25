package com;

import com.Dashboard;
import com.Logger;
import com.map.*;
import com.serial.*;
import com.ui.*;
import com.xml;
import com.ContextViewer;
import jssc.SerialPort;
import jssc.SerialPortException;
import java.util.Vector;
import java.util.Iterator;

public class Context{
	public Dashboard       dash;
	public AlertPanel      alert;
	public SerialSender    sender;
	public SerialParser    parser;
	public Logger          log;
	public WaypointList    waypoint;
	public boolean         connected;
	public boolean         isLogged[] = new boolean[Serial.NUM_DATA_SLOTS];
	public float           data[]     = new float  [Serial.NUM_DATA_SLOTS];

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
						SerialPort   serialPort){
		dash     = dashboard;
		alert    = alertPanel;
		sender   = serialSender;
		parser   = serialParser;
		waypoint = waypointList;
		log      = logger;
		port     = serialPort;
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
	public void updateData(int index, float value){
		if(index < 0 || index >= Serial.NUM_DATA_SLOTS) return;
		data[index] = value;
		Message msg = new Message((byte)index, value);
		sender.sendMessage(msg);
	}
}
