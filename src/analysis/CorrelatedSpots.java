package analysis;

import java.io.Serializable;
import java.util.Vector;

public class CorrelatedSpots implements Serializable{

	private static final long serialVersionUID = -7811026760589640055L;
	private Spot[] spots;
	private double x, y, I;
	
	public CorrelatedSpots(int numFrames) {
		spots = new Spot[numFrames];
	}
	public void add(int idx, Spot aSpot) {
		spots[idx] = aSpot;
	}
	
	public void calcProperties() {
		x = 0;
		y = 0;
		I = 0;
		int size = spots.length;
		for(int i = 0; i < spots.length; i++) {
			if(spots[i] == null) {
				size--;
				continue;
			}
			x += spots[i].getX();
			y += spots[i].getY();
			I += spots[i].getI();
		}
		x /= size;
		y /= size;
		I /= size;
	}
	public Pixel getClosestPixel(int idx) {
		double floorxVal=0, ceilingxVal=0, flooryVal=0, ceilingyVal=0;
		double floorIdx=0, ceilingIdx=0;
		int curPos = idx;
		// get the closest in the negative time direction
		if(spots[idx] != null) { return new Pixel((int) spots[curPos].getX(), (int) spots[curPos].getY(), 0, 0); }
		while(curPos >= 0) {
			if(spots[curPos] != null) {
				floorxVal = spots[curPos].getX();
				flooryVal = spots[curPos].getY();
				floorIdx = curPos;
				break;
			}
			curPos--;
		}
		// get the closest in the positive time direction
		curPos = idx;
		while(curPos < spots.length) {
			if(spots[curPos] != null) {
				ceilingxVal = spots[curPos].getX();
				ceilingyVal = spots[curPos].getY();
				ceilingIdx = curPos;
				break;
			}
			curPos++;
		}
		double xVal, yVal;
		
		if(floorxVal == 0 && flooryVal == 0) {
			return new Pixel((int) ceilingxVal, (int) ceilingyVal, 0, 0);
		}
		if(ceilingxVal == 0 && ceilingyVal == 0) {
			return new Pixel((int) floorxVal, (int) flooryVal, 0, 0);
		}
		xVal = (floorxVal * (ceilingIdx-idx) + ceilingxVal * (idx - floorIdx)) / (ceilingIdx - floorIdx);
		yVal = (flooryVal * (ceilingIdx-idx) + ceilingyVal * (idx - floorIdx)) / (ceilingIdx - floorIdx);
		
		return new Pixel((int) xVal, (int) yVal, 0, 0);
		
	}
	public double getX() { return x; }
	public double getY() { return y; }
	public double getI() { return I; }
	public double getI(int idx) {
		if(spots[idx] == null) { return 0; }
		else return spots[idx].getI();
	}
	public Spot getAverageSpot() {
		calcProperties();
		return new Spot(x, y, I);
	}
	public Pixel getAveragePixel() {
		calcProperties();
		return new Pixel((int) x, (int) y, I, 0);
	}
	public Spot getSpot(int idx) { return spots[idx]; }
	public Spot[] getSpots() { return spots; }
}
