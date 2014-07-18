package com.serial;

import jssc.SerialPort;

public class Serial{
	public static final int   MAX_WAYPOINTS 		= 200;
	public static final int   BAUD 					= SerialPort.BAUDRATE_9600;
	public static final int   MAX_CONFIRM_WAIT 		= 2000; //in milliseconds
	public static final int   MAX_FAILURES 			= 6;
	public static final int   NUM_DATA_SLOTS        = 32;
	public static final float FIXED_POINT_FACTOR 	= 0x100000;

	public static final boolean WAYPOINT_CONFIRM_REQ  = true;
	public static final boolean DATA_CONFIRM_REQ  	  = false;

	public static final byte LATITUDE_MSG 	= 0x00;
	public static final byte LONGITUDE_MSG 	= 0x01;
	public static final byte HEADING_MSG 	= 0x02;
	public static final byte PITCH_MSG 		= 0x03;
	public static final byte ROLL_MSG 		= 0x04;
	public static final byte SPEED_MSG 		= 0x05;
	public static final byte VOLTAGE_MSG 	= 0x06;
	public static final byte TARGET_INDEX 	= 0x07;
	public static final byte LOOPING_TOGGLE	= 0x08;

	public static final byte DATA_MSG			 = 0x00;
	public static final byte ADD_WAYPOINT_MSG 	 = 0x10;
	public static final byte CHANGE_WAYPOINT_MSG = 0x11;
	public static final byte DELETE_WAYPOINT_MSG = 0x12;
	public static final byte CLEAR_WAYPOINT_MSG  = 0x13;
	public static final byte CONFIRMATION_MSG    = 0x20;
	public static final byte REQUEST_RESYNC      = 0x30;

	public static final byte[] HEADER = {0x13, 0x37};
	public static final byte[] FOOTER = {0x7A };

	public static final String WAYPOINT_DESCRIPTOR = "Waypoint ";
	public static final String     DATA_DESCRIPTOR = "Data Msg ";
	public static final String    CLEAR_DESCRIPTOR = "Clear List Command ";
	public static final String  CONFIRM_DESCRIPTOR = "Msg Confirmation ";
	public static final String  GENERIC_DESCRIPTOR = "Message ";

	public static boolean connection = false;
	public static float[] data = new float[Serial.NUM_DATA_SLOTS];

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
