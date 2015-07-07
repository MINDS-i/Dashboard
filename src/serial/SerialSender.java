package com.serial;

import com.Context;
import com.Dashboard;
import com.map.Dot;
import com.map.MapPanel;
import com.serial.Messages.*;
import com.serial.*;
import com.ui.AlertPanel;
import java.util.Arrays;
import jssc.SerialPort;
import jssc.SerialPortException;

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
	private Context context;

	public SerialSender(Context cxt){
		context = cxt;
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
						if(msg.isPastExpiration(now)){
							if(msg.numberOfFailures() >= Serial.MAX_FAILURES){
								i.remove();
								System.err.println("Message Failed to send repeatedly; connection bad");
								context.alert.displayMessage("Connection failed; Rover unware of "+msg.toString()+"!");
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

	public void start(){
		if(timer == null){
			setup();
		}
	}

	public void stop(){
		if(timer != null){
			timer.purge();
			timer.cancel();
		}
		timer = null;
	}

	public void addPendingConfirm(Message msg){
		synchronized(pendingConfirm){
			pendingConfirm.add(msg);
		}
	}

	public void sendMessage(Message msg){
		if(context.connected){
			try{
				msg.send(context.port());
				if(msg.needsConfirm())
					synchronized(lock){ pendingConfirm.add(msg); }

				System.err.print("" + Integer.toHexString(msg.getConfirmSum())
									+ " Sent "
									+ msg.toString()
									+ "\n" );
				context.alert.displayMessage(msg.toString()+" Sent");
			} catch (SerialPortException ex){
				System.err.println(ex.getMessage());
				context.alert.displayMessage(ex.getMessage());
			}
		}
	}

	public void resendMessage(Message msg){
		if(context.connected){
			try{
				msg.addFailure();
				msg.send(context.port());
				System.out.print(Integer.toHexString(msg.getConfirmSum()));
				System.out.print(" resend of " + msg.toString());
				System.out.println("");
				context.alert.displayMessage(
										"No response to "+msg.toString()
									   +" resend #"+msg.numberOfFailures());
			} catch (SerialPortException ex){
				System.err.println(ex.getMessage());
				context.alert.displayMessage(ex.getMessage());
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
					context.alert.displayMessage(
						msg.toString()+" Confirmed after "
						+msg.numberOfFailures()+" tries");
					break;
				}
			}
		}
	}

	public void sendWaypointList(){
		sendingWaypointList = true;
		waypointListPosition = 0;
		Message msg = new StandardMessage(Serial.CLEAR_CMD, (byte)0);
		sendMessage(msg);
		advanceWaypointList(waypointListWaitingCode);
	}

	private void advanceWaypointList(int confirm){
		if(confirm == waypointListWaitingCode){
			if(waypointListPosition >= context.waypoint.size()){
				sendingWaypointList = false;
				context.waypoint.sendLoopingStatus();
				return;
			}
			Message msg = new WaypointMessage(Serial.ADD_SUBTYPE,
								(byte) waypointListPosition,
								context.waypoint.get(waypointListPosition));
			sendMessage(msg);
			waypointListWaitingCode = msg.getConfirmSum();
			waypointListPosition++;
		}
	}

	public void sendSync(){
		Message msg = new ProtocolMessage(Serial.SYNC_SUBTYPE);
		sendMessage(msg);
	}
}
