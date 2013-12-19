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
package analysis;

import java.awt.Point;
import java.util.Vector;

import masks.Mask;
import masks.SavGol2ndDer;

public class PixelAnalysis {
	private ApplyFilter af;
	private int xRadius = 4, yRadius = 4;
	public PixelAnalysis() {
		af = new ApplyFilter(new SavGol2ndDer(1));
	}
	public double[] calc2ndDer(double[][] data, Point click) {
		return af.fast2ndDer(click.x, click.y, data);
	}
	public Pixel[] findPixels(double[][] X2ndDer, double[][] Y2ndDer, double[][] data, Point click) {
		Point p = findCenter(data, click);
		
		System.out.println("Center of the spot: " + p);
		int[] bounds = findXAndY(p.x, p.y, X2ndDer, Y2ndDer);
		Pixel[] pix = new Pixel[(bounds[1]-bounds[0]) * (bounds[3] - bounds[2])];
		int idx = 0;
		for(int i = bounds[0]; i < bounds[1]; i++) {
			for(int j = bounds[2]; j < bounds[3]; j++) {
				pix[idx++] = new Pixel(i, j, data[i][j]); 
			}
		}
		return pix;
	}
	/**
	 * Method to find the minimum and maximum values of x and y based on the center of the spot and a maximum of 10 pixels in any direction
	 * @return {minx, maxx, miny, maxy}
	 */
	private int[] findXAndY(int x, int y, double[][] X2ndDer, double[][] Y2ndDer) {
		double xDerVal, yDerVal, xTestVal, yTestVal;
		final int maxPixelDistance = 10;
		
		//traverse the positive x direction
		xDerVal = X2ndDer[x][y];
		int i = 1;
		xTestVal = X2ndDer[x+1][y];
		while(xDerVal < xTestVal && Math.abs(i) < maxPixelDistance) {
			i++;
			xDerVal = xTestVal;
			xTestVal = X2ndDer[x+i][y];
		}
		int maxx = x+i;
		// traverse the negative x direction
		xDerVal = X2ndDer[x][y];
		i = 0;
		xTestVal = 0;
		while(xDerVal < xTestVal && Math.abs(i) < maxPixelDistance) {
			i--;
			xDerVal = xTestVal;
			xTestVal = X2ndDer[x+i][y];
		}
		int minx = x+i;
		// traverse the positive y direction
		yDerVal = Y2ndDer[x][y];
		i = 0;
		yTestVal = 0;
		while(yDerVal < yTestVal && Math.abs(i) < maxPixelDistance) {
			i++;
			yDerVal = yTestVal;
			yTestVal = Y2ndDer[x][y+i];
		}
		int maxy = y+i;
		// traverse the negative y direction
		yDerVal = Y2ndDer[x][y];
		i = 0;
		yTestVal = 0;
		while(yDerVal < yTestVal && Math.abs(i) < maxPixelDistance) {
			i--;
			yDerVal = yTestVal;
			yTestVal = Y2ndDer[x][y+i];
		}
		int miny = y+i;
		return new int[] {minx, maxx, miny, maxy};
	}

	public Point findCenter(double[][] data, Point click) {
		Pixel pix = null;
		double curI = 0;
		for(int x = click.x - xRadius; x < click.x + xRadius; x++) {
			for(int y = click.y - yRadius; y < click.y + yRadius; y++) {
				if(curI < data[x][y]) {
					curI = data[x][y];
					pix = new Pixel(x, y, curI);
				}
			}
		}
		return new Point(pix.getXCoordinate(), pix.getYCoordinate());
	}
	public void setSearchRadius(int x, int y) {
		xRadius = x;
		yRadius = y;
	}
}
