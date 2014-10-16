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

import geometry.JVector;
import io.MyFileInputStream;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Vector;

public class CalculatedXrayFile {

	private String commentLineStartsWith = "#";
	private String JVectorToStringSeparation = ",";
	private File file;
	private double[][] columnarData, matrixData;
	private double qMaxX, qMaxY, qStep;
	private int minX, minY, maxX, maxY;
	private Vector<JVector[]> projections;
	private Point2D.Double midPoint;
	private boolean isOldFormatXrayFile;
	private int numFilesPerImage = 1;
	private int numCalculationsPerFile = 1;
	
	@Override
	public String toString() {
		int maxLen = Math.min(25, file.getName().length());
		return file.getName().substring(0, maxLen);
	}
	public CalculatedXrayFile(File file) {
		this.file = file;
		projections = new Vector<JVector[]>();
	}

	/* ************ */
	/* INPUT/OUTPUT */
	/* ************ */
	private String setAxes(Scanner s) {
		//String line = s.nextLine();
		String line;
		String[] vals;
		Scanner lineScanner;
		Vector<JVector> aProjection = new Vector<JVector>();
		numFilesPerImage = 0;
		do {
			line = s.nextLine();
			lineScanner = new Scanner(line);
			while(lineScanner.hasNext()) {
				vals = lineScanner.next().split(JVectorToStringSeparation);
				if(vals.length == 3) {
					JVector vec = new JVector(Double.valueOf(vals[0]), Double.valueOf(vals[1]),
							Double.valueOf(vals[2]));
					aProjection.add(vec);
				}
				if(aProjection.size() == 3) {
					JVector[] arr = aProjection.toArray(new JVector[aProjection.size()]);
					projections.add(arr);
					aProjection.clear();
					numFilesPerImage++;
				}
			}
			lineScanner.close();
		} while(!line.contains("Maximum q values"));

		return line;
	}
	private void setMaxQVals(String line) {
		Scanner s = new Scanner(line);
		do {
			try {
				qMaxX = s.nextDouble();
				qMaxY = s.nextDouble();
				break;
			} catch(InputMismatchException e) {
				s.next();
			}
		} while(s.hasNext());
		s.close();
	}
	private void setDeltaQ(String line) {
		Scanner s = new Scanner(line);
		do {
			try {
				qStep = s.nextDouble();
				break;
			} catch(InputMismatchException e) {
				s.next();
			}
		} while(s.hasNext());
		s.close();
	}
	private void readOldFormat(Scanner s) {
		String fName = file.getName();
		boolean forcedProjection = true;
		JVector[] axes = new JVector[] {
				new JVector(1, -1, 0),
				new JVector(-1, -1, 2),
				new JVector(1, 1, 1),
		};
		if(!forcedProjection) {
			if(fName.contains("110") || fName.contains("101") || fName.contains("011")) {
				numCalculationsPerFile = 6;
				axes = new JVector[] {
					new JVector(-1, 0, 0),
					new JVector(0, 1, -1),
					new JVector(0, -1, -1),
				};
			}
			else if(fName.contains("111")) {
				numCalculationsPerFile = 4;
				axes = new JVector[] {
					new JVector(1, -1, 0),
					new JVector(-1, -1, 2),
					new JVector(-1, -1, -1),
				};
			}
			else if(fName.contains("001") || fName.contains("010") || fName.contains("100")) {
				numCalculationsPerFile = 3;
				axes = new JVector[] {
					new JVector(1, 0, 0),
					new JVector(0, 1, 0),
					new JVector(0, 0, 1),
				};
			}
		}
		
		projections.add(axes);

		Vector<double[]> lines = new Vector<double[]>(5000);
		double x, y, I;
		do {
			String line = s.nextLine();
			String[] splitLine = line.split("\t");
			if(splitLine.length > 1)  {
				try {
					x = Double.valueOf(splitLine[0]);
					y = Double.valueOf(splitLine[1]);
					I = Double.valueOf(splitLine[2]);
					lines.add(new double[] {x, y, I});
				} catch(NumberFormatException nfe) {
					//nfe.printStackTrace();
				}
			}
		} while(s.hasNextLine());
		
		columnarData = new double[lines.size()][3];
		columnarData = lines.toArray(columnarData);
		midPoint = new Point2D.Double(Math.abs(columnarData[0][0]), Math.abs(columnarData[0][1]));
	}
	private void read_xyI_column(Scanner s) {
		Vector<double[]> lines = new Vector<double[]>(5000);
		double x, y, I;
		numFilesPerImage = 0;
		do {
			String line = s.nextLine();
			if(line.contains("Diffraction axes")) {
				line = setAxes(s);
				setMaxQVals(line);
				setDeltaQ(s.nextLine()); 
				break;
			} else if(line.contains("numTimes")) {
				readOldFormat(s);
			} else {
				String[] splitLine = line.split("\t");
				if(splitLine.length > 1)  {
					try {
						x = Double.valueOf(splitLine[0]);
						y = Double.valueOf(splitLine[1]);
						I = Double.valueOf(splitLine[2]);
						lines.add(new double[] {x, y, I});
					} catch(NumberFormatException nfe) {
						//nfe.printStackTrace();
					}
				}
			}
		} while(s.hasNextLine());
		
		columnarData = new double[lines.size()][3];
		columnarData = lines.toArray(columnarData);
	}
	public void read() {
		midPoint = null;
		MyFileInputStream mfis = new MyFileInputStream(file);
		Scanner s = mfis.getScanner();
		numFilesPerImage = 0;
		if(isOldFormatXrayFile)
			readOldFormat(s);
		else {
			String line;
			do {
				line = s.nextLine();
				if(line.contains("Diffraction axes")) {
					line = setAxes(s);
					setMaxQVals(line);
					setDeltaQ(s.nextLine()); 
				} else if(line.contains("Summed file names: ")) {
					do {
						line = s.nextLine();
						numFilesPerImage++;
					} while(!line.contains("Summed Xray Data"));
					read_xyI_column(s);
				}
			} while(s.hasNextLine() && line.substring(0, 1).compareTo(commentLineStartsWith) == 0);
			
		}
		mfis.close();
	}
	/* ***************** */
	/* DATA MANIPULATION */
	/* ***************** */
	private void columnarToMatrix() {
		if(columnarData == null)
//			read();
			return;
		
		minX = (int) columnarData[0][0];
		minY = (int) columnarData[0][1];
		maxX = -1 * minX;
		maxY = -1 * minY;
		
		midPoint = new Point2D.Double(maxX, maxY);
		int numX2 = (int) Math.rint(2*(qMaxX/qStep))+1;
		int numY2 = (int) Math.rint(2*(qMaxY/qStep))+1;
		int numX = maxX - minX+1;
		int numY = maxY - minY+1;
		
		matrixData = new double[numX][numY];
		int a;
		int b;
		for(int i = 0; i < columnarData.length; i++) {
			try {
				a = (int) (columnarData[i][0] - minX);
				b = (int) (columnarData[i][1] - minY);
				//System.out.println("a b: " + a + " " + b);
				matrixData[a][b] = columnarData[i][2];
			} catch(ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	public JVector pointToQ(Point p) {
		JVector vx = projections.get(0)[1];
		JVector vy = projections.get(0)[2];
//		vy = new JVector(1, 1, 1);
		double x = p.x - midPoint.x;
		double y = p.y - midPoint.y;
		
		double qx = x * qStep;
		double qy = y * qStep;
		JVector Q = JVector.add(JVector.multiply(vx, qx), JVector.multiply(vy, qy));
//		Q.multiply(1./vx.length());
		Q.multiply(2*Math.PI / 8.82);
		return Q;
//		return JVector.add(JVector.multiply(vx.unit(), x), JVector.multiply(vy.unit(), y));
	}
	public JVector qToHKL(JVector Q, double a) {
		return JVector.multiply(Q, a/2/Math.PI);
	}
	public double[][] normalize(double[][] calcData) {
		double[][] normData = new double[calcData.length][calcData[0].length];
		double scalar = numFilesPerImage * numCalculationsPerFile;
		for(int i = 0; i < normData.length; i++) {
			for(int j = 0; j < normData[i].length; j++) {
				normData[i][j] = calcData[i][j] / scalar;
			}
		}
		
		return normData;
	}
	/* ******************* */
	/* GETTERS AND SETTERS */
	/* ******************* */
	public File getFile() {return file;}
	public void setFile(File file) { this.file = file; }
	public double[][] getColumnarData() {
		if(columnarData == null)
			read();
		return columnarData;
	}
	public void setColumnarData(double[][] data) { this.columnarData = data; }
	public double getqMaxX() { return qMaxX; }
	public void setqMaxX(double qMaxX) { this.qMaxX = qMaxX; }
	public double getqMaxY() { return qMaxY; }
	public void setqMaxY(double qMaxY) { this.qMaxY = qMaxY; }
	public double getqStep() { 	return qStep; }
	public void setqStep(double qStep) { this.qStep = qStep; }
	public Vector<JVector[]> getProjections() { return projections; }
	public void setProjections(Vector<JVector[]> projections) { this.projections = projections; }
	public double[][] getMatrixData() {
		if(matrixData == null)
			columnarToMatrix();
		return matrixData;
	}
	public void setMatrixData(double[][] matrixData) { this.matrixData = matrixData; }
	public int getMinX() {
		if(matrixData == null)
			columnarToMatrix();
		return minX;
	}
	public void setMinX(int minX) { this.minX = minX; }
	public int getMinY() {
		if(matrixData == null)
			columnarToMatrix();
		return minY;
	}
	public void setMinY(int minY) { this.minY = minY; }
	public int getMaxX() {
		if(matrixData == null)
			columnarToMatrix();
		return maxX;
	}
	public void setMaxX(int maxX) { this.maxX = maxX; }
	public int getMaxY() {
		if(matrixData == null)
			columnarToMatrix();
		return maxY;
	}
	public void setMaxY(int maxY) {this.maxY = maxY;}
	public Point2D.Double getMidPoint() { 
		if(midPoint == null) 
			columnarToMatrix();
		return midPoint; 
	}
	public void setMidPoint(Point2D.Double midPoint) { 	this.midPoint = midPoint; }
	public boolean isOldFormatXrayFile() { return isOldFormatXrayFile; }
	public void setOldFormatXrayFile(boolean isOldFormatXrayFile) { this.isOldFormatXrayFile = isOldFormatXrayFile; }

}
