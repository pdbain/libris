package LibrisTest;

import javax.swing.JFrame;

import Libris.AndOrPanel;
import junit.framework.TestCase;

public class TestQuery extends TestCase {
	public void testCreatePanel() {
		JFrame mainFrame = new JFrame();
		mainFrame.setSize(250, 250);
		AndOrPanel testPanel = new AndOrPanel();
		testPanel.addPanes();
		mainFrame.getContentPane().add(testPanel);
		mainFrame.setVisible(true);
	}
}
