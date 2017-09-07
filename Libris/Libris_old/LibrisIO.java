/*
 * Created on Sep 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LibrisIO extends java.io.FileReader {
	/**
	 * @param dbFile
	 * @throws FileNotFoundException
	 */
	public int position = 0;
	public LibrisIO(File dbFile) throws FileNotFoundException {
		super(dbFile);
		position = 0;
		// TODO Auto-generated constructor stub
	}
	public int read(char[] cbuf,
            int offset,
            int length) {
		int result = 0;
		
		try {
			result = super.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result==-1) {
			return(0);
		}else{
			cbuf[offset]=(char) result;
			// System.out.print((char) result);
			++position;
			return 1;
		}
		
	}
}
