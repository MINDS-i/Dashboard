package com.table;

import java.awt.event.*;
import java.awt.FlowLayout;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class ColumnTableModel extends AbstractTableModel {
    private List<TelemetryColumn<?>> columns;
   
    public ColumnTableModel(List<TelemetryColumn<?>> cList) {
        columns = cList;
    }
    
    public int getColumnCount() {
        return columns.size();
    }
    
    public int getRowCount() {
        int rowCount = Integer.MAX_VALUE;
        
        Iterator itr = columns.iterator();
        while(itr.hasNext()) {
        	TelemetryColumn col = (TelemetryColumn) itr.next();
            rowCount = Math.min(col.getRowCount(), rowCount);
        }
        return rowCount;
    }
    
    public String getColumnName(int col) {
        return columns.get(col).getName();
    }
    
    public Object getValueAt(int row, int col) {
        return columns.get(col).getValueAt(row);
    }
    
    public Class getColumnClass(int col) {
        return columns.get(col).getDataClass();
    }
    
    public boolean isCellEditable(int row, int col) {
        return columns.get(col).isRowEditable(row);
    }
    
    public void setValueAt(Object value, int row, int col) {
        setValueAtHelper(columns.get(col), value, row);
        fireTableCellUpdated(row, col);
    }
    private <T> void setValueAtHelper(TelemetryColumn<T> tc, Object obj, int row) {
        T val = tc.getDataClass().cast(obj);
        tc.setValueAt(val, row);
    }
}
