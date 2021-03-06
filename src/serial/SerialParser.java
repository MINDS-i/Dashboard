package com.serial;

import com.Context;
import com.Dashboard;
import com.data.stateDescription.*;
import com.map.Dot;
import com.map.MapPanel;
import com.map.WaypointList;
import com.serial.*;
import com.serial.Messages.*;
import com.serial.Serial;
import static com.map.WaypointList.*;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortEventListener;
import jssc.SerialPortEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class SerialParser implements SerialPortEventListener {
    private Context context;
    private Decoder decoder;
    private StateMap descriptionMap;
    private WaypointList waypoints;

    private final Logger seriallog = Logger.getLogger("d.serial");
    private final Logger robotlog = Logger.getLogger("d.robot");

    public SerialParser(Context cxt, WaypointList waypoints) {
        context = cxt;
        this.waypoints = waypoints;
    }

    public void serialEvent(SerialPortEvent event) {
        decoder.update();
    }

    public void updatePort() {
        InputStream serial = new SerialInputStream(context.port());
        Checksum check = new Checksum() {
            public byte[] calc(byte[] plaintext) {
                return Serial.fletcher16bytes(plaintext);
            }
            public int length() {
                return 2;
            }
        };
        decoder = new Decoder(serial, Serial.HEADER, Serial.FOOTER, check);
        decoder.addPacketReader(new DataReader());
        decoder.addPacketReader(new WordReader());
        decoder.addPacketReader(new StringReader());

        try {
            context.port().addEventListener(this);
        } catch(SerialPortException ex) {
            seriallog.severe(ex.getMessage());
        }
    }

/*    private class WaypointReader implements PacketReader {
        public int claim(byte data) {
            if(Serial.getMsgType(data) == Serial.WAYPOINT_TYPE) return 255;
            else return -1;
        }
        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            int tmpLat = (	((msg[1]&0xff)<<24)|
                            ((msg[2]&0xff)<<16)|
                            ((msg[3]&0xff)<< 8)|
                            ((msg[4]&0xff)    ) );
            int tmpLon = (	((msg[5]&0xff)<<24)|
                            ((msg[6]&0xff)<<16)|
                            ((msg[7]&0xff)<< 8)|
                            ((msg[8]&0xff)    ) );
            int index = msg[11];
            //msg 9, 10 are altitude
            float latitude	= Float.intBitsToFloat(tmpLat);
            float longitude	= Float.intBitsToFloat(tmpLon);
            switch(subtype) {
                case Serial.ADD_WAYPOINT:
                    waypoints.add(new Dot(longitude, latitude, 0), index);
                    break;
                case Serial.ALTER_WAYPOINT:
                    waypoints.set(index, new Dot(longitude, latitude, ));
                    get(index).setLocation(
                        new Point.Double(longitude, latitude));
                    break;
            }
        }
    }*/

    private class DataReader implements PacketReader {
        public int claim(byte data) {
            if(Serial.getMsgType(data) == Serial.DATA_TYPE) return 255;
            else return -1;
        }
        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            int index   = msg[1];
            int tmpdata = (	((msg[2]&0xff)<<24)|
                            ((msg[3]&0xff)<<16)|
                            ((msg[4]&0xff)<< 8)|
                            ((msg[5]&0xff)    ) );
            float data  = Float.intBitsToFloat(tmpdata);
            switch(subtype) {
                case Serial.TELEMETRY_DATA:
                    context.setTelemetry(index, data);
                    break;
                case Serial.SETTING_DATA:
                    context.setSettingQuiet(index, data);
                    break;
            }
        }
    }
    private class WordReader implements PacketReader {
        public int claim(byte data) {
            if(Serial.getMsgType(data) == Serial.WORD_TYPE) return 255;
            else return -1;
        }
        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            byte a = (byte)(msg[1]&0xff);
            byte b = (byte)(msg[2]&0xff);
            int join = ( ((int)(a<<8)&0xFF00) | ((int)(b&0xFF)) );
            switch(subtype) {
                case Serial.CONFIRMATION:
                    context.sender.notifyOfConfirm(join);
                    break;
                case Serial.SYNC_WORD: {
                        if(a == Serial.SYNC_REQUEST) {
                            Message message = Message.syncMessage(Serial.SYNC_RESPOND);
                            context.sender.sendMessage(message);
                            context.onConnection();
                        } else if (a == Serial.SYNC_RESPOND) { //resync seen
                            context.onConnection();
                        }
                    }
                    break;
                case Serial.COMMAND_WORD:
                    if(a == Serial.TARGET_CMD) {
                        if(b < 0 || b >= waypoints.size()){
                            seriallog.severe("Rover transmitted inconsistent target; resyncing");
                            context.sender.sendWaypointList();
                        } else {
                            waypoints.setTarget(b, WaypointListener.Source.REMOTE);
                        }
                    }
                    break;
            }
        }
    }
    private class StringReader implements PacketReader {
        private StateMap sm;
        {
            String dbName = context.getResource("stateDescriptions");
            try (Reader fr = new FileReader(dbName)) {
                sm = StateMap.read(fr);
            } catch (Exception e) {
                seriallog.warning("Can't parse full state descriptions");
                sm = null;
            }
        }
        private String format(String data) {
            if(sm == null) return data;
            Optional<Description> details = sm.getFullDescription(data);
            if(!details.isPresent()) return data;
            Description d = details.get();
            return String.format("Drone: %s %s (from %s)",
                                 d.getName(),
                                 d.getDescription(),
                                 d.getSourceFile());
        }
        public int claim(byte data) {
            if(Serial.getMsgType(data) == Serial.STRING_TYPE) return 255;
            else return -1;
        }
        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            byte[] buff = new byte[msg.length-1];
            for(int i=1; i<msg.length; i++) {
                buff[i-1] = msg[i];
            }
            String data = new String(buff, StandardCharsets.US_ASCII);
            robotlog.info(format(data));
        }
    }
}
