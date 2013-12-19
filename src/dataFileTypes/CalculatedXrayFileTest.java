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

public class CalculatedXrayFileTest {

	public static void main(String[] args) {
		File f = new File("D:\\Documents referenced in lab notebooks\\Dill-4\\83\\EDD_4-83b\\diffraction\\0.xray");
		
		CalculatedXrayFile calc = new CalculatedXrayFile(f);
		calc.read();
	}
}
