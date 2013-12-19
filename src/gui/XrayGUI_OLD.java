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

import image.XrayImage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;


public class XrayGUI_OLD extends JFrame {

	
	private static final long serialVersionUID = -8983067511686711222L;
	/** set up methods */
	public XrayGUI_OLD() {
		setup();
	}
	public void setup() {
		setupNorthPanel();
		add(pnlNorth, BorderLayout.NORTH);
		
		setupWestPanel();
		add(pnlWest, BorderLayout.WEST);
		
		setupCenterPanel();
		add(pnlCenter, BorderLayout.CENTER);
		
		setupEastPanel();
		add(pnlEast, BorderLayout.EAST);
		
		setupSouthPanel();
		add(pnlSouth, BorderLayout.SOUTH);
		
		pack();
		setSize(1500, 1000);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	private void setupNorthPanel() {
		pnlNorth = new ControlPanel();
	}
	
	private void setupEastPanel() {
		pnlEast = new JPanel();
		pnlEast.setLayout(new BoxLayout(pnlEast, BoxLayout.Y_AXIS));
		imageModifierPanel = new ImageModifierPanel();
		pnlEast.add(imageModifierPanel);
		infoPanel = new InfoPanel();
		pnlEast.add(infoPanel);
	}
	private void setupCenterPanel() {
		pnlCenter = new JPanel();
		pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
		imagePanel = new ImagePanel();
		pnlCenter.add(imagePanel);
	}
	private void setupWestPanel() {
		pnlWest = new JPanel();
		pnlWest.setLayout(new BoxLayout(pnlWest, BoxLayout.Y_AXIS));
		regionPanel = new RegionPanel();
		pnlWest.add(regionPanel);
	}
	private void setupSouthPanel() {
		pnlSouth = new JPanel();
		contextPanel = new ContextPanel();
		pnlSouth.add(contextPanel);
	}
	class ControlPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3824139347880828025L;
		/** set up methods */
		public ControlPanel() {
			setup();
		}
		private void setup() {
			pnlSelectionMode = new SelectionPanel();
			add(pnlSelectionMode);
			
			pnlShapes = new ShapePanel();
			add(pnlShapes);
			
			pnlPaths = new PathPanel();
			add(pnlPaths);
			
			pnlZoom = new ZoomPanel();
			add(pnlZoom);
			
		}
		/** variables */
		private TypePanel pnlSelectionMode;
		private TypePanel pnlShapes;
		private TypePanel pnlPaths;
		private TypePanel pnlZoom;
		private JPanel pnlCoords;

		abstract class TypePanel extends JPanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2812534282969136579L;
			public TypePanel(String title) {
				setBorder(new TitledBorder(title));
				setLayout(new GridLayout(2, 0));
				btnGroup = new ButtonGroup();
			}
			public void setEnabled(boolean enable) {
				Enumeration<AbstractButton> group = btnGroup.getElements();
				btnGroup.clearSelection();
				while(group.hasMoreElements()) {
					group.nextElement().setEnabled(enable);
				}
			}
			public abstract void toggle(String msg);
			public void resetButtons() { btnGroup.clearSelection(); }
			protected ButtonGroup btnGroup;
		}
		class SelectionPanel extends TypePanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2495685512638348275L;

			/** getter methods */
			public SelectionMode getSelectionMode() { return selectionMode; }
			/** set up methods */
			public SelectionPanel() {
				super("Selection");
				setup();
			}
			private void setup() {
				listener = new ToggleActionListener(this);
				setupButtons();
				
			}
			private void setupButtons() {
				JToggleButton btn;
				for(SelectionMode selectionMode : SelectionMode.values()) {
					btn = new JToggleButton(selectionMode.toString());
					btn.addActionListener(listener);
					btnGroup.add(btn);
					add(btn);
				}
			}
			/** variables */
			private SelectionMode selectionMode;
			private ActionListener listener;
			
