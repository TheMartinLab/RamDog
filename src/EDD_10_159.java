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
import io.MyPrintStream;
import io.StringConverter;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import masks.SavGol2ndDer;
import analysis.ApplyFilter;
import equations_1d.Avrami;
import equations_1d.Equation1;
import equations_2d.Equation2;
import equations_2d.Gaussian2;


public class EDD_10_159 implements Runnable {

	// gaussian parameters
	private int sigX = 5;
	private int sigY = sigX;
	private int gridDim = sigX*10;
	private int muX = gridDim/2;
	private int muY = muX;
	private double A = 1e2;
	private double threshold = -5;

	// avrami parameters
	private final static double k = 0.05;
	private final static int n = 3;
	private final static int tau = 5;
	
	// program variables
	private double[][][] grid;
	private double[][][] activePixels;
	private double[][] transformed, difference, alphas;
	private double[] likeRamdog, sumOfDifferences, maxVals;
	private Equation2 e = new Gaussian2(new double[] {1, muX, sigX}, new double[] {muY, sigY});
	private Vector<int[]> pix;
	private String[] differenceLabels;
	private final int threadIdx;
	private static int NUM_THREADS = 0;
	
	// output info
	private static String root = "EDD_10-159--";
	private File fGrid, f2ndDers, fTransformed, fAlphas, fDifference;
	private final static File fParameterSpace = new File(root + "parameterSpace.txt");
	private final static MyPrintStream mpsParamSpace = new MyPrintStream(fParameterSpace);
	
