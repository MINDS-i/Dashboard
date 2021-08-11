package com.serial.Messages;

import com.map.Dot;
import com.serial.Serial;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.util.*;

public class Message {
    protected byte[] content;
    protected byte[] checkPair;
    protected int    checkSum;
    protected int    confirmSum;
    protected int    failCount;
    protected Date   sent;
    protected Message() { //subclasses must call buildChecksums after making content
        failCount = 0;
    }
    public Message(byte[] data) {
        failCount	= 0;
        content     = data.clone();
        buildChecksum();
    }
    public void sendTime(Date date) {
        sent = (Date)date.clone();
    }
    public boolean isConfirmedBy(int confirmation) {
        return ((confirmation&0xFFFF) == (confirmSum&0xFFFF));
    }
    public boolean isPastExpiration(Date now) {
        return (now.getTime()-sent.getTime()) > Serial.MAX_CONFIRM_WAIT;
    }
    public void addFailure() {
        failCount++;
    }
    public int numberOfFailures() {
        return failCount;
    }
    public int getConfirmSum() {
        return confirmSum;
    }
    public void send(SerialPort port) throws SerialPortException {
        port.writeBytes(Serial.HEADER);
        port.writeBytes(content);
        port.writeBytes(checkPair);
        port.writeBytes(Serial.FOOTER);
        sent = new Date();
    }
    private byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length+b.length];
        for(int i=0; i< a.length; i++) {
            c[i] = a[i];
        }
        for(int i=0; i< b.length; i++) {
            c[i+a.length] = b[i];
        }
        return c;
    }
    protected void buildChecksum() {
        checkPair  = Serial.fletcher16bytes(content);
        confirmSum = Serial.fletcher16( concat(content,checkPair) )&0xffff;
    }
    //these should be overridden
    public boolean needsConfirm() {
        return false;
    }
    @Override
    public String toString() {
        return "A message";
    }

    public static Message setWaypoint(byte index, Dot dot) {
        return new WaypointMessage(Serial.ALTER_WAYPOINT, index, dot);
    }
    
    public static Message addWaypoint(byte index, Dot dot) {
        return new WaypointMessage(Serial.ADD_WAYPOINT, index, dot);
    }
    
    public static Message confirmSum(int sum) {
        return new WordMessage(Serial.CONFIRMATION, sum);
    }
    
    public static Message syncMessage(byte resync) {
        return new WordMessage(Serial.SYNC_WORD, resync, (byte)0);
    }
    
    public static Message telemetry(byte index, float data) {
        return new DataMessage(Serial.TELEMETRY_DATA, index, data);
    }
    
    public static Message setSetting(byte index, float data) {
        return new DataMessage(Serial.SETTING_DATA, index, data);
    }
    
    public static Message sensor(byte index, float data) {
    	return new DataMessage(Serial.SENSOR_DATA, index, data);
    }
    
    public static Message errorString(String err) {
        return new StringMessage(Serial.ERROR_STRING, err);
    }
    
    public static Message stateString(String state) {
        return new StringMessage(Serial.STATE_STRING, state);
    }
    
    public static Message command(byte cmd, byte spec) {
        return new WordMessage(Serial.COMMAND_WORD, cmd, spec);
    }
    
    public static Message estop() {
        return new WordMessage(Serial.COMMAND_WORD, Serial.ESTOP_CMD, (byte)0);
    }
    
    public static Message setTarget(byte index) {
        return new WordMessage(Serial.COMMAND_WORD, Serial.TARGET_CMD, index);
    }
    
    public static Message setLooping(byte index) {
        return new WordMessage(Serial.COMMAND_WORD, Serial.LOOPING_CMD, index);
    }
    
    public static Message clearWaypoints() {
        return new WordMessage(Serial.COMMAND_WORD, Serial.CLEAR_CMD, (byte)0);
    }
    
    public static Message deleteWaypoint(byte index) {
        return new WordMessage(Serial.COMMAND_WORD, Serial.DELETE_CMD, index);
    }
    
    public static Message stopDriving() {
    	return new WordMessage(Serial.COMMAND_WORD, Serial.STOP_CMD, (byte)0);
    }
    
    public static Message startDriving() {
    	return new WordMessage(Serial.COMMAND_WORD, Serial.START_CMD, (byte)0);
    }
    
    public static Message requestAPMVersion() {
    	return new DataMessage(Serial.INFO_DATA, (byte)Serial.APM_VERSION, (byte)0);
    }
    
    public static Message enableBumper() {
    	return new WordMessage(Serial.COMMAND_WORD, Serial.ENABLE_BUMPER_CMD, (byte)0);
    }
    
    public static Message disableBumper() {
    	return new WordMessage(Serial.COMMAND_WORD, Serial.DISABLE_BUMPER_CMD, (byte)0);
    }
    
}
