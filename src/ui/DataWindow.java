package com.ui;
import com.Dashboard;
import com.serial.*;
import com.Context;
import com.remote.*;
import com.table.TableColumn;
import com.table.ColumnTableModel;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.FlowLayout;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class DataWindow implements ActionListener {
    public static final long PERIOD = 200; //update period in MS

    private static final int WINDOW_X = 300;
    private static final int WINDOW_Y = 560;

    private static final Dimension telemBoxPref   = new Dimension(300, 140);
    private static final Dimension telemBoxMax    = new Dimension(Integer.MAX_VALUE, 140);
    private static final Dimension settingBoxPref = new Dimension(300, 300);
    private static final Dimension settingBoxMax  = new Dimension(Integer.MAX_VALUE, 300);
    private static final Dimension descriptionMin = new Dimension(300, 80);
    private static final Dimension descriptionPref= new Dimension(300, 200);

    private JTable telTable, setTable;
    private ColumnTableModel setModel;
    private ColumnTableModel telModel;
    private Context 		 context;
    private java.util.Timer  update;
    private JPanel 		  	 logPanel;
    private JTextField	  	 logInput;
    private JTextComponent	 descriptionBox;

    public DataWindow(Context cxt) {
        context = cxt;
        JFrame frame = new JFrame("Telemetry");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WINDOW_X,WINDOW_Y);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Data window closed");
                onClose();
            }
        });

        final SettingList settingList = context.settingList;

        ArrayList<TableColumn<?>> telem = new ArrayList<TableColumn<?>>();
        telem.add( new TableColumn<String>() {
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

        telem.add( new TableColumn<String>() {
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

        ArrayList<TableColumn<?>> settings = new ArrayList<TableColumn<?>>();
        settings.add( new TableColumn<String>() {
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
        settings.add( new TableColumn<String>() {
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
                    JOptionPane.showMessageDialog(mf, "Caution: new value is outside of logical bounds");
                }
                settingList.pushSetting(row,newVal);
            }
        });

        //JTable telTable, setTable;
        JScrollPane telScroll, setScroll;
        telModel	= new ColumnTableModel(telem);
        telTable	= new JTable(telModel);
        telScroll	= new JScrollPane(telTable);
        setModel	= new ColumnTableModel(settings);
        setTable	= new JTable(setModel);
        setScroll	= new JScrollPane(setTable);

        telTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        telTable.setFillsViewportHeight(true);
        setTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setTable.setFillsViewportHeight(true);

        telScroll.setMaximumSize(  telemBoxMax);
        telScroll.setPreferredSize(telemBoxPref);
        telScroll.setMinimumSize(  telemBoxPref);

        setScroll.setMaximumSize(  settingBoxMax);
        setScroll.setPreferredSize(settingBoxPref);
        setScroll.setMinimumSize(  settingBoxPref);

        Border tableBorders = BorderFactory.createCompoundBorder(
                                  BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                  BorderFactory.createLineBorder(Color.BLACK)  );
        setScroll.setBorder(tableBorders);
        telScroll.setBorder(tableBorders);

        javax.swing.table.TableColumn col;
        col = telTable.getColumn(telem.get(1).getName());
        col.setPreferredWidth(1);
        col = setTable.getColumn(settings.get(1).getName());
        col.setPreferredWidth(1);

        setTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                setDetail(setTable.getSelectedRow());
            }
        });

        JTextPane dBox = new JTextPane();
        dBox.setBorder(BorderFactory.createLineBorder(Color.gray));
        dBox.setContentType("text/html");
        dBox.setMinimumSize(descriptionMin);
        dBox.setPreferredSize(descriptionPref);
        //dBox.setBorder(tableBorders);
        dBox.setOpaque(false);
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
    private void onClose() {
        if(update != null) update.cancel();
    }
    private void constructLogPane() {
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

    private void setDetail(int row) {
        StringBuilder detail = new StringBuilder();
        if(row >= 0 && row < context.settingList.size()) {
            Setting set = context.settingList.get(row);
            detail.append("min: ");
            detail.append(set.getMin());
            detail.append(" max: ");
            detail.append(set.getMax());
            detail.append(" default: ");
            detail.append(set.getDefault());
            detail.append("<br><hr>");
            detail.append(set.getDescription());
        }
        if(descriptionBox != null) descriptionBox.setText(detail.toString());
    }
    private void startUpdateTimer() {
        update = new java.util.Timer();
        update.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if(telModel == null) return;
                if(setModel == null) return;
                if(context.connected) {
                    telModel.fireTableRowsUpdated(0, Serial.MAX_TELEMETRY);
                    setModel.fireTableRowsUpdated(0, Serial.MAX_SETTINGS);
                    telTable.invalidate();
                    setTable.invalidate();
                }
            }
        }, PERIOD, PERIOD);
    }
    public void actionPerformed(ActionEvent evt) {
        if(logInput == null) return;
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
