package Libris.pdf;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Strip;

import Libris.LibrisDatabase;
import Libris.LibrisRecord;
import Libris.LibrisSchema;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class ProcessPdfDocument extends LibrisRecord {
	private String pdfSource = null;
	private boolean outputMessages;
	private static final int UNIQUENESS = 4;
	private PdfDecoder decodePdf;
	private boolean isFile = true;
	private byte[] byteArray;

	public ProcessPdfDocument(LibrisDatabase database, LibrisSchema schema, String fileName) {
		super(database, schema);
		this.pdfSource = fileName;
		wordCount = 0;
		typeStyleCounts = new HashMap<String, Integer>();
		meanFontSize = 0;
	}


	WordHeap keywords;
	protected ArrayList<String> documentWords;
	protected ArrayList<WordInfo> docWordInfo;
	protected HashMap<String, Integer> typeStyleCounts;
	int meanFontSize;
	int wordCount;

	protected String getTypeStyle(String wordRecord) {
		return null;		
	}

	public void readDocument() {

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
				documentWords = new ArrayList<String>();
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

						documentWords.addAll(pageWords);
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

	/**
	 * 
	 * @param wordList word information as parsed from PDF
	 * @return list of words, de-hyphenated, font style and size separated out
	 * Also creates the font style stats and mean font size
	 * 
	 * Dehyphenation heuristic: if the current string is entirely alphabetic and ends in a hyphen and the following
	 * word is below and to the left and is entirely alphabetic, the word has been hyphenated.
	 */
	public List<WordInfo> parseWordInfo() {
		docWordInfo = new ArrayList<WordInfo>(documentWords.size());
		Iterator wordIterator=documentWords.iterator();
		WordInfo curWordInfo = null;
		int sizeCount = 0; /* number of words with associate size info */
		while(wordIterator.hasNext()){

			String currentWord=(String) wordIterator.next();

			/**remove the XML formatting if present - not needed for pure text*/
			// currentWord=Strip.convertToText(currentWord);

			/**if(currentWord.indexOf(" ")!=-1){
					System.out.println("word="+currentWord);
					System.exit(1);
				}*/

			int wx1=(int)Float.parseFloat((String) wordIterator.next());
			int wy1=(int)Float.parseFloat((String) wordIterator.next());
			int wx2=(int)Float.parseFloat((String) wordIterator.next());
			int wy2=(int)Float.parseFloat((String) wordIterator.next());
			docWordInfo.add(curWordInfo = new WordInfo(currentWord)) ;
			if (curWordInfo.size != 0) ++sizeCount;
			meanFontSize += curWordInfo.size;
			if (typeStyleCounts.get(curWordInfo.typeStyle) == null) {
				typeStyleCounts.put(curWordInfo.typeStyle, 1);
			} else {
				Integer i = typeStyleCounts.get(curWordInfo.typeStyle);
				i = i + 1;
			}
		}
		createWordStats(sizeCount);
		return docWordInfo;
	}

	protected void createWordStats(int sizeCount) {
		Double d = 0.0;
		
		if (sizeCount > 0) {
			meanFontSize /= sizeCount;
		}
		if (meanFontSize == 0) meanFontSize = 1;
		for (String element : typeStyleCounts.keySet()) {
			Integer s = typeStyleCounts.get(element);
			d = java.lang.Math.sqrt(sizeCount/s); // a style's influence is inversely proportional to its frequency
			s = d.intValue();
		}
		HashMap<String, Integer> wordSignificances = new HashMap<String, Integer>((d = java.lang.Math.sqrt(sizeCount)).intValue());
		for (WordInfo w: docWordInfo) {
			Integer i;
			String wordText = w.word.toLowerCase();
			if ((i = wordSignificances.get(w.word)) == null) {
				i = new Integer(0);
				wordSignificances.put(w.word, i);
			}
			if (w.size == 0) {
				w.size = meanFontSize;
			}
			i += w.size * typeStyleCounts.get(w.typeStyle);
		}
		
		keywords = new WordHeap(wordSignificances.size());
		for (String w: wordSignificances.keySet()) {
		// TODO convert keywords to lowercase
			keywords.add(w, wordSignificances.get(w));
		}
	}

	public ArrayList<String> getKeywords(int max) {
		ArrayList <String> result = new ArrayList<String>(max);
		
		for (int i = 0; i < max; ++i) {
			KeywordInfo w = keywords.removeFirstKeyword();
			if (w != null) result.add(i, w.text);
		}
		return result;
	}

	public class WordHeap {
		private int fill;
		private int fromPos, toPos;
		/* Now pick the largest N using the heap algorithm */
		int numUnique;
		 
		private ArrayList<KeywordInfo> sortBuffer = null;

		/**
		 * @param numUnique - number of unique keywords
		 */
		public WordHeap(int numUnique) {
			super();
			this.numUnique = numUnique;
			sortBuffer = new ArrayList<KeywordInfo>(numUnique);
			fill = 0;
		}
		
		void add(String word, int significance) {
			KeywordInfo parent, newNode;
			
			sortBuffer.add(fill, newNode = new KeywordInfo(word, significance));
			fromPos = fill;
			fill++;
			toPos = fromPos/2;
			parent = sortBuffer.get(toPos);
			while (toPos < fromPos) {
				if (newNode.signficance > parent.signficance) {
					sortBuffer.set(toPos, newNode);
					sortBuffer.set(fromPos, parent);
					fromPos = toPos;
					toPos = toPos/2;
				} else break; // already has heap property
			}
		}
		
		/**
		 * Destructive read of the first (most significant) keyword
		 * @return first item in the list
		 */
		
		KeywordInfo removeFirstKeyword() {
			KeywordInfo head, parent, leftChild, rightChild;
			int parentPos, lChildPos, rChildPos;

			if (fill == 0) { 
				return null;

			} else {
				head = sortBuffer.get(0);
				parent = sortBuffer.remove(fill-1);
				--fill;
				if (fill > 0) {
					sortBuffer.set(0, parent);
					parentPos = 0;
					while (parentPos*2 < fill) {
						lChildPos = 2 * parentPos;
						rChildPos = lChildPos + 1;
						if (rChildPos >= fill) rChildPos = lChildPos;
						parent = sortBuffer.get(parentPos);
						leftChild = sortBuffer.get(lChildPos);
						rightChild = sortBuffer.get(rChildPos);
						if (rightChild.signficance > leftChild.signficance) {
							if (rightChild.signficance > parent.signficance) {
								sortBuffer.set(parentPos, rightChild);
								sortBuffer.set(rChildPos, parent);
								parentPos = rChildPos;
							} else {
								break; /* the parent is correctly placed */
							}
						} else {
							if (leftChild.signficance > parent.signficance){
								sortBuffer.set(parentPos, leftChild);
								sortBuffer.set(lChildPos, parent);
								parentPos = lChildPos;
							} else {
								break; /* the parent is correctly placed */
							}
						}
					}
				}
			}
			return head;
		}

	}
	
	private class WordInfo {
		protected String word;
		protected String typeStyle;
		protected int size;

		static final String fontTag = "font";
		static final String faceRegex = ".*face=\"([^\"]*)\".*";
			// ".*face=\"([^\"]*)\".*";
		static final String sizeRegex = ".*style=\"font-size:(\\d*)pt\".*";

		/**
		 * @param word the word itself
		 * @param font font and style
		 * @param size point size
		 */
		public WordInfo(String xmlIn) {
			boolean gotInfo = false;
			StringTokenizer tokens =new StringTokenizer(xmlIn, "<>", true);

			Pattern facePattern = Pattern.compile(faceRegex);
			Pattern sizePattern = Pattern.compile(sizeRegex);
			String face = "";
			String sizeString = "";
			String text = "";
			int sizeValue = 0; // unknown
			while(tokens.hasMoreTokens() ){
				String curTok = tokens.nextToken();
				if (curTok.equals( "<" )) {
					if (tokens.hasMoreTokens() && !(curTok=tokens.nextToken()).equals("/") && curTok.startsWith(fontTag)) {
						Matcher m = facePattern.matcher(curTok);
						if (gotInfo) continue;
						else gotInfo = true;
						if (m.matches()) {
							face = m.group(1);
						}
						m = sizePattern.matcher(curTok);
						if (m.matches()) {
							sizeString = m.group(1);
							try {
							sizeValue = Integer.parseInt(sizeString);
							} catch (NumberFormatException e) {
								sizeValue = 0; 
							}
						}
					}
				} else if (curTok.equals(">")) {
					continue;
				} else {
					text = text+curTok;
				}
			}
			this.word = text;
			this.typeStyle = face;
			this.size = sizeValue;
		}
		
	}
	private class KeywordInfo {
		public String text;
		public int signficance;
		/**
		 * @param text
		 * @param signficance
		 */
		public KeywordInfo(String text, int signficance) {
			super();
			this.text = text;
			this.signficance = signficance;
		}

		public boolean isMoreSignificantThan(KeywordInfo other) {
			return (this.signficance > other.signficance);
		}
	}

}
