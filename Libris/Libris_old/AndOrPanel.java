package Libris;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
public class AndOrPanel extends JPanel {
	private JPanel mainPanel;
	private JSplitPane orPane, andPane;
	private JTextArea searchTerms;
	private AndOrPanel orSubQuery, andSubQuery; 
	/**
	 * 
	 */
	public AndOrPanel() {
		super();
		this.setSize(200, 200);
		setLayout(new FlowLayout());
		this.add(new JTextArea("Pebbles\nBam-bam"));
	}
	public void addPanes() {
		this.removeAll();
		searchTerms = new JTextArea("fred\nBarney");
		searchTerms.setLayout(new FlowLayout());
		orSubQuery = new AndOrPanel();
		andSubQuery = new AndOrPanel();
		andPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchTerms, orSubQuery);
		orPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, andPane, andSubQuery);
		this.add(orPane);
	}
	
	
}
