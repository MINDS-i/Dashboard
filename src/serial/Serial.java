package com.serial;

import jssc.SerialPort;

public class Serial{
	public enum messageType{
		STANDARD(0),
		SETTINGS(1),
		WAYPOINT(2),
		PROTOCOL(3);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		messageType(byte val){ bitValue = val; }
	}
	public enum standardSubtype{
		TELEMETRY(0),
		COMMAND(1);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		standardSubtype(byte val){ bitValue = val; }
	}
	public enum waypointSubtype{
		ADD(0),
		ALTER(1),
		DELETE(2);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		waypointSubtype(byte val){ bitValue = val; }
	}
	public enum settingsSubtype{
		SET(0),
		POLL(1);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		settingsSubtype(byte val){ bitValue = val; }
	}
	public enum protocolSubtype{
		SYNC(0),
		CONFIRM(1);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		protocolSubtype(byte val){ bitValue = val; }
	}
	public enum telemetry{
		LATITUDE(0),
		LONGITUDE(1),
		HEADING(2),
		PITCH(3),
		ROLL(4),
		SPEED(5),
		VOLTAGE(6);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		telemetry(byte val){ bitValue = val; }
	}
	public enum commands{
		ESTOP(0),
		TARGET(1),
		LOOPING(2),
		CLEAR_WAYPOINTS(3);
		private static final byte bitValue;
		public byte getValue(){ return bitValue; }
		commands(byte val){ bitValue = val; }
	}

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
}
