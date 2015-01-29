package com.serial;

import com.Dashboard;
import com.map.Dot;
import com.map.MapPanel;
import com.serial.Serial;
import com.ui.AlertPanel;
import com.Context;
import com.serial.*;
import com.serial.Messages.*;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortEventListener;
import jssc.SerialPortEvent;
import java.util.Arrays;

import java.awt.*;

public class SerialParser implements SerialPortEventListener{
	Dashboard parent;
	SerialSender sender;
	byte[] buffer;
	byte bufPos = 0;
	Context context;
	static int BUFLEN = 25;

	public SerialParser(Context cxt){
		context = cxt;
		buffer = new byte[BUFLEN];
	}

	public void updatePort(){
		try{
			context.port().addEventListener(this);
		} catch(SerialPortException ex) {
			System.err.println(ex.getMessage());
      		context.alert.displayMessage(ex.getMessage());
		}
	}

	synchronized public void serialEvent(SerialPortEvent serialEvent){
		if(context.port() == null) return;
		try{
			byte tmp;
			while(context.port().getInputBufferBytesCount()>0){
				tmp = (byte)context.port().readBytes(1)[0];
				buffer[bufPos] = tmp;
				bufPos++;
				if(bufPos >= BUFLEN) bufPos = 0;
				else if(rightMatch(buffer, bufPos, Serial.HEADER)) bufPos = 0;
				else if(rightMatch(buffer, bufPos, Serial.FOOTER)) {
					byte[] msg = Arrays.copyOfRange(
								buffer, 0, bufPos-Serial.FOOTER.length);
					checkBuffer(msg);
				}
			}
		} catch(SerialPortException ex) {
			System.err.println(ex.getMessage());
			context.alert.displayMessage(ex.getMessage());
		}
	}

	private boolean rightMatch(byte[] buf, int len, byte[] match){
		if(match.length > len) return false;
		for(int i=0; i < match.length; i++){
			if(buf[len-i-1]!=match[match.length-i-1]) return false;
		}
		return true;
	}

	private void checkBuffer(byte[] msg){
		if(msg.length == 0) return;
		int type = Serial.getMsgType(msg[0]);
		if(!Serial.fletcher(msg, msg.length)){
			System.err.print("ERROR: bad sum, length "
								+ msg.length
								+ " type "
								+ type
								+ " subtype "
								+ Serial.getSubtype(msg[0])
								+"\n");
			return;
		}
		System.err.print("Received length "
								+ msg.length
								+ " type "
								+ type
								+ " subtype "
								+ Serial.getSubtype(msg[0])
								+"\n");
		switch(type){
			case Serial.STANDARD_TYPE:
				handleStandard(msg);
				break;
			case Serial.SETTINGS_TYPE:
				handleSetting(msg);
				break;
			case Serial.WAYPOINT_TYPE:
				handleWaypoint(msg);
				break;
			case Serial.PROTOCOL_TYPE:
				handleProtocol(msg);
				break;
			default:
				break;
		}
	}

	private void handleStandard(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		switch(subtype){
			case Serial.TELEMETRY_SUBTYPE:
				int index = msg[1];
				int tmp = (	((msg[2]&0xff)<<24)|
							((msg[3]&0xff)<<16)|
							((msg[4]&0xff)<< 8)|
							((msg[5]&0xff)    ) );
				float data = Float.intBitsToFloat(tmp);
				context.setTelemetry(index, data);
				break;
			case Serial.COMMAND_SUBTYPE:
				switch(msg[1]){
					case Serial.TARGET_CMD:
						//set dash target to msg[2]
						context.waypoint.updateTarget(msg[2]);
						break;
					default:
						break;
				}
				break;
		}
	}

	private void handleProtocol(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		switch(subtype){
			case Serial.SYNC_SUBTYPE:
				Message message = new ProtocolMessage(Serial.SYNC_RESP_SUBTYPE);
				context.sender.sendMessage(message);
				context.onConnection();
				break;
			case Serial.SYNC_RESP_SUBTYPE:
				context.onConnection();
				break;
			case Serial.CONFIRM_SUBTYPE:
				int confirmation = ((msg[1]&0xff)<<8) | (msg[2]&0xff);
				context.sender.notifyOfConfirm(confirmation);
				break;
		}
	}

	private void handleSetting(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		switch(subtype){
			case Serial.SET_SUBTYPE:
				int index = msg[1];
				int tmp = (	((msg[2]&0xff)<<24)|
							((msg[3]&0xff)<<16)|
							((msg[4]&0xff)<< 8)|
							((msg[5]&0xff)    ) );
				float data = Float.intBitsToFloat(tmp);
				context.inputSetting(index, data);
				break;
			case Serial.POLL_SUBTYPE:
				context.sendSetting(msg[1]);
				break;
		}
	}

	private void handleWaypoint(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		int tmpLat = (	((msg[1]&0xff)<<24)|
						((msg[2]&0xff)<<16)|
						((msg[3]&0xff)<< 8)|
						((msg[4]&0xff)    ) );
		int tmpLon = (	((msg[5]&0xff)<<24)|
						((msg[6]&0xff)<<16)|
						((msg[7]&0xff)<< 8)|
						((msg[8]&0xff)    ) );
		int index = msg[11];
		//msg 9, 10 are altitude
		float latitude	= Float.intBitsToFloat(tmpLat);
		float longitude	= Float.intBitsToFloat(tmpLon);
		switch(subtype){
			case Serial.ADD_SUBTYPE:
				context.waypoint.add(longitude, latitude, index);
				break;
			case Serial.ALTER_SUBTYPE:
				context.waypoint.get(index).setLocation(
								new Point.Double(longitude, latitude));
				break;
			case Serial.DELETE_SUBTYPE:
				context.waypoint.remove(index);
				break;
		}
	}
}
