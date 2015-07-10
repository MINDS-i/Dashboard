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
import javax.swing.text.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

//import settings labels from properties

public class DataWindow implements ActionListener{
	public static final long PERIOD = 200; //update period in MS

	private static final int WINDOW_X = 300;
	private static final int WINDOW_Y = 500;

	private static final Dimension telemBoxDim   = new Dimension(300, 180);
	private static final Dimension settingBoxDim = new Dimension(300, 300);

	ColumnTableModel setModel;
	ColumnTableModel telModel;
	Context 		 context;
	final JFrame 	 frame;
	java.util.Timer  update;
	JPanel 		  	 logPanel;
	JPanel 		  	 panel;
	JScrollPane 	 scroll;
	JTextField	  	 logInput;
	Graph			 graph;
	JTextComponent	 descriptionBox;

	public DataWindow(Context cxt){
		context = cxt;
		frame = new JFrame("Telemetry");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(WINDOW_X,WINDOW_Y);
    	//frame.setLayout(new BoxLayout(frame,BoxLayout.PAGE_AXIS));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		ArrayList<TableColumn> telem = new ArrayList<TableColumn>();
		telem.add( new TableColumn(){
			public String	getName(){ return "name"; }
			public Object	getValueAt(int row){
				return context.getTelemetryName(row);
			}
			public int		getRowCount(){ return 256; }
			public Class	getDataClass(){ return String.class; }
			public boolean	isRowEditable(int row){ return false; }
			public void		setValueAt(Object val, int row){ ; }
		});

		telem.add( new TableColumn(){
			public String	getName(){ return "Value"; }
			public Object	getValueAt(int row) { return " "+context.getTelemetry(row); }
			public int		getRowCount(){ return context.getTelemetryCount(); }
			public Class	getDataClass(){ return String.class; }
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
			public int		getRowCount(){ return 256; }
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

		telScroll.setMaximumSize(  telemBoxDim);
		telScroll.setPreferredSize(telemBoxDim);
		setScroll.setMaximumSize(  settingBoxDim);
		setScroll.setPreferredSize(settingBoxDim);

		//improve preferred size interface
		javax.swing.table.TableColumn col;
		col = telTable.getColumn(telem.get(0).getName());
		col.setPreferredWidth(10);
		col = telTable.getColumn(telem.get(1).getName());
		col.setPreferredWidth(10);
		col = setTable.getColumn(settings.get(0).getName());
		col.setPreferredWidth(10);

        setTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
                setDetail(setTable.getSelectedRow());
            }
        });
/*        JTextPane dBox = new JTextPane();
        dBox.setBorder(BorderFactory.createLineBorder(Color.gray));
        //dBox.setColumns(20);
        //dBox.setLineWrap(true);
        //dBox.setWrapStyleWord(true);
        descriptionBox = dBox;*/

        JTextPane dBox = new JTextPane();
        dBox.setBorder(BorderFactory.createLineBorder(Color.gray));
        dBox.setContentType("text/html");
        //dBox.setMaximumSize(new Dimension(200,20000000));
        //dBox.setColumns(20);
        //dBox.setLineWrap(true);
        //dBox.setWrapStyleWord(true);
        descriptionBox = dBox;

		constructLogPane();
    	panel.add(logPanel);
    	panel.add(telScroll);
    	panel.add(setScroll);
    	panel.add(descriptionBox);
    	panel.add(Box.createVerticalGlue());

    	frame.add(panel);
    	frame.pack();
    	frame.setVisible(true);
    	startUpdateTimer();
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
	private void setDetail(int row){
		ResourceBundle res = ResourceBundle.getBundle( "settingLabels",
														context.locale);
		String detail;
		try{
			detail = res.getString("long"+row);
		} catch (Exception e) {
			detail = "";
		}
		if(descriptionBox != null){
			descriptionBox.setText(detail);
		}

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