	public EDD_10_159() {
		run();
		threadIdx = NUM_THREADS++;
		initFiles();
	}
	public EDD_10_159(int sigX, int sigY, int gridDim, int muX, int muY, int A, int thresh) {
		this.sigX = sigX;
		this.sigY = sigY;
		this.gridDim = gridDim;
		this.muX = muX;
		this.muY = muY;
		this.A  = A;
		threshold = thresh;
		threadIdx = NUM_THREADS++;
		initFiles();
	}
	private void initFiles() {
		String root = "--" + EDD_10_159.root + threadIdx;
		fGrid = new File("grid" + root + ".txt");
		f2ndDers = new File("ActivePixels" + root + ".txt");
		fTransformed = new File("transformed" + root + ".txt");
		fAlphas = new File("alphas" + root + ".txt");
		fDifference= new File("difference" + root + ".txt");
	}
	public Double[] getIntensity() {
		Equation1 avrami = new Avrami(new double[] {k, tau, n});
		Vector<Double> I = new Vector<Double>();
		double val = 0;
		int t = 0;
		while(val <= 0.999) {
			val = avrami.evaluate(t++);
			I.add(val);
		}
		Double[] intensity = new Double[I.size()];
		intensity = I.toArray(intensity);
		return intensity;
	}
	// 7x7 savitzky-golay mask
	private void calc2ndDers() {
		activePixels = new double[grid.length][grid[0].length][grid[0][0].length];
		double[] ders2nd;
		ApplyFilter af = new ApplyFilter(new SavGol2ndDer(1));
		pix = new Vector<int[]>();
		for(int i = 0; i < grid.length; i++) {
			for(int x = 3; x < gridDim-3; x++) {
				for(int y = 3; y < gridDim-3; y++) {
					ders2nd = af.fast2ndDer(x, y, grid[i]);
					if(ders2nd[0] <= threshold && ders2nd[1] <= threshold) {
						activePixels[i][x][y] = grid[i][x][y];
						if(i == grid.length-1) {
							pix.add(new int[] {x, y});
						}
					}
				}
			}
		}
	}
	// compute stamp values (like the current version of the Ramdog Prototype does
	private void likeRamdog() {
		likeRamdog = new double[grid.length];
		double frameSum = 0;
		int[] pixels;
		for(int i = 0; i < likeRamdog.length; i++, frameSum = 0) {
			Iterator<int[]> iter = pix.iterator();
			while(iter.hasNext()) {
				pixels = iter.next();
				frameSum += grid[i][pixels[0]][pixels[1]];
			}
			likeRamdog[i] = frameSum;
		}
	}
	private void extractTransformed() {
		transformed = new double[grid.length][4];
		for(int i = 0; i < transformed.length; i++) {
			transformed[i][0] = i;
			transformed[i][1] = sumArr2(grid[i]);
			transformed[i][2] = sumArr2(activePixels[i]);
			transformed[i][3] = likeRamdog[i];
		}
	}
	private void computeAlphas() {
		alphas = new double[transformed.length][transformed[0].length];
		maxVals = new double[alphas[0].length-1];
		int lastRow = alphas.length;
		for(int i = 0; i < maxVals.length; i++) {
			maxVals[i] = transformed[lastRow-1][i+1];
		}
		for(int i = 0; i < alphas.length; i++) {
			for(int j = 1; j < alphas[0].length; j++) {
				alphas[i][j] = transformed[i][j] / maxVals[j-1];
			}
		}
		// put in times
		for(int i = 0; i < alphas.length; i++) {
			alphas[i][0] = transformed[i][0];
		}
	}
	private void computeDifference() {
		difference = new double[alphas.length][alphas[0].length];
		differenceLabels = new String[difference[0].length-1];
		for(int i = 0; i < difference.length; i++) {
			for(int j = 1; j < alphas[i].length; j++) {
				for(int k = j+1; k < alphas[i].length; k++) {
					differenceLabels[(k+j-3)] = (j-1) + "-" + (k-1);
					difference[i][j] += alphas[i][j] - alphas[i][k];
				}
			}
		}
		// put in times
		for(int i = 0; i < difference.length; i++) {
			difference[i][0] = alphas[i][0];
		}
	}
	private void computeSumsOfDifferences() {
		sumOfDifferences = new double[difference[0].length-1];
		for(int i = 0; i < difference.length; i++) {
			for(int j = 1; j < difference[0].length; j++) {
				sumOfDifferences[j-1] += difference[i][j];
			}
		}
	}
	private double sumArr2(double[][] arr) {
		double sum = 0;
		for(int i = 0; i < arr.length; i++) {
			for(int j = 0; j < arr[i].length; j++) {
				sum += arr[i][j];
			}
		}
		return sum;
	}
	public void run() {
		Double[] I = getIntensity();
		grid = new double[I.length][gridDim][gridDim];
		for(int i = 0; i < grid.length; i++) {
			e.setParam(Equation2.X, I[i], 0);
			for(int x = 0; x < gridDim; x++) {
				for(int y = 0; y < gridDim; y++) {
					grid[i][x][y] = e.evaluate(x, y)*A;
				}
			}
		}
		calc2ndDers();
		likeRamdog();
		extractTransformed();
		computeAlphas();
		computeDifference();
		computeSumsOfDifferences();
		printDifferences();
		printToFile(grid, fGrid);
		printToFile(activePixels, f2ndDers);
		printToFile(transformed, fTransformed);
		printToFile(alphas, fAlphas);
		printToFile(difference, fDifference);
		printParams();
	}
	private synchronized void printParams() {
		mpsParamSpace.println(threadIdx + "\t" + muX + "\t" + muY + "\t" + sigX + "\t" + sigY + "\t" + gridDim +
				"\t" + A + "\t" + threshold + "\t" + 
				StringConverter.arrayToTabString(sumOfDifferences) +
				StringConverter.arrayToTabString(maxVals));
	}
	private void printDifferences() {
		System.out.println(StringConverter.arrayToTabString(differenceLabels));
		System.out.println(StringConverter.arrayToTabString(sumOfDifferences));
		System.out.println(StringConverter.arrayToTabString(maxVals));
	}
	public void printToFile(double[][] arr, File f) {
		MyPrintStream mps = new MyPrintStream(f);
		for(int i = 0; i < arr.length; i++) {
			mps.println(StringConverter.arrayToTabString(arr[i]));
		}
	}
	public void printToFile(double[][][] arr, File f) {
		MyPrintStream mps = new MyPrintStream(f);
		for(int i = 0; i < arr.length; i++) {
			for(int x = 0; x < gridDim; x++) {
				mps.println(StringConverter.arrayToTabString(arr[i][x]));
			}
			mps.println();
		}
	}
	
	public static void main(String[] args) {
		int[] sigX = new int[] {1, 2, 3, 4, 5};
		int gridDim = 50;
		int muX = gridDim/2;
		int muY = muX;
		int maxA = 1000;
		int threshold = -5;
		Executor e = Executors.newFixedThreadPool(2);
		for(int x = 0; x < sigX.length; x++) {
			for(int y = x; y < sigX.length; y++) {
				for(int a = 10; a < maxA; a += 10) {
					e.execute(new EDD_10_159(sigX[x], sigX[y], gridDim, muX, muY, a, threshold));
				}
			}
		}
	}
}
