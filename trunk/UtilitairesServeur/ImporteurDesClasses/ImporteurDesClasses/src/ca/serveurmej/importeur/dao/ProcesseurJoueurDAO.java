package ca.serveurmej.importeur.dao;

/**
 * Class responsable pour l'insertion des donnees dans la BD mysql
 */

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import java.sql.Statement;

public class ProcesseurJoueurDAO {
	
	private static final String pstmtInsertUser = "INSERT INTO jos_users (name, username, email, password, usertype, gid, registerDate, lastvisitDate, activation, params, teacher_ref) VALUES(?,?,?,PASSWORD(?),?,?,?,?,?,?,?)";
	private static final String pstmtInsertJosCompro = "INSERT INTO jos_comprofiler (id, user_id, firstname, lastname, registeripaddr, cbactivation, acceptedterms, cb_gradelevel, cb_gender, cb_school, cb_country, cb_province)  VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String pstmtInsertJosCor = "INSERT INTO jos_core_acl_aro (section_value, value, name) VALUES(?,?,?)";
	private static final String pstmtInsertJosCorMap = "INSERT INTO jos_core_acl_groups_aro_map (group_id, section_value, aro_id) VALUES(?,?,?)";
	
	
	private static final Logger logger = LogManager.getLogger(ProcesseurJoueurDAO.class);
	private static final String appConfigFilename = "etc/app-configuration.ini";
	private final Properties config;
	private String[] data;
	private MysqlConnectionPoolDataSource mysqlDataSource;
	
	private static final int SERVER_NAME=0;
	private static final int DB_NAME=1;
	private static final int USER=2;
	private static final int PASSWORD=3;
	
	private StringBuilder output;
	
	public ProcesseurJoueurDAO() {
		
	}
	
	public ProcesseurJoueurDAO (String[] initdata) {
		
		this.data = initdata;
		logger.info("*************** Init DB processing ************************");
	}
	
