package com.serial;

import jssc.SerialPort;

public class Serial {
    public static final int WAYPOINT_TYPE = 0x0;
    public static final int DATA_TYPE = 0x1;
    public static final int WORD_TYPE = 0x2;
    public static final int STRING_TYPE = 0x3;

    //Waypoint type
    public static final int ADD_WAYPOINT = 0x0;
    public static final int ALTER_WAYPOINT = 0x1;

    //Data type
    public static final int TELEMETRY_DATA = 0x0;
    public static final int SETTING_DATA = 0x1;
    public static final int SENSOR_DATA = 0x2;
    public static final int INFO_DATA = 0x3;

    //Sensor Types
    public static final int OBJDETECT_SONIC = 0x0;
    public static final int OBJDETECT_BUMPER = 0x1;

    //Bumper Sub Types
    public static final int BUMPER_BUTTON_LEFT = 0x0;
    public static final int BUMPER_BUTTON_RIGHT = 0x1;

    //Info Types
    public static final int APM_VERSION = 0x0;
    public static final int HEARTBEAT = 0x1;

    //Word type
    public static final int CONFIRMATION = 0x0;
    public static final int SYNC_WORD = 0x1;
    public static final int COMMAND_WORD = 0x2;
    public static final int STATE_WORD = 0x3;

    //String type
    public static final int ERROR_STRING = 0x0;
    public static final int STATE_STRING = 0x1;

    //Commands
    public static final byte ESTOP_CMD = 0x0;
    public static final byte TARGET_CMD = 0x1;
    public static final byte LOOPING_CMD = 0x2;
    public static final byte CLEAR_CMD = 0x3;
    public static final byte DELETE_CMD = 0x4;
    public static final byte STOP_CMD = 0x5;
    public static final byte START_CMD = 0x6;
    public static final byte DISABLE_BUMPER_CMD = 0x7;
    public static final byte ENABLE_BUMPER_CMD = 0x8;
    public static final byte SETTINGS_RESET_CMD = 0x9;

    //State types
    public static final byte APM_STATE = 0x0;
    public static final byte DRIVE_STATE = 0x1;
    public static final byte AUTO_STATE = 0x2;
    public static final byte AUTO_FLAGS = 0x3;
    public static final byte GPS_STATE = 0x4;

    //APM state sub values
    public static final byte APM_STATE_INIT = 0x1;
    public static final byte APM_STATE_SELF_TEST = 0x2;
    public static final byte APM_STATE_DRIVE = 0x3;

    //Drive state sub values
    public static final byte DRIVE_STATE_INVALID = 0x0;
    public static final byte DRIVE_STATE_STOP = 0x1;
    public static final byte DRIVE_STATE_AUTO = 0x2;
    public static final byte DRIVE_STATE_RADIO = 0x3;
    public static final byte DRIVE_STATE_LOW_VOLTAGE_STOP = 0x4;
    public static final byte DRIVE_STATE_LOW_VOLTAGE_RESTART = 0x5;
    public static final byte DRIVE_STATE_RADIO_FAILSAFE = 0x6;


    //Auto state sub values
    public static final byte AUTO_STATE_FULL = 0x1;
    public static final byte AUTO_STATE_AVOID = 0x2;
    public static final byte AUTO_STATE_STALLED = 0x3;

    //Auto flags sub values
    public static final byte AUTO_STATE_FLAGS_NONE = 0B00;
    public static final byte AUTO_STATE_FLAGS_CAUTION = 0B01;
    public static final byte AUTO_STATE_FLAGS_APPROACH = 0B10;
    public static final byte AUTO_STATE_FLAGS_TURNAROUND = 0B100;

    //Sync
    public static final byte SYNC_REQUEST = 0x00;
    public static final byte SYNC_RESPOND = 0x01;

    //Telemetry IDs
    public static final int LATITUDE = 0;
    public static final int LONGITUDE = 1;
    public static final int HEADING = 2;
    public static final int PITCH = 3;
    public static final int ROLL = 4;
    public static final int SPEED = 5;
    public static final int VOLTAGE = 6;
    public static final int AMPERAGE = 7;
    public static final int ALTITUDE = 8;
    public static final int RDTHROTTLE = 9;
    public static final int RDPITCH = 10;
    public static final int RDROLL = 11;
    public static final int RDYAW = 12;
    public static final int RDSWITCH = 13;
    public static final int RDAUX2 = 14;
    public static final int HOMELATITUDE = 15;
    public static final int HOMELONGITUDE = 16;
    public static final int HOMEALTITUDE = 17;
    public static final int DELTAALTITUDE = 18;
    public static final int GPSNUMSAT = 19;
    public static final int GPSHDOP = 20;
    public static final int HEADING_LOCK = 21;

    public static final int MAX_WAYPOINTS = 64;
    public static final int MAX_SETTINGS = 64;
    public static final int MAX_TELEMETRY = 256;

    public static final int BAUD = SerialPort.BAUDRATE_57600;
    public static final int U16_FIXED_POINT = 256;

    public static final int MAX_CONFIRM_WAIT_MS = 400;
    public static final int MAX_FAILURES = 2;

    public static final byte[] HEADER = {0x13, 0x37};
    public static final byte[] FOOTER = {(byte) 0x9A};

    public static byte[] fletcher16bytes(byte[] message, int length) {
        int iterator = 0;
        int aSum = 0xff;
        int bSum = 0xff;
        short tmp;

        while (length > 0) {
            int tlen = (length > 359) ? 359 : length;
            length -= tlen;

            do {
                bSum += aSum += (message[iterator++] & 0xff);
            } while (--tlen > 0);

            aSum = (aSum & 0xff) + (aSum >> 8);
            bSum = (bSum & 0xff) + (bSum >> 8);
        }

        aSum = (aSum & 0xff) + (aSum >> 8);
        bSum = (bSum & 0xff) + (bSum >> 8);

        return new byte[]{(byte) bSum, (byte) aSum};
    }

    public static byte[] fletcher16bytes(byte[] message) {
        return fletcher16bytes(message, message.length);
    }

    public static int fletcher16(byte[] message) {
        byte[] sum = fletcher16bytes(message);
        return ((sum[0] & 0xff) << 8) | (sum[1] & 0xff);
    }

    public static int getMsgType(byte input) {
        return (input & 0x0F);
    }

    public static int getSubtype(byte input) {
        return ((input >> 4) & 0x0F);
    }

    //deprecated
    public static byte buildMessageLabel(int type, int subType, int length) {
        return buildMessageLabel(type, subType);
    }

    public static byte buildMessageLabel(int type, int subType) {
        if ((type > 0xF) || (subType > 0xF)) {
            return 0;
        }

        return (byte) ((subType << 4) | type);
    }

    public static byte buildMessageLabel(int label) {
        return (byte) label;
    }
}
