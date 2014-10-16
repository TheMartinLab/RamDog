/*******************************************************************************
 * Copyright (c) 2013 Eric Dill -- eddill@ncsu.edu. North Carolina State University. All rights reserved.
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Eric Dill -- eddill@ncsu.edu - initial API and implementation
 * 	James D. Martin -- jdmartin@ncsu.edu - Principal Investigator
 ******************************************************************************/
package gui;

import geometry.JVector;
import imageManipulation.BufferedImageTools;
import io.FileIO;
import io.MyPrintStream;
import io.ObjectIO;
import io.ReadATD;
import io.ReadFile;
import io.StringConverter;
import jama.Matrix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import lattice.BraggReflection;
import lattice.ReciprocalLattice;
import uiComponents.DataTypeChecker;
import uiComponents.MyJTextField;
import JavaToC.DirectFourierTransform;
import analysis.Pixel;
import analysis.PixelAnalysis;
import analysis.Spot;
import bravaisLattice.BravaisLattice;
import bravaisLattice.BravaisLatticeFactory;
import calculate.Calibration;
import chemistry.JAtom;
import color.ColorChooser;
import color.SigmoidalColorModel;
import dataFileTypes.BIN;
import dataFileTypes.CalculatedXrayCollection;
import dataFileTypes.CalculatedXrayFile;
import dataFileTypes.CalculatedXrayFileTools;
import dataFileTypes.ImageFile;
import dataFileTypes.ImageProperties;
import dataFileTypes.ImagePropertiesViewer;

public class ImageDisplay extends JFrame {
	private Color defaultButtonColor;
	private final static int WEST_WIDTH = 400, EAST_WIDTH = 400;
	enum ClickMode {zoom, region, spot, none, pixel, path};
	enum shapeOptions {rectangle, ellipse};
	enum imageFilterScaling {Linear, Logarithmic, AbsVal, ByMinusOne};
	enum imageFilterFiltering {Median, Min, Max, None};
	enum imageFilterRange {GreaterThan, LessThan, Between, EqualTo, NoLimit};
	enum maskFilterType {Click, WholeImage,};
	public enum XrayType { calc100, calc110, calc111, calc_other; }
	private PixelAnalysis pixelAnalysis;
	private static final long serialVersionUID = 6269710586037287004L;
	private ImagePanel imagePanel;
	private ImageTab_Calc imageTabCalc;
	private JTabbedPane imageTabs;
	private JPanel pnlMain;
	private int x1, y1;
	private int width, height;
	private JButton btnZoomReset;
	private JButton btnZoomClick;
	private JButton btnResetImage;
	private JTextField txtImageX, txtImageY, txtDisplayX, txtDisplayY;
	private CoordsPanel coordsPanel;
	private FilterPanel filterPanel;
	private JPanel pnlEast, pnlWest;
	private SelectionPanel selectionPanel;
	private ShapesPanel shapesPanel;
	private ImageOptionsFrame imageOptionsFrame;
	private OutputMenu outputMenu;
	private CalculationMenu calcMenu;
	private ContextPanel contextPanel;
	private ColorPanel colorPanel;
	private ImageViewPanel imgViewPanel;
	private FourierViewPanel ftViewPanel;
	private ImageFile curImageFile;
	private ImageProperties imgProp;
	private CalibrationPanel calib;
	private Vector<Calibration> calibFiles;
	private Calibration curCalib;
	private CalibrationFilesFrame calibFrame;
	private FocusListener txtFieldFocusListener;
	private JFileChooser chooserMultiFile;
	private CalculationFrame calcFrame;
	private ActivePixelDeterminationFrame activePixelFrame;
	private JFileChooser chooserFolder;
	private ColorChooser colorChooser;
	public final static int HORIZONTAL_STRUT_WIDTH = 10;
	public final static int VERTICAL_STRUT_HEIGHT = 10;
	private JCheckBoxMenuItem autoLevelImage;
	
	private boolean isOldFormatXrayFile = false;
	private Color badEntryColor = Color.YELLOW;
	private Color okEntryColor = new JTextField().getBackground();
	
	public ImageDisplay() {
		txtFieldFocusListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				JTextField txt = (JTextField) e.getSource();
				txt.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {}
		};
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		x1 = y1 = 100;
		width = height = 500;
		setSize(width, height);
		setLocation(x1, y1);
		setVisible(true);
		pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());
		imageTabs = new JTabbedPane();
		imageTabCalc = new ImageTab_Calc();
		imageTabs.add(pnlMain, "Experimental images");
		imageTabs.add(imageTabCalc, "Calculated images");
		add(imageTabs);
		setup();
		initFileChoosers();
		colorChooser = ColorChooser.createAndShowGUI(false);
		pnlMain.add(setupEastPanel(), BorderLayout.EAST);
		pnlMain.add(setupWestPanel(), BorderLayout.WEST);
		pnlMain.add(setupSouthPanel(), BorderLayout.SOUTH);
		setupMenuBar();
		imageOptionsFrame = new ImageOptionsFrame();
		activePixelFrame = new ActivePixelDeterminationFrame();
		pack();
		pixelAnalysis = new PixelAnalysis();
		setFocusable(true);
	}
	public void addToMainPanel(Component comp, Object constraints) {
		pnlMain.add(comp, constraints);
	}
	private void initFileChoosers()  {
		chooserMultiFile = new JFileChooser();
		chooserMultiFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooserMultiFile.setMultiSelectionEnabled(false);
		
		chooserMultiFile = new JFileChooser();
		chooserMultiFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooserMultiFile.setMultiSelectionEnabled(true);
		ImagePropertiesViewer imgPropViewer = new ImagePropertiesViewer(chooserMultiFile);
		chooserMultiFile.setAccessory(imgPropViewer);
		
		chooserFolder = new JFileChooser();
		chooserFolder.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooserFolder.setMultiSelectionEnabled(false);
	}
	private JPanel setupSouthPanel() {
		contextPanel = new ContextPanel();
		contextPanel.setFocusable(true);
		contextPanel.addKeyListener(new ImageKeyListener());
		return contextPanel;
	}
	private void setupMenuBar() {
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem loadXray = new JMenuItem("Load Xray Image");
		loadXray.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) { 
				coordsPanel.buttonClick(Coordinates.RECIPROCAL_SPACE);
				imgViewPanel.buttonClick(CurrentView.INPUT_IMAGE);
				imagePanel.loadImageFile();		
//				curImageFile = new BIN(imgProp);
//				imagePanel.readFile();
				
			}
		});
		JMenuItem loadTemp = new JMenuItem("Load Spots/Pixels");
		loadTemp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.loadTempData();
			}
			
		});
		
		JMenuItem loadCalc = new JMenuItem("Load calculated xray images");
		loadCalc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				coordsPanel.buttonClick(Coordinates.CALCULATED_SPACE);
				imgViewPanel.buttonClick(CurrentView.CALCULATED);
				String prevTitle = chooserFolder.getDialogTitle();
				chooserFolder.setDialogTitle("Load calculated xray images.");
				int returnVal = chooserFolder.showOpenDialog(null);
				switch(returnVal) {
				case JFileChooser.APPROVE_OPTION:
					File folder = chooserFolder.getCurrentDirectory();
					Vector<CalculatedXrayCollection> collections = CalculatedXrayFileTools.splitIntoCollections(folder);
					selectionPanel.addCalculatedXrayFile(collections.get(0), XrayType.calc100);
					selectionPanel.addCalculatedXrayFile(collections.get(1), XrayType.calc110);
					selectionPanel.addCalculatedXrayFile(collections.get(2), XrayType.calc111);
					selectionPanel.addCalculatedXrayFile(collections.get(3), XrayType.calc_other);
					break;
				}
				
				chooserFolder.setDialogTitle(prevTitle);
			}
		});
		

		JMenuItem loadImage = new JMenuItem("Load image (png, gif, jpg, etc.)");
		loadImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String prevTitle = chooserMultiFile.getDialogTitle();
				chooserMultiFile.setDialogTitle("Load image (png, gif, jpeg, etc.)");
				int returnVal = chooserMultiFile.showOpenDialog(null);
				switch(returnVal) {
				case JFileChooser.APPROVE_OPTION:
					File imageFile = chooserMultiFile.getSelectedFile();
					imagePanel.imageFile = imageFile;
					imagePanel.loadGeneralImage();
					break;
				}
				
				chooserMultiFile.setDialogTitle(prevTitle);
			}
		});
		menu.add(loadXray);
		menu.add(loadTemp);
		menu.add(loadCalc);
		menu.add(loadImage);
		bar.add(menu);
		
		JMenu image = new JMenu("Image");
		JMenuItem imageOptions = new JMenuItem("Image Options");
		imageOptions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imageOptionsFrame.setVisible(true);
			}
		});
		image.add(imageOptions);
		final JCheckBoxMenuItem oldFormat = new JCheckBoxMenuItem("Calculated Image is Old Format");
		oldFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				isOldFormatXrayFile = oldFormat.getState();
			}
		});
		final JCheckBoxMenuItem normalizeCalcImage = new JCheckBoxMenuItem("Normalize Calculated Image");
		normalizeCalcImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectionPanel.normalizeCalculatedImage = normalizeCalcImage.getState();
			}
		});
		JMenuItem spotPickImage = new JMenuItem("Spot pick this image.");
		spotPickImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				activePixelFrame.setVisible(true);
				/**
				String threshold = JOptionPane.showInputDialog(null,
						"Give me a value for the minimum curvature (ex: -50)",
						"Threshold Value",
						JOptionPane.INFORMATION_MESSAGE);
				Spot[] spots = imagePanel.spotPick(Double.valueOf(threshold));
				for(int i = 0; i < spots.length; i++) {
					selectionPanel.addSpot(spots[i].getPixels());
				}
				*/
			}
		});
		image.add(spotPickImage);
		JMenuItem showColorModel = new JMenuItem("Color Model");
		showColorModel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				colorChooser.showGUI();
			}
		});

		autoLevelImage = new JCheckBoxMenuItem("Auto-scale image?");
		
		showColorModel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				colorChooser.showGUI();
			}
		});
		JMenuItem spotPickSeries = new JMenuItem("Spot pick a series of images");
		spotPickSeries.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Spot pick series button clicked.");
			}
		});
		image.add(spotPickSeries);
		image.add(showColorModel);
		image.add(autoLevelImage);
		image.add(new JSeparator());
		image.add(oldFormat);
		image.add(normalizeCalcImage);
		JMenu imageSubMenu = new JMenu("Toggleable options");
		final JCheckBoxMenuItem fillSpace = new JCheckBoxMenuItem("Fill All Available Space");
		final JCheckBoxMenuItem maintainAspectRatio = new JCheckBoxMenuItem("Maintain Aspect Ratio");
		fillSpace.setSelected(true);
		fillSpace.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				imagePanel.fillSpace = fillSpace.isSelected();
				maintainAspectRatio.setEnabled(fillSpace.isSelected());
				imagePanel.getParent().repaint();
			}
			
		});
		maintainAspectRatio.setSelected(true);
		maintainAspectRatio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.maintainAspectRatio = maintainAspectRatio.isSelected();
				imagePanel.getParent().repaint();
			}
			
		});
		maintainAspectRatio.setEnabled(false);
		imageSubMenu.add(fillSpace);
		imageSubMenu.add(maintainAspectRatio);
		image.add(imageSubMenu);
		bar.add(image);
		outputMenu = new OutputMenu();
		bar.add(outputMenu);
		calcMenu = new CalculationMenu();
		bar.add(calcMenu);
		setJMenuBar(bar);
	}
	private JPanel setupWestPanel() {
		JPanel pnlWest = new JPanel();
		pnlWest.setLayout(new BoxLayout(pnlWest, BoxLayout.Y_AXIS));
		selectionPanel = new SelectionPanel();
		shapesPanel = new ShapesPanel();
		calib = new CalibrationPanel();

		pnlWest.add(calib);
		pnlWest.add(selectionPanel);
		pnlWest.add(shapesPanel);
		
		return pnlWest;
	}
	private JPanel setupEastPanel() {
		JPanel pnlEast = new JPanel();
		pnlEast.setLayout(new BoxLayout(pnlEast, BoxLayout.Y_AXIS));
		
		coordsPanel = new CoordsPanel();
		filterPanel = new FilterPanel();
		colorPanel = new ColorPanel();
		imgViewPanel = new ImageViewPanel();
		ftViewPanel = new FourierViewPanel();
		pnlEast.add(coordsPanel);
		pnlEast.add(filterPanel);
		pnlEast.add(colorPanel);
		pnlEast.add(imgViewPanel);
		pnlEast.add(ftViewPanel);
		
		return pnlEast;
	}
	private void setup() {
		imagePanel = new ImagePanel();
		
		pnlMain.add(imagePanel, BorderLayout.CENTER);
		
		imagePanel.init();
		JPanel northPanel = new JPanel();

		JButton btnCalc2ndDer = new JButton("Calc 2nd Ders");
		btnCalc2ndDer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.calc2ndDers();
			}
			
		});
		
		btnZoomClick = new JButton("Enable Two Click Zoom");
		btnZoomClick.addActionListener(new ZoomInListener());
		btnZoomReset = new JButton("Reset Zoom");
		btnZoomReset.addActionListener(new ResetZoomListener());
		btnResetImage = new JButton("Reload Image");
		btnResetImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.reloadImage();
			}
		});

		JButton btnPrevious = new JButton("Previous image");
		
		btnPrevious.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(imagePanel.curFileIdx > 0) {
					imagePanel.curFileIdx--;
					imagePanel.readFile();
				}
			}
		});
		JButton btnNext = new JButton("Next Image");
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(imagePanel.curFileIdx > 0) {
					imagePanel.curFileIdx++;
					imagePanel.readFile();
				}
			}
		});
		JButton btnFT = new JButton("Fourier Transform");
		btnFT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.fourierTransform();
			}
		});
		northPanel.add(btnZoomClick);
		northPanel.add(btnZoomReset);
		northPanel.add(btnResetImage);
		northPanel.add(btnCalc2ndDer);
		northPanel.add(btnPrevious);
		northPanel.add(btnNext);
		northPanel.add(btnFT);
		
		pnlMain.add(northPanel, BorderLayout.NORTH);
	}
	public void setDim(int x, int y) {
		width = x;
		height = y;
	}
	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }catch(Exception ex) {
	        ex.printStackTrace();
	    }
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                new ImageDisplay();
            }
        });
	}
	class RedrawListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//pnlImage.defaultImage();
			imagePanel.repaint();
		}
	}
	class SetImageSizeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			imagePanel.setImageDim(Integer.valueOf(txtImageX.getText()), Integer.valueOf(txtImageY.getText()));
			imagePanel.repaint();
		}
	}
	class SetDisplaySizeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			imagePanel.setDisplayDim(Integer.valueOf(txtDisplayX.getText()), Integer.valueOf(txtDisplayY.getText()));
			imagePanel.repaint();
		}
	}
	class ZoomInListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if(imagePanel.click != ClickMode.zoom) { 
				((JButton) e.getSource()).setText("Disable Two Click Zoom"); 
				imagePanel.setClickMode( ClickMode.zoom );
			} else {
				((JButton) e.getSource()).setText("Enable Two Click Zoom"); 
				imagePanel.setClickMode( ClickMode.none );
			}
		}
		
	}
	class ResetZoomListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			imagePanel.resetZoom();
		}
	}
	class CalculationMenu extends JMenu {
		private JMenuItem btnLoadAtomsFile;
		private JMenuItem btnComputeDiffraction;
		private JMenuItem btnPlotComputed;
		private BraggReflection[] calculated;
		
		public CalculationMenu() {
			super("Calculation");
			calcFrame = new CalculationFrame();
			
			setup();
		}
		private void setup() {
			btnLoadAtomsFile = new JMenuItem("Setup Bragg Reflection Calculation");
			btnLoadAtomsFile.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					calcFrame.setVisible(true);
				}
				
			});
			
			add(btnLoadAtomsFile);
		}
		
	}
	class OutputMenu extends JMenu {
		private JMenuItem btnSave;
		private JMenuItem btnOutputPixels;
		private JMenuItem btnGetHistories;
		private FileShowerFrame showFiles;
		private File[] files;
		private JFrame frameSave;
		public OutputMenu() {
			super("Output");
			initSaveMenu();
			setup();
			showFiles = new FileShowerFrame();
			showFiles.setVisible(false);
		}
		private void initSaveMenu() {
			frameSave = new JFrame();
			frameSave.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			final JToggleButton btnAll = new JToggleButton("Select Entire Image");
			final JToggleButton btnRegion = new JToggleButton("Select Specific Region");
			final JToggleButton btnFromCenter = new JToggleButton("Select Region From Image Center");
			ButtonGroup grp = new ButtonGroup();
			grp.add(btnAll);
			grp.add(btnRegion);
			grp.add(btnFromCenter);
			
			
			JButton btnSave = new JButton("Save Image");
			JButton btnSaveSeries = new JButton("Save Image Series");
			final JTextField txtMinX = new JTextField(5);
			final JTextField txtMaxX = new JTextField(5);
			final JTextField txtMinY = new JTextField(5);
			final JTextField txtMaxY = new JTextField(5);
			
			txtMinX.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER));
			txtMaxX.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER));
			txtMinY.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER));
			txtMaxY.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER));
			
			final JLabel lblMinX = new JLabel("Min x");
			final JLabel lblMaxX = new JLabel("Max x");
			final JLabel lblMinY = new JLabel("Min y");
			final JLabel lblMaxY = new JLabel("Max y");
			
			ActionListener al = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Object obj = arg0.getSource();
					if(obj instanceof JToggleButton) {
						JToggleButton tog = (JToggleButton) obj;
						if(tog == btnAll) {
							txtMinX.setEditable(false);
							txtMaxX.setEditable(false);
							txtMinY.setEditable(false);
							txtMaxY.setEditable(false);
							
							
						} else if(tog == btnRegion) {
							txtMinX.setEditable(true);
							txtMaxX.setEditable(true);
							txtMinY.setEditable(true);
							txtMaxY.setEditable(true);

							lblMinX.setText("Min x");
							lblMaxX.setText("Max x");
							lblMinY.setText("Min y");
							lblMaxY.setText("Max y");
							
						} else if(tog == btnFromCenter) {
							txtMinX.setEditable(true);
							txtMaxX.setEditable(true);
							txtMinY.setEditable(true);
							txtMaxY.setEditable(true);

							lblMinX.setText("Min x = x Center - ");
							lblMaxX.setText("Max x = x Center + ");
							lblMinY.setText("Min y = y Center - ");
							lblMaxY.setText("Max y = y Center + ");
						}
					}
				}
			};
			
			btnAll.addActionListener(al);
			btnRegion.addActionListener(al);
			btnFromCenter.addActionListener(al);

			btnAll.doClick();
			
			btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int minX = 0, maxX = 0, minY = 0, maxY = 0;
					if(btnAll.isSelected()) {
					
					} else if(btnRegion.isSelected()) {
						minX = Integer.valueOf(txtMinX.getText());
						maxX = Integer.valueOf(txtMaxX.getText());
						minY = Integer.valueOf(txtMinY.getText());
						maxY = Integer.valueOf(txtMaxY.getText());
						imagePanel.zoomTo(new Point(minX, minY), new Point(maxX, maxY));
						
					} else if(btnFromCenter.isSelected()) {
						if(coordsPanel.coords.compareTo(Coordinates.CALCULATED_SPACE) == 0 ||
								coordsPanel.coords.compareTo(Coordinates.CALCULATED_SPACE) == 0) {
							minX = ((int) Math.rint(coordsPanel.xMid - Integer.valueOf(txtMinX.getText())));
							maxX = ((int) Math.rint(coordsPanel.xMid + Integer.valueOf(txtMaxX.getText())));
							minY = ((int) Math.rint(coordsPanel.yMid - Integer.valueOf(txtMinY.getText())));
							maxY = ((int) Math.rint(coordsPanel.yMid + Integer.valueOf(txtMaxY.getText())));
							
						} else {
							minX = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.x))) - Integer.valueOf(txtMinX.getText());
							maxX = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.x))) + Integer.valueOf(txtMaxX.getText());
							minY = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.y))) - Integer.valueOf(txtMinY.getText());
							maxY = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.y))) + Integer.valueOf(txtMaxY.getText());
						}
						imagePanel.zoomTo(new Point(minX, minY), new Point(maxX, maxY));
					}
					save();
				}
				
			});
			btnSaveSeries.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int minX = 0, maxX = 0, minY = 0, maxY = 0;
					Point p1 = null;
					Point p2 = null;
					if(btnAll.isSelected()) {
						saveSeries(null, null);
					
					} else if(btnRegion.isSelected()) {
						minX = Integer.valueOf(txtMinX.getText());
						maxX = Integer.valueOf(txtMaxX.getText());
						minY = Integer.valueOf(txtMinY.getText());
						maxY = Integer.valueOf(txtMaxY.getText());
						p1 = new Point(minX, minY);
						p2 = new Point(maxX, maxY);
						saveSeries(p1, p2);
						
					} else if(btnFromCenter.isSelected()) {
						minX = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.x))) - Integer.valueOf(txtMinX.getText());
						maxX = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.x))) + Integer.valueOf(txtMaxX.getText());
						minY = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.y))) - Integer.valueOf(txtMinY.getText());
						maxY = ((int) Math.rint((Double) curCalib.getParam(Calibration.parameters.y))) + Integer.valueOf(txtMaxY.getText());
						p1 = new Point(minX, minY);
						p2 = new Point(maxX, maxY);
						saveSeries(p1, p2);
					}
				}
				
			});
			Box boxMain = Box.createVerticalBox();
			JPanel pnlButtons = new JPanel();
			pnlButtons.setBorder(BorderFactory.createTitledBorder("Image Region Selection"));
			pnlButtons.setLayout(new GridLayout(0, 3));
			pnlButtons.add(btnAll);
			pnlButtons.add(btnRegion);
			pnlButtons.add(btnFromCenter);
			
			boxMain.add(pnlButtons);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));

			Box box = Box.createHorizontalBox();
			box.add(lblMinX);
			box.add(txtMinX);
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblMaxX);
			box.add(txtMaxX);
			box.add(Box.createGlue());
			
			boxMain.add(box);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));

			box = Box.createHorizontalBox();
			box.add(lblMinY);
			box.add(txtMinY);
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblMaxY);
			box.add(txtMaxY);
			box.add(Box.createGlue());
			
			boxMain.add(box);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			
			box = Box.createHorizontalBox();
			box.add(Box.createGlue());
			box.add(btnSaveSeries);
			box.add(btnSave);
			
			boxMain.add(box);
			boxMain.add(Box.createGlue());
			
			frameSave.add(boxMain);
			frameSave.pack();
			
		}
		private void save() {
			chooserMultiFile.showSaveDialog(null);
			File output = chooserMultiFile.getSelectedFile();
			int len = output.toString().length();
			if(output.toString().substring(len-3, len).compareTo("png") != 0) {
				output = new File(output + ".png");
			}
			save(output);
		}
		private void saveSeries(Point p1, Point p2) {
			File[] toImages = null;
			String oldTitle1 = chooserMultiFile.getDialogTitle();
			chooserMultiFile.setDialogTitle("Select Series of Images to Save");
			switch(chooserMultiFile.showOpenDialog(null)) {
			case JFileChooser.APPROVE_OPTION:
				toImages = chooserMultiFile.getSelectedFiles();
				ImagePropertiesViewer imgPropViewer = (ImagePropertiesViewer) chooserMultiFile.getAccessory();
				ImageProperties imgProp = imgPropViewer.parseSelections();
				imagePanel.filesInFolder = toImages;
				/*
				switch(JOptionPane.showConfirmDialog(null, "Background Subtraction?"))  {
				case JOptionPane.YES_OPTION:
					getBackgroundFile();
					break;
				case JOptionPane.NO_OPTION:
					imagePanel.setSubtractingBackground(false);
					break;
				case JOptionPane.CANCEL_OPTION:
					imagePanel.setSubtractingBackground(false);
					return;
				}
				*/
				for(int i = 0; i < toImages.length; i++) {
					imagePanel.curFileIdx = i;
					curImageFile = new BIN(imgProp);
					imagePanel.readFile();
					coordsPanel.coords = Coordinates.RECIPROCAL_SPACE;
					imgViewPanel.view = CurrentView.INPUT_IMAGE;
					if(p1 == null || p2 == null)
						imagePanel.resetZoom();
					else
						imagePanel.zoomTo(p1, p2);
					String outputFileName = imagePanel.filesInFolder[imagePanel.curFileIdx].getAbsolutePath();
					outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf("."));
					outputFileName += "-auto.png";
					save(new File(outputFileName));
				}
			}
			chooserMultiFile.setDialogTitle(oldTitle1);
		}
		private void save(File f) {
			try {
				imagePanel.outputPNG(f);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Selected file not available for writing", 
						"File Output Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		private void setup() {
			JMenuItem mnuFTSeries = new JMenuItem("Fourier Transform Series");
			
			mnuFTSeries.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					File[] toFT = null;
					String oldTitle1 = chooserMultiFile.getDialogTitle();
					chooserMultiFile.setDialogTitle("Select Series of Images to Fourier Transform");
					switch(chooserMultiFile.showOpenDialog(null)) {
					case JFileChooser.APPROVE_OPTION:
						toFT = chooserMultiFile.getSelectedFiles();
						ImagePropertiesViewer imgPropViewer = (ImagePropertiesViewer) chooserMultiFile.getAccessory();
						ImageProperties imgProp = imgPropViewer.parseSelections();
						imagePanel.filesInFolder = toFT;
						/*
						switch(JOptionPane.showConfirmDialog(null, "Background Subtraction?"))  {
						case JOptionPane.YES_OPTION:
							getBackgroundFile();
							break;
						case JOptionPane.NO_OPTION:
							imagePanel.setSubtractingBackground(false);
							break;
						case JOptionPane.CANCEL_OPTION:
							imagePanel.setSubtractingBackground(false);
							return;
						}
						*/
						for(int i = 0; i < toFT.length; i++) {
							String outputFileName = imagePanel.filesInFolder[imagePanel.curFileIdx].getAbsolutePath();
							outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf("."));
							imagePanel.curFileIdx = i;
							curImageFile = new BIN(imgProp);
							imagePanel.readFile();
							imagePanel.resetZoom();
							coordsPanel.coords = Coordinates.RECIPROCAL_SPACE;
							imgViewPanel.view = CurrentView.INPUT_IMAGE;
							save(new File(outputFileName + "-auto.png"));
							outputFileName += "--FT-raw.png";
							imagePanel.fourierTransform();
							coordsPanel.coords = Coordinates.REAL_SPACE;
							imgViewPanel.view = CurrentView.FOURIER_TRANSFORM;
							imagePanel.resetZoom();
							save(new File(outputFileName));
						}
					}
					chooserMultiFile.setDialogTitle(oldTitle1);
				}
			});
			add(mnuFTSeries);
			JMenuItem mnuImgSeries = new JMenuItem("Save Series of Images");
			
			mnuImgSeries.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveSeries(null, null);
				}
			});
			btnSave = new JMenuItem("Save Image");
			btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frameSave.setVisible(true);
				}
				
			});
			add(btnSave);
			
			btnOutputPixels = new JMenuItem("Output Pixel Regions");
			btnOutputPixels.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					chooserMultiFile.setMultiSelectionEnabled(false);
					int returnVal = chooserMultiFile.showSaveDialog(null);
					switch(returnVal) {
					case JFileChooser.APPROVE_OPTION:
						File output = chooserMultiFile.getSelectedFile();
						int len = output.toString().length();
						String suffix = "";
						if(output.toString().substring(len-3, len).compareTo("txt") != 0) {
							suffix = ".txt";
						} else {
							output = new File(output.toString().subSequence(0, len-3).toString());
							suffix = ".txt";
						}
						
						// output regions
						Pixel[][] thePix = selectionPanel.getRegions();
						MyPrintStream mps = new MyPrintStream(new File(output + "_summary"));
						if(thePix != null) {
							Spot spot;
							mps.println("Regions");
							for(int i = 0; i < thePix.length; i++) {
								FileIO.writeToFileXYI(thePix[i], new File(output + "_region" + i + suffix));
								spot = new Spot(thePix[i]);
								spot.calculateSpotProperties(curCalib);
								mps.println(spot);
							}
						};
						// output Spots
						thePix = selectionPanel.getSpotsPerFrame();
						if(thePix != null) {
							Spot spot;
							mps.println("Regions");
							for(int i = 0; i < thePix.length; i++) {
								FileIO.writeToFileXYI(thePix[i], new File(output + "_spot" + i + suffix));
								spot = new Spot(thePix[i]);
								spot.calculateSpotProperties(curCalib);
								mps.println(spot);
							}
						}
						// output paths
						thePix = selectionPanel.getPaths();
						if(thePix != null) {
							for(int i = 0; i < thePix.length; i++) {
								FileIO.writeToFileXYI(thePix[i], new File(output + "_path" + i + suffix));
							}
						}
						// output target spots
						thePix = selectionPanel.getTargetSpots();
						if(thePix != null) {
							Spot spot;
							mps.println("Target Spots");
							for(int i = 0; i < thePix.length; i++) {
								FileIO.writeToFileXYI(thePix[i], new File(output + "_region" + i + suffix));
								spot = new Spot(thePix[i]);
								spot.calculateSpotProperties(curCalib);
								mps.println(spot);
							}
						}
						mps.close();
						break;
						default:
							return;
					}
				}
				
			});
			add(btnOutputPixels);
			
			btnGetHistories = new JMenuItem("Get Histories for All Regions");
			btnGetHistories.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					getFiles();
				}
			});
			add(btnGetHistories);
		}
		private Spot getIntensityWeightedAveraged(Pixel[] arr) {
			double ITot = 0;
			double x = 0;
			double y = 0;
			double q = 0;
			double phi = 0;
			double Icur = 0;
			for(Pixel pix : arr) {
				Icur = pix.getIntensity();
				ITot += Icur;
				x += pix.getX() * Icur;
				y += pix.getY() * Icur;
				q += pix.getDist() * Icur;
				phi += pix.getPhi() * Icur;
			}
			Spot spot = new Spot(x, y, ITot);
			spot.setQ(q);
			spot.setPhi(phi);
			
			return spot;
		}
		public void getFiles() {
			if(imagePanel.filesInFolder != null) {
				if(imagePanel.curFileIdx < 0) {
					imagePanel.curFileIdx = 0;
				} else if(imagePanel.curFileIdx >= imagePanel.filesInFolder.length){
					imagePanel.curFileIdx = imagePanel.filesInFolder.length-1;
				}
				chooserMultiFile.setCurrentDirectory(imagePanel.filesInFolder[imagePanel.curFileIdx]);
			}
			chooserMultiFile.setMultiSelectionEnabled(true);
			int returnVal = chooserMultiFile.showSaveDialog(null);
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				files = chooserMultiFile.getSelectedFiles();
				break;
			case JFileChooser.CANCEL_OPTION:
				return;
			}
			// if theFiles has a length of 1 check to see if it is a directory
			if(files.length == 1) {
				// if subFiles is not equal to null then theFiles is a directory
				if(files[0].listFiles() != null) {
					files = files[0].listFiles();
				}
			}
			// display and rename (if necessary) the files
			showFiles.displayFiles(files);
			showFiles.setVisible(true);
		}
		private void runHistory() {
			if(imagePanel.curFileIdx >= imagePanel.filesInFolder.length) {
				imagePanel.curFileIdx = 0;
			}
			chooserMultiFile.setCurrentDirectory(imagePanel.filesInFolder[imagePanel.curFileIdx]);
			int returnVal = chooserMultiFile.showSaveDialog(null);
			switch(returnVal) {
			case JFileChooser.CANCEL_OPTION:
				return;
			}
			File output = chooserMultiFile.getSelectedFile();
			System.out.println("Running History:");
			System.out.println("File order:");
			for(int i = 0; i < files.length; i++) {
				System.out.println(files[i]);
			}
			boolean runRegions = true;
			boolean runSpots = true;
			boolean runPaths = true;
			boolean integrate = true;
			activePixelFrame.isSelectingTargetSpots = false;
			if(integrate) {
				String input = "";
				double secondsPerFrame = 0;
				boolean validValue = false;
				while(!validValue) {
					input = (String) JOptionPane.showInputDialog(null, "How many seconds are there per XRD frame?",
							"Seconds per frame input dialog", JOptionPane.DEFAULT_OPTION,
							null, null, null);
					try {
						secondsPerFrame = Double.valueOf(input);
						validValue = true;
					} catch(NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Input value of: " + input + " is not a valid number.", 
								"Seconds per frame error message", JOptionPane.ERROR_MESSAGE);
						validValue = false;
					}
				}
				getIntegrated(output, runRegions, runSpots, runPaths, secondsPerFrame, activePixelFrame.getSpotPickAllImages());
			} else {
				getAll(output, runRegions, runSpots, runPaths);
			}
			activePixelFrame.isSelectingTargetSpots = true;
			
