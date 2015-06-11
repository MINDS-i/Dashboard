package com.ui;
import com.Dashboard;
import com.serial.*;
import com.Context;
import com.ui.Graph;

import com.ui.TableColumn;
import com.ui.ColumnTableModel;

import java.awt.*;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

//import settings labels from properties

public class DataWindow implements ActionListener{
	public static final long PERIOD = 200; //update period in MS

	ColumnTableModel setModel;
	ColumnTableModel telModel;
	Context 		context;
	final JFrame 	frame;
	java.util.Timer update;
	JPanel 		  	logPanel;
	JPanel 		  	panel;
	JScrollPane 	scroll;
	JTextField	  	logInput;
	Graph			graph;

	public DataWindow(Context cxt){
		context = cxt;
		frame = new JFrame("Telemetry");
		panel = new JPanel();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	frame.setLayout(new FlowLayout());
    	frame.setVisible(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		ArrayList<TableColumn> telem = new ArrayList<TableColumn>();
		telem.add( new TableColumn(){
			public String	getName(){ return "#"; }
			public Object	getValueAt(int row){ return row; }
			public int		getRowCount(){ return 10000; }
			public Class	getDataClass(){ return Integer.class; }
			public boolean	isRowEditable(int row){ return false; }
			public void		setValueAt(Object val, int row){ ; }
		});

/*		telem.add( new TableColumn(){
			public String	getName(){ return "Log?"; }
			public Object	getValueAt(int row) { return context.isLogged[row]; }
			public int		getRowCount(){ return context.isLogged.length; }
			public Class	getDataClass(){ return Boolean.class; }
			public boolean	isRowEditable(int row){ return true; }
			public void		setValueAt(Object val, int row){
				if(val.getClass()==Boolean.class)
					context.isLogged[row] = (boolean) val;
			}
		});*/
/*		telem.add( new TableColumn(){
			public String	getName(){ return "graph?"; }
			public Object	getValueAt(int row) { return false; }
			public int		getRowCount(){ return 256; }
			public Class	getDataClass(){ return Boolean.class; }
			public boolean	isRowEditable(int row){ return true; }
			public void		setValueAt(Object val, int row){
				System.out.println("trying to add data source");
				if(graph != null) graph.addSource(
					new TelemetryDataSource(row,context.telemetry));
			}
		});
*/

		telem.add( new TableColumn(){
			public String	getName(){ return "Value"; }
			public Object	getValueAt(int row) { return context.getTelemetry(row); }
			public int		getRowCount(){ return 256; }
			public Class	getDataClass(){ return Float.class; }
			public boolean	isRowEditable(int row){ return false; }
			public void		setValueAt(Object val, int row){
				;
			}
		});

		ArrayList<TableColumn> settings = new ArrayList<TableColumn>();
		settings.add( new TableColumn(){
			private ResourceBundle res = ResourceBundle.getBundle(
													"settingLabels",
													context.locale);
			public String	getName(){ return "name"; }
			public Object	getValueAt(int row){
				String ans;
				try{
					ans = res.getString("s"+row);
				} catch(MissingResourceException e) {
					ans = new String("#"+row);
				}
				return ans;
			}
			public int		getRowCount(){ return 10000; }
			public Class	getDataClass(){ return String.class; }
			public boolean	isRowEditable(int row){ return false; }
			public void		setValueAt(Object val, int row){ ; }
		});
		settings.add( new TableColumn(){
			public String	getName(){ return "Setting"; }
			public Object	getValueAt(int row) {
				float val = context.upstreamSettings[row];
				return "  "+val;
			}
			public int		getRowCount(){ return context.upstreamSettings.length; }
			public Class	getDataClass(){ return String.class; }
			public boolean	isRowEditable(int row){ return true; }
			public void		setValueAt(Object val, int row){
				if(val.getClass()==Float.class){
					context.setSetting(row,(Float)val);
					System.out.println("Setting New Value "+(Float)val);
				} else if(val.getClass()==String.class){
					try{
						Float newVal = new Float((String)val);
						context.setSetting(row,newVal);
						System.out.println("Setting New Value "+newVal);
					} catch(Exception e) {
						System.out.println("Bad new value");
					}
				}
			}
		});

		JTable telTable, setTable;
		JScrollPane telScroll, setScroll;
		telModel	= new ColumnTableModel(telem);
		telTable	= new JTable(telModel);
		telScroll	= new JScrollPane(telTable);
		setModel	= new ColumnTableModel(settings);
		setTable	= new JTable(setModel);
		setScroll	= new JScrollPane(setTable);

		telScroll.setPreferredSize(new Dimension(120, 160));
		setScroll.setPreferredSize(new Dimension(120, 300));

		//improve preferred size interface
		javax.swing.table.TableColumn col;
		col = telTable.getColumn(telem.get(0).getName());
		col.setPreferredWidth(10);
		col = telTable.getColumn(telem.get(1).getName());
		col.setPreferredWidth(10);
		col = setTable.getColumn(settings.get(0).getName());
		col.setPreferredWidth(10);


		constructLogPane();
    	panel.add(logPanel);
    	panel.add(telScroll);
    	panel.add(setScroll);
    	frame.add(panel);
    	frame.pack();
    	startUpdateTimer();




		//pitch and roll data sources
		//graph.add();
	}
	private void constructLogPane(){
		logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout());
		JLabel label = new JLabel("Set logging period (ms)");

		logInput = new JTextField();
		logInput.addActionListener(this);
		logInput.setText(Integer.toString(context.telemetry.getLogPeriod()));
		logInput.setColumns(8);

		logPanel.add(label);
		logPanel.add(logInput);
	}
	private void startUpdateTimer(){
    	update = new java.util.Timer();
		update.scheduleAtFixedRate(new TimerTask(){
				public void run(){
					if(telModel == null) return;
					if(setModel == null) return;
					if(context.connected){
						telModel.fireTableRowsUpdated(0, Serial.MAX_TELEMETRY);
						setModel.fireTableRowsUpdated(0, Serial.MAX_SETTINGS);
					}
				}
			}, PERIOD, PERIOD);
	}
	public void actionPerformed(ActionEvent evt) {
	    String inputText = logInput.getText();
	    int input;
	    try {
      		input = Integer.parseInt(inputText);
      		logInput.setText(Integer.toString(input));
      		context.telemetry.setLogPeriod(input);
		} catch (NumberFormatException e) {
			logInput.setText(Integer.toString(context.telemetry.getLogPeriod()));
		}
	}
}
