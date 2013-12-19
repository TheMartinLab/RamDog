package io;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class RenameFiles {

	private File path;
	private Scanner s = new Scanner(System.in);
	private String[] options = {"rename"};
	public RenameFiles(String path) {
		this.path = new File(path);
		run();
	}
	public RenameFiles(File path) {
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
		String prefix, suffix;
		System.out.println(files[0].toString());
		
		System.out.println("Enter prefix: ");
		prefix = s.next();
		System.out.println("Enter suffix: ");
		suffix = s.next();

		int idxStart, idxEnd, maxIdx = 0;
		int[] newIdx = new int[files.length]; 
		for(int i = 0; i < files.length; i++) {
			idxStart = prefix.length();
			idxEnd = idxStart + files[i].toString().length() - suffix.length()-prefix.length();
			newIdx[i] = Integer.valueOf(files[i].toString().substring(idxStart, idxEnd));
			if(maxIdx < newIdx[i]) { maxIdx = newIdx[i]; }
			System.out.print(newIdx[i] + "\t");
			System.out.println(idxStart + "\t" + idxEnd);
		}
		String newFileName = "";
		for(int i = 0; i < files.length; i++) {
			newFileName = prefix + getNewIdx(newIdx[i], maxIdx) + suffix;
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
		System.out.println("prefix: " + prefix);
		System.out.println("suffix: " + suffix);
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
}
