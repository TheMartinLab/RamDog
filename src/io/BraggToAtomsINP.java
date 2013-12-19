/**
 * @author Eric D. Dill eddill@ncsu.edu
 * @author James D. Martin jdmartin@ncsu.edu
 * Copyright © 2010-2013 North Carolina State University. All rights reserved
 */
package io;

import geometry.JVector;

import java.awt.Color;
import java.io.File;
import java.util.Vector;

import analysis.Spot;

/**
 * @author Eric
 *
 */
public class BraggToAtomsINP {

	private JVector[] colorVectors;
	private Color[] colors;
	private int numColorSteps;
	private JVector imageCenter;
	private double radius = 1.5;
	private JVector[] reciprocalLatticeVectorFamilies;
	private ColorMethod coloringType;
	enum ColorMethod {
		BY_FRAME,
		BY_LATTICE_VECTOR,
		;
	}
	/**
	 * ATOMS INP KEYWORDS
	 * LAB: (char)  6 characters for atom label
	 * COO: (float) 3 floating point fields for atomic coordinates
	 * TYP: (int)   Atom type for locating bonds and polyhedra
	 * RAD: (float) atom radius when plotted as sphere
	 * RMC: (int)   color for atom rim or edge for three RGB components 0-255
	 * FLC: (int)   color for atom fill, three RGB components 0-255
	 * DUM: (field) character/integer/float that exists in the input line but should be ignored by atoms
	 * 
	*/
	
	private final static String atomLabel = "LAB";
	private final static String atomCoords = "COO";
	private final static String atomType = "TYP";
	private final static String atomRadius = "RAD";
	private final static String atomRimColor = "RMC";
	private final static String atomFillColor = "FLC";
	private final static String atomDummy = "DUM";
	
	private String[] header = new String[] {atomLabel, atomCoords, 
			atomType, atomRadius, atomFillColor};
	
	public double[][] spotRadii(Spot[][] spots) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double I;
		for(int i = 0; i < spots.length; i++) {
			for(int j = 0; j < spots[i].length; j++) {
				I = spots[i][j].getI();
				if(I > max)
					max = I;
				if(I < min)
					min = I;
			}
		}
		
