package com.serial;

import com.Context;
import com.data.stateDescription.Description;
import com.data.stateDescription.StateMap;
import com.map.WaypointList;
import com.serial.Messages.Message;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.map.WaypointList.WaypointListener;

public class SerialParser implements SerialPortEventListener {
    private final Context context;
    private final WaypointList waypoints;
    private final CommsMonitor commsMonitor;
    private final Logger seriallog = LoggerFactory.getLogger("d.serial");
    private final Logger robotlog = LoggerFactory.getLogger("d.robot");
    private Decoder decoder;
    private StateMap descriptionMap;

    public SerialParser(Context cxt, WaypointList waypoints) {
        context = cxt;
        this.waypoints = waypoints;
        commsMonitor = CommsMonitor.getInstance();
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
        }
        catch (SerialPortException ex) {
            seriallog.error("Error opening serial port", ex);
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
            if (Serial.getMsgType(data) == Serial.DATA_TYPE) {
                return 255;
            }
            else {
                return -1;
            }
        }

        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            int index = msg[1];

            int tempdata;
            float data;

            switch (subtype) {
                case Serial.TELEMETRY_DATA:
                    tempdata = (((msg[2] & 0xff) << 24) |
                            ((msg[3] & 0xff) << 16) |
                            ((msg[4] & 0xff) << 8) |
                            ((msg[5] & 0xff)));
                    data = Float.intBitsToFloat(tempdata);
                    context.setTelemetry(index, data);
                    break;

                case Serial.SETTING_DATA:
                    tempdata = (((msg[2] & 0xff) << 24) |
                            ((msg[3] & 0xff) << 16) |
                            ((msg[4] & 0xff) << 8) |
                            ((msg[5] & 0xff)));
                    data = Float.intBitsToFloat(tempdata);
                    context.setSettingQuiet(index, data);
                    break;

                case Serial.SENSOR_DATA:
                    int sensorVal = 0;
                    int sensorSubtype = msg[1];
                    int sensorIndex = msg[2];
                    int[] sensorData = new int[2];

                    sensorData[0] = (msg[3] & 0xff);
                    sensorData[1] = (msg[4] & 0xff);
                    for (int val : sensorData) {
                        sensorVal = (sensorVal << 8) | val;
                    }

                    switch (sensorSubtype) {
                        case Serial.OBJDETECT_SONIC:
                            //SensorData: [0]MSB [1]LSB
                            context.dash.pingWidget.update(sensorIndex, sensorVal);
                            break;

                        case Serial.OBJDETECT_BUMPER:
                            //SensorData: 0 = Off, 1 = On
                            context.dash.bumperWidget.update(sensorIndex, sensorVal);
                            break;

                        default:
                            seriallog.error(
                                    "SerialParser - Sensor Data -"
                                            + "Unrecognized Sensor Subtype");
                            break;
                    }
                    break;
                case Serial.INFO_DATA:
                    int infoSubtype = msg[1];

                    switch (infoSubtype) {
                        case Serial.APM_VERSION:
                            int versionMajor = msg[2];
                            int versionMinor = msg[3];
                            int versionRev = msg[4];

                            context.setAPMVersion(String.format("%d.%d.%d",
                                    versionMajor, versionMinor, versionRev));
                            break;

                        case Serial.HEARTBEAT:
                            int pulse = msg[2];

                            commsMonitor.receiveHeartbeatPulse(pulse);
                            break;

                        default:
                            seriallog.error(
                                    "SerialParser - Info Data - "
                                            + "Unrecognized Info Subtype");
                            break;
                    }

                    break;
            }
        }
    }

    private class WordReader implements PacketReader {
        public int claim(byte data) {
            if (Serial.getMsgType(data) == Serial.WORD_TYPE) {
                return 255;
            }
            else {
                return -1;
            }
        }

        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            byte a = (byte) (msg[1] & 0xff);
            byte b = (byte) (msg[2] & 0xff);
            int join = (((a << 8) & 0xFF00) | (b & 0xFF));

            switch (subtype) {
                case Serial.CONFIRMATION:
                    SerialSendManager.getInstance().addConfirmToQueue(join);
                    break;

                case Serial.SYNC_WORD: {
                    if (a == Serial.SYNC_REQUEST) {
                        SerialSendManager.getInstance().addMessageToQueue(
                                Message.syncMessage(Serial.SYNC_RESPOND));
                    }

                    SerialSendManager.getInstance().sendWaypointList(waypoints);
                }
                break;

                case Serial.COMMAND_WORD:
                    if (a == Serial.TARGET_CMD) {
                        if (b < 0 || b >= waypoints.size()) {
                            seriallog.error("Rover transmitted inconsistent target; resyncing");

                            SerialSendManager.getInstance().sendWaypointList(waypoints);
                        }
                        else {
                            waypoints.setTarget(b, WaypointListener.Source.REMOTE);
                            seriallog.error("SerialParser - New target index from rover: " + b);
                        }
                    }
                    break;

                case Serial.STATE_WORD:
                    context.dash.stateWidget.update(a, b);
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
            }
            catch (Exception e) {
                seriallog.error("Can't parse full state descriptions", e);
                sm = null;
            }
        }

        private String format(String data) {
            if (sm == null) {
                return data;
            }
            Optional<Description> details = sm.getFullDescription(data);
            if (!details.isPresent()) {
                return data;
            }
            Description d = details.get();
            return String.format("Drone: %s %s (from %s)",
                    d.getName(),
                    d.getDescription(),
                    d.getSourceFile());
        }

        public int claim(byte data) {
            if (Serial.getMsgType(data) == Serial.STRING_TYPE) {
                return 255;
            }
            else {
                return -1;
            }
        }

        public void handle(byte[] msg) {
            int subtype = Serial.getSubtype(msg[0]);
            byte[] buff = new byte[msg.length - 1];
            System.arraycopy(msg, 1, buff, 0, msg.length - 1);
            String data = new String(buff, StandardCharsets.US_ASCII);
            robotlog.info(format(data));
        }
    }
}
