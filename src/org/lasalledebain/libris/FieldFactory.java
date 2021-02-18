package org.lasalledebain.libris;

import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;

public interface FieldFactory {
	GenericField<? extends FieldValue> newField(FieldTemplate masterCopy);
}
