package ServeurJeu.BD;

import java.sql.*;
import java.util.*;
import java.util.Date;
import org.apache.log4j.Logger;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * This class is a Singleton that provides access to one 
 * connection pool. It creates new connections on demand, 
 * up to a max number if specified. It also makes sure a
 * connection is still open before it is returned to a client.. 
 * A client gets access to the single instance through the 
 * static getInstance() method and can then check-out and 
 * check-in connections from a pool.
 * When the client shuts down it should call the release() method
 * to close all open connections and do other clean up.
 */

public final class DBConnectionsPoolManager{

	private static  DBConnectionsPoolManager manager;       // The single instance
	private static  Logger objLogger = Logger.getLogger(DBConnectionsPoolManager.class);
	private static  GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

	// It's number of given connectons. Did in the scope to manage
	// correctly them
	private int givenNuumber;
	// It's our pool with not used connections
	private ArrayList<Connection> freeConnections = new ArrayList<Connection>();
	// Max number of the connections in the pool
	// if == 0 is ignored
	private static int MAXCONN = config.obtenirNombreEntier("gestionnairebd.max_connections");
	// Min number of the connections in the pool
	private static int MINCONN = config.obtenirNombreEntier("gestionnairebd.min_connections");
	
	private static String PASSWORD = config.obtenirString("gestionnairebd.mot-de-passe");
	private static String HOTE = config.obtenirString("gestionnairebd.hote");
	private static String USER = config.obtenirString("gestionnairebd.utilisateur");
	private static String DRIVER = config.obtenirString("gestionnairebd.jdbc-driver");


	/**
	 * Returns the single instance, creating one if it's the
	 * first time this method is called.
	 *
	 * @return DBConnectionsPoolManager The single instance.
	 */
	static synchronized public DBConnectionsPoolManager getInstance() {
		if (manager == null) {
			manager = new DBConnectionsPoolManager();
		}
		return manager;
	}

	/**
	 * A private constructor since this is a Singleton
	 */
	private DBConnectionsPoolManager() {
		initialize();
	}





	/**
	 * Load and initialise the driver instance with its value
	 * and create min asked number of connections.
	 */
	private void initialize() {
		//Création du driver JDBC
		try {
			Class.forName(DBConnectionsPoolManager.DRIVER);
		} catch (Exception e) {
			// Une erreur est survenue lors de l'instanciation du pilote
			objLogger.error(GestionnaireMessages.message("bd.erreur_creer_driver"), e);

			return;
		}
		
		while(freeConnections.size() < DBConnectionsPoolManager.MINCONN){
			Connection conn = newConnection();
			freeConnections.add(conn);
		}
	}

	/**
	 * Checks in a connection to the pool. Notify other Threads that
	 * may be waiting for a connection.
	 *
	 * @param con The connection to check in
	 */
	public synchronized void freeConnection(Connection conn) {
		// Put the connection at the end of the our List
		// but first verify it  TO DO!!!!
		boolean verify = false;
		try {
			if(conn != null)
			   verify = conn.isValid(0);
		} catch (SQLException e) {	
			objLogger.error("Error in verify DB connection ", e);			
		}
		
		if(verify){
			freeConnections.add(conn);
			givenNuumber--;		
			notifyAll();
		}else{
			
		}
	}

	/**
	 * Checks out a connection from the pool. If no free connection
	 * is available, a new connection is created unless the max
	 * number of connections has been reached. If a free connection
	 * has been closed by the database, it's removed from the pool
	 * and this method is called again recursively.
	 */
	public synchronized Connection getConnection() {
		Connection conn = null;
		
		boolean verify = false;
		
		int size = freeConnections.size();
		
		// get new connection and verify that is valid
		// return to the client only the valid connection
		while(!verify){
			size = freeConnections.size();
			if(size == 0)
				conn = newConnection();

			if (size > 0) {
				// Pick the last Connection in the list
				conn = (Connection) freeConnections.get(size - 1);
			
			}
			else if (DBConnectionsPoolManager.MAXCONN == 0 || givenNuumber < DBConnectionsPoolManager.MAXCONN) {
				conn = newConnection();
			}
			
						
			try {
				if (conn.isClosed()) {
					verify = false;
				}
			}
			catch (SQLException e) {
				verify = false;
			}

			try {
				verify = conn.isValid(0);
			} catch (SQLException e) {	
				objLogger.error("Error in verify DB connection ", e);
				verify = false;
			}
		}
		
		if (conn != null) {
			givenNuumber++;
		}
		return conn;
	}

	/**
	 * Checks out a connection from the pool. If no free connection
	 * is available, a new connection is created unless the max
	 * number of connections has been reached. If a free connection
	 * has been closed by the database, it's removed from the pool
	 * and this method is called again recursively.
	 * 
	 * If no connection is available and the max number has been 
	 * reached, this method waits the specified time for one to be
	 * checked in.
	 *
	 * @param timeout The timeout value in milliseconds
	 */
	public synchronized Connection getConnection(long timeout) {
		long startTime = new Date().getTime();
		Connection con;
		while ((con = getConnection()) == null) {
			try {
				wait(timeout);
			}
			catch (InterruptedException e) {
				objLogger.error(GestionnaireMessages.message("bd.erreur_prendre_DBconn"), e);
			}
			if ((new Date().getTime() - startTime) >= timeout) {
				// Timeout has expired
				return null;
			}
		}
		return con;
	}

	/**
	 * Closes all available connections.
	 */
	public synchronized void release() {
		
		for(Connection conn: freeConnections){
			try {
				conn.close();				
			}
			catch (SQLException e) {
				objLogger.error(GestionnaireMessages.message("bd.erreur_liberer_DBconn"), e);
			}
		}
		freeConnections.clear();
	}

	/**
	 * Creates a new connection, using a userid and password
	 * if specified.
	 */
	private Connection newConnection() {
		Connection conn = null;
		try {
			if (DBConnectionsPoolManager.USER == null) {
				conn = DriverManager.getConnection(DBConnectionsPoolManager.HOTE);
			}
			else {
				conn = DriverManager.getConnection(DBConnectionsPoolManager.HOTE, DBConnectionsPoolManager.USER, DBConnectionsPoolManager.PASSWORD);
			}		
		}
		catch (SQLException e) {
			objLogger.error(GestionnaireMessages.message("bd.erreur_cree_DBconn"), e);
			return null;
		}
		return conn;
	}

} // end class