//			// get pixels to get from the read in data
//			Pixel[][] regions = selectionPanel.getRegions();
//			Pixel[][] spots = selectionPanel.getSpots();
//			Pixel[][] paths = selectionPanel.getPaths();
			
//			if(regions != null) { runRegions = true; }
//			if(spots != null) { runSpots = true; }
//			if(paths != null) { runPaths = true; }
//			// init the intensity arrays
//			double[][][] regionI, spotI, pathI;
//			regionI = spotI = pathI = null;
//			if(runRegions) { regionI = new double[regions.length][files.length][]; }
//			if(runSpots) { spotI = new double[spots.length][files.length][]; }
//			if(runPaths) { pathI = new double[paths.length][files.length][]; }
			
			// read in a file
			/*double[][] data;
			if(runSpots || runRegions || runPaths) {
				for(int i = 0; i < files.length; i++) {
					System.out.println("Cur file: " + files[i]);
					data = curImageFile.readFile(files[i]);
					// loop through the regions
					if(runRegions) { 
						for(int j = 0; j < regionI.length; j++) {
							regionI[j][i] = getIntensities(regions[j], data);
						}
					}
					// loop through the spots
					if(runSpots) {
						for(int j = 0; j < spotI.length; j++) {
//							spotI[j][i] = getIntensities(spots[j], data);
						}
					}
					// loop through the paths
					if(runPaths) {
						for(int j = 0; j < pathI.length; j++) {
							pathI[j][i] = getIntensities(paths[j], data);
						}
					}
				}
			}
			// print out to file
			if(runRegions) { 
				for(int i = 0; i < regions.length; i++) {
					FileIO.writeToFile(regions[i], regionI[i], new File(output + "_region" + i + ".txt"));
				}
			}
			if(runSpots) { 
//				for(int i = 0; i < spots.length; i++) {
//					FileIO.writeToFile(spots[i], spotI[i], new File(output + "_spot" + i + ".txt"));
//				}
			}
			if(runPaths) {
				for(int i = 0; i < paths.length; i++) {
					FileIO.writeToFile(paths[i], pathI[i], new File(output + "_path" + i + ".txt"));
				}
			}*/
			
		}
		private void getAll(File output, boolean runRegions, boolean runSpots, boolean runPaths) {
			runRegions = false;
			runSpots = false;
			runPaths = false;
			// get pixels to get from the read in data
			Pixel[][] regions = selectionPanel.getRegions();
			Pixel[][] spots = selectionPanel.getSpotsPerFrame();
			Pixel[][] paths = selectionPanel.getPaths();
			
			if(regions != null) { runRegions = true; }
			if(spots != null) { runSpots = true; }
			if(paths != null) { runPaths = true; }
			// init the intensity arrays
			double[][][] regionI, spotI, pathI;
			regionI = spotI = pathI = null;
			if(runRegions) { regionI = new double[regions.length][files.length][]; }
			if(runSpots) { spotI = new double[spots.length][files.length][]; }
			if(runPaths) { pathI = new double[paths.length][files.length][]; }
			
			// read in a file
			double[][] data;
			for(int i = 0; i < files.length; i++) {
				System.out.println("Cur file: " + files[i]);
				data = curImageFile.readFile(files[i]);
				// loop through the regions
				if(runRegions) { 
					for(int j = 0; j < regionI.length; j++) {
						regionI[j][i] = getIntensities(regions[j], data);
					}
				}
				// loop through the spots
				if(runSpots) {
					for(int j = 0; j < spotI.length; j++) {
						spotI[j][i] = getIntensities(spots[j], data);
					}
				}
				// loop through the paths
				if(runPaths) {
					for(int j = 0; j < pathI.length; j++) {
						pathI[j][i] = getIntensities(paths[j], data);
					}
				}
			}
			
			// print out to file
			if(runRegions) { 
				for(int i = 0; i < regions.length; i++) {
					FileIO.writeToFile(regions[i], regionI[i], new File(output + "_region" + i + ".txt"));
				}
			}
			if(runSpots) { 
				for(int i = 0; i < spots.length; i++) {
					FileIO.writeToFile(spots[i], spotI[i], new File(output + "_spot" + i + ".txt"));
				}
			}
			if(runPaths) {
				for(int i = 0; i < paths.length; i++) {
					FileIO.writeToFile(paths[i], pathI[i], new File(output + "_path" + i + ".txt"));
				}
			}
		}
		private void getIntegrated(File output, boolean runRegions, boolean runSpots, 
				boolean runPaths, double secondsPerFrame, boolean spotPick) {
			// get pixels to get from the read in data
			Pixel[][] regions = selectionPanel.getRegions();
			Pixel[][] spots = selectionPanel.getTargetSpots();
			Pixel[][] paths = selectionPanel.getPaths();
			
			if(regions != null) { runRegions = true; }
			if(spots != null) { runSpots = true; }
			if(paths != null) { runPaths = true; }
			// init the intensity arrays
			double[][] regionI = null, spotI = null, pathI = null;
			double[][] regionNorm = null, spotNorm = null, pathNorm = null;
			double[][] regionQAndPhiAndIAndXY = null, spotQAndPhiAndIAndXY = null, pathQAndPhiAndIAndXY = null;
			double[][] regionXandY = null, spotXandY = null, pathXandY = null;
			int[][] numPerQVal = null;
			
			File numSpotsPerFrame = new File(output + "_spotsPerFramePerReflection.txt");
			MyPrintStream mps_spotPick = null;
			
			File numSpotsPerFrame_Q = new File(output + "_spots -- Q values per frame.txt");
			MyPrintStream mps_numSpotsPerFrame_Q = null;
			runRegions = regions != null;
			runSpots = spots != null;
			runPaths = paths != null;
			
			if(runRegions) { 
				regionI = new double[regions.length][files.length]; 
				regionNorm = new double[regions.length][files.length];
				regionQAndPhiAndIAndXY = new double[regions.length][5];
				regionXandY = new double[regions.length][2];
			}
			if(runSpots) { 
				spotI = new double[spots.length][files.length]; 
				spotNorm = new double[spots.length][files.length];
				spotQAndPhiAndIAndXY = new double[spots.length][5];
				spotXandY = new double[spots.length][2];
			}
			if(runPaths) { 
				pathI = new double[paths.length][files.length]; 
				pathNorm = new double[paths.length][files.length];
				pathQAndPhiAndIAndXY = new double[paths.length][5];
				pathXandY = new double[paths.length][2];
			}
			if(spotPick) {
				
				numPerQVal = new int[files.length][calcFrame.calc.length+1];
				
				String val = "File name\tfileIndex\tAll spots\t";
				for(int i = 0; i < calcFrame.calc.length; i++) {
					val += calcFrame.calc[i].getHkl().toString() + "\t";
				}
				mps_spotPick = new MyPrintStream(numSpotsPerFrame);
				mps_numSpotsPerFrame_Q = new MyPrintStream(numSpotsPerFrame_Q);
				mps_spotPick.println(val);
			}
			
			// read in a file
			double[][] data;
			double[] intensities;
			imagePanel.filesInFolder = files;
			for(int i = 0; i < files.length; i++) {
				System.out.println("Cur file: " + files[i]);
				data = curImageFile.readFile(files[i]);
				imagePanel.loadImage(data);
				// TODO implement automatic clicking of "Spots/Regions/Paths" to permanently overlay the spots on the image
				// loop through the regions
				if(runRegions) { 
					for(int j = 0; j < regionI.length; j++) {
						intensities = getIntensities(regions[j], data);

						regionXandY[j][0] = intensities[intensities.length-4]; // avgx
						regionXandY[j][1] = intensities[intensities.length-3]; // avgy
						
						regionQAndPhiAndIAndXY[j][0] = intensities[intensities.length-2]; 
						regionQAndPhiAndIAndXY[j][1] = intensities[intensities.length-1];
						for(int k = 0; k < intensities.length; k++) {
							regionI[j][i] += intensities[k];
						}
						regionQAndPhiAndIAndXY[j][2] = regionI[j][i];
						regionQAndPhiAndIAndXY[j][3] = regionXandY[j][0];
						regionQAndPhiAndIAndXY[j][4] = regionXandY[j][1];
						//printPixelArray(regionXandY, j);
					}
				}
				// loop through the spots
				if(runSpots) {
					for(int j = 0; j < spotI.length; j++) {
						intensities = getIntensities(spots[j], data);
						
						spotXandY[j][0] = intensities[intensities.length-4]; // x
						spotXandY[j][1] = intensities[intensities.length-3]; // y
						
						spotQAndPhiAndIAndXY[j][0] = intensities[intensities.length-2]; // q
						spotQAndPhiAndIAndXY[j][1] = intensities[intensities.length-1]; // phi
						for(int k = 0; k < intensities.length-4; k++) {
							spotI[j][i] += intensities[k];
						}
						spotQAndPhiAndIAndXY[j][2] = spotI[j][i]; // I
						spotQAndPhiAndIAndXY[j][3] = spotXandY[j][0]; // y
						spotQAndPhiAndIAndXY[j][4] = spotXandY[j][1]; // x
					}
				}
				
				// loop through the paths
				if(runPaths) {
					for(int j = 0; j < pathI.length; j++) {
						intensities = getIntensities(paths[j], data);

						pathXandY[j][0] = intensities[intensities.length-4];
						pathXandY[j][1] = intensities[intensities.length-3];
						
						pathQAndPhiAndIAndXY[j][0] = intensities[intensities.length-2];
						pathQAndPhiAndIAndXY[j][1] = intensities[intensities.length-1];
						for(int k = 0; k < intensities.length; k++) {
							pathI[j][i] += intensities[k];
						}
						pathQAndPhiAndIAndXY[j][2] = pathI[j][i];
						pathQAndPhiAndIAndXY[j][3] = pathXandY[j][0];
						pathQAndPhiAndIAndXY[j][4] = pathXandY[j][1];
					}
				}
				if(spotPick) {
					imagePanel.curFileIdx = i;
					imagePanel.readFile();
					activePixelFrame.btnRunSpotpicking.doClick();
					numPerQVal[i] = getNumPerQ();
					int idx = outputMenu.showFiles.getIdx(imagePanel.filesInFolder[imagePanel.curFileIdx]);
					mps_spotPick.println(files[i].getName() + "\t" + idx + "\t" + StringConverter.arrayToTabString(numPerQVal[i]));
					mps_numSpotsPerFrame_Q.println(files[i].getName() + "\t" + getQ());
				}
				// TODO: call method to permanently overlay the pixels on the image
				colorPanel.firePermanentClick();
				selectionPanel.fireNodeClicks();
				imagePanel.outputSpotsOverlay();
			}
			
			// print out to file
			if(runRegions) { 
				FileIO.writeIntegratedToFile(regionI, new File(output + "_region.txt"));
				// normalize regions
				regionNorm = normalize(regionI, secondsPerFrame, regionQAndPhiAndIAndXY, regionXandY);
				FileIO.write2DArrToFile(regionNorm, new File(output + "_normalized_region.txt"));
			}
			if(runSpots) { 
				FileIO.writeIntegratedToFile(spotI, new File(output + "_spot.txt"));
				// normalize spots
				spotNorm = normalize(spotI, secondsPerFrame, spotQAndPhiAndIAndXY, spotXandY);
				FileIO.write2DArrToFile(spotNorm, new File(output + "_normalized_spot.txt"));
				FileIO.write2DArrToFile(spotQAndPhiAndIAndXY, new File(output + "_QandPhiandI.txt"));
			}
			if(runPaths) {
				FileIO.writeIntegratedToFile(pathI, new File(output + "_path.txt"));
				// normalize regions
				pathNorm = normalize(pathI, secondsPerFrame, pathQAndPhiAndIAndXY, pathXandY);
				FileIO.write2DArrToFile(pathNorm, new File(output + "_normalized_path.txt"));
			}
			if(spotPick) {
				mps_spotPick.close();
				System.out.println("Spots per time per Q written to file: " + mps_spotPick.getFile().getAbsolutePath());
			}
			if(mps_numSpotsPerFrame_Q != null) { mps_numSpotsPerFrame_Q.close(); }
			//SendMailTLS.send("eddill@ncsu.edu", "Spotpicking run finished.\n" + files[0].getName());
		}
		private int[] getNumPerQ() {
			Spot[] spots = activePixelFrame.spots;
			BraggReflection[] bragg = calcFrame.calc;
			int[] numPerQ = new int[bragg.length+1];
			int braggIdxMin = 0;
			double braggDistMin, braggDist;
			double spotQ;
			Spot spot;
			for(int i = 0; i < spots.length; i++) {
				spot = spots[i];
				braggDistMin = Double.MAX_VALUE;
				braggIdxMin = 0;
				spotQ = calib.coordsToQAndPhi(spot.getX(), spot.getY())[0];
				for(int j = 0; j < bragg.length; j++) {
					braggDist = Math.abs(spotQ - bragg[j].getQ().length());
					if(braggDistMin > braggDist) {
						braggDistMin = braggDist;
						braggIdxMin = j;
					}
				}
				numPerQ[0]++;
				numPerQ[braggIdxMin+1]++;
			}
			
			return numPerQ;
		}
		
		private String getQ() {
			Spot[] spots = activePixelFrame.spots;
			String Q = "";
			double spotQ;
			Spot spot;
			for(int i = 0; i < spots.length; i++) {
				spot = spots[i];
				spotQ = calib.coordsToQAndPhi(spot.getX(), spot.getY())[0];
				Q += spotQ + "\t";
			}
			
			return Q;
		}
		private void printPixelArray(double[][] regionXandY, int j) {
			
		}

		private double[][] normalize(double[][] I, double secondsPerFrame, double[][] qAndPhi, double[][] avgXandY) {
			double[][] iTrans = new Matrix(I).transpose().getArray();
			int numSpots = iTrans[0].length;
			int numFrames = iTrans.length;
			int extraRows = 7;
			int extraColumns = 1;
			double[][] normalized = new double[numFrames+extraRows][numSpots+extraColumns];
			double[][] bounds = new double[numSpots][2];
			
			// find the min and max vals
			for(int s = 0; s < numSpots; s++) {
				bounds[s][0] = Double.MAX_VALUE;
				bounds[s][1] = Double.MIN_VALUE;
				for(int f = 0; f < numFrames; f++) {
					if(bounds[s][0] > iTrans[f][s])
						bounds[s][0] = iTrans[f][s];
					if(bounds[s][1] < iTrans[f][s])
						bounds[s][1] = iTrans[f][s];					
				}
			}
			// label the column headers with the spot number, the max and min intensities, avg x and y, and q and phi
			for(int s = 0; s < numSpots; s++) {
				normalized[0][s+extraColumns] = s;
				normalized[1][s+extraColumns] = bounds[s][0];
				normalized[2][s+extraColumns] = bounds[s][1];
				normalized[3][s+extraColumns] = qAndPhi[s][0];
				normalized[4][s+extraColumns] = qAndPhi[s][1];
				normalized[5][s+extraColumns] = avgXandY[s][0];
				normalized[6][s+extraColumns] = avgXandY[s][1];
			}
			// label the row headers with the frame number
			for(int f = 0; f < numFrames; f++) {
				normalized[f+extraRows][0] = f*secondsPerFrame;
			}
			// normalize the transformations
			for(int f = 0; f < numFrames; f++) {
				for(int s = 0; s < numSpots; s++) {
					normalized[f+extraRows][s+extraColumns] = (iTrans[f][s] - bounds[s][0]) / (bounds[s][1] - bounds[s][0]);
				}
			}
			return normalized;
		}
		private double[][] normalize_old(double[][] I) {
			double[][] iTrans = new Matrix(I).transpose().getArray();
			int numSpots = iTrans.length;
			int numFrames = iTrans[0].length;
			double[][] normalized = new double[numFrames+2][numSpots+1];
			double[][] bounds = new double[numSpots][2];
			
			// find the min and max vals
			for(int i = 0; i < numSpots; i++) {
				bounds[i][0] = Double.MAX_VALUE;
				bounds[i][1] = Double.MIN_VALUE;
				for(int j = 0; j < I[i].length; j++) {
					if(bounds[i][0] > I[i][j])
						bounds[i][0] = I[i][j];
					if(bounds[i][1] < I[i][j])
						bounds[i][1] = I[i][j];
				}
			}
			
			// label the column headers with the spot number and the max intensity
			for(int i = 0; i < numSpots; i++) {
				normalized[i][0] = i;
				normalized[i][1] = bounds[i][1];
			}
			for(int j = 0; j < numFrames; j++) {
				normalized[0][j] = j;
			}
			// label the row headers with the 
			// normalize the transformations
			for(int i = 1; i < numSpots; i++) {
				normalized[i][0] = i;
				for(int j = 0; j < I[i].length; j++) {
					if(j == 0) {
						normalized[i][j+1] = bounds[i][1];
					} else {
						normalized[i][j+1] = (I[i][j] - bounds[i][0]) / (bounds[i][1] - bounds[i][0]);
					}
				}
			}
			return normalized;
		}
		/**
		 * 
		 * @param curPix
		 * @param data
		 * @return I[num_pixels] [I for each pixel in spot] + {avgX, avgY, q, phi}
		 */
		private double[] getIntensities(Pixel[] curPix, double[][] data) {
			double[] I = new double[curPix.length+4];
			double avgX = 0;
			double avgY = 0;
			double totalI = 0;
			int x, y, i;
			for(i = 0; i < curPix.length; i++) {
				x = curPix[i].getX();
				y = curPix[i].getY();
				I[i] = data[x][y];
				totalI += I[i];
				avgX += x*I[i];
				avgY += y*I[i];
			}
			if(totalI > 0) {
				avgX /= totalI;
				avgY /= totalI;
				double[] qphi = calib.coordsToQAndPhi(avgX, avgY); 
				I[i] = avgX;
				I[i+1] = avgY;
				I[i+2] =  qphi[0]; // q
				I[i+3] = qphi[1]; // phi
			}
			return I;
		}
		private double[] getSummedIntensityQAndPhi(Pixel[] curPix, double[][] data) {
			int x, y, i;
			double avgX=0, avgY=0;
			double I=0;
			for(i = 0; i < curPix.length; i++) {
				x = curPix[i].getX();
				y = curPix[i].getY();
				I += data[x][y];
				avgX += x*data[x][y];
				avgY += y*data[x][y];
			}
			avgX /= I;
			avgY /= I;
			double[] qphi = calib.coordsToQAndPhi(avgX, avgY); 
			double[] returnVal = new double[3];
			returnVal[0] = I;
			returnVal[1] =  qphi[0];
			returnVal[2] = qphi[1];
			return returnVal;
		}
		class FileShowerFrame extends JFrame {
			private JFrame renamer;
			private JPanel pnlButtons;
			private File[] files;
			private JTextArea txt;
			private String prefix, suffix;
			public FileShowerFrame() {
				super();
				setDefaultCloseOperation(HIDE_ON_CLOSE);
				setTitle("File Display and Renamer");
				setup();
				setSize(750, 750);
				setupRenamer();
			}
			private void setupRenamer() {
				prefix = "This should be everything in the file name before the file index";
				suffix= "This should be everything in the file name after the file index";
				renamer = new JFrame();
				renamer.setDefaultCloseOperation(HIDE_ON_CLOSE);
				renamer.setTitle("Renamer");
				JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				final JLabel lblPrefix = new JLabel(prefix);
				final JLabel lblSuffix = new JLabel(suffix);
				
				JPanel temp = new JPanel();
				temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
				temp.add(new JLabel("Prefix: "));
				temp.add(lblPrefix);
				mainPanel.add(temp);
				temp = new JPanel();
				temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
				temp.add(new JLabel("Suffix: "));
				temp.add(lblSuffix);
				mainPanel.add(temp);
				
				// prefix
				JPanel pnlPrefix = new JPanel();
				pnlPrefix.setLayout(new BoxLayout(pnlPrefix, BoxLayout.X_AXIS));
				final JTextField txtPrefix = new JTextField(30);
				txtPrefix.setText(prefix);
				txtPrefix.addFocusListener(txtFieldFocusListener);
				JButton btnPrefix = new JButton("Set Prefix");
				btnPrefix.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						prefix = txtPrefix.getText();
						lblPrefix.setText(prefix);
					}
					
				});
				pnlPrefix.add(btnPrefix);
				pnlPrefix.add(txtPrefix);
				// suffix
				JPanel pnlSuffix = new JPanel();
				pnlPrefix.setLayout(new BoxLayout(pnlPrefix, BoxLayout.X_AXIS));
				final JTextField txtSuffix = new JTextField(30);
				txtSuffix.setText(suffix);
				txtSuffix.addFocusListener(txtFieldFocusListener);
				JButton btnSuffix = new JButton("Set Suffix");
				btnSuffix.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						suffix = txtSuffix.getText();
						lblSuffix.setText(suffix);
					}
					
				});
				pnlSuffix.add(btnSuffix);
				pnlSuffix.add(txtSuffix);
				
				mainPanel.add(pnlPrefix);
				mainPanel.add(pnlSuffix);
				renamer.add(mainPanel, BorderLayout.CENTER);
				
				JButton btnRename = new JButton("Rename");
				btnRename.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						renamer.setVisible(false);
						rename();
						displayFiles(files);
					}
				});
				renamer.add(btnRename, BorderLayout.SOUTH);
				renamer.pack();
				renamer.setLocationRelativeTo(null);
			}
			private void rename() {
				int idxStart, idxEnd, maxIdx = 0;
				int[] newIdx = new int[files.length]; 
				for(int i = 0; i < files.length; i++) {
					idxStart = prefix.length();
					idxEnd = idxStart + files[i].toString().length() - suffix.length()-prefix.length();
					newIdx[i] = Integer.valueOf(files[i].toString().substring(idxStart, idxEnd));
					if(maxIdx < newIdx[i]) { maxIdx = newIdx[i]; }
					System.out.print(newIdx[i] + "\t");
					System.out.println(idxStart + "\t" + idxEnd);
				}
				String newFileName = "";
				boolean renameSuccessful = false;
				File newFile;
				for(int i = 0; i < files.length; i++) {
					newFileName = prefix + getNewIdx(newIdx[i], maxIdx) + suffix;
					newFile = new File(newFileName);
					renameSuccessful = files[i].renameTo(newFile);
					files[i] = newFile;
					System.out.println("File rename " + i + " renamed successfully: " + renameSuccessful );
				}
				System.out.println("Before sort but with new file names: ");
				for(int i = 0; i < files.length; i++) {
					System.out.println(files[i].toString());
				}
				Arrays.sort(files);
				System.out.println("After sort with new file names: ");
				for(int i = 0; i < files.length; i++) {
					System.out.println(files[i].toString());
				}
				System.out.println(maxIdx);
				System.out.println("Your choices are: ");
				System.out.println("prefix: " + prefix);
				System.out.println("suffix: " + suffix);
			}
			
			private String getNewIdx(int curIdx, int maxIdx) {
				String newIdx = String.valueOf(curIdx);
				String strMaxIdx = String.valueOf(maxIdx);
				int totalLen = newIdx.length();
				while(totalLen < strMaxIdx.length()) {
					newIdx = "0" + newIdx;
					totalLen++;
				}
				return newIdx;
			}
			public int getIdx(File file) {
				String fName = file.getAbsolutePath();
				int idx = 0;
				fName = fName.substring(prefix.length(), fName.length());
				int charIdx = 0;
				while(fName.charAt(charIdx) != (suffix.charAt(0))) {
					charIdx++;
				}
				fName = fName.substring(0, charIdx);
				idx = Integer.valueOf(fName);
				
				return idx;
			}
			private void setup() {
				txt = new JTextArea();
				txt.setEditable(false);
				JScrollPane scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scroll.setViewportView(txt);
				JPanel pnlMain = new JPanel();
				pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
				pnlButtons = new JPanel();
				JPanel pnlReopen = new JPanel();
				pnlReopen.setLayout(new BoxLayout(pnlReopen, BoxLayout.X_AXIS));
				JButton btnOpen = new JButton("Select Files");
				btnOpen.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int returnVal = chooserMultiFile.showOpenDialog(null);
						switch(returnVal) {
						case JFileChooser.APPROVE_OPTION:
							files = chooserMultiFile.getSelectedFiles();
							break;
						case JFileChooser.CANCEL_OPTION:
							return;
						}
						displayFiles(files);
					}
				});
				pnlReopen.add(new JLabel("Click this to select different files: "));
				pnlReopen.add(btnOpen);
				pnlButtons.add(pnlReopen);
				JPanel pnlHistory = new JPanel();
				pnlHistory.setLayout(new BoxLayout(pnlHistory, BoxLayout.X_AXIS));
				JButton btnGetHistory = new JButton("Get Histories");
				btnGetHistory.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						outputMenu.files = files;
						outputMenu.runHistory();
					}
					
				});
				pnlHistory.add(new JLabel("Click this if you're satisfied with the order of files: "));
				pnlHistory.add(btnGetHistory);
				
				pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));
				pnlButtons.add(pnlHistory);
				
				JButton btnRename = new JButton("Rename");
				btnRename.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						renamer.setVisible(true);
					}
				});
				JPanel pnlRename = new JPanel();
				pnlRename.setLayout(new BoxLayout(pnlRename, BoxLayout.X_AXIS));
				pnlRename.add(new JLabel("Click this to rename your files: "));
				pnlRename.add(btnRename);
				
				pnlButtons.add(pnlRename);
				pnlMain.add(scroll);
				pnlMain.add(pnlButtons);
				add(pnlMain);
			}
			public void displayFiles(File[] files) {
				this.files = files;
				Arrays.sort(files);
				String s = "";
				for(int i = 0; i < files.length; i++) {
					s += files[i].toString() + "\n";
				}
				txt.setText(s);
			}
		}
	}
	class OutputListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			outputMenu.setVisible(true);
		}
		
	}
	enum Coordinates {
		REAL_SPACE,
		RECIPROCAL_SPACE,
		CALCULATED_SPACE,
		GENERAL_IMAGE,
		;
	}
	class CoordsPanel extends JPanel {
		private JLabel lblx, lbly, lblI, lbl2ndDerx, lbl2ndDery, lblQ, lblPhi, lblH, lblK, lblL, lblMin, lblMax;
		private double xMid, yMid, qPerPix, xNumPixInOriginalImage, yNumPixInOriginalImage, a;
		private JTextField txtXMid, txtYMid, txtXPix, txtYPix, txtQPerPix, txtA;
		private Coordinates coords = Coordinates.RECIPROCAL_SPACE;
		private Vector<JToggleButton> togCoords;
		public CoordsPanel() {
			super();
			setBorder(new TitledBorder("Coordinate Info"));
			setup();
			setMaximumSize(new Dimension(EAST_WIDTH, getPreferredSize().height));
		}
		private void setup() {
			Box box = Box.createHorizontalBox();
			Box boxMain = Box.createVerticalBox();

			lblx = new JLabel();
			lbly = new JLabel();
			lblI = new JLabel();
			lbl2ndDerx = new JLabel();
			lbl2ndDery = new JLabel();
			lblQ = new JLabel();
			lblPhi = new JLabel();
			lblH = new JLabel();
			lblK = new JLabel();
			lblL = new JLabel();
			lblMin = new JLabel("0");
			lblMax = new JLabel("0");
			
			box.add(new JLabel("x: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblx);
			box.add(Box.createHorizontalStrut(2*HORIZONTAL_STRUT_WIDTH));
			box.add(new JLabel("     y: "));
			box.add(lbly);
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(Box.createGlue());
			
			boxMain.add(box);
			
			box = Box.createHorizontalBox();
			box.add(new JLabel("I: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblI);
			box.add(Box.createGlue());
			
			boxMain.add(box);

			box = Box.createHorizontalBox();
			box.add(new JLabel("2nd der x: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lbl2ndDerx);
			
			box.add(Box.createHorizontalStrut(3*HORIZONTAL_STRUT_WIDTH));

			box.add(new JLabel("2nd der y: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lbl2ndDery);
			box.add(Box.createGlue());
			
			boxMain.add(box);

			box = Box.createHorizontalBox();
			box.add(new JLabel("Q: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblQ);
			box.add(Box.createGlue());
			
			boxMain.add(box);
			
			box = Box.createHorizontalBox();
			box.add(new JLabel("Phi: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblPhi);
			box.add(Box.createGlue());
			
			boxMain.add(box);
			
			box = Box.createHorizontalBox();
			box.add(new JLabel("Min value displayed: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblMin);
			box.add(Box.createGlue());

			boxMain.add(box);
			
			box = Box.createHorizontalBox();
			box.add(new JLabel("Max value displayed: "));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblMax);
			box.add(Box.createGlue());
			
			boxMain.add(box);

			box = Box.createHorizontalBox();
			box.add(new JLabel("h k l"));
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblH);
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblK);
			box.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			box.add(lblL);
			box.add(Box.createGlue());
			
			boxMain.add(box);
			
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			boxMain.add(setupToggleSwitch());
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			boxMain.add(setupInputBox());
			add(boxMain);
		}
		private Box setupToggleSwitch() {
			Box box = Box.createHorizontalBox();
			ButtonGroup bg = new ButtonGroup();
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Object obj = arg0.getSource();
					if(obj instanceof JToggleButton) {
						JToggleButton tog = (JToggleButton) obj;
						switch(Coordinates.valueOf(tog.getText())) {
						case CALCULATED_SPACE:
							coords = Coordinates.CALCULATED_SPACE;
							break;
						case REAL_SPACE:
							coords = Coordinates.REAL_SPACE;
							break;
						case RECIPROCAL_SPACE:
							coords = Coordinates.RECIPROCAL_SPACE;
							break;
						case GENERAL_IMAGE:
							coords = Coordinates.GENERAL_IMAGE;
						default:
							break;
						}
					}
				}
			};
			JPanel pnl = new JPanel();
			pnl.setLayout(new GridLayout(2, 0));
			JToggleButton tog;
			togCoords = new Vector<JToggleButton>();
			for(Coordinates coord : Coordinates.values()) {
				tog = new JToggleButton(coord.name());
				tog.addActionListener(al);
				bg.add(tog);
				pnl.add(tog);
				togCoords.add(tog);

				buttonClick(coords);
			}
			box.add(pnl);
			box.add(Box.createGlue());
			return box;
		}
		private Box setupInputBox() {
			Box box = Box.createVerticalBox();
			// init text fields
			txtXMid = new JTextField(10);
			txtYMid = new JTextField(10);
			txtXPix = new JTextField(10);
			txtYPix = new JTextField(10);
			txtQPerPix = new JTextField(10);
			txtA = new JTextField(10);

			txtXMid.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			txtYMid.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			txtXPix.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			txtYPix.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			txtQPerPix.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			txtA.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			
			a = 8.82;
			qPerPix = 0.048;
			txtXMid.setText("" + 127);
			txtYMid.setText("" + 127);
			txtQPerPix.setText("" + qPerPix);
			txtA.setText("" + a);
			txtXMid.addFocusListener(new FocusListener() {
				double previousVal = -1, newVal;
				@Override
				public void focusGained(FocusEvent e) {
					previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					((JTextField) e.getSource()).selectAll(); 
				}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
						((JTextField) e.getSource()).setBackground(okEntryColor);
						xMid = newVal;
						if(previousVal == -1 || previousVal != newVal) {
							previousVal = newVal;
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				
			});
			
			txtYMid.addFocusListener(new FocusListener() {
				double previousVal = -1, newVal;
				@Override
				public void focusGained(FocusEvent e) {
					previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					((JTextField) e.getSource()).selectAll(); 
				}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
						((JTextField) e.getSource()).setBackground(okEntryColor);
						yMid = newVal;
						if(previousVal == -1 || previousVal != newVal) {
							previousVal = newVal;
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				
			});
			txtXPix.addFocusListener(new FocusListener() {
				double previousVal = -1, newVal;
				@Override
				public void focusGained(FocusEvent e) {
					previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					((JTextField) e.getSource()).selectAll(); 
				}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
						((JTextField) e.getSource()).setBackground(okEntryColor);
						xNumPixInOriginalImage = newVal;
						if(previousVal == -1 || previousVal != newVal) {
							previousVal = newVal;
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				
			});
			txtYPix.addFocusListener(new FocusListener() {
				double previousVal = -1, newVal;
				@Override
				public void focusGained(FocusEvent e) {
					previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					((JTextField) e.getSource()).selectAll(); 
				}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
						((JTextField) e.getSource()).setBackground(okEntryColor);
						yNumPixInOriginalImage = newVal;
						if(previousVal == -1 || previousVal != newVal) {
							previousVal = newVal;
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				
			});
			txtQPerPix.addFocusListener(new FocusListener() {
				double previousVal = -1, newVal;
				@Override
				public void focusGained(FocusEvent e) {
					previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					((JTextField) e.getSource()).selectAll(); 
				}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
						((JTextField) e.getSource()).setBackground(okEntryColor);
						qPerPix = newVal;
						if(qPerPix != 0 & coordsPanel.coords == Coordinates.CALCULATED_SPACE) {
							imagePanel.xrayFile.setqStep(qPerPix);
							imagePanel.xrayFile.setqMaxX((xNumPixInOriginalImage - xMid) * qPerPix);
							imagePanel.xrayFile.setqMaxY((yNumPixInOriginalImage - yMid) * qPerPix);
						}
						if(previousVal == -1 || previousVal != newVal) {
							previousVal = newVal;
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				
			});

			txtA.addFocusListener(new FocusListener() {
				double previousVal = -1, newVal;
				@Override
				public void focusGained(FocusEvent e) {
					previousVal = Double.parseDouble(((JTextField) e.getSource()).getText());
					((JTextField) e.getSource()).selectAll(); 
				}
				@Override
				public void focusLost(FocusEvent e) {
					try {
						newVal = Double.parseDouble(((JTextField) e.getSource()).getText());
						((JTextField) e.getSource()).setBackground(okEntryColor);
						a = newVal;
						if(previousVal == -1 || previousVal != newVal) {
							previousVal = newVal;
						}
					}
					catch (NumberFormatException nfe) {
						((JTextField) e.getSource()).setBackground(badEntryColor);
					}
				}
				
			});
			
			Box boxh1 = Box.createHorizontalBox();
			boxh1.add(new JLabel("x mid"));
			boxh1.add(txtXMid);
			boxh1.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			boxh1.add(new JLabel("y mid"));
			boxh1.add(txtYMid);
			boxh1.add(Box.createHorizontalGlue());
			
			Box boxh2 = Box.createHorizontalBox();
			boxh2.add(new JLabel("x pix"));
			boxh2.add(txtXPix);
			boxh2.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			boxh2.add(new JLabel("y pix"));
			boxh2.add(txtYPix);
			boxh2.add(Box.createHorizontalGlue());
			
			Box boxh3 = Box.createHorizontalBox();
			boxh3.add(new JLabel("q per pix"));
			boxh3.add(txtQPerPix);
			boxh3.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			boxh3.add(new JLabel("a"));
			boxh3.add(txtA);
			boxh3.add(Box.createHorizontalGlue());
			
			box.add(boxh1);
			box.add(boxh2);
			box.add(boxh3);
			return box;
		}
		public void setImageParams(double xMid, double yMid, double nPixX, double nPixY, double qPerPix) {
			txtXMid.setText("" + format(xMid));
			txtYMid.setText("" + format(yMid));
			txtXPix.setText("" + format(nPixX));
			txtYPix.setText("" + format(nPixY));
			if(qPerPix != 0)
				txtQPerPix.setText("" + format(qPerPix));
			
			this.xMid = xMid;
			this.yMid = yMid;
			xNumPixInOriginalImage = nPixX;
			yNumPixInOriginalImage = nPixY;
			this.qPerPix = qPerPix;
		}
		public void updateInfo(Point p, double I) {
			JVector hkl = new JVector();
			if(curCalib != null)
				hkl = toHKL(p);
			updateInfo(p, I, hkl);
		}
		public double pixToQ(Point p) {
			double q = 0;
			double x0 = Double.valueOf(curCalib.getParam(Calibration.parameters.x) + "");
			double y0 = Double.valueOf(curCalib.getParam(Calibration.parameters.y) + "");
			double sampleToDetector = 1000*Double.valueOf(curCalib.getParam(Calibration.parameters.distance) + "");
			double wavelength = Double.valueOf(curCalib.getParam(Calibration.parameters.wavelength) + "");
			JVector center = new JVector(x0, y0, 0);
			JVector cursorLoc = new JVector(p.x, p.y, 0);
			double dist = JVector.distance(center, cursorLoc);
			dist *=  Double.valueOf(curCalib.getParam(Calibration.parameters.pixel_size) + "");
			double twoTheta =  Math.atan(dist / sampleToDetector);
			q = 4 * Math.PI * Math.sin(twoTheta/2) / wavelength;
			return q; 
		}
		public double pixToQ_Calc(Point p) {
			JVector Q;
			try {
				Q = imagePanel.getQ(p);
				return Q.length();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return 0;
		}
		private double getPhi(Point p) {
			double xc = 0;
			double yc = 0;
			switch(coords) {
			case GENERAL_IMAGE:
			case CALCULATED_SPACE:
			case REAL_SPACE:
				xc = coordsPanel.xMid;
				yc = coordsPanel.yMid;
				break;
			case RECIPROCAL_SPACE:
				if(calib.hasCalibration) {
					xc = calib.x0;
					yc = calib.y0;
				}
				break;
			default:
				break;
			}
			JVector pos = new JVector(p.x, p.y, 0);
			JVector center = new JVector(xc, yc, 0);
			
			pos = JVector.subtract(center, pos);
			center.i += 100;
			double degrees = JVector.angleDegrees(pos, new JVector(100, 0, 0));
			if(pos.j > 0)
				degrees = 360 - degrees;
			return degrees;
		}
		public void updateInfo(Point p, double I, JVector Q) {
			lblx.setText("" + p.x);
			lbly.setText("" + p.y);
			if(I > 10000)
				lblI.setText("" + formatExp(I));
			else
				lblI.setText("" + format(I));
			lblPhi.setText("" + getPhi(p));
			switch(coords) {
			case CALCULATED_SPACE:
				lblQ.setText("" + Q.length());
				JVector hkl = imagePanel.xrayFile.qToHKL(Q, coordsPanel.getA());
				lblH.setText(format(hkl.getI()) + "");
				lblK.setText(format(hkl.getJ()) + "");
				lblL.setText(format(hkl.getK()) + "");
				break;
			case REAL_SPACE:
				lblQ.setText("" + format(pixToDist(p)));
				break;
			case RECIPROCAL_SPACE:
				double[] d = imagePanel.get2ndDers(p);
				double[] qphi = new double[2];
				if(calib.hasCalibration) 
					qphi = calib.coordsToQAndPhi(p.x, p.y);
				else {
					qphi[0] = 0;
					qphi[1] = 0;
				}
				lblPhi.setText("" + format(qphi[1]));
				lblQ.setText("" + format(qphi[0]));
				if(d[0] > 1000)
					lbl2ndDerx.setText("" + formatExp(d[0]));
				else
					lbl2ndDerx.setText("" + format(d[0]));
				
				if(d[1] > 1000)
					lbl2ndDery.setText("" + formatExp(d[1]));
				else
					lbl2ndDery.setText("" + format(d[1]));
					
				lblH.setText(format(Q.getI()) + "");
				lblK.setText(format(Q.getJ()) + "");
				lblL.setText(format(Q.getK()) + "");
				break;
			case GENERAL_IMAGE:
				
			default:
				break;
			
			}
		}
		public double pixToDist(Point p) {
			double x = xMid - p.x;
			double y = yMid - p.y;
			
			double xScale = Math.PI / (qPerPix * xNumPixInOriginalImage/2.);
			double yScale = Math.PI / (qPerPix * yNumPixInOriginalImage/2.);
			
			double dist = Math.sqrt(Math.pow(x * xScale, 2) + Math.pow(y * yScale, 2));
			return dist;
		}
		public void buttonClick(Coordinates coord) {
			for(JToggleButton tog : togCoords)
				if(tog.getText().compareTo(coord.name()) == 0) {
					tog.doClick();
					return;
				}
		}
		public void setMinMax(double min, double max) {
			if(min > 100000)
				lblMin.setText(formatExp(min) + "");
			else
				lblMin.setText(format(min) + "");
			if(max > 100000)
				lblMax.setText(formatExp(max) + "");
			else
				lblMax.setText(format(max) + "");
		}
		private JVector toHKL(Point p) {
			return calcFrame.toHKL(p);
		}
		public double getA() {
			return a;
		}
		public void setA(double a) {
			this.a = a;
		}
	}
	class ImageClickListener extends MouseAdapter {
		private int numClicks = 0;
		private Point p1;
		private GeneralPath path;
		@Override
		public void mouseClicked(MouseEvent arg0) {
			click(arg0.getPoint());
		}
		public void click(Point p) {
			System.out.println(p);
			switch(imagePanel.type) {
			case Click:
				imagePanel.clickFilter(imagePanel.viewCoordsToImageCoords(p));
				return;
			case WholeImage:
				break;
			default:
				break;
			}
			if(numClicks == 0) {
				p1 = (Point) p.clone();
				int ellipseWidth = 14;
				int ellipseHeight = 14;
				imagePanel.setShape(new Ellipse2D.Float((float) p1.x-ellipseWidth/2, (float) p1.y-ellipseHeight/2, ellipseWidth, ellipseHeight));
				imagePanel.getParent().repaint();
				numClicks++;
				switch(imagePanel.click) {
				case spot:
					numClicks = 0;
					System.out.println("in view coords: " + p1);
					p1 = imagePanel.viewCoordsToImageCoords(p1);
					System.out.println("in image coords: " + p1);
					Pixel[] pix = pixelAnalysis.findPixels(imagePanel.X2ndDer, imagePanel.Y2ndDer, imagePanel.imageData, p1);
					selectionPanel.addTargetSpot(pix);
					p1 = null;
					break;
				case pixel:
					p1 = imagePanel.viewCoordsToImageCoords(p1);
					selectionPanel.addPixel(p1, imagePanel.imageData[p1.x][p1.y]);
					numClicks = 0;
					p1 = null;
					break;
				case none:
					numClicks = 0;
					p1 = null;
					break;
				case path:
					path = new GeneralPath();
					path.moveTo(p1.x, p1.y);
					break;
				case region:
					break;
				case zoom:
					break;
				default:
					break;
				}
				
			}
			else if(numClicks > 0) {
				switch(imagePanel.click) {
				case zoom:
					imagePanel.zoomTwoClick(p1, p);
					p1 = null;
					numClicks = 0;
					break;
				case region:
					region(p);
					p1 = null;
					numClicks = 0;
					break;
				case path:
					path.lineTo(p.x, p.y);
					imagePanel.setShape(path);
					imagePanel.getParent().repaint();
					break;
				case none:
					break;
				case pixel:
					numClicks = 0;
					break;
				case spot:
					break;
				default:
					break;
				}
			}
		}
		public void submitPath() {
			imagePanel.setShape(path);
			numClicks = 0;
			p1 = null;
			imagePanel.getParent().repaint();
		}
		public void clearPath() {
			imagePanel.shape = null;
			numClicks = 0;
			p1 = null;
			imagePanel.getParent().repaint();
		}
		private void region(Point p2) {
			int x1 = Math.min(p1.x, p2.x);
			int x2 = Math.max(p1.x, p2.x);
			int y1 = Math.min(p1.y, p2.y);
			int y2 = Math.max(p1.y, p2.y);
			p1.x = x1;
			p1.y = y1;
			p2.x = x2;
			p2.y = y2;
			
			numClicks = 0;
			Shape s;
			if(shapesPanel.selected == null) { return; }
			System.out.println(shapesPanel.selected);
			switch(shapesPanel.selected) {
			case ellipse:
				s = new Ellipse2D.Float(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);
				break;
			case rectangle:
				s = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
				break;
			default:
				s = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
			}
			imagePanel.setShape(s);
			p1 = null;
			imagePanel.getParent().repaint();
		}
	}
	class ImagePanel extends JPanel implements KeyListener {
		private BufferedImage image;
		private int imgWidth, imgHeight;
		private int viewWidth, viewHeight;
		private int zoomX1, zoomY1, zoomX2, zoomY2;
		private double val1, val2;
		private int maskSize;
		private double imageData[][], inputData[][], fourierTransformData[][][];
		/**
		 * [x][y][0] = 2ndDerX; [x][y][1] = 2ndDerY
		 */
		private double [][] X2ndDer, Y2ndDer;
		private imageFilterScaling filterScaling = imageFilterScaling.Logarithmic;
		private imageFilterRange filterRange = imageFilterRange.NoLimit;
		private imageFilterFiltering filter = imageFilterFiltering.None;
		private boolean fillSpace = true;
		private boolean maintainAspectRatio = true;
		private boolean haveCalculated2ndDers = false;
		private Shape shape;
		private Shape[] pixelHighlight;
		private ClickMode click;
		private ColorFilter theColor;
		private int dimension;
		private File imageFile;
		private int curFileIdx;
		private File[] filesInFolder;
		private File backgroundFile;
		private ImageFile backgroundFileType;
		private boolean isSubtractingBackground = false;
		private ImageClickListener imageClickListener;
		private Color c1 = new Color(118, 185, 0, 180);
		private boolean colorPermanent = false;
		private boolean isCalculatedImage = false;
		private CalculatedXrayFile xrayFile;
		private maskFilterType type;
		private double imageMin, imageMax;
		private double rawDataMin, rawDataMax;
		private boolean recalc2ndDers = true;
		public ImagePanel() {
			super();
			imageData = new double[imgWidth][imgHeight];
			new2ndDers(imgWidth, imgHeight);
			imgWidth = width;
			imgHeight = height;
			viewWidth = width;
			viewHeight = height;
			zoomX1 = 0;
			zoomY1 = 0;
			zoomX2 = width;
			zoomY2 = height;
			imageClickListener = new ImageClickListener();
			addMouseListener(imageClickListener);
			filterScaling = imageFilterScaling.Logarithmic;
			click = ClickMode.none;
			theColor = ColorFilter.inverseGrayscale;
			setFocusable(true);
			addKeyListener(this);
		}
		public void setPermanentPaint(boolean permanent) {
			colorPermanent = permanent;
		}
		public void setShape(Shape s) {
			shape = s;
		}
		public void setVal1(double val) { val1 = val; }
		public void setVal2(double val) { val2 = val; }
		public void setMaskSize(int val) { maskSize = val; }
		public void setClickMode(ClickMode theMode) {
			click = theMode;
			shapesPanel.shapesPanel.setVisible(false);
			shapesPanel.pathPanel.setVisible(false);
			switch(click) {
			case region:
				shapesPanel.shapesPanel.setVisible(true);
				break;
			case path:
				shapesPanel.pathPanel.setVisible(true);
				break;
			case none:
				break;
			case pixel:
				break;
			case spot:
				break;
			case zoom:
				break;
			default:
				break;
			}
		}
		public void init() {
			image = (BufferedImage) imagePanel.createImage(imgWidth, imgHeight);
			defaultImage();
		}
		public void outputPNG(File output) throws IOException {
			BufferedImage img = image.getSubimage(zoomX1, zoomY1, zoomX2-zoomX1, zoomY2-zoomY1);
			int type = img.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img.getType();
			int width = zoomX2-zoomX1;
			int height = zoomY2-zoomY1;
			height = width > height ? width : height;
			width = height > width ? height : width;
			img = BufferedImageTools.resizeImage(img, type, width, height);
			ImageIO.write(img, "png", output);
		}
		public void setImageDim(int x, int y) {
			imgWidth = x;
			imgHeight = y;
			zoomX2 = imgWidth;
			zoomY2 = imgHeight;
			getParent().repaint();
		}
		public void setDisplayDim(int x, int y) {
			viewWidth = x;
			viewHeight = y;
			getParent().repaint();
		}
		public double[] get2ndDers(Point p) {
			if(!haveCalculated2ndDers()) 
				calc2ndDers();
			try {
				double x = X2ndDer[p.x][p.y];
				double y = Y2ndDer[p.x][p.y];
				x = Double.valueOf(format(x));
				y = Double.valueOf(format(y));
				return new double[] {x, y};
			} catch(ArrayIndexOutOfBoundsException e) {
				return new double[] {0, 0};
			}
		}
		public Dimension getDimensions() {
			int viewWidth = Integer.valueOf(imageOptionsFrame.txtViewX.getText());
			int viewHeight = Integer.valueOf(imageOptionsFrame.txtViewY.getText());
			if(fillSpace) {
				if(maintainAspectRatio) {
					double xyRatio = ((double) viewWidth) / ((double) viewHeight);
					viewHeight = getHeight();
					viewWidth = getWidth();
					if(viewHeight < viewWidth) {
						viewWidth = (int) (Math.rint((viewHeight) / xyRatio));
					} else {
						viewHeight = (int) (Math.rint((viewWidth) * xyRatio));
					}
				} else {
					viewWidth = getWidth();
					viewHeight = getHeight();
				}
			} 
//			if(coordsPanel.coords == Coordinates.REAL_SPACE) {
//				viewHeight = getHeight();
//				viewWidth = getWidth();
//				viewHeight = viewWidth * zoomY2 / zoomX2;
//			}
			return new Dimension(viewWidth, viewHeight);
		}
		@Override
		public void paint(Graphics g) {
			Dimension d = getDimensions();
			viewWidth = d.width;
			viewHeight = d.height;
			System.out.println("image width, height = " + viewWidth + ", " + viewHeight);
			Graphics2D g2 = (Graphics2D) g;
			//g2.drawImage(image, 0, 0, width, height, null);
			/*AffineTransform rotateImage = AffineTransform.getRotateInstance(Math.PI/2., imgWidth/2., imgHeight/2);
			rotateImage = new AffineTransform();
			rotateImage.translate(0, viewHeight);
			rotateImage.rotate(-Math.PI/2.);
			BufferedImage bi = image.getSubimage(zoomX1, zoomY1, zoomX2-zoomX1, zoomY2-zoomY1);
			//g2.drawImage(bi.getScaledInstance(viewWidth, viewHeight, 0), rotateImage, null);*/
			g2.drawImage(image, 0, 0, viewWidth, viewHeight, zoomX1, zoomY1, zoomX2, zoomY2, null);
			if(shape != null) { g2.draw(shape); }
			if(pixelHighlight != null) {
				Rectangle approximateArea = pixelHighlight[0].getBounds();
				g2.setPaint(c1);
				for(int i = 0; i < pixelHighlight.length; i++) {
					approximateArea.add(pixelHighlight[i].getBounds());
					//g2.fill(rotateImage.createTransformedShape(pixelHighlight[i]));
					g2.fill(pixelHighlight[i]);
				}
				//Shape s = rotateImage.createTransformedShape(approximateArea);
				/*Shape s = approximateArea;
				g2.setPaint(Color.black);
				g2.draw(s);
				g2.setPaint(new Color(255, 255, 0, 16));
				g2.fill(s);*/
			}
		}
		public JVector pointToQ_oldFormat(Point p) {
			JVector vx = new JVector(1, -1, 0);
			JVector vy = new JVector(-1, -1, 2);
//			vy = new JVector(1, 1, 1);
			double x = (p.x - coordsPanel.xMid) * coordsPanel.qPerPix;//*vx.length();
			double y = (p.y - coordsPanel.yMid) * coordsPanel.qPerPix;//*vy.length();
			JVector Q = JVector.add(JVector.multiply(vx, x), JVector.multiply(vy, y));
//			Q.multiply(2*Math.PI / a);
			return Q;
//			return JVector.add(JVector.multiply(vx.unit(), x), JVector.multiply(vy.unit(), y));
		}
		private JVector getQ(Point p) throws Exception {
			if(isCalculatedImage())
				if(isOldFormatXrayFile)
					return pointToQ_oldFormat(p);
				else
					return xrayFile.pointToQ(p);
			throw new Exception("Not using a calculated xray image.");
		}
		private void defaultImage() {
			image = (BufferedImage) imagePanel.createImage(imgWidth, imgHeight);
			WritableRaster raster = image.getRaster();
			ColorModel model = image.getColorModel();
			Color color;
			Object colorData;
			Random r = new Random();
			int x0 = (int) (r.nextInt(imgWidth/2) + imgWidth/0.25);
			int y0 = (int) (r.nextInt(imgHeight/2) + imgWidth/0.25);
			double x = 0.05;
			double y = x;
			for(int i = 0; i < imgWidth; i++) {
				for(int j = 0; j < imgHeight; j++) {
					color = new Color((int) Math.abs(r.nextInt(255)*Math.sin(x*j)*Math.cos(y*i))%255, 
							(int) Math.abs(r.nextInt(255)*Math.sin(x*j)*Math.cos(y*i))%255, 
							(int) Math.abs(r.nextInt(255)*Math.sin(x*j)*Math.cos(y*i))%255);
					colorData = model.getDataElements(color.getRGB(), null);
					raster.setDataElements(i, j, colorData);
				}
			}
		}
		public void setFilterScale(imageFilterScaling scaling) {
			pixelHighlight = null;
			filterScaling = scaling;
		}
		public void setFilterRange(imageFilterRange range) {
			pixelHighlight = null;
			filterRange = range;
		}
		public void setFiltering(imageFilterFiltering filter) {
			pixelHighlight = null;
			this.filter = filter;
		}
		public void setClickOrWholeImage(maskFilterType type) {
			pixelHighlight = null;
			this.type = type;
		}
		public void setColor(ColorFilter color) {
			theColor = color;
			getParent().repaint();
		}
		public void resetZoom() {
			zoomX1 = 0;
			zoomY1 = 0;
			switch(coordsPanel.coords) {
			case GENERAL_IMAGE:
			case CALCULATED_SPACE:
				zoomX2 = imgWidth;
				zoomY2 = imgHeight;
				break;
			case REAL_SPACE:
				zoomX2 = (int) Math.rint(coordsPanel.xNumPixInOriginalImage);
				zoomY2 = (int) Math.rint(coordsPanel.yNumPixInOriginalImage);
				break;
			case RECIPROCAL_SPACE:
				zoomX2 = imgWidth;
				zoomY2 = imgHeight;
				break;
			default:
				break;
			
			}
			//for(MouseMotionListener mml : getMouseMotionListeners()) {
			//	removeMouseMotionListener(mml);
			//}
			pixelHighlight = null;
			getParent().repaint();
			//addMouseMotionListener(new ImageMouseMotionListener());
		}
		public void paintPixels(Pixel[] toPaint, Color color) {
			int imgPixWidth = (int) Math.rint(((double) viewWidth) / ((double) (zoomX2 - zoomX1)));
			int imgPixHeight = (int) Math.rint(((double) viewHeight) / ((double) (zoomY2 - zoomY1)));
			Point curPoint = new Point();
			Graphics g = null;
			if(colorPermanent) { g = image.getGraphics(); }
			Rectangle[] toDraw = new Rectangle[toPaint.length];
			for(int i = 0; i < toPaint.length; i++) {
//				System.out.println(i);
				curPoint.x = toPaint[i].getX();
				curPoint.y = toPaint[i].getY();
				if(colorPermanent) {
					if(color == null) {
						color = c1;
					}
					g.setColor(color);
					g.drawLine(curPoint.x, curPoint.y, curPoint.x, curPoint.y);
				}
				if(color != null) {
					c1 = color;
				}
				curPoint = imageCoordsToViewCoords(curPoint);
				toDraw[i] = new Rectangle(curPoint.x, curPoint.y, imgPixWidth, imgPixHeight);
			}
			pixelHighlight = toDraw;
			getParent().repaint();
		}
		public void zoomTwoClick(Point p1, Point p2) {
			p1 = viewCoordsToImageCoords(p1);
			p2 = viewCoordsToImageCoords(p2);
			zoomTo(p1, p2);
		}
		public void zoomTo(Point p1, Point p2) {
			zoomX1 = p1.x;
			zoomX2 = p2.x;
			
			zoomY1 = p1.y;
			zoomY2 = p2.y;
			
			System.out.println(zoomX1 + "\t" + zoomY1 + "\t\t" + zoomX2 + "\t" + zoomY2);

			pixelHighlight = null;
			getParent().repaint();
		}
		public void updateData(double[][] data) {
			loadImage(data);
		}
		public void clickFilter(Point p) {
			double[][] data = imageData;
			switch(imgViewPanel.getView()) {
			case BACKGROUND:
				data = calib.getBackgroundData();
				break;
			case CALCULATED:
				data = getCalculatedData();
				break;
			case FOURIER_TRANSFORM:
				data = fourierTransformData[0];
				break;
			case INPUT_IMAGE:
				data = inputData;
				break;
			case GENERAL_IMAGE:
				data = imageData;
				break;
			default:
				break;
			
			}
			data[p.x][p.y] = applyFilteringFilter(data, p.x, p.y);
			double I = data[p.x][p.y];
			I = applyScalingFilter(I);
//			I = Math.rint();
			imageData[p.x][p.y] = ((I - imageMin) / (imageMax - imageMin)) * 255;
			I = Math.rint(data[p.x][p.y]);
			I = (int) I;
			if(I < 0) { I = 0; }
			if(I > 255) { I = 255; }
			I = 255-I;
			Color color = getColor(I);
			WritableRaster raster = image.getRaster();
			ColorModel model = image.getColorModel();
			Object colorData = model.getDataElements(color.getRGB(), null);
			raster.setDataElements(p.x, p.y, colorData);
			getParent().repaint();
		}
		private double applyScalingFilter(double val) {
			switch(filterScaling) {
			case Logarithmic:
				if(val > 0)
					val = Math.log(val);
				else
					val = 0;
				break;
			case Linear:
				break;
			case AbsVal:
				val = Math.abs(val);
				break;
			case ByMinusOne:
				val = -val;
				break;
			}
			return val;
		}
		private double getMin(double curMin) {
			switch(filterRange) {
			case GreaterThan:
				return val1;
			case LessThan:
				return curMin;
			case Between:
				return val1;
			case EqualTo:
				return val1;
			case NoLimit:
				return curMin;
			}
			return curMin;
		}
		private double getMax(double curMax) {
			switch(filterRange) {
			case GreaterThan:
				return curMax;
			case LessThan:
				return val1;
			case Between:
				return val2;
			case EqualTo:
				return val1;
			case NoLimit:
				return curMax;
			}
			return curMax;
		}
		private double applyFilteringFilter(double[][] d, int x, int y) {
			double val = d[x][y];
			switch(filter) {
			case None:
				val = d[x][y];
				break;
			case Min:
				val = minFilter(d, x, y, maskSize);
				break;
			case Median:
				val = medianFilter(d, x, y, maskSize);
				break;
			case Max:
				val = maxFilter(d, x, y, maskSize);
			}
			return val;
		}
		private double[] getSortedMask(double[][] d, int x, int y, int maskSize) {
			int numVals = (int) Math.rint(Math.pow(maskSize * 2 + 1, 2));
			double[] arr = new double[numVals];
			int a, b;
			int idx = 0;
			for(int i = x-maskSize; i <= x+maskSize; i++) {
				for(int j = y-maskSize; j <= y+maskSize; j++) {
					a = i;
					if(i < 0) { a = 0; }
					if(i >= d.length) { a = d.length-1; }
					b = j;
					if(j < 0) { b = 0; }
					if(j >= d[a].length) { b = d[a].length-1; }
					arr[idx++] = d[a][b];
				}
			}
			Arrays.sort(arr);
			return arr;
		}
		private double minFilter(double[][] d, int x, int y, int maskSize) {
			double[] sortedVals = getSortedMask(d, x, y, maskSize);
			return sortedVals[0];
		}
		private double maxFilter(double[][] d, int x, int y, int maskSize) {
			double[] sortedVals = getSortedMask(d, x, y, maskSize);
			return sortedVals[sortedVals.length-1];
		}
		private double medianFilter(double[][] d, int x, int y, int maskSize) {
			// x +/- maskSize & y +/- maskSize
			double[] sortedVals = getSortedMask(d, x, y, maskSize);
			int size = sortedVals.length;
			double median = 0;
			if(size%2 == 0) {
				int idx = size/2;
				median = (sortedVals[idx] + sortedVals[idx+1] )/ 2.;
			} else {
				int idx = size/2;
				median = sortedVals[idx];
			}
			if(median > 0) {
				int i = 1;
			}
			return median;
		}
		private Color getColor(double I) {
			switch(theColor) {
			case grayscale:
				I = Math.rint(I);
				return new Color((int) I, (int) I, (int) I);
			case inverseGrayscale:
				I = 255-Math.rint(I);
				return new Color((int) I, (int) I, (int) I);
			default:
				I = Math.rint(I);
				return new Color((int) I, (int) I, (int) I);					
			}

		}
		private void loadImage(int[][] data) {
			double[][] d_data = new double[data.length][data[0].length];
			rawDataMax = Double.MIN_VALUE;
			rawDataMin = Double.MAX_VALUE;
			for(int i = 0; i < data.length; i++) {
				for(int j = 0; j < data[0].length; j++) {
					d_data[i][j] = data[i][j];
					rawDataMax = rawDataMax < d_data[i][j] ? d_data[i][j] : rawDataMax;
					rawDataMin = rawDataMin < d_data[i][j] ? d_data[i][j] : rawDataMin;
				}
			}
			loadImage(d_data);
		}
		private void loadGeneralImage() {
			setHaveCalculated2ndDers(false);
			image = null;
			try {
				this.image = ImageIO.read(imageFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Raster raster = image.getData();
			System.out.println("Num raster bands: " + raster.getNumBands());
			int width = raster.getWidth();
			int height = raster.getHeight();
			zoomX2 = width;
			zoomY2 = height;
			int x0 = raster.getMinX();
			int y0 = raster.getMinY();
			coordsPanel.setImageParams(width/2, height/2, width, height, 0.05);
			DataBuffer buf = raster.getDataBuffer();
			inputData = new double[width-x0][height-y0];
			double val = 0, valtot = 0, maxVal = 0;
			for(int x = x0; x < width; x++) {
				for(int y = y0; y < height; y++) {
					val1 = 0;
					valtot = 0;
					for(int i = 0; i < raster.getNumBands(); i++) {
						val1 = raster.getSampleDouble(x, y, i) + 255*i;
						valtot += val1;
					}
					inputData[x][y] = valtot;
				}
			}
			if(getMouseMotionListeners().length != 1) {
				addMouseMotionListener(new ImageMouseMotionListener());
			}
			if(getMouseWheelListeners().length != 1) {
				addMouseWheelListener(new ImageMouseWheelListener());
			}
			setFocusable(true);
			getParent().repaint();
			loadImage(inputData);
			
		}
		private double[][] subtractBackground(double[][] data) {
			double[][] backgroundData = calib.getBackgroundData();
			double backgroundScale = curCalib.getBackgroundScale();
			int nCols = data.length;
			int nRows = data[0].length;
			double[][] subtracted = new double[nRows][nCols];
			for(int x = 0; x < nCols; x++) {
				for(int y = 0; y < nRows; y++) {
					subtracted[x][y] = data[x][y] - backgroundScale * backgroundData[x][y];
				}
			}
			return subtracted;
		}
		private void loadFTImage() {
			
			int nCols = fourierTransformData[0].length;
			int nRows = fourierTransformData[0][0].length;
			double[][] data = new double[nCols][nRows];

			switch(ftViewPanel.getView()) {
			case IMAGINARY:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = fourierTransformData[1][x][y]; 
				break;
			case MODULUS:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = Math.sqrt(Math.pow(fourierTransformData[0][x][y], 2) + 
								Math.pow(fourierTransformData[1][x][y], 2));
				break;
			case POWER_SPECTRUM:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = Math.pow(fourierTransformData[0][x][y], 2) + Math.pow(fourierTransformData[1][x][y], 2);
				break;
			case REAL:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = fourierTransformData[0][x][y];
				break;
			default:
				break;
			}
			recalc2ndDers = false;
			loadImage(data);
			recalc2ndDers = true;
		}
		public double[][] getFourierTransformImageData() {
			
			int nCols = fourierTransformData[0].length;
			int nRows = fourierTransformData[0][0].length;
			double[][] data = new double[nCols][nRows];

			switch(ftViewPanel.getView()) {
			case IMAGINARY:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = fourierTransformData[1][x][y]; 
				break;
			case MODULUS:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = Math.sqrt(Math.pow(fourierTransformData[0][x][y], 2) + 
								Math.pow(fourierTransformData[1][x][y], 2));
				break;
			case POWER_SPECTRUM:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = Math.pow(fourierTransformData[0][x][y], 2) + Math.pow(fourierTransformData[1][x][y], 2);
				break;
			case REAL:
				for(int x = 0; x < nCols; x++)
					for(int y = 0; y < nRows; y++)
						data[x][y] = fourierTransformData[0][x][y];
				break;
			default:
				break;
			}
			return data;
			
		}
		private void loadImage(double[][] data) {
			if(isSubtractingBackground && data == inputData)
				data = subtractBackground(data);
			imgWidth = data.length;
			imgHeight = data[0].length;
			imageData = new double[imgWidth][imgHeight];
			if(recalc2ndDers) {
				new2ndDers(imgWidth, imgHeight);
			}
			image = (BufferedImage) imagePanel.createImage(imgWidth, imgHeight);
			WritableRaster raster = image.getRaster();
			SigmoidalColorModel sigModel = colorChooser.getSigmoidColorModel();
			ColorModel model = image.getColorModel();
			Color color;
			Object colorData;
			imageMin = Double.MAX_VALUE;
			imageMax = Double.MIN_VALUE;
			int[] minLoc = new int[2];
			int[] maxLoc = new int[2];
			double CUTOFF = 1e4;
			double cur = 0;
			for(int i = 0; i < imgWidth; i++) {
				for(int j = 0; j < imgHeight; j++) {
					if(type == maskFilterType.WholeImage)
						cur = applyFilteringFilter(data, i, j);
					else
						cur = data[i][j];
					
					cur = applyScalingFilter(cur);
					if(cur < imageMin) { 
						imageMin = cur;
						minLoc[0] = i;
						minLoc[1] = j;
					}
					if(cur > imageMax) { 
						imageMax = cur;
						maxLoc[0] = i;
						maxLoc[1] = j;
					}
				}
			}
			imageMin = getMin(imageMin);
			imageMax = getMax(imageMax);
			if (autoLevelImage.isSelected()) {
				double[] vals = new double[] {imageMin, imageMax};
				Color[] col = new Color[] {Color.white, Color.black};
				colorChooser.newLevels(vals, col);
			}
			double I;
//			if(min < 0) { min = 0; }
			for(int i = 0; i < imgWidth; i++) {
				for(int j = 0; j < imgHeight; j++) {
//					imageData[i][j] = data[i][j];
//					X2ndDer[i][j] = 0;
//					Y2ndDer[i][j] = 0;
					if(type == maskFilterType.WholeImage)
						I = applyFilteringFilter(data, i, j);
					else
						I = data[i][j];
					I = applyScalingFilter(I);
					imageData[i][j] = I;
					color = sigModel.getColor(I);
//					I = Math.rint();
//					imageData[i][j] = ((I - imageMin) / (imageMax - imageMin)) * 255;
//					I = Math.rint(imageData[i][j]);
//					I = (int) I;
//					if(I < 0) { I = 0; }
//					if(I > 255) { I = 255; }
//					I = 255-I;
//					color = getColor(I);
					colorData = model.getDataElements(color.getRGB(), null);
					raster.setDataElements(i, j, colorData);
				}
			}
			if(getMouseMotionListeners().length != 1) {
				addMouseMotionListener(new ImageMouseMotionListener());
			}
			if(getMouseWheelListeners().length != 1) {
				addMouseWheelListener(new ImageMouseWheelListener());
			}
			coordsPanel.setMinMax(imageMin, imageMax);
			setFocusable(true);
			getParent().repaint();
			if(recalc2ndDers)
				calc2ndDers();
		}
		private void new2ndDers(int w, int h) {
			X2ndDer = new double[w][h];
			Y2ndDer = new double[w][h];
		}
		public void calc2ndDers() {
			setHaveCalculated2ndDers(true);
			if(imageData.length == 0)
				return;
			double[] vals;
			for(int i = 7; i < X2ndDer.length-7; i++) {
				for(int j = 7; j < X2ndDer[0].length-7; j++) {
					vals = pixelAnalysis.calc2ndDer(inputData, new Point(i, j)); 
					X2ndDer[i][j] = vals[1];
					Y2ndDer[i][j] = vals[0];
				}
			}
			
		}
		/**
		 * 
		 * @param xOrY x=0, y=1
		 */
		public void display2ndDers(int xOrY) {
			recalc2ndDers = false;
			switch(xOrY) {
			case 0:
				loadImage(X2ndDer);
				break;
			case 1:
				loadImage(Y2ndDer);
				break;
			case 2:
				double[][] sum = new double[X2ndDer.length][X2ndDer[0].length];
				for(int i = 0; i < X2ndDer.length; i++)
					for(int j = 0; j < X2ndDer[0].length; j++)
						sum[i][j] = X2ndDer[i][j] + Y2ndDer[i][j];
				
				loadImage(sum);
				break;
			}
			recalc2ndDers = true;
		}
		public boolean insideBounds(Point p) {
			if(p.x >= zoomX2) { return false; }
			if(p.x < zoomX1) { return false; }
			if(p.y >= zoomY2) { return false; }
			if(p.y < zoomY1) { return false; }
			return true;
		}
		public double getIntensity(Point p, double[][] data) {
//			return imageData[p.x][p.y];
			try {
				double val = imagePanel.applyFilteringFilter(data, p.x, p.y);
				val = imagePanel.applyScalingFilter(val);
				return val;
			} catch(ArrayIndexOutOfBoundsException e) {
				return Double.NaN;
			}
		}
		private Point viewCoordsToImageCoords(Point p) {
			int newx1 = zoomX1 + (int) Math.rint(((double) zoomX2 - (double) zoomX1) / (viewWidth) * p.x);
			int newy1 = zoomY1 + (int) Math.rint(((double) zoomY2 - (double) zoomY1) / (viewHeight) * p.y);
			
			return new Point(newx1, newy1);
		}
		private Point2D.Double viewCoordsToImageCoords(Point2D.Double p) {
			double newx1 = zoomX1 + ((double) zoomX2 - (double) zoomX1) / (viewWidth) * p.x;
			double newy1 = zoomY1 + ((double) zoomY2 - (double) zoomY1) / (viewHeight) * p.y;
			
			return new Point2D.Double(newx1, newy1);
		}
		private Point imageCoordsToViewCoords(Point p) {
			int newx1 = (int) Math.rint( ( (double) (p.x - zoomX1) ) / ( (double) (zoomX2 - zoomX1) ) * viewWidth);
			int newy1 = (int) Math.rint( ( (double) (p.y - zoomY1) ) / ( (double) (zoomY2 - zoomY1) ) * viewHeight);
			
			return new Point(newx1, newy1);
		}
		public void loadTempData() {
			// there are spots/pixels available for loading
			boolean validFile = false;
			while(!validFile) {
				chooserMultiFile.showOpenDialog(null);
				File tempFile = chooserMultiFile.getSelectedFile();
				validFile = selectionPanel.loadJavaObject(tempFile);
				if(!validFile) {
					switch(JOptionPane.showConfirmDialog(null,
							"Your file was not the correct Java object, either Pixels or Spots.  Do you wish to try another file?", 
							"Load Spot Data", 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE, 
							null)) {
					case 0: validFile = false;
					break;
					case 1: validFile = true;
					break;
					}
				}
			}
		}
		
		public double[][] getQValAxes(double[][] inputData) {
			int nCols = inputData.length;
			int nRows = inputData[0].length;
			Point p = new Point();
			int sign = 1;
			double x0 = 0, y0 = 0;
			switch(coordsPanel.coords) {
			case GENERAL_IMAGE:
			case CALCULATED_SPACE:
			case REAL_SPACE:
				x0 = coordsPanel.xMid;
				y0 = coordsPanel.yMid;
				break;
			case RECIPROCAL_SPACE:
				x0 = Double.valueOf(curCalib.getParam(Calibration.parameters.x) + "");
				y0 = Double.valueOf(curCalib.getParam(Calibration.parameters.y) + "");
				curCalib.setParam("x", x0 + "", 0);
				curCalib.setParam("y", y0 + "", 0);
				break;
			default:
				break;
			
			}
			int xMin = 0;
			int yMin = 0;
			int yMax = nRows;
			int xMax = nCols;
			int maskSize = 0;
			switch(filter) {
			case Max:
			case Median:
			case Min:
				maskSize = imagePanel.maskSize;
				xMin = maskSize;
				yMin = maskSize;
				yMax = nRows - maskSize;
				xMax = nCols - maskSize;
				break;
			case None:
				break;
			default:
				break;
			
			}
			
			double[][] qVals = new double[2][];
			qVals[0] = new double[xMax - xMin];
			qVals[1] = new double[yMax - yMin];
			p.y = (int) Math.rint(y0);
			for(p.x = xMin; p.x < xMax; p.x++) {
				if(p.x < x0)
					sign = -1;
				else
					sign = 1;
				switch(coordsPanel.coords) {
				case CALCULATED_SPACE:
					qVals[0][p.x-maskSize] = sign * coordsPanel.pixToQ_Calc(p);
					break;
				case REAL_SPACE:
					break;
				case RECIPROCAL_SPACE:
					qVals[0][p.x-maskSize] = sign * coordsPanel.pixToQ(p);
					break;
				default:
					break;
				}
			}
			
			p.x = (int) Math.rint(x0);
			for(p.y = yMin; p.y < yMax; p.y++) {
				if(p.y < y0)
					sign = -1;
				else
					sign = 1;

				switch(coordsPanel.coords) {
				case CALCULATED_SPACE:
					qVals[1][p.y-maskSize] = sign * coordsPanel.pixToQ_Calc(p);
					break;
				case REAL_SPACE:
					break;
				case RECIPROCAL_SPACE:
					qVals[1][p.y-maskSize] = sign * coordsPanel.pixToQ(p);
					break;
				default:
					break;
				}
			}	
			
			return qVals;
		}
		
		public void fourierTransform() {
			int colsFirst = 0; // 0 - rowsFirst, 1 - colsFirst
			int outputType = 0; // 0 - complex modulus, 1 - real only, 2 - imaginary only, 3 - power spectrum
			
			double[][] qVals = getQValAxes(imageData);
			
			double qxmax = 10;
			double qymax = 10;
			if(qxmax > Math.abs(qVals[0][0])) 
				qxmax = Math.abs(qVals[0][0]);
			if(qymax > Math.abs(qVals[1][0]))
				qymax = Math.abs(qVals[1][0]);
			if(qxmax > qymax)
				qxmax = qymax;
			if(qymax > qxmax)
				qymax = qxmax;
			double qxstep = 0.01;
			double qystep = 0.01;
			boolean interpolate = true;
			double[][] interpolated = imageData;
			switch(imgViewPanel.getView()) {
			case BACKGROUND:
				break;
			case FOURIER_TRANSFORM:
				break;
			case INPUT_IMAGE:
				qxstep = qystep = Double.valueOf(coordsPanel.txtQPerPix.getText());
				interpolated = inputData;
				break;
			case CALCULATED:
				qxstep = qystep = imagePanel.xrayFile.getqStep();
				interpolated = imagePanel.getCalculatedData();
				interpolated = imagePanel.imageData;
				break;
			case GENERAL_IMAGE:
				interpolate = false;
				qxstep = qystep = Double.valueOf(coordsPanel.txtQPerPix.getText());
				interpolated = inputData;
				break;
			default:
				break;
			}

			if(interpolate)
				interpolated = interpolate2D.Interpolate.interpolate(interpolated, qVals, qxstep, qystep, qxmax, qymax);

//			imagePanel.loadImage(interpolated);
//			return;
			
			coordsPanel.xNumPixInOriginalImage = interpolated.length;
			coordsPanel.xNumPixInOriginalImage = interpolated[0].length;
//			loadImage(ft);
			int nPixX = interpolated.length;
			int nPixY = interpolated[0].length;
			int xMid = nPixX / 2;
			int yMid = nPixY / 2;
			coordsPanel.setImageParams(xMid, yMid, nPixX, nPixY, qxstep);
			DirectFourierTransform.useGPU = true;
			fourierTransformData = DirectFourierTransform.direct(interpolated, colsFirst, outputType);
			
			System.out.println("Test");
			fourierTransformData = shiftByHalfHalf(fourierTransformData);
			fourierTransformData[0][xMid][yMid] = (fourierTransformData[0][xMid][yMid-1] + fourierTransformData[0][xMid-1][yMid] + 
					fourierTransformData[0][xMid+1][yMid] + fourierTransformData[0][xMid][yMid+1] + 
					fourierTransformData[0][xMid-1][yMid-1] + fourierTransformData[0][xMid-1][yMid+1] + 
					fourierTransformData[0][xMid+1][yMid-1] + fourierTransformData[0][xMid+1][yMid+1] ) / 8;
			fourierTransformData[1][xMid][yMid] = (fourierTransformData[1][xMid][yMid-1] + fourierTransformData[1][xMid-1][yMid] + 
					fourierTransformData[1][xMid+1][yMid] + fourierTransformData[1][xMid][yMid+1] + 
					fourierTransformData[1][xMid-1][yMid-1] + fourierTransformData[1][xMid-1][yMid+1] + 
					fourierTransformData[1][xMid+1][yMid-1] + fourierTransformData[1][xMid+1][yMid+1] ) / 8;
			imagePanel.setFourierTransformData(fourierTransformData);
			loadFTImage();

			coordsPanel.buttonClick(Coordinates.REAL_SPACE);
			imgViewPanel.buttonClick(CurrentView.FOURIER_TRANSFORM);
			
		}
		private double[][][] shiftByHalfHalf(double[][][] input) {
			int nCols = input[0].length;
			int nRows = input[0][0].length;
			double[][][] newArr = new double[2][nCols][nRows];
			int newX, newY;
			for(int x = 0; x < nCols; x++) {
				newX = (x + (nCols/2) ) % nCols;
				for(int y = 0; y < nRows; y++) {
					newY = (y + (nRows/2) ) % nRows;
					newArr[0][newX][newY] = input[0][x][y];
					newArr[1][newX][newY] = input[1][x][y];
				}
			}
			return newArr;
		}
		private double[][] ftCols(double[][] inputData, int outputType) {
			double[][] byCols = new double[inputData.length][inputData[0].length];
			int nRows = byCols[0].length;
			int nCols = byCols.length;
			for(int ky = 0; ky < nRows; ky++) {
				for(int kx = 0; kx < nCols; kx++) {
					double sumReal = 0;
					double sumImag = 0;
					for(int t = 0; t < nCols; t++) {
						sumReal +=  inputData[t][ky] * Math.cos(2*Math.PI * t * kx / nCols);
						sumImag += -inputData[t][ky] * Math.sin(2*Math.PI * t * kx / nCols);
					}
					switch(outputType) {
					case 0: 
						byCols[kx][ky] = Math.sqrt(Math.pow(sumReal, 2) + Math.pow(sumImag, 2));
						break;
					case 1:
						byCols[kx][ky] = sumReal;
						break;
					case 2:
						byCols[kx][ky] = sumImag;
						break;
					}
				}
			}
			
			return byCols;
		}
		private double[][] ftRows(int outputType) {
			double[][] byRows = new double[imageData.length][imageData[0].length];
			int nCols = byRows.length;
			int nRows = byRows[0].length;
			for(int kx = 0; kx < nCols; kx++) {
				for(int ky = 0; ky < nRows; ky++) {
					double sumReal = 0;
					double sumImag = 0;
					for(int t = 0; t < nRows; t++) {
						sumReal +=  imageData[kx][t] * Math.cos(2*Math.PI * t * ky / nRows);
						sumImag += -imageData[kx][t] * Math.sin(2*Math.PI * t * ky / nRows);
					}
					switch(outputType) {
					case 0: 
						byRows[kx][ky] = Math.sqrt(Math.pow(sumReal, 2) + Math.pow(sumImag, 2));
						break;
					case 1:
						byRows[kx][ky] = sumReal;
						break;
					case 2:
						byRows[kx][ky] = sumImag;
						break;
					}
				}
			}
			return byRows;
		}
		private int getFileIdx(File f) {
			String f1Path = f.getAbsolutePath();
			for(int i = 0; i < filesInFolder.length; i++) {
				String f2Path = filesInFolder[i].getAbsolutePath();
				int compare = f1Path.compareTo(f2Path);
				if(compare == 0) { return i; }
			}
			return -1;
		}
		public void loadImageFile() {
			if(filesInFolder != null && curFileIdx > 0 && curFileIdx < filesInFolder.length) { 
				chooserMultiFile.setSelectedFile(filesInFolder[curFileIdx]); 
			}
			ImagePropertiesViewer imgPropViewer = new ImagePropertiesViewer(chooserMultiFile);
			chooserMultiFile.setAccessory(imgPropViewer);
//			imgPropViewer.update();
			int returnVal = chooserMultiFile.showDialog(this, "Open");
			switch(returnVal) {
			case JFileChooser.APPROVE_OPTION:
				File f = chooserMultiFile.getSelectedFile();
				filesInFolder = f.getParentFile().listFiles();
				curFileIdx = getFileIdx(f);
				break;
			case JFileChooser.CANCEL_OPTION:
				return;
			}
			contextPanel.txt.setText("Currently Displayed File: " + filesInFolder[curFileIdx].toString());
			switch(JOptionPane.showConfirmDialog(null,
					"Do you have pixels or spots to load for this image?", 
					"Load Spot Data", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.QUESTION_MESSAGE, 
					null)) {
			case 0:
				loadTempData();
				break;
			}
			imgProp = imgPropViewer.parseSelections();
			curImageFile = new BIN(imgProp);
			readFile();
		}
		private void readFile() {
			contextPanel.txt.setText("Currently Displayed File: " + filesInFolder[curFileIdx].toString());
			inputData = curImageFile.readFile(filesInFolder[curFileIdx]);
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for(int i = 0; i < inputData.length; i++) {
				for(int j = 0; j < inputData[i].length; j++) {
					if(min > inputData[i][i]) { min = inputData[i][j]; }
					if(max < inputData[i][j]) { max = inputData[i][j]; }
				}
			}
			coordsPanel.setMinMax(min, max);
			loadImage(inputData);
			
			outputCurrentImage();
			
			setHaveCalculated2ndDers(false);
			resetZoom();
			getParent().repaint();
		}
		
		public void outputSpotsOverlay() {
			String fileOutput = filesInFolder[curFileIdx].getName();
			fileOutput = fileOutput.substring(0, fileOutput.lastIndexOf("."));
			String imgType = imageOptionsFrame.getImageOutputType();
			if(imgType.compareTo("no output") != 0) {
				File folder = new File(filesInFolder[curFileIdx].getParent() + File.separator + "images" + File.separator + "spotsOverlay");
				folder.mkdirs();
				if(!folder.mkdir() && !folder.exists()) {
					System.err.println("Creation of image output folder: " + folder.getAbsolutePath() + " failed. Do you have write privileges for this folder?");
				} else {
					File imgOutput = new File(folder + File.separator + fileOutput + "." + imgType);
					try {
						ImageIO.write(image, imgType, imgOutput);
						System.out.println("Image type: " + imgType + " for spots overlay written to file: " + imgOutput.getAbsolutePath());
					} catch (IOException e) {
						System.err.println("Image output exception for imageType: " + imgType + " and file: " + imgOutput.getAbsolutePath());
						System.err.println("IOException e: " + e.getMessage());
					}
				}
			}
		}
		private void outputCurrentImage() {
			String fileOutput = filesInFolder[curFileIdx].getName();
			fileOutput = fileOutput.substring(0, fileOutput.lastIndexOf("."));
			String imgType = imageOptionsFrame.getImageOutputType();
			if(imgType.compareTo("no output") != 0) {
				File folder = new File(filesInFolder[curFileIdx].getParent() + File.separator + "images" + File.separator + "raw");
				folder.mkdirs();
				if(!folder.mkdir() && !folder.exists()) {
					System.err.println("Creation of image output folder: " + folder.getAbsolutePath() + " failed. Do you have write privileges for this folder?");
				} else {
					File imgOutput = new File(folder + File.separator + fileOutput + "." + imgType);
					try {
						ImageIO.write(image, imgType, imgOutput);
						System.out.println("Image type: " + imgType + " written to file: " + imgOutput.getAbsolutePath());
					} catch (IOException e) {
						System.err.println("Image output exception for imageType: " + imgType + " and file: " + imgOutput.getAbsolutePath());
						System.err.println("IOException e: " + e.getMessage());
					}
				}
			}
		}
		
		public Pixel[] getPixelsInsideShape() {
			Vector<Pixel> pix = new Vector<Pixel>();
			Rectangle viewableBounds = shape.getBounds();
			// convert viewable bounds into image bounds
			Point rectMin = new Point((int)viewableBounds.getX(), (int)viewableBounds.getY());
			Point rectMax = new Point((int)(rectMin.x + viewableBounds.getWidth()), (int)(rectMin.y + viewableBounds.getHeight()));
			rectMin = viewCoordsToImageCoords(rectMin);
			rectMax = viewCoordsToImageCoords(rectMax);
			// convert xray image pixels into viewable area pixels
			Point tempP = new Point();
			Pixel p;
			double[] qphi = new double[2];
			Point point = new Point();
			for(int x = rectMin.x; x < rectMax.x; x++) {
				for(int y = rectMin.y; y < rectMax.y; y++) {
					tempP.x = x;
					tempP.y = y;
					tempP = imageCoordsToViewCoords(tempP);
					if(shape.contains(tempP)) {
						p = new Pixel(x, y, imageData[x][y]);
						point.x = x;
						point.y = y;
						switch(coordsPanel.coords) {
						case CALCULATED_SPACE:
							qphi[0] = xrayFile.pointToQ(point).length();
							qphi[1] = coordsPanel.getPhi(point);
							break;
						case REAL_SPACE:
							qphi[0] = coordsPanel.pixToDist(point);
							qphi[1] = coordsPanel.getPhi(point);
							break;
						case RECIPROCAL_SPACE:
							if(calib != null)
								qphi = calib.coordsToQAndPhi(x, y);
							else {
								qphi[0] = 0;
								qphi[1] = coordsPanel.getPhi(point);
							}
							break;
						default:
							break;
						
						}
						p.setDist(qphi[0]);
						p.setPhi(qphi[1]);
						pix.add(p);
					}
				}
			}
			return pix.toArray(new Pixel[pix.size()]);
		}
		public void getPixelsBetweenPoints(Point2D.Double p1, Point2D.Double p2, Vector<Pixel> pix) {
			p1 = viewCoordsToImageCoords(p1);
			p2 = viewCoordsToImageCoords(p2);
			double xAdd = 1;
			double yAdd = 1;
			int endIdx = 0;
			double slope = 0;
			// for horizontal and vertical lines and when the slope = +/- 1 the slope is irrelevant
			// vertical line
			if(p2.x - p1.x == 0) {
				xAdd = 0;
				// line points down
				if(p2.y < p1.y) { yAdd = -1; } 
			}
			// horizontal line
			else if(p2.y - p1.y == 0) {
				yAdd = 0;
				// line points left
				if(p2.x < p1.x) { xAdd = -1; }
			}
			else {
				slope = (p2.y - p1.y) / (p2.x - p1.x);
				if(slope > 0) {
					if(slope >= 1) {
						xAdd = 1F/slope;
						if(p2.y < p1.y) {	// line points left 
							yAdd = -1; 
							xAdd = -xAdd;
						}
					}
					else {	// 0 < slope < 1
						yAdd = slope;
						if(p2.y < p1.y) {// line points left
							xAdd = -1; 
							yAdd = -yAdd;
						} 
					}
				}
				else {	// slope < 0
					if(slope <= -1) {
						xAdd = 1F/slope;
						if(p2.x > p1.x){ // line points right 
							yAdd = -1; 
							xAdd = -xAdd;
						} 
					}
					else {	// 0 > slope > -1
						yAdd = slope;
						if(p2.y > p1.y) {	// line points right
							xAdd = -1; 
							yAdd = -yAdd;
						}
					}
				}
			}
			double dy = Math.abs(p2.y - p1.y);
			double dx = Math.abs(p2.x - p1.x); 
			endIdx = (int) Math.rint(dy > dx ? dy : dx); 
			int x = 0;
			int y = 0;
			double[][] data = imageData;
			switch(coordsPanel.coords) {
			case CALCULATED_SPACE:
				data = imageData;
				break;
			case REAL_SPACE:
				data = imagePanel.getFourierTransformImageData();
				break;
			case RECIPROCAL_SPACE:
				data = imageData;
				break;
			default:
				break;
			}
			for(int idx = 0; idx <= endIdx; idx++) {
				x = (int) Math.rint(p1.x + idx*xAdd);
				y = (int) Math.rint(p1.y + idx*yAdd);
				pix.add(new Pixel(x, y, data[x][y]));
			}
		}
		public Pixel[] getPixelsUnderLine() {
			Vector<Pixel> pix = new Vector<Pixel>();
			PathIterator path = shape.getPathIterator(null);
			double[] points = new double[6];
			int seg;
			Point2D.Double beginning = new Point2D.Double();
			Point2D.Double start = new Point2D.Double();
			Point2D.Double end = new Point2D.Double();
			while(!path.isDone()) {
				seg = path.currentSegment(points);
				switch(seg) {
				case PathIterator.SEG_CLOSE:
					getPixelsBetweenPoints(start, beginning, pix);
					break;
				case PathIterator.SEG_MOVETO:
					beginning.x = points[0];
					beginning.y = points[1];
					start.x = points[0];
					start.y = points[1];
					break;
				case PathIterator.SEG_LINETO:
					end.x = points[0];
					end.y = points[1];
					getPixelsBetweenPoints(start, end, pix);
					start.x = end.x;
					start.y = end.y;
					break;
				}
				path.next();
			}
			Pixel[] pixels = new Pixel[pix.size()];
			pixels = pix.toArray(pixels);
			return pixels;
		}
		public Spot[] spotPick(Vector<Vector<Pixel>> pix) {
			Vector<Pixel> activePix = pix.remove(0);
			while(activePix == null && pix.size() > 0) {
				activePix = pix.remove(0);
			}
			while(pix.size() > 0) {
				activePix.addAll(pix.remove(0));
			}
			activePix = removeDuplicates(activePix);
			
			Vector<Spot> spots = coalescePixels(activePix);
			Spot[] theSpots = new Spot[spots.size()];
			theSpots = spots.toArray(theSpots);
			return theSpots;
		}
		public Spot[] spotPick(double threshold) {
			Vector<Pixel> activePix = getActivePixels(threshold);
			Vector<Spot> spots = coalescePixels(activePix);
			Spot[] theSpots = new Spot[spots.size()];
			theSpots = spots.toArray(theSpots);
			return theSpots;
		}
		private Vector<Pixel> getActivePixels(double threshold) {
			Vector<Pixel> pix = new Vector<Pixel>();
			calc2ndDers();
			double[] ders;
			for(int x = 3; x < imageData.length-3; x++) {
				for(int y = 3; y < imageData[x].length-3; y++) {
					if(X2ndDer[x][y] < threshold && Y2ndDer[x][y] < threshold) {
						pix.add(new Pixel(x, y, imageData[x][y], 0));
					}
				}
			}
			return pix;
		}
		
		private Vector<Pixel> findActivePixels_Threshold(double thresholdVal, boolean selectionCriteria) {
			Vector<Pixel> pix = new Vector<Pixel>();
			calc2ndDers();
			boolean and=false, greaterOr=false, xless=false, yless=false;
			double xval=0, yval=0;
			for(int x = 0; x < imageData.length; x++) {
				for(int y = 0; y < imageData[x].length; y++) {
					and = false;
					greaterOr = false;
					xval=X2ndDer[x][y];
					yval = Y2ndDer[x][y];
					xless = X2ndDer[x][y] < thresholdVal;
					yless = Y2ndDer[x][y] < thresholdVal;
					if(xless && yless)
						and = true;
					else if(xless && !yless && Math.abs(xval) > yval)
						greaterOr = true;
					else if(yless && !xless && Math.abs(yval) > xval)
						greaterOr = true;
					
					if(selectionCriteria == (and)) {// || greaterOr)) {
						pix.add(new Pixel(x, y, imageData[x][y], 0));
					}
				}
			}
			return pix;
		}
		
		private final static int BETWEEN = 0;
		private final static int GREATER = 1;
		private final static int LESS = 2; 
		private final static int EQUAL = 3;
		private final static int NOT_EQUAL = 4;
		
		private Vector<Pixel> findActivePixels_Simple(double[] thresholdVal, boolean selectionCriteria, int type) {
			Vector<Pixel> pix = new Vector<Pixel>();
			for(int x = 0; x < imageData.length; x++) {
				for(int y = 0; y < imageData[x].length; y++) {
					switch(type) {
					case BETWEEN:
						if(selectionCriteria == (imageData[x][y] > thresholdVal[0] && imageData[x][y] < thresholdVal[1])) {
							pix.add(new Pixel(x, y, imageData[x][y], 0));
						} 
						break;
					case GREATER:
						if(selectionCriteria == (imageData[x][y] > thresholdVal[0])) {
							pix.add(new Pixel(x, y, imageData[x][y], 0));
						} 
						break;
					case LESS:
						if(selectionCriteria == (imageData[x][y] < thresholdVal[0])) {
							pix.add(new Pixel(x, y, imageData[x][y], 0));
						} 
						break;
					case EQUAL:
						if(selectionCriteria == (imageData[x][y] == thresholdVal[0])) {
							pix.add(new Pixel(x, y, imageData[x][y], 0));
						} 
						break;
					case NOT_EQUAL:
						if(selectionCriteria == (imageData[x][y] != thresholdVal[0])) {
							pix.add(new Pixel(x, y, imageData[x][y], 0));
						} 
						break;
					}
				}
			}
			return pix;
		}
		public Vector<Vector<Pixel>> getActivePixels() {
			Vector<Vector<Pixel>> activePixels = new Vector<Vector<Pixel>>();
			Vector<Pixel> temp;
			boolean[] thresh = activePixelFrame.getThresholdActive();
			boolean[] between = activePixelFrame.getBetweenActive();
			boolean[] greaterThan = activePixelFrame.getGreaterThanActive();
			boolean[] lessThan = activePixelFrame.getLessThanActive();
			boolean[] equalTo = activePixelFrame.getEqualToActive();
			boolean[] notEqualTo = activePixelFrame.getNotEqualToActive();
			
			if(thresh[0]) {
				double val = activePixelFrame.getThresholdVal();
				temp = findActivePixels_Threshold(val, thresh[1]);
				temp = removeDuplicates(temp);
				activePixels.add(temp);
			}
			
			if(between[0]) {
				double[] val = activePixelFrame.getBetweenVals();
				temp = findActivePixels_Simple(val, between[1], BETWEEN);
				temp = removeDuplicates(temp);
				activePixels.add(temp);
			}
			
			if(greaterThan[0]) {
				double val = activePixelFrame.getGreaterThanVal();
				temp = findActivePixels_Simple(new double[] {val}, greaterThan[1], GREATER);
				temp = removeDuplicates(temp);
				if(temp != null) {
					activePixels.add(temp);
				}
			}
			
			if(lessThan[0]) {
				double val = activePixelFrame.getLessThanVal();
				temp = findActivePixels_Simple(new double[] {val}, lessThan[1], LESS);
				temp = removeDuplicates(temp);
				activePixels.add(temp);
			}
			if(equalTo[0]) {
				double val = activePixelFrame.getEqualToVal();
				temp = findActivePixels_Simple(new double[] {val}, equalTo[1], EQUAL);
				temp = removeDuplicates(temp);
				activePixels.add(temp);
			}
			
			if(notEqualTo[0]) {
				double val = activePixelFrame.getNotEqualToVal();
				temp = findActivePixels_Simple(new double[] {val}, notEqualTo[1], NOT_EQUAL);
				temp = removeDuplicates(temp);
				activePixels.add(temp);
				
			}
			return activePixels;
		}
		/**
		 * Remove duplicate pixels from the input Vector data structure
		 * @param pix Array of pixels to check for and remove duplicates
		 * @return duplicate-free array of pixels
		 */
		private Vector<Pixel> removeDuplicates(Vector<Pixel> pix) {
			if(pix == null || pix.size() == 0) {
				return null;
			}
			Vector<Pixel> duplicatesRemoved = new Vector<Pixel>();
			Pixel pix1, pix2;
			duplicatesRemoved.add(pix.remove(0));
			while(pix.size() > 0) {
				pix1 = pix.remove(0);
				boolean alreadyPresent = false;
				for(int i = 0; i < duplicatesRemoved.size() && !alreadyPresent; i++) {
					pix2 = duplicatesRemoved.get(i);
					if(pix1.getX() == pix2.getX() && pix1.getY() == pix2.getY()) {
						alreadyPresent = true;
					}
				}
				if(!alreadyPresent) {
					duplicatesRemoved.add(pix1);
				}
			}
			return duplicatesRemoved;
		}
		private Vector<Spot> coalescePixels(Vector<Pixel> pix) {
			int minimumPixelsPerSpot = activePixelFrame.getCoalesceVal();
			Vector<Spot> spots = new Vector<Spot>();
			Pixel currentPix;
			Spot currentSpot;
			boolean added = false;
			int numUnfiledPix = pix.size();
			while(numUnfiledPix > 0) {
				// check if pix[i] fits inside one of the already existing spots
				currentPix = pix.get(0);
				for(int spotIdx = 0; spotIdx < spots.size(); spotIdx++) {
					// get the current spot
					currentSpot = spots.get(spotIdx);
					// test to see if the current pixel is within the block (spot_x-1, spot_y-1 and spot_x+1, spot_y+1)
					if(touchingSpot(currentSpot, currentPix)) {
						currentSpot.add(currentPix);
						pix.remove(currentPix);
						numUnfiledPix--;
						added = true;
						break;
					}
				}
				// if pix[i] doesn't fit in any of the already existing spots then create a new spot, add the pixel,
				// and add the spot to the list of spots
				if(!added) {
					currentSpot = new Spot("currentImage", 0);
					currentSpot.add(currentPix);
					pix.remove(currentPix);
					numUnfiledPix--;
					spots.add(currentSpot);
				}
				added = false;
			}
			// check to make sure the number of pixels per spot is greater or equal to the coalescence value
			int spotIdx = 0;
			while(true) {
				try { 
					currentSpot = spots.get(spotIdx);
				} catch(ArrayIndexOutOfBoundsException e) { break; }
				if(currentSpot.getNumPixels() < minimumPixelsPerSpot) {
					spots.remove(currentSpot);
				}
				else {
					// update each spots number to reflect its position in the Vector
					currentSpot.setSpotNumber(spotIdx);
					spotIdx++; 
				}
			}
			
			
			return spots;
		}
		
		private boolean touchingSpot(Spot currentSpot, Pixel currentPixel) {
			// loop through the current pixels in the spot
			Pixel[] spotPixels = currentSpot.getPixels();
			int x, y;
			for(int i = 0; i < spotPixels.length; i++) {
				x = spotPixels[i].getX() - currentPixel.getX();
				y = spotPixels[i].getY() - currentPixel.getY();
				if(Math.abs(x) <= 1 && Math.abs(y) <= 1) { return true; }
			}
			
			return false;
		}
		public void reloadImage() {
			loadImage(inputData);
			// TODO
		}
		public void paintCircle(int diameter) {
			Graphics2D g = image.createGraphics();
			int xC = (int) Math.rint((Double) curCalib.getParam(Calibration.parameters.x) - 0.5 * diameter);
			int yC = (int) Math.rint((Double) curCalib.getParam(Calibration.parameters.y) - 0.5 * diameter);
			g.setColor(c1);
			g.drawOval(xC, yC, diameter, diameter);
			getParent().repaint();
		}
		@Override
		public void keyPressed(KeyEvent arg0) {
			int keyCode = arg0.getKeyCode();
			switch(keyCode) {
			case KeyEvent.VK_UP:
				break;
			case KeyEvent.VK_LEFT:
				if(imagePanel.curFileIdx > 0) {
					imagePanel.curFileIdx--;
					imagePanel.readFile();
				}
				System.out.println("Left key pressed.");
				break;
			case KeyEvent.VK_RIGHT:
				if(imagePanel.curFileIdx < imagePanel.filesInFolder.length) {
					imagePanel.curFileIdx++;
					imagePanel.readFile();
				}
				System.out.println("Right key pressed.");
				break;
			case KeyEvent.VK_DOWN:
				break;
			}
		}
		@Override
		public void keyReleased(KeyEvent arg0) {
			
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			
		}
		public boolean haveCalculated2ndDers() { return haveCalculated2ndDers; }
		public void setHaveCalculated2ndDers(boolean haveCalculated2ndDers) { 
			this.haveCalculated2ndDers = haveCalculated2ndDers; 
		}
		public File getBackgroundFile() { return backgroundFile; }
		public void setBackgroundFile(File backgroundFile) { this.backgroundFile = backgroundFile; }
		public ImageFile getBackgroundFileType() { return backgroundFileType; }
		public void setBackgroundFileType(ImageFile backgroundFileType) { this.backgroundFileType = backgroundFileType; }
		public boolean isSubtractingBackground() { return isSubtractingBackground; }
		public void setSubtractingBackground(boolean isSubtractingBackground) { this.isSubtractingBackground = isSubtractingBackground; }
		public double[][] getInputData() { return inputData; }
		public void setInputData(double[][] inputData) { this.inputData = inputData; }
		public double[][][] getFourierTransformData() { return fourierTransformData; 	}
		public void setFourierTransformData(double[][][] fourierTransform) { this.fourierTransformData = fourierTransform; }
		public double[][] getCalculatedData() { return xrayFile.getMatrixData(); }
		public boolean isCalculatedImage() { return isCalculatedImage; }
		public void setCalculatedImage(boolean isCalculatedImage) { this.isCalculatedImage = isCalculatedImage; }
		 
	}
	class ImageKeyListener implements KeyListener {
		private volatile boolean keyPressed = false;
		private volatile boolean keyReleased = true;
		@Override
		public void keyPressed(KeyEvent arg0) {
			keyPressed = true;
			if(imagePanel != null && imagePanel.filesInFolder != null && keyReleased) {
				keyReleased = false;
				int keyCode = arg0.getKeyCode();
				switch(keyCode) {
				case KeyEvent.VK_UP:
					break;
				case KeyEvent.VK_LEFT:
					if(imagePanel.curFileIdx > 0) {
						imagePanel.curFileIdx--;
						imagePanel.readFile();
					}
					System.out.println("Left key pressed.");
					break;
				case KeyEvent.VK_RIGHT:
					if(imagePanel.curFileIdx < imagePanel.filesInFolder.length-1) {
						imagePanel.curFileIdx++;
						imagePanel.readFile();
					}
					System.out.println("Right key pressed.");
					break;
				case KeyEvent.VK_DOWN:
					break;
				}
			}
			keyPressed = false;
		}
		@Override
		public void keyReleased(KeyEvent arg0) {
			keyReleased = true;
			// TODO Auto-generated method stub
		}
		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class ImageMouseWheelListener implements MouseWheelListener {
		private double increment = .05;
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int direction = e.getWheelRotation();
			Point point = e.getPoint();
			Rectangle rect = imagePanel.getVisibleRect();
			Dimension d = imagePanel.getDimensions();
			int width = Math.min(rect.width, d.width);
			int height = Math.min(rect.height, d.height);

			if(d.width == d.height) {
				width = Math.min(rect.width, rect.height);
				height = width;
			}
			int xc = width / 2;
			int yc = height / 2;
			
			double val = 0.2;
			increment = (direction < 0) ? val : val;
			
			double weight = .1;
			int x0 = (int) (xc + (point.x - xc) * weight);
			int y0 = (int) (yc + (point.y - yc) * weight);
			
			double scalar = 1. + direction * increment;

			int x1 = (int) Math.rint(x0 - 0.5 * width * scalar);
			int y1 = (int) Math.rint(y0 - 0.5 * height * scalar);
			int x2 = (int) Math.rint(x0 + 0.5 * width * scalar);
			int y2 = (int) Math.rint(y0 + 0.5 * height * scalar);
			/*
			if(x1 < xMin) {
				x1 = xMin;
				x2 += (xMin-x1);
			}
			if(x2 > xMax) {
				x2 = xMax;
				x1 -= (xMax-x2);
			}
			x1 = (x1 < xMin) ? xMin : x1;
			y1 = (y1 < yMin) ? yMin : y1;
			x2 = (x2 > xMax) ? xMax : x2;
			y2 = (y2 > yMax) ? yMax : y2;
			*/
			Point p1 = new Point(x1, y1);
			Point p2 = new Point(x2, y2);
			
			System.out.println("Mouse wheel direction: " + direction);
			System.out.println(rect.toString());
			
			imagePanel.zoomTwoClick(p1, p2);
			System.out.println(imagePanel.getVisibleRect().toString());
	    }
	}
	class ImageMouseMotionListener implements MouseMotionListener {
		private long timeOfLastDrag;
		@Override
		public void mouseDragged(MouseEvent arg0) {
			long curTime = System.currentTimeMillis();
			if(curTime - timeOfLastDrag < 50) {
				
				System.out.println("Dragged: " + arg0.getPoint().toString());
			}
			timeOfLastDrag = System.currentTimeMillis();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			Point point = arg0.getPoint();
			point = imagePanel.viewCoordsToImageCoords(point);
			if(imagePanel.insideBounds(point)) {
				double[][] data = imagePanel.imageData;
				double I = 0;
				switch(imgViewPanel.getView()) {
				case BACKGROUND:
					data = calib.getBackgroundData();
					I = imagePanel.getIntensity(point, data);
					break;
				case FOURIER_TRANSFORM:
					double re = imagePanel.getFourierTransformData()[0][point.x][point.y];
					double im = imagePanel.getFourierTransformData()[1][point.x][point.y];
					switch(ftViewPanel.view) {
					case IMAGINARY:
						I = im;
						break;
					case MODULUS:
						I = Math.sqrt(re * re + im * im);
						break;
					case POWER_SPECTRUM:
						I = re * re + im * im;
						break;
					case REAL:
						I = re;
						break;
					}
					break;
				case INPUT_IMAGE:
					data = imagePanel.inputData;
					I = imagePanel.getIntensity(point, data);
					break;
				case CALCULATED:
					data = imagePanel.getCalculatedData();
					I = imagePanel.getIntensity(point, data);
					break;
				case GENERAL_IMAGE:
					data = imagePanel.inputData;
					I = imagePanel.getIntensity(point, data);
					break;
				case _2ND_DER_X:
					data = imagePanel.X2ndDer;
					I = imagePanel.getIntensity(point, data);
					break;
				case _2ND_DER_Y:
					data = imagePanel.Y2ndDer;
					I = imagePanel.getIntensity(point, data);
					break;
				case _2ND_DER_XY:
					I = imagePanel.getIntensity(point, imagePanel.X2ndDer) +
							imagePanel.getIntensity(point, imagePanel.Y2ndDer);
					break;
					
				default:
					break;
				}
 				try {
 					JVector Q = imagePanel.getQ(point);
 					coordsPanel.updateInfo(point, I, Q);
 				} catch(Exception e) {
 	 				coordsPanel.updateInfo(point, I);
 				}
			}
		}
	}
	class FilterPanel extends JPanel {
		public FilterPanel() {
			super();
			setBorder(new TitledBorder("Filter Options"));
			setup();
			setMaximumSize(new Dimension(EAST_WIDTH, getPreferredSize().height));
		}
		private void setup() {
			int nRows = 0;
			int nCols = 3;
			GridLayout btnLayout = new GridLayout(nRows, nCols);
			
			Box boxVal1 = Box.createHorizontalBox();
			final JLabel lblVal1 = new JLabel("Minimum Value: ");
			final JTextField txtVal1 = new JTextField(5);
			txtVal1.setEnabled(false);
			txtVal1.setText(0 + "");
			txtVal1.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			boxVal1.add(lblVal1);
			boxVal1.add(Box.createHorizontalStrut(5));
			boxVal1.add(txtVal1);
			boxVal1.add(Box.createGlue());
			
			Box boxVal2 = Box.createHorizontalBox();
			final JLabel lblVal2 = new JLabel("Maximum Value: ");
			final JTextField txtVal2 = new JTextField(5);
			txtVal2.setEnabled(false);
			txtVal2.setText(0 + "");
			txtVal2.addFocusListener(new DataTypeChecker(DataTypeChecker.DOUBLE));
			boxVal2.add(lblVal2);
			boxVal2.add(Box.createHorizontalStrut(5));
			boxVal2.add(txtVal2);
			boxVal2.add(Box.createGlue());
			
			Box boxMaskSize = Box.createHorizontalBox();
			final JLabel lblMaskSize = new JLabel("Mask Size (+/-)");
			final JTextField txtMaskSize = new JTextField(5);
			txtMaskSize.setEnabled(false);
			txtMaskSize.setText(imagePanel.maskSize + "");
			txtMaskSize.addFocusListener(new DataTypeChecker(DataTypeChecker.INTEGER, 0, -1));
			boxMaskSize.add(lblMaskSize);
			boxMaskSize.add(Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH));
			boxMaskSize.add(txtMaskSize);
			boxMaskSize.add(Box.createGlue());
			
			JPanel pnlImageOrClick = new JPanel();
			pnlImageOrClick.setLayout(new GridLayout(0, 3));
			ButtonGroup grpImageOrClick = new ButtonGroup();

			ActionListener al_clickType = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(arg0.getSource() instanceof JToggleButton) {
						JToggleButton btn = (JToggleButton) arg0.getSource();
						maskFilterType type = maskFilterType.valueOf(btn.getText());
						imagePanel.setClickOrWholeImage(type);
					}
				}
			};
			
			for(maskFilterType type : maskFilterType.values()) {
				JToggleButton btn = new JToggleButton(type.name());
				btn.addActionListener(al_clickType);
				grpImageOrClick.add(btn);
				pnlImageOrClick.add(btn);
				if(type == maskFilterType.WholeImage)
					btn.doClick();
			}
			
			JPanel pnlScaling = new JPanel();	
			JPanel pnlScalingButtons = new JPanel();
			pnlScalingButtons.setLayout(btnLayout);
			pnlScaling.setLayout(new BoxLayout(pnlScaling, BoxLayout.Y_AXIS));
			pnlScaling.setBorder(BorderFactory.createTitledBorder("Scaling"));
			pnlScaling.add(pnlScalingButtons);

			JPanel pnlRange = new JPanel();
			JPanel pnlRangeButtons = new JPanel();
			pnlRangeButtons.setLayout(btnLayout);
			pnlRange.setLayout(new BoxLayout(pnlRange, BoxLayout.Y_AXIS));
			pnlRange.setBorder(BorderFactory.createTitledBorder("Range"));
			pnlRange.add(pnlRangeButtons);
			
			JPanel pnlMask = new JPanel();
			JPanel pnlMaskButtons = new JPanel();
			pnlMaskButtons.setLayout(btnLayout);
			pnlMask.setLayout(new BoxLayout(pnlMask, BoxLayout.Y_AXIS));
			pnlMask.setBorder(BorderFactory.createTitledBorder("Mask Filter"));
			pnlMask.add(pnlMaskButtons);
			
			ButtonGroup groupScaling = new ButtonGroup();
			ButtonGroup groupRange = new ButtonGroup();
			ButtonGroup groupFiltering = new ButtonGroup();

			
			ActionListener al_scaling = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(arg0.getSource() instanceof JToggleButton) {
						JToggleButton btn = (JToggleButton) arg0.getSource();
						imagePanel.setFilterScale(imageFilterScaling.valueOf(btn.getText()));
					}
				}
			};
			
			ActionListener al_range = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(arg0.getSource() instanceof JToggleButton) {
						JToggleButton btn = (JToggleButton) arg0.getSource();
						imagePanel.setFilterRange(imageFilterRange.valueOf(btn.getText()));
					}
				}
			};

			
			ActionListener al_filtering = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(arg0.getSource() instanceof JToggleButton) {
						JToggleButton btn = (JToggleButton) arg0.getSource();
						imageFilterFiltering filter = imageFilterFiltering.valueOf(btn.getText());
						imagePanel.setFiltering(filter);
						switch(filter) {
						case Median:
						case Min:
						case Max:
							txtMaskSize.setEnabled(true);
							break;
						case None:
							txtMaskSize.setEnabled(false);
							break;
						}
					}
				}
			};
			
			JToggleButton btn;
			for(imageFilterScaling option : imageFilterScaling.values()) {
				btn = new JToggleButton(option.name());
				groupScaling.add(btn);
				btn.addActionListener(al_scaling);
				pnlScalingButtons.add(btn);
			}

			for(imageFilterFiltering filter : imageFilterFiltering.values()) {
				btn = new JToggleButton(filter.name());
				groupFiltering.add(btn);
				btn.addActionListener(al_filtering);
				pnlMaskButtons.add(btn);
			}
			
			JToggleButton btnGreaterThan = new JToggleButton(imageFilterRange.GreaterThan.name());
			JToggleButton btnLessThan = new JToggleButton(imageFilterRange.LessThan.name());
			JToggleButton btnBetween = new JToggleButton(imageFilterRange.Between.name());
			JToggleButton btnEqualTo = new JToggleButton(imageFilterRange.EqualTo.name());
			JToggleButton btnNoLimit = new JToggleButton(imageFilterRange.NoLimit.name());
			
			groupRange.add(btnGreaterThan);
			groupRange.add(btnLessThan);
			groupRange.add(btnBetween);
			groupRange.add(btnEqualTo);
			groupRange.add(btnNoLimit);

			
			btnGreaterThan.addActionListener(al_range);
			btnLessThan.addActionListener(al_range);
			btnBetween.addActionListener(al_range);
			btnEqualTo.addActionListener(al_range);
			
			btnNoLimit.setSelected(true);
			
			pnlRangeButtons.add(btnGreaterThan);
			pnlRangeButtons.add(btnLessThan);
			pnlRangeButtons.add(btnBetween);
			pnlRangeButtons.add(btnEqualTo);
			pnlRangeButtons.add(btnNoLimit);
			
			btnGreaterThan.addActionListener(new ActionListener()  {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToggleButton btn = (JToggleButton) e.getSource();
					imagePanel.setFilterRange(imageFilterRange.valueOf(btn.getText()));
					txtVal1.setEnabled(true);
					txtVal2.setEnabled(false);
					lblVal1.setText("Greater than: ");
				}
			});
			btnLessThan.addActionListener(new ActionListener()  {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToggleButton btn = (JToggleButton) e.getSource();
					imagePanel.setFilterRange(imageFilterRange.valueOf(btn.getText()));
					txtVal1.setEnabled(true);
					txtVal2.setEnabled(false);
					lblVal1.setText("Less than: ");
				}
			});
			btnBetween.addActionListener(new ActionListener()  {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToggleButton btn = (JToggleButton) e.getSource();
					imagePanel.setFilterRange(imageFilterRange.valueOf(btn.getText()));
					txtVal1.setEnabled(true);
					txtVal2.setEnabled(true);
					lblVal1.setText("Greater than: ");
					lblVal1.setText("Less than: ");
				}
			});
			btnEqualTo.addActionListener(new ActionListener()  {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToggleButton btn = (JToggleButton) e.getSource();
					imagePanel.setFilterRange(imageFilterRange.valueOf(btn.getText()));
					txtVal1.setEnabled(true);
					txtVal2.setEnabled(false);
					lblVal1.setText("Equal to: ");
				}
			});
			btnNoLimit.addActionListener(new ActionListener()  {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToggleButton btn = (JToggleButton) e.getSource();
					imagePanel.setFilterRange(imageFilterRange.valueOf(btn.getText()));
					txtVal1.setEnabled(false);
					txtVal2.setEnabled(false);
				}
			});
			
			JButton btnApply = new JButton("Apply Filter");
			btnApply.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					imagePanel.setVal1(Double.valueOf(txtVal1.getText()));
					imagePanel.setVal2(Double.valueOf(txtVal2.getText()));
					imagePanel.setMaskSize(Integer.valueOf(txtMaskSize.getText()));
					switch(imgViewPanel.getView()) {
					case BACKGROUND:
						imagePanel.updateData(calib.getBackgroundData());
						break;
					case FOURIER_TRANSFORM:
						imagePanel.loadFTImage();
						break;
					case INPUT_IMAGE:
						imagePanel.updateData(imagePanel.getInputData());
						break;
					case CALCULATED:
						selectionPanel.setAndLoadCalcData(imagePanel.xrayFile);
						return;
					case GENERAL_IMAGE:
						imagePanel.loadGeneralImage();
					case _2ND_DER_X:
						imagePanel.display2ndDers(0);
						break;
					case _2ND_DER_Y:
						imagePanel.display2ndDers(1);
						break;
					case _2ND_DER_XY:
						imagePanel.display2ndDers(2);
						break;
					default:
						break;
					}
				}
			});

			Box applyBox = Box.createHorizontalBox();
			applyBox.add(btnApply);
			applyBox.add(Box.createGlue());

			Box boxMain = Box.createVerticalBox();
			boxMain.add(applyBox);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			
			boxMain.add(pnlScaling);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			boxMain.add(pnlRange);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			boxMain.add(pnlMask);
			pnlMask.add(boxMaskSize);
			pnlMask.add(pnlImageOrClick);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			pnlRange.add(boxVal1);
			pnlRange.add(boxVal2);
			boxMain.add(Box.createVerticalStrut(VERTICAL_STRUT_HEIGHT));
			
			add(boxMain);
		}
	}
	enum FourierView {
		MODULUS,
		REAL,
		IMAGINARY,
		POWER_SPECTRUM,
		;
	}
	class FourierViewPanel extends JPanel {
		private FourierView view;
		public FourierViewPanel() {
			super();
			setBorder(new TitledBorder("Fourier View"));
			setup();
			setMaximumSize(new Dimension(EAST_WIDTH, getPreferredSize().height));
		}
		private void setup() {
			Box boxMain = Box.createVerticalBox();
			JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridLayout(0, 2));

			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object obj = e.getSource();
					if(obj instanceof JToggleButton) {
						JToggleButton tog = (JToggleButton) obj;
						switch(FourierView.valueOf(tog.getText())) {
						case IMAGINARY:
							view = FourierView.IMAGINARY;
							break;
						case MODULUS:
							view = FourierView.MODULUS;
							break;
						case POWER_SPECTRUM:
							view = FourierView.POWER_SPECTRUM;
							break;
						case REAL:
							view = FourierView.REAL;
						default:
							break;
						
						}
					}
				}
			};

			ButtonGroup bg = new ButtonGroup();
			JToggleButton tog;
			for(FourierView view : FourierView.values()) {
				tog = new JToggleButton(view.name());
				bg.add(tog);
				pnlButtons.add(tog);
				tog.addActionListener(al);
				if(view == FourierView.MODULUS)
					tog.doClick();
			}
			boxMain.add(pnlButtons);
			add(boxMain);
		}
		public FourierView getView() { return view; }
		public void setView(FourierView view) { this.view = view; }
	}
	enum CurrentView {
		BACKGROUND,
		FOURIER_TRANSFORM,
		INPUT_IMAGE,
		CALCULATED,
		GENERAL_IMAGE,
		_2ND_DER_X,
		_2ND_DER_Y,
		_2ND_DER_XY,
		;
	}
	class ImageViewPanel extends JPanel {
		private CurrentView view = CurrentView.INPUT_IMAGE;
		private Vector<JToggleButton> togView;
		public ImageViewPanel() {
			super();
			setBorder(new TitledBorder("Image View"));
			setup();
			setMaximumSize(new Dimension(EAST_WIDTH, getPreferredSize().height));
		}
		private void setup() {
			Box boxMain = Box.createVerticalBox();
			JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridLayout(0, 2));

			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object obj = e.getSource();
					if(obj instanceof JToggleButton) {
						view = CurrentView.valueOf(((JToggleButton) obj).getText());
					}
				}
			};

			ButtonGroup bg = new ButtonGroup();
			JToggleButton tog;
			togView = new Vector<JToggleButton>();
			for(CurrentView view : CurrentView.values()) {
				tog = new JToggleButton(view.name());
				bg.add(tog);
				pnlButtons.add(tog);
				tog.addActionListener(al);
				togView.add(tog);
			}
			buttonClick(view);
			boxMain.add(pnlButtons);
			add(boxMain);
		}

		public void buttonClick(CurrentView view) {
			for(JToggleButton tog : togView)
				if(tog.getText().compareTo(view.name()) == 0) {
					tog.doClick();
					return;
				}
		}
		public CurrentView getView() { return view; }
		public void setView(CurrentView view) { this.view = view; }
	}
	enum ColorFilter {grayscale, inverseGrayscale};
	
	class ColorPanel extends JPanel {
		private JToggleButton btnGrayscale, btnInverseGrayscale,
			btnPermanent, btnOverlay;
		public ColorPanel() {
			super();
			setBorder(new TitledBorder("Color Options"));
			setup();
			setMaximumSize(new Dimension(EAST_WIDTH, getPreferredSize().height));
		}
		private void setup() {
			JPanel pnlMain = new JPanel();
			pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
			JPanel pnl1 = new JPanel();
			pnl1.setLayout(new GridLayout(2, 1));
			pnl1.setBorder(BorderFactory.createLineBorder(Color.black));
			ButtonGroup colors = new ButtonGroup();
			btnGrayscale = new JToggleButton("Grayscale");
			btnInverseGrayscale = new JToggleButton("Inverse Grayscale");
			colors.add(btnGrayscale);
			colors.add(btnInverseGrayscale);
			
			btnGrayscale.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setColor(ColorFilter.grayscale);
					imagePanel.updateData(imagePanel.imageData);
					imagePanel.getParent().repaint();
				}
			});
			btnInverseGrayscale.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setColor(ColorFilter.inverseGrayscale);
					imagePanel.updateData(imagePanel.imageData);
					imagePanel.getParent().repaint();
				}
			});
			btnInverseGrayscale.setSelected(true);
			pnl1.add(btnGrayscale);
			pnl1.add(btnInverseGrayscale);
			
			ButtonGroup paintMethod = new ButtonGroup();
			btnPermanent = new JToggleButton("Permanent");
			btnPermanent.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setPermanentPaint(true);
				}
			});
			
			btnOverlay = new JToggleButton("Temporary Overlay");
			btnOverlay.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setPermanentPaint(false);
				}
			});
			
			paintMethod.add(btnPermanent);
			paintMethod.add(btnOverlay);
			JPanel pnl2 = new JPanel();
			pnl2.setLayout(new GridLayout(2, 1));
			pnl2.setBorder(BorderFactory.createLineBorder(Color.black));
			pnl2.add(btnPermanent);
			pnl2.add(btnOverlay);

			btnOverlay.setSelected(true);
			
			pnlMain.add(pnl1);
			pnlMain.add(new JLabel("    "));
			pnlMain.add(pnl2);
			add(pnlMain);
		}
		public void firePermanentClick() { btnPermanent.doClick(); }
		public void fireOverlayClick() { btnOverlay.doClick(); }
	}
	enum SelectionMode {}
	class SelectionPanel extends JPanel {
		private JTree tree;
		private JButton btnRemove;
		private JToggleButton btnRegion, btnPath, btnPixel, btnSpot;
		private ButtonGroup group;
		private DefaultMutableTreeNode spotsPerFrame, paths, regions, calcBragg, root, targetSpots;
		private DefaultMutableTreeNode calcImages, calc110, calc100, calc111, calc_other;
		private DefaultTreeModel model;
		int numSpots, numPaths, numRegions, numBragg, numTargetSpots;
		private NodeSelectionListener nodeListener;
		private boolean normalizeCalculatedImage = false;
		public SelectionPanel() {
			super();
			setBorder(new TitledBorder("Pixel Groupings"));
			setup();
			numSpots = 0;
			numPaths = 0;
			numRegions = 0;
			numBragg = 0;
			numTargetSpots = 0;
			nodeListener = new NodeSelectionListener();
			tree.addTreeSelectionListener(nodeListener);
		}
		
		private void setup() {
			group = new ButtonGroup();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			initTree();
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(tree);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			add(scroll);
			JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridLayout(0, 2));
			btnSpot = new JToggleButton("Add new spot");
			btnSpot.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setClickMode(ClickMode.spot);
				}
				
			});
			btnRegion = new JToggleButton("Add new region");
			btnRegion.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					imagePanel.setClickMode(ClickMode.region);
				}
				
			});
			btnRemove = new JButton("Remove selected");
			btnRemove.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					removeSelectedNode();
				}
				
			});
			btnPixel = new JToggleButton("Add a pixel");
			btnPixel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setClickMode(ClickMode.pixel);
				}
				
			});
			
			btnPath = new JToggleButton("Add new path");
			btnPath.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.setClickMode(ClickMode.path);
				}
				
			});
			group.add(btnRegion);
			group.add(btnSpot);
			group.add(btnPixel);
			group.add(btnPath);

			pnlButtons.add(btnRemove);
			pnlButtons.add(btnRegion);
			pnlButtons.add(btnSpot);
			pnlButtons.add(btnPixel);
			pnlButtons.add(btnPath);
			pnlButtons.setMaximumSize(new Dimension(WEST_WIDTH, 200));
			add(pnlButtons);
		}
		private void removeChildren(DefaultMutableTreeNode node) {
			while(node.getChildCount() > 0) {
				((DefaultMutableTreeNode) node.getFirstChild()).removeFromParent();
			}
			node.removeAllChildren();
			model.reload();
		}
		private void removeSelectedNode() {
			//DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			//theNode.removeFromParent();
			TreePath currentSelection = tree.getSelectionPath();
	        if (currentSelection != null) {
	            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
	                         (currentSelection.getLastPathComponent());
	            if(currentNode == root ) {
	            	removeChildren(spotsPerFrame);
	            	removeChildren(regions);
	            	removeChildren(paths);
	            	removeChildren(calcBragg);
	            	return;
	            } 
	            else if(currentNode == spotsPerFrame) { 
	            	removeChildren(spotsPerFrame);
	            	return;
	            } 
	            else if(currentNode == targetSpots) { 
	            	removeChildren(targetSpots);
	            	return;
	            }
	            else if(currentNode == regions) { 
	            	removeChildren(regions); 
	            	return;
	            }
	            else if(currentNode == paths) { 
	            	removeChildren(paths); 
	            	return;
	            }
	            else if(currentNode == calcBragg) { 
	            	removeChildren(calcBragg); 
	            	return;
	            } else if(currentNode == calcImages) {
	            	removeChildren(calc100);
	            	removeChildren(calc110);
	            	removeChildren(calc111);
	            	removeChildren(calc_other);
	            	return;
	            }
	            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(currentNode.getParent());
	            // if the current node is a Pixel then it needs to be removed from the Pixel[] arrray
	            if(currentNode.getUserObject() instanceof Pixel) {
	            	Pixel[] temp = (Pixel[]) parent.getUserObject();
	            	Pixel[] newUserObject = new Pixel[temp.length-1];
	            	Pixel cur = (Pixel) currentNode.getUserObject();
	            	int newIdx = 0;
	            	for(int i = 0; i < temp.length; i++) {
	            		if(temp[i].compareTo(cur) == 0) {
	            			temp[i] = null;
	            		} else {
		            		newUserObject[newIdx++] = (Pixel) temp[i].clone();
	            		}
	            	}
	            	parent.setUserObject(newUserObject);
	            }
	            if (parent != null) {
	                model.removeNodeFromParent(currentNode);
	                return;
	            }
	        } 
		}
		public void addPixel(Point p, double I) {
			DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			DefaultMutableTreeNode parent;
			Pixel newPixel = new Pixel(p.x, p.y, I, 0);
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newPixel);
			if(theNode == root) { return; }
			if(theNode == spotsPerFrame || theNode == regions || theNode == paths) {
				Pixel[] thePixels = new Pixel[1];
				thePixels[0] = newPixel;
				numRegions++;
				addNode(thePixels, regions);
			} else {
				parent = (DefaultMutableTreeNode) theNode.getParent();
				if(theNode.isLeaf()) {
					parent.setUserObject(addNewPixel(parent, newPixel));
					parent.add(newNode);
				} else {
					theNode.setUserObject(addNewPixel(theNode, newPixel));
					theNode.add(newNode);
					parent = theNode;
				}
			}
