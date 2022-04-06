package com.map.command;

import java.util.Vector;
import java.util.logging.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.JFileChooser;

import com.Context;
import com.map.*;
import com.map.WaypointList;
import com.map.WaypointList.*;
import com.map.command.WaypointCommand.CommandType;
import com.Dashboard;


/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-28-22
 * Description: Command Type class used for processing parsing
 * commands such as saving and loading waypoint lists.
 */
public class WaypointCommandParse extends WaypointCommand {

	private static final Double FEET_PER_METER = 3.28084;
	private static final String GPX_SCHEMA_VERSION = "1.1";
	private static final String GPX_SCHEMA_URI = 
			"http://www.topografix.com/GPX/1/1";

	//Enum of parsing formats supported
	public enum ParseType {XML, JSON};
	//Enum of operational modes supported
	public enum ParseMode {READ, WRITE};
	
	//Enum of XML element tags
	protected enum XMLElement {
		GPXROOT		("gpx"),
		ROUTE		("rte"),
		ROUTEPOINT	("rtep"),
		LATITUDE	("lat"),
		LONGITUDE	("lng"),
		ELEVATION	("ele");
		
		private final String element;
		
		XMLElement(String element) {
			this.element = element;
		} 
		
		public String getValue() {
			return this.element;
		}
	};
	
	//Vars
	protected Context context;
	protected ParseType parseType;
	protected ParseMode parseMode;
	protected String fileName;

	/**
	 * Initializes a parse command object for use and tracking by the Command
	 * Manager.
	 * @param waypoints - The current waypoint list
	 * @param fileName - the filename to either read from or write data to. 
	 * @param parseType - the type of information to parse (See ParseType enum
	 * 						for supported types)
	 * @param parseMode - The mode (Read/Write) of the operation to be performed.
	 */
	public WaypointCommandParse(WaypointList waypoints, Context context, 
			String fileName, ParseType parseType, ParseMode parseMode) {
		super(waypoints, CommandType.PARSE);
		
		this.context = context;
		this.fileName = fileName;
		this.parseType = parseType;
		this.parseMode = parseMode;
	}
	
	/**
	 * Either Reads an existing file for waypoint information and uses it,
	 * or writes a new file using the current waypoint list.
	 * @return boolean - Whether or not the operation was successfull.
	 */
	@Override
	public boolean execute() {
		
		switch(parseMode) {
			case READ:
				read();
				break;
			case WRITE:
				write();
				break;
			default:
				System.err.println("Command Parse - Unrecognized parse mode.");
		}
		
		return true;
	}
	
	/**
	 * Clear the waypoint list, removing all points in the loaded path.
	 * @return boolean - whether or not the operation was successful.
	 */
	@Override
	public boolean undo() {
		CommandManager manager = CommandManager.getInstance();
		
		waypoints.clear(WaypointListener.Source.REMOTE);
		manager.getGeofence().setIsEnabled(false);
		
		if(context.sender != null) {
			context.sender.sendWaypointList();
			context.sender.changeMovement(false);
		}
		
		return true;
	}
	
	/**
	 * Re-execute file parsing.
	 * @return boolean - whether or not the operation was successful.
	 */
	@Override
	public boolean redo() {
		return execute();
	}
	
	/**
	 * Directs reading functionality based on an operations ParseType.
	 */
	protected void read() {
		
		switch(parseType) {
			case XML:
				readXML();
				break;
				
			case JSON:
				//readJSON();
				break;
			default:
				System.err.println("Command Parse - Unrecognized read type");
		}
	}

	/**
	 * Directs writing functionality based on an operations ParseType
	 */
	protected void write() {
		
		switch(parseType) {
			case XML:
				writeXML();
				break;
				
			case JSON:
				//writeJSON();
				break;
			default:
				System.err.println("Command Parse - Unrecognized write type");
		}
	}
	