		double minR = 0.5;
		double maxR = 3;
		double[][] radii = new double[spots.length][];
		for(int i = 0; i < spots.length; i++) {
			radii[i] = new double[spots[i].length];
			for(int j = 0; j < spots[i].length; j++) {
				I = spots[i][j].getI();
				radii[i][j] = (I - min) / (max - min) * (maxR - minR) + minR;
			}
		}
		return radii;
	}
	public void SpotsToINP(Spot[][] spot, Double[] angle, JVector rotationAxis, File inpFile) {
		// rotate spots
		JVector[][] coordinates = rotate(spot, angle, rotationAxis);
		// set up atomic colors and outlines
		numColorSteps = angle.length;
		Color[] colors = this.colors;
		if(colors == null)
			colors = getColors();
		// print to atoms file
		MyPrintStream mps = new MyPrintStream(inpFile);
		mps.println("FIELDS " + StringConverter.arrayToTabString(header));
		mps.println(getOriginLine());
		JVector family;
		JVector imageHandle = new JVector(0, 0, 100);
		double[][] radii = spotRadii(spot);
		for(int i = 0; i < coordinates.length; i++) {
//			mps.println(getAxisLine(JVector.rotate(imageHandle, rotationAxis, imageCenter, angle[i]), 
//					angle[i], colors[i%colors.length]));
			for(int j = 0; j < coordinates[i].length; j++) {
				if(spot[i][j].getAssignedFamily() == null)
					family = spot[i][j].getAssignedReflection();
				else
					family = spot[i][j].getAssignedFamily().get(0).getKey();
				mps.println(getAtomsLine(radii[i][j], coordinates[i][j], family, angle[i], i));
			}
		}
		mps.println("DEFAULT " + atomRadius + "=" + radius);
		mps.println(getAxisLine(JVector.multiply(rotationAxis, -50), 0, Color.cyan));
		mps.println(getAxisLine(JVector.multiply(rotationAxis, 50), 0, Color.cyan));
		mps.close();
	}
	
	private String getAxisLine(JVector point, double angle, Color color) {
		String line = "";
		for(String keyword : header) {
			if(keyword.compareTo(atomLabel) == 0) {
				line += "AXS" + angle + " ";
			} else if(keyword.compareTo(atomCoords) == 0)
				line += point.i + " " + point.j + " " + point.k + " ";
			else if(keyword.compareTo(atomType) == 0)
				line += "0 ";
			else if(keyword.compareTo(atomRadius) == 0)
				line += radius * 3 + " ";
			else if(keyword.compareTo(atomFillColor) == 0)
				line += color.getRed() + " " + color.getGreen() + " " + color.getBlue() + " ";
			else if(keyword.compareTo(atomRimColor) == 0)
				line += "0 0 0 ";
			else if(keyword.compareTo(atomDummy) == 0)
				;
		}
		return line;
	}
	private String getOriginLine() {
		String line = "";
		for(String keyword : header) {
			if(keyword.compareTo(atomLabel) == 0) {
				line += "ORIGIN ";
			} else if(keyword.compareTo(atomCoords) == 0)
				line += "0 0 0 ";
			else if(keyword.compareTo(atomType) == 0)
				line += "0 ";
			else if(keyword.compareTo(atomRadius) == 0)
				line += radius * 2 + " ";
			else if(keyword.compareTo(atomFillColor) == 0)
				line += "0 255 255";
			else if(keyword.compareTo(atomRimColor) == 0)
				line += "0 0 0 ";
			else if(keyword.compareTo(atomDummy) == 0)
				;
		}
		return line;
	}
	private String getAtomsLine(double radius, JVector coords, JVector family, double angle, int frame) {
		String line = "";
		for(String keyword : header) {
			if(keyword.compareTo(atomLabel) == 0) {
				JVector intCoords = family.roundInt();
				String atomLabel = (int) intCoords.getI() + "" + (int)intCoords.getJ() + "" + (int)intCoords.getK() + "";
				line += atomLabel + angle + " ";
			} else if(keyword.compareTo(atomCoords) == 0)
				line += coords.toString(3) + " ";
			else if(keyword.compareTo(atomType) == 0)
				line += "0 ";
			else if(keyword.compareTo(atomRadius) == 0)
				line += radius + " ";
			else if(keyword.compareTo(atomFillColor) == 0) {
				Color color = getColorByMethod(frame, family);
				line += color.getRed() + " " + color.getGreen() + " " + color.getBlue() + " ";
			}
			else if(keyword.compareTo(atomRimColor) == 0)
				line += "0 0 0 ";
			else if(keyword.compareTo(atomDummy) == 0)
				;
		}
		return line;
	}

	private Color getColorByMethod(int frame, JVector recip) {
		switch(coloringType) {
		case BY_FRAME:
			return colors[frame%colors.length];
		case BY_LATTICE_VECTOR:
			int idx = 0;
			for(JVector vec : reciprocalLatticeVectorFamilies) {
				if(JVector.isSameFamily(recip, vec, 3))
					return colors[idx%colors.length];
				idx++;
			}
			break;
		}
		return Color.black;
	}
	private JVector[][] rotate(Spot[][] spots, Double[] angles, JVector rotationAxis) {
		JVector pos;
		Vector<JVector> imagePositions;
		JVector[][] positions = new JVector[spots.length][];
		for(int i = 0; i < spots.length; i++) {
			imagePositions = new Vector<JVector>();
			for(Spot spot : spots[i]) {
				pos = new JVector(spot.getY(), spot.getX(), 0);
				pos = JVector.subtract(pos, imageCenter);
				pos = JVector.rotate(pos, rotationAxis, JVector.zero, angles[i]);
				imagePositions.add(pos);
			}
			positions[i] = new JVector[imagePositions.size()];
			imagePositions.toArray(positions[i]);
		}
		return positions;
	}

	public JVector getImageCenter() { return imageCenter; }
	public void setImageCenter(JVector imageCenter) { this.imageCenter = imageCenter; }
	public void setColors(Color[] colors) { this.colors = colors; }
	public Color[] getColors() { return colors; }
	public JVector[] getReciprocalLatticeVectorFamilies() {
		return reciprocalLatticeVectorFamilies;
	}
	public void setReciprocalLatticeVectorFamilies(
			JVector[] reciprocalLatticeVectorFamilies) {
		this.reciprocalLatticeVectorFamilies = reciprocalLatticeVectorFamilies;
	}
	public ColorMethod getColoringType() {
		return coloringType;
	}
	public void setColoringType(ColorMethod coloringType) {
		this.coloringType = coloringType;
	}
}
