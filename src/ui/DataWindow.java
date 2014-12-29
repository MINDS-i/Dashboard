package com.ui;
import com.Dashboard;
import com.serial.*;
import com.Logger;
import com.Context;

import com.ui.TableColumn;
import com.ui.ColumnTableModel;

import java.awt.*;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

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
		telem.add( new TableColumn(){
			public String	getName(){ return "Log?"; }
			public Object	getValueAt(int row) { return context.isLogged[row]; }
			public int		getRowCount(){ return context.isLogged.length; }
			public Class	getDataClass(){ return Boolean.class; }
			public boolean	isRowEditable(int row){ return true; }
			public void		setValueAt(Object val, int row){
				if(val.getClass()==Boolean.class)
					context.isLogged[row] = (boolean) val;
			}
		});
		telem.add( new TableColumn(){
			public String	getName(){ return "Value"; }
			public Object	getValueAt(int row) { return context.telemetry[row]; }
			public int		getRowCount(){ return context.telemetry.length; }
			public Class	getDataClass(){ return Float.class; }
			public boolean	isRowEditable(int row){ return false; }
			public void		setValueAt(Object val, int row){ ; }
		});

		ArrayList<TableColumn> settings = new ArrayList<TableColumn>();
		settings.add( new TableColumn(){
			public String	getName(){ return "name"; }
			public Object	getValueAt(int row){ return row; }
			public int		getRowCount(){ return 10000; }
			public Class	getDataClass(){ return Integer.class; }
			public boolean	isRowEditable(int row){ return false; }
			public void		setValueAt(Object val, int row){ ; }
		});
		settings.add( new TableColumn(){
			public String	getName(){ return "Setting"; }
			public Object	getValueAt(int row) { return context.upstreamSettings[row]; }
			public int		getRowCount(){ return context.upstreamSettings.length; }
			public Class	getDataClass(){ return Float.class; }
			public boolean	isRowEditable(int row){ return true; }
			public void		setValueAt(Object val, int row){
				if(val.getClass()==Float.class)
						context.setSetting(row,(Float)val);
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
	}
	private void constructLogPane(){
		logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout());
		JLabel label = new JLabel("Set logging period (ms)");

		logInput = new JTextField();
		logInput.addActionListener(this);
		logInput.setText(Integer.toString(context.log.getLogPeriod()));
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
      		context.log.setLogPeriod(input);
		} catch (NumberFormatException e) {
			logInput.setText(Integer.toString(context.log.getLogPeriod()));
		}
	}
}

/*class dataTableModel extends AbstractTableModel{
	public static final String[] COL_NAMES = {"ID", "Log", "Value"};
	private Context context;

	public dataTableModel(Context cxt){
		context = cxt;
	}

	public int getColumnCount() {
        return COL_NAMES.length;
    }
    public int getRowCount() {
        return Serial.MAX_TELEMETRY;
    }
    public String getColumnName(int col) {
        return COL_NAMES[col];
    }
    public Object getValueAt(int row, int col) {
    	switch(col){
    		case 0:
    			return row;
    		case 1:
    			return context.isLogged[row];
    		case 2:
    			return context.telemetry[row];
    		default:
    			return false;
    	}
    }
    public Class getColumnClass(int col) {
        switch(col){
        	case 0:
        		return Integer.class;
        	case 1:
        		return Boolean.class;
        	case 2:
        		return Float.class;
        	default:
        		return Object.class;
        }
    }
    public boolean isCellEditable(int row, int col) {
    	return (col!=0);
    }
    public void setValueAt(Object value, int row, int col) {
        switch(col){
        	case 1:
        		if(value.getClass()==Boolean.class)
        			context.isLogged[row] = (boolean)value;
        		break;
        	case 2:
        		if(value.getClass()==Float.class){
        			context.setSetting((int)row, (float)value);
        		}
        		break;
        }
    	fireTableCellUpdated(row, col);
    }
}*/
