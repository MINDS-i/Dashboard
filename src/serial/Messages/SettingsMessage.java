package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import com.serial.Messages.*;

import java.util.Date;

public class SettingsMessage extends Message{
	int msgType;
	int settingIndex;
	public SettingsMessage(int index){
		super();
		msgType = Serial.POLL_SUBTYPE;
		settingIndex = index;

		int length	= 2;
		content		= new byte[length];
		content[0]	= Serial.buildMessageLabel( Serial.SETTINGS_TYPE,
												msgType, length);
		content[1]  = (byte) index;
		buildChecksum();
	}
	public SettingsMessage(int index, float value){
		super();
		msgType = Serial.SET_SUBTYPE;
		settingIndex = index;

		//throw error if index is out of bounds?

		int num = Float.floatToIntBits(value);

		int length	= 8;
		content		= new byte[length];
		content[0]	= Serial.buildMessageLabel( Serial.SETTINGS_TYPE,
												msgType, length);
		content[1]	= (byte) index;
		content[2]  = (byte)((num>>24)&0xff);
		content[3]  = (byte)((num>>16)&0xff);
		content[4]  = (byte)((num>>8 )&0xff);
		content[5]  = (byte)((num    )&0xff);
		buildChecksum();
	}
	@Override
	public boolean needsConfirm(){
		return Serial.SETTINGS_CONFIRM_REQ;
	}
	@Override
	public String toString(){
		return "setting " + settingIndex;
	}
}
