package org.lasalledebain.libris;

import java.net.URI;
import java.util.Objects;

public class ArtifactParameters {
	String date;
	String title;
	String comments;
	String keywords;
	String doi;
	String recordParentName;
	String recordName;
	URI source;
	public ArtifactParameters(URI source) {
		super();
		this.source = source;
		recordName = "";
		recordParentName = "";
		date = LibrisMetadata.getCurrentDateAndTimeString();
	}
	public String getSourceString() {
		return source.toASCIIString();
	}
	public URI getSource() {
		return source;
	}
	public String getDate() {
		return date;
	}
	public String getTitle() {
		return title;
	}
	public String getComments() {
		return comments;
	}
	public String getKeywords() {
		return keywords;
	}
	public String getDoi() {
		return doi;
	}
	public String getRecordParentName() {
		return recordParentName;
	}
	public String getRecordName() {
		return recordName;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public void setDoi(String doi) {
		this.doi = doi;
	}
	public void setRecordParentName(String recordParentName) {
		this.recordParentName = recordParentName;
	}
	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}
	public void setSource(URI source) {
		this.source = source;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (Objects.nonNull(obj) && getClass().isAssignableFrom(obj.getClass())) {
			ArtifactParameters other = (ArtifactParameters) obj;
			return
					Objects.equals(other.comments, comments)
					&& Objects.equals(other.date, date)
					&& Objects.equals(other.doi, doi)
					&& Objects.equals(other.keywords, keywords)
					&& other.source.equals(source)
					&& Objects.equals(other.recordName, recordName)
					&& Objects.equals(other.recordParentName, recordParentName)
					&& Objects.equals(other.title, title);
		} else return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder(100);
		buff.append("comments=\""); buff.append(comments); buff.append("\",");
		buff.append("date=\""); buff.append(date); buff.append("\",");
		buff.append("doi=\""); buff.append(doi); buff.append("\",");
		buff.append("keywords=\""); buff.append(keywords); buff.append("\",");
		buff.append("recordName=\""); buff.append(recordName); buff.append("\",");
		buff.append("recordParentName=\""); buff.append(recordParentName); buff.append("\",");
		buff.append("source=\""); buff.append(getSourceString()); buff.append("\",");
		buff.append("title=\""); buff.append(title); buff.append("\"");
		return buff.toString();
	}
}