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
package io;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ObjectIO {
	public static String getIdx(int curIdx, int maxIdx) {
		int maxLen = Integer.toString(maxIdx).length();
		String idx = Integer.toString(curIdx);
		
		while(idx.length() < maxLen) {
			idx = "0" + idx;
		}
		return idx;
	}
	/**
	 * Call this method by: ObjectIO.writeObject(aFileName, anObject);
	 * @param fName	The file output name
	 * @param obj	The object to be written to file
	 */
	public static synchronized void writeObject(File fName, Object obj) {
		System.out.println("Writing Object of type: " + obj.getClass().getCanonicalName() + " to file: " + fName);
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			fos = new FileOutputStream(fName);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Call this method by: <<objectType>> newObj = (<<objectType>>) ObjectIO.readObject(aFileName);
	 * @param objFile	The file to read an object from
	 * @return	The Object that was read in from the file
	 */
	public static synchronized Object readObject(File objFile) {
		System.out.println("Reading an object from file: " + objFile);
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(objFile);
			ois = new ObjectInputStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
