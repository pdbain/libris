package org.lasalledebain.libris.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;

public class LibrisHttpServer<RecordType extends Record> extends HeadlessUi {

	public LibrisHttpServer(int thePort, String theContext) {
		portNumber = thePort;
		context = theContext;
	}
	int portNumber;
	private String context;
	public static final int default_port = 8080;
	public static final String DEFAULT_CONTEXT="/libris";
	public void startServer() {
		Server theServer = new Server(portNumber);
		ServletContextHandler handler = new ServletContextHandler(theServer, context);
		try {
			LibrisServlet<RecordType> theServlet = new LibrisServlet<RecordType>(this);
			ServletHolder theHolder = new ServletHolder(theServlet);
			handler.addServlet(theHolder, "/");
			theServer.start();
			theServer.join();
		} catch (Exception e) {
			alert("Error launching server: ", e);
		}
	}

	@Override
	public LibrisDatabase openDatabase(LibrisDatabaseConfiguration config) throws DatabaseException {
		super.openDatabase(config);
		startServer();
		return currentDatabase;
	}
}
