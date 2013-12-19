package dataFileTypes;

import java.io.File;

public interface ImageFile {
	enum types {ASC, BIN, SPR};
	public double[][] readFile(File aFile);
	public double[][] readFile(File aFile, int[] bounds);
}
