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
import java.nio.charset.StandardCharsets;

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
			System.err.print("[");
			for(int i=0; i<msg.length; i++){
				System.err.print(  Integer.toHexString(msg[i]) );
				System.err.print(",");
			}
			System.err.println("]");
			return;
		}
		switch(type){
			case Serial.WAYPOINT_TYPE:
				handleWaypoint(msg);
				break;
			case Serial.DATA_TYPE:
				handleData(msg);
				break;
			case Serial.WORD_TYPE:
				handleWord(msg);
				break;
			case Serial.STRING_TYPE:
				handleString(msg);
				break;
			default:
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
			case Serial.ADD_WAYPOINT:
				context.waypoint.add(longitude, latitude, index);
				break;
			case Serial.ALTER_WAYPOINT:
				context.waypoint.get(index).setLocation(
								new Point.Double(longitude, latitude));
				break;
		}
	}
	private void handleData(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		int index   = msg[1];
		int tmpdata = (	((msg[2]&0xff)<<24)|
						((msg[3]&0xff)<<16)|
						((msg[4]&0xff)<< 8)|
						((msg[5]&0xff)    ) );
		float data  = Float.intBitsToFloat(tmpdata);
		switch(subtype){
			case Serial.TELEMETRY_DATA:
				context.setTelemetry(index, data);
				break;
			case Serial.SETTING_DATA:
				context.inputSetting(index, data);
				break;
		}
	}
	private void handleWord(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		byte a = (byte)(msg[1]&0xff);
		byte b = (byte)(msg[2]&0xff);
		int join = ( ((int)(a<<8)&0xFF00) | ((int)(b&0xFF)) );
		switch(subtype){
			case Serial.CONFIRMATION:
				context.sender.notifyOfConfirm(join);
				break;
			case Serial.SYNC_WORD:{
					if(a == Serial.SYNC_REQUEST){
						Message message = Message.syncMessage(Serial.SYNC_RESPOND);
						context.sender.sendMessage(message);
						context.onConnection();
					} else if (a == Serial.SYNC_RESPOND) { //resync seen
						context.onConnection();
					}
				}
				break;
			case Serial.COMMAND_WORD:
				if(a == Serial.TARGET_CMD){
					context.waypoint.updateTarget(b);
				}
				break;
		}
	}
	private void handleString(byte[] msg){
		int subtype = Serial.getSubtype(msg[0]);
		byte[] buff = new byte[msg.length-3];
		for(int i=1; i<msg.length-2; i++){
			buff[i-1] = msg[i];
		}
		String data = new String(buff, StandardCharsets.US_ASCII);
		context.alert.displayMessage(data);
	}
}
