package Libris;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RecordList {
	LibrisDatabase database;
	LibrisSchema schema;
	private final int ARRAYSIZE = 100;
	private ArrayList<String> recordList; // list of record numbers
	private JFrame recFrame;
	private JScrollPane recPane;
	private JList recPaneList;
	private LibrisRecord displayedRecord;
	protected DatabaseFileRecords reader;
	private RecordWindow rWindow = null;

	public RecordList(LibrisDatabase database) {
		this.database = database;
		schema = database.getSchema();
		result = new ArrayList<LibrisRecord>();
		recordList = new ArrayList<String>(ARRAYSIZE);
		reader = new DatabaseFileRecords(database.getCurrentDBFile(), database, this);
	}
	public void display() {
		recFrame = new JFrame();
		recFrame.setJMenuBar(LibrisMain.getMenubar());
		recFrame.setSize(300,200);
		recPaneList = new JList(recordList.toArray());
		recPane = new JScrollPane(recPaneList);
		recFrame.getContentPane().add(recPane, BorderLayout.CENTER);
		recFrame.setVisible(true);
		rlMouseListener mouseListener = new rlMouseListener();
		recPaneList.addMouseListener(mouseListener);
		recPaneList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent arg0) {
				if (rWindow != null) {
					int index = arg0.getFirstIndex();
					displayedRecord = result.get(index);
					rWindow = displayedRecord.display(rWindow, false);
					recFrame.toFront();
				}				
			}
		});
	}

	private class rlMouseListener extends MouseInputAdapter {

		private int verbosity = 1;
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int index = recPaneList.locationToIndex(e.getPoint());
				logMsg("Double clicked on Item " + index);
				displayedRecord = result.get(index);
				rWindow = displayedRecord.display();
			} else 	if ((e.getClickCount() == 1) && (rWindow != null)) {
				int index = recPaneList.locationToIndex(e.getPoint());
				displayedRecord = result.get(index);
				rWindow = displayedRecord.display(rWindow, false);
				recFrame.toFront();
			}
		}
		private void logMsg(String msg) {
			LibrisMain.logMsg(verbosity , msg);
		}		
		
	}
	
	private ArrayList<LibrisRecord> result;

	public void addrecord(LibrisRecord record) {
		result.add(record);
		String summary = record.getSummary();
		recordList.add(summary);

	}
	public ArrayList getList() {
		return(result);
	}

	/* read all records */
	public void readRecords() {
		reader.readDatabase();
		display();
	}

	public LibrisRecord readRecord(int recNum) throws LibrisException {
		LibrisRecord r = reader.readRecord(recNum);
		return r;
	}
	
	public void runQuery(ArrayList<String[]> q) throws LibrisException {
		try {
			database.keywordFilter.clearQuery();
			{
				Iterator<String[]> i = q.iterator();
				while (i.hasNext()) {
					String[] k = i.next();
					database.keywordFilter.addQueryKeywords(k);
				}
			}
			{
				ArrayList<Integer> l = 
					database.keywordFilter.runQuery(0, database.fileIndex.getRecCount()-1);
				Iterator<Integer> i = l.iterator();
				while (i.hasNext()) {
					readRecord(i.next());
				}
			}
		} catch (LibrisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
