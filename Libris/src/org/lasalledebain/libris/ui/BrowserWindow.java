package org.lasalledebain.libris.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.SingleRecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class BrowserWindow extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3109074830658001908L;

	static final int LIST_LIMIT = 1024;
	private static final int VISIBLE_LIMIT = 64;

	private LibrisDatabase database;
	private JList chooser;
	private LibrisGui gui;
	private JButton moreButton;
	private JButton refreshButton;
	private RandomAccessBrowserList recordList;
	private Iterator<Record> recordsIterator;

	private RecordList recordsSource;

	public BrowserWindow(LibrisDatabase database, LibrisGui ui) throws DatabaseException {
		super();
		this.database = database;
		this.gui = ui;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(true);
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
				recordList = getRecords(recordsIterator); // user records as ListModel
				chooser.setModel(recordList);
			}			
		});
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recordsIterator = recordsSource.iterator();
				recordList = getRecords(recordsIterator);
				chooser.setModel(recordList);
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
		this.layout = layouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USER_SUMMARYDISPLAY);
		fieldIds = layout.getFieldIds();
		recordsIterator = recordsSource.iterator();
		recordList = getRecords(recordsIterator);
		chooser.setModel(recordList);
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

	public RecordId getSelectedRecordId() {
		BrowserRow chosenRecord = (BrowserRow) chooser.getSelectedValue();
		if (null != chosenRecord) {		
			return chosenRecord.getRecordId();
		} else {
			return null;
		}
	}

	public int getNumRecords() {
		return recordList.getSize();
	}

	public void addRecord(Record rec) throws DatabaseException {
		if (null == recordList) {
			initialize(new SingleRecordList(database, rec));
		} else {
			int index =  recordList.add(rec);
			chooser.ensureIndexIsVisible(index);
		}
	}
	
	private RandomAccessBrowserList getRecords(Iterator<Record> iter) {
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
		RecordId recId = chosenRecord.getRecordId();
		try {
			gui.displayRecord(recId);
		} catch (LibrisException e) {
			gui.alert("error displaying record "+recId.toString(), e);
		}
	}

	Layout layout;
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
