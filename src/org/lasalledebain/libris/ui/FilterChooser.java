package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_PAIR;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_TEXT;

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

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.search.RecordFilter;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.search.RecordFilter.SEARCH_TYPE;
import org.lasalledebain.libris.search.TextFilter;
import org.lasalledebain.libris.util.StringUtils;

@SuppressWarnings("serial")
public class FilterChooser<RecType extends Record> extends JFrame {
	static final EnumSet<FieldType> keywordSearchFieldTypes = EnumSet.of(T_FIELD_STRING, T_FIELD_TEXT, T_FIELD_PAIR);
	protected List<FilterStage> filterList;
	GenericDatabase<RecType> db;
	final private JButton doneButton, cancelButton;
	private final JPanel dialoguePanel, filterPanel;
	public FilterChooser(GenericDatabase<RecType> theDatabase) {
		db = theDatabase;
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
		addStage();

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

	private void addStage() {
		addStage(new DefaultFilterStage());
	}

	private void addStage(FilterStage stage) {
		filterPanel.add(stage);
		filterList.add(stage);
		pack();
	}

	private void removeStage(FilterStage victim) {
		if (filterList.size() > 1) {
			filterList.remove(victim);
			filterPanel.remove(victim);
		}
		disableFirstRemove();
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

	protected abstract class FilterStage extends JPanel {
		private final JButton addButton, removeButton;
		private final JComboBox<SEARCH_TYPE> searchTypeChooser;
		abstract void addTitle();
		abstract void addControls();
		public FilterStage() {
			setLayout(new FlowLayout());
			addControls();

			searchTypeChooser = new JComboBox<>(SEARCH_TYPE.values());
			JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			add(sep);
			Dimension d = sep.getPreferredSize();
			d.height = searchTypeChooser.getPreferredSize().height;
			sep.setPreferredSize(d);
			add(searchTypeChooser);
			addButton = new JButton("+");
			removeButton = new JButton("-");
			addButton.addActionListener(e -> addStage());
			removeButton.addActionListener(e -> removeStage(this));
			add(addButton);
			add(removeButton);
			addTitle();
		}

		void enableRemove(boolean enabled) {
			removeButton.setEnabled(enabled);
		}

		public abstract RecordFilter<RecType> getFilter();
	}

	class KeywordFilterStage extends FilterStage {
		private JTextField keywords;
		private boolean caseSensitive;
		JRadioButton prefixButton;
		private MATCH_TYPE myMatchType;
		JRadioButton wholeWordButton;
		JRadioButton containsButton;
		FieldChooser fldChooser;
		JCheckBox caseSensitiveCheckBox;
		
		KeywordFilterStage(TextFilter<RecType> theFilter) {
			setCaseSensitive(theFilter.isCaseSensitive());
		}
		
		KeywordFilterStage()
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
			Border myBorder = BorderFactory.createTitledBorder("Keyword search");
			setBorder(myBorder);
		}

		@Override
		void addControls() {
			add(new JLabel("Keywords"));
			keywords = new JTextField(30);
			add(keywords);
			fldChooser = new FieldChooser(db.getSchema(), keywordSearchFieldTypes, "Search fields");
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

	}

	protected class DefaultFilterStage extends KeywordFilterStage {

	}
}
