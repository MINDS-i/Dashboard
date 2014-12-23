package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import com.serial.Messages.*;

import java.util.Date;

public class ProtocolMessage extends Message{
	int msgType;
	public ProtocolMessage(int type, int confirmationSum){
		// for now this will ignore type
		msgType = Serial.CONFIRM_SUBTYPE;

		int length	= 3;
		content		= new byte[length+2];
		content[0]	= Serial.buildMessageLabel( Serial.PROTOCOL_TYPE,
												msgType, length);
		content[1]	= (byte)((confirmationSum >> 8) & 0xff);
		content[2]	= (byte)((confirmationSum     ) & 0xff);
		buildChecksum();
	}
	public ProtocolMessage(int type){ //sync message
		msgType = type;

		int length	= 1;
		content 	= new byte[length+2];
		content[0]	= Serial.buildMessageLabel( Serial.PROTOCOL_TYPE,
												msgType, length);
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return false;
	}
	@Override
	public String describeSelf(){
		switch(msgType){
			case Serial.SYNC_SUBTYPE:
				return "Sync Message ";
			case Serial.CONFIRM_SUBTYPE:
				return "Confirmation ";
			case Serial.SYNC_RESP_SUBTYPE:
				return "Sync Response ";
		}
		return "Bad Protocol Message";
	}
}
