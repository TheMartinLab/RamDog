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

import java.util.Vector;

/**
 * A spot stamp is a collection of pixels relative to a center point of (0, 0)
 * @author Eric-t61p
 *
 */
public class SpotStamp {

	private Vector<Pixel> stamp;
	public SpotStamp() {
		stamp = new Vector<Pixel>();
	}
	
	/**
	 * Add a pixel to a pixel stamp
	 * @param toAdd	The pixel to add to the stamp
	 * @param x	The x coordinate of the spot center that the pixel belongs to
	 * @param y	The y coordinate of the spot center that the pixel belongs to
	 */
	public void addToStamp(Pixel toAdd, int x, int y) {
		if(!inStamp(toAdd, x, y)) {
			stamp.add(new Pixel(toAdd.getX() - x, toAdd.getY() - y, 0, 0));
		}
	}
	/**
	 * Get the relative stamp 
	 * @return	The relative pixel stamp.
	 */
	public Pixel[] getStamp() { return stamp.toArray(new Pixel[stamp.size()]); }
	
	/**
	 * Return a copy of the stamp at the location centered on (x, y)
	 * @param x	The x position that the stamp should be moved to
	 * @param y	The y position that the stamp should be moved to
	 * @return	A translated copy of the stamp centered on (x, y)
	 */
	public Pixel[] translate(int x, int y) {
		Pixel[] thePixels = new Pixel[stamp.size()];
		int curx, cury;
		for(int i = 0; i < thePixels.length; i++) {
			curx = stamp.get(i).getX();
			cury = stamp.get(i).getY();
			thePixels[i] = new Pixel(x+curx, y+cury, 0, 0);
		}
		return thePixels;
	}
	/**
	 * Test to see if the pixel is already in the stamp
	 * @param toAdd	The pixel to add to the stamp
	 * @param x	The x coordinate of the spot center that the pixel belongs to
	 * @param y	The y coordinate of the spot center that the pixel belongs to
	 * @return	True if the pixel is already in the stamp. False if the pixel is not in the stamp.
	 */
	private boolean inStamp(Pixel toAdd, int x, int y) {
		int xTest, yTest;
		x = toAdd.getX() - x;
		y = toAdd.getY() - y;
		
		int stampSize = stamp.size();
		int idx = 0;
		while(idx < stampSize) {
			xTest = stamp.get(idx).getX();
			yTest = stamp.get(idx).getY();
			if(x == xTest && y == yTest) { 
				return true;
			}
			idx++;
		}
		return false;
	}
}
