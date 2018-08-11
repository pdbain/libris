package org.lasalledebain.libris.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class PdfDocument extends PDFTextStripper {
private PdfDocument() throws IOException {
		super();
		wordSet = new HashSet<String>(1000);
	}
/* (non-Javadoc)
 * @see org.apache.pdfbox.util.PDFTextStripper#endDocument(org.apache.pdfbox.pdmodel.PDDocument)
 */
@Override
protected void endDocument(PDDocument pdf) throws IOException {
	for (String w:wordSet) {
		System.out.println(w);
	}
}
File myFile;
private HashSet<String> wordSet;

	Iterator<String> getFormattedText() throws IOException {
		String theText = getText(PDDocument.load(myFile));
		return (Iterator<String>) Arrays.asList(theText.split("\\s")).iterator();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.pdfbox.util.PDFTextStripper#writePage()
	 */
	protected Iterator<String> getWords() throws IOException {
		String theText = getText(PDDocument.load(myFile));
        StringBuilder buff = new StringBuilder();
        for( int i = 0; i < charactersByArticle.size(); i++) {
            List<TextPosition> textList = charactersByArticle.get( i );
            for (TextPosition p: textList) {
            	buff.append(p.getCharacter());
            }
        }
        String text = buff.toString();
        String[] result = {text};
        return (Iterator<String>) Arrays.asList(result);
	}

	/* (non-Javadoc)
	 * @see org.apache.pdfbox.util.PDFTextStripper#writePage()
	 */
	@Override
	protected void writePage() throws IOException {
        StringBuilder buff = new StringBuilder();
        for( int i = 0; i < charactersByArticle.size(); i++) {
        	System.err.println("Start of article");
            List<TextPosition> textList = charactersByArticle.get( i );
            for (TextPosition p: textList) {
            	buff.append(p.getCharacter());
            }
            String[] wordlist = buff.toString().split("[\\p{Space}\\p{Punct}]+");
            for (String w: wordlist) {
            	wordSet.add(w.toLowerCase());
            }
            buff.setLength(0);
        }
     //  writeString(documentText);
       output.flush();
	}

	public static void main(String[] args) {
		try {
			PdfDocument doc = new PdfDocument();
			doc.writeText(PDDocument.load(new File(args[0])), new OutputStreamWriter(System.out));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
