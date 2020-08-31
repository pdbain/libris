package org.lasalledebain.libris.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;

public class LibrisHttpServer<RecordType extends Record> extends HeadlessUi<RecordType> {

	public LibrisHttpServer(int thePort, String theContext) {
		portNumber = thePort;
		context = theContext;
	}
	int portNumber;
	private String context;
	public static final int default_port = 8080;
	public static final String DEFAULT_CONTEXT="/libris";
	private Server theServer;
	public boolean startServer() {
		theServer = new Server(portNumber);
		ServletContextHandler handler = new ServletContextHandler(theServer, context);
		try {
			LibrisServlet<RecordType> theServlet = new LibrisServlet<RecordType>(this);
			ServletHolder theHolder = new ServletHolder(theServlet);
			handler.addServlet(theHolder, "/");
			theServer.start();
			message("Starting server context="+context+" port="+portNumber);
		} catch (Exception e) {
			alert("Failed to launch server: ", e);
			return false;
		}
		return true;
	}

	@Override
	public LibrisDatabase openDatabase(LibrisDatabaseConfiguration config) throws DatabaseException {
		super.openDatabase(config);
		return currentDatabase;
	}

	@Override
	public boolean start() {
		return startServer();
	}

	@Override
	public boolean stop() {
		message("Stopping server");
		try {
			theServer.stop();
			theServer.join();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
