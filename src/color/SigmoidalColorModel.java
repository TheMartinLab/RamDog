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
package color;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.util.Iterator;
import java.util.Vector;

import equations.Avrami;
import equations.Equation;

import Lists.Pair;

public class SigmoidalColorModel extends ColorModel {

	private Vector<Double> levels;
	private Vector<Color> colors;
	private Equation e;
	private boolean sigmoid = false;
	public SigmoidalColorModel(int bits) {
		super(bits);
		e = new Avrami(new double[] {0, 0, 3});
	}
	
	

	@Override
	public int getAlpha(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBlue(int arg0) {
		return getColor(arg0).getBlue();
	}

	@Override
	public int getGreen(int arg0) {
		return getColor(arg0).getGreen();
	}

	@Override
	public int getRed(int arg0) {
		return getColor(arg0).getRed();
	}
	@Override
	public int getRGB(int arg0) {
		return getColor(arg0).getRGB();
	}

	private Color toColor(Double dLower, Double dUpper, Color cLower, Color cUpper, double value) {
		sigmoid = false;
		if(cLower == null)
			return cUpper;
		if(value > dUpper)
			return cUpper;
		if(value < dLower)
			return cLower;
		double fractionHigh = 0;
		double fractionLow = 1;
		if(sigmoid) {
			double half = 0.5 * (dUpper - dLower) + dLower;
			double k = Math.cbrt(-1 * Math.log(0.5)) / half;
			e.setParam(k, 0);
			e.setParam(dLower, 1);
			
			fractionHigh = e.evaluate(value);
			fractionLow = 1-fractionHigh;
		} else {
			fractionLow = (dUpper - value) / (dUpper - dLower);
			fractionHigh = 1 - fractionLow;
		}
		
		int red = (int) (cLower.getRed() * fractionLow + cUpper.getRed() * fractionHigh);
		int green = (int) (cLower.getGreen() * fractionLow + cUpper.getGreen() * fractionHigh);
		int blue = (int) (cLower.getBlue() * fractionLow + cUpper.getBlue() * fractionHigh);
		
		return new Color(red, green, blue);
		
	}
	public Color getColor(double arg0) {
		Double dLower = 0., dUpper = 0.;
		Color cLower = Color.white, cUpper = Color.black;
		
		int idx = 0;
		dUpper = levels.get(idx);
		cUpper = colors.get(idx);
		
		do {
			if(arg0 >= dUpper) {
				dLower = dUpper;
				cLower = cUpper;
				dUpper = levels.get(++idx);
				cUpper = colors.get(idx);
				
			} else 
				break;
		} while(idx+1 < colors.size());
		
		return toColor(dLower, dUpper, cLower, cUpper, arg0);
	}



	public Vector<Double> getLevels() { return levels; }
	public void setLevels(Vector<Double> levels) { this.levels = levels; }
	public Vector<Color> getColors() { return colors; }
	public void setColors(Vector<Color> colors) { this.colors = colors; }
}
