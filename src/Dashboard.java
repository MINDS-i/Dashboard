package com;

import com.map.MapPanel;
import com.map.WaypointList;
import com.serial.Serial;
import com.serial.SerialParser;
import com.serial.SerialSender;
import com.serial.SerialConnectPanel;
import com.serial.SerialEventListener;
import com.ui.*;
import com.ui.ninePatch.*;
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
  private static final int START_WIDTH  = 820; //default window width
  private static final int START_HEIGHT = 820; //default window height
  private static final int[] dataBorderSize = {15,18,46,18};//top,left,bottom,right
  private static final String dataLabels[] = {"Lat:", "Lng:", "Dir:", "Ptc:",
                                              "Rol:", "MPH:", "Vcc:", "Amp:" };
  private Collection<DataLabel> displays = new ArrayList<DataLabel>(dataLabels.length);
  private Context context = new Context();

  @Override
  public void run() {
    try{
      //setup a loading frame
      BufferedImage logo = ImageIO.read(new File("./data/startup-logo.png"));
      JFrame loading = new JFrame("MINDS-i Loading Box");
      loading.setUndecorated(true);
      loading.setBackground(new Color(0,0,0,0));
      loading.add(new JLabel(new ImageIcon(logo)));
      loading.pack();
      loading.setVisible(true);
      //initialize the major classes into the context
      context.give(this,
                   new AlertPanel(),
                   new SerialSender(context),
                   new SerialParser(context),
                   new WaypointList(context),
                   null //serialPort
                   );
      //build the UI - set alert font
      InitUI();
      context.alert.setFont(context.theme.text);
      //remove loading window
      loading.dispose();
    } catch (IOException e) {
      displayErrorPopup((Exception)e);
    }
  }

  private void InitUI(){
    JFrame f = new JFrame("MINDS-i Dashboard");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(false);
    f.setIconImage(context.theme.appIcon);//roverTop);
    f.setTitle("MINDS-i dashboard");

    SerialEventListener connectActions = new SerialEventListener(){
      public void connectionEstablished(SerialPort port){
        context.updatePort(port);
        context.alert.displayMessage("Port opened");
        context.sender.sendSync();
      }
      public void disconnectRequest(){
        context.closePort();
        context.alert.displayMessage("Serial Port Closed");
        resetData();
      }
    };
    JPanel serialPanel = new SerialConnectPanel(connectActions);

    MapPanel mapPanel = new MapPanel(  context,
                              new Point(0,0),
                              4,
                              serialPanel,
                              makeDashPanel(),
                              context.alert);

    f.add(mapPanel);
    f.pack();
    f.setSize(START_WIDTH, START_HEIGHT);
    f.setVisible(true);
  }

  private JPanel makeDashPanel(){
    RotatePanel sideGauge  = new RotatePanel(context.theme.roverSide,
                                             context.theme.gaugeBackground,
                                             context.theme.gaugeGlare);
    RotatePanel topGauge   = new RotatePanel(context.theme.roverTop,
                                             context.theme.gaugeBackground,
                                             context.theme.gaugeGlare);
    RotatePanel frontGauge = new RotatePanel(context.theme.roverFront,
                                             context.theme.gaugeBackground,
                                             context.theme.gaugeGlare);
    context.telemetry.registerListener(Serial.HEADING, topGauge);
    context.telemetry.registerListener(Serial.PITCH, sideGauge);
    context.telemetry.registerListener(Serial.ROLL, frontGauge);

    NinePatchPanel dataPanel = new NinePatchPanel(context.theme.screenPatch);
    dataPanel.setBorder(new EmptyBorder(dataBorderSize[0],
                                        dataBorderSize[1],
                                        dataBorderSize[2],
                                        dataBorderSize[3]) );
    dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
    dataPanel.setOpaque(false);
    for(int i=0; i<dataLabels.length; i++){
      DataLabel label = new DataLabel(dataLabels[i]);
      label.setMaxLength(13);
      context.telemetry.registerListener(i, label);
      label.setForeground(context.theme.textColor);
      label.setFont(context.theme.text);
      dataPanel.add(label);
    }

    JPanel dashPanel = new JPanel();
    dashPanel.setLayout(new GridBagLayout());
    dashPanel.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
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

  public static void displayErrorPopup(Exception e){
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
