package com.ui.telemetry;

import com.Context;
import com.remote.*;
import com.serial.*;
import com.table.TableFactory;

import java.util.TimerTask;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.table.AbstractTableModel;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-3-21
 * Description: Application window used for displaying vehicle telemetry 
 * information and configurable settings.
 */
public class TelemetryDataWindow implements ActionListener {
	
	//Constants
	private static final int UPDATE_PERIOD_MS 	= 200;
	private static final int WINDOW_WIDTH 		= 300;
	private static final int WINDOW_HEIGHT 		= 560;
	private static final int LOG_FIELD_WIDTH 	= 8;

    private static final Dimension TELEM_DIM_PREF    = new Dimension(500, 140);
    private static final Dimension TELEM_DIM_MIN	 = new Dimension(500, 140);
    private static final Dimension TELEM_DIM_MAX     = new Dimension(Integer.MAX_VALUE, 140);
    
    private static final Dimension SETTINGS_DIM_PREF = new Dimension(300, 300);
    private static final Dimension SETTINGS_DIM_MIN	 = new Dimension(300, 300);
    private static final Dimension SETTINGS_DIM_MAX  = new Dimension(Integer.MAX_VALUE, 300);
    
    private static final Dimension SLIDERS_DIM_PREF  = new Dimension(200, 300);
    private static final Dimension SLIDERS_DIM_MIN	 = new Dimension(200, 300);
    private static final Dimension SLIDERS_DIM_MAX   = new Dimension(Integer.MAX_VALUE, 300);
        
    private static final Dimension DESC_DIM_MIN 	 = new Dimension(500, 80);
    private static final Dimension DESC_DIM_PREF	 = new Dimension(500, 200);
	private static final Border    TABLE_BORDERS	 = BorderFactory.createCompoundBorder(
													   	BorderFactory.createEmptyBorder(5, 5, 5, 5),
													   	BorderFactory.createLineBorder(Color.BLACK));
	
	//UI Elements
	private JFrame FRM_Window;
	private JPanel PNL_Main;
	private JPanel PNL_Settings;
	private JPanel PNL_Log;
	private JLabel LBL_Log;
	private JTextField TXF_Log;
	private JTextPane TXP_Description;
	private JTextComponent TXC_DescriptionBox;
	private JTable TBL_Telemetry;
	private JTable TBL_Settings;
	private JTable TBL_SettingsSliders;
	
	private JScrollPane SCL_Telemetry;
	private JScrollPane SCL_Settings;
	private JScrollPane SCL_SettingsSliders;
	
	//Standard Vars
	private Context context;
	private java.util.Timer updateTimer;
		
