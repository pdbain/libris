package org.lasalledebain.libris.ui;

import javax.swing.SwingWorker;

class LibrisUiWorker extends SwingWorker<Object, Object> {
	private final Runnable workToDo;

	LibrisUiWorker(Runnable theWorkToDo) {
		workToDo = theWorkToDo;
	}

	@Override
	protected Object doInBackground() throws Exception {
		workToDo.run();
		return null;
	}
	
	public void setWorkerProgress(int progress) {
		setProgress(progress);
	}
}