package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import com.serial.Messages.*;

import java.util.Date;

public class WaypointMessage extends Message{
	int msgType;
	byte waypointIndex;
	public WaypointMessage(int type, byte index, Dot dot){
		msgType = type;
		waypointIndex = index;

		int lat = Float.floatToIntBits((float)dot.getLatitude() );
		int lon = Float.floatToIntBits((float)dot.getLongitude());

		int length = 12;
		content = new byte[length+2];
		content[0] = Serial.buildMessageLabel( Serial.WAYPOINT_TYPE,
												msgType, length);
		content[1]  = (byte)((lat>>24)&0xff);
		content[2]  = (byte)((lat>>16)&0xff);
		content[3]  = (byte)((lat>>8 )&0xff);
		content[4]  = (byte)((lat    )&0xff);
		content[5]  = (byte)((lon>>24)&0xff);
		content[6]  = (byte)((lon>>16)&0xff);
		content[7]  = (byte)((lon>>8 )&0xff);
		content[8]  = (byte)((lon    )&0xff);
		content[9]  = (byte)((dot.getAltitude()>>8)&0xff);
		content[10] = (byte)((dot.getAltitude()   )&0xff);
		content[11] = index;
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return Serial.WAYPOINT_CONFIRM_REQ;
	}
	@Override
	public String toString(){
		return "Waypoint " + waypointIndex;
	}
}
