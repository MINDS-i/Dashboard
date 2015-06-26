package com;

import com.map.MapPanel;
import com.map.WaypointList;
import com.serial.Serial;
import com.serial.SerialParser;
import com.serial.SerialSender;
import com.ui.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.geom.AffineTransform;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.Locale;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Dashboard implements Runnable {
  final static String dataLabels[] = {"Lat:", "Lng:", "Dir:", "Ptc:",
                                              "Rol:", "MPH:", "Vcc:" };
  JPanel serialPanel;
  JButton refreshButton;
  JButton connectButton;
  JComboBox dropDown;
  RotatePanel sideGauge;
  RotatePanel topGauge;
  RotatePanel frontGauge;

  Collection<DataLabel> displays;

  Frame loading;
  static final int[] dataBorderSize = {15,18,46,18};//top,left,bottom,right
  JFrame f;
  Context context;
  MapPanel mapPanel;

  @Override
  public void run() {
    try{
      BufferedImage logo = ImageIO.read(new File("./data/startup-logo.png"));
      loading = new Frame("MINDS-i Loading Box");
      loading.setUndecorated(true);
      loading.setBackground(new Color(0,0,0,0));
      loading.add(new JLabel(new ImageIcon(logo)));
      loading.pack();
      loading.setSize(540,216);
      //loading.setLocationRelativeTo(null);
      loading.setVisible(true);

      context = new Context();
      context.give(this,
                   new AlertPanel(new Font(Font.MONOSPACED,Font.PLAIN,12)),
                   new SerialSender(context),
                   new SerialParser(context),
                   new WaypointList(context),
                   null, //serialPort
                   new Locale("en","US","air"));
      context.alert.setFont(context.theme.text);
      InitUI();
    } catch (IOException e) {
      DisplayError((Exception)e);
    }
  }

  private void InitUI(){
    f = new JFrame("MINDS-i Dashboard");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(false);
    f.setIconImage(context.theme.appIcon);//roverTop);
    f.setTitle("MINDS-i dashboard");

    Action refreshAction = new AbstractAction(){
      {
        String text = "Refresh";
        putValue(Action.SHORT_DESCRIPTION, text);
        putValue(Action.SMALL_ICON, new ImageIcon(context.theme.refreshImage));
      }
      public void actionPerformed(ActionEvent e) {
        dropDown.removeAllItems();
        AddSerialList(dropDown);
        serialPanel.updateUI();
      }
    };
    Action connectAction = new AbstractAction(){
      {
        String text = "Connect";
        putValue(Action.NAME, text);
        putValue(Action.SHORT_DESCRIPTION, text);
      }
      public void actionPerformed(ActionEvent e){
        if (context.connected == false){
          if(connectSerial())
            putValue(Action.NAME, "Disconnect");
        }
        else {
          if(disconnectSerial())
            putValue(Action.NAME, "Connect");
        }
      }
    };

    serialPanel = new JPanel(new FlowLayout());
    refreshButton = new JButton(refreshAction);
    connectButton = new JButton(connectAction);
    dropDown = new JComboBox();
    AddSerialList(dropDown);
    refreshButton.setToolTipText("Refresh");
    connectButton.setToolTipText("Attempt connection");
    serialPanel.add(refreshButton);
    serialPanel.add(dropDown);
    serialPanel.add(connectButton);
    serialPanel.setOpaque(false);

    mapPanel = new MapPanel(  context,
                              new Point(628,1211),
                              4,
                              serialPanel,
                              makeDashPanel(),
                              context.alert);
    mapPanel.setVgap(-45);
    serialPanel.setOpaque(false);

    f.add(mapPanel);
    f.pack();
    f.setSize(800, 650);
    f.setVisible(true);

    loading.dispose();
  }

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
      context.alert.displayMessage("Port not available");
      return false;
    }

    try{
      serialPort.setParams(    Serial.BAUD,
                           SerialPort.DATABITS_8,
                           SerialPort.STOPBITS_1,
                           SerialPort.PARITY_NONE);
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
      System.err.println("Flow Control mode: "+serialPort.getFlowControlMode());

      context.updatePort(serialPort);
      context.alert.displayMessage("Port opened");
      context.sender.sendSync();

      refreshButton.setEnabled(false);
      dropDown.setEnabled(false);
    } catch(SerialPortException ex){
      System.err.println(ex.getMessage());
      context.alert.displayMessage(ex.getMessage());
      context.alert.displayMessage("Connection Failed");
      return false;
    }
    return true;
  }

  private boolean disconnectSerial(){
    try{
      context.closePort();
    } catch(Exception ex){
      System.err.println(ex.getMessage());
      context.alert.displayMessage(ex.getMessage());
      return false;
    }

    dropDown.setEnabled(true);
    refreshButton.setEnabled(true);
    context.alert.displayMessage("Serial Port Closed");
    resetData();
    return true;
  }

  private JPanel makeDashPanel(){
    Color orange = new Color(255,155,30);
    sideGauge  = new RotatePanel(context.theme.roverSide,
                                 context.theme.gaugeBackground,
                                 context.theme.gaugeGlare);
    topGauge   = new RotatePanel(context.theme.roverTop,
                                 context.theme.gaugeBackground,
                                 context.theme.gaugeGlare);
    frontGauge = new RotatePanel(context.theme.roverFront,
                                 context.theme.gaugeBackground,
                                 context.theme.gaugeGlare);
    context.telemetry.registerListener(Serial.HEADING, topGauge);
    context.telemetry.registerListener(Serial.PITCH, sideGauge);
    context.telemetry.registerListener(Serial.ROLL, frontGauge);


    BackgroundPanel dataPanel = new BackgroundPanel(context.theme.gaugeSquare);
    GridBagConstraints c = new GridBagConstraints();
    JPanel dashPanel = new JPanel();

    dataPanel.setBorder(new EmptyBorder(dataBorderSize[0],
                                        dataBorderSize[1],
                                        dataBorderSize[2],
                                        dataBorderSize[3]) );
    dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));



    displays = new ArrayList<DataLabel>();
    for(int i=0; i<dataLabels.length; i++){
      DataLabel label = new DataLabel(dataLabels[i]);
      context.telemetry.registerListener(i, label);
      label.setForeground(orange);
      label.setFont(context.theme.text);
      dataPanel.add(label);
    }

    dataPanel.setOpaque(false);

    dashPanel.setLayout(new GridBagLayout());
    dashPanel.setOpaque(false);
    c.gridy = 1;
    dashPanel.add(dataPanel,c);
    c.gridy = 2;
    dashPanel.add(frontGauge,c);
    c.gridy = 3;
    dashPanel.add(topGauge,c);
    c.gridy = 4;
    dashPanel.add(sideGauge,c);

    return dashPanel;
  }

  private void resetData(){
    for(DataLabel label : displays){
      label.update(0);
    }
  }

  public static void DisplayError(Exception e){
    final JFrame errorFrame = new JFrame("Data");
    errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    errorFrame.setLayout(new FlowLayout());
    errorFrame.setVisible(true);
    JPanel panel = new JPanel();
    JLabel text = new JLabel("Error: \n"+e.getMessage());
    panel.add(text);
    errorFrame.add(panel);
    errorFrame.pack();
  }

  public static void main(String[] args) {
    Dashboard se = new Dashboard();
    SwingUtilities.invokeLater(se);
  }
}
