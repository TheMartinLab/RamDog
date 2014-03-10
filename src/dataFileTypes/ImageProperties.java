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

import io.MyFileInputStream;

import java.io.File;
import java.util.Scanner;

public class ImageProperties {
	private int bytesPerEntry = 2;
	private int headerSize = 4096;
	private int endianType = BIN.LITTLE_ENDIAN;
	private int[] dimensions;
	public ImageProperties(int bytesPerEntry, int headerSize, int endianType, int[] dimensions) {
		setBytesPerEntry(bytesPerEntry);
		setHeaderSize(headerSize);
		setEndianType(endianType);
		setDimensions(dimensions);
	}
	public ImageProperties() {
		setDefaults(null);
	}
	public int getBytesPerEntry() { return bytesPerEntry; }
	public void setBytesPerEntry(int bytesPerEntry) {
		this.bytesPerEntry = bytesPerEntry;
	}
	
	public int getHeaderSize() { return headerSize; }
	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}
	
	public int getEndianType() { 
		return endianType; 
	}
	public void setEndianType(int endianType) {
		this.endianType = endianType;
	}
	
	public int[] getDimensions() { return dimensions; }
	public void setDimensions(int[] dim) {
		dimensions = new int[dim.length];
		for(int i = 0; i < dim.length; i++) {
			dimensions[i] = dim[i];
		}
	}
	public void setSPEDefaults() {
		bytesPerEntry = 4;
		headerSize = 4096;
//		endianType = BIN.LITTLE_ENDIAN;
		dimensions = new int[] {2084, 2084};
	}
	
	public void setCORDefaults() {
		bytesPerEntry = 2;
		headerSize = 4096;
//		endianType = BIN.LITTLE_ENDIAN;
		dimensions = new int[] {2048, 2048};
	}
	public void setDefaults(File f) {
		long fileSize = (long) Math.pow(512, 2);
		if(f != null) {
			fileSize = f.length();
			fileSize -= headerSize;
		}
		int size = (int) Math.rint(Math.sqrt(((double) fileSize) / ((double) bytesPerEntry))) + 1;
		dimensions = new int[] {size, size};
	}
	private int[] fileDimensions; // in pixels
	private int[] dataSizes; // in bytes
	
	private int[] estimateFileProperties(File f) {
		if(fileDimensions == null || dataSizes == null)
			parseCommonProperties();
		
		if(fileDimensions == null && dataSizes == null)
			return new int[] {0, 0, 0};
		
		if(fileDimensions == null)
			return estimateWithoutFileDims(f);
		
		if(dataSizes == null)
			return estimateWithoutDataSizes(f);
		
		return null;
	}
	
	private int[] estimateWithoutFileDims(File f) {
		long fileBytes = f.length();
		
		return null;
		
	}
	
	private int[] estimateWithoutDataSizes(File f) {
		long fileBytes = f.length();
		
		return null;
	}
	
	private void parseCommonProperties() {
		File inputFile = new File("settings" + File.separator + "CommonXrayProperties.txt");
		if(inputFile.exists()) {
			MyFileInputStream mfis = new MyFileInputStream(inputFile);
			Scanner s = mfis.getScanner();
			// lines with '%' at the beginning are comment lines
			// lines with spaces at the beginning are comment lines
			// the keyword "dims" is the line that indicates which image dimensions are legitimate to estimate the file size with
			// the keyword "dims" is the line that indicates which data element sizes can be used.
			String delimiter = " ";
			String line = "";
			String firstChar;
			String[] splitLine;
			while(s.hasNextLine()) {
				line = s.nextLine();
				firstChar = line.substring(0, 1);
				if(firstChar.compareTo("%") != 0 || firstChar.compareTo(" ") != 0) {
					splitLine = line.split(delimiter);
					if(splitLine[0].compareTo("dims") == 0) {
						fileDimensions = new int[splitLine.length-1];
						for(int i = 0; i < fileDimensions.length; i++)
							fileDimensions[i] = Integer.valueOf(splitLine[i+1]);
					} else if(splitLine[0].compareTo("size") == 0) {
						dataSizes = new int[splitLine.length-1];
						for(int i = 0; i < dataSizes.length; i++)
							dataSizes[i] = Integer.valueOf(splitLine[i+1]);
					}
				}
			}
		}
	}
	
}
