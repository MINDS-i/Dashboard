package com.ui;

import com.serial.SerialEventListener;
import com.serial.SerialConnectPanel;
import com.serial.Serial;
import jssc.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.nio.charset.Charset;
import com.ui.TableColumn;
import com.ui.ColumnTableModel;

public class RadioConfigScreen extends JPanel{

    class Setting{
        public int id;
        public String name;
        public int value;
        public Setting(){}
        public Setting(int id, String name, int value){
            this.id = id;
            this.value = value;
            this.name = name;
        }
        public Setting(String line){
            String set = line.substring(1);
            String[] idSplit = set.split("[:=\\n\\r]");
            String id    = idSplit[0];
            String name  = idSplit[1];
            String value = idSplit[2];
            this.id    = Integer.valueOf(id);
            this.value = Integer.valueOf(value);
            this.name  = name;
        }
        @Override
        public String toString(){
            return "S"+id+":"+name+"="+value;
        }
        public String getWriteCmd(){
            return "\r\nATS"+id+"="+value+"\r\n";
        }
    }

    class TelemRadio{ //put this in its own file soon
        SerialPort port = null;
        java.util.List<Setting> settings = new ArrayList<Setting>();
        public TelemRadio(){}
        public TelemRadio(SerialPort port){
            this.port = port;
            try{
                Thread.sleep(1100);
                port.writeString("+++");
                Thread.sleep(1100);
                port.readString();
                loadSettings();
            } catch (Exception e){
                System.err.println("Failed to communicate with radio");
                e.printStackTrace();
            }

            for(Setting s : settings){
                System.out.println(s.toString());
            }
        }
        synchronized String getResponse(String call) throws SerialPortException{
            port.writeBytes(call.getBytes(Charset.forName("US-ASCII")));

            StringBuffer output = new StringBuffer();
            class Bool{
                public boolean val = false;
                public Bool(){}
            }
            final Bool stopped = new Bool();
            SerialPortEventListener listener = new SerialPortEventListener(){
                public void serialEvent(SerialPortEvent e){
                    try{
                        output.append(port.readString());
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                    stopped.val = false;
                }
            };
            port.addEventListener(listener);
            while(!stopped.val){
                stopped.val = true;
                try{
                    Thread.sleep(150);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            port.removeEventListener();

            return output.toString();
        }
        void loadSettings(){
            String rsp = "";
            try{
                rsp = getResponse("\r\nATI5\r\n");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            for(String line : rsp.split("\n")){
                if(line.length() <= 1) continue;
                if(line.charAt(0) != 'S') continue;
                Setting found = new Setting(line);
                settings.add(found);
            }
        }
        int getIndexByID(int id){
            for(int i=0; i<settings.size(); i++){
                Setting old = settings.get(i);
                if(old.id == id){
                    return i;
                }
            }
            return -1;
        }
        void updateValue(int i, int val){
            Setting changed = settings.get(i);
            int orig = changed.value;
            changed.value = val;
            String rsp = "";
            boolean failed = false;
            try{
                rsp = getResponse(changed.getWriteCmd());
            } catch (Exception e) {
                failed = true;
                e.printStackTrace();
            }
            if(!rsp.contains("OK")) failed = true;
            if(failed){
                changed.value = orig;
                JFrame mf = new JFrame("Error changing setting");
                JOptionPane.showMessageDialog(mf, "Error:\n"+rsp);
            }
        }
        void writeToEEPROM(){
            try{
                String rsp = getResponse("\r\nAT&W\r\n");
                System.out.println(rsp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        void writeDefaults(){
            updateValue(getIndexByID(1), 9); //serial speed = 9600
            updateValue(getIndexByID(2), 32);//air speed = 32k
            updateValue(getIndexByID(6), 0); //mavlink mode off
        }
    }










    TelemRadio radio = new TelemRadio();
    JTable settingTable = null;
    SerialEventListener sel = new SerialEventListener(){
        public void connectionEstablished(SerialPort newConnection){
            radio = new TelemRadio(newConnection);
            settingTable.invalidate();
        }
        public void disconnectRequest(){
            radio = new TelemRadio();
            settingTable.invalidate();
        }
    };

    public RadioConfigScreen(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        SerialConnectPanel scp = new SerialConnectPanel(sel);
        scp.showBaudSelector(true);
        settingTable = makeSettingTable();
        JScrollPane sScroll = new JScrollPane(settingTable);
        sScroll.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(5, 10, 5,10),
                            BorderFactory.createLineBorder(Color.BLACK)  ));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(loadDefualtsAction));
        buttonPanel.add(new JButton(saveAction));
        add(scp);
        add(sScroll);
        add(buttonPanel);
    }
    private Action saveAction = new AbstractAction() {
        {
            String text = "Save Settings";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            radio.writeToEEPROM();
        }
    };
    private Action loadDefualtsAction = new AbstractAction() {
        {
            String text = "Write Defaults";
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void actionPerformed(ActionEvent e) {
            radio.writeDefaults();
        }
    };
    private JTable makeSettingTable(){
        java.util.List<TableColumn> setCols = new ArrayList<TableColumn>();
        setCols.add( new TableColumn(){
            public String   getName(){ return "ID"; }
            public Object   getValueAt(int row){
                return radio.settings.get(row).id;
            }
            public int      getRowCount(){ return radio.settings.size(); }
            public Class    getDataClass(){ return Integer.class; }
            public boolean  isRowEditable(int row){ return false; }
            public void     setValueAt(Object val, int row){ ; }
        });
        setCols.add( new TableColumn(){
            public String   getName(){ return "Name"; }
            public Object   getValueAt(int row){
                return radio.settings.get(row).name;
            }
            public int      getRowCount(){ return radio.settings.size(); }
            public Class    getDataClass(){ return String.class; }
            public boolean  isRowEditable(int row){ return false; }
            public void     setValueAt(Object val, int row){ ; }
        });
        setCols.add( new TableColumn(){
            public String   getName(){ return "Value"; }
            public Object   getValueAt(int row){
                return radio.settings.get(row).value;
            }
            public int      getRowCount(){ return radio.settings.size(); }
            public Class    getDataClass(){ return Integer.class; }
            public boolean  isRowEditable(int row){ return true; }
            public void     setValueAt(Object val, int row){
                if(val.getClass() != Integer.class) return;
                radio.updateValue(row, (Integer)val);
            }
        });
        ColumnTableModel setModel    = new ColumnTableModel(setCols);
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

        while(f.isShowing()){
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
