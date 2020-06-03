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
	public boolean equals(FieldValue comparand) {
		if (comparand.getClass() == getClass()) {
			FieldLocationValue otherValue = (FieldLocationValue) comparand;
			if (location.equals(otherValue.location)) {
				if (null == locationName) {
					return (null == otherValue.locationName);
				} else {
					return locationName.equals(otherValue.locationName);
				}
			}
		}
		return false;
	}
	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		if (comparand.getClass() == getClass()) {
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

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.field.FieldValue#getNumberOfValues()
	 */
	@Override
	public int getNumberOfValues() {
		return (Objects.nonNull(locationName) && !locationName.isEmpty())? 1: 0;
	}

	@Override
	public FieldValue duplicate() {
		FieldLocationValue dup = new FieldLocationValue(location, locationName);
		return dup;
	}


}