	/**
	 * Opens an input stream and reads out the GPX formatted XML tags and
	 * corresponding values. If more than one route is found within the parsed
	 * file, the user will be prompted to choose which one they would like to
	 * load.
	 * @return boolean - Whether or not the operation was successful.
	 */
	protected boolean readXML() {
		//XML reading vars 
		FileReader inputStream;
		XMLInputFactory xmlInput;
		XMLStreamReader xmlReader;
		
		//Route data vars
		Vector<Vector<Dot>> routeList = new Vector<Vector<Dot>>();
		Vector<Dot> route = new Vector<Dot>();
		Dot point = new Dot();
		String data = "";
		
		//Open the input stream
		try {
			inputStream = new FileReader(fileName);
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			return false;
		}
		
		//Set up and read GPX file
		try {
			xmlInput = XMLInputFactory.newInstance();
			xmlReader = xmlInput.createXMLStreamReader(inputStream);
			
			//Parse data by tag.
			while(xmlReader.hasNext()) {
				int tag = xmlReader.next();
				switch(tag) {
					case XMLStreamConstants.START_DOCUMENT:
						break;
						
					case XMLStreamConstants.START_ELEMENT:
						if(xmlReader.getLocalName() == XMLElement.ROUTE.getValue()) {
							route = new Vector<Dot>();
						} 
						else if(xmlReader.getLocalName() == XMLElement.ROUTEPOINT.getValue()) {
							double lat, lng;
							
							try {
								lat = Double.parseDouble(xmlReader.getAttributeValue(0));
							}
							catch (NumberFormatException | NullPointerException ex) {
								lat = 0;
							}
							
							try {
								lng = Double.parseDouble(xmlReader.getAttributeValue(1));
							}
							catch (NumberFormatException | NullPointerException ex) {
								lng = 0;
							}
							point = new Dot(lat, lng, (short)0);
						} 
						else if(xmlReader.getLocalName() == XMLElement.ELEVATION.getValue()) {
							//Do nothing for now (May be implimented at a later date
						}						
						break;
					case XMLStreamConstants.CHARACTERS:
						data = xmlReader.getText().trim();
						break;
						
					case XMLStreamConstants.END_ELEMENT:
						if(xmlReader.getLocalName() == XMLElement.ROUTE.getValue()) {
							routeList.add(route);
						}
						else if(xmlReader.getLocalName() == XMLElement.ROUTEPOINT.getValue()) {
							route.add(point);
						}
						else if(xmlReader.getLocalName() == XMLElement.ELEVATION.getValue()) {
							if(point == null) {
								continue;
							}
							
							try {
								point.setAltitude((short)(Double.parseDouble(data) * FEET_PER_METER));
							}
							catch(NumberFormatException | NullPointerException ex) {
								point.setAltitude((short)0);
							}
						}
						break;
						
					default:
						break;
				}
			}
			
			//Close down input stream
			try {
				inputStream.close();
			}
			catch(IOException ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				return false;
			}
			
			//If no routes found, skeedattle
			if(routeList.size() == 0) {
				return false;
			}
			
			//If there is more than one route parsed...
			int selection = 0;
			if(routeList.size() > 1 ) {
				Integer[] options = new Integer[routeList.size()];
				String prompt = "Multiple routes were found;\n"
							  + "Please enter the number of your choice: \n";
				
				
				//create a user prompt for route selection...
				for(int i = 0; i < routeList.size(); i++) {
					options[i] = (Integer)i;
					prompt += "Route " + i + " (" + routeList.get(i).size() + " Points)\n";
				}
				
				//and show a dialog to the user
				selection = (int) JOptionPane.showInputDialog(
						null, prompt, "Pick a route", JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
			}
			
			//Clear waypoint list
			while(waypoints.size() != 0) {
				waypoints.remove(0);
			}
			
			//Place newly parsed waypoints.
			for(int i = 0; i < routeList.get(selection).size(); i++) {
				waypoints.add(routeList.get(selection).get(i), i);
			}
			
			//Shut 'er down chief
			xmlReader.close();
		}
		catch (XMLStreamException ex) {
			System.err.println(ex.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Opens an output stream using the filename and writes out XML data for
	 * the current waypoint route using the GPX 1.1 Schema (See GPX_SCHEMA_URI).
	 * @return - boolean - Whether or not the operation was successful
	 */
	protected boolean writeXML() {
		FileWriter outputStream;
		XMLOutputFactory xmlOutput;
		XMLStreamWriter xmlWriter;
		
		//Open the output stream
		try {
			//Regex looking for: *.gpx extension, if not found, then...
			if(!fileName.matches("^.*[.]gpx$")) {
				//Just add it.
				fileName += ".gpx";
			}
			
			outputStream = new FileWriter(fileName);
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			return false;
		}

		//Set up and write GPX file
		try {			
			xmlOutput = XMLOutputFactory.newInstance();
			xmlWriter = xmlOutput.createXMLStreamWriter(outputStream);			

			//Namespace
			xmlWriter.writeStartDocument();
			xmlWriter.setDefaultNamespace(GPX_SCHEMA_URI);
			
			//GPX (Root)
			xmlWriter.writeStartElement(GPX_SCHEMA_URI, XMLElement.GPXROOT.getValue());
			xmlWriter.writeAttribute("version", GPX_SCHEMA_VERSION);
			xmlWriter.writeAttribute("creator", "MINDSi Dashboard");
			xmlWriter.writeCharacters("\n");
			
			//Route
			xmlWriter.writeStartElement(GPX_SCHEMA_URI, XMLElement.ROUTE.getValue());
			xmlWriter.writeCharacters("\n");
			
			//Route Points
			for(int i = 0; i < waypoints.size(); i++) {
				xmlWriter.writeStartElement(GPX_SCHEMA_URI, XMLElement.ROUTEPOINT.getValue());
				
				xmlWriter.writeAttribute(
						XMLElement.LATITUDE.getValue(), 
						Double.toString(waypoints.get(i).dot().getLatitude()));
				
				xmlWriter.writeAttribute(
						XMLElement.LONGITUDE.getValue(), 
						Double.toString(waypoints.get(i).dot().getLongitude()));
				xmlWriter.writeCharacters("\n");
				
				xmlWriter.writeStartElement(GPX_SCHEMA_URI, XMLElement.ELEVATION.getValue());
				xmlWriter.writeCharacters(
						Double.toString(waypoints.get(i).dot().getAltitude() / FEET_PER_METER));
				
				//End XMLElement.ELEVATION
				xmlWriter.writeEndElement(); 
				xmlWriter.writeCharacters("\n");
				
				//End XMLElement.ROUTPOINT
				xmlWriter.writeEndElement(); 
				xmlWriter.writeCharacters("\n");
			}
			
			//End XMLElement.ROUTE
			xmlWriter.writeEndElement();
			xmlWriter.writeCharacters("\n");
			
			//End XMLElement.GPXROOT
			xmlWriter.writeEndElement(); 
			xmlWriter.flush();
			
			//Close down writer and output stream
			try {
				xmlWriter.close();
				outputStream.close();
			}
			catch(IOException ex) {
				System.err.println(ex.getMessage());
				return false;
			}
			
			return true;
		}
		catch(XMLStreamException ex) {
			System.err.println(ex.getMessage());
			return false;
		}
	}
	
	/**
	 * Returns the filename for this command.
	 * @return String - the filename
	 */
	public String getFilename() {
		return fileName;
	}
	
	
}
