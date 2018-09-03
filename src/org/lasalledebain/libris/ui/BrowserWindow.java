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
	private JList<?> chooser;
	private final LibrisGui gui;
	private JPanel filterView;
	private JButton moreButton;
	private JButton refreshButton;
	private RandomAccessBrowserList recList;
	private Iterator<Record> recordsIterator;
	private ImageIcon searchIcon;

	private RecordList recordsSource;

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
		chooser = new JList();
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
				recList = getRecords();
				chooser.setModel(recList);
			}			
		});
		JPanel buttonBar = new JPanel(new FlowLayout());
		buttonBar.add(refreshButton);
		buttonBar.add(moreButton);
		add(buttonBar);
		refreshButton.setVisible(true);
		moreButton.setVisible(true);
	}

	public void initialize(RecordList records) throws DatabaseException {
		recordsSource = records;
		enableNext(false);
		final Layouts layouts = database.getLayouts();
		this.myLayout = layouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_SUMMARYDISPLAY);
		fieldIds = myLayout.getFieldIds();
		recordsIterator = recordsSource.iterator();
		recList = getRecords();
		chooser.setModel(recList);
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
		BrowserRow chosenRecord = (BrowserRow) chooser.getSelectedValue();
		if (null != chosenRecord) {		
			return chosenRecord.getRecordId();
		} else {
			return RecordId.getNullId();
		}
	}

	public int getNumRecords() {
		return recList.getSize();
	}

	void doRefresh() {
		recList = getRecords(); // user records as ListModel
		chooser.setModel(recList);
	}			

	public void doRefresh(RecordList src, RecordFilter filter) {
		setFilter(filter);
		FilteredRecordList filteredList = new FilteredRecordList(src, filter);
		recordsIterator = filteredList.iterator();
		recList = getRecords();
		chooser.setModel(recList);
	}

	public void doRefresh(RecordList src) {
		recordsIterator = src.iterator();
		recList = getRecords();
		chooser.setModel(recList);
	}

	public void doRefresh(Iterable<Record> src) {
		recordsIterator = src.iterator();
		recList = getRecords();
		chooser.setModel(recList);
}

	public void setFilter(RecordFilter filter) {
		this.filter = filter;
	}

	public RecordFilter getFilter() {
		return filter;
	}

	public void addRecord(Record rec) throws DatabaseException {
		removeRecord(rec);
		if (null == recList) {
			initialize(new SingleRecordList(rec));
		} else {
			int index =  recList.add(rec);
			chooser.ensureIndexIsVisible(index);
		}
	}

	public void removeRecord(Record rec) {
		if (null != recList) {
			int index =  recList.remove(rec);
			chooser.ensureIndexIsVisible(index);
		}
	}

	private RandomAccessBrowserList getRecords() {
		RandomAccessBrowserList list = new RandomAccessBrowserList(fieldIds);
		int recordCount = 0;
		while (recordsIterator.hasNext() && (LIST_LIMIT > recordCount)) {
			Record r = recordsIterator.next();
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
		return list;
	}

	private void enableNext(boolean b) {
		moreButton.setEnabled(b);
	}

	public void displaySelectedRecord() {
		BrowserRow chosenRecord = (BrowserRow) chooser.getSelectedValue();
		int recId = chosenRecord.getRecordId();
		try {
			gui.displayRecord(recId);
		} catch (LibrisException e) {
			gui.alert("error displaying record "+recId, e);
		}
	}

	Layout myLayout;
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

}
