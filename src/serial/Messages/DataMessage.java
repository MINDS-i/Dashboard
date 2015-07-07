package com.serial.Messages;

import com.serial.Serial;
import com.serial.Messages.*;

class DataMessage extends Message{
    int msgType;
    public DataMessage(int type, byte index, float data){
        super();
        msgType = type;

        int idata   = Float.floatToIntBits(data);

        content     = new byte[6];
        content[0]  = Serial.buildMessageLabel(Serial.WAYPOINT_TYPE, type);
        content[1]  = (byte) index;
        content[2]  = (byte)((idata>>24)&0xff);
        content[3]  = (byte)((idata>>16)&0xff);
        content[4]  = (byte)((idata>>8 )&0xff);
        content[5]  = (byte)((idata    )&0xff);

        buildChecksum();
    }
    @Override
    public boolean needsConfirm(){
        return false;
    }
    @Override
    public String toString(){
        return "data message";
    }
}
