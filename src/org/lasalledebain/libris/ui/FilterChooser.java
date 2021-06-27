package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_ENUM;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_PAIR;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_TEXT;
import static java.util.Objects.isNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.field.FieldEnumValue;
import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.search.EnumFilter;
import org.lasalledebain.libris.search.RecordFilter;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.search.RecordFilter.SEARCH_TYPE;
import org.lasalledebain.libris.search.TextFilter;
import org.lasalledebain.libris.util.StringUtils;

@SuppressWarnings("serial")
public class FilterChooser<RecType extends Record> extends JFrame {
	public class BooleanFilterControlPanel extends FilterControlPanel {
JRadioButton fieldTrue, fieldFalse;
		@Override
		void addTitle() {
			addTitle("Boolean field search");
		}

		@Override
		void addControls() {
			EnumSet<FieldType> searchFieldTypes = EnumSet.of(T_FIELD_ENUM);

			Schema dbSchema = myDatabase.getSchema();
			FieldChooser fChooser = new FieldChooser(dbSchema, searchFieldTypes, false, "Search fields", null);
			add(fChooser);
			ButtonGroup buttonState = new ButtonGroup();
			
		}

		@Override
		public RecordFilter<RecType> getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		SEARCH_TYPE getSearchType() {
			return SEARCH_TYPE.T_SEARCH_BOOLEAN;
		}
	}



	static final EnumSet<FieldType> keywordSearchFieldTypes = EnumSet.of(T_FIELD_STRING, T_FIELD_TEXT, T_FIELD_PAIR);
	protected List<FilterControlPanel> filterList;
	final GenericDatabase<RecType> myDatabase;
	final private JButton doneButton, cancelButton;
	private final JPanel dialoguePanel, filterPanel;
	final Schema mySchema;
	public FilterChooser(GenericDatabase<RecType> theDatabase) {
		myDatabase = theDatabase;
		mySchema = theDatabase.getSchema();
		dialoguePanel = new JPanel(new BorderLayout());
		filterPanel = new JPanel();
		filterList = new ArrayList<>();
		doneButton = new JButton("Done");
		cancelButton = new JButton("Cancel");
		initialize();
	}

	private void initialize() {

		filterPanel.setLayout(new BoxLayout(filterPanel,BoxLayout.Y_AXIS));
		filterPanel.setOpaque(true);
		addStage(new DefaultFilterControlPanel());

		dialoguePanel.add(filterPanel, BorderLayout.CENTER);

		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonBar.setOpaque(true);
		buttonBar.add(doneButton);
		doneButton.addActionListener(e -> doClose(true));
		buttonBar.add(cancelButton);
		cancelButton.addActionListener(e -> doClose(false));
		dialoguePanel.add(buttonBar, BorderLayout.SOUTH);

		setContentPane(dialoguePanel);
		pack();
		setVisible(true);
	}

	void addStage() {
		addStage(new DefaultFilterControlPanel());
	}

	private void addStage(SEARCH_TYPE theType) {
		FilterControlPanel theStage;
		if (isNull(theType)) {
			theStage = new DefaultFilterControlPanel();
		} else switch (theType) {
		case T_SEARCH_KEYWORD: theStage = new KeywordFilterControlPanel();
		case T_SEARCH_ENUM: theStage = new EnumerationFilterControlPanel();
		default: theStage = new DefaultFilterControlPanel();
		}
		addStage(theStage);
	}

	private void addStage(FilterControlPanel stage) {
		filterPanel.add(stage);
		filterList.add(stage);
		pack();
	}

	private void removeStage(FilterControlPanel victim) {
		if (filterList.size() > 1) {
			filterList.remove(victim);
			filterPanel.remove(victim);
		}
		disableFirstRemove();
		pack();
	}

