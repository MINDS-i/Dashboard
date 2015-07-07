package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import jssc.SerialPort;
import jssc.SerialPortException;

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
	public void sendTime(Date date){
		sent = date;
	}
	public boolean isConfirmedBy(int confirmation){
		return ((short)confirmation == (short)confirmSum);
	}
	public boolean isPastExpiration(Date now){
		return (now.getTime()-sent.getTime()) > Serial.MAX_CONFIRM_WAIT;
	}
	public void addFailure(){
		failCount++;
	}
	public int numberOfFailures(){
		return failCount;
	}
	public int getConfirmSum(){
		return confirmSum;
	}
	public void send(SerialPort port) throws SerialPortException {
		port.writeBytes(Serial.HEADER);
		port.writeBytes(content);
		port.writeBytes(Serial.FOOTER);
		sent = new Date();
	}
	protected void buildChecksum(){
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
	@Override
	public String toString(){
		return "A message";
	}
}
