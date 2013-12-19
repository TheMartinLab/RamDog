package dataFileTypes;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

public class CalculatedXrayCollection {

	private Vector<CalculatedXrayFile> files;
	private double[][] columnarData, matrixData;
	public CalculatedXrayCollection(Vector<CalculatedXrayFile> calculatedXray) {
		files = new Vector<CalculatedXrayFile>();
		for(CalculatedXrayFile f : calculatedXray)
			files.add(f);
	}
	
	/**
	 * Read in the data from the calculated xray files list
	 * @param forceRead default behavior is to not reread all the data if it has already been read in once.<br>
	 * a value of true will override this behavior and force the data to be read in again.
	 */
	public void readFiles(boolean forceRead) {
		if(columnarData == null || forceRead) {
			for(CalculatedXrayFile calc : files) {
				calc.read();
				if(columnarData == null)
					columnarData = calc.getColumnarData();
				else
					addToColumnar(calc.getColumnarData());
			}
		}
	}
	
	private void addToColumnar(double[][] data) {
		for(int i = 0; i < data.length; i++) {
			columnarData[i][2] += data[i][2];
		}
	}
	
	private void compileMatrixData() {
		for(CalculatedXrayFile calc : files) {
			if(matrixData == null)
				matrixData = calc.getMatrixData();
			else
				addToMatrix(calc.getMatrixData());
		}
	}
	
	private void addToMatrix(double[][] matrix) {
		for(int i = 0; i < matrixData.length; i++) {
			for(int j = 0; j < matrixData[i].length; j++) {
				try {
					matrixData[i][j] += matrix[i][j];
				} catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Vector<CalculatedXrayFile> getFiles() {
		return files;
	}

	public void setFiles(Vector<CalculatedXrayFile> files) {
		this.files = files;
	}

	public double[][] getColumnarData() {
		return columnarData;
	}

	public void setColumnarData(double[][] columnarData) {
		this.columnarData = columnarData;
	}

	public double[][] getMatrixData() {
		if(matrixData == null)
			compileMatrixData();
		return matrixData;
	}

	public void setMatrixData(double[][] matrixData) {
		this.matrixData = matrixData;
	}
	
}