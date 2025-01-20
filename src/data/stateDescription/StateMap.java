package com.data.stateDescription;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read and contain a map of short state description Strings to full length
 * descriptions that have been taken from the drone source code comments
 * and put into an xml database
 */
public class StateMap {
    private final Map<String, Description> map;

    private StateMap(Reader xmlDatabase) throws ParseException {
        map = new ConcurrentHashMap<>();
        // parse the xml database into a map of String=>Descriptions
        try {
            XMLStreamReader r = XMLInputFactory.newInstance().
                    createXMLStreamReader(xmlDatabase);
            String name = null, file = null;
            StringBuilder text = null;
            while (r.hasNext()) {
                switch (r.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (r.getLocalName().equals("Message")) {
                            name = r.getAttributeValue(null, "name");
                            file = r.getAttributeValue(null, "path");
                            text = new StringBuilder();
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        text.append(r.getText().trim());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (name != null && file != null && text != null) {
                            addDescription(name, file, text.toString());
                            name = null;
                            file = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (Exception e) {
            throw new ParseException("Failed to parse XML from the stream" + e, 0);
        }
    }

    /**
     * Create a StateMap using the data in `xmlDatabase`
     */
    public static StateMap read(Reader xmlDatabase) throws ParseException {
        return new StateMap(xmlDatabase);
    }

    private void addDescription(String name, String file, String text) {
        Description d = new Description(name, file, text);
        map.put(name, d);
    }

    /**
     * Retreive an expanded descrption of `state` if available
     */
    public Optional<Description> getFullDescription(String state) {
        Description d = map.get(state);
        if (d == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(d);
        }
    }
}
