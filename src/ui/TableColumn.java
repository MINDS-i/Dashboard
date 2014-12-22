package com.ui;

public interface TableColumn{
	//constructor
	public String	getName();
	public Object	getValueAt(int row);
	public int		getRowCount();
	public Class	getDataClass();
	public boolean	isRowEditable(int row);
	public void		setValueAt(Object value, int row);
}
