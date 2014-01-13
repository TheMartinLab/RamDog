package JavaToC;

import java.io.File;

import javax.swing.JFileChooser;

public class DirectFourierTransform {

	public static boolean useGPU = true;
	
	public static native double[] direct(double[] input, int rowsFirst, int nRows, int nCols);
	
	/**
	 * 
	 * @param input
	 * @param rowsFirst
	 * @param outputType 0: sqrt(re^2+im^2) <br>1: re<br>2: im
	 * @return
	 */
	public static double[][][] direct(double[][] input, int rowsFirst, int outputType) {
		int nCols = input.length;
		int nRows = input[0].length;
		double[] oneD = new double[input.length * input[0].length];
		int idx = 0;
		for(int x = 0; x < nCols; x++) {
			for(int y = 0; y < nRows; y++) {
				idx = y * nCols + x;
				oneD[idx] = input[x][y];
			}
		}
		double[][][] returnVals = null;
		if(useGPU) {
			oneD = direct(oneD, rowsFirst, nRows, nCols);
			int len = oneD.length/2;
			double[] ft_re = new double[len];
			double[] ft_im = new double[len];
			for(int i = 0; i < ft_re.length; i++) {
				ft_re[i] = oneD[i];
				ft_im[i] = oneD[i+len];
			}
			returnVals =  new double[2][nCols][nRows];
			for(int x = 0; x < nCols; x++) {
				for(int y = 0; y < nRows; y++) {
					idx = y * nCols + x;
					returnVals[0][x][y] = ft_re[idx];
					returnVals[1][x][y] = ft_im[idx];
				}
			}
		} else {
			returnVals = ftRows2(input);
			returnVals = ftCols2(returnVals);
		}
		return returnVals;
	}
	static {
		if(useGPU) {
			String prop = "java.library.path";
			String curPath = System.getProperty(prop);
			String newPath = "\"D:\\$research\\current\\eclipse projects\\My Packages\\JavaToC\"";
			if(!curPath.contains(newPath)) {
				System.out.println(prop + " before setting to: " + newPath + ": " + System.getProperty(prop));
				System.setProperty(prop, curPath + ";" + newPath);
				System.out.println(System.getProperty(prop));
				System.out.println(prop + " after setting to: " + newPath + ": " + System.getProperty(prop));
			}
			String fileToLoad = "DirectFourierTransform";
	
			System.loadLibrary(fileToLoad);
			if(!new File(fileToLoad + ".dll").exists()) {
				File loc = new File(".");
				System.out.println("Current file location: " + loc.getAbsolutePath());
				JFileChooser chooser = new JFileChooser(loc);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = chooser.showOpenDialog(null);
				switch(returnVal) {
				case JFileChooser.APPROVE_OPTION:
					fileToLoad = chooser.getSelectedFile().getName();
					fileToLoad = fileToLoad.substring(0, fileToLoad.lastIndexOf("."));
					System.setProperty(prop, curPath + ";\"" + chooser.getSelectedFile().getParent() + "\"");
					System.out.println(prop + " after setting to: " + newPath + ": " + System.getProperty(prop));
					break;
				}
			}
			
			System.loadLibrary(fileToLoad);
			
		}
		//cuInit();
	}
	
