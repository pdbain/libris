package org.lasalledebain.libris.ui;

public interface ProgressTracker {

	int getAccomplishedWork();

	int addAccomplishedWork(int theWork);

	int getExpectedWork();

	public default void addProgress(int progress) {
		return;
	}
}
