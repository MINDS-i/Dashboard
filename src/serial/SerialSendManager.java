package com.serial;

import java.util.*;
import java.util.logging.Logger;

import com.Context;

import com.serial.Messages.*;
import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;

import com.map.Dot;
import com.map.WaypointList;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 2-9-2023
 * Description: Singleton class used to process outgoing serial communications.
 */
public class SerialSendManager {
	private static SerialSendManager smInstance = null;
	
	//Constants
	private static final int SERVICE_TIMER_INIT_MS	 = 1000;
    private static final int SERVICE_TIMER_PERIOD_MS = 100;
	
	//Timer Task Vars
	private static final Object lock = new Object();
	private Timer serviceTimer;
	
	//Reference to the application context
	private Context context;
	
	//Sender tracked Queues/Lists
	private Queue<Message> messageQueue;
	private LinkedList<Integer> confirmationQueue;
	
	//Logger
	private final Logger seriallog = Logger.getLogger("d.serial");
	
	//--------------Singleton Construction/Initiation Functions-----------------
	
	/**
	 * Private Constructor
	 * @param ctx - the application context
	 */
	private SerialSendManager(Context ctx) {
		context = ctx;
		messageQueue = new LinkedList<Message>();
		confirmationQueue = new LinkedList<Integer>();
	}
	
	/**
	 * Returns the singleton instance of the SerialSendManager. If the manager
	 * has not been initialized with the application context yet, then an
	 * error is thrown.
	 * @return smInstance - The singleton instance of this class.
	 */
	public static SerialSendManager getInstance() {
		if (smInstance == null) {
			throw new AssertionError("Init must be called first.");
		}
		
		return smInstance;
	}
	
	/**
	 * Initializes AND returns the singleton instance for the SerialSendManager.
	 * If the manager has already been initialized, an error is thrown.
	 * @param ctx - The application context;
	 * @return smInstance - The singleton instance of this class.
	 */
	public synchronized static SerialSendManager init(Context ctx) {
		if(smInstance != null) {
			throw new AssertionError("Manager has already been instantiated.");
		}
		
		smInstance = new SerialSendManager(ctx);
		return smInstance;
	}
	
	//-------------------Timer Task/Queue Service Functions---------------------
	
	/**
	 * Initialize, define, and start the timer task that services the 
	 * serial queue
	 */
	private void initQueueServiceTask() {
		TimerTask queueTask = new TimerTask() {
			
			@Override
			public void run() {
				synchronized(lock) {
					
					//If queue is empty, no service is required.
					if(messageQueue.size() == 0) {
						//Clear out any confirmation queue leftovers
						confirmationQueue.clear();
						return;
					}

					//peek the front of the queue (do not remove message)
					Message headMsg = messageQueue.peek();
					
					//If has not seen an initial send, send it now.
					if(!headMsg.getHasBeenSent()) {
						sendMessage(headMsg);
					}
					
					//If message does not require a confirm, remove it
					if(!headMsg.needsConfirm()) {
						messageQueue.remove();
						return;
					}
					
					//Iterate over all confirmations and remove the message 
					//and its confirmation if found.
					Iterator<Integer> confIter = confirmationQueue.iterator();
					while(confIter.hasNext()) {
						if(headMsg.isConfirmedBy(confIter.next())) {
							messageQueue.remove();
							confIter.remove();
							return;
						}
					}
					
					//Check if a resend is needed or maximum failed send
					//attempts have been reached. Report error if necessary.
					Date now = new Date();
					if(headMsg.isPastExpiration(now)) {
						if(headMsg.numberOfFailures() >= Serial.MAX_FAILURES) {
							
                            seriallog.severe(
                            		"Connection failed; Rover unware of " 
                            		+ headMsg.toString()
                            		+ "!");
							messageQueue.remove();
							return;
						}
						
						//Resend and increment failure count
						sendMessage(headMsg, true);
					}
				}
			}
		};
		
		serviceTimer = new Timer();
		serviceTimer.scheduleAtFixedRate(
				queueTask, SERVICE_TIMER_INIT_MS, SERVICE_TIMER_PERIOD_MS);
	}
	
	/**
	 * Start the serial queue service timer
	 */
	public void start() {
		if(serviceTimer == null) {
			initQueueServiceTask();
		}
	}
	
	/**
	 * Stop the serial queue service timer
	 */
	public void stop() {
		if(serviceTimer != null) {
			serviceTimer.purge();
			serviceTimer.cancel();
		}
		
		//Clear out queues to prevent carry over between serial
		//connection events.
		synchronized(lock) {
			messageQueue.clear();
			confirmationQueue.clear();			
		}
		serviceTimer = null;
	}
	
