package org.lasalledebain.libris.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.LibrisException;

@SuppressWarnings("serial")
public class DatabaseRecordWindow extends RecordWindow {
	final DatabaseRecord record;
	private JButton artifactButton;
	
	public DatabaseRecordWindow(LibrisWindowedUi ui, Layout layout, DatabaseRecord rec, boolean editable, 
			ActionListener modificationListener) throws LibrisException {
		this(ui, layout, rec, new Point(0, 0), editable, modificationListener);
	}

	public DatabaseRecordWindow(LibrisWindowedUi ui, Layout layout, DatabaseRecord rec, Point position, boolean editable, 
			ActionListener modificationListener) throws LibrisException {
		super(ui, layout, rec, position, editable, modificationListener);
		this.record = rec;
	}

	public DatabaseRecordWindow(LibrisWindowedUi ui, Layout myGuiLayout, DatabaseRecord rec, Point point, 
			ActionListener modificationListener) throws LibrisException {
		this(ui, myGuiLayout,  rec,  point, false, modificationListener);
	}
	
	protected void layOutWindow(LibrisWindowedUi ui, Layout layout, Record rec) throws LibrisException {
		super.layOutWindow(ui, layout, rec);
		String recordName = rec.getName();
		navPanel = new JPanel(new BorderLayout());
		add(navPanel);
		navPanel.add(recordPanel, BorderLayout.CENTER);
		if (null != recordName) {
			navPanel.add(new JLabel(recordName, SwingConstants.CENTER), BorderLayout.NORTH);
		}
		ArtifactParameters artifactInfo = ui.currentDatabase.getArtifactInfo(rec.getArtifactId());
		if (null != artifactInfo) {
			addArtifactButton(artifactInfo);
		}
		recordPanel.setVisible(true);
		ui.fieldSelected(false);
		ui.setSelectedField(null);
	}

	public void addArtifactButton(ArtifactParameters artifactInfo) {
		if (Objects.nonNull(artifactButton)) {
			navPanel.remove(artifactButton);
		}
		if (Objects.nonNull(artifactInfo)) {
			URI archivepath = artifactInfo.getArchivepath();
			final File artifactFile = new File(archivepath);
			artifactButton = new JButton(artifactInfo.getTitle());
			navPanel.add(artifactButton, BorderLayout.SOUTH);
			artifactButton.addActionListener(e -> {
				try {
					Desktop.getDesktop().open(artifactFile);
				} catch (IOException e1) {
					throw new DatabaseError(e1);
				}
			});
		}
	}

	public DatabaseRecord getRecord() {
		return record;
	}

}
