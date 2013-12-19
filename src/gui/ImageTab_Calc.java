package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dataFileTypes.CalculatedXrayFile;

public class ImageTab_Calc extends JScrollPane {

	private JPanel pnlMain;
	
	/* *********************** */
	/* IMAGE LAYOUT PARAMETERS */
	/* *********************** */
	private final static int IMAGE_TO_EDGE_HORIZONTAL = 5;
	private final static int IMAGE_TO_EDGE_VERTICAL = 5;
	private final static int IMAGE_TO_IMAGE_HORIZONTAL = 5;
	private final static int IMAGE_TO_IMAGE_VERTICAL = 5;
	private final static int DEFAULT_IMAGE_DIMENSIONS = 250;
	
	/* ********************* */
	/* IMAGE INPUT VARIABLES */
	/* ********************* */
	private BufferedImage bi100, bi110, bi111, bi_other;
	private double[][] data100, data110, data111, data_other;
	private File imagesFolder;
	private Vector<CalculatedXrayFile> f100, f110, f111, f_other;
	private Vector<CalculatedImagePanel> imagePanels;
	
	public final static String[] s100 = new String[] 
		{	
			"100", "-100", 
			"010", "0-10", 
			"001", "00-1"
		};
	public final static String[] s110 = new String[] 
		{	
			"110", "-110", "1-10", "-1-10", 
			"101", "-101", "10-1", "-10-1",
			"011", "0-11", "01-1", "0-1-1"
		};
	
	public final static String[] s111 = new String[]
		{	
			"111", "-111", "1-11", "11-1",
			"-1-1-1", "-1-11", "-11-1", "1-1-1"
		};
	
	/* ************** */
	/* CONSTRUCTOR(S) */
	/* ************** */
	
	public ImageTab_Calc() {
		super();
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		initializeLayout();

		getViewport().add(pnlMain);
		//add(pnlMain);
	}
	/* ***************** */
	/* GUI SETUP METHODS */
	/* ***************** */
	private void initializeLayout() {
		pnlMain = new JPanel();
		pnlMain.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		imagePanels = new Vector<CalculatedImagePanel>();
		
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		
		imagePanels.add(new CalculatedImagePanel(f100, "100"));
		imagePanels.add(new CalculatedImagePanel(f110, "110"));
		imagePanels.add(new CalculatedImagePanel(f111, "111"));
		imagePanels.add(new CalculatedImagePanel(f_other, "???"));
		
		pnlMain.add(imagePanels.get(0).getPnlMain());
		pnlMain.add(imagePanels.get(1).getPnlMain());
		pnlMain.add(imagePanels.get(2).getPnlMain());
		pnlMain.add(imagePanels.get(3).getPnlMain());
		
		JButton btn = new JButton("Init images");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				initImages();
			}
			
		});
		pnlMain.add(btn);
	}
	
	/* *************** */
	/* RUNTIME METHODS */
	/* *************** */
	private void initImages() {
		imagePanels.get(0).initImage();
		imagePanels.get(1).initImage();
		imagePanels.get(2).initImage();
		imagePanels.get(3).initImage();
	}
	
	public void loadFromDisk() {
		File[] files = imagesFolder.listFiles();
		if(files.length < 1) {
			System.out.println("No files in folder.");
			return;
		}
		f100 = new Vector<CalculatedXrayFile>();
		f110 = new Vector<CalculatedXrayFile>();
		f111 = new Vector<CalculatedXrayFile>();
		f_other = new Vector<CalculatedXrayFile>();
		
		String name;
		for(int i = 0; i < files.length; i++) {
			boolean added = false;
			name = files[i].getName();
			for(int j = 0; j < s100.length && !added; j++) {
				if(name.contains(s100[j])) {
					f100.add(new CalculatedXrayFile(files[i]));
					added = true;
					System.out.println("Added: " + name + " to [100] listing.");
				}
			}
			for(int j = 0; j < s110.length && !added; j++) {
				if(name.contains(s110[j])) {
					f110.add(new CalculatedXrayFile(files[i]));
					added = true;
					System.out.println("Added: " + name + " to [110] listing.");
				}
			}
			for(int j = 0; j < s111.length && !added; j++) {
				if(name.contains(s111[j])) {
					f111.add(new CalculatedXrayFile(files[i]));
					added = true;
					System.out.println("Added: " + name + " to [111] listing.");
				}
			}
			if(!added) {
				f_other.add(new CalculatedXrayFile(files[i]));
				System.out.println("Added: " + name + " to [other] listing.");
			}
		}
	}

	
	
	/* ******************* */
	/* GETTERS AND SETTERS */
	/* ******************* */
	
	public double[][] getData100() {
		return data100;
	}

	public void setData100(double[][] data100) {
		this.data100 = data100;
	}

	public double[][] getData110() {
		return data110;
	}

	public void setData110(double[][] data110) {
		this.data110 = data110;
	}

	public double[][] getData111() {
		return data111;
	}

	public void setData111(double[][] data111) {
		this.data111 = data111;
	}

	public double[][] getData_other() {
		return data_other;
	}

	public void setData_other(double[][] data_other) {
		this.data_other = data_other;
	}

	public File getImagesFolder() {
		return imagesFolder;
	}

	public void setImagesFolder(File imagesFolder) {
		this.imagesFolder = imagesFolder;
	}
}
