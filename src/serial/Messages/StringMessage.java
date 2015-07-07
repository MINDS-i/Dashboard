package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import com.serial.Messages.*;

import java.nio.charset.StandardCharsets;

class StringMessage extends Message{
    int msgType;
    String str;
    public StringMessage(int type, String str){
        super();
        msgType = type;
        this.str = str;

        content = (" "+str).getBytes(StandardCharsets.US_ASCII);
        content[0] = Serial.buildMessageLabel(Serial.STRING_TYPE, type);
        buildChecksum();
    }
    @Override
    public boolean needsConfirm(){
        return false;
    }
    @Override
    public String toString(){
        return "Message: \""+str+"\"";
    }
}
