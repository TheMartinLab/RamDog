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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class CalculatedImage extends JPanel implements MouseMotionListener, MouseWheelListener {

	private static final long serialVersionUID = 2834732043531881969L;
	/* *************** */
	/* IMAGE VARIABLES */
	/* *************** */
	private BufferedImage image;
	private double[][] data;
	private double displayedMinVal;
	private double displayedMaxVal;
	private double rawMinVal;
	private double rawMaxVal;
	private int zoomX1, zoomX2, zoomY1, zoomY2;
	private int viewWidth, viewHeight;
	
	public final static int DEFAULT_IMAGE_WIDTH = 250;
	public final static int DEFAULT_IMAGE_HEIGHT = 250;
	/* *************** */
	/* INPUT VARIABLES */
	/* *************** */
	private String imageName;
	private Vector<File> files;
	private Vector<String> fileComments;
	private JVector vx, vy;
	private double qMaxX, qMaxY;
	private double qStep;
	
	private JTextField txtX, txtY, txtI;
	
	public CalculatedImage(String imageName) {
		this.imageName = imageName;
		viewWidth = DEFAULT_IMAGE_WIDTH;
		viewHeight = DEFAULT_IMAGE_HEIGHT;
		zoomX1 = 0;
		zoomY1 = 0;
		zoomX2 = viewWidth;
		zoomY2 = viewHeight;
	}
	
	public void initImage() {
		WritableRaster raster = image.getRaster();
		ColorModel model = image.getColorModel();
		Color color;
		Object colorData;
		Random r = new Random();
		double x = 0.05;
		double y = x;
		for(int i = 0; i < DEFAULT_IMAGE_WIDTH; i++) {
			for(int j = 0; j < DEFAULT_IMAGE_HEIGHT; j++) {
				color = new Color((int) Math.abs(r.nextInt(255)*Math.sin(x*j)*Math.cos(y*i))%255, 
						(int) Math.abs(r.nextInt(255)*Math.sin(x*j)*Math.cos(y*i))%255, 
						(int) Math.abs(r.nextInt(255)*Math.sin(x*j)*Math.cos(y*i))%255);
				colorData = model.getDataElements(color.getRGB(), null);
				raster.setDataElements(i, j, colorData);
			}
		}
		repaint();
	}
	/* ********************* */
	/* IMAGE RUNTIME METHODS */
	/* ********************* */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, 0, 0, viewWidth, viewHeight, zoomX1, zoomY1, zoomX2, zoomY2, null);
	}
	public void update() {
		
	}
	private Point viewCoordsToImageCoords(Point p) {
		int newx1 = zoomX1 + (int) Math.rint(((double) zoomX2 - (double) zoomX1) / (viewWidth) * p.x);
		int newy1 = zoomY1 + (int) Math.rint(((double) zoomY2 - (double) zoomY1) / (viewHeight) * p.y);
		
		return new Point(newx1, newy1);
	}
	private boolean insideBounds(Point p) {
		if(p.x >= zoomX2) 
			return false;
		if(p.x < zoomX1) 
			return false;
		if(p.y >= zoomY2)  
			return false;
		if(p.y < zoomY1) 
			return false;
		
		return true;
	}
	public JPanel getIntensityHistogram() {
		JPanel histogram = new JPanel();
		
		
		
		return histogram;
	}
	/* ******************************** */
	/* INTERFACE IMPLEMENTATION METHODS */
	/* ******************************** */
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		Point point = arg0.getPoint();
		point = viewCoordsToImageCoords(point);
		if(insideBounds(point)) {
			Point coords = viewCoordsToImageCoords(point);
			double I = data[coords.x][coords.y];
			JVector x = JVector.multiply(this.vx, coords.x * qStep);
			JVector y = JVector.multiply(this.vy, coords.y * qStep);
			txtI.setText(I + "");
			txtX.setText(x.toString(3));
			txtY.setText(y.toString(3));
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		
	}

	
	/* ******************* */
	/* GETTERS AND SETTERS */
	/* ******************* */
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public double[][] getData() {
		return data;
	}

	public void setData(double[][] data) {
		this.data = data;
	}

	public double getDisplayedMinVal() {
		return displayedMinVal;
	}

	public void setDisplayedMinVal(double displayedMinVal) {
		this.displayedMinVal = displayedMinVal;
	}

	public double getDisplayedMaxVal() {
		return displayedMaxVal;
	}

	public void setDisplayedMaxVal(double displayedMaxVal) {
		this.displayedMaxVal = displayedMaxVal;
	}

	public double getRawMinVal() {
		return rawMinVal;
	}

	public void setRawMinVal(double rawMinVal) {
		this.rawMinVal = rawMinVal;
	}

	public double getRawMaxVal() {
		return rawMaxVal;
	}

	public void setRawMaxVal(double rawMaxVal) {
		this.rawMaxVal = rawMaxVal;
	}

	public int getZoomX1() {
		return zoomX1;
	}

	public void setZoomX1(int zoomX1) {
		this.zoomX1 = zoomX1;
	}

	public int getZoomX2() {
		return zoomX2;
	}

	public void setZoomX2(int zoomX2) {
		this.zoomX2 = zoomX2;
	}

	public int getZoomY1() {
		return zoomY1;
	}

	public void setZoomY1(int zoomY1) {
		this.zoomY1 = zoomY1;
	}

	public int getZoomY2() {
		return zoomY2;
	}

	public void setZoomY2(int zoomY2) {
		this.zoomY2 = zoomY2;
	}

	public int getViewWidth() {
		return viewWidth;
	}

	public void setViewWidth(int viewWidth) {
		this.viewWidth = viewWidth;
	}

	public int getViewHeight() {
		return viewHeight;
	}

	public void setViewHeight(int viewHeight) {
		this.viewHeight = viewHeight;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public Vector<File> getFiles() {
		return files;
	}

	public void setFiles(Vector<File> files) {
		this.files = files;
	}

	public Vector<String> getFileComments() {
		return fileComments;
	}

	public void setFileComments(Vector<String> fileComments) {
		this.fileComments = fileComments;
	}

	public double getqMaxX() {
		return qMaxX;
	}

	public void setqMaxX(double qMaxX) {
		this.qMaxX = qMaxX;
	}

	public double getqMaxY() {
		return qMaxY;
	}

	public void setqMaxY(double qMaxY) {
		this.qMaxY = qMaxY;
	}

	public double getqStep() {
		return qStep;
	}

	public void setqStep(double qStep) {
		this.qStep = qStep;
	}

	public JTextField getTxtX() {
		return txtX;
	}

	public void setTxtX(JTextField txtX) {
		this.txtX = txtX;
	}

	public JTextField getTxtY() {
		return txtY;
	}

	public void setTxtY(JTextField txtY) {
		this.txtY = txtY;
	}

	public JTextField getTxtI() {
		return txtI;
	}

	public void setTxtI(JTextField txtI) {
		this.txtI = txtI;
	}

	public JVector getVx() {
		return vx;
	}

	public void setVx(JVector vx) {
		this.vx = vx;
	}

	public JVector getVy() {
		return vy;
	}

	public void setVy(JVector vy) {
		this.vy = vy;
	}
	
}
