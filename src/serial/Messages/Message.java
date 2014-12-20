package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;

import java.util.Date;

public abstract class Message{
	byte[]	content;
	int		failCount;
	int		checkSum;
	int		confirmSum;
	Date	sent;
	public Message(){
		failCount	= 0;
		checkSum	= 0;
		confirmSum	= 0;
	}
	void sendTime(Date date){
		sent = date;
	}
	boolean isConfirmedBy(int confirmation){
		return confirmation == confirmSum;
	}
	boolean isPastExpiration(Date now){
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
	private void buildChecksum(){
		int pos = content.length - 2;
		checkSum		= Serial.fletcher16(content, pos);
		content[pos+0]	= (byte)(checkSum>>8);
		content[pos+1]	= (byte)(checkSum&0xff);
		confirmSum  	= Serial.fletcher16(content, content.length  )&0xffff;
	}
	//these should be overridden
	public boolean needsConfirm(){
		return false;
	}
	String describeSelf(){
		return "A message";
	}
}
