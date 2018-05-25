package org.lasalledebain.libris.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordIdNameMapper;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.index.GroupDef;

public class AffiliatesField extends GenericField implements Field, Iterable<FieldValue>{
	private int affiliates[];
	private GroupDef grp;
	private RecordIdNameMapper mapper;

	public AffiliatesField(FieldTemplate template) {
		super(template);
	}

	@Override
	public void addIntegerValue(int value) throws FieldDataException {
		int pos;
		if (null == affiliates) {
			affiliates = new int[1];
			pos = 0;
		} else {
			pos = affiliates.length+1;
			int[] newAffiliates = new int[pos];
			System.arraycopy(affiliates, 0, newAffiliates, 0, affiliates.length);
			affiliates = newAffiliates;
		}
		affiliates[pos] = value;
	}

	@Override
	public void addValue(String recName) throws FieldDataException {
		try {
			int recId = mapper.getId(recName);
			if (RecordId.getNullId() == recId) {
				throw new FieldDataException("Record "+recName+" not found");
			}
			addIntegerValue(recId);
		} catch (InputException e) {
			throw new FieldDataException("Error looking up record "+recName, e);
		}
	}

	@Override
	public void addValuePair(String value, String extraValue)
			throws FieldDataException {
		throw new FieldDataException("addValuePair not permitted on this field");

	}

	@Override
	public void addValuePair(Integer value, String extraValue)
			throws FieldDataException {
		throw new FieldDataException("addValuePair not permitted on this field");
	}

	@Override
	public void changeValue(String recName) throws FieldDataException {
		try {
			int recId = mapper.getId(recName);
			setValue(recName, recId);
		} catch (InputException e) {
			throw new FieldDataException("Error looking up record "+recName, e);
		}
	}

	private void setValue(String recName, int recId) throws FieldDataException {
		if (RecordId.getNullId() == recId) {
			throw new FieldDataException("Record "+recName+" not found");
		}
		affiliates = new int[1];
		affiliates[0] = recId;
	}

	@Override
	public void changeValue(FieldValue fieldValue) throws FieldDataException {
		int recId = fieldValue.getValueAsInt();
		if (RecordId.getNullId() == recId) {
			String recName = fieldValue.getValueAsString();
			changeValue(recName);
		} else {
			setValue(fieldValue.getValueAsString(), recId);
		}
	}

	@Override
	public Field duplicate() throws FieldDataException {
		return null;
	}

	@Override
	public boolean equals(Field comparand) {
		if (comparand.getType() == FieldType.T_FIELD_AFFILIATES) {
			AffiliatesField other = (AffiliatesField) comparand;
			if (Arrays.equals(affiliates, other.affiliates)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getFieldId() {
		return grp.getFieldId();
	}

	@Override
	public Iterable<FieldValue> getFieldValues() {
		return this;
	}

	@Override
	public FieldValue getFirstFieldValue() {
		if ((null == affiliates) || (affiliates.length == 0)) {
			return new FieldNullValue();
		} else {
			return new FieldIntValue(affiliates[0]);
		}
	}

	@Override
	public EnumFieldChoices getLegalValues() {
		return null;
	}

	@Override
	public int getNumberOfValues() {
		if ((null == affiliates) || (affiliates.length == 0)) {
			return 0;
		} else {
			return affiliates.length;
		}
	}

	@Override
	public Field getReadOnlyView() {
		return new ReadOnlyField(this);
	}

	@Override
	public String getValuesAsString() {
		StringBuilder buff = new StringBuilder();
		if (!isEmpty()) {
		String separator = "";
		for (int i: affiliates) {
			String recName;
			try {
				recName = mapper.getName(i);
			} catch (InputException e) {
				throw new InternalError("error mapping record "+i, e);
			}
			buff.append(separator);
			buff.append(recName);
			separator = ", ";
		}
		}
		return buff.toString();
	}

	@Override
	public boolean isEmpty() {
		return (null == affiliates) || (affiliates.length == 0);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isSingleValue() {
		return false;
	}

	@Override
	public FieldValue removeValue() {
		FieldValue result;
		if (isEmpty()) {
			result = new FieldNullValue();
		} else {
			result = new FieldIntValue(affiliates[0]);
			if (affiliates.length == 1) {
				affiliates = null;
			} else {
				int newLen = affiliates.length-1;
				int[] newAffiliates = new int[newLen];
				System.arraycopy(affiliates, 1, newAffiliates, 0, newLen);
				affiliates = newAffiliates;

			}
		}
		return null;
	}

	@Override
	public void setValues(FieldValue[] valueArray) throws FieldDataException {
		setValues(Arrays.asList(valueArray));
	}

	@Override
	public void setValues(Iterable<FieldValue> values)
			throws FieldDataException {
		ArrayList<Integer> buff = new ArrayList<Integer>();
		for (FieldValue v: values) {
			int recId = v.getValueAsInt();
			if (RecordId.getNullId() == recId) {
				String recName = v.getValueAsString();

				try {
					recId = mapper.getId(recName);
				} catch (InputException e) {
					throw new FieldDataException("Error looking up record "+recName, e);
				}
				if (RecordId.getNullId() == recId) {
					throw new FieldDataException("Error looking up record "+recName);
				}
				buff.add(recId);
			}
		}
		affiliates = new int[buff.size()];
		int index = 0;
		for (Integer i: buff) {
			affiliates[index++] = i.intValue();
		}
	}

	@Override
	public Iterator<FieldValue> iterator() {
		return new Iterator<FieldValue>() {

			int index = 0;
			@Override
			public boolean hasNext() {
				return ((null != affiliates) && (index < affiliates.length));
			}

			@Override
			public FieldValue next() {
				return new FieldIntValue(affiliates[index++]);
			}

			@Override
			public void remove() {
				return;
			}
			
		};
	}

}
