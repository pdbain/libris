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
import javax.swing.text.JTextComponent;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;

public class LocationField<RecordType extends Record> extends ValuePairField<RecordType> {

	JTextField urlBox, linkTextBox;
	static final JLabel urlLabel = new JLabel("URL: ");
	static final JLabel textLabel = new JLabel("Link text: ");
	public LocationField(int height, int width, boolean editable) {
		super(height, width, editable);
		LayoutManager layout = new BoxLayout(control, BoxLayout.Y_AXIS);
		control.setLayout(layout);
		copyValuesToControls();
	}
	
	protected JPanel displayControls() {	
		return new JPanel();			
	}
	
	private final void showEditableFields() {
		JPanel urlPanel = new JPanel();
		urlPanel.add(urlLabel);
		urlBox = new JTextField(mainValue, width);
		addModificationListener(urlBox);
		urlPanel.add(urlBox);
		control.add(urlPanel);
		JPanel textPanel = new JPanel();
		textPanel.add(textLabel);
		linkTextBox = new JTextField(extraValue, width);
		addModificationListener(linkTextBox);
		textPanel.add(linkTextBox);
		control.add(textPanel);
	}
	
	private final void showNonEditableFields() {
		/* values have been copied to mainValue & extraValue */
		control.removeAll();
		if (!isEmpty()) {
			JLabel urlFld;
			URL fieldURL = checkURL();
			if (null != fieldURL) {
				URL finalUrl = fieldURL;
				urlFld = extraValue.isEmpty()? new URLField(finalUrl): new URLField(finalUrl, extraValue);
				if (Desktop.isDesktopSupported()) {
					urlFld.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent e) {
							try {
								Desktop.getDesktop().browse(finalUrl.toURI());
							} catch (IOException | URISyntaxException urlExceptiopn) {
								LibrisWindowedUi.alert(parentFrame, "Invalid URL: "+mainValue, urlExceptiopn);
							}
						}
					});
				}
				control.add(urlFld);
			}
		}
	}

	protected URL checkURL() {
		URL fieldURL;
		try {
			fieldURL = new URL(mainValue);
		} catch (MalformedURLException e) {
			fieldURL = null;
			LibrisWindowedUi.alert(parentFrame, "Invalid URL: "+mainValue, e);
		}
		return fieldURL;
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
	protected
	void copyValuesFromControls() {
		if (isEditable()) {
			mainValue = urlBox.getText();
			extraValue = linkTextBox.getText();
		}
	}

	@Override
	protected void checkFieldValues() throws FieldDataException {
		if (mainValue.isEmpty()) {
			if (!extraValue.isEmpty()) {
				throw new FieldDataException("URL is empty");
			}
		} else {
			if (null == checkURL()) {
				throw new FieldDataException("URL "+mainValue+" is invalid");
				}
		}

	}

	@Override
	protected void copyValuesToControls() {
		if (editable) {
			showEditableFields();
		} else  {
			showNonEditableFields();			
		}
	}
	
	protected void addModificationListener(JTextComponent comp) {
		comp.getDocument().addDocumentListener(getModificationListener());
	}
}



