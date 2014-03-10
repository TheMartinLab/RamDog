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
import java.io.Serializable;


/**
 * A pixel knows 5 things about itself: it's x- and y- coordinates in the corresponding
 * image,  its intensity at that location, and finally the 2nd derivatives in the 
 * x- and y- directions.  At this time, a pixel cannot do anything except exist.
 * @author Eric Dill
 * 			eddill@ncsu.edu
 *
 */
public class Pixel implements Serializable, Cloneable, Comparable {

	/** Instance variables */
	
	/** Automatically generated serialVersionUID */
	private static final long serialVersionUID = 16683999894266738L;

	/** The x- and y- coordinates of the pixel */
	private int x, y;
	
	/** The intensity of the pixel */
	private double intensity;
	
	/** The relative frame index that the pixel appears in */
	private int frameNumber;
	
	/** The name of the pixel, if one is given. */
	private String name = "Pixel";
	
	/** dist is either the q value or the r value, depending on the image under evaluation */
	private double dist;
	
	private double phi;
	
	/** Constructor to initialize the pixel */
	public Pixel(int x, int y, double intensity, int frameNumber)
	{
		/** set the x-coordinate */
		this.x = x;
		
		/** set the y-coordinate */
		this.y = y;
		
		/** set the intensity */
		this.intensity = intensity;
		
		this.frameNumber = frameNumber;
	}
	
	public Pixel(int x, int y, double I) {
		this.x = x;
		this.y = y;
		this.intensity= I;
	}

	/**
	 * Getter method to return the x-coordinate of the pixel
	 * @return An integer containing the x-coordinate
	 */
	public int getX() { return x; }
	
	/**
	 * Getter method to return the y-coordinate of the pixel
	 * @return An integer containing the y-coordinate
	 */
	public int getY()	{ return y;	}
	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void swapXY() {
		int tempx = x;
		x = y;
		y = tempx;
	}
	/**
	 * Getter method to return the intensity of the pixel
	 * @return A double containing the intensity
	 */
	public double getIntensity() { return intensity; }
	
	/**
	 * Getter method to return the spot number
	 * @return	An integer containing the index of the spot number
	 */
	public int getFrameNumber() { return frameNumber; }
	
	public static String getHeader() { return "Frame Number\tx\ty\tphi\tQ or R\tI"; }
	@Override
	public String toString() { return frameNumber + "\t" + x + "\t" + y + "\t" + phi + "\t" + dist + "\t" + intensity; }
	
	public Object clone() {
		Pixel p = new Pixel(x, y, intensity, frameNumber);
		p.frameNumber = frameNumber;
		return (Object) p;
	}

	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof Pixel) {
			Pixel pix = (Pixel) arg0;
			if(pix.x == x && pix.y == y && pix.intensity == intensity && pix.frameNumber == frameNumber) {
				return 0;
			}
		}
		return -1;
	}

	public double getDist() { return dist; }
	public void setDist(double dist) { this.dist = dist; }
	public double getPhi() { return phi; }
	public void setPhi(double phi) { this.phi = phi; }
	public void setIntensity(double intensity) { this.intensity = intensity; }
	public void setFrameNumber(int frameNumber) { this.frameNumber = frameNumber; }
}
