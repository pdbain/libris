package org.lasalledebain.libris.ui;

import javax.servlet.http.HttpServletResponse;

public class HttpParameters {
	public final int recId;
	public final int browserFirstId;
	public final HttpServletResponse resp;

	public HttpParameters(int theRecId, int theFirstId, HttpServletResponse theResp) {
		recId = theRecId;
		resp = theResp;
		browserFirstId = theFirstId;
	}
}