package com.telemetry;

import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.Context;
import com.util.UtilHelper;

import java.util.logging.Logger;

import com.telemetry.TelemetryListener;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-14-21
 * Description: Class responsible for monitoring and responding to
 * changes in various telemetry data types. This can sometimes include
 * issuing commands to the greater UI system in response to those changes.
 *
 */
public class TelemetryMonitor {
	
	//Constants
	protected static final int UPDATE_CYCLE_MS = 100;
	protected static final int VCC_EVAL_CYCLE_MS = 1000;
	protected final Logger serialLog = Logger.getLogger("d.serial");
	
	//Vars, Standard Refs
	private Context context;
	protected VCCMonitor vccMonitor;
	
	//Observer Update Timer
	protected javax.swing.Timer updateTimer;
	protected javax.swing.Timer vccEvalTimer;
	
	//List of all subscribed listeners
	protected List<IMonitorListener> listeners;
	
	/**
	 * Pre-defined strings for identify those telemetry
	 * value types that the monitor can service in some way.
	 */
	public enum TelemetryDataType {
		VOLTAGE ("Vcc");
		
		private final String text;
		
		TelemetryDataType(String text) {
			this.text = text;
		}
	};
	
	/**
	 * Class Constructor
	 * 
	 * @param ctx
	 */
	public TelemetryMonitor(Context ctx) {
		context = ctx;
		vccMonitor = new VCCMonitor();
		updateTimer = new javax.swing.Timer(UPDATE_CYCLE_MS, cycleUpdateAction);
		listeners = new LinkedList<IMonitorListener>();
	}
	
	/**
	 * Stops the timers for this monitor.
	 */
	public void stop() {
		updateTimer.stop();
	}
	
	/**
	 * Starts the timers for this monitor.
	 */
	public void start() {
		updateTimer.start();
	}
	
	/**
	 * Adds a listener along with its corresponding type to the
	 * tracking list.
	 * @param toRegister - the listener to track
	 */
	public void register(IMonitorListener toRegister) {
		
		listeners.add(toRegister);
	}
	
	/**
	 * Removes a lisener from the tracking list.
	 * @param toUnregister
	 */
	public void unregister(IMonitorListener toUnregister) {
		listeners.remove(toUnregister);
	}
	
	/**
	 * Action response that triggers listener updates.
	 */
	protected ActionListener cycleUpdateAction = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			signalUpdate();
		}
	};
	
	/**
	 * Notifies all listeners that its time to run their update 
	 * services and send in new values.
	 */
	public void signalUpdate() {
		for(IMonitorListener l : listeners) {
			l.updateMonitor(this);
		}
	}
	
	/**
	 * Listener driven function call that updates tracked values for
	 * monitoring of specific telemetry data types, if the type is not
	 * recognized or handled, it is ignored.
	 * @param data - The telemetry data to store
	 * @param type - the type of telemetry data being sent
	 */
	public void storeData(double data, TelemetryDataType type) {
		switch(type) {
		case VOLTAGE:
			vccMonitor.add(data);
			break;
		default:
			System.err.println(
					"ERROR - TelemetryMonitor - data storage" + 
					" attempted for unknown type");
		}
	}
	
	/**
	 * @author Chris Park
	 * Date: 9-14-21
	 * Description: Tracks a units voltage and responds to persistent 
	 * low voltage events.
	 */
	protected class VCCMonitor {
		
		//Constants
		protected static final double BATTERY_LOW_WARNING_THRESHOLD = 6.5;
		protected static final double BATTERY_LOW_CUTOFF_THRESHOLD 	= 6.0;
		protected static final int 	  VCC_AVERAGING_SIZE 			= 20;
		protected static final int 	  MAX_SEC_BELOW_THRESHOLD 		= 20;
		protected static final int 	  LOW_VCC_SETTLING_TIME_MS 		= 20000;
		
		//Vars
		protected int sampleIndex;
		protected double average;
		protected double[] sampleArray;
		protected boolean isBelowThreshold;
		protected int elapsedMS;
		
		/**
		 * Class Constructor
		 */
		public VCCMonitor() {
			sampleIndex = 0;
			average = 0.0;
			sampleArray = new double[VCC_AVERAGING_SIZE];
			isBelowThreshold = false;
			elapsedMS = 0;
			
		}
		
		/**
		 * Adds a value to the sample array and increments the count.
		 * Wraps to 0 once the end of the array is reached.
		 * @param val
		 */
		public void add(double val) {
			if(sampleIndex == VCC_AVERAGING_SIZE) {
				sampleIndex = 0;
			}
			
			sampleArray[sampleIndex] = val;
			sampleIndex++;
			
			elapsedMS += UPDATE_CYCLE_MS;
			
			//Check for acceptable voltage every VCC_EVAL_CYCLE_MS
			if(elapsedMS % VCC_EVAL_CYCLE_MS == 0) {
				calculateAverage();
				evaluateVoltage();
			}
		}
		
		/**
		 * Updates the stored average using the the values currently stored in
		 * the sample array.
		 */
		public void calculateAverage() {
			average = UtilHelper.getInstance().average(sampleArray, 
					VCC_AVERAGING_SIZE);
		}
		
		/**
		 * Checks the current average against BATTERY_LOW_CUTOFF_THRESHOLD.
		 * If the average is above the cutoff, settling time is reset. If
		 * the settling time is met and the voltage remains low, the units
		 * operation is stopped (GROUND VEHICLES ONLY)
		 */
		public void evaluateVoltage() {
			
			//Voltage monitor reset
			if(average > BATTERY_LOW_CUTOFF_THRESHOLD) {
				isBelowThreshold = false;
				elapsedMS = 0;
				return;
			}
			
			//LOW_VCC_SETTLING_TIME_MS time starts from here.
			if(average <= BATTERY_LOW_CUTOFF_THRESHOLD) {
				isBelowThreshold = true;
			}
			
			//if voltage has remained low over settling time. stop the unit.
			if(elapsedMS == LOW_VCC_SETTLING_TIME_MS) {
				if(isBelowThreshold 
				&& (context.getCurrentLocale() == "ground")) {
					
					if((context.dash.mapPanel != null)
					&& context.dash.mapPanel.waypointPanel.getIsMoving()) {
						serialLog.warning("Battery voltage low. Stopping unit.");
						context.sender.changeMovement(false);
					}
					
					elapsedMS = 0;
				}
			}
		}
		
	}
}
