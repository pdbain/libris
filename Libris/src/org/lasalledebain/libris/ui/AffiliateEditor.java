package org.lasalledebain.libris.ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;

@SuppressWarnings("serial")
public class AffiliateEditor {
	Frame ownerFrame;
	JDialog dLog;
	final Vector<KeyIntegerTuple> affInfo;
	final JList affList;
	private SortedKeyValueFileManager<KeyIntegerTuple> namedRecordIndex;
	private final GuiControl guiCtrl;
	public AffiliateEditor(final GuiControl ctrl, Frame ownerFrame, SortedKeyValueFileManager<KeyIntegerTuple> namedRecIndex, 
			Vector<KeyIntegerTuple> affiliateInfo, JList affiliateList, String title) {
		guiCtrl = ctrl;
		affInfo = affiliateInfo;
		affList = affiliateList;
		this.ownerFrame = ownerFrame;
		namedRecordIndex = namedRecIndex;
		dLog = new JDialog(ownerFrame, title);
		final JPanel optionPane = new JPanel();
		optionPane.setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel(new FlowLayout());
		final int selectedAffiliate = affiliateList.getSelectedIndex();
		KeyIntegerTuple editedValue = null;
		if (-1 != selectedAffiliate) {
			editedValue = (KeyIntegerTuple) affiliateList.getSelectedValue();
		}
		final RecordNameFilter recordFilter = new RecordNameFilter(editedValue);
		RecordSelectorByName nameBrowser = new RecordSelectorByName(recordFilter);
		/* 
		 * TODO add buttons to set parent, edit affiliates
		 * TODO list parent and affiliates
		 */
		nameBrowser.setEditable(false);
		contentPanel.add(nameBrowser);
		JTextField parentId = new JTextField(editedValue.toString());
		contentPanel.add(parentId);
		final JButton setParentButton = new JButton("Set parent");
		setParentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				KeyIntegerTuple newAffiliate = (KeyIntegerTuple) recordFilter.getSelectedItem();
				if (selectedAffiliate >= 0) {
					affInfo.set(selectedAffiliate, newAffiliate);
				} else {
					affInfo.add(newAffiliate);
				}
				affList.setListData(affInfo);
				guiCtrl.setModified(true);
				setParentButton.setEnabled(false);
			}			
		});
		contentPanel.add(setParentButton);
		affList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				setParentButton.setEnabled(true);
			}
			
		});
		optionPane.add(contentPanel);
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dLog.setVisible(false);
				dLog.dispose();
			};		
		};
		JPanel buttonBar = new JPanel(new FlowLayout());
		JButton okayButton = new JButton("OK");
		okayButton.addActionListener(buttonListener);
		okayButton.setSelected(true);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(buttonListener);
		buttonBar.add(cancelButton);
		buttonBar.add(okayButton);
		optionPane.add(buttonBar, BorderLayout.SOUTH);
		dLog.setContentPane(optionPane);
		dLog.pack();
		dLog.setLocationRelativeTo(ownerFrame);
		dLog.setVisible(true);
	}

	private class RecordNameFilter extends DefaultComboBoxModel {

		private StringBuffer currentPrefix;

		RecordNameFilter(KeyIntegerTuple editedValue) {
			currentPrefix = new StringBuffer(16);
			initializeList(editedValue);
		}

		private void initializeList(KeyIntegerTuple editedValue) {
			removeAllElements();
			int parentNum = RecordId.getNullId();
			if (null != editedValue) {
				parentNum = editedValue.getValue();
			}
			for (KeyIntegerTuple recNameAndNumber: namedRecordIndex) {
				addElement(recNameAndNumber);
				if (parentNum == recNameAndNumber.getValue()) {
					setSelectedItem(recNameAndNumber);
					editedValue = null;
				}
			}
		}

		String keyTyped(char prefixChar) {
			int lastIndex = currentPrefix.length();
			if ((KeyEvent.VK_BACK_SPACE == prefixChar) || (KeyEvent.VK_DELETE == prefixChar)) {
				if (lastIndex > 0) {

					currentPrefix.deleteCharAt(lastIndex-1);
					initializeList(null);
				}
			} else if (((lastIndex > 0) && Character.isJavaIdentifierPart(prefixChar))
					|| (lastIndex == 0) && Character.isJavaIdentifierStart(prefixChar)) {
				currentPrefix.append(prefixChar);
			}
			// TODO expand the list if characters deleted, add more if the list shrinks
			String prefix = currentPrefix.toString();
			int currentSize = getSize();
			int currentIndex = 0;
			while (currentIndex < currentSize) {
				// TODO 1 class cast exception during typing
				KeyIntegerTuple element = (KeyIntegerTuple) getElementAt(currentIndex);
				String e = element.getKey();
				if (!e.startsWith(prefix)) {
					removeElementAt(currentIndex);
					--currentSize;
				} else {
					++currentIndex;
				}
			}
			if (0 == currentSize) {
				addElement("");
			}
			fireContentsChanged(this, 0, currentSize-1);
			return prefix;
		}
	}

	public class RecordSelectorByName extends JComboBox {
		private final RecordNameFilter recordFilter;

		public RecordSelectorByName (RecordNameFilter recFilter) {
			super(recFilter);
			recordFilter = recFilter;
			setKeySelectionManager(new KeySelectionManager() {

				@Override
				public int selectionForKey(char key, ComboBoxModel model) {
					recordFilter.keyTyped(key);
					return 0;
				}
			}
			);
		}
	}
}
