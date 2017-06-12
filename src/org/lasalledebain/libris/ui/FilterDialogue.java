package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.search.KeywordFilter;
import org.lasalledebain.libris.search.RecordFilter;
import org.lasalledebain.libris.search.RecordNameFilter;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.search.RecordFilter.SEARCH_TYPE;


import static org.lasalledebain.libris.Field.FieldType.*;
import static org.lasalledebain.libris.search.RecordFilter.SEARCH_TYPE.*;

class FilterDialogue {
	/* TODO filter dialogue
	 * 1. Regenerate dialogue from current filter
	 * 2. Search in record name
	 */
	JRadioButton prefixButton;
	private ButtonGroup matchGroup;
	private JRadioButton containsButton;
	private JRadioButton exactButton;
	private Schema dbSchema;
	private JDialog dLog;
	private JCheckBox caseSensitiveButton;
	private FieldChooser fChooser;
	private BrowserWindow browserWindow;
	private JTextField filterWords;
	MATCH_TYPE matchType;
	private JPanel actionPanel;
	private final JComboBox searchTypeSelector;
	private final int KEYWORD_ORDINAL = 0;
	private final int RECNAME_ORDINAL = 1;
	private Frame ownerFrame;
	private int searchType;
	private LibrisDatabase database;
	public FilterDialogue(LibrisDatabase db, Frame ownerFrame, BrowserWindow browser) {
		database = db;
		this.dbSchema = db.getSchema();
		this.ownerFrame = ownerFrame;
		browserWindow = browser;
		dLog = new JDialog(ownerFrame, "Filter");	
		searchTypeSelector = new JComboBox(new String[] {"Keyword", "Record name"});
		searchTypeSelector.add(new JLabel("Search type"));
		searchTypeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createSearchDialogue(searchTypeSelector.getSelectedIndex());
			}
		});	
		createSearchDialogue(KEYWORD_ORDINAL);
	}

	private void createSearchDialogue(int searchType) {
		this.searchType = searchType;
		JPanel dialogueContent = new JPanel();
		dialogueContent.setLayout(new BoxLayout(dialogueContent, BoxLayout.Y_AXIS));
		JPanel searchPanel;
		switch (searchType) {
		case KEYWORD_ORDINAL: searchPanel = createKeywordSearchDialogue(); break;
		case RECNAME_ORDINAL: searchPanel = createRecordNameSearchDialogue(); break;
		default: return;
		}

		dialogueContent.add(searchTypeSelector);
		dialogueContent.add(searchPanel);
		actionPanel = createActionPanel();
		dialogueContent.add(actionPanel);
		dLog.setContentPane(dialogueContent);
		dLog.pack();
		dLog.setLocationRelativeTo(ownerFrame);
		dLog.setVisible(true);
	}

	private JPanel createRecordNameSearchDialogue() {
		JPanel controlPanel = createTextSearchControls();
		
		createSearchTermsField();
		
		actionPanel = createActionPanel();
		
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(controlPanel, BorderLayout.NORTH);
		searchPanel.add(filterWords, BorderLayout.CENTER);
		return searchPanel;
	}

	private JPanel createKeywordSearchDialogue() {
		JPanel controlPanel = createTextSearchControls();
		
		createSearchTermsField();
				
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(controlPanel, BorderLayout.NORTH);
		searchPanel.add(filterWords, BorderLayout.CENTER);
		searchPanel.add(fChooser, BorderLayout.EAST);
		return searchPanel;
	}
	
	private void createSearchTermsField() {
		filterWords = new JTextField();
	}
	private JPanel createTextSearchControls() {
		matchGroup = new ButtonGroup();
		prefixButton = new JRadioButton("Starts with");
		prefixButton.addActionListener(new ButtonListener(MATCH_TYPE.MATCH_PREFIX));
		prefixButton.setSelected(true);
		matchType = MATCH_TYPE.MATCH_PREFIX;
		
		exactButton = new JRadioButton("Exact match");
		exactButton.addActionListener(new ButtonListener(MATCH_TYPE.MATCH_EXACT));
		containsButton = new JRadioButton("Contains");
		containsButton.addActionListener(new ButtonListener(MATCH_TYPE.MATCH_CONTAINS));
		matchGroup.add(prefixButton);
		matchGroup.add(containsButton);
		matchGroup.add(exactButton);
		
		caseSensitiveButton = new JCheckBox("Case sensitive");
		EnumSet<FieldType> searchFieldTypes = EnumSet.of(T_FIELD_STRING, T_FIELD_TEXT, T_FIELD_PAIR);
		fChooser = new FieldChooser(dbSchema, searchFieldTypes);
		
		JPanel controlPanel = new JPanel();
		controlPanel.add(prefixButton);
		controlPanel.add(containsButton);
		controlPanel.add(exactButton);
		controlPanel.add(caseSensitiveButton);
		return controlPanel;
	}
	private JPanel createActionPanel() {
		JButton okayButton = new JButton("Okay");
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dLog.setVisible(false);
				dLog.dispose();
				doRefresh();
			}

		});
		dLog.getRootPane().setDefaultButton(okayButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dLog.setVisible(false);
				dLog.dispose();
			};		
		});
		
		JPanel actionPanel = new JPanel();
		actionPanel.add(okayButton);
		actionPanel.add(cancelButton);
		return actionPanel;
	}
	private void doRefresh() {
		switch (searchType) {
		case KEYWORD_ORDINAL: doKeywordRefresh(); break;
		case RECNAME_ORDINAL: doRecordNameRefresh(); break;
		default: return;
		}
	}

	private void doRecordNameRefresh() {
		final boolean caseSensitive = caseSensitiveButton.isSelected();
		String searchTerms = filterWords.getText();
		if (!caseSensitive) {
			searchTerms = searchTerms.toLowerCase();
		}
		String[] searchList = searchTerms.trim().split("\\s+");
		RecordNameFilter filter = new RecordNameFilter(matchType, caseSensitive, searchList);
		browserWindow.doRefresh(database.getNamedRecords(), filter);
	}

	private void doKeywordRefresh() {
		int[] selectedFields = fChooser.getFieldNums();
		if (0 != selectedFields.length) {
			final boolean caseSensitive = caseSensitiveButton.isSelected();
			String searchTerms = filterWords.getText();
			if (!caseSensitive) {
				searchTerms = searchTerms.toLowerCase();
			}
			String[] searchList = searchTerms.trim().split("\\s+");
			KeywordFilter filter = new KeywordFilter(matchType, caseSensitive, selectedFields, searchList);
			browserWindow.doRefresh(database.getRecords(), filter);
		}
	}
	public class ButtonListener implements ActionListener {
		protected ButtonListener(MATCH_TYPE mtype) {
			super();
			this.mtype = mtype;
		}
		final MATCH_TYPE mtype;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			matchType = mtype;
		}
	
	}
	
}
