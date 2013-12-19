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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ASC implements ImageFile {
	private final static int FIRST_DATA_ROW = 6;
	private int rows, columns;
	public ASC(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
	}
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
		// reset the row index
		rowIdx = 0;
		return null;
	}
	@Override
	public double[][] readFile(File aFile) {
		return readFile(aFile, new int[] {rows, columns} );
	}

}
