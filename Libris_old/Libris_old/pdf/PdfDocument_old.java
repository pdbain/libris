package Libris.pdf;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Strip;

public class PdfDocument_old {
	/* DO NOT USE */
	
	private String pdfSource = null;
	private boolean outputMessages;
	private PdfDecoder decodePdf;
	private boolean isFile = true;
	private byte[] byteArray;
	private ArrayList docWords = null;

	/**
	 * @param pdfSource
	 */
	public PdfDocument_old(String pdfSource) {
		super();
		this.pdfSource = pdfSource;
		docWords = new ArrayList();
	}
	
	public void readDocument(OutputStreamWriter outputStream ) {
		
		/**if you do not require XML content, pure text extraction 
		 * is much faster.
		 * 
		 */
		PdfDecoder.useXMLExtraction();
		
		//PdfDecoder returns a PdfException if there is a problem
		try {
			decodePdf = new PdfDecoder(false);
			decodePdf.setExtractionMode(PdfDecoder.TEXT); //extract just text
			decodePdf.init(true);
			//make sure widths in data CRITICAL if we want to split lines correctly!!

			PdfGroupingAlgorithms.useUnrotatedCoords=false;
			
        		/**
			 * open the file (and read metadata including pages in  file)
			 */
			if(isFile)
				decodePdf.openPdfFile(pdfSource);
			else
				decodePdf.openPdfArray(byteArray);
		} catch (PdfSecurityException e) {
			System.err.println("Exception " + e+" in pdf code for wordlist"+pdfSource);
			e.printStackTrace();
		} catch (PdfException e) {
			System.err.println("Exception " + e+" in pdf code for wordlist"+pdfSource);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception " + e+" in pdf code for wordlist"+pdfSource);
			e.printStackTrace();
		}
	
		/**
		 * extract data from pdf (if allowed). 
		 */
        if(!decodePdf.isExtractionAllowed()){
            if(outputMessages)
				System.out.println("Text extraction not allowed");
        }else if (decodePdf.isEncrypted() && !decodePdf.isPasswordSupplied()) {
			if(outputMessages){
				System.out.println("Encrypted settings");
				System.out.println("Please look at SimpleViewer for code sample to handle such files");
			}
		} else{
			//page range
			int start = 1, end = decodePdf.getPageCount();
			
			/**
			 * extract data from pdf
			 */
			try {
				for (int page = start; page < end + 1; page++) { //read pages
				
					//decode the page
					decodePdf.decodePage(page);
			
					/** create a grouping object to apply grouping to data*/
					PdfGroupingAlgorithms currentGrouping =decodePdf.getGroupingObject();
				
					/**use whole page size for  demo - get data from PageData object*/
					PdfPageData currentPageData = decodePdf.getPdfPageData();
				
					int x1 = currentPageData.getMediaBoxX(page);
					int x2 = currentPageData.getMediaBoxWidth(page)+x1;

					int y2 = currentPageData.getMediaBoxX(page);
					int y1 = currentPageData.getMediaBoxHeight(page)-y2;
					
					/**Co-ordinates are x1,y1 (top left hand corner), x2,y2(bottom right) */
					
					/**The call to extract the list*/
					List pageWords =null;
					
					try{
						pageWords =currentGrouping.extractTextAsWordlist(
							x1,
							y1,
							x2,
							y2,
							page,
							true,
							true,"&:=()!;.,\\/\"\"\'\'");
					} catch (PdfException e) {
						decodePdf.closePdfFile();
						System.err.println("Exception= "+ e+" in "+pdfSource);
					}
					
					if (pageWords == null) {
						if(outputMessages)
						System.out.println("No text found");
						
					} else {
						/**
						 * output the data
						 */
							
						docWords.addAll(pageWords);
						Iterator wordIterator=pageWords.iterator();
						while(wordIterator.hasNext()){
								
								String currentWord=(String) wordIterator.next();
								
								/**remove the XML formatting if present - not needed for pure text*/
								currentWord=Strip.convertToText(currentWord);
								
								/**if(currentWord.indexOf(" ")!=-1){
									System.out.println("word="+currentWord);
									System.exit(1);
								}*/
							
								int wx1=(int)Float.parseFloat((String) wordIterator.next());
								int wy1=(int)Float.parseFloat((String) wordIterator.next());
								int wx2=(int)Float.parseFloat((String) wordIterator.next());
								int wy2=(int)Float.parseFloat((String) wordIterator.next());
							
								/**this could be inserting into a database instead*/
							outputStream.write(currentWord+","+wx1+","+wy1+","+wx2+","+wy2+"\n");	

						}
						
					}

					//remove data once written out
					decodePdf.flushObjectValues(false);
					
				}
			} catch (Exception e) {
				decodePdf.closePdfFile();
				System.err.println("Exception "+ e+" in "+pdfSource);
				e.printStackTrace();
			}

			/**
			 * flush data structures - not strictly required but included
			 * as example
			 */
			decodePdf.flushObjectValues(true); //flush any text data read
		}

		/**close the pdf file*/
		decodePdf.closePdfFile();
		
		decodePdf=null;	
	}
	
}
