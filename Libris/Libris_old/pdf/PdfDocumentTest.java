package Libris.pdf;

import junit.framework.TestCase;

public class PdfDocumentTest {
	
	public static  void main(String fileName) {
		ProcessPdfDocument foo = new ProcessPdfDocument(null, null, fileName);
		foo.readDocument();
		foo.parseWordInfo();
	}
}
