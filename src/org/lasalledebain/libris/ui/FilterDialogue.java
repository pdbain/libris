package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_AFFILIATES;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_PAIR;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_TEXT;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_ENUM;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldEnumValue;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.search.TextFilter;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.search.RecordNameFilter;
import org.lasalledebain.libris.ui.AffiliateEditor.RecordSelectorByName;
import org.lasalledebain.libris.ui.FieldChooser.FieldInfo;

class FilterDialogue {
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
	private final JComboBox<String> searchTypeSelector;
	private final int KEYWORD_ORDINAL = 0;
	private final int RECNAME_ORDINAL = 1;
	private final int CHILDREN_ORDINAL = 2;
	private Frame ownerFrame;
	private int searchType;
	private LibrisDatabase database;
	private JComboBox<String> affiliatesChooser;
	private RecordSelectorByName nameBrowser;
	private LastFilterSettings lastSettings;
	public FilterDialogue(LibrisDatabase db, Frame theFrame, BrowserWindow browser) {
		database = db;
		lastSettings = database.getMetadata().getLastFilterSettings();
		dbSchema = db.getSchema();
		ownerFrame = theFrame;
		browserWindow = browser;
		searchTypeSelector = new JComboBox<String>(new String[] {"Keyword", "Record name", "Affiliates"});
		createSearchDialogue();
	}

	public void createSearchDialogue() {
		dLog = new JDialog(ownerFrame, "Filter");	
		searchTypeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectSearchDialogue(searchTypeSelector.getSelectedIndex());
			}
		});	
		searchTypeSelector.setSelectedIndex(lastSettings.lastSearchType);
		selectSearchDialogue(searchTypeSelector.getSelectedIndex());
	}

	private void selectSearchDialogue(int searchType) {
		lastSettings.lastSearchType = searchType;
		this.searchType = searchType;
		JPanel dialogueContent = new JPanel();
		dialogueContent.setLayout(new BoxLayout(dialogueContent, BoxLayout.Y_AXIS));
		JPanel searchPanel;
		switch (searchType) {
		case KEYWORD_ORDINAL: searchPanel = createKeywordSearchDialogue(); break;
		case RECNAME_ORDINAL: searchPanel = createRecordNameSearchDialogue(); break;
		case CHILDREN_ORDINAL: searchPanel = createChildrenSearchDialogue(); break;
		default: return;
		}

		LibrisGui.makeLabelledControl(dialogueContent, searchTypeSelector, "Search type", false);
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
	
	private JPanel createChildrenSearchDialogue() {
		int currentId = browserWindow.getSelectedRecordId();
		JPanel controlPanel = createChildSearchControls();
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(controlPanel, BorderLayout.NORTH);
		try {
			final RecordNameChooser recordFilter = new RecordNameChooser(new KeyIntegerTuple(null, currentId), 
					database.getNamedRecordIndex());
			nameBrowser = new RecordSelectorByName(recordFilter);
			LibrisGui.makeLabelledControl(searchPanel, nameBrowser, "Search root", false);
		} catch (InputException e) {
			throw new DatabaseError("Error finding children for record "+currentId);
		}
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
		fChooser = new FieldChooser(dbSchema, searchFieldTypes, true, "Search fields");
		
		JPanel controlPanel = new JPanel();
		controlPanel.add(prefixButton);
		controlPanel.add(containsButton);
		controlPanel.add(exactButton);
		controlPanel.add(caseSensitiveButton);
		return controlPanel;
	}
	private JPanel createChildSearchControls() {
		affiliatesChooser = new JComboBox<>(new String[] {"Children", "Descendents", "Affiliates"});
		affiliatesChooser.setSelectedIndex(lastSettings.lastAffiliatesExtent);
		EnumSet<FieldType> searchFieldTypes = EnumSet.of(T_FIELD_AFFILIATES);
		fChooser = new FieldChooser(dbSchema, searchFieldTypes, false, "Search fields");
		fChooser.setSelectedIndex(lastSettings.lastGroupNum);
		
		JPanel controlPanel = new JPanel();
		LibrisGui.makeLabelledControl(controlPanel, affiliatesChooser, "Search extent", false);
		controlPanel.add(affiliatesChooser);
		controlPanel.add(fChooser);
		return controlPanel;
	}
	
	private JPanel createEnumSearchDialogue() {
		JPanel controlPanel = new JPanel();
		EnumSet<FieldType> searchFieldTypes = EnumSet.of(T_FIELD_ENUM);
		fChooser = new FieldChooser(dbSchema, searchFieldTypes, false, "Search fields");
		controlPanel.add(fChooser);
		 JComboBox<FieldEnumValue> valueList = new JComboBox<>();
		 fChooser.addListSelectionListener​(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int searchField = fChooser.getFieldNum();
				valueList.removeAllItems();
				if (searchField >= 0 ) {
					FieldTemplate ft = dbSchema.getFieldTemplate(searchField);
					EnumFieldChoices choices = ft.getEnumChoices();
					List<FieldEnumValue> lv = choices.getLegalEnumValues();
					valueList.setModel(new DefaultComboBoxModel(lv.toArray()));
				}
			}
		});
		 controlPanel.add(valueList);
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
		case CHILDREN_ORDINAL: doChildrenRefresh(); break;
		default: return;
		}
	}

	private void doKeywordRefresh() {
		int[] selectedFields = fChooser.getFieldNums();
		lastSettings.lastKeywordFields = Arrays.copyOf(selectedFields, selectedFields.length);
		if (0 != selectedFields.length) {
			final boolean caseSensitive = caseSensitiveButton.isSelected();
			String searchTerms = filterWords.getText();
			if (!caseSensitive) {
				searchTerms = searchTerms.toLowerCase();
			}
			String[] searchList = searchTerms.trim().split("\\s+");
			TextFilter filter = new TextFilter(matchType, caseSensitive, selectedFields, searchList);
			browserWindow.doRefresh(database.getRecords(), filter);
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

	private void doChildrenRefresh() {
		int parent = nameBrowser.getSelectedId();
		int fieldNum = fChooser.getFieldNum();
		lastSettings.lastGroupNum = fieldNum;

		int affiliateType = affiliatesChooser.getSelectedIndex();
		lastSettings.lastAffiliatesExtent = affiliateType;
		Iterable<DatabaseRecord> children;
		if (2 == affiliateType) {
			children = database.getAffiliateRecords(parent, fieldNum);			
		} else {
			boolean isDescendents = (affiliateType == 1);
			children = database.getChildRecords(parent, fieldNum, isDescendents);
		}
		browserWindow.doRefresh(children);
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
