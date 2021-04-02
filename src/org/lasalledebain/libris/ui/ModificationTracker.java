/**
 * 
 */
package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.ActionListener;

import org.lasalledebain.libris.Record;

/**
 * @author pdbain
 * keeps track of whether a data have changed. 
 */
public class ModificationTracker {
	private boolean modified;
	private boolean modifiable;
	/**
	 * @param ui user interface (GUI, command line, batch, etc.)
	 * @param modificationListener 
	 * @param parentComponent parent GUI element
	 * @param dialogueMessage printed if database closed with modified records
	 */
	public ModificationTracker(DatabaseUi<Record> ui, ActionListener modificationListener, 
			Component parentComponent, String dialogueMessage) {
		this.modificationListener = modificationListener;
		this.dialogueMessage = dialogueMessage;
		this.ui = ui;
		ui.setSelectedField(null);
		ui.fieldSelected(false);
		setModifiable(false);
		modified = false;
	}


	public boolean isModifiable() {
		return modifiable;
	}


	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}

	private ActionListener modificationListener;
	String dialogueMessage;
	private DatabaseUi<Record> ui;
	public synchronized boolean isModified() {
		return modified;
	}

	public synchronized void setModified(boolean modified) {
 		if ((this.modified != modified) && (null != modificationListener)) {
			this.modified = modified;
			modificationListener.actionPerformed(null);
		}
	}
	
	/**
	 * check if there are modifications. If so, put up a dialogue and return a save yes/no/cancel value as defined in Dialogue.
	 * @return no if the component is unchanged, otherwise the result of a dialogue.
	 */
	public int checkClose() {
		if (isModified()) {
			return ui.confirmWithCancel(dialogueMessage);
		}
		return Dialogue.NO_OPTION;
	}

	public void select(UiField uiField) {
		ui.setSelectedField(uiField);
		uiField.setSelected(true);
		ui.fieldSelected(true);
	}


	public void unselect() {
		UiField selectedField = ui.getSelectedField();
		if (null != selectedField) {
			selectedField.setSelected(false);
		}
		selectedField = null;
		ui.fieldSelected(false);
	}
}
