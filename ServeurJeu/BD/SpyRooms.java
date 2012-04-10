package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
	
	private static Logger objLogger = Logger.getLogger( SpyRooms.class );

	
	
	// Constructor
	public SpyRooms(ControleurJeu controleur, int controlTime){
	
		objControleurJeu = controleur;
		DELAY = controlTime;
		stopSpy = false;
		
		DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
        
        createStatement();      		
	}
	
	
	 /**
     * Method used to release the problematic connection
     * and to take another one
     */
    public void getNewConnection(){
    	releaseConnection();
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
        connexion = pool.getConnection();
		createStatement();
		objLogger.info("bd statement created");

    }
    
    
    /**
     * Cette fonction permet de créer un objet requête
     */
    public void createStatement() {

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

    }
    
    /**
     * Return the connection to the pool, 
     * but first close the statement created from this connection
     */
    public void releaseConnection(){
    	try {
			requete.close();
		} catch (SQLException e) {
			objLogger.error(GestionnaireMessages.message("bd.erreur_fermer_requete"));			
		}
    	DBConnectionsPoolManager pool = DBConnectionsPoolManager.getInstance();
    	pool.freeConnection(connexion);
    	connexion = null;
    }
	
	/**
	 * Thread run and periodically put the new rooms from DB in 'ControleurJeu' 
	 * and remove old rooms
	 */
    public void run() {
    	while (stopSpy == false) {
    		if(connexion == null)
    			getNewConnection();

    		// Update rooms liste 
    		detectNewRooms(objControleurJeu.removeOldRooms());
    		
    		// Detect changes in the existing rooms  
    		detectUpdates(); 
    		
    		objControleurJeu.detectErasedRooms();

    		try
    		{
    			// Bloquer la thread jusqu'à la prochaine mise à jour
    			Thread.sleep(DELAY);
    		}
    		catch( InterruptedException e )
    		{
    			objLogger.info(GestionnaireMessages.message("spy.erreur_thread_newRooms"));
    			objLogger.error( e.getMessage());
    			e.printStackTrace();    			
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
			ResultSet rs = null;
			try
			{
				rs = requete.executeQuery( "SELECT room.room_id FROM room where ((beginDate < NOW() AND endDate > NOW()) OR (beginDate is NULL AND endDate > NOW()) OR (beginDate < NOW() AND endDate is NULL) OR (beginDate is NULL AND endDate is NULL)) AND room_id NOT IN (" + list + ");" );
				while(rs.next())
				{
					int roomId = rs.getInt("room.room_id");
					rooms.add(roomId);
				}   

			}
			catch (SQLException e)
			{
				// Une erreur est survenue lors de l'exécution de la requète
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_detect_newRoom"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
				
			    e.printStackTrace();
			    getNewConnection();
			}
			catch( RuntimeException e)
			{
				//Une erreur est survenue lors de la recherche de la prochaine salle
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_detect_newRoom"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
			    e.printStackTrace();
			    getNewConnection();
			}finally{ 
				dbUtilCloseResultSet(rs, "Error in release ResultSet in detectNewRooms");    			
			}
			
			//put in Controleur finded rooms
			objControleurJeu.obtenirGestionnaireBD().fillRoomsList(rooms);
			
			//now find all old rooms from BD and fill in ArrayList
			rooms.clear();
			try
			{
				rs = requete.executeQuery( "SELECT room.room_id FROM room where  endDate < NOW() AND room_id IN (" + list + ");" );
				while(rs.next())
				{
					int roomId = rs.getInt("room.room_id");
					rooms.add(roomId);
				}
			}				
			catch (SQLException e)
			{
				// Une erreur est survenue lors de l'exécution de la requète
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_oldRoom"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );				
			    e.printStackTrace();
			    getNewConnection();
			}
			catch( RuntimeException e)
			{
				//Une erreur est survenue lors de la recherche de la prochaine salle
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_oldRoom"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
			    e.printStackTrace();
			    getNewConnection();
			}finally{ 
				dbUtilCloseResultSet(rs, "Error in release ResultSet in mettreAJourJoueur");    			
			}
			
			//put in Controleur finded rooms
			objControleurJeu.removeOldRooms(rooms);
			
	}// end methode detectNewRooms
		
	
	/**
	 * Method used to detect the rooms to be updated in the controler list
	 */
	private void detectUpdates()
	{
		   ArrayList<Integer> rooms = new ArrayList<Integer>();		
		   //find all rooms with updates in the DB  and fill in ArrayList
		   ResultSet rs = null;
			try
			{
				rs = requete.executeQuery( "SELECT room.room_id FROM room where room.update = 1;" );
				while(rs.next())
				{
					int roomId = rs.getInt("room.room_id");
					rooms.add(roomId);
				}   


			}
			catch (SQLException e)
			{
				// Une erreur est survenue lors de l'exécution de la requète
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_newRoomSelect1"));
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
			}finally{ 
				dbUtilCloseResultSet(rs, "Error in release ResultSet in detectUpdates");    			
			}
			
			//update in Controler finded rooms
			objControleurJeu.updateRooms(rooms);
			
			//after did updates remove key in db
			try
			{
				String update = "UPDATE room SET room.update = 0;";
				requete.executeUpdate(update);
			}
			catch (SQLException e)
			{
				// Une erreur est survenue lors de l'exécution de la requète
				objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_updateRoom0"));
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
			
			
	}// end methode detectUpdates
	
	public void stopSpy(){
		releaseConnection();
		stopSpy = true;		
	}
	
	private static void dbUtilCloseResultSet(ResultSet rs, String message)
    {
    	if(rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				objLogger.error(message);
			}
    }
}
