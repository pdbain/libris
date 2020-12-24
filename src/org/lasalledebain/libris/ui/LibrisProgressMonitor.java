package org.lasalledebain.libris.ui;

import javax.swing.SwingWorker;

public class LibrisProgressMonitor {
	final Runnable task;
	public LibrisProgressMonitor(Runnable theTask) {
		this.task = theTask;
	}
	
	private class Worker extends SwingWorker<Object, Object> {

		@Override
		protected Object doInBackground() throws Exception {

			return null;
		}

	}
}