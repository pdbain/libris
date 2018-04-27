package org.lasalledebain.libris.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LocationField extends ValuePairField {

	public LocationField(int height, int width) {
		super(height, width);
	}
	
	private final void showEditableFields(ArrayList<JTextField[]> fieldList) {
		control.removeAll();
		for (JTextField[] rf: fieldList) {
			control.add(rf[0]);
			if (rf.length == 2) {
				control.add(rf[1]);
			}
		}
	}
	
	private final void showNonEditableFields(ArrayList<JTextField[]> fieldList) {
		control.removeAll();
		for (JTextField[] rf: fieldList) {
			String urlText = rf[0].getText();
			if (urlText.isEmpty()) {
				continue;
			}
			JLabel urlFld;
			try {
				URL fieldURL;
				fieldURL = new URL(urlText);
				if (rf.length == 1) {
					urlFld = new URLField(fieldURL);
				} else {
					urlFld = new URLField(fieldURL, rf[1].getText());
				}
			} catch (MalformedURLException e) {
				LibrisWindowedUi.alert(parentFrame, "Invalid URL: "+rf[0].getText(), e);
				if (rf.length == 1) {
					urlFld  = new JLabel(urlText);
				} else {
					urlFld = new JLabel(urlText + ": "+rf[1].getText());
				}
				
			}
			control.add(urlFld);
		}
	}

	@Override
	public void setEditable(boolean editable) {
		if (null == pairTextFields) {
			return;
		}
		if (editable) {
			showEditableFields(pairTextFields);
		} else  {
			showNonEditableFields(pairTextFields);			
		}
	}

	@SuppressWarnings("serial")
	class URLField extends JLabel {
		URL href;
		String linkText;
		private URLField(URL href, String linkText) {
			super(linkText);
			this.href = href;
			this.linkText = linkText;
		}
		private URLField(URL href) {
			this(href, href.toString());
		}
	}
}
