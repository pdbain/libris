package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LocationField extends ValuePairField {

	JTextField urlBox, linkTextBox;
	static final JLabel urlLabel = new JLabel("URL: ");
	static final JLabel textLabel = new JLabel("Link text: ");
	public LocationField(int height, int width, boolean editable) {
		super(height, width, editable);
		LayoutManager layout = new BoxLayout(control, BoxLayout.Y_AXIS);
		control.setLayout(layout);
	}
	
	protected void displayControls() {	
		if (editable) {
			showEditableFields();
		} else  {
			showNonEditableFields();			
		}
	}
	
	private final void showEditableFields() {
		control.removeAll();
		JPanel urlPanel = new JPanel();
		urlPanel.add(urlLabel);
		urlBox = new JTextField(mainValue);
		urlPanel.add(urlBox);
		control.add(urlPanel);
		JPanel textPanel = new JPanel();
		textPanel.add(textLabel);
		linkTextBox = new JTextField(extraValue);
		textPanel.add(linkTextBox);
		control.add(textPanel);
	}
	
	private final void showNonEditableFields() {
		/* values have been copied to mainValue & extraValue */
		control.removeAll();
		if (!isEmpty()) {
			JLabel urlFld;
			try {
				final URL fieldURL = new URL(mainValue);
				urlFld = extraValue.isEmpty()? new URLField(fieldURL): new URLField(fieldURL, extraValue);
				if (Desktop.isDesktopSupported()) {
					urlFld.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent e) {
							try {
								Desktop.getDesktop().browse(fieldURL.toURI());
							} catch (IOException | URISyntaxException urlExceptiopn) {
								LibrisWindowedUi.alert(parentFrame, "Invalid URL: "+mainValue, urlExceptiopn);
							}
						}
					});
				}
			} catch (MalformedURLException e) {
				LibrisWindowedUi.alert(parentFrame, "Invalid URL: "+mainValue, e);
				urlFld = extraValue.isEmpty()? new JLabel(mainValue): new JLabel(extraValue);


			}
			control.add(urlFld);
		}
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable = editable;
		showEditableFields();
	}

	@SuppressWarnings("serial")
	class URLField extends JLabel {
		URL href;
		String linkText;
		private URLField(URL href, String linkText) {
			super(linkText);
			this.href = href;
			this.linkText = linkText;
	        setForeground(Color.BLUE.darker());
	        Map<TextAttribute, Object> underlineAttrs = new HashMap<>();
	        underlineAttrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
	        setFont(getFont().deriveFont(underlineAttrs));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		private URLField(URL href) {
			this(href, href.toString());
		}
	}

	@Override
	protected void copyValuesFromControls() {
		if (isEditable()) {
			mainValue = urlBox.getText();
			extraValue = linkTextBox.getText();
		}
	}
}
