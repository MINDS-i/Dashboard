package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import com.serial.Messages.*;

import java.util.Date;

public class StandardMessage extends Message{
	int msgType;
	public StandardMessage(int tLabel, float value){
		msgType = Serial.TELEMETRY_SUBTYPE;

		int num = Float.floatToIntBits(value);

		int length	= 8;
		content		= new byte[length+2];
		content[0]	= Serial.buildMessageLabel(Serial.STANDARD_TYPE,
													msgType, length);
		content[1]	= (byte) tLabel;
		content[2]  = (byte)((num>>24)&0xff);
		content[3]  = (byte)((num>>16)&0xff);
		content[4]  = (byte)((num>>8 )&0xff);
		content[5]  = (byte)((num    )&0xff);
		buildChecksum();
	}
	public StandardMessage(int cmd){
		msgType = Serial.COMMAND_SUBTYPE;

		int length	= 2;
		content 	= new byte[length+2];
		content[0]	= Serial.buildMessageLabel(Serial.STANDARD_TYPE,
													msgType, length);
		content[1]	= (byte) cmd;
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return Serial.STANDARD_CONFIRM_REQ;
	}
	@Override
	public String describeSelf(){
		switch(msgType){
			case Serial.TELEMETRY_SUBTYPE:
				return "Telemetry message ";
			case Serial.COMMAND_SUBTYPE:
				return "Command Message ";
		}
		return "Error";
	}
}
