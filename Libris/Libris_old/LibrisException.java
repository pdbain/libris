package Libris;

public class LibrisException extends Exception {
	private ErrorIds errId;
	private static int currNum = 1;
	public static final int maxErrNum = 100;
	private static boolean suppress[] = new boolean[maxErrNum];

	public ErrorIds getErrId() {
		return errId;
	}
	public static enum ErrorIds {
		ERR_NO_START_POS("cannot find start position for record "),
		ERR_ADD_TO_INT("Cannot add values to an integer field"),
		ERR_ADD_TO_BOOLEAN("Cannot add values to a boolean field"),
		ERR_ADD_TO_INDEX("Cannot add values to an index entry field"),
		ERR_NO_RECORD_POSITION_FILE("Cannot open record position file: "),
		ERR_READ_RECORD_POSITION("Error reading record position file"),
		ERR_NO_USER_HOME_DIR("System property user.home not defined"),
		ERR_CANT_SAVE_PREFS("Cannot save preferences file"),
		ERR_INVALID_ENUMSET_ID("enumset ID not defined or a duplicate of existing ID"),
		ERR_NO_INDEX_FILE("Cannot open index file: "), 
		ERR_ENUMCHOICE_NOT_IN_ENUMSET("enumchoice element is not inside an enumset element"), 
		ERR_INVALID_ENUMCHOICE_ID("enumchoice ID not defined or a duplicate of existing ID"), 
		ERR_INVALID_ENUMCHOICE_VALUE("no value attribute defined for enumchoice ID "), 
		ERR_ADD_TO_ENUM("Cannot add values to an enum field"), 
		ERR_UNDEFINED_ENUMSET_ID("Invalid enumset reference"), ERR_NO_ENUMSET_ID("No enumset declared for field "), 
		ERR_ENUMSET_IN_NON_ENUM_FIELD("Enumset attrbute is not allowed in non-enum field ")
		;
		private int errNum;
		private String msg;

		ErrorIds(String msg) {
			this.errNum = currNum+1;
			this.msg = msg;
		}

		public int getNum() {
			return this.errNum;
		}
		public String getMsg() {
			return this.msg;
		}
}
	public static boolean setSuppress(ErrorIds id, boolean value) {
		int	errNum = id.getNum();
		suppress[errNum] = value;
		return true;
	}

	public LibrisException(ErrorIds errId, String errorString) {
		this.errId = errId;
		if (!suppress[errId.getNum()]) {
			this.printStackTrace(System.err);
			System.err.println("ELBR"+Integer.toString(errId.getNum())+": "+errId.getMsg()+" "+errorString);
		}
	}

	public LibrisException(ErrorIds errId) {
		this.errId = errId;
		if (!suppress[errId.getNum()]) {
			this.printStackTrace(System.err);
			System.err.println("ELBR"+Integer.toString(errId.getNum())+": "+errId.getMsg());
		}
	}

}
