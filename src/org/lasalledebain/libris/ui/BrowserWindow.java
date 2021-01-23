package org.lasalledebain.libris.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.SingleRecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.search.RecordFilter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class BrowserWindow extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3109074830658001908L;

	static final int LIST_LIMIT = 1024;
	private static final int VISIBLE_LIMIT = 64;

	private LibrisDatabase database;
	private JList<RecordInfo<Record>> chooser;
	private final LibrisGui gui;
	private JPanel filterView;
	private JButton moreButton;
	private JButton refreshButton;
	private RandomAccessBrowserList<Record> resultRecords;
	private Iterator<DatabaseRecord> recordsIterator;
	private ImageIcon searchIcon;

	private RecordList<DatabaseRecord> recordsSource;

	private RecordFilter filter;

	public BrowserWindow(LibrisDatabase db, LibrisGui ui) throws DatabaseException {
		super();
		this.database = db;
		this.gui = ui;

		if (null == searchIcon) {
			ClassLoader l = this.getClass().getClassLoader();
			URL iconUrl = l.getResource("magnifier12.png");
			searchIcon = new ImageIcon(iconUrl);
		}
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
		filterView = new JPanel();
		JLabel icon = new JLabel(searchIcon);
		filterView.add(icon);
		filterView.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				gui.createSearchDialogue();
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		add("Filter", filterView);
		chooser = new JList<>();
		chooser.addMouseListener(chooserMouseListener);
		chooser.setVisibleRowCount(VISIBLE_LIMIT);
		scroller = new JScrollPane(chooser);
		add(scroller);
		scroller.setVisible(true);

		refreshButton = new JButton("Refresh");
		moreButton = new JButton("More");
		moreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRefresh();
			}

		});
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recordsIterator = recordsSource.iterator();
				setResultList();
				chooser.setModel(resultRecords);
			}			
		});
		JPanel buttonBar = new JPanel(new FlowLayout());
		buttonBar.add(refreshButton);
		buttonBar.add(moreButton);
		add(buttonBar);
		refreshButton.setVisible(true);
		moreButton.setVisible(true);
	}

	public void initialize(RecordList<DatabaseRecord> records) throws DatabaseException {
		recordsSource = records;
		enableNext(false);
		final Layouts layouts = database.getLayouts();
		myLayout = layouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
		fieldIds = myLayout.getFieldIds();
		recordsIterator = recordsSource.iterator();
		setResultList();
		chooser.setModel(resultRecords);
	}

	/**
	 * set the selection to the specified index
	 * @param index
	 */
	public void setSelectedRecordIndex(int index){
		chooser.setSelectedIndex(index);
	}

	public int getSelectedRecordIndex() {
		return chooser.getSelectedIndex();
	}

	public int getSelectedRecordId() {
		RecordInfo<Record> chosenRecord = chooser.getSelectedValue();
		if (null != chosenRecord) {		
			return chosenRecord.getRecordId();
		} else {
			return RecordId.NULL_RECORD_ID;
		}
	}

	public int getNumRecords() {
		return resultRecords.getSize();
	}

	void doRefresh() {
		setResultList(); // user records as ListModel
		chooser.setModel(resultRecords);
	}			

	public void doRefresh(RecordList<DatabaseRecord> src, RecordFilter filter) {
		setFilter(filter);
		FilteredRecordList<DatabaseRecord> filteredList = new FilteredRecordList<DatabaseRecord>(src, filter);
		recordsIterator = filteredList.iterator();
		doRefresh();
	}

	public void doRefresh(Iterable<DatabaseRecord> src) {
		recordsIterator = src.iterator();
		doRefresh();
	}

	public void setFilter(RecordFilter filter) {
		this.filter = filter;
	}

	public RecordFilter getFilter() {
		return filter;
	}

	public void addRecord(DatabaseRecord rec) throws DatabaseException {
		removeRecord(rec);
		if (null == resultRecords) {
			initialize(new SingleRecordList<DatabaseRecord>(rec));
		} else {
			int index =  resultRecords.add(rec);
			chooser.ensureIndexIsVisible(index);
		}
	}

	public void removeRecord(Record rec) {
		if (null != resultRecords) {
			int index =  resultRecords.remove(rec);
			chooser.ensureIndexIsVisible(index);
		}
	}

	public void setResultList() {
		RandomAccessBrowserList<Record> list = new RandomAccessBrowserList<>(fieldIds);
		int recordCount = 0;
		while (recordsIterator.hasNext() && (LIST_LIMIT > recordCount)) {
			DatabaseRecord r = recordsIterator.next();
			list.add(r);
			++recordCount;
			if (recordCount > LIST_LIMIT) {
				break;
			}
		}
		if (LIST_LIMIT == recordCount) {
			enableNext(true);
		} else {
			enableNext(false);
		}
		resultRecords = list;
	}

	private void enableNext(boolean b) {
		moreButton.setEnabled(b);
	}

	public void displaySelectedRecord() {
		RecordInfo<Record> chosenRecord = chooser.getSelectedValue();
		int recId = chosenRecord.getRecordId();
		try {
			gui.displayRecord(recId);
		} catch (LibrisException e) {
			gui.alert("error displaying record "+recId, e);
		}
	}

	LibrisLayout myLayout;
	private String[] fieldIds;

	MouseListener chooserMouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				displaySelectedRecord();
			}
			gui.setViewViewSelectedRecordEnabled(!chooser.isSelectionEmpty());
			gui.setRecordDuplicateRecordEnabled(!chooser.isSelectionEmpty() 
					&& (chooser.getMinSelectionIndex() == chooser.getMaxSelectionIndex()));
		}
	};

	private JScrollPane scroller;

	public void clearSelection() {
		chooser.clearSelection();	
	}

	public RecordList<Record> getResultRecords() {
		return resultRecords;
	}

}
