package com.map;

import com.Dashboard;
import com.map.coordinateListener;
import com.map.Dot;
import com.serial.*;
import com.ui.DataWindow;
import com.xml;
import com.ContextViewer;
import com.Context;
import com.ui.Graph;
import com.ui.Theme;
import com.ui.ninePatch.NinePatchPanel;
import com.ui.SystemConfigWindow;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;

class WaypointPanel extends NinePatchPanel implements ContextViewer{
	protected static final int MOVE_STEP = 32;
	protected static final int BDR_SIZE = 25;
	protected static final String NO_WAYPOINT_MSG = "N / A";
	private int selectedWaypoint = 0;
	private Context context;
	private MapPanel map;
	JTextField latitude;
	JTextField longitude;
	JTextField altitude;
	JLabel waypointIndexDisplay;

	public WaypointPanel(Context cxt, MapPanel mapPanel) {
		super(cxt.theme.panelPatch);
		map = mapPanel;
		context = cxt;
		context.registerViewer(this);
		setOpaque(false);
		LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(BDR_SIZE,BDR_SIZE,BDR_SIZE,BDR_SIZE));
		buildPanel();
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent me) {
				//do nothing here to prevent clicks from falling through
				//to the map panel in the background
			}
		});
	}

	public void waypointUpdate(){
		updateDisplay();
	}

	public int getSelectedWaypoint(){
		return selectedWaypoint;
	}

	public void setSelectedWaypoint(int selected){
		selectedWaypoint = selected;
		updateDisplay();
	}

	public void updateDisplay(){
		if (selectedWaypoint > context.waypoint.size()-1) selectedWaypoint = context.waypoint.size()-1;
		else if(selectedWaypoint < 0) selectedWaypoint = 0;

		if(context.waypoint.size() == 0) {
			waypointIndexDisplay.setText(NO_WAYPOINT_MSG);
			latitude .setText("");
			longitude.setText("");
			altitude .setText("");
			return;
		}

		String indexField = (selectedWaypoint+1) + " / " + context.waypoint.size();
		waypointIndexDisplay.setText(indexField);
		if(selectedWaypoint >= 0 && selectedWaypoint < context.waypoint.size()) {
			Dot dot = context.waypoint.get(selectedWaypoint);
			latitude .setText(((float)dot.getLatitude())+"");
			latitude .setForeground(Color.BLACK);
			longitude.setText(((float)dot.getLongitude())+"");
			longitude.setForeground(Color.BLACK);
			altitude .setText(fixedToDouble(dot.getAltitude())+"");
			altitude .setForeground(Color.BLACK);
		}
	}

	private void buildPanel(){
		final Theme theme 			= context.theme;
		final Dimension space		= new Dimension(0,5);
		final Dimension buttonSize	= new Dimension(140, 25);

		//make all the buttons
		JButton tileButton 	= theme.makeButton(nextTileServer);
		JButton dataPanel 	= theme.makeButton(openDataPanel);
		JButton graphButton = theme.makeButton(buildGraph);
		JButton reTarget 	= theme.makeButton(reTargetRover);
		JButton looping 	= theme.makeButton(toggleLooping);
		JButton config      = theme.makeButton(openConfigWindow);
		JComponent[] format = new JComponent[]{
			tileButton, dataPanel, graphButton, reTarget, looping, config
		};
		for(JComponent jc : format){
			jc.setAlignmentX(Component.CENTER_ALIGNMENT);
			jc.setMaximumSize(buttonSize);
		}

		//make zoom button panel
		JPanel zoom = new JPanel(new FlowLayout());
		zoom.setOpaque(false);
		zoom.add(theme.makeButton(zoomInAction));
		zoom.add(theme.makeButton(zoomOutAction));

		//add selectedWaypoint flow layout
		JPanel selector = new JPanel(new BorderLayout());
		selector.setOpaque(false);
		waypointIndexDisplay = new JLabel(NO_WAYPOINT_MSG);
		waypointIndexDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		selector.add(theme.makeButton(previousWaypoint), BorderLayout.LINE_START);
		selector.add(waypointIndexDisplay, BorderLayout.CENTER);
		selector.add(theme.makeButton(nextWaypoint), BorderLayout.LINE_END);

		//Make edit boxes
		class EditBoxSpec{
			public final JTextField ref;
			public final String label;
			public EditBoxSpec(JTextField ref, String label){
				this.ref = ref;
				this.label = label;
			}
		}
		ArrayList<EditBoxSpec> editorBoxes = new ArrayList<EditBoxSpec>();
		ResourceBundle res = ResourceBundle.getBundle("resources", context.locale);
		latitude  = new JTextField();
		longitude = new JTextField();
		altitude  = new JTextField();
		editorBoxes.add(new EditBoxSpec(latitude , "Lat: "));
		editorBoxes.add(new EditBoxSpec(longitude, "Lng: "));
		editorBoxes.add(new EditBoxSpec(altitude , res.getString("waypointExtra")+" "));
		ArrayList<JPanel> editorPanels = new ArrayList<JPanel>();
		for(EditBoxSpec box : editorBoxes){
			//construct panel
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.setOpaque(false);
			//add label box
			JLabel label = new JLabel(box.label);
			label.setFont(context.theme.text);
			panel.add(label);
			//add textField
			JTextField tf = box.ref;
			tf.setBorder(BorderFactory.createLineBorder(Color.gray));
			panel.add(tf);
			//Setup text field listener
			coordinateListener listener = new coordinateListener(tf, this);
			tf.getDocument().addDocumentListener(listener);
			tf.addActionListener(listener);
			//add to layout
			editorPanels.add(panel);
		}

											//rows, cols, hgap, vgap
		JPanel waypointOptions = new JPanel(new GridLayout(2,2,5,5));
		waypointOptions.setOpaque(false);
		waypointOptions.add(theme.makeButton(newWaypoint));
		waypointOptions.add(theme.makeButton(interpretLocationAction));
		waypointOptions.add(theme.makeButton(saveWaypoints));
		waypointOptions.add(theme.makeButton(loadWaypoints));

		add(config);
		add(Box.createRigidArea(space));
		add(tileButton);
		add(zoom);
		add(dataPanel);
		add(Box.createRigidArea(space));
		add(graphButton);
		add(Box.createRigidArea(space));
		add(new JSeparator(SwingConstants.HORIZONTAL));
		add(Box.createRigidArea(space));
		add(selector);
		add(Box.createRigidArea(space));
		for(JPanel panel : editorPanels){ add(panel); }
		add(Box.createRigidArea(space));
		add(waypointOptions);
		add(Box.createRigidArea(space));
		add(reTarget);
		add(Box.createRigidArea(space));
		add(looping);
	}

	private double fixedToDouble(int i){
		return ((double)(i&0xffff))/((double)Serial.U16_FIXED_POINT);
	}
	private int    doubleToFixed(double i){
		return (int)(i*Serial.U16_FIXED_POINT);
	}

	public void interpretLocationEntry(){
		try{
			Double newLatitude  = Double.parseDouble(latitude.getText());
			Double newLongitude = Double.parseDouble(longitude.getText());
			Double tmpAltitude  = Double.parseDouble(altitude.getText());

			int newAltitude = doubleToFixed(tmpAltitude);
			Point.Double newPosition = new Point.Double(newLongitude, newLatitude);
			if((newAltitude&0xffff) == newAltitude){
				context.waypoint.set(selectedWaypoint, newPosition, (short)newAltitude);
				//set to display reconverted value
				altitude.setText(fixedToDouble(newAltitude)+"");
			} else {
				context.waypoint.set(selectedWaypoint, newPosition);
			}
		} catch (NumberFormatException e) {}
	}

	private Action zoomInAction = new AbstractAction() {
		{
			String text = "+";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			map.zoomInAnimated(new Point(map.getWidth() / 2, map.getHeight() / 2));
		}
	};
	private Action zoomOutAction = new AbstractAction() {
		{
			String text = "-";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			map.zoomOutAnimated(new Point(map.getWidth() / 2, map.getHeight() / 2));
		}
	};
	private Action nextTileServer = new AbstractAction() {
		String satMSG = "Satellite";
		String mapMSG = "Street";
		boolean state = false;
		{
			putValue(Action.NAME, (state)?satMSG:mapMSG);
		}

		public void actionPerformed(ActionEvent e){
			map.nextTileServer();
			state = !state;
			putValue(Action.NAME, (state)?satMSG:mapMSG);
			map.repaint();
		}
	};
	private Action toggleLooping = new AbstractAction(){
		{
			String text = "Looping On";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e){
			context.waypoint.setLooped(!context.waypoint.isLooped());
			putValue(Action.NAME,
				(context.waypoint.isLooped())? "Looping Off" : "Looping On");
		}
	};
	private Action previousWaypoint = new AbstractAction(){
		{
			String text = "<";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
			setSelectedWaypoint(selectedWaypoint-1);
			updateDisplay();
			map.repaint();
		}
	};
	private Action nextWaypoint = new AbstractAction(){
		{
			String text = ">";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
			setSelectedWaypoint(selectedWaypoint+1);
			updateDisplay();
			map.repaint();
		}
	};
	private Action saveWaypoints = new AbstractAction(){

		{
			String text = "Save";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
			try{
				xml.writeXML(context);
			} catch (XMLStreamException ex){
				System.err.println(ex.getMessage());
			}
		}
	};
	private Action loadWaypoints = new AbstractAction(){
		{
			String text = "Load";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
			try{
				xml.readXML(context);
			} catch (XMLStreamException ex){
				System.err.println(ex.getMessage());
			}
			context.waypointUpdated();
		}
	};
	private Action interpretLocationAction = new AbstractAction(){
		{
			String text = "Enter";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			interpretLocationEntry();
		}
	};
	private Action reTargetRover = new AbstractAction(){
		{
			String text = "Set Target";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			context.waypoint.setTarget(selectedWaypoint);
		}
	};
	private Action newWaypoint = new AbstractAction(){
		{
			String text = "New";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			context.waypoint.add( new Dot(context.waypoint.get(selectedWaypoint)), selectedWaypoint );
		}
	};
	private Action openDataPanel = new AbstractAction(){
		{
			String text = "Data Log";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			final DataWindow window = new DataWindow(context);
		}
	};
	private Action openConfigWindow = new AbstractAction(){
		{
			String text = "Configuration";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			final SystemConfigWindow window = new SystemConfigWindow(context);
		}
	};
	private Action buildGraph = new AbstractAction(){
		{
			String text = "Graph";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			Graph graph = new Graph(context.telemetry.getDataSources());
			graph.setPreferredSize(new Dimension(500, 300));
			JFrame gFrame = new JFrame("Telemetry Graph");
			gFrame.add(graph);
			gFrame.pack();
			gFrame.setVisible(true);
		}
	};
}

