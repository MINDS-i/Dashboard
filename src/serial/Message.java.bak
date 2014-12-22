package com.serial;

import com.map.Dot;
import com.serial.Serial;

import java.util.Date;

public class Message{
	byte[] 	content;
	byte 	waypointIndex;
	byte    msgType;
	boolean needsConfirm;
	int 	failCount;
	int 	checkSum;
	int 	confirmSum;
	Date 	sent;

	public Message(int sum){
		waypointIndex = 0;
		msgType       = Serial.CONFIRMATION_MSG;
		needsConfirm  = false;
		failCount	  = 0;
		content       = new byte[5];

		content[0] = Serial.CONFIRMATION_MSG;
		content[1] = (byte)(sum>>8);
		content[2] = (byte)(sum&0xff);

		checkSum   = Serial.fletcher16(content, content.length-2);
		content[3] = (byte)(checkSum>>8);
		content[4] = (byte)(checkSum&0xff);
		confirmSum = sum;
	}
	public Message(byte id, float data){
		waypointIndex = 0;
		msgType		  = Serial.DATA_MSG;
		needsConfirm  = Serial.DATA_CONFIRM_REQ;
		failCount     = 0;
		content		  = new byte[8];
		int tmpData   = (int) (data*Serial.FIXED_POINT_FACTOR);

		content[0] = Serial.DATA_MSG;
		content[1] = id;
		content[2] = (byte)((tmpData>>24)&0xff);
		content[3] = (byte)((tmpData>>16)&0xff);
		content[4] = (byte)((tmpData>>8 )&0xff);
		content[5] = (byte)((tmpData    )&0xff);

		checkSum   = Serial.fletcher16(content, content.length-2);
		content[6] = (byte)(checkSum>>8);
		content[7] = (byte)(checkSum&0xff);
		confirmSum = Serial.fletcher16(content, content.length  )&0xffff;
	}
	public Message(byte type, Dot dot, byte position){
		waypointIndex = position;
		msgType 	  = type;
		needsConfirm  = Serial.WAYPOINT_CONFIRM_REQ;
		failCount     = 0;
		content 	  = new byte[14];
		int tmpLat = (int) (dot.getLatitude() *Serial.FIXED_POINT_FACTOR);
		int tmpLon = (int) (dot.getLongitude()*Serial.FIXED_POINT_FACTOR);

		content[0]  = type;
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
		content[11] = position;

		checkSum    = Serial.fletcher16(content, content.length-2);
		content[12] = (byte)(checkSum>>8);
		content[13] = (byte)(checkSum&0xff);
		confirmSum  = Serial.fletcher16(content, content.length  )&0xffff;
	}
	public boolean needsConfirm(){
		return needsConfirm;
	}
	void sendTime(Date date){
		sent = date;
	}
	boolean isConfirmedBy(int confirmation){
		return confirmation == confirmSum;
	}
	boolean pastExpiration(Date now){
		return (now.getTime()-sent.getTime()) > Serial.MAX_CONFIRM_WAIT;
	}
	void addFailure(){
		failCount++;
	}
	int numberOfFailures(){
		return failCount;
	}
	int getConfirmSum(){
		return confirmSum;
	}
	String describeSelf(){
		switch(msgType){
			case Serial.CHANGE_WAYPOINT_MSG:
			case Serial.DELETE_WAYPOINT_MSG:
			case Serial.ADD_WAYPOINT_MSG   :
				return Serial.WAYPOINT_DESCRIPTOR + waypointIndex;
			case Serial.DATA_MSG:
				return Serial.DATA_DESCRIPTOR + Integer.toHexString(confirmSum);
			case Serial.CONFIRMATION_MSG:
				return Serial.CONFIRM_DESCRIPTOR +
												Integer.toHexString(confirmSum);
			case Serial.CLEAR_WAYPOINT_MSG:
				return Serial.CLEAR_DESCRIPTOR;
			default:
				return Serial.GENERIC_DESCRIPTOR;
		}
	}
}
