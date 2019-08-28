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
	int parentId;
	String recordName;
	URI source;
	public ArtifactParameters(URI source) {
		super();
		this.source = source;
		recordName = "";
		recordParentName = "";
		date = LibrisMetadata.getCurrentDateAndTimeString();
		parentId = RecordId.NULL_RECORD_ID;
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
	public int getParentId() {
		return parentId;
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
					&& Objects.equals(other.parentId, parentId)
					&& Objects.equals(other.title, title);
		} else return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		BufferBuilder buff = new BufferBuilder();
		buff.append("comments", comments);
		buff.append("date", date);
		buff.append("doi", doi);
		buff.append("keywords", keywords);
		buff.append("recordName", recordName);
		if (parentId != RecordId.NULL_RECORD_ID) {
			buff.append("parentId", Integer.toString(parentId));
		}
		buff.append("recordParentName", recordParentName);
		buff.append("source", getSourceString());
		buff.append("title", title);
		return buff.toString();
	}
	static class BufferBuilder {
		private boolean first;
		private StringBuilder buff;

		BufferBuilder() {
			first = true;
			 buff = new StringBuilder(100);
		}
		
		void append(String key, String value) {
			if (Objects.nonNull(value) && !value.isEmpty()) {
				if (!first) {
					buff.append(", ");
				}
				first = false;
				buff.append(key);
				buff.append("=\""); 
				buff.append(value);
				buff.append("\"");		
			}
		}

		@Override
		public String toString() {
			return buff.toString();
		}
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
}