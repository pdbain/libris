package org.lasalledebain.libris.field;

import java.util.Objects;

import org.lasalledebain.libris.exception.FieldDataException;

public class FieldLocationValue extends FieldValue {

	final String location;
	final String locationName;
	public FieldLocationValue(String loc, String locName) {
		location = loc;
		locationName = locName;
	}

	@Override
	public String getValueAsString() {
		return (Objects.isNull(locationName))? location: location+": "+locationName;
	}

	@Override
	protected boolean equals(FieldValue comparand) {
		if (comparand instanceof FieldLocationValue) {
			FieldLocationValue otherValue = (FieldLocationValue) comparand;
			return (location.equals(otherValue.location));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.field.FieldValue#getMainValueAsKey()
	 */
	@Override
	public String getMainValueAsKey() throws FieldDataException {
		return super.getMainValueAsKey();
	}

	@Override
	public FieldValue duplicate() {
		FieldLocationValue dup = new FieldLocationValue(location, locationName);
		return dup;
	}


}
