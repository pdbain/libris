/**
 * 
 */
package org.lasalledebain.libris.field;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

class ReadOnlyField extends GenericField {
	@Override
	public String getElementTag() {
		throw new DatabaseError("Cannot call toXml on "+getClass().getName());
	}

	@Override
	public void fromXml(ElementManager mgr) {
		throw new DatabaseError("Cannot call toXml on "+getClass().getName());
	}

	@Override
	public void addURLValue(URL value) throws FieldDataException {
		throwReadOnlyException();
	}

	public ReadOnlyField(Field actualField) {
		this.actualField = actualField;
	}

	@Override
	public boolean isText() {
		return (Objects.isNull(actualField))? false: actualField.isText();
	}

	Field actualField;
	@Override
	public void addIntegerValue(int value) throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	protected FieldValue valueOf(String valueString) throws FieldDataException {
		throwReadOnlyException();
		return null;
	}

	@Override
	protected FieldValue valueOf(int value, String valueString) throws FieldDataException {
		throwReadOnlyException();
		return null;
	}

	protected void throwReadOnlyException() throws FieldDataException {
		throw new FieldDataException("field is read-only");
	}

	@Override
	public void addValuePair(String value, String extraValue)
			throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	public void addValuePair(Integer value, String extraValue)
			throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	public void changeValue(String string) throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	public void changeValue(FieldValue fieldValue)
			throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	public Field duplicate() throws FieldDataException {
		return actualField.duplicate();
	}

	@Override
	public boolean equals(Field comparand) {
		return actualField.equals(comparand);
	}

	@Override
	public String getFieldId() {
		return actualField.getFieldId();
	}

	@Override
	public Iterable<FieldValue> getFieldValues() {
		return actualField.getFieldValues();
	}

	@Override
	public Optional<FieldValue> getFirstFieldValue() {
		return actualField.getFirstFieldValue();
	}

	@Override
	public EnumFieldChoices getLegalValues() {
		return actualField.getLegalValues();
	}

	@Override
	public int getNumberOfValues() {
		return actualField.getNumberOfValues();
	}

	@Override
	public FieldType getType() {
		return actualField.getType();
	}

	@Override
	public String getValuesAsString() {
		return actualField.getValuesAsString();
	}

	@Override
	public boolean isEmpty() {
		return actualField.isEmpty();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public boolean isRestricted() {
		return actualField.isRestricted();
	}

	@Override
	public boolean isSingleValue() {
		return actualField.isSingleValue();
	}

	@Override
	public boolean isTrue() throws FieldDataException {
		return actualField.isTrue();
	}

	@Override
	public Field getReadOnlyView() {
		return this;
	}

	@Override
	public FieldValue removeValue() {
		throw new DatabaseError("changing read-only field");
	}

	@Override
	public void setValues(FieldValue[] valueArray) throws FieldDataException {
		throw new DatabaseError("changing read-only field");
	}

	@Override
	public void setValues(Iterable<FieldValue> values) throws FieldDataException {
		throw new DatabaseError("changing read-only field");
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		return actualField.getAttributes();
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		return;
	}

	@Override
	public void addValue(FieldValue fieldData) throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		throwReadOnlyException();
	}

	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return false;
	}

}