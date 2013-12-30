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
package dataFileTypes;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ImagePropertiesViewer extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 4015426427209584857L;

	private ImageProperties properties;
	
	/**
	 * gui components
	 */
	private JTextField txtRows;
	private JTextField txtColumns;
	private JTextField txtHeader;
	private JToggleButton btn1ByteInt;
	private JToggleButton btn2ByteInt;
	private JToggleButton btn4ByteInt;
	private JToggleButton btnOther;
	private JTextField txtDataType;
	private JToggleButton btnLittleEndian;
	private JToggleButton btnBigEndian;
	private ActionListener listener;

	public ImagePropertiesViewer(ImageProperties prop, JFileChooser fc) {
		setProperties(new ImageProperties());
		fc.addPropertyChangeListener(this);
		setProperties(prop);
		setup();
	}
	public ImagePropertiesViewer(JFileChooser fc) {
		setProperties(new ImageProperties());
		fc.addPropertyChangeListener(this);
		setup();
	}
	private void setup() {
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
		pnlMain.add(setupDimension());
		pnlMain.add(setupHeader());
		pnlMain.add(setupDataType());
		pnlMain.add(setupEndianType());
		add(pnlMain);
	}

	private JPanel setupEndianType() {
		JPanel pnlEndian = new JPanel();
		pnlEndian.setBorder(BorderFactory.createTitledBorder("Select the Endianness"));
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new GridLayout(1, 0));
		
		ButtonGroup bg = new ButtonGroup();
		btnLittleEndian = new JToggleButton("Little Endian");
		bg.add(btnLittleEndian);
		btnBigEndian = new JToggleButton("Big Endian");
		bg.add(btnBigEndian);
		
		pnlEndian.add(btnLittleEndian);
		pnlEndian.add(btnBigEndian);
		
		return pnlEndian;
	}
	
	private JPanel setupDataType() {
		JPanel pnlDataType = new JPanel();
		pnlDataType.setLayout(new BoxLayout(pnlDataType, BoxLayout.Y_AXIS));
		pnlDataType.setBorder(BorderFactory.createTitledBorder("Select the Data Type"));
		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new GridLayout(2, 2));
		
		ButtonGroup bg = new ButtonGroup();
		btn1ByteInt = new JToggleButton("1 byte Int");
		bg.add(btn1ByteInt);
		btn2ByteInt = new JToggleButton("2 byte Int");
		bg.add(btn2ByteInt);
		btn4ByteInt = new JToggleButton("4 byte Int");
		bg.add(btn4ByteInt);
		btnOther = new JToggleButton("Other Int size");
		bg.add(btnOther);
		txtDataType = new JTextField(5);
		listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton btn = (JToggleButton) e.getSource();
				if(btn == btn1ByteInt) {
					txtDataType.setText("1");
					getProperties().setBytesPerEntry(1);
					txtDataType.setEditable(false);
				} else if(btn == btn2ByteInt) {
					txtDataType.setText("2");
					getProperties().setBytesPerEntry(2);
					txtDataType.setEditable(false);
				} else if(btn == btn4ByteInt) {
					txtDataType.setText("4");
					getProperties().setBytesPerEntry(4);
					txtDataType.setEditable(false);
				} else if(btn == btnOther) {
					txtDataType.setEditable(true);
					txtDataType.setText("");
				}
				btn.setSelected(true);
			}
		};
		pnlButtons.add(btn1ByteInt);
		pnlButtons.add(btn2ByteInt);
		pnlButtons.add(btn4ByteInt);
		pnlButtons.add(btnOther);
		pnlDataType.add(pnlButtons);
		btn1ByteInt.addActionListener(listener);
		btn2ByteInt.addActionListener(listener);
		btn4ByteInt.addActionListener(listener);
		btnOther.addActionListener(listener);
		
		pnlDataType.add(txtDataType);
		
		return pnlDataType;
	}
	private JPanel setupHeader() {
		JPanel pnlHeader = new JPanel();
		pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.X_AXIS));
		pnlHeader.setBorder(BorderFactory.createTitledBorder("At which byte does the data start?"));
		
		txtHeader = new JTextField(5);
		pnlHeader.add(txtHeader);
		
		return pnlHeader;
	}
	
	private JPanel setupDimension() {
		JPanel pnlDim = new JPanel();
		pnlDim.setLayout(new BoxLayout(pnlDim, BoxLayout.Y_AXIS));
		pnlDim.setBorder(BorderFactory.createTitledBorder("What are the image dimensions?"));
		
		JPanel pnlRows = new JPanel();
		pnlRows.add(new JLabel("Rows: "));
		txtRows = new JTextField(5);
		pnlRows.add(txtRows);
		
		JPanel pnlColumns = new JPanel();
		pnlColumns.add(new JLabel("Columns: "));
		txtColumns = new JTextField(5);
		pnlColumns.add(txtColumns);
		
		pnlDim.add(pnlRows);
		pnlDim.add(pnlColumns);
		return pnlDim;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		File curFile;
		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			curFile = (File) evt.getNewValue();
			if(curFile != null) {
				if(curFile.toString().contains(".")) {
					int pos = curFile.toString().lastIndexOf('.');
					String suffix;
					if (pos > 0 && pos < curFile.length() - 1) {
						suffix = curFile.toString().substring(pos+1);
						suffix = suffix.toLowerCase();
						if(suffix.compareTo("cor") == 0 ) {
							getProperties().setCORDefaults();
						} else if(suffix.compareTo("spe") == 0) {
							getProperties().setSPEDefaults();
						} else {
							getProperties().setDefaults(curFile);
						}
						update();
					}
				}
			}
		}
	}
	private void update() {
		int[] dim = getProperties().getDimensions();
		if(dim == null) {
			txtRows.setText("0");
			txtColumns.setText("0");
		} else if(dim.length == 1) {
			txtRows.setText(""+dim[0]);
			txtColumns.setText(""+dim[0]);
		} else if(dim.length == 2) {
			txtRows.setText(""+dim[0]);
			txtColumns.setText(""+dim[1]);
		}
		txtHeader.setText(""+getProperties().getHeaderSize());
		int dataType = getProperties().getBytesPerEntry();
		switch(dataType) {
			case 1: listener.actionPerformed(new ActionEvent(btn1ByteInt, 0, "")); break;
			case 2: listener.actionPerformed(new ActionEvent(btn2ByteInt, 0, "")); break;
			case 4: listener.actionPerformed(new ActionEvent(btn4ByteInt, 0, "")); break;
			default:
				listener.actionPerformed(new ActionEvent(btnOther, 0, "")); 
				txtDataType.setText(""+dataType);
		}
		switch(getProperties().getEndianType()) {
		case BIN.LITTLE_ENDIAN: btnLittleEndian.setSelected(true); break;
		case BIN.BIG_ENDIAN: btnBigEndian.setSelected(true); break;
		}
	}
	public ImageProperties parseSelections() {
		int rows = Integer.parseInt(txtRows.getText());
		int columns = Integer.parseInt(txtColumns.getText());
		properties.setDimensions(new int[] {rows, columns});
		
		int headerSize = Integer.parseInt(txtHeader.getText());
		properties.setHeaderSize(headerSize);
		
		int bytesPerEntry = Integer.parseInt(txtDataType.getText());
		properties.setBytesPerEntry(bytesPerEntry);
		
		int endianType;
		if(btnLittleEndian.isSelected()) { endianType = BIN.LITTLE_ENDIAN; }
		else {endianType = BIN.BIG_ENDIAN; }
		properties.setEndianType(endianType);
		return properties;
	}
	public ImageProperties getProperties() {
		return properties;
	}
	public void setProperties(ImageProperties properties) {
		this.properties = properties;
	}

}
/** 
 * TODO Perhaps it would be interesting to be able to "guess" the file type based on the file size and knowledge of the dimensions of the image.
 * ie if the file size is 528,384 bytes and I know that the image dimensions are 512 then the most likely 
 * data type size is 2 byte int: 512*512*2 =  524288.  528384-524288 = 4096, which is the file header size!
 */