//			model.reload();
		}
		private Pixel[] addNewPixel(DefaultMutableTreeNode node, Pixel pix) {
			Pixel[] oldPixels = (Pixel[]) node.getUserObject();
			Pixel[] newPixels = new Pixel[oldPixels.length+1];
			int i = 0;
			for(i = 0; i < oldPixels.length; i++) {
				if(oldPixels[i].compareTo(pix) == 0) {
					return oldPixels;
				}
				newPixels[i] = (Pixel) oldPixels[i].clone();
			}
			newPixels[i] = (Pixel) pix.clone();
			return newPixels;
		}
		private void initTree() {
			root = new DefaultMutableTreeNode("");
			tree = new JTree(root);
			model = new DefaultTreeModel(root);
			tree.setRootVisible(false);
			tree.setModel(model);
			regions = new DefaultMutableTreeNode("Regions");
			spotsPerFrame = new DefaultMutableTreeNode("Spots picked per frame");
			paths = new DefaultMutableTreeNode("Paths");
			calcBragg = new DefaultMutableTreeNode("Calculated Bragg Reflections");
			targetSpots = new DefaultMutableTreeNode("Target Spots");
			calcImages = new DefaultMutableTreeNode("Calculated Xray Images");
			calc110 = new DefaultMutableTreeNode("110");
			calc100 = new DefaultMutableTreeNode("100");
			calc111 = new DefaultMutableTreeNode("111");
			calc_other = new DefaultMutableTreeNode("other");
			root.add(regions);
			root.add(spotsPerFrame);
			root.add(paths);
			root.add(calcBragg);
			root.add(targetSpots);
			root.add(calcImages);
			calcImages.add(calc100);
			calcImages.add(calc110);
			calcImages.add(calc111);
			calcImages.add(calc_other);
			model.reload();
		}
		private void addNode(Object obj, DefaultMutableTreeNode root) {
			
			DefaultMutableTreeNode temp;
			if(obj instanceof Pixel[]) {
				Pixel[] pix = (Pixel[]) obj;
				DefaultMutableTreeNode curRoot = new DefaultMutableTreeNode(pix);
				root.add(curRoot);
				for(int i = 0; i < pix.length; i++) {
					temp = new DefaultMutableTreeNode(pix[i]);
					curRoot.add(temp);
				}
			} else if(obj instanceof CalculatedXrayCollection) {
				CalculatedXrayCollection calc = (CalculatedXrayCollection) obj;
				DefaultMutableTreeNode tempRoot = new DefaultMutableTreeNode(calc);
				root.add(tempRoot);
				for(CalculatedXrayFile file : calc.getFiles()) 
					tempRoot.add(new DefaultMutableTreeNode(file));
			}
			model.setRoot(this.root);
		}
		public void addNewSpotNode(String name, Spot[] curSpots) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
			DefaultMutableTreeNode spot, pix;
			for(int i = 0; i < curSpots.length; i++) {
				spot = new DefaultMutableTreeNode(curSpots[i].getPixels());
				Pixel[] p = curSpots[i].getPixels();
				for(int j = 0; j < p.length; j++) {
					pix = new DefaultMutableTreeNode(p[j]);
					spot.add(pix);
				}
				node.add(spot);
			}
			spotsPerFrame.add(node);
		}
		public void addRegion(Pixel[] pix) {
			numRegions++;
			addNode(pix, regions);
		}
		public void addPath(Pixel[] pix) {
			numPaths++;
			addNode(pix, paths);
		}
		public void addBragg(Pixel[] pix) {
			numBragg++;
			addNode(pix, calcBragg);
		}
		public void addTargetSpot(Pixel[] pix) {
			numTargetSpots++;
			addNode(pix, targetSpots);
		}
		public void addCalculatedXrayFile(CalculatedXrayCollection calc, XrayType type) {
			DefaultMutableTreeNode root = calcImages;
			switch(type) {
			case calc100:
				root = calc100;
				break;
			case calc110:
				root = calc110;
				break;
			case calc111:
				root = calc111;
				break;
			case calc_other:
				root = calc_other;
				break;
			}
			addNode(calc, root);
		}
		public boolean loadJavaObject(File javaObject) {
			Object obj = ObjectIO.readObject(javaObject);
			int objType = 0;
			if(obj instanceof Vector<?>) {
				Vector<?> anObj = (Vector<?>) obj;
				Vector<Pixel> pix = null;
				Vector<Spot> spot = null;
				if(anObj.firstElement() instanceof Pixel) {
					pix = (Vector<Pixel>) obj;
					objType = 0;
				} else if(anObj.firstElement() instanceof Spot) {

					spot = (Vector<Spot>) obj;
					objType = 1;
				}
				switch(objType) {
				case 0:
					Pixel[] pixels = new Pixel[pix.size()];
					pixels = pix.toArray(pixels);
					for(int i = 0; i < pixels.length; i++) {
						pixels[i].swapXY();
					}
					addRegion(pixels);
					return true;
				case 1:
					Spot[] spots = new Spot[spot.size()];
					spots = spot.toArray(spots);
					Pixel[] temp;
					for(int i = 0; i < spots.length; i++) {
						temp = spots[i].getPixels();
						for(int j = 0; j < temp.length; j++) {
							temp[j].swapXY();
						}
						addTargetSpot(temp);
					}
					
					return true;
					
				}
				
			} else if(obj instanceof Spot[]) {
				Spot[] spots = (Spot[]) obj;
				for(int i = 0; i < spots.length; i++) {
					addTargetSpot(spots[i].getPixels());
				}
				return true;
			}
			return false;
		}
		public void fireNodeClicks() {
			DefaultMutableTreeNode node;
			try {
				node = (DefaultMutableTreeNode) spotsPerFrame.getLastChild();
			} catch(NoSuchElementException e) {
				return;
			}
			TreePath path = new TreePath(node);
			tree.setSelectionPath(path);
			TreeSelectionEvent arg0 = new TreeSelectionEvent(node, null, null, null, null);
			nodeListener.valueChanged(arg0);
		}
		class NodeSelectionListener implements TreeSelectionListener {

			private void getPixelsBelowSelection(DefaultMutableTreeNode node, Vector<Pixel> pix) {
				Enumeration<DefaultMutableTreeNode> e = node.children();
				Pixel[] pArr;
				Pixel p;
				while(e.hasMoreElements()) {
					DefaultMutableTreeNode cur = e.nextElement();
					try { 
						p = (Pixel) cur.getUserObject();
						pix.add(p);
					} catch(ClassCastException cce1) {
						try {
							pArr = (Pixel[]) cur.getUserObject();
							for(int i = 0; i < pArr.length; i++)
								pix.add(pArr[i]);
						} catch(ClassCastException cce2) {
							if(!cur.isLeaf())
								getPixelsBelowSelection(cur, pix);
						}
					}
				}
			}
			private DefaultMutableTreeNode getParentNode(DefaultMutableTreeNode node) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				if(parent == null || parent == root)
					return node;
				
				if(parent == regions || parent == spotsPerFrame || parent == paths || parent == calcBragg || parent == targetSpots)
					return parent;
				
				return getParentNode(parent);
			}
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if(theNode == null) return;
				if(theNode == root) {
					return;
				} 
				Object userObject = theNode.getUserObject();
				if(userObject instanceof Pixel[] || userObject instanceof Pixel) {
					Vector<Pixel> pix = new Vector<Pixel>();
					getPixelsBelowSelection(theNode, pix);
					Pixel[] pixArray = new Pixel[pix.size()];
					pixArray = pix.toArray(pixArray);
					theNode = getParentNode(theNode);
					if(pixArray != null) { 
						Color color = new Color(118, 185, 0, 180);
						if(pixArray.length > 0) {
							if(theNode == regions) {
								color = new Color(0,198,24,180);
							} else if(theNode == spotsPerFrame) {
								color = new Color(2,142,155,180);
							} else if(theNode == paths) {
								color = new Color(255,120,0, 180);
							} else if(theNode == calcBragg) {
								color = new Color(255,13,0, 180);
							} else if(theNode == targetSpots) {
								color = new Color(255,0,255, 180);
							}
						} else
							try {
								pixArray = new Pixel[] {(Pixel) userObject};
							} catch (ClassCastException e) {
								pixArray = null;
							}
						if(pixArray != null) { 
							imagePanel.paintPixels(pixArray, color);
							imagePanel.setCalculatedImage(false);
						}
					}
				} else if(userObject instanceof Pixel) {
					imagePanel.paintPixels(new Pixel[] {(Pixel) userObject}, Color.green);
					imagePanel.setCalculatedImage(false);
				} else if(userObject instanceof CalculatedXrayFile) {
					setAndLoadCalcData((CalculatedXrayFile) userObject);
				} else if(userObject instanceof CalculatedXrayCollection) {
					CalculatedXrayCollection calc = (CalculatedXrayCollection) userObject;
					setAndLoadCalcData(calc.getFiles().get(0));
				} else {
					Color color = Color.yellow;
					if(theNode == regions) {
						color = new Color(0,198,24,180);
					} else if(theNode == spotsPerFrame) {
						color = new Color(2,142,155,180);
					} else if(theNode == paths) {
						color = new Color(255,120,0, 180);
					} else if(theNode == calcBragg) {
						color = new Color(255,13,0, 180);
					} else if(theNode == targetSpots) {
						color = new Color(0,255,0, 180);
					}
					Vector<Pixel> pix = new Vector<Pixel>();
					getPixelsBelowSelection(theNode, pix);
					Pixel[] pixArray = new Pixel[pix.size()];
					pixArray = pix.toArray(pixArray);
					if(pixArray.length == 0)
						return;
					imagePanel.paintPixels(pixArray, color);
					imagePanel.setCalculatedImage(false);
				}
			}
		}
		private void setAndLoadCalcData(CalculatedXrayFile calc) {
			imagePanel.setCalculatedImage(true);
			imagePanel.xrayFile = calc;
			CalculatedXrayFile xi = imagePanel.xrayFile;
			contextPanel.txt.setText("Currently Displayed File: " + imagePanel.xrayFile.getFile().toString());
			xi.setOldFormatXrayFile(isOldFormatXrayFile);
			xi.read();
			double[][] calcData = imagePanel.getCalculatedData();
			if(normalizeCalculatedImage)
				calcData = xi.normalize(calcData);
			Point2D.Double midPoint = xi.getMidPoint();
			int maxX = (int) Math.floor(midPoint.x) * 2 + 1;
			int maxY = (int) Math.floor(midPoint.y) * 2 + 1;
			double qStep = xi.getqStep();
			coordsPanel.setImageParams(midPoint.x, midPoint.y, maxX, maxY, qStep);
			imagePanel.recalc2ndDers = false;
			imagePanel.loadImage(calcData);
			imagePanel.recalc2ndDers = true;
			imagePanel.resetZoom();
		}
		private Pixel[][] getPixelArray(DefaultMutableTreeNode root) {
			if(root.getChildCount() == 0) { return null; }
			Pixel[][] pix = new Pixel[root.getChildCount()][];
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getFirstChild();
			for(int i = 0; i < root.getChildCount(); i++) {
				pix[i] = (Pixel[]) child.getUserObject();
				child = child.getNextSibling();
			}
			return pix;
		}
		public Pixel[][] getRegions() {
			return getPixelArray(regions);
		}
		public Pixel[][] getSpotsPerFrame() {
			return getPixelArray(spotsPerFrame);
		}
		public Pixel[][] getTargetSpots() {
			return getPixelArray(targetSpots);
		}
		public Pixel[][] getPaths() {
			return getPixelArray(paths);
		}
		public Pixel[][] getBragg() {
			return getPixelArray(calcBragg);
		}
	}
	class ImageOptionsFrame extends JFrame {
		private static final long serialVersionUID = -5716105483543117710L;
		private JPanel pnlView, pnlImage;
		private JButton btnUpdateView, btnUpdateImage;
		private JTextField txtViewX, txtViewY, txtImgX, txtImgY;
		private JPanel pnlMain;
		private JComboBox<String> imageOutputOptions;
		private String[] imageOutputTypes = new String[] {"no output", "png", "gif", "jpg"};
		public ImageOptionsFrame() {
			super();
			setup();
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			pack();
		}
		private void setup() {
			pnlMain = new JPanel();
			pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
			pnlView = new JPanel();
			pnlView.setBorder(new TitledBorder("Viewable Size"));
			pnlView.setLayout(new BoxLayout(pnlView, BoxLayout.Y_AXIS));
			pnlView.setMaximumSize(new Dimension(EAST_WIDTH, 200));
			JPanel temp = new JPanel();
			temp.add(new JLabel("x "));
			txtViewX = new JTextField(5);
			txtViewX.addFocusListener(txtFieldFocusListener);
			txtViewX.setText("500");
			temp.add(txtViewX);
			pnlView.add(temp);
			
			temp = new JPanel();
			temp.add(new JLabel("y "));
			txtViewY = new JTextField(5);
			txtViewY.addFocusListener(txtFieldFocusListener);
			txtViewY.setText("500");
			temp.add(txtViewY);
			pnlView.add(temp);
			
			ActionListener listener = new UpdateListener();
			btnUpdateView = new JButton("Update");
			defaultButtonColor = btnUpdateView.getBackground();
			btnUpdateView.addActionListener(listener);
			pnlView.add(btnUpdateView);
			pnlMain.add(pnlView);
			
			pnlImage = new JPanel();
			pnlImage.setMaximumSize(new Dimension(EAST_WIDTH, 200));
			pnlImage.setBorder(new TitledBorder("Image Size"));
			pnlImage.setLayout(new BoxLayout(pnlImage, BoxLayout.Y_AXIS));
			
			temp = new JPanel();
			temp.add(new JLabel("x "));
			txtImgX = new JTextField(5);
			txtImgX.addFocusListener(txtFieldFocusListener);
			temp.add(txtImgX);
			pnlImage.add(temp);
			
			temp = new JPanel();
			temp.add(new JLabel("y "));
			txtImgY = new JTextField(5);
			txtImgY.addFocusListener(txtFieldFocusListener);
			temp.add(txtImgY);
			pnlImage.add(temp);
			
			btnUpdateImage = new JButton("Update");
			defaultButtonColor = btnUpdateImage.getBackground();
			btnUpdateImage.addActionListener(listener);
			pnlImage.add(btnUpdateImage);
			
			Box boxH1 = Box.createHorizontalBox();
			imageOutputOptions = new JComboBox<String>(imageOutputTypes);
			
			boxH1.add(new JLabel("OutputImagesToFile?"));
			boxH1.add(Box.createHorizontalStrut(5));
			boxH1.add(imageOutputOptions);
			pnlImage.add(boxH1);
			pnlMain.add(pnlImage);
			add(pnlMain);
		}
		public String getImageOutputType() { return (String) imageOutputOptions.getSelectedItem(); }
		class UpdateListener implements ActionListener {
			private String numberParseError = "Can't parse the number in the highlighted box.";
			private String numberParseErrorTitle = "Number Format Error";
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JButton temp = (JButton) arg0.getSource();
				if(temp == btnUpdateView) {
					int x = 0;
					try {
						x = Integer.valueOf(txtViewX.getText());
					} catch (NumberFormatException e) {
						showError(numberParseError, numberParseErrorTitle, txtViewX);
						return;
					}
					txtViewX.setBackground(defaultButtonColor);
					int y = 0;
					try {
						y = Integer.valueOf(txtViewY.getText());
					} catch (NumberFormatException e) {
						showError(numberParseError, numberParseErrorTitle, txtViewY);
						return;
					}
					txtViewY.setBackground(defaultButtonColor);
					imagePanel.setDisplayDim(x, y);
				}
				else if(temp == btnUpdateImage) {
					int x = 0;
					try {
						x = Integer.valueOf(txtImgX.getText());
					} catch (NumberFormatException e) {
						showError(numberParseError, numberParseErrorTitle, txtImgX);
						return;
					}
					txtImgX.setBackground(defaultButtonColor);
					int y = 0;
					try {
						y = Integer.valueOf(txtImgY.getText());
					} catch (NumberFormatException e) {
						showError(numberParseError, numberParseErrorTitle, txtImgY);
						return;
					}
					txtImgY.setBackground(defaultButtonColor);
					imagePanel.setImageDim(x, y);
				}
			}
			private void showError(String errMsg, String title, JComponent component) {
				JOptionPane.showMessageDialog(component, errMsg, title, JOptionPane.ERROR_MESSAGE);
				component.setBackground(Color.yellow);
			}
			
		}
	}
	enum shapePathRegion { shape, path, region};
	class SelectionInterpreter implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			switch(shapePathOrRegion((JButton) arg0.getSource())) {
			case region:
				
				break;
			}
		}
		
		private shapePathRegion shapePathOrRegion(JButton btn) {
			// test to see if the button comes from the shape enum
			if(shapeOptions.valueOf(btn.toString()) != null) { return shapePathRegion.region; }
			return null;
		}
		
	}
	class ContextPanel extends JPanel {
		private JTextArea txt;
		public ContextPanel() {
			super();
			txt = new JTextArea();
			txt.setEditable(false);
			txt.setFocusable(true);
			txt.addKeyListener(new ImageKeyListener());
			add(txt);
		}
	}
	
	class ShapesPanel extends JPanel {
		private shapeOptions selected;
		private ButtonGroup group;
		private JPanel shapesPanel;
		private JPanel pathPanel;
		public ShapesPanel() {
			super();
			setBorder(new TitledBorder("Shape Selection"));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setMaximumSize(new Dimension(WEST_WIDTH, 400));
			setupShapesPanel();
			setupPathPanel();
		}

		private void setupPathPanel() {
			pathPanel = new JPanel();
			pathPanel.setLayout(new GridLayout(0, 4));
			group = new ButtonGroup();
			JButton btnClose = new JButton("Close Path");
			btnClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					imagePanel.imageClickListener.path.closePath();
					imagePanel.setShape(imagePanel.imageClickListener.path);
					imagePanel.getParent().repaint();
				}
				
			});
			pathPanel.add(btnClose);
			JButton btnSave = new JButton("Save Path");
			btnSave.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.imageClickListener.submitPath();
					imagePanel.setShape(imagePanel.imageClickListener.path);
					Pixel[] p = imagePanel.getPixelsUnderLine();
					for(Pixel pix : p) {
						Point point = new Point(pix.getX(), pix.getY());
						double val = 0;
						double[][] data = imagePanel.imageData;
						switch(coordsPanel.coords) {
						case CALCULATED_SPACE:
							imagePanel.setCalculatedImage(true);
							try {
								val = imagePanel.getQ(point).length();
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						case REAL_SPACE:
							val = coordsPanel.pixToDist(point);
							break;
						case RECIPROCAL_SPACE:
							val = coordsPanel.pixToQ(point);
							break;
						default:
							break;
						
						}
						
						pix.setDist(val);
						pix.setPhi(coordsPanel.getPhi(point));
					}
					selectionPanel.addPath(p);
				}
			});
			pathPanel.add(btnSave);
			JButton btnClear = new JButton("Clear Path");
			btnClear.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					imagePanel.imageClickListener.clearPath();
				}
			});
			pathPanel.add(btnClear);
			
			JButton btnClickCenter = new JButton("Center Click");
			btnClickCenter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Point2D.Double center_d = calib.getCenter();
					Point center = new Point((int) Math.rint(center_d.x), (int) Math.rint(center_d.y));
					center = imagePanel.imageCoordsToViewCoords(center);
					imagePanel.imageClickListener.click(center);
				}
			});
			pathPanel.add(btnClickCenter);			
			
			add(pathPanel);
			pathPanel.setVisible(false);
		}
		private void setupShapesPanel() {
			shapesPanel = new JPanel();
			shapesPanel.setLayout(new GridLayout(0, 3));
			JToggleButton btn;
			group = new ButtonGroup();
			ActionListener shapeToggleListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					selected = shapeOptions.valueOf(((JToggleButton) arg0.getSource()).getText());
					imagePanel.setClickMode(ClickMode.region);
				}
			};
			for(shapeOptions temp : shapeOptions.values()) {
				btn = new JToggleButton(temp.toString());
				btn.addActionListener(shapeToggleListener);
				group.add(btn);
				shapesPanel.add(btn);
			}
			JButton btnAccept = new JButton("Save Region");
			btnAccept.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Pixel[] p = imagePanel.getPixelsInsideShape();
					selectionPanel.addRegion(p);
				}
				
			});
			shapesPanel.add(btnAccept);
			add(shapesPanel);
		}
	}
	class CalibrationPanel extends JPanel {
		private static final long serialVersionUID = -8252469973981877156L;
		private JTextField txtWavelength, txtx0, txty0, txtPixSize, txtSampleToDetector, 
			txtSynchrotron, txtDate, txtCalibrant, txtNotes, txtBackgroundScale;
		private double wavelength, x0, y0;
		/** in mm */
		private double pixSize;
		/** in mm */
		private double sampleToDetector;
		private String synchrotron, date, calibrant, notes;
		private JPanel pnlMain;
		private boolean hasCalibration = false;
		private CalibrationFilesFrame calibFrame;
		private double[][] backgroundData;
		private double backgroundScale;
		public CalibrationPanel() {
			super();
			calibFrame = new CalibrationFilesFrame();
			setBorder(new TitledBorder("Calibration"));
			setupPanel();
			setMaximumSize(new Dimension(EAST_WIDTH, getPreferredSize().height));
		}
		
		
		public void updateCalibration() {
			txtWavelength.setText(curCalib.getParam(Calibration.parameters.wavelength) + "");
			txtx0.setText(curCalib.getParam(Calibration.parameters.x) + "");
			txty0.setText(curCalib.getParam(Calibration.parameters.y) + "");
			txtPixSize.setText(curCalib.getParam(Calibration.parameters.pixel_size) + "");
			txtSampleToDetector.setText(curCalib.getParam(Calibration.parameters.distance) + "");
			txtSynchrotron.setText(curCalib.getParam(Calibration.parameters.synchrotron) + "");
			txtDate.setText(curCalib.getParam(Calibration.parameters.date) + "");
			txtCalibrant.setText(curCalib.getParam(Calibration.parameters.calibrant) + "");
			txtNotes.setText(curCalib.getParam(Calibration.parameters.notes) + "");
			txtBackgroundScale.setText(curCalib.getParam(Calibration.parameters.backgroundScale) + "");
			parse();
		}
		// panel in the UI
		private void setupPanel() {
			txtWavelength = new JTextField(6);
			txtx0 = new JTextField(6);
			txty0 = new JTextField(6);
			txtPixSize = new JTextField(6);
			txtSampleToDetector = new JTextField(6);
			txtSynchrotron = new JTextField(6);
			txtDate = new JTextField(6);
			txtCalibrant = new JTextField(6);
			txtNotes = new JTextField(6);
			txtBackgroundScale = new JTextField(6);
			
			txtWavelength.addFocusListener(txtFieldFocusListener);
			txtx0.addFocusListener(txtFieldFocusListener);
			txty0.addFocusListener(txtFieldFocusListener);
			txtPixSize.addFocusListener(txtFieldFocusListener);
			txtSampleToDetector.addFocusListener(txtFieldFocusListener);
			txtSynchrotron.addFocusListener(txtFieldFocusListener);
			txtDate.addFocusListener(txtFieldFocusListener);
			txtCalibrant.addFocusListener(txtFieldFocusListener);
			txtNotes.addFocusListener(txtFieldFocusListener);
			txtBackgroundScale.addFocusListener(txtFieldFocusListener);
			
			pnlMain = new JPanel();
			pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
			addToMain("Synchrotron: ", txtSynchrotron);
			addToMain("Date: ", txtDate);
			addToMain("Calibrant: ", txtCalibrant);
			addToMain("Notes: ", txtNotes);
			addToMain("Wavelength: ", txtWavelength);
			addToMain("x0: ", txtx0);
			addToMain("y0: ", txty0);
			addToMain("Pixel Size (um): ", txtPixSize);
			addToMain("Sample To Detector (mm): ", txtSampleToDetector);
			add(pnlMain);
			JPanel pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridLayout(0, 2));
			
			JButton btnSubmit = new JButton("Submit");
			btnSubmit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					parse();
				}
			});
			JButton btnSaveNewCalib = new JButton("Save as New Calibration");
			btnSaveNewCalib.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					parse();
					writeNewCalibrationFile();
				}
			});
			JButton btnLoadCalib = new JButton("Load Calibration File");
			btnLoadCalib.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					calibFrame.clickToShow();
				}
			});
			JButton btnPaintCenter = new JButton("Mark calibrated center.");
			btnPaintCenter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Pixel[] pix = new Pixel[5];
					double xD = Double.valueOf(""+curCalib.getParam(Calibration.parameters.x));
					double yD = Double.valueOf(""+curCalib.getParam(Calibration.parameters.y));
					int x = (int) xD;
					int y = (int) yD;
					pix[0] = new Pixel(x, y, 1);
					pix[1] = new Pixel(x-1, y, 1);
					pix[2] = new Pixel(x+1, y, 1);
					pix[3] = new Pixel(x, y-1, 1);
					pix[4] = new Pixel(x, y+1, 1);
					imagePanel.paintPixels(pix, Color.black);
				}
			});
			JPanel pnlBackgroundButtons = new JPanel();
			pnlBackgroundButtons.setLayout(new GridLayout(0, 2));
			pnlBackgroundButtons.setBorder(BorderFactory.createTitledBorder("Subtract Background?"));
			final JToggleButton btnBackgroundYes = new JToggleButton("Yes");
			final JToggleButton btnBackgroundNo = new JToggleButton("No");
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(btnBackgroundYes);
			bg.add(btnBackgroundNo);
			
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object src = e.getSource();
					if(src instanceof JToggleButton) {
						if(src == btnBackgroundYes) {
							imagePanel.setSubtractingBackground(true);
							txtBackgroundScale.setEnabled(true);
							getBackgroundFile();
						} else if(src == btnBackgroundNo) {
							imagePanel.setSubtractingBackground(false);
							txtBackgroundScale.setEnabled(false);
						}
					}
					
				}
			};
			btnBackgroundYes.addActionListener(al);
			btnBackgroundNo.addActionListener(al);
			btnBackgroundNo.doClick();
			
			pnlButtons.add(btnSubmit);
			pnlButtons.add(btnSaveNewCalib);
			pnlButtons.add(btnLoadCalib);
			pnlButtons.add(btnPaintCenter);
			pnlMain.add(pnlButtons);

			pnlBackgroundButtons.add(btnBackgroundYes);
			pnlBackgroundButtons.add(btnBackgroundNo);
			pnlBackgroundButtons.add(new JLabel("Background Scaling Factor: "));
			pnlBackgroundButtons.add(txtBackgroundScale);
			pnlMain.add(pnlBackgroundButtons);
		}
		private void getBackgroundFile() {
			
			String oldTitle2 = chooserMultiFile.getDialogTitle();
			chooserMultiFile.setDialogTitle("Select background");
			switch(chooserMultiFile.showOpenDialog(null)) {
			case JFileChooser.APPROVE_OPTION:
				File background = chooserMultiFile.getSelectedFile();
				imagePanel.setBackgroundFile(background);
				ImagePropertiesViewer imgPropViewer = (ImagePropertiesViewer) chooserMultiFile.getAccessory();
				ImageProperties imgProp = imgPropViewer.parseSelections();
				ImageFile backgroundType = new BIN(imgProp);
				readBackgroundFile(background, backgroundType);
				String returnVal = JOptionPane.showInputDialog("Background subtraction scale. i.e. image collection was 2 seconds, background collection was 10 seconds, scale is 2/10=0.2");
				double bgScale = Double.valueOf(returnVal);
				curCalib.setBackgroundScale(bgScale);
				calib.updateCalibration();
				break;
			case JFileChooser.CANCEL_OPTION:
				imagePanel.setSubtractingBackground(false);
				break;
			}
			chooserMultiFile.setDialogTitle(oldTitle2);
		}
		private void readBackgroundFile(File f, ImageFile imgFile) {
			backgroundData = imgFile.readFile(f);
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for(int i = 0; i < backgroundData.length; i++) {
				for(int j = 0; j < backgroundData[i].length; j++) {
					if(min > backgroundData[i][i]) { min = backgroundData[i][j]; }
					if(max < backgroundData[i][j]) { max = backgroundData[i][j]; }
				}
			}
			
		}
		private void writeNewCalibrationFile() {
			calibFiles.add(new Calibration(synchrotron, date, calibrant, notes, x0, y0, 
					(int) pixSize, sampleToDetector, wavelength, backgroundScale));
			calibFrame.refreshTable();
		}
		private void parse() {
			hasCalibration = true;
			try {
				wavelength = Double.valueOf(txtWavelength.getText());	
				txtWavelength.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtWavelength.setBackground(Color.yellow);
				hasCalibration = false;
			}
			

			try {
				x0 = Double.valueOf(txtx0.getText());	
				txtx0.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtx0.setBackground(Color.yellow);
				hasCalibration = false;
			}
			

			try {
				y0 = Double.valueOf(txty0.getText());	
				txty0.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txty0.setBackground(Color.yellow);
				hasCalibration = false;
			}
			

			try {
				pixSize = Double.valueOf(txtPixSize.getText());	
				txtPixSize.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtPixSize.setBackground(Color.yellow);
				hasCalibration = false;
			}
			

			try {
				sampleToDetector = Double.valueOf(txtSampleToDetector.getText());	
				txtSampleToDetector.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtSampleToDetector.setBackground(Color.yellow);
				hasCalibration = false;
			}
			
			try {
				date = txtDate.getText();	
				txtDate.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtDate.setBackground(Color.yellow);
				hasCalibration = false;
			}
			
			try {
				synchrotron = txtSynchrotron.getText();	
				txtSynchrotron.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtSynchrotron.setBackground(Color.yellow);
				hasCalibration = false;
			}
			
			try {
				calibrant = txtCalibrant.getText();	
				txtCalibrant.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtCalibrant.setBackground(Color.yellow);
				hasCalibration = false;
			}
			
			try {
				notes = txtNotes.getText();	
				txtNotes.setBackground(Color.white);
			} catch(NumberFormatException nfe) {
				txtNotes.setBackground(Color.yellow);
				hasCalibration = false;
			}
			
			try {
				if(imagePanel.isSubtractingBackground()) { 
					backgroundScale = Double.valueOf(txtBackgroundScale.getText());	
					curCalib.setBackgroundScale(backgroundScale);
					txtBackgroundScale.setBackground(Color.white);
				}
			} catch(NumberFormatException nfe) {
				txtBackgroundScale.setBackground(Color.yellow);
				hasCalibration = false;
			}
		}
		private void addToMain(String lbl, JTextField txt) {
			JPanel pnl = getPanel();
			pnl.add(new JLabel(lbl));
			pnl.add(txt);
			pnlMain.add(pnl);
		}
		private JPanel getPanel() {
			JPanel pnl = new JPanel();
			pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
			return pnl;
		}
		/**
		 * 
		 * @param x
		 * @param y
		 * @return {q, phi}
		 */
		private double[] coordsToQAndPhi(double x, double y) {
			if(curCalib != null) 
				return curCalib.coordsToQAndPhi(x, y);

			return Calibration.coordsToQAndPhi(x, y, pixSize, x0, y0, wavelength, sampleToDetector);
		}
		public double[][] getBackgroundData() { return backgroundData; }
		public void setBackgroundData(double[][] backgroundData) { this.backgroundData = backgroundData; }
		public Point2D.Double getCenter() {
			double x = x0;
			double y = y0;
			return new Point2D.Double(x, y);
		}
	}
	class CalibrationFilesFrame extends JFrame {
		private static final long serialVersionUID = 4395858603758801865L;
		private JTable table;
		private JPanel pnlWest, pnlSouth, pnlEast, pnlNorth;
		private JScrollPane scrollPane;
		// frame that pops up
		private CalibrationFilesFrame() {
			super();
			setupBorderPanels();
			setupTablePanel();
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			setVisible(false);
			setSize(new Dimension(1000, 500));
			pack();
		}
		private void hideWindow() {
			setVisible(false);
		}
		private void setupBorderPanels() {
			setupSouthPanel();
			
			pnlWest = new JPanel();
			pnlEast = new JPanel();
			pnlNorth = new JPanel();
			add(pnlWest, BorderLayout.WEST);
			add(pnlEast, BorderLayout.EAST);
			add(pnlNorth, BorderLayout.NORTH);
			add(pnlSouth, BorderLayout.SOUTH);
		}
		private void setupSouthPanel() {
			pnlSouth = new JPanel();
			pnlSouth.setLayout(new BoxLayout(pnlSouth, BoxLayout.X_AXIS));
			
			JButton btnSave = new JButton("Save changes to calibration files.");
			JButton btnHide= new JButton("Hide this window");
			JButton btnNew = new JButton("Add new calibration file");
			JButton btnDelete = new JButton("Delete calibration file");
			
			btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("Button clicked: Save changes to calibration files.");
				}
			});
			
			btnHide.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					hideWindow();
				}
			});
			
			btnNew.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("Button clicked: Add new calibration file.");
					
				}
			});
			
			btnDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					calibFiles.remove(row-1);
					refreshTable();
				}
			});
			Box box = Box.createHorizontalBox();
			box.add(btnSave);
			box.add(Box.createHorizontalStrut(10));
			box.add(btnHide);
			box.add(Box.createHorizontalStrut(10));
			box.add(btnNew);
			box.add(Box.createHorizontalStrut(10));
			box.add(btnDelete);
			box.add(Box.createHorizontalGlue());
			pnlSouth.add(box);
		}
		public void clickToShow() {
			System.out.println("show()");
			refreshTable();
			setVisible(true);
		}
		private JToggleButton[] getButtonGroup() {
			ButtonGroup group = new ButtonGroup();
			JToggleButton[] btns = new JToggleButton[calibFiles.size()];
			for(int i = 0; i < btns.length; i++) {
				final JToggleButton btn = new JToggleButton("" + i);
				btns[i] = btn;
				btns[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int idx = Integer.valueOf(btn.getText());
						curCalib = calibFiles.get(idx);
						calib.updateCalibration();
					}
				});
				group.add(btn);
			}
			return btns;
		}
		private void refreshButtonPanel() {
			pnlNorth.removeAll();
			pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.X_AXIS));
			pnlNorth.add(new JLabel("Select which calibration file to load: "));
			pnlNorth.add(new JLabel("     "));
			JToggleButton[] btns = getButtonGroup();
			for(int i = 0; i < btns.length; i++) {
				pnlNorth.add(btns[i]);
			}
		}
		public void refreshTable() {
			loadCalibrationFiles();
			refreshButtonPanel();
			if(scrollPane != null) { remove(scrollPane); }
			
			Vector<String> columnNames = new Vector<String>();
			columnNames.add("File #");
			Calibration.parameters[] p = Calibration.parameters.values();
			for(int i = 0; i < p.length; i++) {
				columnNames.add(p[i].toString());
			}
			Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();
			Vector<Object> row;
			Calibration cur;
			for(int i = 0; i < calibFiles.size(); i++) {
				cur = calibFiles.get(i);
				row = new Vector<Object>();
				row.add(i);
				for(int j = 0; j < p.length; j++) {
					row.add(cur.getParam(p[j]));
				}
				rowData.add(row);
			}
			
			table = new JTable(rowData, columnNames);
			TableColumn column = null;
			for(int i = 0; i < columnNames.size(); i++) {
				column = table.getColumnModel().getColumn(i);
				column.setMaxWidth(column.getPreferredWidth());
			}
			scrollPane = new JScrollPane(table);
			add(scrollPane, BorderLayout.CENTER);
			pack();
			repaint();
		}
		private void setupTablePanel() {
			refreshTable();
			
			pack();
		}
	}
	class ActivePixelDeterminationFrame extends JFrame {
		private JToggleButton btnThreshRejection, btnThreshSelection,
			btnGreaterRejection, btnGreaterSelection,
			btnLessRejection, btnLessSelection,
			btnBetweenRejection, btnBetweenSelection,
			btnEqualRejection, btnEqualSelection,
			btnNotEqualRejection, btnNotEqualSelection;

		private JCheckBox btnCoalesce, btnPickAll, btnThresh, btnGreater, btnLess, btnBetween, btnEqual, btnNotEqual;
		
		private MyJTextField txtThresh, txtGreater, txtLess, 
			txtBetweenMin, txtBetweenMax, txtEqual, txtNotEqual, txtCoalesce;
		
		private JButton btnRunSpotpicking;
		
		private int txtWidth = 5;
		
		private Spot[] spots;
		
		private JComboBox<String> boxImagesOutput;
		
		private boolean isSelectingTargetSpots = true;
		
		public ActivePixelDeterminationFrame() {
			setupButtons();
			setupTxtFields();
			setupGUI();
			
			btnThresh.doClick();
			btnGreater.doClick();
			btnLess.doClick();
			btnBetween.doClick(); 
			btnEqual.doClick();
			btnNotEqual.doClick();
//			btnPickAll.doClick();
			
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			pack();
			setVisible(false);
		}
		public void setupButtons() {
			String selection = "==";
			String rejection = "!=";
			btnThreshSelection = new JToggleButton(selection);
			btnThreshRejection = new JToggleButton(rejection);
			
			btnGreaterSelection = new JToggleButton(selection);
			btnGreaterRejection = new JToggleButton(rejection);
			
			btnLessSelection = new JToggleButton(selection);
			btnLessRejection = new JToggleButton(rejection);
			
			btnBetweenSelection = new JToggleButton(selection);
			btnBetweenRejection = new JToggleButton(rejection);
			
			btnEqualSelection = new JToggleButton(selection);
			btnEqualRejection = new JToggleButton(rejection);
			
			btnNotEqualSelection = new JToggleButton(selection);
			btnNotEqualRejection = new JToggleButton(rejection);
			
			btnThreshSelection.setSelected(true);
			btnGreaterSelection.setSelected(true);
			btnLessSelection.setSelected(true);
			btnBetweenSelection.setSelected(true);
			btnEqualSelection.setSelected(true);
			btnNotEqualSelection.setSelected(true);
			
			ButtonGroup thresh = new ButtonGroup();
			ButtonGroup greater = new ButtonGroup();
			ButtonGroup less = new ButtonGroup();
			ButtonGroup between = new ButtonGroup();
			ButtonGroup equal = new ButtonGroup();
			ButtonGroup notEqual = new ButtonGroup();
			
			thresh.add(btnThreshSelection);
			thresh.add(btnThreshRejection);
			
			greater.add(btnGreaterSelection);
			greater.add(btnGreaterRejection);
			
			less.add(btnLessSelection);
			less.add(btnLessRejection);
			
			between.add(btnBetweenSelection);
			between.add(btnBetweenRejection);
			
			equal.add(btnEqualSelection);
			equal.add(btnEqualRejection);
			
			notEqual.add(btnNotEqualSelection);
			notEqual.add(btnNotEqualRejection);
			
			btnThresh = new JCheckBox();
			btnPickAll = new JCheckBox();
			btnGreater = new JCheckBox();
			btnLess = new JCheckBox();
			btnBetween = new JCheckBox(); 
			btnEqual = new JCheckBox();
			btnNotEqual = new JCheckBox();
			btnCoalesce = new JCheckBox();
			
			btnThresh.setSelected(false);
			btnPickAll.setSelected(false);
			btnGreater.setSelected(true);
			btnLess.setSelected(true);
			btnBetween.setSelected(true); 
			btnEqual.setSelected(true);
			btnNotEqual.setSelected(true);
			btnCoalesce.setSelected(true);
			
			
			btnThresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnThresh.isSelected()) {
						txtThresh.setEnabled(true);
						btnThreshSelection.setEnabled(true);
						btnThreshRejection.setEnabled(true);
					} else {
						txtThresh.setEnabled(false);
						btnThreshSelection.setEnabled(false);
						btnThreshRejection.setEnabled(false);
					}
				}
			});
			
			btnGreater.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnGreater.isSelected()) {
						txtGreater.setEnabled(true);
						btnGreaterSelection.setEnabled(true);
						btnGreaterRejection.setEnabled(true);
					} else {
						txtGreater.setEnabled(false);
						btnGreaterSelection.setEnabled(false);
						btnGreaterRejection.setEnabled(false);
					}
				}
			});
			
			btnLess.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnLess.isSelected()) {
						txtLess.setEnabled(true);
						btnLessSelection.setEnabled(true);
						btnLessRejection.setEnabled(true);
					} else {
						txtLess.setEnabled(false);
						btnLessSelection.setEnabled(false);
						btnLessRejection.setEnabled(false);
					}
				}
			});
			
			btnBetween.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnBetween.isSelected()) {
						txtBetweenMin.setEnabled(true);
						txtBetweenMax.setEnabled(true);
						btnBetweenSelection.setEnabled(true);
						btnBetweenRejection.setEnabled(true);
					} else {
						txtBetweenMin.setEnabled(false);
						txtBetweenMax.setEnabled(false);
						btnBetweenSelection.setEnabled(false);
						btnBetweenRejection.setEnabled(false);
					}
				}
			});
			
			btnEqual.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnEqual.isSelected()) {
						txtEqual.setEnabled(true);
						btnEqualSelection.setEnabled(true);
						btnEqualRejection.setEnabled(true);
					} else {
						txtEqual.setEnabled(false);
						btnEqualSelection.setEnabled(false);
						btnEqualRejection.setEnabled(false);
					}
				}
			});
			
			btnNotEqual.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnNotEqual.isSelected()) {
						txtNotEqual.setEnabled(true);
						btnNotEqualSelection.setEnabled(true);
						btnNotEqualRejection.setEnabled(true);
					} else {
						txtNotEqual.setEnabled(false);
						btnNotEqualSelection.setEnabled(false);
						btnNotEqualRejection.setEnabled(false);
					}
				}
			});
			
			btnCoalesce.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(btnCoalesce.isSelected()) {
						txtCoalesce.setEnabled(true);
					} else {
						txtCoalesce.setEnabled(false);
					}
				}
			});
		}
		public void setupTxtFields() {
			txtThresh = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			txtGreater = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			txtLess = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			txtBetweenMin = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			txtBetweenMax = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			txtEqual = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			txtNotEqual = new MyJTextField(MyJTextField.txtFieldType.Double, txtWidth);
			
			MyJTextField.txtFieldCondition condition = MyJTextField.txtFieldCondition.greaterThan;
			condition.setVal1(0);
			txtCoalesce = new MyJTextField(MyJTextField.txtFieldType.Integer, txtWidth, condition);
			txtCoalesce.setText("" + 5);
		}
		public void setupGUI() {
			Box boxMain = Box.createVerticalBox();

			Box boxH1 = Box.createHorizontalBox();
			Box boxH2 = Box.createHorizontalBox();
			Box boxH3 = Box.createHorizontalBox();
			Box boxH4 = Box.createHorizontalBox();
			Box boxH5 = Box.createHorizontalBox();
			Box boxH6 = Box.createHorizontalBox();
			Box boxH7 = Box.createHorizontalBox();
			Box boxH8 = Box.createHorizontalBox();
			Box boxH9 = Box.createHorizontalBox();
			
			int vertStrutHeight = 10;
			int horizontalStrutWidth = 5;
			
			JLabel lblThresh = new JLabel("Threshold");
			JLabel lblGreater = new JLabel("Greater than");
			JLabel lblLess = new JLabel("Less than");
			JLabel lblBetween = new JLabel("Between");
			JLabel lblEqual = new JLabel("Equal to");
			JLabel lblNotEqual = new JLabel("Cannot be equal to");
			JLabel lblCoalesce = new JLabel("Coalesce active pixels into spots?");
			JLabel lblCoalesce2 = new JLabel("Number of pixels per spot: ");
			JLabel lblPickAll = new JLabel("Spot pick all images?");
			JLabel lblOutputImages = new JLabel("Output images to file?");
			
			boxH2.add(btnThresh);
			boxH2.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH2.add(lblThresh);
			boxH2.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH2.add(txtThresh);
			boxH2.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH2.add(btnThreshRejection);
			boxH2.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH2.add(btnThreshSelection);
			boxH2.add(Box.createHorizontalGlue());
			
			boxH3.add(btnGreater);
			boxH3.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH3.add(lblGreater);
			boxH3.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH3.add(txtGreater);
			boxH3.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH3.add(btnGreaterRejection);
			boxH3.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH3.add(btnGreaterSelection);
			boxH3.add(Box.createHorizontalGlue());
			
			boxH4.add(btnLess);
			boxH4.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH4.add(lblLess);
			boxH4.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH4.add(txtLess);
			boxH4.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH4.add(btnLessRejection);
			boxH4.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH4.add(btnLessSelection);
			boxH4.add(Box.createHorizontalGlue());
			
			boxH5.add(btnBetween);
			boxH5.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH5.add(lblBetween);
			boxH5.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH5.add(txtBetweenMin);
			boxH5.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH5.add(new JLabel("and"));
			boxH5.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH5.add(txtBetweenMax);
			boxH5.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH5.add(btnBetweenRejection);
			boxH5.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH5.add(btnBetweenSelection);
			boxH5.add(Box.createHorizontalGlue());
			
			boxH6.add(btnEqual);
			boxH6.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH6.add(lblEqual);
			boxH6.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH6.add(txtEqual);
			boxH6.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH6.add(btnEqualRejection);
			boxH6.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH6.add(btnEqualSelection);
			boxH6.add(Box.createHorizontalGlue());

			boxH7.add(btnNotEqual);
			boxH7.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH7.add(lblNotEqual);
			boxH7.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH7.add(txtNotEqual);
			boxH7.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH7.add(btnNotEqualRejection);
			boxH7.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH7.add(btnNotEqualSelection);
			boxH7.add(Box.createHorizontalGlue());
			
			Box boxV1 = Box.createVerticalBox();
			Box boxH81 = Box.createHorizontalBox();
			Box boxH82 = Box.createHorizontalBox();
			
			boxH81.add(btnCoalesce);
			boxH81.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH81.add(lblCoalesce);
			boxH81.add(Box.createHorizontalGlue());

			boxH82.add(Box.createHorizontalStrut(10*horizontalStrutWidth));
			boxH82.add(lblCoalesce2);
			boxH82.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH82.add(txtCoalesce);
			boxH82.add(Box.createHorizontalGlue());

			boxV1.add(boxH81);
			boxV1.add(boxH82);
			
			boxH8.add(boxV1);
			
			boxH9.add(btnPickAll);
			boxH9.add(Box.createHorizontalStrut(horizontalStrutWidth));
			boxH9.add(lblPickAll);
			boxH9.add(Box.createHorizontalGlue());

			boxMain.add(boxH1);
			boxMain.add(boxH8);
			boxMain.add(boxH9);
			boxMain.add(boxH2);
			boxMain.add(boxH3);
			boxMain.add(boxH4);
			boxMain.add(boxH5);
			boxMain.add(boxH6);
			boxMain.add(boxH7);
			
			btnRunSpotpicking = new JButton("Run spotpicking");
			btnRunSpotpicking.setFont(new Font(btnRunSpotpicking.getFont().getName(), Font.BOLD, 16));
			btnRunSpotpicking.setForeground(Color.BLUE);
			btnRunSpotpicking.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Vector<Vector<Pixel>> activePix = imagePanel.getActivePixels();
					if(activePix.get(0) != null && activePix.size() > 0) {
						if(getCoalesceIntoPixels()) {
							spots = imagePanel.spotPick(activePix);
							if(isSelectingTargetSpots)
								for(int i = 0; i < spots.length; i++) {
									selectionPanel.addTargetSpot(spots[i].getPixels());
								}
							else 
								selectionPanel.addNewSpotNode(imagePanel.filesInFolder[imagePanel.curFileIdx].getName(), spots);
						} else {
							for(int i = 0; i < activePix.size(); i++) {
								Vector<Pixel> vec = activePix.get(i);
								Pixel[] arr = new Pixel[vec.size()];
								arr = vec.toArray(arr);
								selectionPanel.addRegion(arr);
							}
						}
					}
				}
			});
			
			boxMain.add(btnRunSpotpicking);
			
			boxMain.add(Box.createVerticalGlue());
			add(boxMain, BorderLayout.CENTER);
		}
		public int getCoalesceVal() { return Integer.valueOf(txtCoalesce.getText()); }
		public double getThresholdVal() { return Double.valueOf(txtThresh.getText()); }
		public double getLessThanVal() { return Double.valueOf(txtLess.getText()); }
		public double getGreaterThanVal() { return Double.valueOf(txtGreater.getText()); }
		public double getEqualToVal() { return Double.valueOf(txtEqual.getText()); }
		public double getNotEqualToVal() { return Double.valueOf(txtNotEqual.getText()); }
		public double[] getBetweenVals() { 
			double[] arr = new double[] {Double.valueOf(txtBetweenMin.getText()), Double.valueOf(txtBetweenMax.getText()) }; 
			return  arr;
		}
		
		public void setCoalesceVal(int val) { txtCoalesce.setText(val + ""); }
		public void setThresholdVal(double val) { txtThresh.setText(val + ""); };
		public void setGreaterThanVal(double val) { txtGreater.setText(val + ""); }
		public void setLessThanVal(double val) { txtLess.setText(val + ""); }
		public void setEqualToVal(double val) { txtEqual.setText(val + ""); }
		public void setBetweenVals(double valMin, double valMax) { 
			txtBetweenMin.setText(valMin + ""); 
			txtBetweenMax.setText(valMax + "");
		}
		public void setNotEqualToVal(double val) { txtNotEqual.setText(val + ""); }
		
		/**
		 * 
		 * @return <br>arr[0] = 2nd derivative value is a criteria (true) or not a criteria (false)
		 * <br>arr[1] = 2nd derivative value is a selection criteria (true) or a rejection criteria (false)
		 */
		public boolean[] getThresholdActive() { 
			boolean active = btnThresh.isSelected();
			boolean selection = btnThreshSelection.isSelected();
			return new boolean[] { active, selection }; 
		}
		/**
		 * 
		 * @return <br>arr[0] = greater than threshold is a criteria (true) or not a criteria (false) 
		 * <br>arr[1] = greater than threshold is a selection (true) or a rejection criteria (false)
		 */
		public boolean[] getGreaterThanActive() { 
			boolean active = btnGreater.isSelected();
			boolean selection = btnGreaterSelection.isSelected();
			return new boolean[] { active, selection }; 
		}
		/**
		 * 
		 * @return <br>arr[0] = less than threshold is a criteria (true) or not a criteria (false)
		 * <br>arr[1] = less than threshold is a selection (true) or a rejection criteria (false)
		 */
		public boolean[] getLessThanActive() { 
			boolean active = btnLess.isSelected();
			boolean selection = btnLessSelection.isSelected();
			return new boolean[] { active, selection }; 
		}
		/**
		 * 
		 * @return <br>arr[0] = equal to is a criteria (true) or not a criteria (false) 
		 * <br>arr[1] = equal to is a selection (true) or a rejection criteria (false)
		 */
		public boolean[] getEqualToActive() { 
			boolean active = btnEqual.isSelected();
			boolean selection = btnEqualSelection.isSelected();
			return new boolean[] { active, selection }; 
		}
		/**
		 * 
		 * @return arr[0] = between values threshold is a criteria (true) or not a criteria (false) 
		 * <br>arr[1] = between values is a selection (true) or a rejection criteria (false)
		 */
		public boolean[] getBetweenActive() { 
			boolean active = btnBetween.isSelected();
			boolean selection = btnBetweenSelection.isSelected();
			return new boolean[] { active, selection }; 
		}
		/**
		 * 
		 * @return arr[0] = not equal to is a criteria (true) or not a criteria (false)<br> 
		 * arr[1] = not equal to is a selection (true) or a rejection criteria (false) }
		 */
		public boolean[] getNotEqualToActive() {
			boolean active = btnNotEqual.isSelected();
			boolean selection = btnNotEqualSelection.isSelected();
			return new boolean[] { active, selection }; 
		}

		public boolean getCoalesceIntoPixels() { return btnCoalesce.isSelected(); }

		public boolean getSpotPickAllImages() { return btnPickAll.isSelected(); }
		
		public void setThresholdActive(boolean thresholdActive, boolean selectionCriteria) {
			if(thresholdActive) {
				if(selectionCriteria) {
					btnThreshSelection.setSelected(true);
				} else {
					btnThreshRejection.setSelected(true);
				}
			} else {
				btnThreshSelection.setSelected(true);
				btnThreshRejection.setSelected(true);
			}
		}
		public void setGreaterThanActive(boolean greaterThanActive, boolean selectionCriteria) { 
			if(greaterThanActive) {
				if(selectionCriteria) {
					btnGreaterSelection.setSelected(true);
				} else {
					btnGreaterRejection.setSelected(true);
				}
			} else {
				btnGreaterSelection.setSelected(true);
				btnGreaterRejection.setSelected(true);
			}
		}
		public void setLessThanActive(boolean lessThanActive, boolean selectionCriteria) { 
			if(lessThanActive) {
				if(selectionCriteria) {
					btnLessSelection.setSelected(true);
				} else {
					btnLessRejection.setSelected(true);
				}
			} else {
				btnLessSelection.setSelected(true);
				btnLessRejection.setSelected(true);
			}
		}
		public void setEqualToActive(boolean equalToActive, boolean selectionCriteria) { 
			if(equalToActive) {
				if(selectionCriteria) {
					btnEqualSelection.setSelected(true);
				} else {
					btnEqualRejection.setSelected(true);
				}
			} else {
				btnEqualSelection.setSelected(true);
				btnEqualRejection.setSelected(true);
			}
		}
		public void setBetweenActive(boolean betweenActive, boolean selectionCriteria) { 
			if(betweenActive) {
				if(selectionCriteria) {
					btnBetweenSelection.setSelected(true);
				} else {
					btnBetweenRejection.setSelected(true);
				}
			} else {
				btnBetweenSelection.setSelected(true);
				btnBetweenRejection.setSelected(true);
			}
		}
		public void setNotEqualToActive(boolean notEqualToActive, boolean selectionCriteria) { 
			if(notEqualToActive) {
				if(selectionCriteria) {
					btnNotEqualSelection.setSelected(true);
				} else {
					btnNotEqualRejection.setSelected(true);
				}
			} else {
				btnNotEqualSelection.setSelected(true);
				btnNotEqualRejection.setSelected(true);
			}
		}
		
	}
	class CalculationFrame extends JFrame {
		private static final long serialVersionUID = -2159183445158534641L;
		private JTextArea atomsArea;
		private JButton btnLoadAtomsATD, btnCalculate, btnPlotQMax;
		private JTextField txtFileName, txtA, txtB, txtC, txtAlpha, txtBeta, txtGamma, txtQMax;
		private JComboBox<BravaisLattice.LatticeType> boxLattice;
		private JComboBox<BraggReflection> box1, box2;
		private JTabbedPane tabs;
		private JAtom[] atoms;
		private BraggReflection[] calc;
		private double maxZ = 5;
		
		public CalculationFrame() {
			super();
			initTabs();
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			setVisible(false);
			setSize(new Dimension(1000, 500));
			pack();
		}
		public double[] getParams() {
			return parseLatticeParams();
		}
		/**
		 * Determinination of the nearest hkl reflection based only on the magnitude of Q. Works with
		 * either calculated Bragg reflections or read in Bragg reflections.
		 * @param Q
		 * @return Nearest Q value (JVector)
		 */
		public JVector getNearestReflection(double Q) {
			int nearestIdx = 0;
			double nearestVal = Double.MAX_VALUE;
			double diff;
			for(int i = 0; i < calc.length; i++) {
				diff = Math.abs(Q - calc[i].getQ().length());
				if(nearestVal > diff) {
					nearestIdx = 0;
					nearestVal = diff;
				}
			}
			return calc[nearestIdx].getQ();
		}
		
		public JVector getNearestReflection(JVector pos) {
			
			return null;
		}
		
		private void initTabs() {
			tabs = new JTabbedPane();
			tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			tabs.setTabPlacement(SwingConstants.TOP);

	        Border paneEdge = BorderFactory.createEmptyBorder(0,10,10,10);
			JPanel pnlCalc = getCalculationTab();
			pnlCalc.setBorder(paneEdge);
			
			tabs.addTab("Calculation", null, pnlCalc, null);
			
			
			add(tabs, BorderLayout.CENTER);
		}
		private JPanel getRotationTab() {
			JPanel pnlMain = new JPanel();
			pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
			
			JPanel pnlClick1 = new JPanel();
			pnlClick1.setLayout(new BoxLayout(pnlClick1, BoxLayout.X_AXIS));
			
			box1 = new JComboBox<BraggReflection>();
			box1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					BraggReflection br = (BraggReflection) box1.getSelectedItem();
//					br
				}
			});
			
			pnlClick1.add(new JLabel("Click 1 options: "));
			pnlClick1.add(box1);
			
			
			box2 = new JComboBox<BraggReflection>();
			
			
			
			JPanel thePanel = new JPanel();
			thePanel.setLayout(new BorderLayout());
			thePanel.add(pnlMain, BorderLayout.CENTER);
			
			return thePanel;
		}
		private JPanel getCalculationTab() {
			JPanel pnlMain = new JPanel();
			pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
			
			atomsArea = new JTextArea(15, 50);
			atomsArea.append("Atoms");
			JScrollPane scroll = new JScrollPane(atomsArea);

			btnCalculate = new JButton("Calculate Bragg Reflections");
			
			btnCalculate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(curCalib == null) {
						JOptionPane.showMessageDialog(null, "No Calibration Selected.", 
								"Calibration selection error.", JOptionPane.ERROR_MESSAGE);
					} else {
						double[] params = parseLatticeParams();
						BravaisLattice.LatticeType latticeType = parseLatticeType();
						BravaisLattice bravais = 
								BravaisLatticeFactory.getLattice(latticeType, 
										params,
										atoms);
						double qMax = parseQMax();
						ReciprocalLattice reciprocal = new ReciprocalLattice(bravais);
						reciprocal.setCalib(curCalib);
						JVector x00 = JVector.add(curCalib.qToX00(qMax), curCalib.getDetectorCenter());
						JVector q = curCalib.coordsToQ(new Point((int) x00.getI(), (int) x00.getJ()));
						double a = calcFrame.getParams()[0];
						try {
							reciprocal.calculateReciprocalLattice(qMax, q.length()*a/2/Math.PI);
						} catch (Exception e1) {
							// this exception will be thrown if either one of the Z values is not good
							// or the wavelength selection is not within the bounds
							e1.printStackTrace();
						}
						
						Vector<BraggReflection> bragg = reciprocal.getReflections();
						calc = new BraggReflection[bragg.size()];
						calc = bragg.toArray(calc);
						
						// add the calculated bragg reflections to the pixel groupings panel
						Pixel[] p;
						BraggReflection br;
						for(int i = 0; i < calc.length; i++) {
							br = calc[i]; 
							q = curCalib.qToPixels(br.getQ());
							if(Math.abs(q.getK()) < maxZ) {
								p = new Pixel[] {new Pixel((int) Math.rint(q.getI()),
										(int) Math.rint(q.getJ()),
										br.getMeasI())};
								selectionPanel.addBragg(p);
							}
						}
					}
				}
			});
			
			JButton btnReadQValues = new JButton("Read Q values from file");
			btnReadQValues.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("Select file containing Q values");
					chooser.setMultiSelectionEnabled(false);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = chooser.showOpenDialog(null);
					switch(returnVal) {
					case JFileChooser.APPROVE_OPTION:
						double[][] vals = null;
						try {
							vals = new ReadFile(chooser.getSelectedFile(), "\t").read();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						Vector<BraggReflection> bragg = new Vector<BraggReflection>();
						for(int i = 0; i < vals.length; i++) {
							BraggReflection br = new BraggReflection(vals[i][4], vals[i][0], new JVector(vals[i][1], vals[i][2], vals[i][3]));
							br.setCalcI(br.getMeasI());
							br.setMeasI(0);
							bragg.add(br);
						}
						calc = new BraggReflection[bragg.size()];
						calc = bragg.toArray(calc);
					}
				}
			});
			
			pnlMain.add(getLoadPanel());
			pnlMain.add(getBravaisPanel());
			pnlMain.add(getParamsPanel());
			pnlMain.add(getQPanel());
			pnlMain.add(btnCalculate);
			pnlMain.add(btnReadQValues);
			
			JPanel thePanel = new JPanel();

			thePanel.setLayout(new BorderLayout());
			
			thePanel.add(pnlMain, BorderLayout.CENTER);
			thePanel.add(scroll, BorderLayout.SOUTH);
			
			return thePanel;
		}
		private JPanel getQPanel() {
			JPanel pnlQ = new JPanel();
			pnlQ.setLayout(new BoxLayout(pnlQ, BoxLayout.X_AXIS));
			
			txtQMax = new JTextField(10);
			txtQMax.addFocusListener(txtFieldFocusListener);
			txtQMax.setText(10 + "");
			btnPlotQMax = new JButton("Plot maximum Q");
			btnPlotQMax.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					double q = Double.valueOf(txtQMax.getText());
					System.out.println("q = " + q);
					double pixels = curCalib.qToX00(q).length();
					System.out.println("pixels (radius) = " + pixels);
					imagePanel.paintCircle((int) (2*pixels));
				}
			});

			JButton btnPlotRings = new JButton("Plot Bragg rings");
			btnPlotRings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					for(int i = 0; i < calc.length; i++) {
						BraggReflection br = calc[i];
						if(br.getCalcI() >= .1) {
							double q = 2*br.getQ().length();
							double pixels = curCalib.qToX00(q).length();
							imagePanel.paintCircle((int) pixels);
						}
					}
				}
			});
			
			pnlQ.add(new JLabel("Maximum Q to calculate: "));
			pnlQ.add(new JLabel("     "));
			pnlQ.add(txtQMax);
			pnlQ.add(btnPlotQMax);
			pnlQ.add(btnPlotRings);
			
			return pnlQ;
		}
		private JPanel getParamsPanel() {
			JPanel pnlParams = new JPanel();
			pnlParams.setLayout(new BoxLayout(pnlParams, BoxLayout.Y_AXIS));
			
			JPanel pnlAxes = new JPanel();
			pnlAxes.setLayout(new BoxLayout(pnlAxes, BoxLayout.X_AXIS));

			JPanel pnlAngles = new JPanel();
			pnlAngles.setLayout(new BoxLayout(pnlAngles, BoxLayout.X_AXIS));
			
			txtA = new JTextField(10);
			txtB = new JTextField(10);
			txtC = new JTextField(10);
			txtAlpha = new JTextField(10);
			txtBeta = new JTextField(10);
			txtGamma = new JTextField(10);

			txtA.addFocusListener(txtFieldFocusListener);
			txtB.addFocusListener(txtFieldFocusListener);
			txtC.addFocusListener(txtFieldFocusListener);
			txtAlpha.addFocusListener(txtFieldFocusListener);
			txtBeta.addFocusListener(txtFieldFocusListener);
			txtGamma.addFocusListener(txtFieldFocusListener);

			txtA.setText(0 + "");
			txtB.setText(0 + "");
			txtC.setText(0 + "");
			txtAlpha.setText(0 + "");
			txtBeta.setText(0 + "");
			txtGamma.setText(0 + "");
			
			pnlAxes.add(new JLabel("a"));
			pnlAxes.add(new JLabel("     "));
			pnlAxes.add(txtA);
			pnlAxes.add(new JLabel("     "));
			
			pnlAxes.add(new JLabel("b"));
			pnlAxes.add(new JLabel("     "));
			pnlAxes.add(txtB);
			pnlAxes.add(new JLabel("     "));

			pnlAxes.add(new JLabel("c"));
			pnlAxes.add(new JLabel("     "));
			pnlAxes.add(txtC);
			pnlAxes.add(new JLabel("     "));

			
			pnlAngles.add(new JLabel("\u03b1"));
			pnlAngles.add(new JLabel("     "));
			pnlAngles.add(txtAlpha);
			pnlAngles.add(new JLabel("     "));
			
			pnlAngles.add(new JLabel("\u03b2"));
			pnlAngles.add(new JLabel("     "));
			pnlAngles.add(txtBeta);
			pnlAngles.add(new JLabel("     "));

			pnlAngles.add(new JLabel("\u03b3"));
			pnlAngles.add(new JLabel("     "));
			pnlAngles.add(txtGamma);
			pnlAngles.add(new JLabel("     "));
			
			pnlParams.add(pnlAxes);
			pnlParams.add(pnlAngles);

			setParamsEditable(BravaisLattice.LatticeType.isNeeded((BravaisLattice.LatticeType) boxLattice.getSelectedItem()));
			
			return pnlParams;
		}
		private void setParamsEditable(boolean[] isNeeded) {
			txtA.setEditable(isNeeded[0]);
			txtB.setEditable(isNeeded[1]);
			txtC.setEditable(isNeeded[2]);
			txtAlpha.setEditable(isNeeded[3]);
			txtBeta.setEditable(isNeeded[4]);
			txtGamma.setEditable(isNeeded[5]);
			
			Color needed = Color.white;
			Color notNeeded = Color.LIGHT_GRAY;
			
			if(isNeeded[0]) { txtA.setBackground(needed); } 
			else { txtA.setBackground(notNeeded); }
			
			if(isNeeded[1]) { txtB.setBackground(needed); } 
			else { txtB.setBackground(notNeeded); }
			
			if(isNeeded[2]) { txtC.setBackground(needed); } 
			else { txtC.setBackground(notNeeded); }
			
			if(isNeeded[3]) { txtAlpha.setBackground(needed); } 
			else { txtAlpha.setBackground(notNeeded); }
			
			if(isNeeded[4]) { txtBeta.setBackground(needed); } 
			else { txtBeta.setBackground(notNeeded); }
			
			if(isNeeded[5]) { txtGamma.setBackground(needed); } 
			else { txtGamma.setBackground(notNeeded); }
		}
		private JPanel getBravaisPanel() {
			JPanel pnlBravais = new JPanel();
			pnlBravais.setLayout(new BoxLayout(pnlBravais, BoxLayout.X_AXIS));
			
			pnlBravais.add(new JLabel("Select the bravais lattice: "));
			pnlBravais.add(new JLabel("     "));
			
			boxLattice = new JComboBox<BravaisLattice.LatticeType>(BravaisLattice.LatticeType.values());
			
			boxLattice.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JComboBox<BravaisLattice.LatticeType> cb = (JComboBox<BravaisLattice.LatticeType>) e.getSource();
					boolean[] isNeeded = BravaisLattice.LatticeType.isNeeded((BravaisLattice.LatticeType) cb.getSelectedItem());
					setParamsEditable(isNeeded);
				}
			});
			
			pnlBravais.add(boxLattice);
			
			return pnlBravais;
		}
		private JPanel getLoadPanel() {
			JPanel pnlLoad = new JPanel();
			pnlLoad.setLayout(new BoxLayout(pnlLoad, BoxLayout.X_AXIS));
			
			txtFileName = new JTextField(20);
			
			txtFileName.setEditable(false);
			txtFileName.setBackground(Color.LIGHT_GRAY);
			
			btnLoadAtomsATD = new JButton("Load Atoms .atd file");
			btnLoadAtomsATD.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int returnVal = chooserMultiFile.showOpenDialog(null);
					File f;
					switch(returnVal) {
					case JFileChooser.APPROVE_OPTION:
						f = chooserMultiFile.getSelectedFile();
						atoms = new ReadATD(f).getAtoms();
						updateAtomsPanel();
						txtFileName.setText(f.getName());
						
						break;
					case JFileChooser.CANCEL_OPTION:
						return;
					}
				}
			});
		
			pnlLoad.add(btnLoadAtomsATD);
			pnlLoad.add(new JLabel("     "));
			pnlLoad.add(txtFileName);
			return pnlLoad;
		}
		private void updateAtomsPanel() {
			atomsArea.setText("Z\tx\ty\tz\toccupancy\n\n");
			for(int i = 0; i < atoms.length; i++) {
				atomsArea.append(atoms[i].toStringForXYZ() + "\t" + atoms[i].getOccupancy() + "\n");
			}
		}
		private double parseQMax() {
			return Double.valueOf(txtQMax.getText());
		}
		private BravaisLattice.LatticeType parseLatticeType() {
			return BravaisLattice.LatticeType.values()[boxLattice.getSelectedIndex()];
		}
		private double[] parseLatticeParams() {
			double a, b, c, alpha, beta, gamma;

			a = Double.valueOf(txtA.getText());
			b = Double.valueOf(txtB.getText());
			c = Double.valueOf(txtC.getText());
			alpha = Double.valueOf(txtAlpha.getText());
			beta = Double.valueOf(txtBeta.getText());
			gamma = Double.valueOf(txtGamma.getText());
			
			return new double[] {a, b, c, alpha, beta, gamma};
		}
		public JVector toHKL(Point p) {
			JVector hkl = new JVector(p.x, p.y, 0);
			double x0 = Double.valueOf(curCalib.getParam(Calibration.parameters.x) + "");
			double y0 = Double.valueOf(curCalib.getParam(Calibration.parameters.y) + "");
			JVector center = new JVector(x0, y0, 0);
			hkl = JVector.subtract(hkl, center);
			JVector match = new JVector();
			JVector coords;
			double angleCur, angleMin = 10, maxZ = 10, matchDist = 10, coordsDist = 0;
			if(calc != null) {
				for(int i = 0; i < calc.length; i++) {
					if(i == 2310) {
						System.out.println("i = 2310");
					}
					coords = curCalib.qToPixels(calc[i].getQ());
					coords = JVector.subtract(coords, center);
					if(Math.abs(coords.getK()) < maxZ) {
						coords = (JVector) coords.clone();
						//coords.setK(0);
						//angleCur = JVector.angleDegrees(hkl, coords);
						coordsDist = JVector.distance(coords, hkl);
						//if(angleMin >= angleCur && matchDist > coordsDist) {
						if(matchDist > coordsDist) {
							//angleMin = angleCur;
							match = calc[i].getHkl();
							matchDist = coordsDist;
						}
					}
				}
			}
			return match;
		}
	}
	private DecimalFormat format1 = new DecimalFormat("#.##");
	private DecimalFormat format2 = new DecimalFormat("0.##E0");
	public String format(double d) {
		return format1.format(d);
	}
	public String formatExp(double d) {
		return format2.format(d);
	}
	private void loadCalibrationFiles() {
		File calibrationFolder = new File("Calibration");
		File[] calibrationFiles = calibrationFolder.listFiles();
		calibFiles = new Vector<Calibration>();
		for(int i = 0; i < calibrationFiles.length; i++) {
			calibFiles.add(new Calibration(calibrationFiles[i]));
		}
	}
}
