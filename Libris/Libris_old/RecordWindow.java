/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 * @author pdbain
 *
 */
public class RecordWindow extends JFrame {
	private LibrisMenu menus;
	private LibrisDatabase database;
	private RecordPanel recPanel = null;
	private JPanel topPanel, inputPanel;
	private boolean dataChanged;
	private LibrisRecord record;
	boolean editable;
	private JScrollPane recScroll;
	/**
	 * @param database
	 * @param editable
	 * @param record
	 * @throws HeadlessException
	 */
	public RecordWindow(LibrisRecord record, LibrisDatabase database,
			boolean editable) {
		this.database = database;
		this.editable = editable;
		this.record = record;
		setTitle("Libris");
		topPanel = new JPanel();
		this.getContentPane().add(topPanel);
		topPanel.setLayout(new BorderLayout());
		inputPanel = topPanel;
		this.setJMenuBar(LibrisMain.getMenubar());

		setContent(record);
		setLocation(100, 100);
		this.addWindowListener(new WindowCloser());
		if (editable) {
			addButtons();
		}
		setVisible(true);
	}

	public RecordWindow(LibrisRecord record, LibrisDatabase database) {
		this(record, database, false);
	}

	public void setContent(LibrisRecord record) {
		if (recPanel != null) {
			topPanel.remove(recPanel);
		}
		recPanel = new RecordPanel(record, editable);
		recScroll = new JScrollPane();
		recScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		recScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		recScroll.getViewport().setBounds(recPanel.getBounds());
		recScroll.getViewport().add( recPanel );
		Dimension rpSize = recScroll.getPreferredSize();
		setSize(rpSize.height+50, rpSize.width+50);

		inputPanel.add(BorderLayout.NORTH, recScroll);
		menus = new LibrisMenu(database);
		this.setJMenuBar(menus.createMenus());
	}
	
	private void addButtons() {
		FlowLayout layout;
		JPanel buttonBar = new JPanel(layout = new FlowLayout(FlowLayout.LEFT));
		topPanel.add(BorderLayout.SOUTH, buttonBar);
		JButton importButton = new JButton("Import...");
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");
		buttonBar.add(importButton);
		buttonBar.add(cancelButton);
		buttonBar.add(saveButton);
		
		importButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JTextArea importTextArea = addTextArea();
			}

		});
		saveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				database.addNewRecord(record);
			}
			
		});
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
			System.out.println("recPanel.getWidth: "+recPanel.getWidth()+"recPanel.getHeight: "+recPanel.getHeight());
				// close();
			}
			
		});
	}

	private JTextArea addTextArea() {
		JTextArea textArea = new JTextArea("hello, world");
		JScrollPane textScroll = new JScrollPane();
		textScroll.add(textArea);
		textArea.setSize(50, 50);
		textScroll.setSize(100, 100);
		topPanel.add(BorderLayout.CENTER, textScroll);
		return textArea;
	}
	
	/**
	 *  Helper method that hides the window and disposes of its resources.  Finally, we exit.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}

	/**
	 *  This class handles action events.  The event handler
	 *  simply exists the program.
	 */
	class ExitActionListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {

			close();
		}
	}

	/**
	 *  This class handles window closing event.  The event handler
	 *  simply exists the program.
	 */
	class WindowCloser extends WindowAdapter {

		public void windowClosing(WindowEvent e) {

			if (dataChanged) {
				JDialog d = new JDialog();
				// TODO add detection of data change and confirmation dialogue
			}
			close();
		}
	}

}
