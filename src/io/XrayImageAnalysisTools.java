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
package io;

import geometry.JVector;
import io.BraggToAtomsINP.ColorMethod;

import java.awt.Color;
import java.io.File;
import java.util.Scanner;
import java.util.Vector;

import Lists.Pair;
import analysis.Spot;

public class XrayImageAnalysisTools {

	private JVector[] allowedHKL;
	private JVector[] allowedQ;
	private double a;

	public XrayImageAnalysisTools(JVector[] allowedHKL, JVector[] allowedQ) {
		this.allowedHKL = allowedHKL;
		this.allowedQ = allowedQ;
	}
	
	public Spot[] readSpotFile(File f) {
		Vector<Spot> spots = new Vector<Spot>();
		
		MyFileInputStream mfis = new MyFileInputStream(f);
		Scanner s = mfis.getScanner();
		
		// skip header in file
		s.nextLine();
		
		// parse spot data
		String line, splitLine[];
		Spot spot;
		double x0, y0, I, q0, phi0;
		while(s.hasNextLine()) {
			line = s.nextLine();
			splitLine = line.split("\t");
			x0 = Double.valueOf(splitLine[0]);
			y0 = Double.valueOf(splitLine[1]);
			I  = Double.valueOf(splitLine[2]);
			q0 = Double.valueOf(splitLine[3]);
			phi0 = Double.valueOf(splitLine[4]);
			spot = new Spot(x0, y0, I);
			spot.setQ(q0);
			spot.setPhi(phi0);
			spots.add(spot);
		}
		
		return spots.toArray(new Spot[spots.size()]);
	}
	public void determineReflectionFamily(Spot[] spots) {
		int familyIdx;
		
		Vector<JVector> hklVals = new Vector<JVector>();
		Vector<Double> sortingVals = new Vector<Double>();
		
		for(Spot spot : spots) {
			double spotQ = spot.getQ();
			for(JVector q : allowedQ) {
				double qLen = q.length();
				double qDiff = Math.abs(spotQ - qLen);
				insert(hklVals, sortingVals, q, qDiff);
			}
			
			double[] confidence = confidence(sortingVals);
			System.out.println("\nConfidence values for Spot: " + spot);
			int numVals = confidence.length > 4 ? 5 : confidence.length;
			double totalConfidence = 0;
			int i = 0;
			for(i = 0; i < numVals; i++) {
				JVector q = hklVals.get(i);
				System.out.println(confidence[i] + "\t" + XrayImageAnalysisTools.QToHKL(q, a).roundInt() + "\t" + q);
				if(confidence[i] / confidence[i+1] > 1.25)
					break;
			}
			for(int j = 0; j <= i; j++) {
				spot.addAssignedFamily(hklVals.get(j), confidence[j]);
			}
			hklVals.clear();
			sortingVals.clear();
		}
	}
	private double[] confidence(Vector<Double> qList) {
		double inverseSum = 0;
		for(Double d : qList) {
			inverseSum += Math.pow(1./d, 4);
		}
		double[] conf = new double[qList.size()];
		int idx = 0;
		for(Double d : qList) {
			conf[idx++] = Math.pow(1./d, 4) / inverseSum * 100;
		}
		return conf;
	}
	private void insert(Vector<JVector> hklList, Vector<Double> qList, JVector hkl, Double q) {
		if(hklList.size() == 0) {
			hklList.add(hkl);
			qList.add(q);
			return;
		}
		Double qCompare;
		int listSize = hklList.size();
		for(int i = 0; i < listSize; i++) {
			qCompare = qList.get(i);
			
			if(q < qCompare) {
				hklList.insertElementAt(hkl, i);
				qList.insertElementAt(q, i);
				return;
			}
		}
		hklList.add(hkl);
		qList.add(q);
	}
	
