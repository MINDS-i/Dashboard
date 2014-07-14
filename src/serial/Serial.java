package com.serial;

import jssc.SerialPort;

public class Serial{
	public static final byte DATA_MSG_LENGTH = 7;
	public static final byte WAYPOINT_MSG_LENGTH = 12;
	public static final byte COMMAND_MSG_LENGTH = 3;
	public static final byte CONFIRM_MSG_LENGTH = 4;
	public static final int  MAX_WAYPOINTS = 200;

	public static final byte LATITUDE_MSG = 0x00;
	public static final byte LONGITUDE_MSG = 0x01;
	public static final byte HEADING_MSG = 0x02;
	public static final byte PITCH_MSG = 0x03;
	public static final byte ROLL_MSG = 0x04;
	public static final byte SPEED_MSG = 0x05;
	public static final byte DISTANCE_MSG = 0x06;

	public static final byte ADD_WAYPOINT_MSG = 0x10;
	public static final byte CHANGE_WAYPOINT_MSG = 0x11;
	public static final byte DELETE_WAYPOINT_MSG = 0x12;
	public static final byte ROVER_AT_WAYPOINT_MSG = 0x13;
	public static final byte SEND_ROVER_TO_MSG = 0x14;

	public static final byte SEND_WAYPOINT_CMD = 0x20;
	public static final byte LOOP_ON_CMD = 0x21;
	public static final byte LOOP_OFF_CMD = 0x22;
	public static final byte CLEAR_LIST_CMD = 0x23;

	public static final byte[] END_TAG = {0x17, 0x1F};

	public static final int MAX_CONFIRM_WAIT = 2000; //in milliseconds
	public static final int MAX_FAILURES = 6;
	public static final boolean WAYPOINT_CONFIRM_REQ = true;
	public static final boolean COMMAND_CONFIRM_REQ = true;

	public static final String WAYPOINT_DESCRIPTOR = "Waypoint ";
	public static final String COMMAND_DESCRIPTOR = "Command ";
	public static final String GENERIC_DESCRIPTOR = "Message";

	public static final int   BAUD = SerialPort.BAUDRATE_9600;
	public static final float FIXED_POINT_FACTOR = 0x100000;

	public static boolean connection = false;

	public static int fletcher16( short[] message, int length){
		int iterator = 0;
		int aSum=0xff, bSum=0xff;
		while(length > 0){
			int tlen = length > 359 ? 359 : length;
			length -= tlen;
			do{
				bSum += aSum += (message[iterator++]);
			} while (--tlen > 0);
			aSum = (aSum & 0xff) + (aSum >> 8);
			bSum = (bSum & 0xff) + (bSum >> 8);
		}
		aSum = (aSum & 0xff) + (aSum >> 8);
		bSum = (bSum & 0xff) + (bSum >> 8);
		return (int) ( ((bSum)<<8)|(aSum) );
	}

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

	public static boolean fletcher(short[] message, int length){
		if(length <= 2) return false;
		int foundSum = ( (message[length-2]<<8)|(message[length-1]&0xff) );
		int generatedSum = fletcher16(message, length-2);
		return generatedSum == foundSum;
	}

	public static boolean fletcher(byte[] message, int length){
		if(length <= 2) return false;
		int foundSum = ( (message[length-2]<<8)|(message[length-1]&0xff) );
		int generatedSum = fletcher16(message, length-2);
		return generatedSum == foundSum;
	}
}
