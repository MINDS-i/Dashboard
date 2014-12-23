package com.serial;

import jssc.SerialPort;

public class Serial{
	public static final int STANDARD_TYPE = 0;
	public static final int SETTINGS_TYPE = 1;
	public static final int WAYPOINT_TYPE = 2;
	public static final int PROTOCOL_TYPE = 3;
	//standard type
	public static final int TELEMETRY_SUBTYPE = 0;
	public static final int COMMAND_SUBTYPE   = 1;
	//waypoint type
	public static final int ADD_SUBTYPE    = 0;
	public static final int ALTER_SUBTYPE  = 1;
	public static final int DELETE_SUBTYPE = 2;
	//settings type
	public static final int SET_SUBTYPE  = 0;
	public static final int POLL_SUBTYPE = 1;
	//protocol type
	public static final int SYNC_SUBTYPE      = 0;
	public static final int CONFIRM_SUBTYPE   = 1;
	public static final int SYNC_RESP_SUBTYPE = 2;
	//telemetry tags
	public static final int LATITUDE	= 0;
	public static final int LONGITUDE	= 1;
	public static final int HEADING		= 2;
	public static final int PITCH		= 3;
	public static final int ROLL		= 4;
	public static final int SPEED		= 5;
	public static final int VOLTAGE		= 6;
	//commands
	public static final int ESTOP_CMD	= 0;
	public static final int TARGET_CMD	= 1;
	public static final int LOOPING_CMD	= 2;
	public static final int CLEAR_CMD	= 3;

	public static final int   MAX_WAYPOINTS		= 64;
	public static final int   MAX_SETTINGS		= 32;
	public static final int   MAX_TELEMETRY		= 8;
	public static final int   BAUD				= SerialPort.BAUDRATE_9600;

	public static final int   MAX_CONFIRM_WAIT	= 2000; //in milliseconds
	public static final int   MAX_FAILURES		= 6;

	public static final boolean STANDARD_CONFIRM_REQ	= false;
	public static final boolean SETTINGS_CONFIRM_REQ	= true;
	public static final boolean WAYPOINT_CONFIRM_REQ	= true;

	public static final byte[] HEADER = {0x13, 0x37};
	public static final byte[] FOOTER = {0x7A };

	public static final String WAYPOINT_DESCRIPTOR = "Waypoint ";
	public static final String     DATA_DESCRIPTOR = "Data Msg ";
	public static final String    CLEAR_DESCRIPTOR = "Clear List Command ";
	public static final String  CONFIRM_DESCRIPTOR = "Msg Confirmation ";
	public static final String  GENERIC_DESCRIPTOR = "Message ";

	public static int fletcher16( byte[] message, int length){
		int iterator = 0;
		int aSum=0xff, bSum=0xff;
		short tmp;
		while(length > 0){
			int tlen = length > 359 ? 359 : length;
			length -= tlen;
			do{
				bSum += aSum += (message[iterator++]&0xff);
			} while (--tlen >0);
			aSum = (aSum & 0xff) + (aSum >> 8);
			bSum = (bSum & 0xff) + (bSum >> 8);
		}
		aSum = (aSum & 0xff) + (aSum >> 8);
		bSum = (bSum & 0xff) + (bSum >> 8);
		return (int) ( ((bSum)<<8)|(aSum) );
	}

	public static boolean fletcher(byte[] message, int length){
		if(length <= 2) return false;
		int foundSum = ( ( (message[length-2]&0xff) <<8) | (message[length-1]&0xff) );
		int generatedSum = fletcher16(message, length-2);
		return generatedSum == foundSum;
	}

	public static int getMsgType(byte input){
		return (input&0x03);
	}
	public static int getSubtype(byte input){
		return ((input>>2)&0x03);
	}
	public static byte buildMessageLabel(int type, int subType, int length){
		if(length	> 0xf) return 0;
		if(type 	> 0x3) return 0;
		if(subType	> 0x3) return 0;
		return (byte) ((length<<4)|(subType<<2)|type);
	}
}
