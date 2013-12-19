package io;

import java.io.File;

public class ReadSpotpickFiles {

	private File[] files;
	public ReadSpotpickFiles(File[] files) {
		this.files = files;
	}
	
	public ReadSpotpickFiles(File file) {
		new ReadSpotpickFiles(new File[] {file});
	}
	
	public static void main(String[] args) {
		
	}
}
