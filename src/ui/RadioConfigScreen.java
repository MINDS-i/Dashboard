package com.ui;

import com.serial.SerialEventListener;
import com.serial.SerialConnectPanel;
import com.serial.Serial;
import jssc.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.Charset;
import com.table.TableColumn;
import com.table.ColumnTableModel;

public class RadioConfigScreen extends JPanel {

    static class Setting {
        public int id;
        public String name;
        public int value;
        public Setting() {}
        public Setting(int id, String name, int value) {
            this.id = id;
            this.value = value;
            this.name = name;
        }
        @Override
        public String toString() {
            return "S"+id+":"+name+"="+value;
        }
        public String getWriteCmd() {
            return "\r\nATS"+id+"="+value+"\r\n";
        }
    }

    class TelemRadio {
        final long RESPONSE_READ_WAIT_TIME = 1500;
        final int[][] RADIO_DEFAULTS = new int[][]{
            {1, 9}, // Serial speed = 9 (9600 baud)
            {2, 32}, // Air Speed = 32 (32K baud)
            {3, 25}, // Net ID
            {4, 20}, // TXPOWER
            {5, 1}, // ECC = 1 on
            {6, 0}, // Mavlink = 0 off
            {7, 1}, // OPPRESEND = 0 off
            {8, 915000}, // MIN_FREQ
            {9, 928000}, // MAX_FREQ
            {10, 50}, // NUM_CHANNELS
            {11, 100}, // DUTY_CYCLE
            {12, 0}, // LBT_RSSI off
            {13, 0}, // Manchester off
            {14, 0}, // RTSCTS off
            {15, 131} // MAX_WINDOW
        };
        SerialPort port = null;
        java.util.List<Setting> settings = new ArrayList<Setting>();
        public TelemRadio() {}
        public TelemRadio(SerialPort port) {
            this.port = port;
            try {
                Thread.sleep(RESPONSE_READ_WAIT_TIME);
                port.writeString("+++");
                Thread.sleep(RESPONSE_READ_WAIT_TIME);
                port.readString();
                loadSettings();
            } catch (Exception e) {
                System.err.println("Failed to communicate with radio");
                e.printStackTrace();
            }
        }
        synchronized String getResponse(String call) throws SerialPortException {
            // Clear any buffered input
            port.readString();

            // Write the command
            port.writeBytes(call.getBytes(Charset.forName("US-ASCII")));

            // Wait for data to return
            try {
                Thread.sleep(RESPONSE_READ_WAIT_TIME);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String response = port.readString();
            return (response == null)? "" : response;
        }

        final Pattern settingRegex = Pattern.compile("S(\\d+):(.+)=(\\d+)");

        void loadSettings() {
            String rsp = "";
            try {
                rsp = getResponse("\r\nATI5\r\n");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            Matcher matcher = settingRegex.matcher(rsp);

            while(matcher.find()){
                int id = Integer.valueOf(matcher.group(1));
                String name = matcher.group(2);
                int value = Integer.valueOf(matcher.group(3));
                settings.add(new Setting(id,name,value));
            }

            if(settings.size() == 0) {
                JFrame mf = new JFrame("Radio did not respond");
                JOptionPane.showMessageDialog(mf,
                    "The radio did not respond to a request to lead setting " +
                    "data.\n Check its connection to the computer and that the"+
                    " correct baud rate is selected.\nNote that factory radios"+
                    " use 57600 baud but radios configured for MINDS-i drones" +
                    " operate at 9600 baud.");
            }
        }
        int getIndexByID(int id) {
            for(int i=0; i<settings.size(); i++) {
                Setting old = settings.get(i);
                if(old.id == id) {
                    return i;
                }
            }
            return -1;
        }
        void updateValue(int i, int val) {
            Setting setting = settings.get(i);
            if(setting.value == val) return;

            // Write the value so it will return the `getWriteCmd` we want
            int original = setting.value;
            setting.value = val;

            String rsp = "";
            boolean failed = false;
            try {
                rsp = getResponse(setting.getWriteCmd());
            } catch (Exception e) {
                failed = true;
                e.printStackTrace();
            }

            if(!rsp.contains("OK")) failed = true;

            if(failed) {
                setting.value = original;
                JFrame mf = new JFrame("Error changing setting");
                JOptionPane.showMessageDialog(mf,
                    "Setting could not be updated.\nRadio response:\n"+rsp);
            } else {
                enableSaveButton();
            }
        }
        void writeToEEPROM() {
            try {
                String rsp = getResponse("\r\nAT&W\r\n");
                System.out.println("Response Read");
                System.out.println(rsp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        void writeDefaults() {
            for(int i=0; i<RADIO_DEFAULTS.length; i++){
                updateValue(getIndexByID(RADIO_DEFAULTS[i][0]),
                            RADIO_DEFAULTS[i][1]);
            }
        }
    }

    private JButton defsButton;
    private JButton saveButton;
    private TelemRadio radio = new TelemRadio();
    private JTable settingTable = null;
    SerialEventListener sel = new SerialEventListener() {
        public void connectionEstablished(SerialPort newConnection) {
            radio = new TelemRadio(newConnection);
            settingTable.invalidate();
            defsButton.setEnabled(true);
        }
        public void disconnectRequest() {
            radio = new TelemRadio();
            settingTable.invalidate();
            defsButton.setEnabled(false);
            disableSaveButton();
        }
    };

    private void enableSaveButton(){
        saveButton.setEnabled(true);
    }

    private void disableSaveButton(){
        saveButton.setEnabled(false);
    }

    public RadioConfigScreen() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        SerialConnectPanel scp = new SerialConnectPanel(sel);
        scp.showBaudSelector(true);
        settingTable = makeSettingTable();
        JScrollPane sScroll = new JScrollPane(settingTable);
        sScroll.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createEmptyBorder(5, 10, 5,10),
                              BorderFactory.createLineBorder(Color.BLACK)  ));
        JPanel buttonPanel = new JPanel();
        defsButton = new JButton(loadDefualtsAction);
        saveButton = new JButton(saveAction);
        defsButton.setEnabled(false);
        saveButton.setEnabled(false);
        buttonPanel.add(defsButton);
        buttonPanel.add(saveButton);
        add(scp);
        add(sScroll);
        add(buttonPanel);
    }
    private Action saveAction = new AbstractAction() {
        {
            String text = "Save Changes";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            radio.writeToEEPROM();
            disableSaveButton();
        }
    };
    private Action loadDefualtsAction = new AbstractAction() {
        {
            String text = "Import Defaults";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            radio.writeDefaults();
        }
    };
    private JTable makeSettingTable() {
        java.util.List<TableColumn<?>> setCols = new ArrayList<TableColumn<?>>();
        setCols.add( new TableColumn<Integer>() {
            public String getName() {
                return "ID";
            }
            public Integer getValueAt(int row) {
                return radio.settings.get(row).id;
            }
            public int getRowCount() {
                return radio.settings.size();
            }
            public Class<Integer> getDataClass() {
                return Integer.class;
            }
            public boolean isRowEditable(int row) {
                return false;
            }
            public void setValueAt(Integer val, int row) {
                ;
            }
        });
        setCols.add( new TableColumn<String>() {
            public String getName() {
                return "Name";
            }
            public String getValueAt(int row) {
                return radio.settings.get(row).name;
            }
            public int getRowCount() {
                return radio.settings.size();
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
        setCols.add( new TableColumn<Integer>() {
            public String getName() {
                return "Value";
            }
            public Integer getValueAt(int row) {
                return radio.settings.get(row).value;
            }
            public int getRowCount() {
                return radio.settings.size();
            }
            public Class<Integer> getDataClass() {
                return Integer.class;
            }
            public boolean isRowEditable(int row) {
                return true;
            }
            public void setValueAt(Integer val, int row) {
                radio.updateValue(row, val);
            }
        });
        ColumnTableModel setModel = new ColumnTableModel(setCols);
        JTable table = new JTable(setModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(200, 120));
        return table;
    }


    public static void main(String[] args) {
        RadioConfigScreen g = new RadioConfigScreen();
        JFrame f = new JFrame("RCS test");
        f.add(g);
        f.pack();
        f.setVisible(true);

        while(f.isShowing()) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
