package org.lasalledebain.libris;

import org.lasalledebain.libris.field.GenericField;

public interface FieldFactory {
	GenericField newField(FieldMasterCopy masterCopy);
}
