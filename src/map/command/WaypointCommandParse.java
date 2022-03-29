package com.map.command;

import java.util.logging.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.*;

import javax.swing.filechooser.*;
import javax.swing.JFileChooser;

import com.map.WaypointList;
import com.map.command.WaypointCommand.CommandType;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-28-22
 * Description: Command Type class used for processing parsing
 * commands such as saving and loading waypoint lists.
 */
public class WaypointCommandParse extends WaypointCommand {

	private static final String SCHEMA_URI = "http://www.topografix.com/GPX/1/1";
	private static final String SCHEMA_VERSION = "1.1";
	
	//Enum ofparsing formats supported
	public enum ParseType {XML, JSON};
	//Enum of Operational modes supported
	public enum ParseMode {READ, WRITE};
	
	//Enum of XML Element Tags
	protected enum XMLElement {
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
	protected ParseType parseType;
	protected ParseMode parseMode;
	protected String fileName;

	/**
	 * 
	 * @param waypoints
	 * @param fileName
	 * @param parseType
	 * @param parseMode
	 */
	public WaypointCommandParse(WaypointList waypoints, String fileName, 
			ParseType parseType, ParseMode parseMode) {
		super(waypoints, CommandType.PARSE);
		
		this.fileName = fileName;
		this.parseType = parseType;
		this.parseMode = parseMode;
	}
	
	/**
	 * 
	 * @return
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
				//Add error here
		}
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public boolean undo() {
		//TODO - CP - FILE SAVE/LOAD - Determine if should implement undo
		//Clear Waypoints Command Equivelent
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public boolean redo() {
		//TODO - CP - FILE SAVE/LOAD - Determine if should implement redo
		return false;
	}
	
	//TODO - CP - FILE SAVE/LOAD - Impl. read XML
	/**
	 * 
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
	
		}
	}

	//TODO - CP - FILE SAVE/LOAD - Impl. write XML
	/**
	 * 
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
		
		}
	}
	
	/**
	 * 
	 * @return
	 */
	protected boolean readXML() {
		
		
		return false;
	}
	
	
	//TODO - CP - FILE SAVE/LOAD - Will need to throw XMLStreamException
	/**
	 * 
	 * @return
	 */
	protected boolean writeXML() {
		FileWriter outputStream;
		
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
		
		//Set up and verify xml writer
		try {
			XMLOutputFactory xmlOutput = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlWriter = 
					xmlOutput.createXMLStreamWriter(outputStream);			
		}
		catch(XMLStreamException ex) {
			//Error out here
			return false;
		}

		//TODO - CP - FILE SAVE/LOAD - Finish Write function here.
		
		
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFilename() {
		return fileName;
	}
	
	
}
