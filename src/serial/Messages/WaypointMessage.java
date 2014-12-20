package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;

import java.util.Date;

public class WaypointMessage extends Message{
	Serial.waypointSubtype msgType;
	byte waypointIndex;
	public WaypointMessage(Serial.waypointSubtype type, byte index, Dot dot){
		msgType = type;
		waypointIndex = index;

		int lat = Float.floatToIntBits(dot.getLatitude() );
		int lon = Float.floatToIntBits(dot.getLongitude());

		int length = 12;
		content = new byte[length+2];
		content[0] = Serial.buildMessageLabel(msgType, length);
		content[1]  = (byte)((tmpLat>>24)&0xff);
		content[2]  = (byte)((tmpLat>>16)&0xff);
		content[3]  = (byte)((tmpLat>>8 )&0xff);
		content[4]  = (byte)((tmpLat    )&0xff);
		content[5]  = (byte)((tmpLon>>24)&0xff);
		content[6]  = (byte)((tmpLon>>16)&0xff);
		content[7]  = (byte)((tmpLon>>8 )&0xff);
		content[8]  = (byte)((tmpLon    )&0xff);
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
	public String describeSelf(){
		return "Waypoint " + waypointIndex;
	}
}