	//------------------------Send/Queue Functions------------------------------

	/**
	 * Adds a message to messageQueue to be handled by the serviceTask.
	 * @param msg - The message to be added to the queue
	 */
	public void addMessageToQueue(Message msg) {
		synchronized(lock) {
			messageQueue.add(msg);
		}
	}
	
	/**
	 * Adds a confirmation checksum (fletcher16) to a list of currently 
	 * confirmed message sums. the service timer will check these against the 
	 * message at the head of the queue and remove any matches found.
	 * @param confirm - the checksum to add to the queue/list
	 */
	public void addConfirmToQueue(Integer confirm) {
		synchronized(lock) {
			confirmationQueue.add(confirm);
		}
	}
	
	//NOTE: SendMessage should never be called outside of the queue timer task
	//		synchronized block!
	
	/**
	 * Send the provided message over available serial connection.
	 * (Overloaded function that assumes false for a resend)
	 * @param msg - The serial message to be sent.
	 */
	private void sendMessage(Message msg) {
		sendMessage(msg, false);
	}
	
	/**
	 * Send the provided message over available serial connection.
	 * @param msg - The serial message to be sent
	 * @param isResend - whether this is a resend of a previous message or not
	 */
	private void sendMessage(Message msg, boolean isResend) {
		if(context.connected) {
			try {
				msg.send(context.port());
				
				if(isResend) { //If resend attempt, log a warning, add failure
					msg.addFailure();
	                seriallog.warning(Integer.toHexString(
	                		msg.getConfirmSum())
	                		+ " " 
	                		+ "No response to " 
	                		+ msg.toString()
	                		+ " resend #" 
	                		+ msg.numberOfFailures());
				}
				else { //otherwise just log as a normally sent message
					seriallog.finer(Integer.toHexString(
							msg.getConfirmSum()) 
							+ " Sent " 
							+ msg.toString());
				}

			}
			catch(SerialPortException ex) {
				seriallog.severe(ex.getMessage());
			}
		}
	}
	
	//---------------------Individual Command Functions-------------------------
	
	/**
	 * Sends a clear command to the unit and the enqueues all available points
	 * in the waypoint list to be sent. A looping mission message is enqueued
	 * after all available waypoints if applicable.
	 * @param waypointList - The list of waypoints to be sent.
	 */
	public void sendWaypointList(WaypointList waypointList) {
		
		//Clear existing points from mission
		addMessageToQueue(Message.clearWaypoints());
		
		//If there are no waypoints, return
		if(waypointList.isEmpty()) {
			return;
		}
		
		
		//Iterate over the waypoint list and queue up all the points
		ListIterator<Dot> pointIter = waypointList.getPoints().listIterator();
		while(pointIter.hasNext()) {
			addMessageToQueue(Message.addWaypoint(
					(byte)pointIter.nextIndex(),
					pointIter.next()));
		}

		//Once all points are queued, queue whether or not the mission loops
		addMessageToQueue(Message.setLooping(
				(byte) (waypointList.getIsLooped() ? 1 : 0)));
	}

	/**
	 * Sends a message to the unit telling it whether it should start or
	 * stop movement.
	 * @param shouldMove - Whether or not the unit should move or stop.
	 */
	public void changeMovement(boolean shouldMove) {
		if(context.connected) {
			
			if(shouldMove) {
				addMessageToQueue(Message.startDriving());
				return;
			}
			
			addMessageToQueue(Message.stopDriving());
		}
	}
	
	/**
	 * Enables or disables the vehicle bumper sensors.
	 * @param shouldEnable - Whether the bumper should be enabled or disabled.
	 */
	public void toggleBumper(boolean shouldEnable) {
		if(context.connected) {
			
			if(shouldEnable) {
				addMessageToQueue(Message.enableBumper());
				return;
			}
			
			addMessageToQueue(Message.disableBumper());
		}
	}
	
	/**
	 * Resets the units Telemetry settings to their default values.
	 */
	public void resetSettings() {
		seriallog.warning(
				"SerialSendManager - Requesting reset to default " 
				+ "telemetry settings.");
		
		addMessageToQueue(Message.resetSettings());
	}
	
	/**
	 * Send a Sync message. Typically used after a serial connection is
	 * established in the Dashboard class
	 */
	public void sendSync() {
		addMessageToQueue(Message.syncMessage(Serial.SYNC_REQUEST));
	}
}
