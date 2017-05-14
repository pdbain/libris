/*
 * Created on Apr 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.util.ArrayList;

/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StringArrayList extends ArrayList {
	public void add(int index,
            String element) {
		super.add(index, element);
	}
	public String getString(int index) {
		return(super.get(index).toString());
	}
}
