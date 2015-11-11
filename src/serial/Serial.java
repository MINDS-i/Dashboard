package com.serial;

import jssc.SerialPort;

public class Serial{
	public static final int WAYPOINT_TYPE  = 0x0;
	public static final int DATA_TYPE      = 0x1;
	public static final int WORD_TYPE      = 0x2;
	public static final int STRING_TYPE    = 0x3;
	//waypoint type
	public static final int ADD_WAYPOINT   = 0x0;
	public static final int ALTER_WAYPOINT = 0x1;
	//data type
	public static final int TELEMETRY_DATA = 0x0;
	public static final int SETTING_DATA   = 0x1;
	//word type
	public static final int CONFIRMATION   = 0x0;
	public static final int SYNC_WORD      = 0x1;
	public static final int COMMAND_WORD   = 0x2;
	//string type
	public static final int ERROR_STRING   = 0x0;
	public static final int STATE_STRING   = 0x1;
	//commands
	public static final byte ESTOP_CMD	  = 0x0;
	public static final byte TARGET_CMD	  = 0x1;
	public static final byte LOOPING_CMD  = 0x2;
	public static final byte CLEAR_CMD    = 0x3;
	public static final byte DELETE_CMD   = 0x4;
	//sync
	public static final byte SYNC_REQUEST = 0x00;
	public static final byte SYNC_RESPOND = 0x01;
	//telemetry IDs
	public static final int LATITUDE	= 0;
	public static final int LONGITUDE	= 1;
	public static final int HEADING		= 2;
	public static final int PITCH		= 3;
	public static final int ROLL		= 4;
	public static final int SPEED		= 5;
	public static final int VOLTAGE		= 6;

	public static final int	MAX_WAYPOINTS		= 64;
	public static final int	MAX_SETTINGS		= 64;
	public static final int	MAX_TELEMETRY		= 256;
	public static final int	BAUD				= SerialPort.BAUDRATE_9600;
	public static final int	U16_FIXED_POINT		= 256;

	public static final int MAX_CONFIRM_WAIT	= 2000; //in milliseconds
	public static final int MAX_FAILURES		= 6;

	public static final byte[] HEADER = {0x13, 0x37};
	public static final byte[] FOOTER = {(byte)0x9A};

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

	public static byte[] fletcher16bytes(byte[] message){
		int iterator = 0;
		int aSum=0xff, bSum=0xff;
		short tmp;
		int length = message.length;
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
		return new byte[]{ (byte)(bSum), (byte)(aSum)};
	}

	public static int fletcher16( byte[] message ){
		return fletcher16(message, message.length);
	}

	public static boolean fletcher(byte[] message, int length){
		if(length <= 2) return false;
		int foundSum = ( ( (message[length-2]&0xff) <<8) | (message[length-1]&0xff) );
		int generatedSum = fletcher16(message, length-2);
		return generatedSum == foundSum;
	}

	public static int getMsgType(byte input){
		return (input&0x0F);
	}
	public static int getSubtype(byte input){
		return ((input>>4)&0x0F);
	}
	public static byte buildMessageLabel(int type, int subType, int length){//deprecated
		return buildMessageLabel(type, subType);
	}
	public static byte buildMessageLabel(int type, int subType){
		if(type 	> 0xF) return 0;
		if(subType	> 0xF) return 0;
		return (byte) ((subType<<4)|type);
	}
	public static byte buildMessageLabel(int label){
		return (byte) label;
	}
}
