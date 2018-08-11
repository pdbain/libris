package org.lasalledebain.libris.records;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.Repository.ArtifactParameters;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.util.Stemmer;

public class PdfRecordImporter {
	private static final int MIN_ABSTRACT_LENGTH = 1000;
	LibrisDatabase recordDatabase;
	Repository artifactRepository;
	short abstractField, keywordsField;
	public void setAbstractField(short abstractField) {
		this.abstractField = abstractField;
		setAbstract = true;
	}

	public void setKeywordsField(short keywordsField) {
		this.keywordsField = keywordsField;
		setKeywords = true;
	}
	boolean setAbstract;
	boolean setKeywords;
	public PdfRecordImporter(LibrisDatabase recordDatabase, Repository artifactRepository) {
		this.recordDatabase = recordDatabase;
		this.artifactRepository = artifactRepository;
		setAbstract = false;
		setKeywords = false;
	}
	
	public PdfRecordImporter(LibrisDatabase recordDatabase, Repository artifactRepository, short keyField, short absField) {
		this.recordDatabase = recordDatabase;
		this.artifactRepository = artifactRepository;
		setAbstract = false;
		setKeywords = false;
		setKeywordsField(keyField);
		setAbstractField(absField);
	}
	
	public void importDocument(URI sourceFileUri, Record rec) throws LibrisException, IOException {
		
		/* copy file to repository */
		int artifactId = artifactRepository.importFile(new ArtifactParameters(sourceFileUri));
		/* set repositoryIdField in rec*/
		rec.setArtifactId(artifactId);
		/* extract text */
		PDDocument pdfDoc = PDDocument.load(sourceFileUri.toURL());
		PDFTextStripper doc = new PDFTextStripper();
		String docString = doc.getText(pdfDoc).trim();
		if (setKeywords) {
			Set <String> terms = getTerms(docString);
			StringBuilder keywordsString = new StringBuilder(5 * terms.size());
			for (String s: terms) {
				keywordsString.append(' ');
				keywordsString.append(s);
			}
			rec.addFieldValue(keywordsField, keywordsString.toString());
		}
		/* set abstract if field not null */
		if (setAbstract) {
			/* go 1000 characters (~150 words) into the document and find the end of a sentence. */
			int endOfAbstract = docString.indexOf('.', MIN_ABSTRACT_LENGTH);
			if ((endOfAbstract < 0) || (endOfAbstract > MIN_ABSTRACT_LENGTH + 100)) {
				endOfAbstract = MIN_ABSTRACT_LENGTH;
			}
				endOfAbstract = Math.min(endOfAbstract, docString.length());
				rec.addFieldValue(abstractField, docString.substring(0, endOfAbstract));
		}
	}
// TODO limit strings based on term frequency/inverse document frequency
	private Set<String> getTerms(String docString) {
        HashSet<String> termSet = new HashSet<>(Arrays.asList(docString.toLowerCase().split("[\\p{Space}\\p{Punct}]+")));
        HashSet<String> stems = new HashSet<>(termSet.size()/2);
        for (String word: termSet) {
        	Stemmer wordStemmer = new Stemmer(word.toCharArray());
        	stems.add(wordStemmer.toString());
        }
        termSet.addAll(stems);
		return termSet;
	}
}
