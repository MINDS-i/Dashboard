package com.ui;

import com.Context;
import com.table.TableFactory;
import com.util.DiagnosticManager;

import java.util.TimerTask;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.table.AbstractTableModel;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;


/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 9-15-2022
 * Description: JPanel Window used to display UI diagnostic information.
 */
public class DiagnosticsWindow {
	
	//Constants
	private static final int UPDATE_PERIOD_MS = 200;
	private static final int WINDOW_WIDTH	  = 200;
	private static final int WINDOW_HEIGHT	  = 600;
	
	private static final Dimension DIAG_DIM_PREF = new Dimension(500, 140);
	private static final Dimension DIAG_DIM_MIN	 = new Dimension(500, 140);
	private static final Dimension DIAG_DIM_MAX	 = new Dimension(Integer.MAX_VALUE, 140);
	private static final Border	   TABLE_BORDERS = BorderFactory.createCompoundBorder(
												    BorderFactory.createEmptyBorder(5, 5, 5, 5),
													BorderFactory.createLineBorder(Color.BLACK));
	
	//UI Elements
	private JFrame 		FRM_Window;
	private JPanel		PNL_Main;
	private JTable 		TBL_Diagnostics;
	private JScrollPane SCL_Diagnostics;
	private JButton		BTN_ResetToDefaults;
	
	//Standard Vars
	private Context context;
	private java.util.Timer updateTimer;
	
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 */
	public DiagnosticsWindow(Context ctx) {
		context = ctx;
		
		//Set up window JFrame
		FRM_Window = new JFrame("Diagnostics");
		FRM_Window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		FRM_Window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		FRM_Window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				onClose();
			}
		});
		
		//Set up Table and ScrollPane
		TBL_Diagnostics = TableFactory.createTable(
				TableFactory.TableType.SerialDiagnostics, context);
		
		SCL_Diagnostics = new JScrollPane(TBL_Diagnostics);
		SCL_Diagnostics.setMinimumSize(DIAG_DIM_MIN);
		SCL_Diagnostics.setMaximumSize(DIAG_DIM_MAX);
		SCL_Diagnostics.setPreferredSize(DIAG_DIM_PREF);
		SCL_Diagnostics.setBorder(TABLE_BORDERS);
		
		BTN_ResetToDefaults = new JButton(resetToDefaultsAction);
		
		//Set up main JPanel
		PNL_Main = new JPanel();
		PNL_Main.setLayout(new BoxLayout(PNL_Main, BoxLayout.PAGE_AXIS));
		
		//Add everything to main JPanel
		PNL_Main.add(SCL_Diagnostics);
		PNL_Main.add(Box.createVerticalGlue());
		PNL_Main.add(BTN_ResetToDefaults);
		
		//Add finished panel setup to JFrame
		FRM_Window.add(PNL_Main);
		FRM_Window.pack();
		FRM_Window.setVisible(true);
		
		//Kick off tabel updates
		startTableUpdatetimer();
	}
	
	/**
	 * Brings the window frame to teh front of the
	 * application window draw ordering
	 */
	public void toFront() {
		FRM_Window.toFront();
	}
	
	/**
	 * Performs window closing operations such as
	 * stopping the update timer if it is running when
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
	private void startTableUpdatetimer() {
		updateTimer = new java.util.Timer();
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(TBL_Diagnostics.getModel() == null) {
					return;
				}
				
				if(context.connected) {
					AbstractTableModel diagnosticModel =
							(AbstractTableModel) TBL_Diagnostics.getModel();
					
					diagnosticModel.fireTableRowsUpdated(
							0, DiagnosticManager.NUM_SERIAL_STATS);
					
					TBL_Diagnostics.invalidate();
				}
			}
		}, UPDATE_PERIOD_MS, UPDATE_PERIOD_MS);
	}
	
	private Action resetToDefaultsAction = new AbstractAction() {
		{
			String text = "Reset";
			putValue(Action.NAME, text);
		}
		
		public void actionPerformed(ActionEvent e) {
			DiagnosticManager.getInstance().resetStats();
		}
	};
	
}