	/**
	 * Class constructor resposnible for intializing and creating required 
	 * telemetry/settings tables using the TableFactory class and setting 
	 * up all UI component visual layout.
	 * @param context - the application context
	 */
	public TelemetryDataWindow(Context context) {
		this.context = context; 
		
		//Set up window JFrame
		FRM_Window = new JFrame("Telemetry");
		FRM_Window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		FRM_Window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		FRM_Window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				onClose();
			}
		});
		
		//Setup Description Box
		TXP_Description = new JTextPane();
		TXP_Description.setBorder(BorderFactory.createLineBorder(Color.gray));
		TXP_Description.setContentType("text/html");
		TXP_Description.setMinimumSize(DESC_DIM_MIN);
		TXP_Description.setPreferredSize(DESC_DIM_PREF);
		TXP_Description.setOpaque(false);
		TXC_DescriptionBox = TXP_Description;
		
		//Set up Telemetry Table and ScrollPane
		TBL_Telemetry = TableFactory.createTable(TableFactory.TableType.Telemetry,
				context);
		
		SCL_Telemetry = new JScrollPane(TBL_Telemetry);
		SCL_Telemetry.setMinimumSize(TELEM_DIM_MIN);
		SCL_Telemetry.setMaximumSize(TELEM_DIM_MAX);
		SCL_Telemetry.setPreferredSize(TELEM_DIM_PREF);
		SCL_Telemetry.setBorder(TABLE_BORDERS);
		
		//Set up Settings Table and ScrollPane
		TBL_Settings = TableFactory.createTable(TableFactory.TableType.Settings,
				context);
        TBL_Settings.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
            	setSelectedDetails(TBL_Settings.getSelectedRow());
            }
        });
        
        //Set up slider visual updates when a value is entered manually.
		TBL_Settings.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				updateSliderPercentages();
			}
		});

        SCL_Settings = new JScrollPane(TBL_Settings);
		SCL_Settings.setMinimumSize(SETTINGS_DIM_MIN);
		SCL_Settings.setMaximumSize(SETTINGS_DIM_MAX);
		SCL_Settings.setPreferredSize(SETTINGS_DIM_PREF);
		SCL_Settings.setBorder(TABLE_BORDERS);		

		//Tie scrolling movement to slider table
		SCL_Settings.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int scrollPosition = 
						SCL_Settings.getVerticalScrollBar().getValue();
				SCL_SettingsSliders.getVerticalScrollBar().setValue(scrollPosition);
			}
		});
		
        //Set up SettingSlider Table and ScrollPane
		TBL_SettingsSliders = TableFactory.createTable(TableFactory.TableType.Sliders,
				context);
		
		//Release table cell editing on mouse up so that sliders update correctly
        TBL_SettingsSliders.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseReleased(MouseEvent e) {
        		TBL_SettingsSliders.getCellEditor().stopCellEditing();
        	}
        });
		
		
		SCL_SettingsSliders = new JScrollPane(TBL_SettingsSliders);
		SCL_SettingsSliders.setMinimumSize(SLIDERS_DIM_MIN);
		SCL_SettingsSliders.setMaximumSize(SLIDERS_DIM_MAX);
		SCL_SettingsSliders.setPreferredSize(SLIDERS_DIM_PREF);
		SCL_SettingsSliders.setBorder(TABLE_BORDERS);

		//Tie scrolling movement to settings table
		SCL_SettingsSliders.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int scrollPosition = 
						SCL_SettingsSliders.getVerticalScrollBar().getValue();
				SCL_Settings.getVerticalScrollBar().setValue(scrollPosition);
			}
		});

        //Pack both settings tables in flow layout settings panel
        PNL_Settings = new JPanel();
        PNL_Settings.setLayout(new FlowLayout());
        PNL_Settings.add(SCL_Settings);
        PNL_Settings.add(SCL_SettingsSliders);

		//Build Log Panel
		PNL_Log = new JPanel();
		PNL_Log.setLayout(new FlowLayout());
		LBL_Log = new JLabel("Set logging period (ms)");
		TXF_Log = new JTextField();
		TXF_Log.addActionListener(this);
		TXF_Log.setText(Integer.toString(context.telemLog.getPeriod()));
		TXF_Log.setColumns(LOG_FIELD_WIDTH);
		PNL_Log.add(LBL_Log);
		PNL_Log.add(TXF_Log);
		
		//Set up main JPanel
		PNL_Main = new JPanel();
		PNL_Main.setLayout(new BoxLayout(PNL_Main, BoxLayout.PAGE_AXIS));
		
		//Add everything to main JPanel
		PNL_Main.add(PNL_Log);
		PNL_Main.add(SCL_Telemetry);
		PNL_Main.add(PNL_Settings);
		PNL_Main.add(TXP_Description);
		PNL_Main.add(Box.createVerticalGlue());
		
		//Add finished panel setup to main JFrame
		FRM_Window.add(PNL_Main);
		FRM_Window.pack();
		FRM_Window.setVisible(true);
		
		//Initialize/update slider values to match current telemetry data
		updateSliderPercentages();
		
		//Kick off table updates
		startTableUpdateTimer();
	}
	
	/**
	 * Sets the description details of the selected setting by row.
	 * @param row - The row of the setting that details will be displayed
	 * for.
	 */
    private void setSelectedDetails(int row) {
        StringBuilder details = new StringBuilder();
        
        if(row >= 0 && row < context.settingList.size()) {
            Setting setting = context.settingList.get(row);
            details.append("Min: ");
            details.append(setting.getMin());
            details.append(" Max: ");
            details.append(setting.getMax());
            details.append(" Default: ");
            details.append(setting.getDefault());
            details.append("<br><hr>");
            details.append(setting.getDescription());
        }
        
        if(TXC_DescriptionBox != null) {
        	TXC_DescriptionBox.setText(details.toString());
        } 
    }
	
	/**
	 * Updates the logging period in response to user driven
	 * change.
	 * @param event - The log period field update event
	 */
	public void actionPerformed(ActionEvent event) {
		if(TXF_Log == null) {
			return;
		}
		
		try {
			int input = Integer.parseInt(TXF_Log.getText());
			TXF_Log.setText(Integer.toString(input));
			context.telemLog.setPeriod(input);
		}
		catch(NumberFormatException e) {
			TXF_Log.setText(Integer.toString(context.telemLog.getPeriod()));
		}
	}
	
	/**
	 * Brings the window frame to the front of the 
	 * application window draw ordering.
	 */
	public void toFront() {
		FRM_Window.toFront();
	}
	
	/**
	 * Performs window closing operations such as
	 * stoping the update timer if it is running when
	 * the window is closed.
	 */
	public void onClose() {
		if(updateTimer != null) {
			updateTimer.cancel();
		}
	}
	
	/**
	 * Returns whether or not the window frame is currently 
	 * visible.
	 * @return boolean
	 */
	public boolean getVisible() {
		return FRM_Window.isVisible();
	}
	
	/**
	 * Initializes a periodic table update timer using the UPDATE_PERIOD_MS
	 * variable. When the timer fires the table models will attempt to
	 * update the current values.
	 */
	private void startTableUpdateTimer() {
		updateTimer = new java.util.Timer();
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(TBL_Telemetry.getModel() == null
				|| TBL_Settings.getModel() == null) {
					return;
				}
				
				if(context.connected) {
					AbstractTableModel telemetryModel = 
							(AbstractTableModel) TBL_Telemetry.getModel();
					AbstractTableModel settingsModel = 
							(AbstractTableModel) TBL_Settings.getModel();
					
					telemetryModel.fireTableRowsUpdated(
							0, Serial.MAX_TELEMETRY);
					settingsModel.fireTableRowsUpdated(
							0, Serial.MAX_TELEMETRY);
					
					TBL_Telemetry.invalidate();
					TBL_Settings.invalidate();
				}
			}
		}, UPDATE_PERIOD_MS, UPDATE_PERIOD_MS);
	}
	
	
	/**
	 * Initializes the slider values for current telemetry values when the
	 * window is opened.
	 */
	public void updateSliderPercentages() {
		Setting setting;
		double min; 
		double max;
		double current;
		int percentage;
		
		for(int i = 0; i < context.settingList.size(); i++) {
			setting = context.settingList.get(i);
			min 	= setting.getMin();
			max 	= setting.getMax();
			current = setting.getVal();
			
			percentage = (int) Math.round(((current - min) * 100) / (max - min));
			percentage = (int) Math.floor(percentage);
			
			TBL_SettingsSliders.getModel().setValueAt(percentage, i, 0);
		}
	}
}
