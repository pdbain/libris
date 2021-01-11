package org.lasalledebain.libris.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.ui.DatabaseExporter.ExportFormat;
import org.lasalledebain.libris.util.StringUtils;

@SuppressWarnings("serial")
public class DatabaseExporterAccessoryPanel extends JPanel implements LibrisConstants {
	private ButtonGroup formatButton;
	private JRadioButton librButton;
	private JRadioButton csvButton;
	private JRadioButton textButton;
	private JRadioButton tarButton;
	private String lastExportFormat;
	private JCheckBox includeSchemaControl;
	private JCheckBox includeRecordsControl;
	private JCheckBox includeArtifactsControl;
	private final JFileChooser chooser;
	public DatabaseExporterAccessoryPanel(JFileChooser fileChooser, String lastExportFmt) {
		lastExportFormat = lastExportFmt;
		chooser = fileChooser;
		initialize();
	}

	private void initialize() {
		formatButton = new ButtonGroup();
		librButton = new JRadioButton("LIBR");
		csvButton = new JRadioButton("CSV");
		textButton = new JRadioButton("Formatted Text");
		tarButton = new JRadioButton("TAR file");
		formatButton.add(librButton);
		formatButton.add(csvButton);
		formatButton.add(textButton);
		formatButton.add(tarButton);

		GridBagLayout buttonLayout = new GridBagLayout();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel sepLabel = new JLabel("Select output format");
		
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
		buttonConstraints.anchor = GridBagConstraints.WEST;
		
		buttonLayout.setConstraints(sepLabel, buttonConstraints);
		add(sepLabel);

		buttonLayout.setConstraints(librButton, buttonConstraints);
		librButton.addActionListener(new FileExtensionModifier(FILENAME_LIBRIS_FILES_SUFFIX));
		add(librButton);

		buttonLayout.setConstraints(csvButton, buttonConstraints);
		csvButton.addActionListener(new FileExtensionModifier(FILENAME_CSV_FILES_SUFFIX));
		add(csvButton);

		buttonLayout.setConstraints(textButton, buttonConstraints);
		textButton.addActionListener(new FileExtensionModifier(FILENAME_TEXT_FILES_SUFFIX));
		add(textButton);

		buttonLayout.setConstraints(tarButton, buttonConstraints);
		tarButton.addActionListener(new FileExtensionModifier(FILENAME_ARCHIVE_FILES_SUFFIX));
		add(tarButton);

		if 	(lastExportFormat.equals(ExportFormat.EXPORT_CSV.toString())) {
			csvButton.setSelected(true);
		} else if (lastExportFormat.equals(ExportFormat.EXPORT_TEXT.toString())) {
			textButton.setSelected(true);
		} else if (lastExportFormat.equals(ExportFormat.EXPORT_TAR.toString())) {
			tarButton.setSelected(true);
		} else if (lastExportFormat.equals(ExportFormat.EXPORT_LIBR.toString())) {
			librButton.setSelected(true);
		} 
		

		add(includeSchemaControl = new JCheckBox("Include schema"));
		add(includeRecordsControl = new JCheckBox("Include records"));
		add(includeArtifactsControl = new JCheckBox("Include artifacts"));
		includeSchemaControl.setSelected(true);
		includeRecordsControl.setSelected(true);
		includeArtifactsControl.setSelected(true);
	}
	
	ExportFormat getFormat() throws InputException {
		ExportFormat fmt;
		if (librButton.isSelected()) {
			fmt = ExportFormat.EXPORT_LIBR;
		} else if (csvButton.isSelected()) {
			fmt = ExportFormat.EXPORT_CSV;
		} else if (textButton.isSelected()) {
			fmt = ExportFormat.EXPORT_TEXT;
		} else if (tarButton.isSelected()) {
			fmt = ExportFormat.EXPORT_TAR;
		} else {
			throw new InputException("No format specified");
		}
		return fmt;
	}
	
	boolean isIncludeSchema() {
		return includeSchemaControl.isSelected();
	}
	
	boolean isIncludeRecords() {
		return includeRecordsControl.isSelected();
	}
	
	boolean isIncludeArtifacts() {
		return includeArtifactsControl.isSelected();
	}
	
	class FileExtensionModifier implements ActionListener {
		String newExtension;
		public FileExtensionModifier(String newExtension) {
			this.newExtension = newExtension;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			File oldFile = chooser.getSelectedFile();
			String newName = StringUtils.changeFileExtension(oldFile.getName(), newExtension);
			chooser.setSelectedFile(new File(newName));
			includeArtifactsControl.setEnabled((tarButton.isSelected()));
		}

	}
}
