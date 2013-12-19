package dataFileTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SPR implements ImageFile {

	private final static int FIRST_DATA_ROW = 1;
	@Override
	public double[][] readFile(File aFile, int[] bounds) {
		int x0 = bounds[0];
		int y0 = bounds[1];
		int x1 = bounds[2];
		int y1 = bounds[3];
		
		double[][] data = new double[(x1-x0)][(y1-y0)];
		FileInputStream fis = null;
		Scanner s = null;
		
		int rowIdx = 0;
		int columnIdx = 0;
		
		try {
			fis = new FileInputStream(aFile);
			s = new Scanner(fis);
		} catch(FileNotFoundException fnfe) {
			System.err.println(fnfe);
			System.exit(1);
		}
		// skip the header
		while(rowIdx < FIRST_DATA_ROW) { 
			s.nextLine();
			rowIdx++;
		}
		rowIdx = 0;
		// skip to y0
		while(rowIdx < y0) {
			rowIdx++;
			s.nextLine();
		}
		
		while(s.hasNext() && rowIdx < y1) {
			// check to see if i've reached the end of the area of interest
			if(rowIdx == y1) { break; }
			// skip to x0
			while(columnIdx < x0) {
				columnIdx++;
				s.nextDouble();
			}
			try {
				data[rowIdx][columnIdx] = s.nextDouble();
			} catch(NumberFormatException nfe) {
				System.err.println("Error parsing SPR file at: ");
				System.err.println("Row: " + (rowIdx + FIRST_DATA_ROW) + "\tColumn: " + columnIdx);
			}
			columnIdx++;
			// if i'm at x1 then end the loop, increment the row, and reset the column idx to zero 
			if(columnIdx == x1) {
				rowIdx++;
				columnIdx = 0;
			}
		}
		
		return data;
	}
	@Override
	public double[][] readFile(File aFile) {
		return readFile(aFile, new int[] {0, 1000});
	}

	
}
