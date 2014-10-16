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
import java.util.Arrays;
import java.util.Scanner;

public class RenameFiles2 {

	private File path;
	private Scanner s = new Scanner(System.in);
	private String[] options = {"rename"};
	public RenameFiles2(String path) {
		this.path = new File(path);
		run();
	}
	public RenameFiles2(File path) {
		this.path = path;
		run();
	}
	private void printMenu() {
		System.out.println("-1: exit");
		for(int i = 0; i < options.length; i++) {
			System.out.println(i + ": " + options[i]);
		}
	}
	private int getSelection() { return s.nextInt(); }
	
	private void run() {
		int choice = 0;
		do {
			printMenu();
			choice = getSelection();
			switch(choice) {
			case 0: rename(); break;
			}
		} while(choice >= 0 && choice < options.length);
	}
	
	private void rename() {
		File[] files = path.listFiles();
		if(files == null) { 
			files = new File[1];
			files[0] = path;
		}
		String prefix1, middle, suffix;
		System.out.println(files[0].toString());
		
		System.out.println("Enter prefix: ");
		prefix1 = s.next();
		System.out.println("Enter middle: ");
		middle = s.next();
		System.out.println("Enter suffix: ");
		suffix = s.next();
		
		int idxStart, idxEnd, maxIdx = 0, idxMid;
		int[] newIdx = new int[files.length]; 
		int stride = 320;
		int idx = 0;
		char index = 'a';
		for(int i = 0; i < files.length; i++) {
			idxStart = prefix1.length();
			idxMid = middle.length();
			idxEnd = idxStart + files[i].toString().length() - suffix.length()-prefix1.length();
			index = files[i].toString().charAt(idxMid);
			idx = charToIdx(index);
			newIdx[i] = Integer.valueOf(files[i].toString().substring(idxStart, idxEnd))+stride*idx;
			if(maxIdx < newIdx[i]) { maxIdx = newIdx[i]; }
			System.out.print(newIdx[i] + "\t");
			System.out.println(idxStart + "\t" + idxEnd);
		}
		String newFileName = "";
		for(int i = 0; i < files.length; i++) {
			newFileName = prefix1 + getNewIdx(newIdx[i], maxIdx) + suffix;
			files[i].renameTo(new File(newFileName));
		}
		files = path.listFiles();
		System.out.println("Before sort but with new file names: ");
		for(int i = 0; i < files.length; i++) {
			System.out.println(files[i].toString());
		}
		Arrays.sort(files);
		System.out.println("After sort with new file names: ");
		for(int i = 0; i < files.length; i++) {
			System.out.println(files[i].toString());
		}
		System.out.println(maxIdx);
		System.out.println("Your choices are: ");
		System.out.println("prefix: " + prefix1);
		System.out.println("suffix: " + suffix);
	}
	
	private int charToIdx(char c) {
		return ((c-'a'));
	}
	private String getNewIdx(int curIdx, int maxIdx) {
		String newIdx = String.valueOf(curIdx);
		String strMaxIdx = String.valueOf(maxIdx);
		int totalLen = newIdx.length();
		while(totalLen < strMaxIdx.length()) {
			newIdx = "0" + newIdx;
			totalLen++;
		}
		
		return newIdx;
	}
	public static void main(String[] args) {
		new RenameFiles2(new File("D:\\data\\c230q065a1\\"));
	}
}
