package Libris;

public class LibrisOptions {
	private static final String DASH_FILE = "--database";
	private String currentFileName;
	public  void parseArgs(String[] args) {
		int fileNamePosition = -1;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals(DASH_FILE)) {
				fileNamePosition = i + 1;
			} else if ((i == args.length-1) && !args[i].startsWith("--")) {
				fileNamePosition = i;
			}
		}
		if (fileNamePosition >= 0) {
			setCurrentDBName(args[fileNamePosition]);
		}
	}
	private void setCurrentDBName(String fileName) {
		this.currentFileName = fileName;
	}
	public String getCurrentDBName() {
		return this.currentFileName;
	}

}
