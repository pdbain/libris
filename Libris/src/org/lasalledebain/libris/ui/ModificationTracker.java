/**
 * 
 */
package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.ActionListener;

import org.lasalledebain.libris.LibrisConstants.DatabaseUsageMode;

/**
 * @author pdbain
 * keeps track of whether a data have changed. 
 */
public class ModificationTracker {
	private boolean modified = false;
	private boolean modifiable;
	public boolean isModifiable() {
		return modifiable;
	}


	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}


	private DatabaseUsageMode usage;
	private ActionListener modificationListener;
	String dialogueMessage;
	private LibrisUi ui;
	public LibrisUi getUi() {
		return ui;
	}


	/**
	 * @param database usage indicate whether we are using a GUI, command line, or batch
	 * @param parentComponent Java component
	 * @param dialogueMessage string to use the query of the user wants to store the changed data
	 * @param title 
	 */
	/**
	 * @param ui user interface (GUI, command line, batch, etc.)
	 * @param modificationListener 
	 * @param parentComponent parent GUI element
	 * @param dialogueMessage printed if database closed with modified records
	 */
	public ModificationTracker(LibrisUi ui, ActionListener modificationListener, 
			Component parentComponent, String dialogueMessage) {
		this.modificationListener = modificationListener;
		this.dialogueMessage = dialogueMessage;
		this.ui = ui;
		ui.setSelectedField(null);
		ui.fieldSelected(false);
		setModifiable(false);
	}


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

	public void select(UiField selectedField) {
		ui.setSelectedField(selectedField);
		selectedField.setSelected(true);
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
