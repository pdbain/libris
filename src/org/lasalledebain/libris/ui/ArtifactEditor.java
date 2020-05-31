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
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.ui.AffiliateEditor.RecordSelectorByName;

public class ArtifactEditor {
	Frame ownerFrame;
	JDialog dLog;
	/* Editable fields */
	JTextField title, doi;
	JTextArea comments, keywords;
	
	/* Fixed fields */
	JTextField recordName, parentName, date, sourcePath;
	boolean modified = false;
	ArtifactParameters result = null;

	/**
	 * @param params information about the artifact.  May be modified
	 * @param ui
	 */
	public ArtifactEditor(ArtifactParameters params, LibrisWindowedUi ui, SortedKeyValueFileManager<KeyIntegerTuple> namedRecIndex) {
		DocumentListener listener = new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				modified = true;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				modified = true;
				}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				modified = true;
				}
		};
		ownerFrame = ui.getMainFrame();
		dLog = new JDialog(ownerFrame, "Edit artifact information", true);
		final JPanel fieldsPane = new JPanel();
		fieldsPane.setLayout(new BoxLayout(fieldsPane, BoxLayout.Y_AXIS));
		
		title = new JTextField(params.getTitle());
		fieldsPane.add(title);
		title.setBorder(new TitledBorder("Title"));
		title.getDocument().addDocumentListener(listener);
		
		recordName = new JTextField(params.getRecordName());
		fieldsPane.add(recordName);
		title.setBorder(new TitledBorder("Record name"));
		recordName.setEditable(false);

		parentName = new JTextField(params.getRecordParentName());
		title.setBorder(new TitledBorder("Parent name"));
		fieldsPane.add(parentName);
		parentName.setEditable(false);

		date = new JTextField(params.getDate());
		date.setBorder(new TitledBorder("Date"));
		date.setEditable(false);
		fieldsPane.add(date);
		
		doi = new JTextField(params.getDoi());
		fieldsPane.add(doi);
		doi.setBorder(new TitledBorder("Digital Object Identifier"));
		doi.getDocument().addDocumentListener(listener);

		sourcePath = new JTextField(params.getSourceString());
		fieldsPane.add(sourcePath);
		sourcePath.setBorder(new TitledBorder("Source path"));
		sourcePath.setEditable(false);
		
		comments= new JTextArea(params.getComments());
		fieldsPane.add(comments);
		comments.setBorder(new TitledBorder("Comments"));
		comments.getDocument().addDocumentListener(listener);

		keywords= new JTextArea(params.getKeywords());
		keywords.setBorder(new TitledBorder("Keywords"));
		keywords.getDocument().addDocumentListener(listener);
		fieldsPane.add(keywords);
		
		JPanel buttonBar = new JPanel(new FlowLayout());
		JButton closeButton = new JButton("Close");
		ActionListener closeListener = e -> {
			if (modified && Dialogue.yesNoDialog(ownerFrame, "Discard changes?") == Dialogue.YES_OPTION) return;
			this.dialogueDispose();};
		closeButton.addActionListener(closeListener
		);
		buttonBar.add(closeButton);
		JButton saveButton = new JButton("Save");
		closeButton.addActionListener(e -> this.dialogueDispose());
		buttonBar.add(closeButton);
		ActionListener saveListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!modified) return;
				result = params;
				result.setTitle(title.getText());
				result.setRecordName(recordName.getText());
				result.setRecordParentName(parentName.getText());
				result.setDoi(doi.getText());
				result.setComments(comments.getText());
				result.setKeywords(keywords.getText());
				dialogueDispose();
			}
		};
	}
	
	private void dialogueDispose() {
		dLog.setVisible(false);
		dLog.dispose();
	}

}
