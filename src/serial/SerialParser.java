package com.serial;

import com.Dashboard;
import com.map.Dot;
import com.map.MapPanel;
import com.serial.Serial;
import com.ui.AlertPanel;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortEventListener;
import jssc.SerialPortEvent;

import java.awt.*;

public class SerialParser implements SerialPortEventListener{
	Dashboard parent;
	SerialPort port;
	SerialSender sender;
	short[] buf;
	byte bufPos = 0;
	static int BUFLEN = 25;

	public SerialParser(Dashboard inParent, SerialSender inSender){
		parent = inParent;
		sender = inSender;
		buf = new short[BUFLEN];
	}

	public void updatePort(SerialPort inStream){
		port = inStream;
		try{
			port.addEventListener(this);
		} catch(SerialPortException ex) {
			System.err.println(ex.getMessage());
      		AlertPanel.displayMessage(ex.getMessage());
		}
	}

	public void stop(){
		port = null;
	}

	synchronized public void serialEvent(SerialPortEvent serialEvent){
		if(port == null) return;
		try{
			short tmp;
			while(port.getInputBufferBytesCount()>0){
				tmp = (short)port.readIntArray(1)[0];
				if(bufPos > 1 && (buf[bufPos-1] == Serial.END_TAG[0]
							   && tmp == Serial.END_TAG[1])){
					bufPos--;
					sortMessage();
					bufPos = 0;
					break;
				}
				buf[bufPos++] = tmp;
				if(bufPos >= BUFLEN) //msgs should be read before this happens
					bufPos = 0;
			}
		} catch(SerialPortException ex) {
			System.err.println(ex.getMessage());
			AlertPanel.displayMessage(ex.getMessage());
		}
	}


	private void sortMessage(){
		if(Serial.fletcher(buf, bufPos)){//checksum comes out correct
			switch(bufPos){
				case Serial.DATA_MSG_LENGTH:
					handleData(buf);
					break;
				case Serial.WAYPOINT_MSG_LENGTH:
					handleWaypoint(buf);
					sender.sendConfirm(Serial.fletcher16(buf,bufPos));
					break;
				case Serial.CONFIRM_MSG_LENGTH:
					sender.notifyOfConfirm( buf[0]<<8 | buf[1] );
					break;
				case Serial.COMMAND_MSG_LENGTH:
					if(buf[0] == Serial.SEND_WAYPOINT_CMD)
						parent.sendWaypointList();
					AlertPanel.displayMessage("Rover reboot, list refresh requested");
				default:
					System.err.print("ERROR Bad Length "+bufPos+"\n");
					break;
			}
		} else {
			System.err.print("ERROR: bad checksum, length " + bufPos + "\n");
			for(int i = 0; i<bufPos; i++){
				System.err.print("\n\t("+((char)buf[i])+")\t"+buf[i]);
			}
			AlertPanel.displayMessage("Corrupted message recieved, length " + bufPos);
		}
	}

	private void handleData(short[] message){
		short tag = message[0];
		float data = ((float)(message[1]<<24 | message[2]<<16 | message[3]<<8 | message[4]))/10000000;
		parent.updateData(tag, data);
	}

	private void handleWaypoint(short[] message){
		short tag = message[0];
		float latitude  = ((float)(message[1]<<24 | message[2]<<16 | message[3]<<8 | message[4]))/10000000;
		float longitude = ((float)(message[5]<<24 | message[6]<<16 | message[7]<<8 | message[8]))/10000000;
		short position = message[9];
		if(position > parent.mapPanel.numDot()+1){ //Error; resend whole waypoint list
			parent.sendWaypointList();
		}
		switch(tag){
			case Serial.ADD_WAYPOINT_MSG:
				parent.mapPanel.addDot(longitude, latitude, position);
				break;
			case Serial.CHANGE_WAYPOINT_MSG:
				parent.mapPanel.getDot(position).setLocation(new Point.Double(longitude, latitude));
				break;
			case Serial.DELETE_WAYPOINT_MSG:
				parent.mapPanel.removeDot(position);
				break;
			case Serial.ROVER_AT_WAYPOINT_MSG:
				parent.mapPanel.setRoverTarget(position);
				break;
		}
	}
}
