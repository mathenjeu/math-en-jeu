package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Brind'Amour
 * @author Oloieri Lilian
 */
public abstract class GestionnaireBD {

    public static final SimpleDateFormat mejFormatDate = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat mejFormatHeure = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat mejFormatDateHeure = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    
    // Objet Connection nécessaire pour le contact avec le serveur MySQL
    protected Connection connexion;
    // Objet Statement nécessaire pour envoyer une requête au serveur MySQL
    protected Statement requete;
    protected static Logger objLogger = Logger.getLogger(GestionnaireBD.class);
    protected Object DB_LOCK = new Object();
    
    
    
    /**
     * Constructeur de la classe GestionnaireBD qui permet de garder la
     * référence vers le contrôleur de jeu
     */
    public GestionnaireBD() {
        super();
       
        // get connection from the pool
        DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
        
        // Création de l'objet "requête"
        createStatement();  
    }

    /**
     * Method used to release the problematic connection
     * and to take another one
     */
    protected void getNewConnection(){
    	releaseConnection();
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
		createStatement();
    }
    
       
    /**
     * Cette fonction permet de créer un objet requête
     */
    protected void createStatement() {

    	// Création de l'objet "requête"
    	try {
    		requete = connexion.createStatement();
    	} catch (SQLException e) {
    		// Une erreur est survenue lors de la création d'une requête
    		objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
    		objLogger.error(GestionnaireMessages.message("bd.trace"));
    		objLogger.error(e.getMessage());
    		getNewConnection();    		
    		return;
    	}
    	objLogger.info("bd statement created");
    }
    
    /**
     * 
     */
    protected void releaseConnection(){
    	try {
			requete.close();				
		} catch (SQLException e) {
			objLogger.error(GestionnaireMessages.message("bd.erreur_fermer_requete"));			
		}
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
    	pool.freeConnection(connexion);
    	connexion = null;
    	objLogger.info("bd connection released");
    }
    
   
    /**
     * Cette méthode permet de fermer la connexion de base de données qui
     * est ouverte.
     */
    public void arreterGestionnaireBD() {
    	releaseConnection();
    	DB_LOCK = null;
    }
    
    
    protected static void dbUtilCloseResultSet(ResultSet rs, String message)
    {
    	if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				objLogger.error(message);
			}
    	}
    }
    
    protected static void dbUtilClosePreparedStatement(PreparedStatement prep, String message)
    {
    	if(prep != null){
			try {
				prep.close();
			} catch (SQLException e) {
				objLogger.error(message);
			}
    	}
    }
    
    
}// end class

