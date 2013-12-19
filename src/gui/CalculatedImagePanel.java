package gui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dataFileTypes.CalculatedXrayFile;
import uiComponents.DataTypeChecker;

public class CalculatedImagePanel extends Observable {

	private final static int HORIZONTAL_STRUT_WIDTH = 5;
	private CalculatedImage calc;
	private String title;
	private JLabel lblReflection, lblVY, lblI;
	private JPanel pnlMain;
	private Color badEntryColor = Color.YELLOW;
	private Color defaultEntryColor;
	private Vector<CalculatedXrayFile> files;
	private CalculatedImageIntensityHistogram histo;
	
	public CalculatedImagePanel(Vector<CalculatedXrayFile> files, String title) {
		pnlMain = new JPanel();
		pnlMain.setBorder(BorderFactory.createTitledBorder(title));
		
		this.title = title;
		this.files = files;
		calc = new CalculatedImage(title);
		initVariables();
		setupPanel();
	}
	
	private void initVariables() {
		histo = new CalculatedImageIntensityHistogram();
	}
	public void initImage() {
		calc.setImage((BufferedImage) pnlMain.createImage(
				CalculatedImage.DEFAULT_IMAGE_WIDTH, CalculatedImage.DEFAULT_IMAGE_HEIGHT));
		calc.initImage();
	}
	private void setupPanel() {
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		
		JPanel pnlLabel = setupLabels();
		JPanel pnlInput = setupInput();
		
		pnlMain.add(pnlLabel);
		pnlMain.add(pnlInput);
		pnlMain.add(calc);
		pnlMain.add(histo);
		
	}
	private JPanel setupInput() {
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Imin"));
		box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
		
		JTextField txtImin = new JTextField(15);
		defaultEntryColor = txtImin.getBackground();
		
		txtImin.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
		FocusListener focus = new FocusListener() {

			double previousVal = -1, newVal;
			@Override
			public void focusLost(FocusEvent e) {
				try {
					newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					calc.setRawMinVal(newVal);
					((JTextField) e.getSource()).setBackground(defaultEntryColor);
					if(previousVal == -1 || previousVal != newVal) {
						setChanged();
						notifyObservers("Minimum intensity set to: " + newVal + "(Previous: " + previousVal);
						calc.update();
						previousVal = newVal;
					}
				}
				catch (NumberFormatException nfe) {
					((JTextField) e.getSource()).setBackground(badEntryColor);
				}
			}
			@Override
			public void focusGained(FocusEvent e) { 
				previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
				((JTextField) e.getSource()).selectAll(); 
			}
			
		};
		
		JPanel pnl = new JPanel();
		pnl.add(box);
		return pnl;
	}
	private JPanel setupLabels() {
		JPanel pnlLabels = new JPanel();
		pnlLabels.setLayout(new GridLayout(1, 2));
		
		Box box1 = Box.createHorizontalBox();
		lblReflection = new JLabel("0, 0, 0");
		
		box1.add(Box.createHorizontalGlue());
		box1.add(new JLabel("Q vector: "));
		box1.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
		box1.add(lblReflection);
		
		Box box2 = Box.createHorizontalBox();
		lblI = new JLabel("0");

		box2.add(Box.createHorizontalGlue());
		box2.add(new JLabel("Intensity: "));
		box2.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
		box2.add(lblI);
		
		
		pnlLabels.add(box1);
		pnlLabels.add(box2);
		
		return pnlLabels;
	}

	public JPanel getPnlMain() {
		return pnlMain;
	}

	public void setPnlMain(JPanel pnlMain) {
		this.pnlMain = pnlMain;
	}
}
