package org.lasalledebain.libris.records;

import static org.lasalledebain.libris.util.LibrisStemmer.stem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.util.StringUtils;;

public class PdfRecordImporter {
	private static final int KEYWORD_LIMIT = 100;
	private static final int MIN_ABSTRACT_LENGTH = 1000;
	LibrisDatabase recordDatabase;
	Repository artifactRepository;
	short abstractField, keywordsField;
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

	public void importDocument(URI sourceFileUri, Function<String, Integer> documentFrequency, Record rec) throws LibrisException, IOException {
		
		/* copy file to repository */
		int artifactId = artifactRepository.importFile(new ArtifactParameters(sourceFileUri));
		/* set repositoryIdField in rec*/
		rec.setArtifactId(artifactId);
		String docString = pdfToText(sourceFileUri);
		if (setKeywords) {
			 Stream<String> terms = StringUtils.chooseTerms(StringUtils.getTerms(docString, true), 
					 documentFrequency, KEYWORD_LIMIT);
			Optional<String> keywords = terms.reduce((s, t) -> s + ' ' + t);
			if (keywords.isPresent()) {
				rec.addFieldValue(keywordsField, keywords.get());
			}
		}
		/* set abstract if field not null */
		if (setAbstract) {
			/* go 1000 characters (~150 words) into the document and find the end of a sentence. */
			int endOfAbstract = docString.indexOf('.', MIN_ABSTRACT_LENGTH);
			if ((endOfAbstract < 0) || (endOfAbstract > MIN_ABSTRACT_LENGTH + 100)) {
				endOfAbstract = MIN_ABSTRACT_LENGTH;
			}
				endOfAbstract = Math.min(endOfAbstract, docString.length());
				rec.addFieldValue(abstractField, StringUtils.replaceNonwordOrSpaceChars(docString.substring(0, endOfAbstract), " "));
		}
	}

	public void importPDFDocuments(Iterable<URI> sourceFiles, Record parentRecord) 
			throws MalformedURLException, IOException, LibrisException {
		final int parentId = parentRecord.getRecordId();
		Function<String, Integer> documentFrequency = recordDatabase.getDocumentFrequencyFunction();
		for (URI sourceFileUri: sourceFiles) {
			final String documentText = pdfToText(sourceFileUri);
			recordDatabase.incrementTermCounts(StringUtils.getTerms(documentText, true).map(w -> stem(w)));
		}
		for (URI sourceFileUri: sourceFiles) {
			Record rec = recordDatabase.newRecord();
			importDocument(sourceFileUri, documentFrequency, rec);
			rec.setParent(0, parentId);
			recordDatabase.put(rec);
		}
	}

	public void setAbstractField(short abstractField) {
		this.abstractField = abstractField;
		setAbstract = true;
	}

	public void setKeywordsField(short keywordsField) {
		this.keywordsField = keywordsField;
		setKeywords = true;
	}

	private static String pdfToText(URI sourceFileUri) throws IOException, MalformedURLException {
		/* extract text */
		PDDocument pdfDoc = PDDocument.load(sourceFileUri.toURL());
		PDFTextStripper doc = new PDFTextStripper();
		String docString = doc.getText(pdfDoc).trim();
		pdfDoc.close();
		return docString;
	}
}
