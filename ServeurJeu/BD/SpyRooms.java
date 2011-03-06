package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Configuration.GestionnaireMessages;
import org.apache.log4j.Logger;

/**
 * @author Lilian Oloieri
 */

public class SpyRooms implements Runnable {

	// Declaration of object that points to ControleurJeu
	private final ControleurJeu objControleurJeu;
	
	// Declaration of variable that indicate the time in  milliseconds 
	// between each DB cheqing 
	private final int DELAY;
	
	// Used to stop the spyDB
	private Boolean stopSpy;
	
	// Objet Connection nécessaire pour le contact avec le serveur MySQL
	private Connection connexion;
	
	// Objet Statement nécessaire pour envoyer une requète au serveur MySQL
	private  Statement requete;
	
	static private Logger objLogger = Logger.getLogger( SpyRooms.class );

	
	
	// Constructor
	public SpyRooms(ControleurJeu controleur, int controlTime){
	
		objControleurJeu = controleur;
		DELAY = controlTime;
		stopSpy = false;
		
		DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
        
        this.takeStatement();      		
	}
	
	
	 /**
     * Method used to release the problematic connection
     * and to take another one
     */
    public void getNewConnection(){
    	this.releaseConnection();
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
		this.takeStatement();
    }
    
    
    /**
     * Cette fonction permet de créer un objet requête
     */
    public void takeStatement() {

    	// Création de l'objet "requête"
    	try {
    		requete = connexion.createStatement();
    	} catch (SQLException e) {
    		// Une erreur est survenue lors de la création d'une requête
    		objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
    		objLogger.error(GestionnaireMessages.message("bd.trace"));
    		objLogger.error(e.getMessage());
    		this.getNewConnection();    		
    		return;
    	}

    }
    
    /**
     * Return the connection to the pool, 
     * but first close the statement created from this connection
     */
    public void releaseConnection(){
    	try {
			this.requete.close();
		} catch (SQLException e) {
			objLogger.error(GestionnaireMessages.message("bd.erreur_fermer_requete"));			
		}
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
    	pool.freeConnection(this.connexion);
    	this.connexion = null;
    }
	
	/**
	 * Thread run and periodically put the new rooms from DB in 'ControleurJeu' 
	 * and remove old rooms
	 */
	public void run() {
		while (stopSpy == false) {
			if(this.connexion == null)
				this.getNewConnection();
			try
	    	{
	            // Update rooms liste 
				///System.out.println("test - ");
				detectNewRooms(objControleurJeu.removeOldRooms());

                // Bloquer la thread jusqu'à la prochaine mise à jour
               	Thread.sleep(DELAY);
	               	
	    	}
			catch( InterruptedException e )
			{
    			objLogger.info(GestionnaireMessages.message("spy.erreur_thread"));
				objLogger.error( e.getMessage());
				e.printStackTrace();
				
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Method used to detect new rooms to be activated and put in the list
	 */
	private void detectNewRooms(ArrayList<Integer> rooms)
	{
		    //to not select the existed rooms   
			String list = "";
			for (int room : rooms)
			{
				list += room + ",";
			}
			
		    int ind = list.lastIndexOf(",");
		    if(ind > 0)
		       list = list.substring(0, ind);
		    else list = "0";
            
			rooms.clear();
            
			//find all new rooms  and fill in ArrayList
			try
			{
				synchronized( requete )
				{
					ResultSet rs = requete.executeQuery( "SELECT room.room_id FROM room where ((beginDate < NOW() AND endDate > NOW()) OR (beginDate is NULL AND endDate > NOW()) OR (beginDate < NOW() AND endDate is NULL) OR (beginDate is NULL AND endDate is NULL)) AND room_id NOT IN (" + list + ");" );
					while(rs.next())
					{
						int roomId = rs.getInt("room.room_id");
						//System.out.println(roomId + "NEW");				
						rooms.add(roomId);
					}   
								
				}
			}
			catch (SQLException e)
			{
				// Une erreur est survenue lors de l'exécution de la requète
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_newRoom"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
				
			    e.printStackTrace();
			    getNewConnection();
			}
			catch( RuntimeException e)
			{
				//Une erreur est survenue lors de la recherche de la prochaine salle
				objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
			    e.printStackTrace();
			    getNewConnection();
			}  
			
			//put in Controleur finded rooms
			objControleurJeu.obtenirGestionnaireBD().fillRoomList(rooms);
			
	}// end methode detectNewRooms
		
	
	public void stopSpy(){
		this.releaseConnection();
		this.stopSpy = true;		
	}
	
	
}
