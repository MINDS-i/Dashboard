package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;

import java.util.Date;

public class ProtocolMessage extends Message{
	Serial.protocolSubtype msgType;
	public ProtocolMessage(int confirmationSum){
		msgType = CONFIRM;

		int length	= 3;
		content		= new byte[length+2];
		content[0]	= Serial.buildMessageLabel(msgType, length);
		content[1]	= (byte)((confirmationSum >> 8) & 0xff);
		content[2]	= (byte)((confirmationSum     ) & 0xff);
		buildChecksum();
	}
	public ProtocolMessage(){ //sync message
		msgType = SYNC;

		int length	= 1;
		content 	= new byte[length+2];
		content[0]	= Serial.buildMessageLabel(msgType, length);
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return false;
	}
	@Override
	public String describeSelf(){
		switch(msgType){
			case SYNC:
				return "Sync Message ";
				break;
			case CONFIRM:
				return "Confirmation ";
				break;
		}
	}
}
