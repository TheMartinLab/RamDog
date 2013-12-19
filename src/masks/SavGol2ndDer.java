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
package masks;

public class SavGol2ndDer implements Mask {

	public double[] mask;
	
	public SavGol2ndDer(int points) {
		setUpMask(points);
	}
	private void setUpMask(int points) {
		mask = new double[13];
		mask[0]  = -0.021206968;
		mask[1]  = -0.007034632;
		mask[2]  =  0.001468769;
		mask[3]  =  0.004303236;
		mask[4]  =  0.036255411;
		mask[5]  = -0.001777984;
		mask[6]  = -0.010281385;
		mask[7]  = -0.015383426;
		mask[8]  = -0.017084106;
		mask[9]  = -0.026540919;
		mask[10] = -0.037878788;
		mask[11] = -0.044681509;
		mask[12] = -0.046949083;
	}
	
	@Override
	public double[] getMask() { return mask; }
}
