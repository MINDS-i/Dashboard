package com.serial;

import com.Dashboard;
import com.map.Dot;
import com.map.MapPanel;
import com.serial.Serial;
import com.ui.AlertPanel;
import com.Context;
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
					byte[] msg = Arrays.copyOfRange(buffer,
													0,
													bufPos-Serial.FOOTER.length);
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
		if(Serial.fletcher(msg, msg.length)){//checksum comes out correct
			switch(msg[0]){
				case Serial.DATA_MSG:
					handleData(msg);
					break;
				case Serial.ADD_WAYPOINT_MSG:
				case Serial.CHANGE_WAYPOINT_MSG:
				case Serial.DELETE_WAYPOINT_MSG:
				case Serial.CLEAR_WAYPOINT_MSG:
					handleWaypoint(msg);
					context.sender.sendMessage(
								new Message(Serial.fletcher16(msg,msg.length)));
					break;
				case Serial.CONFIRMATION_MSG:
					context.sender.notifyOfConfirm(
											(msg[1]&0xff)<<8 | (msg[2]&0xff) );
					break;
				case Serial.REQUEST_RESYNC:
					context.sender.sendWaypointList();
					break;
				default:
					break;
			}
		} else {
			System.err.print("ERROR: bad checksum, length " +msg.length +"\n");
		}
	}

	private void handleData(byte[] message){
		if(message.length != 8) {
			System.err.print("ERROR: bad length of data message; was " + message.length);
			return;
		}
		byte id = message[1];
		if(id > Serial.NUM_DATA_SLOTS) return;

		float tmp = (float)(((message[2]&0xff)<<24)|
							((message[3]&0xff)<<16)|
							((message[4]&0xff)<< 8)|
							((message[5]&0xff)    ) );
		tmp /= Serial.FIXED_POINT_FACTOR;

		context.data[id] = tmp;
		context.dash.updateDash(id);
	}

	private void handleWaypoint(byte[] message){
		if(message.length != 14) {
			System.err.print("ERROR: bad length of waypoint message; was " + message.length);
		}
		byte  type = message[0];
		float latitude  = (float)(((message[1]&0xff)<<24)|
								  ((message[2]&0xff)<<16)|
								  ((message[3]&0xff)<< 8)|
								  ((message[4]&0xff)    ) );
		float longitude = (float)(((message[5]&0xff)<<24)|
								  ((message[6]&0xff)<<16)|
								  ((message[7]&0xff)<< 8)|
								  ((message[8]&0xff)    ) );
		short altitude  = (short)(((message[9]&0xff)<< 8)|
								  ((message[10]&0xff)   ) );
		byte position = message[11];
		switch(type){
			case Serial.ADD_WAYPOINT_MSG:
				context.waypoint.add(longitude, latitude, position);
				break;
			case Serial.CHANGE_WAYPOINT_MSG:
				context.waypoint.get(position).setLocation(new Point.Double(longitude, latitude));
				break;
			case Serial.DELETE_WAYPOINT_MSG:
				context.waypoint.remove(position);
				break;
		}
	}
}