	public static JVector[] getAllowedFCCReflections() {
		Vector<JVector> allowedFCC = new Vector<JVector>();
		
		allowedFCC.add(new JVector(1, 1, 1));
		allowedFCC.add(new JVector(2, 0, 0));
		allowedFCC.add(new JVector(2, 2, 0));
		allowedFCC.add(new JVector(3, 1, 1));
		allowedFCC.add(new JVector(2, 2, 2));
		allowedFCC.add(new JVector(4, 0, 0));
		allowedFCC.add(new JVector(4, 2, 0));
		allowedFCC.add(new JVector(4, 2, 2));
		allowedFCC.add(new JVector(3, 3, 3));
		allowedFCC.add(new JVector(5, 1, 1));
		allowedFCC.add(new JVector(4, 4, 0));
		allowedFCC.add(new JVector(5, 3, 1));
		allowedFCC.add(new JVector(6, 0, 0));
		allowedFCC.add(new JVector(6, 2, 0));
		allowedFCC.add(new JVector(5, 3, 3));
		allowedFCC.add(new JVector(6, 2, 2));
		allowedFCC.add(new JVector(4, 4, 4));
		allowedFCC.add(new JVector(7, 1, 1));
		allowedFCC.add(new JVector(6, 4, 0));
		allowedFCC.add(new JVector(6, 4, 2));
		allowedFCC.add(new JVector(7, 3, 1));
		allowedFCC.add(new JVector(8, 0, 0));
		allowedFCC.add(new JVector(7, 3, 3));
		allowedFCC.add(new JVector(6, 4, 4));
		allowedFCC.add(new JVector(8, 2, 2));
		allowedFCC.add(new JVector(6, 6, 0));
		allowedFCC.add(new JVector(5, 5, 5));
		
		return allowedFCC.toArray(new JVector[allowedFCC.size()]);
	}
	public static JVector[] allowedHKLToQ(JVector[] allowed, double a) {
		JVector[] qVec = new JVector[allowed.length];
		for(int i = 0; i < qVec.length; i++) {
			qVec[i] = allowedHKLToQ(allowed[i], a);
		}
		return allowed;
	}	
	public static JVector allowedHKLToQ(JVector allowed, double a) {
		double q;
		q = 2 * Math.PI * allowed.length() / a;
		JVector vec = allowed.unit();
		vec.multiply(q);
		return vec;
	}	
	private Double[] angles;
	public Spot[][] parseTextFile(File f) {
		MyFileInputStream mfis = new MyFileInputStream(f);
		Scanner s = mfis.getScanner();
		Vector<Double> angles = new Vector<Double>();
		Vector<Spot> frame = new Vector<Spot>();
		Vector<Spot[]> allSpots = new Vector<Spot[]>();
		double curAngle = 0, prevCurAngle = 0;
		String line, splitLine[];
		String[] header = s.nextLine().split("\t");
		boolean newFrame = false;
		boolean firstFrame = true;
		int atomIdx = 0;
		while(s.hasNextLine()) {
			int h=0, k=0, l=0;
			double x=0, y=0, I=0, Q=0, phi=0;
			String notes="", unknownAssignment = "";
			line = s.nextLine();
			splitLine = line.split("\t");
			try {
				for(int i = 0; i < header.length && i < splitLine.length; i++) {
					if(header[i].compareTo("unknown assignment") == 0)
						unknownAssignment = splitLine[i];
					else if(header[i].compareTo("frame") == 0) {
						if(atomIdx >0  && curAngle != 0 && prevCurAngle != curAngle) {
							angles.add(prevCurAngle);
							newFrame = true;
						}
						prevCurAngle = curAngle;
						curAngle = Double.valueOf(splitLine[i]);
					}
					else if(header[i].compareTo("h") == 0)
						h = Integer.valueOf(splitLine[i]);
					else if(header[i].compareTo("k") == 0)
						k = Integer.valueOf(splitLine[i]);
					else if(header[i].compareTo("l") == 0)
						l = Integer.valueOf(splitLine[i]);
					else if(header[i].compareTo("x") == 0)
						y = Double.valueOf(splitLine[i]);
					else if(header[i].compareTo("y") == 0)
						x = Double.valueOf(splitLine[i]);
					else if(header[i].compareTo("I") == 0)
						I = Double.valueOf(splitLine[i]);
					else if(header[i].compareTo("Q") == 0)
						Q = Double.valueOf(splitLine[i]);
					else if(header[i].compareTo("phi") == 0)
						phi = Double.valueOf(splitLine[i]);
					else if(header[i].compareTo("notes") == 0)
						notes = splitLine[i];
				}
				if(newFrame && frame.size() > 0) {
					Spot[] arr = new Spot[frame.size()];
					arr = frame.toArray(arr);
					allSpots.add(arr);
					frame.clear();
					newFrame = false;
				}
				if(unknownAssignment.compareTo("") == 0) {
					Spot spot = new Spot(x, y, I);
					spot.setAssignedReflection(new JVector(h, k, l));
					spot.setQ(Q);
					spot.setPhi(phi);
					frame.add(spot);
				}
			} catch(NumberFormatException e) {
				System.err.println("hkl not selected at line: " + line);
			}
			atomIdx++;
		}
		angles.add(prevCurAngle);
		Spot[] arr = new Spot[frame.size()];
		arr = frame.toArray(arr);
		allSpots.add(arr);
		Spot[][] spots = new Spot[allSpots.size()][];
		int idx = 0;
		while(!allSpots.isEmpty()) {
			spots[idx++] = allSpots.remove(0);
		}
		this.angles = new Double[angles.size()];
		this.angles = angles.toArray(this.angles);
		return spots;
	}
	public static JVector QToHKL(JVector allowed, double a) {
		double q100 = 2 * Math.PI / a;
		JVector vec = (JVector) allowed.clone();
		vec.divide(q100);
		return vec;
	}
	public static void main(String[] args) {
		File spotsFile = new File("D:\\Data\\CBr4\\cbr4_singlextal_rotate190_50deg_2s_90kev_203\\selected bragg reflections\\134_summary");
		JVector[] allowedFCC = getAllowedFCCReflections();
		double a = 8.82;
		JVector[] allowedQ = allowedHKLToQ(allowedFCC, a);
		for(JVector vec : allowedFCC)
			System.out.println(vec);
		XrayImageAnalysisTools tools = new XrayImageAnalysisTools(allowedFCC, allowedQ);
		tools.a = a;
		Spot[] spots = tools.readSpotFile(spotsFile);
		
		System.out.println("\nSpots:");
		for(Spot spot : spots)
			System.out.println(spot);
		
		tools.determineReflectionFamily(spots);	
		spots[1].setAssignedFamily(null);
		
		spots[1].addAssignedFamily(allowedHKLToQ(new JVector(2, 2, 2), a), 100.);
		
		
		System.out.println("\nSpots:");
		for(Spot spot : spots) {
			Pair<JVector, Double> pair = spot.getAssignedFamily().get(0);
			System.out.println(spot + "\t" + pair.getKey().length() + "\t" + pair.getKey() + "\t" + pair.getValue());
		}
		

		JVector imageCenter = new JVector(1033.321, 1020.208, 0);
		File spotsFolder = new File("D:\\$research\\current\\projects\\plastic crystals\\CBr4\\aps_jan_09\\cbr4_singlextal_rotate190_50deg_2s_90kev_203\\data\\selected bragg reflections");
		File inpFile = new File(spotsFolder + File.separator + "rotated.inp");
		File[] filesInFolder = spotsFolder.listFiles();
		double rotationAngle;
		Vector<File> summaryFiles = new Vector<File>();
		for(File file : filesInFolder)
			if(file.getName().contains("summary"))
				summaryFiles.add(file);
		
		Spot[][] allSpots = new Spot[summaryFiles.size()][];
		double[] angles = new double[summaryFiles.size()];
		for(int i = 0; i < summaryFiles.size(); i++) {
			File file = summaryFiles.get(i);
			allSpots[i] = tools.readSpotFile(file);
			tools.determineReflectionFamily(allSpots[i]);
			angles[i] = Double.valueOf(file.getName().substring(0,file.getName().indexOf("_")));
		}
		spotsFile = new File("D:\\$research\\current\\projects\\plastic crystals\\CBr4\\aps_jan_09\\cbr4_singlextal_rotate190_50deg_2s_90kev_203\\selectedBragg.txt");
		allSpots =  tools.parseTextFile(spotsFile);
		Vector<Color> colors = new Vector<Color>();
		Color c1 = Color.red;
		Color c2 = Color.blue;
		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();
		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();
		JVector v1 = new JVector(r1, g1, b1);
		JVector v2 = new JVector(r2, g2, b2);
		JVector v1_v2 = JVector.subtract(v2, v1);
		double distance = v1_v2.length();
		int numSteps = allSpots.length;
		JVector colorStep = JVector.multiply(v1_v2.unit(), distance / numSteps);
		JVector curColor = v1;
		int r, g, b;
		colors.add(c1);
		for(int i = 0; i < numSteps; i++) {
			curColor = JVector.add(curColor, colorStep);
			r = (int) Math.rint(curColor.i);
			g = (int) Math.rint(curColor.j);
			b = (int) Math.rint(curColor.k);
			colors.add(new Color(r, g, b));
		}
		colors.add(c2);
		Color[] colorArr = new Color[colors.size()];
		colorArr = colors.toArray(colorArr);
		
		BraggToAtomsINP bragg = new BraggToAtomsINP();
		bragg.setColors(colorArr);
		bragg.setImageCenter(imageCenter);
		bragg.setReciprocalLatticeVectorFamilies(allowedFCC);
		bragg.setColoringType(ColorMethod.BY_FRAME);
		JVector rotationAxis = new JVector(0, 1, 0);
		bragg.SpotsToINP(allSpots, tools.angles, rotationAxis, inpFile);
	}
}
