package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JOptionPane;

public class Dialogue {
	public static final int YES_OPTION = JOptionPane.YES_OPTION;
	public static final int NO_OPTION = JOptionPane.NO_OPTION;
	public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	public static int yesNoCancelDialog(Component parentComponent, String message) throws HeadlessException {
		int choice = JOptionPane.showConfirmDialog(parentComponent, message, "Libris", JOptionPane.YES_NO_CANCEL_OPTION);
		return choice;
	}
	public static int yesNoDialog(Component parentComponent, String message) throws HeadlessException {
		int choice = JOptionPane.showConfirmDialog(parentComponent, message, "Libris", JOptionPane.YES_NO_OPTION);
		return choice;
	}
}
