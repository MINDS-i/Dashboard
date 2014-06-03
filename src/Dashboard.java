package com;

import com.map.MapPanel;
import com.serial.SerialSender;
import com.serial.SerialParser;
import com.serial.Serial;
import com.ui.*;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

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
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Dashboard implements Runnable {
  BufferedImage gaugeBackground;
  BufferedImage gaugeSide;
  BufferedImage gaugeTop;
  BufferedImage gaugeFront;
  BufferedImage refreshImage;
  BufferedImage gaugeRed;
  BufferedImage gaugeSquare;
  BufferedImage gaugeGlare;
  BufferedImage logo;
  Font digital;
  Font ocr;
  JPanel serialPanel;
  JButton refreshButton;
  JButton connectButton;
  JComboBox dropDown;
  RotatePanel sideGauge;
  RotatePanel topGauge;
  RotatePanel frontGauge;
  DataLabel latitude;
  DataLabel longitude;
  DataLabel heading;
  DataLabel speed;
  DataLabel pitch;
  DataLabel roll;
  DataLabel distance;
  Frame loading;
  static final int[] dataBorderSize = {15,18,46,18};//top,left,bottom,right
  JFrame f;
  public AlertPanel alertPanel;
  public MapPanel mapPanel;

  SerialPort serialPort;
  SerialSender serialSender;
  SerialParser serialParser;

  FileWriter fileWriter;
  BufferedWriter logFile;
  java.util.Timer logTimer;

  @Override
  public void run() {
    try{
      logo = ImageIO.read(new File("./data/startup-logo.png"));
      loading = new Frame("MINDS-i Loading Box");
      loading.setUndecorated(true);
      loading.setBackground(new Color(0,0,0,0));
      loading.add(new JLabel(new ImageIcon(logo)));
      loading.pack();
      loading.setSize(540,216);
      loading.setLocationRelativeTo(null);
      loading.setVisible(true);

      alertPanel = new AlertPanel(ocr);
      serialSender = new SerialSender();
      serialParser = new SerialParser(this, serialSender);

      refreshImage = ImageIO.read(
              new File("./data/refresh.png"));
      gaugeBackground = ImageIO.read(new File("./data/Gauge.png"));
      gaugeSide = ImageIO.read(new File("./data/6x6-Side.png"));
      gaugeTop = ImageIO.read(new File("./data/6x6-Top.png"));
      gaugeFront = ImageIO.read(new File("./data/6x6-Front.png"));
      gaugeRed = ImageIO.read(new File("./data/direction-arrow.png"));
      gaugeSquare = ImageIO.read(new File("./data/screenWithGlare.png"));
      gaugeGlare = ImageIO.read(new File("./data/gaugeGlare.png"));
      ocr = Font.createFont(Font.TRUETYPE_FONT,
                            new File("./data/ocr.ttf"));
      digital = Font.createFont(Font.TRUETYPE_FONT,
                                new File("./data/digital.ttf"));
      ocr = ocr.deriveFont(13f);
      digital = digital.deriveFont(36f);
      InitUI();
      InitLog();
    } catch (IOException|FontFormatException e) {
      DisplayError((Exception)e);
    }
  }

  private void InitUI(){
    f = new JFrame("MINDS-i Dashboard");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(false);
    f.setIconImage(gaugeTop);
    f.setTitle("MINDS-i dashboard");

    Action refreshAction = new AbstractAction(){
      {
        String text = "Refresh";
        putValue(Action.SHORT_DESCRIPTION, text);
        putValue(Action.SMALL_ICON, new ImageIcon(refreshImage));
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
        if (Serial.connection == false){
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

    mapPanel = new MapPanel(new Point(628,1211),
                              4,
                              serialPanel,
                              makeDashPanel(),
                              alertPanel);
    mapPanel.setVgap(-45);
    mapPanel.setOutput(serialSender);
    serialPanel.setOpaque(false);

    f.add(mapPanel);
    f.pack();
    loading.dispose();
    f.setVisible(true);
    f.setSize(800, 650);
  }

  private void AddSerialList(JComboBox box){
    String[] portNames = SerialPortList.getPortNames();
    for(int i = 0; i < portNames.length; i++){
        box.addItem(portNames[i]);
    }
  }

  private boolean connectSerial(){
    if(dropDown.getSelectedItem() == null) return false;

    serialPort = new SerialPort((String)dropDown.getSelectedItem());

    try{
      serialPort.openPort();
    } catch (SerialPortException ex){
      System.err.println(ex.getMessage());
      AlertPanel.displayMessage("Port not available");
      return false;
    }

    try{
      serialPort.setParams(SerialPort.BAUDRATE_57600,
                           SerialPort.DATABITS_8,
                           SerialPort.STOPBITS_1,
                           SerialPort.PARITY_NONE);
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
                                    | SerialPort.FLOWCONTROL_RTSCTS_OUT);
      System.err.println("Flow Control mode: "+serialPort.getFlowControlMode());
      serialSender.updatePort( serialPort );
      serialParser.updatePort( serialPort );
      Serial.connection = true;
      AlertPanel.displayMessage("Port opened");
      sendWaypointList();

      refreshButton.setEnabled(false);
      dropDown.setEnabled(false);
    } catch(SerialPortException ex){
      System.err.println(ex.getMessage());
      AlertPanel.displayMessage(ex.getMessage());
      AlertPanel.displayMessage("Connection Failed");
      return false;
    }
    return true;
  }

  private boolean disconnectSerial(){
    try{
      if(serialPort != null) serialPort.closePort();
    } catch(SerialPortException ex){
      System.err.println(ex.getMessage());
      AlertPanel.displayMessage(ex.getMessage());
      return false;
    }

    dropDown.setEnabled(true);
    refreshButton.setEnabled(true);
    Serial.connection = false;
    serialParser.stop();
    serialSender.stop();
    AlertPanel.displayMessage("Serial Port Closed");
    resetData();
    return true;
  }

  private JPanel makeDashPanel(){
    Color orange = new Color(255,155,30);
    sideGauge = new RotatePanel(gaugeSide, gaugeBackground, gaugeGlare);
    topGauge = new RotatePanel(gaugeTop, gaugeBackground, gaugeGlare);
    frontGauge = new RotatePanel(gaugeFront, gaugeBackground, gaugeGlare);
    BackgroundPanel dataPanel = new BackgroundPanel(gaugeSquare);
    GridBagConstraints c = new GridBagConstraints();
    JPanel dashPanel = new JPanel();

    dataPanel.setBorder(new EmptyBorder(dataBorderSize[0],
                                        dataBorderSize[1],
                                        dataBorderSize[2],
                                        dataBorderSize[3]) );
    dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
    latitude = new DataLabel("Lat:", "N");
    latitude.setForeground(orange);
    latitude.setFont(ocr);
    dataPanel.add(latitude);
    longitude = new DataLabel("Lng:", "W");
    longitude.setForeground(orange);
    longitude.setFont(ocr);
    dataPanel.add(longitude);
    heading = new DataLabel("Dir:");
    heading.setForeground(orange);
    heading.setFont(ocr);
    dataPanel.add(heading);
    pitch = new DataLabel("Ptc:");
    pitch.setForeground(orange);
    pitch.setFont(ocr);
    dataPanel.add(pitch);
    roll = new DataLabel("Rol:");
    roll.setForeground(orange);
    roll.setFont(ocr);
    dataPanel.add(roll);
    speed = new DataLabel("Spd:", "MPH");
    speed.setForeground(orange);
    speed.setFont(ocr);
    dataPanel.add(speed);
    distance = new DataLabel("Dst:", "ft");
    distance.setForeground(orange);
    distance.setFont(ocr);
    dataPanel.add(distance);
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
    latitude.update(0);
    longitude.update(0);
    heading.update(0);
    topGauge.update(0);
    pitch.update(0);
    sideGauge.update(0);
    roll.update(0);
    frontGauge.update(0);
    speed.update(0);
    distance.update(0);
  }

  private void InitLog(){
    try{
      fileWriter = new FileWriter("log.txt");
      logFile = new BufferedWriter(fileWriter);

      logTimer = new java.util.Timer();
      logTimer.scheduleAtFixedRate(new TimerTask(){
        public void run(){
          try{
            logFile.write(""+latitude.getData()+" "+longitude.getData()+" "
              +heading.getData()+" "+pitch.getData()+" "+roll.getData()+" "
              +speed.getData()+" "+distance.getData());
            logFile.newLine();
            logFile.flush();
          } catch (IOException ex){
            System.err.println(ex);
          }
        }
      }, 1000, 250);
    } catch (IOException ex) {
      System.err.println(ex);
    }
  }

  public void updateData(short tag, float data){
    switch(tag){
      case Serial.LATITUDE_MSG:
        latitude.update(data);
        mapPanel.updateRoverLatitude((double)data);
        break;
      case Serial.LONGITUDE_MSG:
        longitude.update(data);
        mapPanel.updateRoverLongitude((double)data);
        f.repaint();
        break;
      case Serial.HEADING_MSG:
        heading.update(data);
        topGauge.update(data+90);
        break;
      case Serial.PITCH_MSG:
        pitch.update(data-90);
        sideGauge.update(data-90);
        break;
      case Serial.ROLL_MSG:
        roll.update(data-90);
        frontGauge.update(data-90);
        break;
      case Serial.SPEED_MSG:
        speed.update(data);
        break;
      case Serial.DISTANCE_MSG:
        distance.update(data);
        break;
    }
  }

  public void sendWaypointList(){
    serialSender.sendWaypointList(mapPanel);
  }

  public static void DisplayError(Exception e){
    final JFrame errorFrame = new JFrame("Error");
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
