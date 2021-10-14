package com.ui.telemetry;

import com.Context;
import com.remote.Setting;
import com.remote.SettingList;

import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import java.awt.Component;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-22-21
 * Description: A custom editor that manipulates JSliders in table cells.
 * This editor is responsible for the mapping and representation of the
 * tables underlying SettingPercentge values (integers) and how they are
 * displayed to the user.
 */
public class SliderEditor extends JSlider implements TableCellEditor {
	Context context;
	protected Vector<CellEditorListener> listenerList;
	
	//Slider State Tracking Vars
	protected int startingValue;
	protected int previousChangeValue;
	protected int targetRow;
	protected boolean editing;
	
	/**
	 * Class Constructor. Initializes global tracked values and defines
	 * the ChangeEvent listener responsible for calculating settings updates in
	 * response to slider movement. Final setting adjustments are pushed to the
	 * settings list on mouse up event. 
	 * @param context
	 * @param table
	 */
	public SliderEditor(Context context, JTable table) {
		super(SwingConstants.HORIZONTAL);
		this.context = context;
		listenerList = new Vector<CellEditorListener>();
		previousChangeValue = -1;
		targetRow = -1;
		
		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				Setting setting;
				int row = table.getSelectedRow();
				
				//Don't use a row value that doesn't exist. (Table not init'd)
				if(row < 0) {
					return;
				}
				
				//Edge Case: Ignore false change values of zero triggered when
				//switching between sliders.
				if(row != targetRow) {
					return;
				}

				//Edge Case and Update Window: The last value generated on mouse
				//up is a duplicate of the previous change event. Update the setting
				//value here.
				if(previousChangeValue == getValue()) {
					setting = context.settingList.get(row);
					
					double min 		= setting.getMin();
					double max 		= setting.getMax();
					double range 	= (max - min);
					double settingValue = ((getValue() * range) / 100) + min;
					
					//Set final updated value
					context.settingList.pushSetting(row, settingValue);
					
					return;
				}
				
				//Update the previous value to account for duplication edge case
				previousChangeValue = getValue();
			}
		});
	}

	/**
	 * Handles the standard functionality of the editor. Determining
	 * how the incoming value and position should be operated on.
	 * @param table - The table this editor is attached to
	 * @param value - The value to be manipulated/changed by this event.
	 * @param isSelected - Whether or not the active cell is selected
	 * @param row - The row location of this cell in the table.
	 * @param column - The column location of this cell in the table.
	 * @return - Component
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		targetRow = row;

		if(value instanceof SettingPercentage) {
			setValue(((SettingPercentage) value).getPercentage());
		}
		else {
			setValue(0);
		}
		
		startingValue = getValue();
		editing = true;

		return this;
	}
	
	/**
	 * Gets the value currently held by the cell editor.
	 * @return - Object
	 */
	@Override
	public Object getCellEditorValue() {
		return new SettingPercentage(getValue());
	}
	
	/**
	 * Returns whether or not a cell is editable.
	 * @param eventObj - The event that triggered this check
	 * @return - boolean
	 */
	@Override
	public boolean isCellEditable(EventObject eventObj) {
		return true;
	}
	
	/**
	 * Returns whether or not a cell should be selected
	 * @param eventObj - The event that triggered this check
	 * @return - boolean
	 */
	@Override
	public boolean shouldSelectCell(EventObject eventObj) {
		return true;
	}
	
	/**
	 * Stops cell editing and notifies all listeners
	 * @return - boolean
	 */
	@Override
	public boolean stopCellEditing() {
		fireEditingStopped();
		editing = false;
		return true;
	}

	/**
	 * Cancels cell editing and notifies all listeners.
	 */
	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
		editing = false;
	}
	
	/**
	 * Adds a cell listener to the internally kept list
	 * @param listener - The listener to add
	 */
	@Override
	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.addElement(listener);
	}
	
	/**
	 * Removes a listener from the internally kept list.
	 * @param listener - The listener to remove.
	 */
	@Override
	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.removeElement(listener);
	}

	/**
	 * Iterates through the list of listeners for this editors
	 * change events and notifies them that editing has been canceled.
	 */	
	protected void fireEditingCanceled() {
		setValue(startingValue);
		ChangeEvent changeEvent = new ChangeEvent(this);
		for (int i = listenerList.size() - 1; i >= 0; i--) {
			((CellEditorListener) listenerList.elementAt(i)).editingCanceled(changeEvent);
		}
	}
	
	/**
	 * Iterates through the list of listeners for this editors
	 * change events and notifies them that editing has stopped.
	 */
	protected void fireEditingStopped() {		
		ChangeEvent changeEvent = new ChangeEvent(this);
		for (int i = listenerList.size() - 1; i >= 0; i--) {
			((CellEditorListener) listenerList.elementAt(i)).editingStopped(changeEvent);
		}
	}
}
