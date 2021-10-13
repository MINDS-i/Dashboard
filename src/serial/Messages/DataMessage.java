package com.serial.Messages;

import com.serial.Serial;
import com.serial.Messages.*;

class DataMessage extends Message {
    int msgType;
    public DataMessage(int type, byte index, double data) {
        super();
        msgType = type;

        long idata   = Double.doubleToLongBits(data);

        content     = new byte[6];
        content[0]  = Serial.buildMessageLabel(Serial.DATA_TYPE, type);
        content[1]  = (byte) index;
        content[2]  = (byte)((idata>>24)&0xff);
        content[3]  = (byte)((idata>>16)&0xff);
        content[4]  = (byte)((idata>>8 )&0xff);
        content[5]  = (byte)((idata    )&0xff);

        buildChecksum();
    }
    
    @Override
    public boolean needsConfirm() {
        return (msgType == Serial.SETTING_DATA);
    }
    
    @Override
    public String toString() {
        switch(msgType) {
            case Serial.TELEMETRY_DATA:
                return "Telemetry Message";
            case Serial.SETTING_DATA:
                return "Settings Change";
            case Serial.SENSOR_DATA:
            	return "Sensor Message";
            case Serial.INFO_DATA:
            	return "Info Message";
        }
        return "Data Message";
    }
}
