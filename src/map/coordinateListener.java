package com.map;

import com.Dashboard;
import com.map.Dot;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import javax.swing.text.*;

class coordinateListener implements DocumentListener, ActionListener {
    JTextField field;
    WaypointPanel parent;

    coordinateListener(JTextField speaker, WaypointPanel father) {
        field = speaker;
        parent = father;
    }

    public void insertUpdate(DocumentEvent e) {
        field.setForeground(Color.BLUE);
    }
    public void removeUpdate(DocumentEvent e) {
        field.setForeground(Color.BLUE);
    }
    public void changedUpdate(DocumentEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        parent.interpretLocationEntry();
    }
}
