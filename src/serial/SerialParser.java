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
import java.io.InputStream;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class SerialParser implements SerialPortEventListener{
	private Context context;
	private Decoder decoder;

	public SerialParser(Context cxt){
		context = cxt;
	}

	public void serialEvent(SerialPortEvent event){
		decoder.update();
	}

	public void updatePort(){
		InputStream serial = new SerialInputStream(context.port());
		Checksum check = new Checksum(){
		    public byte[] calc(byte[] plaintext){
		    	return Serial.fletcher16bytes(plaintext);
		    }
		    public int length() {
		    	return 2;
		    }
		};
		decoder = new Decoder(serial, Serial.HEADER, Serial.FOOTER, check);
		decoder.addPacketReader(new WaypointReader());
		decoder.addPacketReader(new DataReader());
		decoder.addPacketReader(new WordReader());
		decoder.addPacketReader(new StringReader());

		try{
			context.port().addEventListener(this);
		} catch(SerialPortException ex) {
			System.err.println(ex.getMessage());
	  		context.alert.displayMessage(ex.getMessage());
		}
	}

	private class WaypointReader implements PacketReader{
    	public int claim(byte data){
    		if(Serial.getMsgType(data) == Serial.WAYPOINT_TYPE) return 255;
    		else return -1;
    	}
		public void handle(byte[] msg){
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
	}
	private class DataReader implements PacketReader{
    	public int claim(byte data){
    		if(Serial.getMsgType(data) == Serial.DATA_TYPE) return 255;
    		else return -1;
    	}
		public void handle(byte[] msg){
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
					context.setSettingQuiet(index, data);
					break;
			}
		}
	}
	private class WordReader implements PacketReader{
    	public int claim(byte data){
    		if(Serial.getMsgType(data) == Serial.WORD_TYPE) return 255;
    		else return -1;
    	}
		public void handle(byte[] msg){
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
	}
	private class StringReader implements PacketReader{
    	public int claim(byte data){
    		if(Serial.getMsgType(data) == Serial.STRING_TYPE) return 255;
    		else return -1;
    	}
		public void handle(byte[] msg){
			int subtype = Serial.getSubtype(msg[0]);
			byte[] buff = new byte[msg.length-1];
			for(int i=1; i<msg.length; i++){
				buff[i-1] = msg[i];
			}
			String data = new String(buff, StandardCharsets.US_ASCII);
			context.alert.displayMessage("Rover: "+data);
		}
	}
}
