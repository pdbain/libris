package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class LibrisFileChooser extends JFileChooser {
	LibrisWindowedUi gui;
	Component parent;
	String title;
	
	public LibrisFileChooser(LibrisWindowedUi gui, String title) {
		this.gui = gui;
		if (Objects.nonNull(gui)) {
			parent = gui.mainFrame;
		} else {
			parent = null;
		}
		this.title = title;
		setDialogTitle(title);
	}

	public File chooseInputFile() throws HeadlessException {
		int option = showOpenDialog(gui.mainFrame);
		if (JFileChooser.APPROVE_OPTION == option) {
			return getSelectedFile();
		} else {
			return null;
		}
	}

	public File chooseOutputFile(String fileName) throws HeadlessException {
		setSelectedFile(new File(fileName));
		int option = showDialog(Objects.nonNull(gui)? gui.mainFrame: null, "Export");
		File result = null;
		if (JFileChooser.APPROVE_OPTION == option) {
			result = getSelectedFile();
		}
		if (!fileNotNullAndNonexistent(parent, result)) {
			result = null;
		}
		return result;
	}
	
	public  boolean fileNotNullAndNonexistent(Component parent, File theFile) {
		boolean result = false;
		if (Objects.nonNull(theFile)) {
			if (theFile.exists()) {
				if (theFile.canWrite()) {
					int selection = Dialogue.yesNoDialog(parent, theFile.getName()+" exists.  Write anyway?");
					result = JOptionPane.YES_OPTION == selection;
				} else {
					gui.alert(theFile.getName()+" is read-only");
				}
			} else {
				result = true;
			}
		}
		return result;
	}

}
