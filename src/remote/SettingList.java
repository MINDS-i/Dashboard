package com.remote;

import com.Context;
import com.serial.Messages.Message;
import com.serial.Serial;
import com.serial.SerialSendManager;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SettingList {
    private final List<Setting> settingData = new ArrayList<>();
    private final Context context;

    public SettingList(Context cxt) {
        context = cxt;
        loadSettingData();
    }

    public int size() {
        return settingData.size();
    }

    public Setting get(int id) {
        return settingData.get(id);
    }

    public void pushSetting(int id, float val) {
        if (id < 0 || id >= settingData.size()) {
            return;
        }
        updateSettingVal(id, val);
        pushSetting(id);
    }

    public void pushSetting(int id) {
        Message msg = Message.setSetting((byte) id, settingData.get(id).getVal());
        SerialSendManager.getInstance().addMessageToQueue(msg);
    }

    public void updateSettingVal(int id, float val) {
        if (id < 0 || id >= settingData.size()) {
            return;
        }
        settingData.get(id).setVal(val);
    }

    private float getFloat(XMLStreamReader reader, String label) {
        String raw = reader.getAttributeValue(null, label);
        if (raw == null) {
            return 0.0f;
        }
        //strip whitespace
        raw = raw.replaceAll("\\s", "");
        //parse
        if (raw.equals("+inf")) {
            return Float.POSITIVE_INFINITY;
        }
        else if (raw.equals("-inf")) {
            return Float.NEGATIVE_INFINITY;
        }
        else {
            return Float.parseFloat(raw);
        }
    }

    private void loadSettingData() {
        settingData.clear();
        for (int i = 0; i < Serial.MAX_SETTINGS; i++) {
            settingData.add(new Setting("#" + i, "", 0, 0, 0));
        }

        try {
            File xmlFile = new File(context.getResource("setting_list"));
            FileReader input = new FileReader(xmlFile);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(input);

            Setting tmp = null;
            StringBuilder str = new StringBuilder();
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (!reader.getLocalName().equals("setting")) {
                            continue;
                        }
                        int index = Integer.parseInt(reader.getAttributeValue(null, "index"));
                        if (index >= settingData.size()) {
                            System.err.println("Setting doc has index outside of bounds");
                            continue;
                        }
                        tmp = settingData.get(index);
                        tmp.name = reader.getAttributeValue(null, "name");
                        tmp.min = getFloat(reader, "min");
                        tmp.max = getFloat(reader, "max");
                        tmp.def = getFloat(reader, "def");
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (tmp != null) {
                            str.append(reader.getText());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (tmp != null) {
                            tmp.description = str.toString();
                            str = new StringBuilder();
                        }
                        tmp = null;
                        break;
                    default:
                        break;
                }
            }

            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
