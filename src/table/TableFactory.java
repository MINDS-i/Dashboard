package com.table;

import com.Context;
import com.remote.*;
import com.table.*;
import com.ui.telemetry.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-4-21
 * Description: Factory class used to encapsulate the creation of
 * tables used for displaying telemetry and configurable vehicle settings.
 */
public class TableFactory {
	
	//Constructor is private here to prevent object instantiation.
	private TableFactory() {} 
	
	public enum TableType {Telemetry, Settings}
	
	public static JTable createTable(TableType type, Context context) {
		switch(type) {
			case Telemetry:
				return buildTelemetryTable(context);
			case Settings:
				return buildSettingsTable(context);
			default:
				System.err.println("TableFactory Error - Unknown table type");
		}
		
		return null;
	}
	
	/**
	 * Creates a vehicle settings table with column layout specified by 
	 * the applied table model and sizing settings.
	 * @param context - The application context
	 * @return - A configured vehicle settings table
	 */
	private static JTable buildSettingsTable(Context context) {
		JTable table;
		ColumnTableModel model;
		SettingList settingList = context.settingList;
		ArrayList<TableColumn<?>> columns = new ArrayList<TableColumn<?>>();
        
		columns.add( new TableColumn<String>() {
            public String getName() {
                return "name";
            }
            
            public String getValueAt(int row) {
                if(row < settingList.size())
                    return settingList.get(row).getName();
                return "#"+row;
            }
            
            public int getRowCount() {
                return settingList.size();
            }
            
            public Class<String> getDataClass() {
                return String.class;
            }
            
            public boolean isRowEditable(int row) {
                return false;
            }
            
            public void setValueAt(String val, int row) {
                ;
            }
        });
        
        columns.add( new TableColumn<String>() {
            public String getName() {
                return "Setting";
            }
            
            public String getValueAt(int row) {
                float val = settingList.get(row).getVal();
                return " "+val;
            }
            
            public int getRowCount() {
                return settingList.size();
            }
            
            public Class<String> getDataClass() {
                return String.class;
            }
            
            public boolean isRowEditable(int row) {
                return true;
            }
            
            public void setValueAt(String val, int row) {
                Float newVal = Float.valueOf((String)val);
                if(settingList.get(row).outsideOfBounds(newVal)) {
                    JFrame mf = new JFrame("Warning");
                    JOptionPane.showMessageDialog(
                    		mf, "Caution: new value is outside of logical bounds");
                }
                settingList.pushSetting(row,newVal);
            }
        });

		model = new ColumnTableModel(columns);
		table = new JTable(model);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setFillsViewportHeight(true);
		
		return table;
	}

	/**
	 * Creates a telemtry data table with column layout specified by 
	 * the applied table model and sizing settings.
	 * @param context - The application context
	 * @return - A configured telemetry data table.
	 */
	private static JTable buildTelemetryTable(Context context) {
		JTable table;
		ColumnTableModel model;
		
		ArrayList<TableColumn<?>> columns = new ArrayList<TableColumn<?>>();
        columns.add( new TableColumn<String>() {
            public String getName() {
                return "name";
            }
            
            public String getValueAt(int row) {
                return context.getTelemetryName(row);
            }
            
            public int getRowCount() {
                return context.getTelemetryCount();
            }
            
            public Class<String> getDataClass() {
                return String.class;
            }
            
            public boolean isRowEditable(int row) {
                return false;
            }
            
            public void setValueAt(String val, int row) {
            }
        });

        columns.add( new TableColumn<String>() {
            public String getName() {
                return "Value";
            }
            
            public String getValueAt(int row) {
                return " "+context.getTelemetry(row);
            }
            
            public int getRowCount() {
                return context.getTelemetryCount();
            }
            
            public Class<String> getDataClass() {
                return String.class;
            }
            
            public boolean isRowEditable(int row) {
                return false;
            }
            
            public void setValueAt(String val, int row) {
                ;
            }
        });
		
        model = new ColumnTableModel(columns);
        table = new JTable(model);
        
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setFillsViewportHeight(true);
        
		return table;
	}
}