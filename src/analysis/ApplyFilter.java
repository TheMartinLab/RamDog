/*******************************************************************************
 * Copyright (c) 2013-2014 Eric Dill -- eddill@ncsu.edu. North Carolina State University. All rights reserved.
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

import masks.Mask;

public class ApplyFilter {
	private Mask maskType;
	public ApplyFilter(Mask maskType) {
		this.maskType = maskType;
	}
	/**
	 * 
	 * @param y the y coordinate of the point to calc the 2nd derivative
	 * @param x the x coordinate of the point to calc the 2nd derivative
	 * @param image the 2 dimensional array of doubles corresponding to an image
	 * @return	an Array: {2nd der y, 2nd der x};
	 */
	public double[] fast2ndDer(int x, int y, double[][] image) {
		double[] mask = maskType.getMask();
		double[] xDerArray = new double[13];
		double[] yDerArray = new double[13];
		
		y-=3;
		x-=3;
		double xDerivative = 0;
		double yDerivative = 0;
		
		try {
		 xDerArray[0] = image[x][y] + image[x + 6][y] + image[x][y + 6] + image[x + 6][y + 6];
         yDerArray[0] = image[x][y] + image[x][y + 6] + image[x + 6][y] + image[x + 6][y + 6];

         xDerArray[1] = image[x + 1][y] + image[x + 5][y] + image[x + 1][y + 6] + image[x + 5][y + 6];
         yDerArray[1] = image[x][y + 1] + image[x][y + 5] + image[x + 6][y + 1] + image[x + 6][y + 5];

         xDerArray[2] = image[x + 2][y] + image[x + 4][y] + image[x + 2][y + 6] + image[x + 4][y + 6];
         yDerArray[2] = image[x][y + 2] + image[x][y + 4] + image[x + 6][y + 2] + image[x + 6][y + 4];

         xDerArray[3] = image[x + 3][y] + image[x + 3][y + 6];
         yDerArray[3] = image[x][y + 3] + image[x + 6][y + 3];

         xDerArray[4] = image[x][y + 1] + image[x + 1][y + 1] + image[x + 2][y + 1] + image[x + 3][y + 1] + 
                      	image[x + 4][y + 1] + image[x + 5][y + 1] + image[x + 6][y + 1] + image[x][y + 5] +
                      	image[x + 1][y + 5] + image[x + 2][y + 5] + image[x + 3][y + 5] + image[x + 4][y + 5] + 
                      	image[x + 5][y + 5] + image[x + 6][y + 5];
         yDerArray[4] = image[x + 1][y] + image[x + 1][y + 1] + image[x + 1][y + 2] + image[x + 1][y + 3] + 
         				image[x + 1][y + 4] + image[x + 1][y + 5] + image[x + 1][y + 6] + image[x + 5][y] + 
         				image[x + 5][y + 1] + image[x + 5][y + 2] + image[x + 5][y + 3] + image[x + 5][y + 4] + 
         				image[x + 5][y + 5] + image[x + 5][y + 6];

         xDerArray[5] = yDerArray[2];
         yDerArray[5] = xDerArray[2];

         xDerArray[6] = image[x + 1][y + 2] + image[x + 5][y + 2] + image[x + 1][y + 4] + image[x + 5][y + 4];
         yDerArray[6] = image[x + 2][y + 1] + image[x + 2][y + 5] + image[x + 4][y + 1] + image[x + 4][y + 5];

         xDerArray[7] = image[x + 2][y + 2] + image[x + 4][y + 2] + image[x + 2][y + 4] + image[x + 4][y + 4];
         yDerArray[7] = xDerArray[7];

         xDerArray[8] = image[x + 3][y + 2] + image[x + 3][y + 4];
         yDerArray[8] = image[x + 2][y + 3] + image[x + 4][y + 3];

         xDerArray[9] = yDerArray[3];
         yDerArray[9] = xDerArray[3];

         xDerArray[10] = image[x + 1][y + 3] + image[x + 5][y + 3];
         yDerArray[10] = image[x + 3][y + 1] + image[x + 3][y + 5];

         xDerArray[11] = yDerArray[8];
         yDerArray[11] = xDerArray[8];

         xDerArray[12] = image[x + 3][y + 3];
         yDerArray[12] = xDerArray[12];
		} catch(Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
         
         /** 
          * Multiply the values together and sum them.  The value calculated here is the 2nd derivative at the point at
          * the center of the 7x7 area.
          */
         for(int index = 0; index < 13; index++)
         {
        	 xDerivative += xDerArray[index] * mask[index];
        	 yDerivative += yDerArray[index] * mask[index];
         }
		return new double[] {xDerivative, yDerivative};
	}
}
