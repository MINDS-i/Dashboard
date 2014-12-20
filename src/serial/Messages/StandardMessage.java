package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;

import java.util.Date;

public class StandardMessage extends Message{
	Serial.standardSubtype msgType;
	public StandardMessage(int tLabel, float value){
		msgType = TELEMETRY;

		int num = Float.floatToIntBits(value);

		int length	= 8;
		content		= new byte[length+2];
		content[0]	= Serial.buildMessageLabel(msgType, length);
		content[1]	= (byte) tLabel;
		content[2]  = (byte)((num>>24)&0xff);
		content[3]  = (byte)((num>>16)&0xff);
		content[4]  = (byte)((num>>8 )&0xff);
		content[5]  = (byte)((num    )&0xff);
		buildChecksum();
	}
	public StandardMessage(Serial.commands cmd){
		msgType = COMMAND;

		int length	= 2;
		content 	= new byte[length+2];
		content[0]	= Serial.buildMessageLabel(msgType, length);
		content[1]	= cmd.getValue();
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return Protocol.STANDARD_CONFIRM_REQ;
	}
	@Override
	public String describeSelf(){
		switch(msgType){
			case TELEMETRY:
				return "Telemetry message ";
				break;
			case COMMAND:
				return "Command Message ";
				break;
		}
	}
}