	private void replaceStage(FilterControlPanel victim, SEARCH_TYPE newType) {
		FilterControlPanel theStage;
		if (isNull(newType)) {
			theStage = new DefaultFilterControlPanel();
		} else switch (newType) {
		case T_SEARCH_KEYWORD: theStage = new KeywordFilterControlPanel(); break;
		case T_SEARCH_ENUM: theStage = new EnumerationFilterControlPanel(); break;
		default: theStage = new DefaultFilterControlPanel();
		}
		int pos = filterList.indexOf(victim);
		filterList.set(pos, theStage);
		filterPanel.remove(pos);
		filterPanel.add(theStage, pos);
		pack();
	}

	private void disableFirstRemove() {
		if (filterList.size() > 0) {
			filterList.get(0).enableRemove(false);
			if (filterList.size() > 1) {
				filterList.get(1).enableRemove(true);
			}
		}

	}

	private boolean doClose(boolean update) {
		setVisible(false);
		filterList.clear();
		return true;
	}

	protected abstract class FilterControlPanel extends JPanel {
		private final JButton addButton, removeButton;
		private final JComboBox<SEARCH_TYPE> searchTypeChooser;
		public JComboBox<SEARCH_TYPE> getSearchTypeChooser() {
			return searchTypeChooser;
		}
		abstract void addTitle();
		abstract void addControls();
		abstract SEARCH_TYPE getSearchType();
		public FilterControlPanel() {
			setLayout(new FlowLayout());
			addControls();

			searchTypeChooser = new JComboBox<>(SEARCH_TYPE.values());
			searchTypeChooser.setEditable(false);
			JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			add(sep);
			Dimension d = sep.getPreferredSize();
			d.height = searchTypeChooser.getPreferredSize().height;
			sep.setPreferredSize(d);
			add(searchTypeChooser);
			addButton = new JButton("+");
			removeButton = new JButton("-");
			addButton.addActionListener(e -> {
				addStage();
			});
			removeButton.addActionListener(e -> removeStage(this));
			add(addButton);
			add(removeButton);
			addTitle();
			searchTypeChooser.setSelectedItem(getSearchType());
			searchTypeChooser.addActionListener(e -> replaceStage(this, searchTypeChooser.getItemAt(searchTypeChooser.getSelectedIndex())));
		}

		 void enableRemove(boolean enabled) {
			removeButton.setEnabled(enabled);
		}

		public abstract RecordFilter<RecType> getFilter();
		protected void addTitle(String title) {
			Border myBorder = BorderFactory.createTitledBorder(title);
			setBorder(myBorder);
		}
	}

	class KeywordFilterControlPanel extends FilterControlPanel {
		private JTextField keywords;
		private boolean caseSensitive;
		JRadioButton prefixButton;
		private MATCH_TYPE myMatchType;
		JRadioButton wholeWordButton;
		JRadioButton containsButton;
		FieldChooser fldChooser;
		JCheckBox caseSensitiveCheckBox;

		KeywordFilterControlPanel(TextFilter<RecType> theFilter) {
			setCaseSensitive(theFilter.isCaseSensitive());
		}

		KeywordFilterControlPanel()
		{
			return;
		}
		public MATCH_TYPE getMatchType() {
			return myMatchType;
		}

		public void setMatchType(MATCH_TYPE myMatchType) {
			this.myMatchType = myMatchType;
		}
		@Override
		void addTitle() {
			String title = "Keyword search";
			addTitle(title);
		}

