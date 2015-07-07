package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import com.serial.Messages.*;

import java.util.Date;

public class StandardMessage extends Message{
	int msgType;
	public StandardMessage(int tLabel, float value){
		super();
		msgType = Serial.TELEMETRY_SUBTYPE;

		int num = Float.floatToIntBits(value);

		int length	= 8;
		content		= new byte[length];
		content[0]	= Serial.buildMessageLabel(Serial.STANDARD_TYPE,
													msgType, length);
		content[1]	= (byte) tLabel;
		content[2]  = (byte)((num>>24)&0xff);
		content[3]  = (byte)((num>>16)&0xff);
		content[4]  = (byte)((num>>8 )&0xff);
		content[5]  = (byte)((num    )&0xff);
		buildChecksum();
	}
	public StandardMessage(int cmd, byte data){
		super();
		msgType = Serial.COMMAND_SUBTYPE;

		int length	= 3;
		content 	= new byte[length];
		content[0]	= Serial.buildMessageLabel(Serial.STANDARD_TYPE,
													msgType, length);
		content[1]	= (byte) cmd;
		content[2]	= data;
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return Serial.STANDARD_CONFIRM_REQ;
	}
	@Override
	public String toString(){
		switch(msgType){
			case Serial.TELEMETRY_SUBTYPE:
				return "Telemetry message ";
			case Serial.COMMAND_SUBTYPE:
				return "Command Message ";
		}
		return "Bad Standard Message";
	}
}
