package org.lasalledebain.libris.ui;

import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;

import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;

class RecordNameChooser extends DefaultComboBoxModel {

	private final SortedKeyValueFileManager<KeyIntegerTuple> namedRecordIndex;
	private StringBuffer currentPrefix;

	RecordNameChooser(KeyIntegerTuple editedValue, SortedKeyValueFileManager<KeyIntegerTuple> recIndex) {
		currentPrefix = new StringBuffer(16);
		namedRecordIndex = recIndex;
		initializeList(editedValue);
	}

	private void initializeList(KeyIntegerTuple editedValue) {
		removeAllElements();
		int parentNum = RecordId.getNullId();
		if (null != editedValue) {
			parentNum = editedValue.getValue();
		}
		for (KeyIntegerTuple recNameAndNumber: namedRecordIndex) {
			addElement(recNameAndNumber);
			if (parentNum == recNameAndNumber.getValue()) {
				setSelectedItem(recNameAndNumber);
				editedValue = null;
			}
		}
	}

	String keyTyped(char prefixChar) {
		int lastIndex = currentPrefix.length();
		if ((KeyEvent.VK_BACK_SPACE == prefixChar) || (KeyEvent.VK_DELETE == prefixChar)) {
			if (lastIndex > 0) {

				currentPrefix.deleteCharAt(lastIndex-1);
				initializeList(null);
			}
		} else if (((lastIndex > 0) && Character.isJavaIdentifierPart(prefixChar))
				|| (lastIndex == 0) && Character.isJavaIdentifierStart(prefixChar)) {
			currentPrefix.append(prefixChar);
		}
		String prefix = currentPrefix.toString();
		int currentSize = getSize();
		int currentIndex = 0;
		while (currentIndex < currentSize) {
			KeyIntegerTuple element = (KeyIntegerTuple) getElementAt(currentIndex);
			String e = element.getKey();
			if (!e.startsWith(prefix)) {
				removeElementAt(currentIndex);
				--currentSize;
			} else {
				++currentIndex;
			}
		}
		if (0 == currentSize) {
			addElement("");
		}
		fireContentsChanged(this, 0, currentSize-1);
		return prefix;
	}
}