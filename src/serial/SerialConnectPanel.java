package com.serial;

import com.serial.SerialEventListener;
import com.serial.Serial;
import jssc.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class SerialConnectPanel extends JPanel {
    private static class BaudRate{
        public String name;
        public int id;
        BaudRate(String name, int id){
            this.name = name;
            this.id = id;
        }
        @Override
        public String toString(){
            return name;
        }
    }
    private static BaudRate[] rates = new BaudRate[]{
        new BaudRate("110   ",SerialPort.BAUDRATE_110    ),
        new BaudRate("300   ",SerialPort.BAUDRATE_300    ),
        new BaudRate("600   ",SerialPort.BAUDRATE_600    ),
        new BaudRate("1200  ",SerialPort.BAUDRATE_1200   ),
        new BaudRate("4800  ",SerialPort.BAUDRATE_4800   ),
        new BaudRate("9600  ",SerialPort.BAUDRATE_9600   ),
        new BaudRate("14400 ",SerialPort.BAUDRATE_14400  ),
        new BaudRate("19200 ",SerialPort.BAUDRATE_19200  ),
        new BaudRate("38400 ",SerialPort.BAUDRATE_38400  ),
        new BaudRate("57600 ",SerialPort.BAUDRATE_57600  ),
        new BaudRate("115200",SerialPort.BAUDRATE_115200 )
    };
    private int baudRate = Serial.BAUD;
    private boolean showBaudPanel = false;
    private SerialEventListener listener;
    private SerialPort connectedPort = null;
    private JButton refreshButton;
    private JButton connectButton;
    private JComboBox dropDown;
    private JComboBox<BaudRate> baudSelect;

    public SerialConnectPanel(SerialEventListener listener){
        this.listener = listener;
        refreshButton = new JButton(refreshAction);
        connectButton = new JButton(connectAction);
        dropDown = new JComboBox();
        addSerialList(dropDown);
        baudSelect = new JComboBox<BaudRate>(rates);
        //if the protocol spec'd baud rate is in the list, choose it
        for(int i=0; i<rates.length; i++){
            if(rates[i].id == Serial.BAUD){
                baudSelect.setSelectedIndex(i);
                break;
            }
        }
        baudSelect.setVisible(false);
        add(refreshButton);
        add(dropDown);
        add(baudSelect);
        add(connectButton);
        setOpaque(false);
    }

    public void setBaudRate(int baudRate){
        this.baudRate = baudRate;
    }

    public void showBaudSelector(boolean show){
        baudSelect.setVisible(show);
    }

    Action refreshAction = new AbstractAction(){
        {
            String text = "Refresh";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            dropDown.removeAllItems();
            addSerialList(dropDown);
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

    private void addSerialList(JComboBox box){
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
            if(baudSelect.isVisible()){
                baudRate = ((BaudRate)baudSelect.getSelectedItem()).id;
            }
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

        final SerialPort portToClose = connectedPort;
        Runnable close = new Runnable(){
            public void run(){
                try{
                    portToClose.closePort();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        if(portToClose != null)
            (new Thread(close)).start();

        connectedPort = null;
        dropDown.setEnabled(true);
        refreshButton.setEnabled(true);
        return true;
    }
}
