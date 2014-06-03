package com.serial;

import com.map.Dot;
import com.serial.Serial;

import java.util.Date;

public class Message{
	byte[] content;
	int checkSum;
	int confirmSum;
	Date sent;
	int failCount;
	boolean needsConfirm;
	byte waypointIndex;

	public Message(byte command){
		needsConfirm = Serial.COMMAND_CONFIRM_REQ;
		content = new byte[Serial.COMMAND_MSG_LENGTH];
		content[0] = command;
		checkSum = Serial.fletcher16(content, content.length-2);
		content[1] = (byte)(checkSum>>8);
		content[2] = (byte)(checkSum&0xff);
		confirmSum = Serial.fletcher16(content, content.length);
	}
	public Message(byte command, Dot dot, byte position){
		waypointIndex = position;
		needsConfirm = Serial.WAYPOINT_CONFIRM_REQ;
		content = new byte[Serial.WAYPOINT_MSG_LENGTH];
		int tmpLat = (int) (dot.getLatitude()*100000);
		int tmpLon = (int) (dot.getLongitude()*100000);
		content[0] = command;
		content[1] = (byte)((tmpLat>>24)&0xff);
		content[2] = (byte)((tmpLat>>16)&0xff);
		content[3] = (byte)((tmpLat>>8 )&0xff);
		content[4] = (byte)((tmpLat    )&0xff);
		content[5] = (byte)((tmpLon>>24)&0xff);
		content[6] = (byte)((tmpLon>>16)&0xff);
		content[7] = (byte)((tmpLon>>8 )&0xff);
		content[8] = (byte)((tmpLon    )&0xff);
		content[9] = position;
		checkSum = Serial.fletcher16(content, content.length-2);
		content[10] = (byte)(checkSum>>8);
		content[11] = (byte)(checkSum&0xff);
		confirmSum = Serial.fletcher16(content, content.length)&0xffff;
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
		switch(content.length){
			case Serial.WAYPOINT_MSG_LENGTH:
				return Serial.WAYPOINT_DESCRIPTOR + waypointIndex;
			case Serial.COMMAND_MSG_LENGTH:
				return Serial.COMMAND_DESCRIPTOR + Integer.toHexString(confirmSum);
			default:
				return Serial.GENERIC_DESCRIPTOR;
		}
	}
}
