package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.util.Utilities.rebuildAndOpenDatabase;
import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldBooleanValue;
import org.lasalledebain.libris.field.FieldEnumValue;
import org.lasalledebain.libris.field.FieldIntValue;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.util.Lorem;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class LayoutTests extends TestCase {

	public static final int LOREM_NUM_WORDS = Lorem.words.length;
	private File workingDirectory;

	@Test
	public void testLayouts() throws FileNotFoundException, IOException, LibrisException {
		TestGUI myGui = rebuildAndOpenDatabase(getName(), workingDirectory, Utilities.EXAMPLE_DATABASE1_FILE);
		LibrisDatabase myDb = myGui.getLibrisDatabase();
		BrowserWindow resultsWindow = myGui.getResultsWindow();
		resultsWindow.setSelectedRecordIndex(3);
		RecordDisplayPanel dispPanel = myGui.getDisplayPanel();
		for (String layoutId: new String[] {"LO_shortFormDisplay", "LO_formDisplay",
				"LO_paragraphDisplay", "LO_tableDisplay", "LO_listDisplay", "LO_browserDisplay"}) {
			LibrisLayout theLayout = myDb.getLayouts().getLayout(layoutId);
			assertNotNull("Layout "+layoutId+" not found", theLayout);
			dispPanel.setRecLayout(theLayout);
			myGui.displaySelectedRecord();
			Utilities.pause();
			myGui.closeWindow(false);
		}
		myGui.quit(true);
	}

	public void testParagraphLayout() throws FileNotFoundException, DatabaseException, IOException, Exception {
		/* test recordToParagraph with missing fields */
		try (TestGUI myGui = rebuildAndOpenDatabase(getName(), workingDirectory, Utilities.TESTDATABASE3_XML)) {
			LibrisDatabase myDb = myGui.getLibrisDatabase();
			XmlSchema mySchema = myDb.getSchema();
			Layouts myLayouts = myDb.getLayouts();
			var paraLayout = myLayouts.getLayout("LO_paragraph");
			Random r = new Random(getName().hashCode());
			for (int i = 0; i < 6; ++i) {
				DatabaseRecord rec = myDb.newRecord();
				StringBuffer expected = new StringBuffer();
				String separator = "";
				int fieldNum = -1;
				final int numWords = LOREM_NUM_WORDS;
				for (FieldTemplate ft: mySchema.getFields()) {
					++fieldNum;
					if (r.nextBoolean()) continue;
					expected.append(separator);
					separator = ", ";
					String valueString = "";
					switch (ft.getFtype()) {
					case T_FIELD_BOOLEAN: {
						var value = new FieldBooleanValue(r.nextBoolean());
						rec.addFieldValue(fieldNum, value.getMainValueAsKey());
						valueString = value.getMainValueAsString();
					}
					break;
					case T_FIELD_INTEGER: {
						var value = new FieldIntValue(r.nextInt());
						rec.addFieldValue(fieldNum, value.getMainValueAsKey());
						valueString = value.getMainValueAsString();
					}
					break;
					case T_FIELD_ENUM: {
						Field f = rec.addFieldValue(fieldNum, r.nextInt(ft.getLegalValues().size()));
						valueString = f.getValuesAsString();
					}
					break;
					case T_FIELD_STRING:
					case T_FIELD_TEXT: {
						final int startPos = r.nextInt(numWords-100);
						String[] wordList = Arrays.copyOfRange(Lorem.words, startPos, startPos + r.nextInt(10) + 1);
						valueString = String.join(" ", wordList);
						rec.addFieldValue(fieldNum, valueString);
					}
					break;
					case T_FIELD_PAIR: {
						String v1 = Lorem.words[r.nextInt(numWords)];
						if (r.nextBoolean()) {
							String v2 = Lorem.words[r.nextInt(numWords)];
							rec.addFieldValue(fieldNum, v1, v2);
							valueString = v1+'-'+v2;
						} else {
							rec.addFieldValue(fieldNum, v1);
							valueString = v1;
						}
					}
					break;
					default: fail("Unhandled field type for "+ft.getFieldTitle());
					}
					expected.append(valueString);
				}
				String expectedParagraph = expected.toString();
				StringBuffer actual = new StringBuffer();
				ParagraphLayoutProcessor.recordToParagraph(rec, paraLayout.getFields(), actual);
				final String actualParagraph = actual.toString();
				assertEquals("Wrong paragraph text for record", expectedParagraph, actualParagraph);
			}
		}
	}
	
	public void testParagraphLayoutWithMultipleValues() throws FileNotFoundException, DatabaseException, IOException, Exception {

		/* test recordToParagraph with multiple field values */
		try (TestGUI myGui = rebuildAndOpenDatabase(getName(), workingDirectory, Utilities.TESTDATABASE3_XML)) {
			LibrisDatabase myDb = myGui.getLibrisDatabase();
			XmlSchema mySchema = myDb.getSchema();
			Layouts myLayouts = myDb.getLayouts();
			var paraLayout = myLayouts.getLayout("LO_paragraph");
			Set<Field.FieldType> checkedTypes = new HashSet<>();
			for (FieldTemplate ft: mySchema.getFields()) {
				final FieldType ftype = ft.getFtype();
				if (checkedTypes.contains(ftype)) continue;
				int fieldNum = mySchema.getFieldNum(ft.getFieldId());
				for (int numValues = 1; numValues < 4; ++numValues) {
					int valueCount = numValues;
					DatabaseRecord rec = myDb.newRecord();
					StringBuffer expected = new StringBuffer();
					String separator = "";
					while(valueCount-- > 0) {
						expected.append(separator);
						separator = ", ";
						String valueString = "";
						checkedTypes.add(ftype);
						switch (ftype) {
						case T_FIELD_BOOLEAN: {
							separator = "";
							continue ;
						}
						case T_FIELD_INTEGER: {
							var value = new FieldIntValue(10*numValues+valueCount);
							rec.addFieldValue(fieldNum, value.getMainValueAsKey());
							valueString = value.getMainValueAsString();
						}
						break;
						case T_FIELD_ENUM: {
							
							var value = new FieldEnumValue(ft.getEnumChoices(), ((10*numValues + valueCount) % ft.getLegalValues().size()));
							Field f = rec.addFieldValue(fieldNum, value.getMainValueAsKey());
							valueString = value.getMainValueAsString();
						}
						break;
						case T_FIELD_STRING:
						case T_FIELD_TEXT: {
							valueString = Lorem.words[10*numValues+valueCount];
							rec.addFieldValue(fieldNum, valueString);
						}
						break;
						case T_FIELD_PAIR: {
							String v1 = Lorem.words[10*numValues+valueCount];
							if (0 == (numValues + valueCount)%2) {
								String v2 = Lorem.words[10*numValues+valueCount+1];
								rec.addFieldValue(fieldNum, v1, v2);
								valueString = v1+'-'+v2;
							} else {
								rec.addFieldValue(fieldNum, v1);
								valueString = v1;
							}
						}
						break;
						default: fail("Unhandled field type for "+ft.getFieldTitle());
						}
						expected.append(valueString);
					}
					String expectedParagraph = expected.toString();
					StringBuffer actual = new StringBuffer();
					ParagraphLayoutProcessor.recordToParagraph(rec, paraLayout.getFields(), actual);
					final String actualParagraph = actual.toString();
					assertEquals("Wrong paragraph text for record field="+ft.getFieldId()+" "+numValues+" values", expectedParagraph, actualParagraph);
				}
			}
		}
	}

	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO, "Starting "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		testLogger.log(Level.INFO, "Ending "+getName());
		Utilities.deleteWorkingDirectory();
	}

}
