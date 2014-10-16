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
package color;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import gui.ImageDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/* ColorChooserDemo.java requires no other files. */
public class ColorChooser extends JPanel
                              implements ChangeListener, TableModelListener {

	private final static int H_SIZE = 5;
    protected JColorChooser jcc;
    
    protected JPanel legend;
    protected JLabel lblLower, lblUpper;
    protected Double initLower = 0.;
    protected Double initUpper = 1000.;
    protected Color cInitLower = Color.white;
    protected Color cInitUpper = Color.black;
    protected ButtonGroup bg;
    final protected Vector<Color> colors = new Vector<Color>();
    final protected Vector<Double> levels = new Vector<Double>();
    protected JComponent boxLevels;
    protected JTable table;
    private MyCellRenderer renderer = new MyCellRenderer(colors);
    private JScrollPane scrollPane;
    private ListSelectionListener tableListener;
    private ImageDisplay img;
    private boolean removingRow = false;
    private SigmoidalColorModel sigmoidColorModel;
    protected JFrame frame;
    public ColorChooser() {
        super(new BorderLayout());

        sigmoidColorModel = new SigmoidalColorModel(24);
        JPanel colorSchemePanel = setupColorSchemePanel();
        
        //Set up color chooser for setting text color
        jcc = new JColorChooser(Color.white);
        jcc.getSelectionModel().addChangeListener(this);
        jcc.setBorder(BorderFactory.createTitledBorder(
                                             "Choose Text Color"));

        add(colorSchemePanel, BorderLayout.CENTER);
        add(jcc, BorderLayout.PAGE_END);
    }
    
    /* ********************* */
    /* INITIAL SETUP METHODS */
    /* ********************* */
    
    public JComponent setupLegend() {
    	Box boxLegend = Box.createVerticalBox();
    	legend = new JPanel();
    	legend.setSize(new Dimension(250, 10));
    	Box boxLabels = Box.createHorizontalBox();
    	lblLower = new JLabel();
    	lblUpper = new JLabel();
    	
    	boxLabels.add(lblLower);
    	boxLabels.add(Box.createHorizontalGlue());
    	boxLabels.add(lblUpper);
    	
    	boxLegend.add(legend);
    	boxLegend.add(boxLabels);
    	
    	return boxLegend;
    }
    public void setInitialColorLevels() {
        levels.add(initLower);
        levels.add(initUpper);

        colors.add(cInitLower);
        colors.add(cInitUpper);
    }
    
    public void initScrollPaneForTable() {
    	scrollPane = new JScrollPane(table);
    	scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
    public Component getButtons() {
    	JButton btnUp = new JButton("Move Up");
    	JButton btnDown = new JButton("Move Down");
    	JButton btnRemove = new JButton("Remove Selected");
    	JButton btnInsert = new JButton("Insert");
    	
    	btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				moveUp();
			}
    	});
    	

    	btnDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				moveDown();
			}
    	});
    	
    	btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeRow();
			}
    	});
    	btnInsert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addRow();
			}
    	});
    	
    	Box box = Box.createHorizontalBox();
    	box.add(btnUp);
    	box.add(Box.createHorizontalStrut(H_SIZE));
    	box.add(btnDown);
    	box.add(Box.createHorizontalStrut(H_SIZE));
    	box.add(btnRemove);
    	box.add(Box.createHorizontalStrut(H_SIZE));
    	box.add(btnInsert);
    	
    	return box;
    }
    public JPanel setupColorSchemePanel() {
    	setInitialColorLevels();
    	
    	JPanel colorSchemePanel = new JPanel();
    	colorSchemePanel.setBorder(BorderFactory.createTitledBorder("Color Scheme"));
    	tableListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selectedRow = table.getSelectedRow();
				if(jcc != null && !removingRow)
					jcc.setColor(colors.get(selectedRow));
			}
    	};

    	initScrollPaneForTable();
    	refreshTable();
    	Box components = Box.createVerticalBox();
        components.add(setupLegend());
        components.add(scrollPane);
        components.add(getButtons());
        
        colorSchemePanel.add(components);
    	return colorSchemePanel;
    }
    
    
    /* ************************************** */
    /* RUNTIME METHODS FOR LEVEL MANIPULATION */
    /* ************************************** */
    public void newLevels(double[] vals, Color[] col) {
    	levels.clear();
    	for(int i = 0; i < vals.length; i++)
    		levels.add(vals[i]);
    	
    	colors.clear();
    	for(int i = 0; i < col.length; i++)
    		colors.add(col[i]);
    	
    	refreshTable();
    }
    private void addRow() {
    	int lastSelectedRow = 1;
    	if(table.getRowCount() == 0) {
    		levels.add(0.);
    		colors.add(Color.white);
    	} else {
	    	int[] rows = table.getSelectedRows();
	    	lastSelectedRow = 0;
	    	if(rows.length > 0)
	    		lastSelectedRow = rows[rows.length-1];
	
	    	
			levels.add(lastSelectedRow+1, levels.get(lastSelectedRow)+1);
			colors.add(lastSelectedRow+1, new Color(colors.get(lastSelectedRow).getRGB()));
    	}
		refreshTable();
    	table.setRowSelectionInterval(lastSelectedRow+1, lastSelectedRow+1);
    	
    }

    private void removeRow() {
    	removingRow = true;
    	int[] rows = table.getSelectedRows();
    	int lastSelectedRow = 0;
    	if(rows.length > 0)
    		lastSelectedRow = rows[rows.length-1];

    	
		levels.remove(lastSelectedRow);
		colors.remove(lastSelectedRow);
    	refreshTable();
    	table.setRowSelectionInterval(lastSelectedRow-1, lastSelectedRow-1);
       	removingRow = false;
    }
    private void moveUp(){
    	int row = table.getSelectedRow();
    	if(row+1 > 1) {
	    	Color c = colors.remove(row);
	    	colors.add(row-1, c);
	    	Double level = levels.remove(row);
	    	levels.add(row-1, level);
	    	refreshTable();
	    	row--;
    	}
    	table.setRowSelectionInterval(row, row);
    }
    private void moveDown(){
    	int row = table.getSelectedRow();
    	if(row+1 < colors.size()) {
	    	Color c = colors.remove(row);
	    	colors.add(row+1, c);
	    	Double level = levels.remove(row);
	    	levels.add(row+1, level);
	    	refreshTable();
	    	row++;
    	}
    	table.setRowSelectionInterval(row, row);
    }
    
	private void refreshTable() {
		int rowSelection = 0;
		if(table != null)
			rowSelection = table.getSelectedRow();
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("Level #");
		columnNames.add("Value");
		columnNames.add("Red");
		columnNames.add("Green");
		columnNames.add("Blue");
		columnNames.add("Color");
		Vector<Vector<Object>> rowData = new Vector<Vector<Object>>();
		Vector<Object> row;
		for(int i = 0; i < levels.size(); i++) {
			row = new Vector<Object>();
			row.add(i);
			row.add(levels.get(i));
			Color c = colors.get(i);
			row.add(c.getRed());
			row.add(c.getGreen());
			row.add(c.getBlue());
//			row.add(lblColors.get(i));
			rowData.add(row);
		}
		
		table = new JTable(rowData, columnNames);
		table.getSelectionModel().addListSelectionListener(tableListener);
		table.getModel().addTableModelListener(this);
		table.setMaximumSize(table.getPreferredSize());
		TableColumn col = table.getColumn("Color");
		col.setCellRenderer(renderer);
		
		TableColumn column = null;
		for(int i = 0; i < columnNames.size(); i++) {
			column = table.getColumnModel().getColumn(i);
			column.setMaxWidth(column.getPreferredWidth());
		}
		scrollPane.setViewportView(table);
		scrollPane.setMaximumSize(scrollPane.getPreferredSize());
		repaint();
		if(removingRow)
			rowSelection--;
    	table.setRowSelectionInterval(rowSelection, rowSelection);
    	
    	sigmoidColorModel.setColors(colors);
    	sigmoidColorModel.setLevels(levels);
	}
	
    private int getSelectedLevelIdx() {
    	return table.getSelectedRow();
    }
    @Override
	public void stateChanged(ChangeEvent e) {
        int rowIdx = getSelectedLevelIdx();
        colors.remove(rowIdx);
        colors.add(rowIdx, jcc.getColor());
		refreshTable();
		table.setRowSelectionInterval(rowIdx, rowIdx);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static ColorChooser createAndShowGUI(boolean standAlone) {
        //Create and set up the window.
        JFrame frame = new JFrame("ColorChooserDemo");
        if(standAlone)
        	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else
        	frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        //Create and set up the content pane.
        ColorChooser chooser = new ColorChooser();
        chooser.setOpaque(true); //content panes must be opaque
        frame.setContentPane(chooser);

        //Display the window.
        frame.pack();
        if(standAlone)
        	frame.setVisible(true);
        else
        	frame.setVisible(false);
        chooser.frame = frame;
        return chooser;
    }
    
    public void showGUI() {
    	frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                createAndShowGUI(true);
            }
        });
    }

	@Override
	public void tableChanged(TableModelEvent e) {
		switch(e.getType()) {
		case TableModelEvent.DELETE:
			return;
		}
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
        
        if(columnName.compareTo("Value") == 0) {
        	levels.remove(row);
        	levels.add(row, Double.valueOf((String) data));
        	refreshTable();
        }
    }

	public ImageDisplay getImg() { return img; }
	public void setImg(ImageDisplay img) { this.img = img; }

	public SigmoidalColorModel getSigmoidColorModel() { return sigmoidColorModel; }
	public void setSigmoidColorModel(SigmoidalColorModel sigmoidColorModel) { this.sigmoidColorModel = sigmoidColorModel; }

}

class MyCellRenderer implements TableCellRenderer {

	private Vector<Color> colors;
	public MyCellRenderer(Vector<Color> colors) {
		this.colors = colors;
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel c = new JLabel();
		Rectangle rect = table.getCellRect(row, column, false);
		Dimension d = new Dimension(rect.width, rect.height);
		c.setSize(d);
		Color color = colors.get(row);
		c.setBackground(color);
		c.setForeground(color);
		c.setOpaque(true);
		return c;
	}
	
}
