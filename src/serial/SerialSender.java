package com.serial;

import com.Dashboard;
import com.map.Dot;
import com.map.MapPanel;
import com.ui.AlertPanel;
import jssc.SerialPort;
import jssc.SerialPortException;
import java.util.Arrays;

import java.util.*;
import javax.swing.SwingUtilities;

public class SerialSender{
	private static final Object lock = new Object();
	private LinkedList<Message> pendingConfirm;
	private Timer timer;
	private SerialPort port;
	private boolean sendingWaypointList;
	private int waypointListPosition;
	private int waypointListWaitingCode;
	private MapPanel mapPanel;

	public SerialSender(){
		setup();
	}

	private void setup(){
		pendingConfirm = new LinkedList<Message>();
		TimerTask checkTask = new TimerTask(){
			@Override
			public void run(){
				synchronized(lock){
					Date now = new Date();
					for(Iterator<Message> i = pendingConfirm.iterator(); i.hasNext();){
						Message msg = i.next();
						if(msg.pastExpiration(now)){
							if(msg.numberOfFailures() >= Serial.MAX_FAILURES){
								i.remove();
								System.err.println("Message Failed to send repeatedly; connection bad");
								AlertPanel.displayMessage("Connection failed; Rover unware of "+msg.describeSelf()+"!");
							} else {
								resendMessage(msg);
							}
						}
					}
				}
			}
		};
		timer = new Timer();
		timer.scheduleAtFixedRate(checkTask, 1000, 100);
	}

	public void updatePort(SerialPort inStream){
		port = inStream;
		if(timer == null){
			setup();
		}
	}

	public void stop(){
		timer.purge();
		timer.cancel();
		timer = null;
		port = null;
	}

	public void addPendingConfirm(Message msg){
		synchronized(pendingConfirm){
			pendingConfirm.add(msg);
		}
	}


	public void sendMessage(Message msg){
		if(port != null){
			try{
				port.writeBytes(Serial.HEADER);
				port.writeBytes(msg.content);
				port.writeBytes(Serial.FOOTER);
				msg.sendTime(new Date());
				if(msg.needsConfirm())
					synchronized(lock){ pendingConfirm.add(msg); }

				System.err.println("Message " + Integer.toHexString(msg.confirmSum) + " Sent");
				AlertPanel.displayMessage("Data for "+msg.describeSelf()+" Sent");
			} catch (SerialPortException ex){
				System.err.println(ex.getMessage());
				AlertPanel.displayMessage(ex.getMessage());
			}
		}
	}

	public void resendMessage(Message msg){
		if(port != null){
			try{
				port.writeBytes(Serial.HEADER);
				port.writeBytes(msg.content);
				port.writeBytes(Serial.FOOTER);
				msg.addFailure();
				msg.sendTime(new Date());

				System.out.print("Message resent; ");
				System.out.print(Integer.toHexString(msg.confirmSum));
				System.out.println(" needed");
				AlertPanel.displayMessage("No response to "+msg.describeSelf()+" resend #"+msg.numberOfFailures());
			} catch (SerialPortException ex){
				System.err.println(ex.getMessage());
				AlertPanel.displayMessage(ex.getMessage());
			}
		}
	}

	public void notifyOfConfirm(int confirm){
		if(sendingWaypointList) advanceWaypointList(confirm);
		System.out.print("Incomming Confirmation Message ");
		System.out.println(Integer.toHexString(confirm));
		synchronized(lock){
			for(Iterator<Message> i = pendingConfirm.iterator(); i.hasNext();){
				Message msg = i.next();
				if(msg.isConfirmedBy(confirm)){
					i.remove();
					AlertPanel.displayMessage(msg.describeSelf()+" Confirmed after "+msg.numberOfFailures()+" tries");
					break;
				}
			}
		}
	}

	public void sendWaypointList(MapPanel panel){
		mapPanel = panel;
		sendingWaypointList = true;
		waypointListPosition = 0;
		Message msg = new Message(Serial.CLEAR_WAYPOINT_MSG, new Dot(), (byte)0);
		sendMessage(msg);
		waypointListWaitingCode = msg.getConfirmSum();
	}

	private void advanceWaypointList(int confirm){
		if(confirm == waypointListWaitingCode){
			if(waypointListPosition < mapPanel.numDot()){
				Message msg = new Message( Serial.ADD_WAYPOINT_MSG, mapPanel.getDot(waypointListPosition), (byte)waypointListPosition);
				sendMessage(msg);
				waypointListWaitingCode = msg.getConfirmSum();
			} else {
				for(byte id=0; id<Serial.NUM_DATA_SLOTS; id++){
					if(Serial.data[id]==0) continue;
					Message msg = new Message( id , Serial.data[id] );
					sendMessage(msg);
				}
				sendingWaypointList = false;
			}
			waypointListPosition++;
		}
	}
}
