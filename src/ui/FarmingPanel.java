package com.ui;

import com.Context;

import com.map.MapPanel;
import com.map.RoverPath;

import com.ui.ninePatch.NinePatchPanel;
import com.ui.widgets.*;
import com.ui.widgets.SwathPreviewWidget.SwathType;
import com.ui.widgets.SwathPreviewWidget.SwathRotation;
import com.ui.widgets.SwathPreviewWidget.SwathInversion;
import com.ui.Theme;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import javax.swing.*;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-21-23
 * Description: Main panel container for holding a group of
 * farming related dashboard UIWidgets.
 */
public class FarmingPanel extends WidgetPanel {
	
	final Theme theme;
	
	//Swath Preview Panel
	private SwathPreviewWidget swathPreviewWidget;
	
	//Type Panel and Internals
	private JPanel typeButtonPanel;
	private JLabel typeGroupLabel;
	private JRadioButton btnHorizontal;
	private JRadioButton btnVertical;
	private ButtonGroup typeButtonGroup;
	
	//Rotation Panel and Internals
	private JPanel rotationButtonPanel;
	private JLabel rotationGroupLabel;
	private JRadioButton btnNotRotated;
	private JRadioButton btnRotated;
	private ButtonGroup rotationButtonGroup;
	
	//Inversion Panel and internals
	private JPanel inversionButtonPanel;
	private JLabel inversionGroupLabel;
	private JRadioButton btnNotInverted;
	private JRadioButton btnInverted;
	private ButtonGroup inversionButtonGroup;
	
	//Mode Panel and Internals
	private JPanel modePanel;
	private JButton swathModeButton;
	
	
	public FarmingPanel(Context ctx, int layoutType) {
		super(ctx, layoutType);
		
		theme = context.theme;
		
		swathPreviewWidget = new SwathPreviewWidget(context);
		this.add(swathPreviewWidget);
		
		//Create all button panel groups and their internals
		createTypeButtonGroup();
		createRotationButtonGroup();
		createInversionButtonGroup();
		createSwathModeButtons();
		
      //Do nothing here to prevent clicks from falling through to map panel
      addMouseListener(new MouseAdapter() {
          public void mousePressed(MouseEvent me) {}
      });
	}
	
	/**
	 * Creates and collects all type radio buttons into a group, placing 
	 * them within their own JPanel. This allows only one button to be 
	 * selected at a time which is the behavior desired.
	 */
	protected void createTypeButtonGroup() {
		//Create Encapsulating JPanel
		typeButtonPanel = new JPanel();
		typeButtonPanel.setLayout(
				new BoxLayout(typeButtonPanel, BoxLayout.PAGE_AXIS));
		typeButtonPanel.setBackground(Color.white);
		
		//Add Label
		typeGroupLabel = new JLabel("Type:");
		typeButtonPanel.add(typeGroupLabel);
		
		//Create Radio Buttons
		btnHorizontal = new JRadioButton();
		btnVertical = new JRadioButton();
		
		//Create button grouping and add to the panel
		typeButtonGroup = new ButtonGroup();
		typeButtonGroup.add(btnHorizontal);
		typeButtonGroup.add(btnVertical);
		
		Enumeration<AbstractButton> typeButtons = 
				typeButtonGroup.getElements();

		//Format Radio Buttons
		AbstractButton radioButton;
		while(typeButtons.hasMoreElements()) {
			radioButton = typeButtons.nextElement();
			radioButton.setBackground(Color.white);
			radioButton.setAction(updateSelection);
			typeButtonPanel.add(radioButton);
		}
		
		//Setting Actiosn to buttons will wipe their text out so we
		//name them here AFTER the actions are set instead.
		btnHorizontal.setText("Horizontal");
		btnVertical.setText("Vertical");
		
		//Set default selection
		btnHorizontal.setSelected(true);
		
		this.add(typeButtonPanel);
	}
	
	/**
	 * Creates and collects all orientation radio buttons into a group, 
	 * placing them within their own JPanel. This allows only one button 
	 * to be selected at a time which is the behavior desired.
	 */
	protected void createRotationButtonGroup() {
		
		//Create Encapsulating JPanel
		rotationButtonPanel = new JPanel();
		rotationButtonPanel.setLayout(
				new BoxLayout(rotationButtonPanel, BoxLayout.PAGE_AXIS));
		rotationButtonPanel.setBackground(Color.white);
		
		//Add Label
		rotationGroupLabel = new JLabel("Rotation:");
		rotationButtonPanel.add(rotationGroupLabel);
		
		//Create Radio Buttons
		btnNotRotated = new JRadioButton();
		btnRotated = new JRadioButton();
		
		//Create button grouping and add to the panel
		rotationButtonGroup = new ButtonGroup();
		rotationButtonGroup.add(btnNotRotated);
		rotationButtonGroup.add(btnRotated);
		
		Enumeration<AbstractButton> rotationButtons = 
				rotationButtonGroup.getElements();
		
		//Format Radio Buttons
		AbstractButton radioButton;
		while(rotationButtons.hasMoreElements()) {
			radioButton = rotationButtons.nextElement();
			radioButton.setBackground(Color.white);
			radioButton.setAction(updateSelection);
			rotationButtonPanel.add(radioButton);
		}
		
		//Setting Actiosn to buttons will wipe their text out so we
		//name them here AFTER the actions are set instead.
		btnNotRotated.setText("None");
		btnRotated.setText("Rotated");
		
		//Set default selection
		btnNotRotated.setSelected(true);
		
		this.add(rotationButtonPanel);
	}		

