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
package test;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Random;

public class PixelBetweenPoints {
	private boolean print = false;
	public void getPixelsBetweenPoints(Point2D.Double p1, Point2D.Double p2) {

		double xAdd = 1;
		double yAdd = 1;
		int endIdx = 0;
		double slope = 0;
		// for horizontal and vertical lines and when the slope = +/- 1 the slope is irrelevant
		// vertical line
		if(p2.x - p1.x == 0) {
			xAdd = 0;
			// line points down
			if(p2.y < p1.y) { yAdd = -1; } 
		}
		// horizontal line
		else if(p2.y - p1.y == 0) {
			yAdd = 0;
			// line points left
			if(p2.x < p1.x) { xAdd = -1; }
		}
		else {
			slope = (p2.y - p1.y) / (p2.x - p1.x);
			if(slope > 0) {
				if(slope >= 1) {
					xAdd = 1F/slope;
					if(p2.y < p1.y) {	// line points left 
						yAdd = -1; 
						xAdd = -xAdd;
					}
				}
				else {	// 0 < slope < 1
					yAdd = slope;
					if(p2.y < p1.y) {// line points left
						xAdd = -1; 
						yAdd = -yAdd;
					} 
				}
			}
			else {	// slope < 0
				if(slope <= -1) {
					xAdd = 1F/slope;
					if(p2.x > p1.x){ // line points right 
						yAdd = -1; 
						xAdd = -xAdd;
					} 
				}
				else {	// 0 > slope > -1
					yAdd = slope;
					if(p2.y > p1.y) {	// line points right
						xAdd = -1; 
						yAdd = -yAdd;
					}
				}
			}
		}
		double dy = Math.abs(p2.y - p1.y);
		double dx = Math.abs(p2.x - p1.x); 
		endIdx = (int) Math.rint(dy > dx ? dy : dx); 
		Point2D.Double endPoint = new Point2D.Double();
		for(int idx = 0; idx <= endIdx; idx++) {
			endPoint.x = (int) Math.rint(p1.x + idx*xAdd);
			endPoint.y = (int) Math.rint(p1.y + idx*yAdd);
			//System.out.println(endPoint);
		}
		if(print) { System.out.println("\tTesting p1: " + p1 + "\tp2: " + p2); }
		if(endPoint.x - p2.x + endPoint.y - p2.y != 0) {
			System.out.print("\n\n\n\n");
			System.out.println("TEST FAIL for:");
			System.out.println("p1: " + p1 + "\np2: " + p2 + "\nfinalPoint: " + endPoint);
			System.out.println("slope: " + slope);
			System.out.println("xAdd: " + xAdd);
			System.out.println("yAdd: " + yAdd);
			
			System.out.print("\n\n\n\n");
		} else {
			if(print) { System.out.println("\tTest pass."); }
		}
	}
	
	public void runTestCases(int x1, int y1, int x2, int y2) {
		Point2D.Double p1, p2;
		if(print) { System.out.println("test case 1 is a positive slope travelling in the positive x & y direction."); }
		p1 = new Point.Double(x1, y1);
		p2 = new Point.Double(x2, y2);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 2 is a positive slope travelling in the negative x & y direction.");}
		p1 = new Point.Double(x2, y2);
		p2 = new Point.Double(x1, y1);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 3 is a negative slope travelling in the positive x, negative y direction");}
		p1 = new Point.Double(x1, y2);
		p2 = new Point.Double(x2, y1);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 4 is a negative slope travelling in the negative x, positive y direction");}
		p1 = new Point.Double(x2, y1);
		p2 = new Point.Double(y1, x2);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 5 is a horizontal line travelling in the positive x direction");}
		p1 = new Point.Double(x1, y1);
		p2 = new Point.Double(x2, y1);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 6 is a horizontal line travelling in the negative x direction");}
		p1 = new Point.Double(x2, y1);
		p2 = new Point.Double(x1, y1);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 7 is a vertical line travelling in the positive y direction");}
		p1 = new Point.Double(x1, y1);
		p2 = new Point.Double(x1, y2);
		getPixelsBetweenPoints(p1, p2);
		
		if(print) { System.out.println("test case 8 is a vertical line travelling in the negative y direction");}
		p1 = new Point.Double(x1, y2);
		p2 = new Point.Double(x1, y1);
		getPixelsBetweenPoints(p1, p2);
	}	
	public void run() {
		Random r = new Random();
		runTestCases(r.nextInt()/2 - Integer.MAX_VALUE/4,
				r.nextInt()/2 - Integer.MAX_VALUE/4,
				r.nextInt()/2 - Integer.MAX_VALUE/4,
				r.nextInt()/2 - Integer.MAX_VALUE/4);
	}
	public static void main(String[] args) {
		PixelBetweenPoints p = new PixelBetweenPoints();
		for(int i = 0; i < 15; i++) {
			p.run();
				System.out.println(i);
		}
	}
}
