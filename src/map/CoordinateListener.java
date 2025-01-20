package com.map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class CoordinateListener implements DocumentListener, ActionListener {
    JTextField field;
    WaypointPanel parent;

    CoordinateListener(JTextField speaker, WaypointPanel father) {
        field = speaker;
        parent = father;
    }

    public void insertUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        parent.interpretLocationEntry();
    }
}
