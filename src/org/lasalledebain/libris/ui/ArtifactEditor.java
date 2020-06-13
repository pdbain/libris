package org.lasalledebain.libris.ui;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lasalledebain.libris.ArtifactParameters;
import static org.lasalledebain.libris.util.StringUtils.isStringEmpty;


public class ArtifactEditor {
	private Frame ownerFrame;
	private JDialog dLog;
	/* Editable fields */
	private JTextField title, doi;
	private JTextArea comments, keywords;

	/* Fixed fields */
	private JTextField recordName, parentName, date, sourcePath;
	private boolean modified = false;
	ArtifactParameters result = null;
	private JButton closeButton;
	private JButton saveButton;
	private JTextField archivePath;

	/**
	 * @param params information about the artifact.  May be modified
	 * @param ui
	 */
	public ArtifactEditor(ArtifactParameters params, LibrisGui ui) {
		boolean editable = ui.isEditable();
		DocumentListener listener = new DocumentListener() {
			protected void modificationAction() {
				modified = true;
				closeButton.setText("Close and discard changes");
				saveButton.setEnabled(true);
			}


			@Override
			public void removeUpdate(DocumentEvent e) {
				modificationAction();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				modificationAction();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				modificationAction();
			}
		};
		ownerFrame = ui.getMainFrame();
		dLog = new JDialog(ownerFrame, "Edit artifact information", true);
		final JPanel fieldsPane = new JPanel();
		fieldsPane.setLayout(new BoxLayout(fieldsPane, BoxLayout.Y_AXIS));

		title = new JTextField(params.getTitle());
		title.setBorder(new TitledBorder("Title"));
		title.setEnabled(editable);
		title.getDocument().addDocumentListener(listener);
		fieldsPane.add(title);

		String recName = params.getRecordName();
		if (!isStringEmpty(recName)) {
			recordName = new JTextField(recName);
			recordName.setBorder(new TitledBorder("Record name"));
			recordName.setEditable(false);
			fieldsPane.add(recordName);
		}

		String recParentName = params.getRecordParentName();
		if (!isStringEmpty(recParentName)) {
			parentName = new JTextField(recParentName);
			parentName.setBorder(new TitledBorder("Parent name"));
			parentName.setEditable(false);
			fieldsPane.add(parentName);
		}

		date = new JTextField(params.getDate());
		date.setBorder(new TitledBorder("Date"));
		date.setEditable(false);
		fieldsPane.add(date);

		doi = new JTextField(params.getDoi());
		fieldsPane.add(doi);
		doi.setBorder(new TitledBorder("Digital Object Identifier"));
		doi.getDocument().addDocumentListener(listener);
		doi.setEnabled(editable);

		sourcePath = new JTextField(params.getSourceString());
		fieldsPane.add(sourcePath);
		sourcePath.setBorder(new TitledBorder("Source path"));
		sourcePath.setEditable(false);

		archivePath = new JTextField(params.getArchivePathString());
		fieldsPane.add(archivePath);
		archivePath.setBorder(new TitledBorder("Archive path"));
		archivePath.setEditable(false);

		comments= new JTextArea(params.getComments());
		fieldsPane.add(comments);
		comments.setBorder(new TitledBorder("Comments"));
		comments.getDocument().addDocumentListener(listener);
		comments.setEnabled(editable);

		keywords= new JTextArea(params.getKeywords());
		keywords.setBorder(new TitledBorder("Keywords"));
		keywords.getDocument().addDocumentListener(listener);
		fieldsPane.add(keywords);
		keywords.setEnabled(editable);

		JPanel buttonBar = new JPanel(new FlowLayout());
		closeButton = new JButton("Close");
		ActionListener closeListener = e -> this.dialogueDispose();
		closeButton.addActionListener(closeListener);
		buttonBar.add(closeButton);

		saveButton = new JButton("Save");
		closeButton.addActionListener(e -> this.dialogueDispose());
		saveButton.setEnabled(false);
		buttonBar.add(saveButton);
		ActionListener saveListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!modified) return;
				result = new ArtifactParameters();
				String tempText = title.getText();
				if (!isStringEmpty(tempText)) result.setTitle(tempText);
				tempText = doi.getText();
				if (!isStringEmpty(tempText)) result.setDoi(tempText);
				tempText = comments.getText();
				if (!isStringEmpty(tempText)) result.setComments(tempText);
				tempText = keywords.getText();
				if (!isStringEmpty(tempText)) result.setKeywords(tempText);
				dialogueDispose();
			}
		};
		saveButton.addActionListener(saveListener);
		fieldsPane.add(buttonBar);
		dLog.setContentPane(fieldsPane);
		dLog.pack();
		dLog.setLocationRelativeTo(ownerFrame);
		dLog.setVisible(true);
	}

	private void dialogueDispose() {
		dLog.setVisible(false);
		dLog.dispose();
	}

}
