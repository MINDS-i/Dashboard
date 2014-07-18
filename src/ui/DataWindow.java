package com.ui;
import com.Dashboard;
import com.serial.*;
import com.Logger;

import java.awt.*;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class DataWindow implements ActionListener{
	public static final long PERIOD = 200; //update period in MS

	final JFrame 	frame;
	JPanel 		  	panel;
	JPanel 		  	logPanel;
	JTextField	  	logInput;
	JTable 		  	table;
	dataTableModel  model;
	JScrollPane 	scroll;
	java.util.Timer update;
	public DataWindow(){
		frame = new JFrame("Data");
		panel = new JPanel();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	frame.setLayout(new FlowLayout());
    	frame.setVisible(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		model  = new dataTableModel();
		table  = new JTable(model);
		scroll = new JScrollPane(table);

		scroll.setPreferredSize(new Dimension(200, 300));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.doLayout();
		table.setDragEnabled(false);
		TableColumn col;
		col = table.getColumn(dataTableModel.COL_NAMES[0]);
		col.setPreferredWidth(20);
		col = table.getColumn(dataTableModel.COL_NAMES[1]);
		col.setPreferredWidth(20);

		constructLogPane();
    	panel.add(logPanel);
    	panel.add(scroll);
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
		logInput.setText(Integer.toString(Dashboard.logWriter.getLogPeriod()));
		logInput.setColumns(8);

		logPanel.add(label);
		logPanel.add(logInput);
	}
	private void startUpdateTimer(){
    	update = new java.util.Timer();
		update.scheduleAtFixedRate(new TimerTask(){
				public void run(){
					if(model == null) return;
					if(Serial.connection){
						model.fireTableRowsUpdated(0, Serial.NUM_DATA_SLOTS);
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
      		Dashboard.logWriter.setLogPeriod(input);
		} catch (NumberFormatException e) {
			logInput.setText(Integer.toString(Dashboard.logWriter.getLogPeriod()));
		}
	}
}

class dataTableModel extends AbstractTableModel{
	public static final String[] COL_NAMES = {"ID", "Log", "Value"};

	public int getColumnCount() {
        return COL_NAMES.length;
    }
    public int getRowCount() {
        return Serial.NUM_DATA_SLOTS;
    }
    public String getColumnName(int col) {
        return COL_NAMES[col];
    }
    public Object getValueAt(int row, int col) {
    	switch(col){
    		case 0:
    			return row;
    		case 1:
    			return Logger.isLogged[row];
    		case 2:
    			return Serial.data[row];
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
        			Logger.isLogged[row] = (boolean)value;
        		break;
        	case 2:
        		if(value.getClass()==Float.class){
        			Message msg = new Message((byte)row, (float)value);
        			Dashboard.serialSender.sendMessage(msg);
        		}
        		break;
        }
    	fireTableCellUpdated(row, col);
    }
}
