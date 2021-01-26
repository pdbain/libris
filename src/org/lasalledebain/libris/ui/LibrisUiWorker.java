package org.lasalledebain.libris.ui;

import javax.swing.SwingWorker;

abstract class LibrisUiWorker extends SwingWorker<Object, Object> {
	@Override
	abstract protected Object doInBackground() throws Exception;
	
	public void setWorkerProgress(int progress) {
		setProgress(progress);
	}
}