			class ToggleActionListener implements ActionListener {
				public ToggleActionListener(TypePanel typePanel) {
					this.typePanel = typePanel;
				}
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JToggleButton btn = (JToggleButton) arg0.getSource();
					selectionMode = SelectionMode.valueOf(btn.getText());
					typePanel.toggle(btn.getText());
					System.out.println(selectionMode);
					SelectionMode mode = SelectionMode.valueOf(btn.getText());
					switch(mode) {
					case Spot:
						pnlShapes.setEnabled(true);
						pnlPaths.setEnabled(false);
						pnlZoom.setEnabled(false);
						break;
					case Region:
						pnlShapes.setEnabled(true);
						pnlPaths.setEnabled(false);
						pnlZoom.setEnabled(false);
						break;
					case Path:
						pnlShapes.setEnabled(false);
						pnlPaths.setEnabled(true);
						pnlZoom.setEnabled(false);
						break;
					case Zoom:
						pnlShapes.setEnabled(false);
						pnlPaths.setEnabled(false);
						pnlZoom.setEnabled(true);
						break;
					case None:
						pnlShapes.setEnabled(false);
						pnlPaths.setEnabled(false);
						pnlZoom.setEnabled(false);
						break;
					
					}
					
				}
				private TypePanel typePanel;
			}

			@Override
			public void toggle(String msg) {
				// TODO Auto-generated method stub
				
			}
		}
		class ShapePanel extends TypePanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7554673937723167267L;

			/** getter methods */
			public ShapeMode getShapeMode() { return shapeMode; }
			/** set up methods */
			public ShapePanel() {
				super("Areas");
				setup();
			}
			private void setup() {
				listener = new ToggleActionListener();
				setupButtons();
				
			}
			private void setupButtons() {
				JToggleButton btn;
				for(ShapeMode shape : ShapeMode.values()) {
					btn = new JToggleButton(shape.toString());
					btn.addActionListener(listener);
					btnGroup.add(btn);
					add(btn);
				}
			}
			/** variables */
			private ShapeMode shapeMode;
			private ActionListener listener;
			
			class ToggleActionListener implements ActionListener {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JToggleButton btn = (JToggleButton) arg0.getSource();
					shapeMode = ShapeMode.valueOf(btn.getText());
					System.out.println(shapeMode);					
				}
				
			}

			@Override
			public void toggle(String msg) {
				// TODO Auto-generated method stub
				
			}
		}
		class PathPanel extends TypePanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4176122737956858893L;

			/** getter methods */
			public PathMode getPathMode() { return pathMode; }
			/** set up methods */
			public PathPanel() {
				super("Paths");
				setup();
			}
			private void setup() {
				listener = new ToggleActionListener();
				setupButtons();
				
			}
			private void setupButtons() {
				JToggleButton btn;
				for(PathMode shape : PathMode.values()) {
					btn = new JToggleButton(shape.toString());
					btn.addActionListener(listener);
					btnGroup.add(btn);
					add(btn);
				}
			}
			/** variables */
			private PathMode pathMode;
			private ActionListener listener;
			
			class ToggleActionListener implements ActionListener {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JToggleButton btn = (JToggleButton) arg0.getSource();
					pathMode = PathMode.valueOf(btn.getText());
					System.out.println(pathMode);					
				}
				
			}

			@Override
			public void toggle(String msg) {
				// TODO Auto-generated method stub
				
			}
		}
		class ZoomPanel extends TypePanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6558853284871803501L;

			/** getter methods */
			public ZoomMode getZoomMode() { return zoomMode; }
			/** set up methods */
			public ZoomPanel() {
				super("Zoom");
				setup();
			}
			private void setup() {
				listener = new ToggleActionListener();
				setupButtons();
				
			}
			private void setupButtons() {
				JToggleButton btn;
				for(ZoomMode shape : ZoomMode.values()) {
					btn = new JToggleButton(shape.toString());
					btn.addActionListener(listener);
					btnGroup.add(btn);
					add(btn);
				}
			}
			/** variables */
			private ZoomMode zoomMode;
			private ActionListener listener;
			
			class ToggleActionListener implements ActionListener {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JToggleButton btn = (JToggleButton) arg0.getSource();
					zoomMode = ZoomMode.valueOf(btn.getText());
					System.out.println(zoomMode);					
				}
				
			}

			@Override
			public void toggle(String msg) {
				// TODO Auto-generated method stub
				
			}
		}
		class CoordsPanel extends JPanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1952366102029800181L;
			public CoordsPanel(ActionListener updateListener) {
				setBorder(new TitledBorder("Coordinates"));
				setLayout(new GridLayout(2, 3));
				setup(updateListener);
			}
			private void setup(ActionListener selectionListener) {
				add(new JLabel("x "));
				xCoord = new JTextField("0");
				add(xCoord);
				JButton accept = new JButton(ACCEPT);
				accept.addActionListener(selectionListener);
				add(accept);
				add(new JLabel("x "));
				yCoord = new JTextField("0");
				JButton cancel = new JButton(CANCEL);
				cancel.addActionListener(selectionListener);
				add(cancel);
			}
			
			private JTextField xCoord;
			private JTextField yCoord;
		}
	}
	
	class RegionPanel extends JTabbedPane {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2069103103671785211L;
		public RegionPanel() {
			super();
			setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			//setSize(400, 500);
			setBorder(new TitledBorder("Selections"));
			addTab("Regions", getNewTab());
			addTab("Spots", getNewTab());
			addTab("Paths", getNewTab());
			pack();
		}
		public JPanel getNewTab() {
			JPanel newTab = new JPanel();
			newTab.setLayout(new BoxLayout(newTab, BoxLayout.Y_AXIS));
			newTab.setBorder(new EtchedBorder());
			newTab.add(new JLabel("regionPanel                          "));
			return newTab;
		}
	}
	class SpotPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8150990556413831612L;
		
	}
	class InfoPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5785637983093752576L;
		public InfoPanel() {
			super();
			setBorder(new TitledBorder("Info"));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setMaximumSize(new Dimension(200, 200));
			setup();
		}
		
		private void setup() {
			lblx = new JLabel("x:");
			lblx.setPreferredSize(new Dimension(10, 25));
			lbly = new JLabel("y:");
			lbly.setPreferredSize(new Dimension(10, 25));
			lblI = new JLabel("I:");
			lblI.setPreferredSize(new Dimension(10, 25));
			
			txtx = new JTextField("0");
			txtx.setPreferredSize(new Dimension(100, 25));
			txtx.setEditable(false);
			txty = new JTextField("0");
			txty.setPreferredSize(new Dimension(100, 25));
			txty.setEditable(false);
			txtI = new JTextField("0");
			txtI.setPreferredSize(new Dimension(100, 25));
			txtI.setEditable(false);
			
			JPanel pnl1 = new JPanel();
			pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.X_AXIS));
			pnl1.add(lblx);
			pnl1.add(txtx);
			
			JPanel pnl2 = new JPanel();
			pnl2.setLayout(new BoxLayout(pnl2, BoxLayout.X_AXIS));
			pnl2.add(lbly);
			pnl2.add(txty);

			JPanel pnl3 = new JPanel();
			pnl3.setLayout(new BoxLayout(pnl3, BoxLayout.X_AXIS));
			pnl3.add(lblI);
			pnl3.add(txtI);
			
			add(pnl1);
			add(pnl2);
			add(pnl3);
			
		}
		public void updateText(int x, int y, double I) {
			txtx.setText("" + x);
			txty.setText("" + y);
			txtI.setText("" + I);
		}
		private JTextField txtx;
		private JTextField txty;
		private JTextField txtI;
		private JLabel lblx;
		private JLabel lbly;
		private JLabel lblI;
		
	}
	class ImagePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8241045728996843103L;
		private JLabel txtFile;
		private JPanel pnlImage;
		private JPanel pnlFileInfo;
		private JButton btnLoad;
		private ActionListener loadFileListener;
		private JFileChooser chooser;
		private XrayImage xrayImage;
		public ImagePanel() {
			super();
			setBorder(new TitledBorder("Image"));
			setLayout(new BorderLayout());
			loadFileListener = new LoadFileListener();
			chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			setup();
		}
		private void setup() { 
			pnlImage = new JPanel();
			xrayImage = new XrayImage();
			pnlImage.add(xrayImage);
			pnlFileInfo = new JPanel();
			pnlFileInfo.setAlignmentX(LEFT_ALIGNMENT);
			pnlFileInfo.setPreferredSize(new Dimension(1000, 25));
			btnLoad = new JButton("Load File");
			btnLoad.addActionListener(loadFileListener);
			pnlFileInfo.add(btnLoad);
			txtFile = new JLabel("file info");
			pnlFileInfo.add(txtFile);
			add(pnlImage);
			add(pnlFileInfo);
		}
		public void updateFile(File newFile) {
			txtFile.setText(newFile.toString());
			xrayImage.loadNewFile(newFile);
			xrayImage.repaint();
			System.out.println(newFile);
			this.updateUI();
		}
		class LoadFileListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooser.showOpenDialog(null);
				updateFile(chooser.getSelectedFile());
			}
			
		}
	}
	class ContextPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3440477822356525592L;

		public ContextPanel() {
			super();
			setBorder(new TitledBorder("Context"));
			setPreferredSize(new Dimension(1000, 50));
		}
	}
	class FilesPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 593500159900065102L;
		public FilesPanel() {
			super();
			setBorder(new TitledBorder("files"));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		public File getSelectedFile() {
			chooser.showOpenDialog(this);
			System.out.println(chooser.getSelectedFile());
			return chooser.getSelectedFile();
		}
		class FileTree extends JScrollPane {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5625756317124410996L;
			public FileTree() {
				super();
				fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				updateTree(new File("C:\\Program Files (x86)\\Project64 1.6"));
			}
			public File getSelectedFile() {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
				System.out.println(((File) node.getUserObject()).toString());
				return (File) node.getUserObject();
			}
			public void updateTree(File newRoot) {
				DefaultMutableTreeNode root = new DefaultMutableTreeNode(newRoot);
				addChildren(root);
				fileTree = new JTree(root);
				setViewportView(fileTree);
			}
			private void addChildren(DefaultMutableTreeNode node) {
				DefaultMutableTreeNode child;
				File theNode = (File) node.getUserObject();
				if(theNode.isDirectory()) {
					File[] theNodesContents = theNode.listFiles();
					for(int i = 0; i < theNodesContents.length; i++) {
						child = new DefaultMutableTreeNode(theNodesContents[i]);
						node.add(child);
						addChildren(child);
					}
				}
			}
			private JTree fileTree;
		}
		/** variables */
		private FileTree theTree;
		private JButton btnLoad;
		private JFileChooser chooser;
	}
	class ImageModifierPanel extends JTabbedPane {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1934968730708977369L;
		public ImageModifierPanel() {
			super();
			setBorder(new TitledBorder("Image Modifications"));
			addTab("Color", new ColorPanel());
			addTab("Image Filters", new FilterPanel());
			setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		}
		class ColorPanel extends JPanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7531094554616309967L;

			public ColorPanel() {
				super();
				add(new JLabel("color panel"));
			}
		}
		class FilterPanel extends JPanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5667559244700284690L;

			public FilterPanel() {
				super();
				add(new JLabel("filter panel"));
			}
		}	
	}
	
	private final static String ACCEPT = "Accept";
	private final static String CANCEL = "Cancel";
	private final static String RESET = "Reset";
	/** variables */

	enum SelectionMode {Spot, Region, Path, Zoom, None};
	enum ShapeMode {Square, Rectangle, Circle, Ellipse, Freeform, MouseClick};
	enum PathMode {Line, QuadCurve, BezierCurve, Freeform, MouseClick};
	enum ZoomMode {BasicIn, BasicOut, RectIn, Coords, Reset};
	private JPanel tabbedPane;
	private JPanel pnlSouth, pnlEast, pnlWest, pnlCenter, pnlNorth;
	private JMenuBar menu;	
	private RegionPanel regionPanel;
	private ImageModifierPanel imageModifierPanel;
	private FilesPanel filesPanel;
	private ImagePanel imagePanel;
	private ContextPanel contextPanel;
	private InfoPanel infoPanel;
}
