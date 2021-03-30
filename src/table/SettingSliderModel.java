package com.table;

import com.ui.telemetry.SettingPercentage;

import javax.swing.table.AbstractTableModel;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-22-21
 * Description: Table model used to define the behavioral model for
 * a settings slider.
 */
public class SettingSliderModel extends AbstractTableModel {
	
	String headers[] = {"Configure"};
	Object data[][];

	
	/**
	 * Class Constructor
	 * @param context - The application context
	 */
	public SettingSliderModel(int size) {
		//Init the data structure based on the size of the
		//telemetry settings list.		
		data = new Object[size][1];
		
		for(int i = 0; i < size; i++) {
			data[i][0] = new SettingPercentage();
		}
		
	}
	
	/**
	 * Get the number of rows in this table.
	 * @return - int
	 */
	@Override
	public int getRowCount() {
		return data.length;
	}
	
	/**
	 * Get the number of columns in this table.
	 * @return - int
	 */
	@Override
	public int getColumnCount() {
		return headers.length;
	}
	
	/**
	 * Get the class type from the specified column
	 * @param column - The column to retrieve the class from
	 * @return - Class
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		return SettingPercentage.class;
	}
	
	/**
	 * Get the column name for the specified index.
	 * @param column - The column index
	 * @return - String
	 */
	@Override
	public String getColumnName(int column) {
		return headers[0];
	} 
	
	/**
	 * Return whether or not the cell at the specified index
	 * is editable.
	 * @param row	 - The row index
	 * @param column - The column index
	 * @return - boolean
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}
	
	/**
	 * Get the specified Object from the table.
	 * @param row 	 - Table row index
	 * @param column - Table column index
	 * @return - Object
	 */
	@Override
	public Object getValueAt(int row, int column) {
		return data[row][column];
	}
	
	/**
	 * Set the value held at the specified index in the table.
	 * @param value	 - The value to be stored
	 * @param row	 - Table row index
	 * @param column - Table column index
	 */
	@Override
	public void setValueAt(Object value, int row, int column) {
//		System.err.println("SliderSettingModel - Setting value to model for row: " + row);
		((SettingPercentage) data[row][column]).setPercentage(value);
		
		fireTableCellUpdated(row, column);
	}
}
