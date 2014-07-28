package com;

import com.Dashboard;
import com.map.*;
import com.Context;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.JFileChooser;
import javax.xml.stream.*;

public class xml{
	public static String getFileName(String title){
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"GPX files", "xml", "gpx");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getPath();
		}
		return "";
	}

	public static void writeXML(Context context) throws XMLStreamException{
		FileWriter outputStream;
		try{
			String fileName = getFileName("Choose a file to save");
			if(!fileName.matches("^.*[.]gpx$")) fileName+=".gpx";
			outputStream = new FileWriter(fileName);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			return;
		}
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		XMLStreamWriter  writer = output.createXMLStreamWriter(outputStream);

		String URI = "http://www.topografix.com/GPX/1/1";
		writer.writeStartDocument();
		writer.setDefaultNamespace(URI);

		writer.writeStartElement(URI, "gpx");
		writer.writeAttribute("version","1.1");
		writer.writeAttribute("creator","MINDSi Dashboard");
		writer.writeCharacters("\n");

		writer.writeStartElement(URI, "rte");
		writer.writeCharacters("\n");

		WaypointList p = context.waypoint;
		for(int i=0; i<p.size(); i++){
			writer.writeStartElement(URI, "rtept");
			writer.writeAttribute("lat", Double.toString(p.get(i).getLatitude ()));
			writer.writeAttribute("lng", Double.toString(p.get(i).getLongitude()));
			writer.writeCharacters("\n");
			writer.writeStartElement(URI, "ele");
			writer.writeCharacters(Double.toString(p.get(i).getAltitude()/3.28084));//feet to meters
			writer.writeEndElement();// /ele
			writer.writeCharacters("\n");
			writer.writeEndElement();// /rtept
			writer.writeCharacters("\n");
		}

		writer.writeEndElement();// /rte
		writer.writeCharacters("\n");
		writer.writeEndElement();// /gpx
		writer.flush();
		try{
			outputStream.close();
		} catch (IOException ex){
			System.err.println(ex.getMessage());
		}
	}

	public static void readXML(Context context) throws XMLStreamException{
		Dot 				 pnt = new Dot();
		Vector<Dot>     	 list = new Vector<Dot>();
		Vector<Vector>       routes = new Vector<Vector>();
		XMLInputFactory 	 factory = XMLInputFactory.newInstance();
		String 				 data = new String();
		XMLStreamReader 	 reader;
		FileReader 			 inputStream;
		try{
			inputStream = new FileReader(getFileName("Choose file to open"));
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			return;
		}
		reader = factory.createXMLStreamReader(inputStream);

		while(reader.hasNext()){
			int event = reader.next();
			switch(event){
				case XMLStreamConstants.START_DOCUMENT:
					break;
				case XMLStreamConstants.START_ELEMENT:
					switch(reader.getLocalName()){
						case "rte":
							list = new Vector<Dot>();
							break;
						case "rtept":
							double lat, lng;
							try {
								lat = Double.parseDouble(reader.getAttributeValue(0));
							} catch (NumberFormatException | NullPointerException e) {
								lat = 0;
							}
							try {
								lng = Double.parseDouble(reader.getAttributeValue(1));
							} catch (NumberFormatException | NullPointerException e) {
								lng = 0;
							}
							pnt = new Dot(lat,lng,(short)0);
							break;
						case "ele":
							break;
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					data = reader.getText().trim();
					break;
				case XMLStreamConstants.END_ELEMENT:
					switch(reader.getLocalName()){
						case "rte":
							if(list == null || routes == null) continue;
							routes.add(list);
							break;
						case "rtept":
							if(pnt == null || list == null) continue;
							list.add(pnt);
							break;
						case "ele":
							if(pnt == null) continue;
							try {
								//convert meters to feet
								pnt.setAltitude((short) (Double.parseDouble(data)*3.28084) );
							} catch (NumberFormatException | NullPointerException e) {
								pnt.setAltitude((short)0);
							}
							break;
					}
					break;
				default:
					break;
			}
		}
		try{
			inputStream.close();
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
		if(routes.size()==0) return;

		int selection = 0;
		if(routes.size()>1){
			Integer[] options = new Integer[routes.size()];
			String prompt = "Multiple routes were found;\n"
							+"Please enter the number of your choice: \n";
			for(int i=0; i<routes.size(); i++){
				options[i] = (Integer)i;
				prompt += "Route "+i+" ("+routes.get(i).size()+" Points)\n";
			}
			selection = (int)JOptionPane.showInputDialog(
												null,
												prompt,
												"Pick a route",
												JOptionPane.PLAIN_MESSAGE,
												null,
												options,
												options[0]);
		}

		//set mapPanel points to new points
		context.waypoint.swap(routes.get(selection));
		context.sender.sendWaypointList();
	}
}
