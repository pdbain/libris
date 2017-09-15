package LibrisTest;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import Libris.pdf.ProcessPdfDocument;

public class TestPdfRead extends TestCase {

//	static PDDocument pdfDoc;
	File pdfFile;
	private Writer output;
	String fName = "/Users/pdbain/Documents/publications/chen_aberer_1999.pdf";
	HashMap<String, Integer> importances;
	String keywordList[] = {"ALLCAPSLARGEWORD", 
			"frequentRegularWord", 
			"italicWord", 
			"LargeWord", 
			"lowercasesmallword", 
			"regularWord", 
			"singleUseofFont"};
	protected void setUp() throws Exception {
		super.setUp();
		pdfFile = new File(fName);
		importances = new HashMap<String, Integer>();
	}
	public void testRead() {
		String testDataFile = System.getenv("TestData");
		fName = (testDataFile == null) ?
				"/Users/pdbain/Documents/publications/chen_aberer_1999.pdf":
				testDataFile;
		ProcessPdfDocument doc = new ProcessPdfDocument(null, null, fName);
		doc.readDocument();
		doc.parseWordInfo();
		ArrayList<String> kw;
		int count = 0;
		String word;
		do {
			kw = doc.getKeywords(1);
			if (!kw.isEmpty()) {
				System.out.println(word = kw.get(0));
				importances.put(word, count++);
			}
			
		} while (!kw.isEmpty());
		assertMoreImportant("ALLCAPSLARGEWORD", "lowercasesmallword");
		assertMoreImportant("LargeWord", "regularWord");
		assertMoreImportant("regularWord", "frequentRegularWord");
		assertMoreImportant("singleUseofFont", "regularWord");
		assertMoreImportant("italicWord", "regularWord");
		for (int i = 0; i < keywordList.length; ++ i) {
			assertNotNull(keywordList[i]+" not found", importances.get(keywordList[i]));
		}
	}
	private void assertMoreImportant(String miw, String liw) {
		int mi = importances.get(miw);
		int li = importances.get(liw);
		assertTrue(miw+" importance = "+Integer.toString(mi)+", "+liw+" importance = "+Integer.toString(li),
				mi < li);
	}
	
}
