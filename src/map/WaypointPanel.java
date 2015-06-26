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

class WaypointPanel extends JPanel implements ContextViewer{
	/**
	 * Make this just extend NinePatchPanel
	 */

	protected static final int MOVE_STEP = 32;
	protected static final int BDR_SIZE = 25;
	protected static final String NO_WAYPOINT_MSG = "N / A";
	protected static final String COPY_RIGHT_TEXT = "Map Tiles Courtesy of MapQuest" +
		"\nStreet Data from OpenStreetMap\nPortions Courtesy NASA/JPL-Caltech" +
		"and U.S. Depart. of Agriculture, Farm Service Agency";
	private int selectedWaypoint = 0;
	private Context context;
	private MapPanel map;
	JTextField latitude;
	JTextField longitude;
	JTextField altitude;
	JLabel waypointIndexDisplay;

	public WaypointPanel(Context cxt, MapPanel mapPanel) {
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

/*		Action actionButtons = new Action[]{
			nextTileServer  ,openDataPanel
		   ,buildGraph		,reTargetRover
		   ,toggleLooping
		};*/

		JButton model = theme.makeButton();
		model.setAlignmentX(Component.CENTER_ALIGNMENT);
		model.setMaximumSize(buttonSize);

		//add tile server switcher button
		JButton tileButton = theme.makeButton(nextTileServer);
		tileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		tileButton.setMaximumSize(buttonSize);
		add(tileButton);
		//add zooming flow layout
		JPanel zoom = new JPanel(new FlowLayout());
		zoom.setOpaque(false);
		zoom.add(theme.makeButton(zoomInAction));
		zoom.add(theme.makeButton(zoomOutAction));
		add(zoom);


		//open the data management panel
		JButton dataPanel = theme.makeButton(openDataPanel);//theme.makeButton(openDataPanel);
		dataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		dataPanel.setMaximumSize(buttonSize);
		add(dataPanel);
		add(Box.createRigidArea(space));
		//make a new graph
		JButton graphButton = theme.makeButton(buildGraph);
		graphButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		graphButton.setMaximumSize(buttonSize);
		add(graphButton);
		add(Box.createRigidArea(space));
		//add spacer
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		add(sep);
		add(Box.createRigidArea(space));

		//add selectedWaypoint flow layout
		JPanel selector = new JPanel(new BorderLayout());
		selector.setOpaque(false);
		waypointIndexDisplay = new JLabel(NO_WAYPOINT_MSG);
		waypointIndexDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		selector.add(theme.makeButton(previousWaypoint), BorderLayout.LINE_START);
		selector.add(waypointIndexDisplay, BorderLayout.CENTER);
		selector.add(theme.makeButton(nextWaypoint), BorderLayout.LINE_END);
		add(selector);
		add(Box.createRigidArea(space));


		class EditBoxSpec{
			public final JTextField ref;
			public final String label;
			public EditBoxSpec(JTextField ref, String label){
				this.ref = ref;
				this.label = label;
			}
		}
		ArrayList<EditBoxSpec> editorBoxes = new ArrayList<EditBoxSpec>();

		ResourceBundle res = ResourceBundle.getBundle("settingLabels", context.locale);
		latitude = new JTextField();
		longitude = new JTextField();
		altitude = new JTextField();
		editorBoxes.add(new EditBoxSpec(latitude , "Lat: "));
		editorBoxes.add(new EditBoxSpec(longitude, "Lng: "));
		editorBoxes.add(new EditBoxSpec(altitude , res.getString("waypointExtra")+" "));

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
			add(panel);
		}

		//add enter button
		JPanel waypointOptions = new JPanel(new FlowLayout());
		waypointOptions.setOpaque(false);
		waypointOptions.add(theme.makeButton(newWaypoint));
		waypointOptions.add(theme.makeButton(interpretLocationAction));
		add(waypointOptions);

		//add button to reset the rover's target
		JButton reTarget = theme.makeButton(reTargetRover);
		reTarget.setAlignmentX(Component.CENTER_ALIGNMENT);
		reTarget.setMaximumSize(buttonSize);
		add(reTarget);
		add(Box.createRigidArea(space));

		//add looping button
		JButton looping = theme.makeButton(toggleLooping);
		looping.setAlignmentX(Component.CENTER_ALIGNMENT);
		looping.setMaximumSize(buttonSize);
		add(looping);

		//add save/load flow layout
		JPanel saveload = new JPanel(new FlowLayout());
		saveload.setOpaque(false);
		saveload.add(theme.makeButton(saveWaypoints));
		saveload.add(theme.makeButton(loadWaypoints));
		add(saveload);

		JTextArea copyRights = new JTextArea();
		Font tmp = copyRights.getFont();
		copyRights.setFont( tmp.deriveFont(7f) );
		copyRights.setOpaque(false);
		copyRights.setLineWrap(true);
		copyRights.setBorder(new EmptyBorder(0,0,0,0));
		copyRights.setText(COPY_RIGHT_TEXT);
		add(copyRights);
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

	public void paint(Graphics gOrig) {
		Graphics2D g = (Graphics2D) gOrig.create();
		try {
			int w = getWidth(), h = getHeight();
			//drawBackground(g, w, h);
			context.theme.panelPatch.paintIn(g, w, h);
		} finally {
			g.dispose();
		}
		super.paint(gOrig);
	}

	private static void drawBackground(Graphics2D g, int width, int height) {
		Color color1 = new Color(0xc0, 0xc0, 0xc0);
		Color color2 = new Color(0xe0, 0xe0, 0xe0);
		Composite oldComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));
		g.setPaint(new GradientPaint(0, 0, color1, 0, height*2, color2));
		g.fillRoundRect(-1, -1, width, height, 12, 12);
		g.setComposite(oldComposite);
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

