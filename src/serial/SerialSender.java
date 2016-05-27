package com.serial;

import com.Context;
import com.Dashboard;
import com.map.Dot;
import com.map.MapPanel;
import com.serial.Messages.*;
import com.serial.*;
import java.util.Arrays;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.util.*;
import javax.swing.SwingUtilities;

public class SerialSender {
    private static final Object lock = new Object();
    private LinkedList<Message> pendingConfirm;
    private Timer timer;
    private boolean sendingWaypointList;
    private int waypointListPosition;
    private int waypointListWaitingCode;
    private Context context;

    private final Logger seriallog = Logger.getLogger("d.serial");

    public SerialSender(Context cxt) {
        context = cxt;
        setup();
    }

    private void setup() {
        pendingConfirm = new LinkedList<Message>();
        TimerTask checkTask = new TimerTask() {
            @Override
            public void run() {
                synchronized(lock) {
                    Date now = new Date();
                    for(Iterator<Message> i = pendingConfirm.iterator(); i.hasNext();) {
                        Message msg = i.next();
                        if(msg.isPastExpiration(now)) {
                            if(msg.numberOfFailures() >= Serial.MAX_FAILURES) {
                                i.remove();
                                seriallog.severe(
                                    "Connection failed; Rover unware of "+
                                    msg.toString()+
                                    "!");
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

    public void start() {
        if(timer == null) {
            setup();
        }
    }

    public void stop() {
        if(timer != null) {
            timer.purge();
            timer.cancel();
        }
        timer = null;
    }

    public void addPendingConfirm(Message msg) {
        synchronized(pendingConfirm) {
            pendingConfirm.add(msg);
        }
    }

    public void sendMessage(Message msg) {
        if(context.connected) {
            try {
                msg.send(context.port());
                if(msg.needsConfirm())
                    synchronized(lock) {
                        pendingConfirm.add(msg);
                    }

                seriallog.finer(Integer.toHexString(msg.getConfirmSum()) +
                                " Sent " +
                                msg.toString());
            } catch (SerialPortException ex) {
                seriallog.severe(ex.getMessage());
            }
        }
    }

    public void resendMessage(Message msg) {
        if(context.connected) {
            try {
                msg.addFailure();
                msg.send(context.port());
                seriallog.warning(Integer.toHexString(msg.getConfirmSum())+" "+
                                  "No response to "+msg.toString()+
                                  " resend #"+msg.numberOfFailures());
            } catch (SerialPortException ex) {
                seriallog.severe(ex.getMessage());
            }
        }
    }

    public void notifyOfConfirm(int confirm) {
        if(sendingWaypointList) advanceWaypointList(confirm);
        seriallog.finer("Incomming Confirmation Message to "+
                        Integer.toHexString(confirm));
        synchronized(lock) {
            for(Iterator<Message> i = pendingConfirm.iterator(); i.hasNext();) {
                Message msg = i.next();
                if(msg.isConfirmedBy(confirm)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    public void sendWaypointList() {
        seriallog.fine("Sending waypoint list");
        sendingWaypointList = true;
        waypointListPosition = 0;
        Message msg = Message.clearWaypoints();
        sendMessage(msg);
        advanceWaypointList(waypointListWaitingCode);
    }

    private void advanceWaypointList(int confirm) {
        if(confirm == waypointListWaitingCode) {
            if(waypointListPosition >= context.waypoint.size()) {
                sendingWaypointList = false;
                context.waypoint.sendLoopingStatus();
                return;
            }
            Message msg = Message.addWaypoint( (byte) waypointListPosition,
                                               context.waypoint.get(waypointListPosition));
            sendMessage(msg);
            waypointListWaitingCode = msg.getConfirmSum();
            waypointListPosition++;
        }
    }

    public void sendSync() {
        Message msg = Message.syncMessage(Serial.SYNC_REQUEST);
        sendMessage(msg);
    }
}
