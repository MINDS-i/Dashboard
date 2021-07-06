package com.ui.widgets;

import com.Context;
import com.serial.Serial;
import com.telemetry.TelemetryListener;

import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 06-30-2021
 * Description: Dashbaord widget child class used to display the
 * GPS signal strength of a unit in the field. This strength is determined
 * using a mix of the number of available/visible GPS satellites, and the
 * quality as determined from an HDOP signal.
 */
public class GPSWidget extends UIWidget {
	//Constants
	protected final static int UPDATE_DELAY_MS = 5000;
	
	//Meter Outer Panel
	protected JPanel meterOuterPanel;
	
	//Meter
	protected ArrayList<JPanel> gpsMeters;
	
	//Meter Update Frequency timer
	protected javax.swing.Timer meterUpdateTimer;
	
	//Satellite Value/State
	protected int satelliteValue;
	protected SatelliteStrength currentSatelliteStrength;

	//HDOP Value/State
	protected double hdopValue;
	protected HDOPStrength currentHDOPStrength;
	
	/**
	 * Pre-defined strength enum used to track the GPS
	 * signal strength derived from HDOP. Used as a rough
	 * metric in conjunction with SatelliteStrength to make
	 * a better determination of signal strength.
	 */
	protected enum HDOPStrength {
		EXCELLENT 	(1),
		GOOD		(2),
		POOR		(3),
		UNKOWN		(-1);
		
		private final int strength;
		
		HDOPStrength(int strength) {
			this.strength = strength;
		}
	};
	
	/**
	 * Pre-defined satellite strength enum based on the
	 * number of available satellites. Used as a rough 
	 * metric in conjunction with HDOPStrength to make
	 * a better determination of signal strength.
	 */
	protected enum SatelliteStrength {
		EXCELLENT	(15),
		GOOD		(10),
		POOR		(5),
		UNKOWN		(-1);
		
		private final int numSats;
		
		SatelliteStrength(int numSats) {
			this.numSats = numSats;
		}
	};
	
	/**
	 * Class constructor
	 * @param ctx - The application context
	 */
	public GPSWidget(Context ctx) {
		super(ctx, "GPS");

		//Init member variables
		satelliteValue 	= -1;
		hdopValue 		= -1.0;
		updateSatelliteStrength(SatelliteStrength.UNKOWN);
		updateHDOPStrength(HDOPStrength.UNKOWN);
		
		//Setup telemetry update listeners
		ctx.telemetry.registerListener(Serial.GPSNUMSAT, new TelemetryListener() {
			//The number of satellites here is an integer sent as floating
			//point, so we account for rounding error
			public void update(double data) {
				satelliteValue = (int)(data + 0.5);
			}
		});
		
		ctx.telemetry.registerListener(Serial.GPSHDOP, new TelemetryListener() {
			public void update(double data) {
				hdopValue = data;
			}
		});
		
		//Build Meter Widget
		meterOuterPanel = new JPanel();
		meterOuterPanel.setMinimumSize(new Dimension(100, 60));
		meterOuterPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 0, 2, 0);
		constraints.anchor = GridBagConstraints.CENTER;
		
		gpsMeters = buildMeterSet();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		meterOuterPanel.add(gpsMeters.get(0), constraints);
		
		this.add(meterOuterPanel);
		
		//Kick off the visual update timer.
		meterUpdateTimer = new javax.swing.Timer(UPDATE_DELAY_MS, meterUpdateAction);
		meterUpdateTimer.start();
	}
	
	/**
	 * Constructs a complete set of meter graphics to be used for a GPS signal
	 * strength meter.
	 * @return
	 */
	protected ArrayList<JPanel> buildMeterSet() {
		ArrayList<JPanel> meterSet;
		JPanel panel;
		
		meterSet = new ArrayList<JPanel>();
		
		//No Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Poor Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Good Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Excellent Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);

		return meterSet;
	}
	
	/**
	 * Periodic update responsible for updating the GPS widget visual meter.
	 * This is fired by a predetermined timer value. See
	 * UPDATE_DELAY_MS for interrupt period.
	 */
	ActionListener meterUpdateAction = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			updateMeter();
		}
	};
	
	/**
	 * Updates the graphical representation of GPS signal strength for the
	 * widget.
	 */
	protected void updateMeter() {
		GridBagConstraints constraints = new GridBagConstraints();
		
		meterOuterPanel.removeAll();
		
		//TODO - CP - Add strength determination algorithm here.
		
		//check levels to determine signal strength (enums)
		//add corresponding meter set to outer panel.
	}
	
	/**
	 * Updates the current satellite strength tracked by the widget.
	 * @param strength - The strength based on number of available satellites.
	 */
	public void updateSatelliteStrength(SatelliteStrength strength) {
		currentSatelliteStrength = strength;
	}
	
	/**
	 * Updates the current HDOP strength tracked by the widget.
	 * @param strength - The strength based on HDOP value.
	 */
	public void updateHDOPStrength(HDOPStrength strength) {
		currentHDOPStrength = strength;
	}
}