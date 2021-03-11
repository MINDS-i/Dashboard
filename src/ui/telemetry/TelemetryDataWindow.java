package com.ui.telemetry;

import com.Context;
import com.remote.*;
import com.table.TableFactory;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-3-21
 * Description: Application window used for displaying vehicle telemetry 
 * information and configurable settings.
 */
public class TelemetryDataWindow implements ActionListener {
	
	//Constants
	private static final int UPDATE_PERIOD_MS = 200;
	private static final int WINDOW_X = 300;
	private static final int WINDOW_Y = 560;

    private static final Dimension TELEM_DIM_PREF    = new Dimension(300, 140);
    private static final Dimension TELEM_DIM_MIN	 = new Dimension(300, 140);
    private static final Dimension TELEM_DIM_MAX     = new Dimension(Integer.MAX_VALUE, 140);
    private static final Dimension SETTINGS_DIM_PREF = new Dimension(300, 300);
    private static final Dimension SETTINGS_DIM_MIN	 = new Dimension(300, 300);
    private static final Dimension SETTINGS_DIM_MAX  = new Dimension(Integer.MAX_VALUE, 300);
    private static final Dimension DESC_DIM_MIN 	 = new Dimension(300, 80);
    private static final Dimension DESC_DIM_PREF	 = new Dimension(300, 200);
	private static final Border    TABLE_BORDERS	 = BorderFactory.createCompoundBorder(
													   	BorderFactory.createEmptyBorder(5, 5, 5, 5),
													   	BorderFactory.createLineBorder(Color.BLACK));
	
	//UI Elements
	private JFrame FRM_Window;
	private JPanel PNL_Main;
	private JTextPane TXP_Description;
	private JTextComponent TXC_DescriptionBox;
	private JTable TBL_Telemetry;
	private JTable TBL_Settings;
	private JScrollPane SCL_Telemetry;
	private JScrollPane SCL_Settings;
	
	//Standard Vars
	private Context context;
		
	/**
	 * Class constructor resposnible for intializing and creating required 
	 * telemetry/settings tables using the factory and setting up all UI component
	 * visual layout.
	 * @param context - the application context
	 */
	public TelemetryDataWindow(Context context) {
		this.context = context; 
		
		//Set up window JFrame
		FRM_Window = new JFrame("Telemetry");
		FRM_Window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		FRM_Window.setSize(WINDOW_X, WINDOW_Y);
		FRM_Window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.out.println("Data window closed");
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
		 ///
		 ///
		 ///
		 ///
		
		//Set up Settings Table and ScrollPane
		TBL_Settings = TableFactory.createTable(TableFactory.TableType.Settings,
				context);
        TBL_Settings.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
            	setSelectedDetails(TBL_Settings.getSelectedRow());
            }
        });

		SCL_Settings = new JScrollPane(TBL_Settings);
		SCL_Settings.setMinimumSize(SETTINGS_DIM_MIN);
		SCL_Settings.setMaximumSize(SETTINGS_DIM_MAX);
		SCL_Settings.setPreferredSize(SETTINGS_DIM_PREF);
		SCL_Settings.setBorder(TABLE_BORDERS);
		
		
		//Set up Main JPanel
			//Make sure all configuration for table elements is completed
		PNL_Main = new JPanel();
		PNL_Main.setLayout(new BoxLayout(PNL_Main, BoxLayout.PAGE_AXIS));
		
		
		//Add Finished Panel to Frame
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
	
	//TODO - CP - Finish Implementation here	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	/**
	 * Brings the window frame to the front of the 
	 * application window draw ordering.
	 */
	public void toFront() {
		FRM_Window.toFront();
	}
	
	//TODO - CP - Finish Implementation here
	public void onClose() {
		
	}
	
	/**
	 * Returns whether or not the window frame is currently 
	 * visible.
	 * @return boolean
	 */
	public boolean getVisible() {
		return FRM_Window.isVisible();
	}
}
