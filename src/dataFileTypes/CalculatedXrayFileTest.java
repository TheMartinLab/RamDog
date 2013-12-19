package dataFileTypes;

import java.io.File;

public class CalculatedXrayFileTest {

	public static void main(String[] args) {
		File f = new File("D:\\Documents referenced in lab notebooks\\Dill-4\\83\\EDD_4-83b\\diffraction\\0.xray");
		
		CalculatedXrayFile calc = new CalculatedXrayFile(f);
		calc.read();
	}
}
