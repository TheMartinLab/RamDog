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
package dataFileTypes;

import java.io.File;
import java.util.Vector;

public class CalculatedXrayFileTools {
	
	public final static String[] s100 = new String[] 
		{	
			"100", "-100", 
			"010", "0-10", 
			"001", "00-1"
		};
	public final static String[] s110 = new String[] 
		{	
			"110", "-110", "1-10", "-1-10", 
			"101", "-101", "10-1", "-10-1",
			"011", "0-11", "01-1", "0-1-1"
		};
	
	public final static String[] s111 = new String[]
		{	
			"111", "-111", "1-11", "11-1",
			"-1-1-1", "-1-11", "-11-1", "1-1-1"
		};
	
	public static Vector<CalculatedXrayCollection> splitIntoCollections(File imagesFolder) {
		File[] files = imagesFolder.listFiles();
		if(files.length < 1) {
			System.out.println("No files in folder.");
			return null;
		}
		Vector<CalculatedXrayFile> f100 = new Vector<CalculatedXrayFile>();
		Vector<CalculatedXrayFile> f110 = new Vector<CalculatedXrayFile>();
		Vector<CalculatedXrayFile> f111 = new Vector<CalculatedXrayFile>();
		Vector<CalculatedXrayFile> f_other = new Vector<CalculatedXrayFile>();
		
		String name;
		for(int i = 0; i < files.length; i++) {
			boolean added = false;
			name = files[i].getName();
			for(int j = 0; j < s100.length && !added; j++) {
				if(name.contains("_" + s100[j]) || name.contains(s100[j] + "--")) {
					f100.add(new CalculatedXrayFile(files[i]));
					added = true;
					System.out.println("Added: " + name + " to [100] listing.");
				}
			}
			for(int j = 0; j < s110.length && !added; j++) {
				if(name.contains("_" + s110[j]) || name.contains(s110[j] + "--")) {
					f110.add(new CalculatedXrayFile(files[i]));
					added = true;
					System.out.println("Added: " + name + " to [110] listing.");
				}
			}
			for(int j = 0; j < s111.length && !added; j++) {
				if(name.contains("_" + s111[j]) || name.contains(s111[j] + "--")) {
					f111.add(new CalculatedXrayFile(files[i]));
					added = true;
					System.out.println("Added: " + name + " to [111] listing.");
				}
			}
			if(!added) {
				f_other.add(new CalculatedXrayFile(files[i]));
				System.out.println("Added: " + name + " to [other] listing.");
			}
		}
		Vector<CalculatedXrayCollection> collections = new Vector<CalculatedXrayCollection>();
		collections.add(new CalculatedXrayCollection(f100));
		collections.add(new CalculatedXrayCollection(f110));
		collections.add(new CalculatedXrayCollection(f111));
		collections.add(new CalculatedXrayCollection(f_other));
		return collections;
	}
}
