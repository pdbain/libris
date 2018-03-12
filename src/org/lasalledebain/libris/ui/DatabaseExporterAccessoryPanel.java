package org.lasalledebain.libris.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.SecondaryLoop;
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

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.ui.DatabaseExporter.ExportFormat;
import org.lasalledebain.libris.util.StringUtils;

public class DatabaseExporterAccessoryPanel extends JPanel {
	private ButtonGroup formatButton;
	private JRadioButton librButton;
	private JRadioButton csvButton;
	private JRadioButton textButton;
	private String lastExportFormat;
	private JCheckBox includeSchemaControl;
	private JCheckBox includeRecordsControl;
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
		formatButton.add(librButton);
		formatButton.add(csvButton);
		formatButton.add(textButton);

		GridBagLayout buttonLayout = new GridBagLayout();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel sepLabel = new JLabel("Select output format");
		
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
		buttonConstraints.anchor = GridBagConstraints.WEST;
		
		buttonLayout.setConstraints(sepLabel, buttonConstraints);
		add(sepLabel);

		buttonLayout.setConstraints(librButton, buttonConstraints);
		librButton.addActionListener(new FileExtensionModifier("libr"));
		add(librButton);

		buttonLayout.setConstraints(csvButton, buttonConstraints);
		csvButton.addActionListener(new FileExtensionModifier("csv"));
		add(csvButton);

		buttonLayout.setConstraints(textButton, buttonConstraints);
		textButton.addActionListener(new FileExtensionModifier("txt"));
		add(textButton);

		if (lastExportFormat.equals(ExportFormat.EXPORT_XML.toString())) {
			librButton.setSelected(true);
		} else if 	(lastExportFormat.equals(ExportFormat.EXPORT_CSV.toString())) {
			csvButton.setSelected(false);
		} else if 	(lastExportFormat.equals(ExportFormat.EXPORT_TEXT.toString())) {
			textButton.setSelected(false);
		}

		includeSchemaControl = new JCheckBox("Include schema");
		includeRecordsControl = new JCheckBox("Include records");
		add(includeSchemaControl);
		add(includeRecordsControl);
		includeSchemaControl.setSelected(true);
		includeRecordsControl.setSelected(true);
	}
	
	ExportFormat getFormat() throws InputException {
		ExportFormat fmt;
		if (librButton.isSelected()) {
			fmt = ExportFormat.EXPORT_XML;
		} else if (csvButton.isSelected()) {
			fmt = ExportFormat.EXPORT_CSV;
		} else if (textButton.isSelected()) {
			fmt = ExportFormat.EXPORT_TEXT;
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
	
	class FileExtensionModifier implements ActionListener {
		String newExtension;
		public FileExtensionModifier(String newExtension) {
			this.newExtension = newExtension;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			File oldFile = chooser.getSelectedFile();
			String newName 
			= StringUtils.changeFileExtension(oldFile.getName(), newExtension);
			chooser.setSelectedFile(new File(newName));
		}

	}
}
