package com.serial.Messages;

import com.serial.Serial;
import com.serial.Messages.*;

public class WordMessage extends Message {
    int msgType;
    int subType;
    public WordMessage(int subtype, byte a, byte b) {
        super();
        msgType = subtype;

        content = new byte[3];
        content[0] = Serial.buildMessageLabel(Serial.WORD_TYPE,
                                              subtype);
        content[1]  = a;
        content[2]  = b;
        buildChecksum();

        subType = a;
    }
    public WordMessage(int subtype, int ab) {
        this(subtype, (byte)((subtype>>8)&0xff), (byte)((subtype)&0xff) );
    }
    @Override
    public boolean needsConfirm() {
        return false;
    }
    @Override
    public String toString() {
        switch(msgType) {
            case Serial.CONFIRMATION:
                return "Confirmation Message";
            case Serial.SYNC_WORD:
                return "Syncronization Message";
            case Serial.COMMAND_WORD:
                return nameCommand(subType);
        }
        return "Word Message";
    }
    private String nameCommand(int command) {
        switch (command) {
            case Serial.ESTOP_CMD:
                return "E-Stop Message";
            case Serial.TARGET_CMD:
                return "Rover Target Message";
            case Serial.LOOPING_CMD:
                return "Looping Message";
            case Serial.CLEAR_CMD:
                return "Clear Waypoints Command";
            case Serial.DELETE_CMD:
                return "Delete Waypoint Command";
        }
        return "Command Message";
    }
}