		@Override
		void addControls() {
			add(new JLabel("Keywords"));
			keywords = new JTextField(30);
			add(keywords);
			fldChooser = new FieldChooser(mySchema, keywordSearchFieldTypes, "Search fields");
			add(fldChooser);
			caseSensitiveCheckBox = new JCheckBox();
			add(new JLabel("Case sensitive"));
			caseSensitiveCheckBox.addItemListener(e -> caseSensitive = (e.getStateChange() == ItemEvent.SELECTED));
			add(caseSensitiveCheckBox);
			ButtonGroup matchGroup = new ButtonGroup();
			prefixButton = new JRadioButton("Prefix");
			prefixButton.addActionListener(e -> setMatchType(MATCH_TYPE.MATCH_PREFIX));
			prefixButton.setSelected(true);
			myMatchType = MATCH_TYPE.MATCH_PREFIX;

			wholeWordButton = new JRadioButton("Whole word");
			wholeWordButton.addActionListener(e -> setMatchType(MATCH_TYPE.MATCH_EXACT));
			containsButton = new JRadioButton("Contains");
			containsButton.addActionListener(e -> setMatchType(MATCH_TYPE.MATCH_CONTAINS));
			matchGroup.add(prefixButton);
			matchGroup.add(containsButton);
			matchGroup.add(wholeWordButton);
			JPanel buttonBar = new JPanel();
			buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.Y_AXIS));
			buttonBar.add(prefixButton);
			buttonBar.add(containsButton);
			buttonBar.add(wholeWordButton);
			add(buttonBar);
		}

		int[] getSearchFields() {
			return fldChooser.getFieldNums();
		}

		void setSearchFields(int[] fieldNums) {
			fldChooser.setSelectedFields(fieldNums);
		}
		String[] getKeywords() {
			String keywordString = keywords.getText();
			return StringUtils.splitStringByWhitespace(keywordString);
		}

		void setKeywords(String[] keywordList) {
			keywords.setText(StringUtils.joinWordsWithSpaces(keywordList));
		}

		boolean isCaseSensitive() {
			return caseSensitive;
		}

		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			caseSensitiveCheckBox.setSelected(caseSensitive);
		}

		@Override
		public RecordFilter<RecType> getFilter() {
			TextFilter<RecType> theFilter = new TextFilter<>(getMatchType(), isCaseSensitive(), true, getSearchFields(), getKeywords());
			return theFilter;
		}

		@Override
		SEARCH_TYPE getSearchType() {
			return SEARCH_TYPE.T_SEARCH_KEYWORD;
		}

	}

	class EnumerationFilterControlPanel extends FilterControlPanel {
		private int searchField;
		JComboBox<FieldEnumValue> valueList;
		JCheckBox includeDefault;
		private EnumFieldChoices valueChoices;
		FieldChooser fChooser;

		@Override
		void addTitle() {
			addTitle("Multiple choice search");
		}

		@Override
		void addControls() {

			EnumSet<FieldType> searchFieldTypes = EnumSet.of(T_FIELD_ENUM);
			valueList = new JComboBox<>();

			fChooser = new FieldChooser(mySchema, searchFieldTypes, false, "Search fields", null);
			add(fChooser);
			fChooser.addListSelectionListener​(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					setChoices(fChooser);
				}
			});
			fChooser.setSelectedIndex(0);
			setChoices(fChooser);
			add(valueList);
			includeDefault = new JCheckBox("Include default/inherited value");
			includeDefault.setSelected(true);
			add(includeDefault);
		}

		FieldEnumValue getValueChoice() throws FieldDataException {
			return valueChoices.getChoice(valueList.getSelectedIndex());
		}
		
		void setValueChoice(String choiceId) throws FieldDataException {
			valueList.setSelectedIndex(valueChoices.indexFromId(choiceId));
		}
		
		void setValueChoice(int index) {
			valueList.setSelectedIndex(index);
		}
		
		@Override
		public RecordFilter<RecType> getFilter() {
			EnumFilter<RecType> theFilter = 
					new EnumFilter<>(searchField, valueList.getItemAt(valueList.getSelectedIndex()), includeDefault.isSelected());
			return theFilter;
		}

		@Override
		SEARCH_TYPE getSearchType() {
			return SEARCH_TYPE.T_SEARCH_ENUM;
		}

		protected void setChoices(FieldChooser fChooser) {
			searchField = fChooser.getFieldNum();
			valueList.removeAllItems();
			if (searchField >= 0 ) {
				FieldTemplate ft = mySchema.getFieldTemplate(searchField);
				valueChoices = ft.getEnumChoices();
				List<FieldEnumValue> lv = valueChoices.getLegalEnumValues();
				valueList.setModel(new DefaultComboBoxModel<FieldEnumValue>(lv.toArray(new FieldEnumValue[lv.size()])));
			}
		}
	}
	
	

	protected class DefaultFilterControlPanel extends KeywordFilterControlPanel {

	}
}