	public static void main(String[] args) {
//		int outputType = 2;
		double[][] arr = new double[25][25];
		for(int i = 0; i < arr.length; i++) {
			for(int j = 0; j < arr[i].length; j++) {
				arr[i][j] = Math.random()-0.5;
			}
		}
		double[][][] newArr = DirectFourierTransform.direct(arr, 0, 0);
//		newArr = DirectFourierTransform.direct(arr, 0, 1);
//		newArr = DirectFourierTransform.direct(arr, 0, 0);

//		System.out.println(StringConverter.arrayToTabString(arr[0]));
//		arr = ftRows(arr, outputType);
//		System.out.println(StringConverter.arrayToTabString(arr[0]));
//		arr = ftCols(arr, outputType);
//		System.out.println(StringConverter.arrayToTabString(arr[0]));
		System.out.println("Original\tNew");
		for(int i = 0; i < newArr.length; i++) {
			System.out.print(arr[i][0] + "\t");
		}
		System.out.println();
		for(int i = 0; i < newArr.length; i++) {
			System.out.print(newArr[0][i][0] + "\t");
		}
	}
	private static double[][][] ftCols2(double[][] inputData) {
		double[][][] byCols = new double[2][inputData.length][inputData[0].length];
		byCols[0] = inputData;
		return ftCols2(byCols);
	}
	private static double[][][] ftRows2(double[][] inputData) {
		double[][][] byRows = new double[2][inputData.length][inputData[0].length];
		byRows[0] = inputData;
		return ftRows2(byRows);
	}
	private static double[][][] ftCols2(double[][][] inputData) {
		double[][][] byCols = new double[2][inputData[0].length][inputData[0][0].length];
		int nRows = byCols[0][0].length;
		int nCols = byCols[0].length;
		for(int ky = 0; ky < nRows; ky++) {
			for(int kx = 0; kx < nCols; kx++) {
				double sumReal = 0;
				double sumImag = 0;
				for(int t = 0; t < nRows; t++) {
					double arg = 2*Math.PI * t * kx / nRows;
					sumReal +=  inputData[0][t][ky] * Math.cos(arg) + inputData[1][t][ky] * Math.sin(arg);
					sumImag += -inputData[0][t][ky] * Math.sin(arg) + inputData[1][t][ky] * Math.cos(arg);
				}
				byCols[0][kx][ky] = sumReal;
				byCols[1][kx][ky] = sumImag;
			}
		}
		return byCols;
	}
	private static double[][][] ftRows2(double[][][] inputData) {
		double[][][] byRows = new double[2][inputData[0].length][inputData[0][0].length];
		int nCols = byRows[0].length;
		int nRows = byRows[0][0].length;
		for(int kx = 0; kx < nRows; kx++) {
			for(int ky = 0; ky < nCols; ky++) {
				double sumReal = 0;
				double sumImag = 0;
				for(int t = 0; t < nCols; t++) {
					double arg = 2*Math.PI * t * kx / nCols;
					sumReal +=  inputData[0][kx][t] * Math.cos(arg) + inputData[1][kx][t] * Math.sin(arg);
					sumImag += -inputData[0][kx][t] * Math.sin(arg) + inputData[1][kx][t] * Math.cos(arg);
				}
				byRows[0][kx][ky] = sumReal;
				byRows[1][kx][ky] = sumImag;
			}
		}
		return byRows;
	}
	private static double[][] ftCols(double[][] inputData, int outputType) {
		double[][] byCols = new double[inputData.length][inputData[0].length];
		int nRows = byCols[0].length;
		int nCols = byCols.length;
		for(int ky = 0; ky < nRows; ky++) {
			for(int kx = 0; kx < nCols; kx++) {
				double sumReal = 0;
				double sumImag = 0;
				for(int t = 0; t < nCols; t++) {
					sumReal +=  inputData[t][ky] * Math.cos(2*Math.PI * t * kx / nCols);
					sumImag += -inputData[t][ky] * Math.sin(2*Math.PI * t * kx / nCols);
				}
				switch(outputType) {
				case 0: 
					byCols[kx][ky] = Math.sqrt(Math.pow(sumReal, 2) + Math.pow(sumImag, 2));
					break;
				case 1:
					byCols[kx][ky] = sumReal;
					break;
				case 2:
					byCols[kx][ky] = sumImag;
					break;
				}
			}
		}
		
		return byCols;
	}
	private static double[][] ftRows(double[][] exptData, int outputType) {
		double[][] byRows = new double[exptData.length][exptData[0].length];
		int nCols = byRows.length;
		int nRows = byRows[0].length;
		for(int kx = 0; kx < nCols; kx++) {
			for(int ky = 0; ky < nRows; ky++) {
				double sumReal = 0;
				double sumImag = 0;
				for(int t = 0; t < nRows; t++) {
					sumReal +=  exptData[kx][t] * Math.cos(2*Math.PI * t * ky / nRows);
					sumImag += -exptData[kx][t] * Math.sin(2*Math.PI * t * ky / nRows);
				}
				switch(outputType) {
				case 0: 
					byRows[kx][ky] = Math.sqrt(Math.pow(sumReal, 2) + Math.pow(sumImag, 2));
					break;
				case 1:
					byRows[kx][ky] = sumReal;
					break;
				case 2:
					byRows[kx][ky] = sumImag;
					break;
				}
			}
		}
		return byRows;
	}
}
