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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Vector;

import analysis.CorrelatedSpots;
import analysis.Pixel;
import analysis.Spot;

public class FileIO {

	private static boolean deleteFile(File aFile) {
		return aFile.delete();
	}
	private static void deleteFolder(File aFolder) {
		File[] filesInFolder = aFolder.listFiles();
		// if filesInFolder is null then aFolder is actually aFile
		if(filesInFolder == null) { 
			deleteFile(aFolder);
			return;
		}
		for(int i = 0; i < filesInFolder.length; i++) {
			if(!deleteFile(filesInFolder[i])) { 
				deleteFolder(filesInFolder[i]); 
			}
		}
		deleteFile(aFolder);
	}
	public static void delete(File fileOrFolder) {
		deleteFolder(fileOrFolder);
	}
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	/**
	 * Print out one pixel through time
	 * @param pix	A one dimensional array of pixels is one pixel through the selected frames
	 * @param write	The output file
	 */
	public static synchronized void writeToFile(Pixel[] pix, File write) {
		System.out.println("Writing a 1d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		ps.println("Pixel_x\t" + pix[0].getXCoordinate());
		ps.println("Pixel_y\t" + pix[0].getYCoordinate());
		for(int i = 0; i < pix.length; i++) {
			ps.println(pix[i].getFrameNumber() + "\t" + pix[i].getIntensity());
		}
		close(ps);
		close(fos);
	}
	/**
	 * Print out the given pixels through time
	 * @param pix	The first dimension are the pixels and the second dimension are the values through time
	 * @param write	The output file
	 */
	public static synchronized void writeToFile(Pixel[][] pix, File write) {
		System.out.println("Writing a 2d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		
		String line1 = "PixelSpotNumber\t";
		String line2 = "Pixel_x\t";
		String line3 = "Pixel_y\t";
		for(int j = 0; j < pix[0].length; j++) {
			line2 += pix[0][j].getXCoordinate() + "\t";
			line3 += pix[0][j].getYCoordinate() + "\t";
		}
		ps.println(line1);
		ps.println(line2);
		ps.println(line3);
		ps.flush();
		for(int i = 0; i < pix.length; i++) {
			ps.print(pix[i][0].getFrameNumber() + "\t");
			for(int j = 0; j < pix[i].length; j++) {
				ps.print(pix[i][j].getIntensity() + "\t");
			}
			ps.print("\n");
		}
		close(ps);
		close(fos);
	}
	public static synchronized void writeToFileXYI(Pixel[] pix, File write) {
		System.out.println("Writing a 1d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		ps.println(Pixel.getHeader());
		// TODO set the phi value in each pixel before printing out. Ideally this should be done when the path is created...
		for(Pixel pixel : pix)
			ps.println(pixel);
		
		close(ps);
		close(fos);
	}
	public static synchronized void writeToFile(Pixel[] pix, double[][] I, File write) {
		System.out.println("Writing a 1d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		ps.println("x\ty\tFrame Number");
		String[] s = new String[pix.length + I[0].length + 1];
		s[0] = "x\ty\t";
		for(int i = 0; i < pix.length; i++) {
			s[i+1] = pix[i].getXCoordinate() + "\t" + pix[i].getYCoordinate() + "\t";
		}
		for(int i = 0; i < I.length; i++) {
			s[0] += (i + "\t");
			for(int j = 0; j < I[i].length; j++) {
				s[j+1] += (I[i][j] + "\t");
			}
		}
		for(int i = 0; i < s.length; i++) {
			ps.println(s[i]);
		}
		close(ps);
		close(fos);
	}
	public static synchronized void write2DArrToFile(double[][] arr, File f) {
		MyPrintStream mps = new MyPrintStream(f);
		for(int i = 0; i < arr.length; i++) {
			mps.println(StringConverter.arrayToTabString(arr[i]));
		}
		mps.close();
		System.out.println("normalized written to file: " + f.getName());
	}
	public static synchronized void write2DArrToFile(int[][] arr, File f, String firstLine) {
		MyPrintStream mps = new MyPrintStream(f);
		mps.println(firstLine);
		for(int i = 0; i < arr.length; i++) {
			mps.println(StringConverter.arrayToTabString(arr[i]));
		}
		mps.close();
		System.out.println("normalized written to file: " + f.getName());
	}
	public static synchronized void writeIntegratedToFile(double[][] I, File write) {
		System.out.println("Writing a 1d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		String[] s = new String[I[0].length + 1];
		Arrays.fill(s, "");
		for(int i = 0; i < I.length; i++) {
			// fill in the spot/region/path index
			s[0] += (i + "\t");
			// 
			for(int j = 0; j < I[i].length; j++) {
				s[j+1] += (I[i][j] + "\t");
			}
		}
		for(int i = 0; i < s.length; i++) {
			ps.println(s[i]);
		}
		close(ps);
		close(fos);
	}
	/**
	 * A 1d array of spots is one frame of spots
	 * @param spots	One frame of spots
	 * @param write	The output file name
	 */
	public static synchronized void writeToFile(Vector<Spot> spots, File write) {
		System.out.println("Writing a 1d spot array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		String s1 = "Spot Number\t";
		String s2 = "Number of pixels\t";
		String s3 = "Spot Avg X\t";
		String s4 = "Spot Avg Y\t";
		String s5 = "Total spot intensity\t";
		for(int i = 0; i < spots.size(); i++) {
			spots.get(i).calculateSpotProperties();
			s1 += spots.get(i).getSpotNumber() + "\t";
			s2 += spots.get(i).getNumPixels() + "\t";
			s3 += spots.get(i).getX() + "\t";
			s4 += spots.get(i).getY() + "\t";
			s5 += spots.get(i).getI() + "\t";
		}
		
		ps.println(s1);
		ps.println(s2);
		ps.println(s3);
		ps.println(s4);
		ps.println(s5);

		close(ps);
		close(fos);
	}
	public static synchronized void writeToFile(CorrelatedSpots correlated, File write, int numFiles) {
		System.out.println("Writing a 2d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		ps.println("SpotFrame\tSpot_x\tSpot_y");
		Spot[] theSpots = correlated.getSpots();
		for(int i = 0; i < theSpots.length; i++) {
			ps.print(i + "\t");
			ps.print(theSpots[i].getX() + "\t");
			ps.print(theSpots[i].getY() + "\n");
		}
		close(ps);
		close(fos);
	}
	public static synchronized void writeToFile(CorrelatedSpots[] correlated, File write, int numFiles) {
		System.out.println("Writing a 2d pixel array to: " + write);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(write);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream ps = new PrintStream(fos);
		String[] lines = new String[3];
		
		lines[0] = "PixelSpotNumber\t";
		lines[1] = "Pixel_x\t";
		lines[2]= "Pixel_y\t";
		for(int i = 0; i < correlated.length; i++) {
			correlated[i].calcProperties();
			lines[0] += i + "\t";
			lines[1] += correlated[i].getX() + "\t";
			lines[2] += correlated[i].getY() + "\t";
		}
		for(int i = 0; i < lines.length; i++) {
			ps.println(lines[i]);
		}
		ps.flush();
		for(int j = 0; j < numFiles; j++) {
			ps.print(j + "\t");
			for(int i = 0; i < correlated.length; i++) {
				ps.print(correlated[i].getI(j) + "\t");
			}
			ps.println();
		}
		close(ps);
		close(fos);
	}
	private static void close(FileOutputStream fos) { 
		try {
		fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void close(PrintStream ps) { ps.close(); }
}
