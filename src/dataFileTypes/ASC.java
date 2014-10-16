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

public class ASC implements ImageFile {
	private final static int FIRST_DATA_ROW = 6;
	private int rows, columns;
	public ASC(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
	}
	@Override
	public double[][] readFile(File aFile, int[] bounds) {
		throw new UnsupportedOperationException("readFile in class ASC is not implemented");
		/*
		int rowIdx = 0;
		MyFileInputStream mfis = new MyFileInputStream(aFile);
		Scanner s = mfis.getScanner();
		// skip the header
		while(rowIdx < FIRST_DATA_ROW) { 
			s.nextLine();
			rowIdx++;
		}
		// read the rest of the data file
		// reset the row index
		rowIdx = 0;
		mfis.close();
		return null;
		*/
	}
	@Override
	public double[][] readFile(File aFile) {
		return readFile(aFile, new int[] {rows, columns} );
	}

}
