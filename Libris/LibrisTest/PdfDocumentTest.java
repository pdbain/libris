package LibrisTest;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.sun.org.apache.bcel.internal.generic.FNEG;

import Libris.pdf.ProcessPdfDocument;
import Libris.pdf.PdfDocument_old;

public class PdfDocumentTest {

	/**
	 * @param args
	 */
	private static String fName;

	public static void main(String[] args) {
		fName = "/Users/pdbain/Documents/publications/chen_aberer_1999.pdf";
		ProcessPdfDocument doc = new ProcessPdfDocument(null, null, fName);
		doc.readDocument();
		doc.parseWordInfo();
	}

}