	// initier les data sources
	{
		logger.info("*************** Init mysql data source creation **********************");
		config = new Properties();
		output = new StringBuilder();
		
		try
		{
			//InputStream conf = ProcesseurJoueurDAO.class.getResourceAsStream(appConfigFilename);
			config.load(new InputStreamReader(new FileInputStream(appConfigFilename), "UTF-8"));
			
			if (data == null){
				data = new String[]{getProperty("db.mej.server"), getProperty("db.mej.name"), getProperty("db.mej.user"), getProperty("db.mej.password")};
			}
		} catch(Exception e)
		{
			logger.error(e.getMessage() + e.fillInStackTrace() + e.getCause());
			output.append("Le traitement de fichier csv a planté a cause d'un erreur de connexion au base de données. Aucun utilisateur créer.\n");
		}
						
		mysqlDataSource = createDataSource(data); //this does not attempt to connect, it merely sets the connection parameters.
		
		logger.info("*************** end mysql data source creation **********************");
		
	}	
	
	
	public String insererJoueurs(List<CSVRecord> listeJoueurs) {
		
		List<CSVRecord> joueurs = listeJoueurs;
		Connection connexion = getConnection();		
		
		//output.append("\n *** Inserer les utilisateurs *** \n\n");
		
		
		String ecole = "";		
		String pays = "";
		String province = "";
		boolean estAjoute = false;
		
		logger.info("*************** begin to process csv records list **********************");
		
		for (CSVRecord joueur : joueurs) {			
						
			String firstname = joueur.get(0);
			String lastname = joueur.get(2);
			String username = joueur.get(4);
			
			
			// on ignore le premiere enregistrement qui contient le map
			if(firstname.equals("firstname") && username.equals("username")) {
				continue;
			}
			
			if(validerNomUtilisateur(username)) {
				output.append("Le profil avec nom utilisateur - " + username + " - non créé. Nom utilisateur déjà existante.\n");
				continue;
			}
						
			//validation de nom utilisateur
			if(username.isEmpty() && !firstname.isEmpty()) {
				username = firstname;
			}
			
			if(username.isEmpty() && firstname.isEmpty() && !lastname.isEmpty()) {
				username = lastname;
			}
			
			// on a besoin du nom utilisateur, autrement pas de insertion
			if(username.isEmpty() && firstname.isEmpty() && lastname.isEmpty()) {
				output.append("Erreur dans les données. Un des profils non inséré.\n");
				continue;
			}
			
			// set the values for ecole - pays - province - get the first non empty values
			// and use them till the end if the values from the next row are empty
			
			if(ecole.isEmpty() && !joueur.get(14).isEmpty()) {
				ecole = joueur.get(14);
			}
			
			if(pays.isEmpty() && !joueur.get(15).isEmpty()) {
				pays = joueur.get(15);
			}
			
			if(province.isEmpty() && !joueur.get(16).isEmpty()) {
				province = joueur.get(16);
			}
			
			try {
				
				// Ajouter l'information pour cette utilisateur dans jos_users
				PreparedStatement prepStatementInsertJosUsers = connexion.prepareStatement(pstmtInsertUser, Statement.RETURN_GENERATED_KEYS);			
				
				prepStatementInsertJosUsers.setString(1, firstname + " " + lastname); // name
				
				prepStatementInsertJosUsers.setString(2, username); // username
				prepStatementInsertJosUsers.setString(3, "" + joueur.get(6)); // email
				prepStatementInsertJosUsers.setString(4, joueur.get(7)); //password 
				prepStatementInsertJosUsers.setString(5, joueur.get(8)); //usertype
				prepStatementInsertJosUsers.setInt(6, Integer.parseInt(joueur.get(9))); //gid				
				prepStatementInsertJosUsers.setDate(7, new java.sql.Date(System.currentTimeMillis())); //registeredDate				
				prepStatementInsertJosUsers.setDate(8, new java.sql.Date(System.currentTimeMillis())); //lastvisitDate
				prepStatementInsertJosUsers.setString(9, ""); // activation
				prepStatementInsertJosUsers.setString(10, "language=" + joueur.get(10) + "\ntimezone=" + joueur.get(11) + "\n\n"); // params
				prepStatementInsertJosUsers.setInt(11, Integer.parseInt(joueur.get(17))); //gid		
				
				executeUpdate(prepStatementInsertJosUsers,"An SQL error occured when trying to add a user to the table jos_users in DB.");
				
				ResultSet rs = prepStatementInsertJosUsers.getGeneratedKeys();
				rs.next();
				int userId = rs.getInt(1);
				rs.close();

				// second insert in jos_comprofiler
				PreparedStatement prepStatementInsertJosComprofiler = connexion.prepareStatement(pstmtInsertJosCompro);			
 
				prepStatementInsertJosComprofiler.setInt(1, userId);
				prepStatementInsertJosComprofiler.setInt(2, userId);
				prepStatementInsertJosComprofiler.setString(3, firstname);
				prepStatementInsertJosComprofiler.setString(4, lastname);
				prepStatementInsertJosComprofiler.setString(5, joueur.get(5));
				prepStatementInsertJosComprofiler.setString(6, "");
				prepStatementInsertJosComprofiler.setInt(7, 1);
				prepStatementInsertJosComprofiler.setString(8, joueur.get(12));
				prepStatementInsertJosComprofiler.setString(9, joueur.get(13));
				prepStatementInsertJosComprofiler.setString(10, joueur.get(14).isEmpty() ? ecole : joueur.get(14));
				prepStatementInsertJosComprofiler.setString(11, joueur.get(15).isEmpty() ? pays : joueur.get(15));
				prepStatementInsertJosComprofiler.setString(12, joueur.get(16).isEmpty() ? province : joueur.get(16));


				executeUpdate(prepStatementInsertJosComprofiler,"An SQL error occured when trying to add a user to the table jos_comprofiler in DB.");

				//third insert for users
				PreparedStatement prepStatementInsertJosCoreAro = connexion.prepareStatement(pstmtInsertJosCor, Statement.RETURN_GENERATED_KEYS);
				prepStatementInsertJosCoreAro.setString(1, "users");
				prepStatementInsertJosCoreAro.setString(2, "" + userId);
				prepStatementInsertJosCoreAro.setString(3, firstname + " " + lastname);

				executeUpdate(prepStatementInsertJosCoreAro,"An SQL error occured when trying to add a user to the table jos_core_acl_aro in the DB.");

				ResultSet rscor = prepStatementInsertJosCoreAro.getGeneratedKeys();
				rscor.next();
				int corId = rscor.getInt(1);
				rscor.close();

				// last inserts
				PreparedStatement prepStatementInsertJosCoreMap = connexion.prepareStatement(pstmtInsertJosCorMap, Statement.RETURN_GENERATED_KEYS);
				prepStatementInsertJosCoreMap.setInt(1, 18);
				prepStatementInsertJosCoreMap.setString(2, "");
				prepStatementInsertJosCoreMap.setInt(3, corId);

				executeUpdate(prepStatementInsertJosCoreMap, "An SQL error occured when trying to add a user to the table jos_core_acl_groups_aro_map in DB.");
				
				estAjoute = true;

			} catch (SQLException se) {
				logger.error(se.getMessage() + "\n " + se.fillInStackTrace());
				output.append("L'insertion du joueur ");
				output.append(username);
				output.append(" a failli du a un erreur de traitement dans la BD.\n");
				
			} catch (Exception ex) {
				logger.error(ex.getMessage() + "\n " + ex.fillInStackTrace());
				output.append("L'insertion du joueur ");
				output.append(username);
				output.append(" a failli du a un erreur de traitement dans la BD.\n");
				
				try {
					connexion.close();
					connexion = getConnection();
				} catch (SQLException se) {
					logger.error(" " + se.fillInStackTrace());
				}
				
			} finally {
				
			} // Catch SQLException
			
			if(estAjoute){
				output.append("Utilisater  : ");
				output.append(username);
				output.append(" a été inséré avec succès.\n");
			}
			
			logger.info("*************** end to process csv records list **********************");
		} // end process players list in the for 
		
		logger.info("*************** End DB processing ************************");
		
		return output.toString();		
	}
	/*
	 * done true si le nom utilisateur present dans BD
	 */
	private boolean validerNomUtilisateur(String username) {
		
		Connection connexion = getConnection();
		boolean estPresent = false;
		try
		{
			java.sql.Statement stmt = connexion.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT username FROM jos_users WHERE username like '" + username + "%'");
					
			while (rs.next())
			{
				String nom = rs.getString("username");
				if (nom.equals(username)) estPresent = true;				
			}
			
			rs.close();
			stmt.close();
			connexion.close();		

		} catch (SQLException sqle)
		{
			try {connexion.close();}catch(Exception e){};
		}
		
		return estPresent;				
	}

