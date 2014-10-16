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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BIN implements ImageFile {

	private int columns;
	private int rows;
	private int bytesPerEntry;
	private int bytesToSkip;
	private final static int BITS_PER_BYTE = 8;
	public final static int LITTLE_ENDIAN = 0;
	public final static int BIG_ENDIAN = 1;
	private int endianType;
	public BIN(ImageProperties prop) {
		int[] dim = prop.getDimensions();
		if(dim.length == 1) {
			rows = dim[0];
			columns = dim[0];
		} else {
			rows = dim[0];
			columns = dim[1];
		}
		bytesPerEntry = prop.getBytesPerEntry();
		bytesToSkip = prop.getHeaderSize();
		endianType = prop.getEndianType();
	}
	@Override
	public double[][] readFile(File aFile) {
		return readFile(aFile, new int[] {0, 0, rows, columns});
	}
	@Override
	public double[][] readFile(File aFile, int[] bounds) {
		int x0 = bounds[0];
		int y0 = bounds[1];
		int x1 = bounds[2];
		int y1 = bounds[3];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(aFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] buffer = new byte[bytesPerEntry*columns];
		byte[] theBytes = new byte[bytesPerEntry];
		
		try {
			bis.read(new byte[bytesToSkip]);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		double[][] data = new double[x1-x0][y1-y0];
		for(int i = y0; i < y1; i++) {
			try {
				bis.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for(int j = x0; j < x1; j++) {
				for(int b = 0; b < bytesPerEntry; b++) {
					theBytes[b] = (buffer[bytesPerEntry*j+b]);
				}

				switch(endianType) {
				case LITTLE_ENDIAN:
					data[i][j] = littleEndian(theBytes);
					break;
				case BIG_ENDIAN: 
					data[i][j] = bigEndian(theBytes);
					break;
				default:
					data[i][j] = littleEndian(theBytes);
				}
				
			}
		}
		try {
			bis.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	private int littleEndian(byte[] theBytes) {
		int curValue = 0;
		int[] byteOrder = new int[theBytes.length];
		for(int i = 0; i < theBytes.length; i++) {
			byteOrder[i] = (int) ((theBytes[i] & 0xFF) * Math.pow(2, BITS_PER_BYTE*i)); 
		}
		for(int i = 0; i < byteOrder.length; i++) {
			curValue += byteOrder[i];
		}
		if(curValue < 0) {
			return 0;
		}
		return curValue;
	}
	private int bigEndian(byte[] theBytes) {
		int curValue = 0;
		int[] byteOrder = new int[theBytes.length];
		for(int i = 0; i < theBytes.length; i++) {
			byteOrder[i] = (int) ((theBytes[i] & 0xFF) * Math.pow(2, BITS_PER_BYTE*(bytesPerEntry-i))); 
		}
		for(int i = 0; i < byteOrder.length; i++) {
			curValue += byteOrder[i];
		}
		if(curValue < 0) {
			return 0;
		}
		return curValue;
	}
}