	protected void createInversionButtonGroup() {
		//Create Encapsulating JPanel
		inversionButtonPanel = new JPanel();
		inversionButtonPanel.setLayout(
				new BoxLayout(inversionButtonPanel, BoxLayout.PAGE_AXIS));
		inversionButtonPanel.setBackground(Color.white);
		
		//Add Label
		inversionGroupLabel = new JLabel("Inversion:");
		inversionButtonPanel.add(inversionGroupLabel);
		
		//Create Radio Buttons
		btnNotInverted = new JRadioButton();
		btnInverted = new JRadioButton();
		
		//Create button grouping and add to the panel
		inversionButtonGroup = new ButtonGroup();
		inversionButtonGroup.add(btnNotInverted);
		inversionButtonGroup.add(btnInverted);
		
		Enumeration<AbstractButton> inversionButtons = 
				inversionButtonGroup.getElements();
		
		//Format Radio Buttons
		AbstractButton radioButton;
		while(inversionButtons.hasMoreElements()) {
			radioButton = inversionButtons.nextElement();
			radioButton.setBackground(Color.white);
			radioButton.setAction(updateSelection); 
			inversionButtonPanel.add(radioButton);
		}
		
		//Setting Actions to buttons will wipe their text out so we
		//name them here AFTER the actions are set instead.
		btnNotInverted.setText("None");
		btnInverted.setText("Flipped");
		
		//Set default selection
		btnNotInverted.setSelected(true);
		
		this.add(inversionButtonPanel);
	}
	
	/**
	 * Creates the required buttons for controlling swath placement mode.s
	 */
	protected void createSwathModeButtons() {
		modePanel = new JPanel();
		modePanel.setBackground(Color.white);
		
		swathModeButton = theme.makeButton(startSwathMode);
		
		modePanel.add(swathModeButton);
		this.add(modePanel);
	}

	/**
	 * Returns the type of swath currently selected
	 * @return - SwathType
	 */
	public SwathType getType() {
		return swathPreviewWidget.getSelectedType();
	}
	
	/**
	 * Returns the type of rotation for the currentlys elected swath
	 * @return - SwathRotation
	 */
	public SwathRotation getRotation() {
		return swathPreviewWidget.getSelectedRotation();
	}
	
	/**
	 * Returns whether or not the swath pattern is in inversion.
	 * @return - SwathInversion
	 */
	public SwathInversion getInversion() {
		return swathPreviewWidget.getSelectedInversion();
	}
	
	/**
	 * Action used to put the user interface into swath placement
	 * mode using the user selected parameters in this farming panel.
	 */
	private Action startSwathMode = new AbstractAction() {
		{
			String text = "Place Swath";
			putValue(Action.NAME, text);
		}
		
		public void actionPerformed(ActionEvent e) {
			//TODO - CP - Disable standard actions on map panel while in placement mode.
			MapPanel mapPanel = context.dash.mapPanel;
			
			//If we're in standard mode, start swath mode.
			if(mapPanel.roverPath.getOpMode() == RoverPath.OpMode.STANDARD) {

				mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				mapPanel.roverPath.setOpMode(RoverPath.OpMode.PLACE_SWATH);
				serialLog.warning("SWATH - Please pick a swath location.");
				return;
			}
			
			//If we we're in swath placement mode, cancel and return to standard
			if(mapPanel.roverPath.getOpMode() == RoverPath.OpMode.PLACE_SWATH) {
				mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				mapPanel.roverPath.setOpMode(RoverPath.OpMode.STANDARD);
				serialLog.warning("SWATH - Swath placement canceled.");
				return;
			}
		}
	};	
	
	/**
	 * Action used to update the swath pattern settings and preview in 
	 * response to changes in radio button selections.
	 */
	private Action updateSelection = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {

			//Check radio button states and set their enums appropriately
			//Type (Horizontal or Vertical)
			SwathType type = (btnHorizontal.isSelected() ? 
					SwathType.HORIZONTAL : 
					SwathType.VERTICAL);
			
			//Rotation (Rotated or No Rotation)
			SwathRotation rotation = (btnRotated.isSelected() ? 
					SwathRotation.ROTATED :
					SwathRotation.NONE);
			
			//Inversion (Flipped or No Inversion)
			SwathInversion inversion = (btnInverted.isSelected() ? 
					SwathInversion.FLIPPED :
					SwathInversion.NONE);
			
			//Update Drawn Preview
			swathPreviewWidget.updatePreview(type, rotation, inversion);
		}
	};
	

	
	//MANDATORY FEATURES

	//TODO - CP - Add cancel mode button and tie to escape key press also?


}