	private Connection getConnection() {
		Connection connexion = null;	
		
		try {
			connexion = mysqlDataSource.getConnection();	
			
		} catch (SQLException se) {
			
			logger.error("Error on getting mysql connection : " + se.getErrorCode() + se.fillInStackTrace());
		} // Catch SQLException
		
		return connexion;
	}
	
	private int executeUpdate(PreparedStatement stmt, String errorHint) throws Exception
	{
		try
		{
			return stmt.executeUpdate();
		}
		catch(SQLException sqle)
		{
			throw new Exception(errorHint + "\n" +
					"The offending query was:\n" + stmt + "\n" +
					"---Technical SQL error message----------\n" +
					"SQLException: " + sqle.getMessage() + "\n" +
					"SQLState: " + sqle.getSQLState() + "\n" + 
					"VendorError: " + sqle.getErrorCode() + "\n");
		}
	}
	
	private MysqlConnectionPoolDataSource createDataSource(String[] data)
	{
		MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
		ds.setServerName(data[SERVER_NAME]);
		ds.setDatabaseName(data[DB_NAME]);
		ds.setUser(data[USER]);
		ds.setPassword(data[PASSWORD]);
		ds.setEncoding("UTF-8");
		return ds;
	}
	
	private String getProperty(String name) throws Exception
	{
		String prop = config.getProperty(name);
		if (prop == null) throw new Exception("Configuration file property called: '" + name + "'");
		return prop.trim();
	}
}
