package LibrisTest;

import java.util.ArrayList;

import Libris.LibrisDatabase;
import Libris.RecordList;
import junit.framework.TestCase;


public class TestLibrisInteractive extends TestCase {

	public void testFile_Open() {
		System.out.println("testFile_Open\n");
		fail("testFile_Open not implemented");
	}

	public void testOrganize_Rebuild() {
		
		System.out.println("testOrganize_Rebuild\n");
		fail("testOrganize_Rebuild not implemented");		
	}

	/**
	 * @testcase file_open_1
	 * @method open database using file->open and browse using search->browse records
	 * @expect correct number of fields
	 */

	/**
	 * @testcase search_browse_1
	 * @method open database using file->open and browse using search->browse records,
	 * double click a record
	 * @expect new window opens with expected field
	 */

	/**
	 * @testcase organize_rebuild_1
	 * @method open database using file->open and rebuild
	 * @expect no errors
	 */
	
 	/**
	 * @testcase edit_newrecord_1
	 * @method open database using file->open, create record using edit-new record
	 * close window, search for record.  Close database and reopen. search for new record.
	 * @expect no errors
	 */
/**
 * @testcase search_browse_2
 * @purpose test navigation within record list
 * @method open a record in the browser. Use command keys to go to next, previous records.
 *  Use browse->next (previous) record to go to next (previous) records.
 * @expect contents of record window change
 * @variations
 */
	/**
	 * @testcase remember_last_directory
	 * @purpose test storing of state
	 * @method navigate to a database, open it, close it. do file->open
	 * @expect starting directory is same as before
	 * @variations
	 */
	/**
	 * @testcase edit_new_record_1
	 * @purpose test creation of new record
	 * @method edit->new record. select various fields.
	 * @expect
	 * @variations: select using tab/shift tab, mouse click
	 */
	
	/**
	 * @testcase test_enum2
	 * @purpose test enums
	 * @method edit field with enums
	 * @expect all enums allowed
	 * @variant one, two, many choices
	 * @variant editable or non-editable values
	 */

}
