package org.lasalledebain.libris.ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;

@SuppressWarnings("serial")
public class AffiliateEditor {
	Frame ownerFrame;
	JDialog dLog;
	final Vector<KeyIntegerTuple> affInfo;
	final JList<KeyIntegerTuple> affList;
	private final GuiControl guiCtrl;
	public AffiliateEditor(Record currentRecord, final GuiControl ctrl, LibrisWindowedUi ui, SortedKeyValueFileManager<KeyIntegerTuple> namedRecIndex, 
			Vector<KeyIntegerTuple> affiliateInfo, JList<KeyIntegerTuple> affiliateList, GroupDef grpDef) {
		guiCtrl = ctrl;
		affInfo = affiliateInfo;
		affList = affiliateList;
		this.ownerFrame = ui.getMainFrame();
		dLog = new JDialog(ownerFrame, grpDef.getFieldTitle());
		final JPanel optionPane = new JPanel();
		optionPane.setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel(new FlowLayout());
		final int selectedAffiliate = affiliateList.getSelectedIndex();
		KeyIntegerTuple editedValue = null;
		if (-1 != selectedAffiliate) {
			editedValue = affiliateList.getSelectedValue();
		}
		final RecordNameChooser recordFilter = new RecordNameChooser(editedValue, namedRecIndex);
		RecordSelectorByName nameBrowser = new RecordSelectorByName(recordFilter);
		/* 
		 * TODO add buttons to set parent, edit affiliates
		 * TODO list parent and affiliates
		 */
		nameBrowser.setEditable(false);
		contentPanel.add(nameBrowser);
		JTextField parentId = new JTextField(editedValue.toString());
		JPanel buttonBar = new JPanel(new FlowLayout());
		contentPanel.add(parentId);
		final JButton setParentButton = new JButton("Set parent");
		setParentButton.setEnabled(ctrl.isEditable());
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
				dialogueDispose();
			}			
		});
		buttonBar.add(setParentButton);
		final JButton newChildButton = new JButton("New child record");

		final JButton addAffiliateButton = new JButton("Add affiliate");
		addAffiliateButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				KeyIntegerTuple newAffiliate = (KeyIntegerTuple) recordFilter.getSelectedItem();
				affInfo.add(newAffiliate);
				affList.setListData(affInfo);
				guiCtrl.setModified(true);
				dialogueDispose();
			}
		});
		addAffiliateButton.setEnabled(ctrl.isEditable());
		buttonBar.add(addAffiliateButton);

		final JButton removeParentButton = new JButton("Remove affiliate");
		removeParentButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Dialogue.yesNoDialog(null, "Remove selected affiliate?") == Dialogue.YES_OPTION) {
					if (selectedAffiliate >= 0) {
						if (0 ==selectedAffiliate) {
							affInfo.set(selectedAffiliate, NameList.NULL_ID_TUPLE);
						} else {
							affInfo.remove(selectedAffiliate);
						}
						affList.setListData(affInfo);
						guiCtrl.setModified(true);
					}
				}
				dialogueDispose();
			}
		});
		removeParentButton.setEnabled(ctrl.isEditable());
		buttonBar.add(removeParentButton);

		newChildButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int groupNum = grpDef.getGroupNum();
				ui.newChildRecord(currentRecord, groupNum);
				dialogueDispose();
			}

		});
		buttonBar.add(newChildButton);

		affList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				setParentButton.setEnabled(true);
			}
			
		});
		optionPane.add(contentPanel);
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dialogueDispose();
			};		
		};
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(buttonListener);
		buttonBar.add(cancelButton);
		optionPane.add(buttonBar, BorderLayout.SOUTH);
		dLog.setContentPane(optionPane);
		dLog.pack();
		dLog.setLocationRelativeTo(ownerFrame);
		dLog.setVisible(true);
	}

	public static class RecordSelectorByName extends JComboBox<KeyIntegerTuple> {
		private final RecordNameChooser recordFilter;

		public RecordSelectorByName (RecordNameChooser recFilter) {
			super(recFilter);
			recordFilter = recFilter;
			setKeySelectionManager(new KeySelectionManager() {

				@Override
				public int selectionForKey(char key, ComboBoxModel<?> model) {
					recordFilter.keyTyped(key);
					return 0;
				}
			});
		}
		
		public int getSelectedId() {
			int id = RecordId.NULL_RECORD_ID;
			KeyIntegerTuple recTuple = (KeyIntegerTuple) recordFilter.getSelectedItem();
			if (null != recTuple) {
				id = recTuple.getValue();
			}
			return id;
		}
	}

	private void dialogueDispose() {
		dLog.setVisible(false);
		dLog.dispose();
	}
}
