package com.serial.Messages;

import com.serial.Serial;
import com.serial.Messages.*;

public class WordMessage extends Message{
	int msgType;
	public WordMessage(int subtype, byte a, byte b){
		super();
		msgType = subtype;

		content = new byte[3];
		content[0] = Serial.buildMessageLabel(Serial.WORD_TYPE,
											  subtype);
		content[1]  = a;
		content[2]  = b;
		buildChecksum();
	}
	public WordMessage(int subtype, int ab){
		this(subtype, (byte)((subtype>>8)&0xff), (byte)((subtype)&0xff) );
	}
	@Override
	public boolean needsConfirm(){
		return Serial.WAYPOINT_CONFIRM_REQ;
	}
	@Override
	public String toString(){
		return "word message ";
	}
}
