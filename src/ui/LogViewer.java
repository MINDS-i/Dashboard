package com.ui;

import com.logging.*;

import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;

public class LogViewer{
    private JFrame frame;
    private JTextPane details;
    private JList<Record> logList;
    private LogModel model = new LogModel();
    /**
     * Construct a new LogViewer object
     * Attach its handler to each logger that log viewer should display
     * log messages from
     */
    public LogViewer(){
        frame = new JFrame("Event Log");

        logList = new JList<Record>();
        logList.setModel(model);
        logList.addListSelectionListener(onSelect);
        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setLayoutOrientation(JList.VERTICAL);
        logList.setCellRenderer(new Renderer());
        JScrollPane listPane = new JScrollPane(logList);

        details = new JTextPane();
        details.setContentType("text/html");
        JScrollPane detailPane = new JScrollPane(details);

        JSplitPane container = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            listPane, detailPane);

        frame.add(container);
        frame.pack();
    }
    /**
     * Show or hide the log viewer window
     */
    public void setVisible(boolean visible){
        frame.setVisible(visible);
    }
    /**
     * Return a handler that can be attached to loggers
     * Messages send to the handler will appear within the viewer
     */
    public Handler getHandler() {
        return model.logHandler;
    }
    /**
     * Action to be fired when a different cell is selected in the log list
     */
    private ListSelectionListener onSelect = (ListSelectionEvent e) -> {
        if(e.getValueIsAdjusting()) return;
        Record r = model.getElementAt(logList.getMaxSelectionIndex());
        details.setText(r.getMessage());
    };
    /**
     * A model for a JList that lists all the LogRecords its handler
     * has received
     */
    private class LogModel extends AbstractListModel<Record>{
        List<Record> records = new Vector<Record>();
        public Record getElementAt(int idx){ return records.get(idx); }
        public int getSize(){ return records.size(); }
        public Handler logHandler = new SimpleHandler((LogRecord l, String s)->{
            records.add(0, new Record(l));
            fireIntervalAdded(this, 0, 0);
        });
    };
    /**
     * Wrap LogRecords as Record so we can override toString
     * This provides better control over formatting and better performance,
     * Because JList will call toString quite often
     */
    private class Record{
        private LogRecord bR;
        private String asString;
        private String fullMessage;
        private DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Record(LogRecord backingRecord){
            this.bR = backingRecord;
            asString = EllipsisFormatter.ellipsize(bR.getMessage(),30);
            fullMessage = buildFullMessage();
        }
        private String buildFullMessage(){
            StringBuilder b = new StringBuilder();
            Date date = new Date(getMillis());
            String dateFormatted = formatter.format(date);
            b.append("<b>Level:</b> ");
            b.append(getLevel());
            b.append(" | <b>Namespace:</b> ");
            b.append(getLoggerName());
            b.append(" | <b>Time:</b> ");
            b.append(dateFormatted);
            b.append("<hr>");
            b.append(bR.getMessage());
            return b.toString();
        }
        @Override public String toString() {
            return asString;
        }
        String getMessage(){ return fullMessage; }
        Level getLevel(){ return bR.getLevel(); }
        String getLoggerName(){ return bR.getLoggerName(); }
        long getMillis(){ return bR.getMillis(); }
        Object[] getParameters(){ return bR.getParameters(); }
        String getResourceBundleName(){ return bR.getResourceBundleName(); }
        long getSequenceNumber(){ return bR.getSequenceNumber(); }
        String getSourceClassName(){ return bR.getSourceClassName(); }
        String getSourceMethodName(){ return bR.getSourceMethodName(); }
        int getThreadID(){ return bR.getThreadID(); }
        Throwable getThrown(){ return bR.getThrown(); }
    }
    /**
     * Returns an object that gets rendered as each element in the list
     */
    private class Renderer extends JLabel implements ListCellRenderer<Record>{
        Renderer(){
            setOpaque(true);
            //setBorder(BorderFactory.createLineBorder(Color.black));
        }
        public Component
        getListCellRendererComponent(JList<? extends Record> list,
                                     Record value,
                                     int index,
                                     boolean isSelected,
                                     boolean cellHasFocus) {
            Color background = Color.WHITE;
            Color foreground = Color.BLACK;

            if(cellHasFocus){
                background = Color.YELLOW;
            }
            else if ((index-model.getSize()) % 2 == 0) {
                background = Color.LIGHT_GRAY;
            }

            setBackground(background);
            setForeground(foreground);
            setText(value.toString());
            return this;
        }
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g.setColor(Color.BLACK);
            g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            g2d.dispose();
        }
    }
    // This method exists for rapid testing of the component by itself
    public static void main(String[] strs){
        LogViewer lv = new LogViewer();
        lv.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Logger log = Logger.getLogger("t");
        log.addHandler(lv.getHandler());
        log.info("Test message A");
        log.info("Test message B");
        log.info("Test message C");
        log.info("Test message D");
        log.info("Test message E");
        log.info("Test message LONG MESSAGE THAT GOES ON A REALLY LONG WAAAAAAAAAAAAAAAAAAAAAAAAAAAAAY");
        log.info("<i>Test message</i> <b>HTML</b>");
        lv.setVisible(true);
        try{
            while(true){
                log.info("New Message");
                Thread.sleep(1000);
            }
        } catch (Exception e) {}
    }
}
