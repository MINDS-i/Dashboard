package com.serial;

import com.serial.SerialEventListener;
import jssc.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class SerialConnectPanel extends JPanel {
    private int baudRate = SerialPort.BAUDRATE_9600;
    private boolean showBaudPanel = false;
    private SerialEventListener listener;
    private SerialPort connectedPort = null;
    private JButton refreshButton;
    private JButton connectButton;
    private JComboBox dropDown;

    public SerialConnectPanel(SerialEventListener listener){
        this.listener = listener;
        refreshButton = new JButton(refreshAction);
        connectButton = new JButton(connectAction);
        dropDown = new JComboBox();
        AddSerialList(dropDown);
        add(refreshButton);
        add(dropDown);
        add(connectButton);
        setOpaque(false);
    }

    public void setBaudRate(int baudRate){
        this.baudRate = baudRate;
    }

    public void showBaudSelector(boolean show){
        //todo
    }

    Action refreshAction = new AbstractAction(){
        {
            String text = "Refresh";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            dropDown.removeAllItems();
            AddSerialList(dropDown);
            SerialConnectPanel.this.updateUI();
        }
    };
    Action connectAction = new AbstractAction(){
        {
        String text = "Connect";
        putValue(Action.NAME, text);
        putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e){
            if (connectedPort == null){
                if(connectSerial()) putValue(Action.NAME, "Disconnect");
            }
            else {
                if(disconnectSerial()) putValue(Action.NAME, "Connect");
            }
        }
    };

    private void AddSerialList(JComboBox box){
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            box.addItem(portNames[i]);
        }
    }

    private boolean connectSerial(){
        if(dropDown.getSelectedItem() == null) return false;

        SerialPort serialPort = new SerialPort((String)dropDown.getSelectedItem());

        try{
            serialPort.openPort();
        } catch (SerialPortException ex){
            System.err.println(ex.getMessage());
            return false;
        }

        try{
            serialPort.setParams(baudRate,              SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
            refreshButton.setEnabled(false);
            dropDown.setEnabled(false);
        } catch(SerialPortException ex){
            System.err.println(ex.getMessage());
            return false;
        }
        connectedPort = serialPort;
        listener.connectionEstablished(serialPort);
        return true;
    }

    private boolean disconnectSerial(){
        listener.disconnectRequest();
        connectedPort = null;
        dropDown.setEnabled(true);
        refreshButton.setEnabled(true);
        return true;
    }
}
