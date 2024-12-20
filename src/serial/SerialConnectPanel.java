package com.serial;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SerialConnectPanel extends JPanel {

    private static final BaudRate[] rates = new BaudRate[]{
            new BaudRate("110   ", SerialPort.BAUDRATE_110),
            new BaudRate("300   ", SerialPort.BAUDRATE_300),
            new BaudRate("600   ", SerialPort.BAUDRATE_600),
            new BaudRate("1200  ", SerialPort.BAUDRATE_1200),
            new BaudRate("4800  ", SerialPort.BAUDRATE_4800),
            new BaudRate("9600  ", SerialPort.BAUDRATE_9600),
            new BaudRate("14400 ", SerialPort.BAUDRATE_14400),
            new BaudRate("19200 ", SerialPort.BAUDRATE_19200),
            new BaudRate("38400 ", SerialPort.BAUDRATE_38400),
            new BaudRate("57600 ", SerialPort.BAUDRATE_57600),
            new BaudRate("115200", SerialPort.BAUDRATE_115200)
    };
    /*
        Sometimes serial port actions can block for many seconds, so connect and
        disconnect are run off the UI thread. These functions control the four
        states and prevent multiple port actions from racing eachother
    */
    private static final String BUTTON_CONNECTING = "Connecting";
    private static final String BUTTON_CONNECTED = "Disconnect";
    private static final String BUTTON_DISCONNECTING = "Disabling ";
    private static final String BUTTON_DISCONNECTED = " Connect  ";
    private final boolean showBaudPanel = false;
    private final SerialEventListener listener;
    private final JButton refreshButton;
    private final JButton connectButton;
    private final JComboBox<String> dropDown;
    private final JComboBox<BaudRate> baudSelect;
    private final Object lock = new Object();
    private int baudRate = Serial.BAUD;
    private SerialPort connectedPort = null;
    private boolean inProgress = false;
    Action refreshAction = new AbstractAction() {
        {
            String text = "Refresh";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            if (inProgress) {
                return;
            }

            refreshDropDown();
            SerialConnectPanel.this.updateUI();
        }
    };
    private final Runnable connectSerial = new Runnable() {
        public void run() {
            String portName = (String) dropDown.getSelectedItem();
            String[] validNames = SerialPortList.getPortNames();
            boolean nameStillExists = false;

            for (String s : validNames) {
                nameStillExists |= s.equals(portName);
            }

            if (!nameStillExists) {
                disconnectDone();
                return;
            }

            SerialPort serialPort = new SerialPort(portName);

            try {
                serialPort.openPort();

                if (baudSelect.isVisible()) {
                    baudRate = ((BaudRate) baudSelect.getSelectedItem()).id;
                }

                serialPort.setParams(
                        baudRate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

//                serialPort.setFlowControlMode(
//                		SerialPort.FLOWCONTROL_RTSCTS_IN |
//                		SerialPort.FLOWCONTROL_RTSCTS_OUT);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            }
            catch (SerialPortException ex) {
                System.err.println(ex.getMessage());
                return;
            }

            connectedPort = serialPort;
            listener.connectionEstablished(serialPort);
            connectDone();
        }
    };
    private final Runnable disconnectSerial = new Runnable() {
        public void run() {
            listener.disconnectRequest();

            try {
                connectedPort.closePort();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            connectedPort = null;
            disconnectDone();
        }
    };
    Action connectAction = new AbstractAction() {
        {
            String text = BUTTON_DISCONNECTED;
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }

        public void actionPerformed(ActionEvent e) {
            if (connectedPort == null) {
                connect();
            }
            else {
                disconnect();
            }
        }
    };

    public SerialConnectPanel(SerialEventListener listener) {
        this.listener = listener;
        refreshButton = new JButton(refreshAction);
        connectButton = new JButton(connectAction);
        dropDown = new JComboBox<>();
        addSerialList(dropDown);
        baudSelect = new JComboBox<>(rates);

        //if the protocol spec'd baud rate is in the list, choose it
        for (int i = 0; i < rates.length; i++) {
            if (rates[i].id == Serial.BAUD) {
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

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public void showBaudSelector(boolean show) {
        baudSelect.setVisible(show);
    }

    private void connect() {
        synchronized (lock) {
            if (inProgress) {
                System.err.println("Connect command issued while a change was in Progress");
                return;
            }

            refreshButton.setEnabled(false);
            dropDown.setEnabled(false);
            connectButton.setEnabled(false);
            connectButton.setText(BUTTON_CONNECTING);
            inProgress = true;
        }
        (new Thread(connectSerial)).start();
    }

    private void connectDone() {
        synchronized (lock) {
            refreshButton.setEnabled(false);
            dropDown.setEnabled(false);
            connectButton.setEnabled(true);
            connectButton.setText(BUTTON_CONNECTED);
            inProgress = false;
        }
    }

    private void disconnect() {
        synchronized (lock) {
            if (inProgress) {
                System.err.println("Connect command issued while a change was in Progress");
                return;
            }

            refreshButton.setEnabled(false);
            dropDown.setEnabled(false);
            connectButton.setEnabled(false);
            connectButton.setText(BUTTON_DISCONNECTING);
            inProgress = true;
        }
        (new Thread(disconnectSerial)).start();
    }

    private void disconnectDone() {
        synchronized (lock) {
            refreshDropDown();
            refreshButton.setEnabled(true);
            dropDown.setEnabled(true);
            connectButton.setEnabled(true);
            connectButton.setText(BUTTON_DISCONNECTED);
            inProgress = false;
        }
    }

    private void refreshDropDown() {
        dropDown.removeAllItems();
        addSerialList(dropDown);
    }

    private void addSerialList(JComboBox<String> box) {
        String[] portNames = SerialPortList.getPortNames();

        for (String portName : portNames) {
            box.addItem(portName);
        }
    }

    public void setEnabled(boolean shouldEnable) {
        refreshButton.setEnabled(shouldEnable);
        connectButton.setEnabled(shouldEnable);
        dropDown.setEnabled(shouldEnable);
        baudSelect.setEnabled(shouldEnable);

    }

    private static class BaudRate {
        public String name;
        public int id;

        BaudRate(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
