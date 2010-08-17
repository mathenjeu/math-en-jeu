package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import ServeurJeu.ControleurJeu;
import ServeurJeu.Configuration.GestionnaireConfiguration;
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
		connexionDB();
	}
	
	/**
	 * Thread run and periodically put the new rooms from DB in 'ControleurJeu' 
	 * and remove old rooms
	 */
	public void run() {
		while (stopSpy == false) {
			try
	    	{
	            // Update rooms liste 
				detectNewRooms(objControleurJeu.removeOldRooms());

                // Bloquer la thread jusqu'à la prochaine mise à jour
               	Thread.sleep(DELAY);
	               	
	    	}
			catch( Exception e )
			{
				
				objLogger.info(GestionnaireMessages.message("spy.erreur_thread"));
				objLogger.error( e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Methode used to detect new rooms to be activated and puted in list
	 */
	private void detectNewRooms(ArrayList<Integer> rooms)
	{
		    //System.out.println("start : " + System.currentTimeMillis());
		    //to not select the existed rooms   
			String list = "";
			for (int room : rooms)
			{
				list += room + ",";
				//System.out.println(list);
			}
		    int ind = list.lastIndexOf(",");
		    if(ind > 0)
		       list = list.substring(0, ind);  
            //Object[] roomS =  rooms.toArray();
			rooms.clear();
            /*
			PreparedStatement prepStatement = null;
			try {
				   prepStatement = connexion.prepareStatement("SELECT room.room_id FROM room where ((beginDate < NOW() AND endDate > NOW()) OR beginDate is NULL OR endDate is NULL) AND room_id NOT IN (?);" );
			
					
						
						// Ajouter l'information pour cette salle
						prepStatement.setObject(1, roomS);
								
						ResultSet rs = prepStatement.executeQuery();
						while(rs.next())
						{
							int roomId = rs.getInt("room.room_id");
							//System.out.println(roomId + "NEW");				
							rooms.add(roomId);
						}   
					
				}
				catch (Exception e)
				{
					System.out.println(GestionnaireMessages.message("bd.erreur_spy_rooms") + e.getMessage());
				}
			
            */
			
			//find all new rooms  and fill in ArrayList
			try
			{
				synchronized( requete )
				{
					ResultSet rs = requete.executeQuery( "SELECT room.room_id FROM room where ((beginDate < NOW() AND endDate > NOW()) OR beginDate is NULL OR endDate is NULL) AND room_id NOT IN (" + list + ");" );
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
			}
			catch( RuntimeException e)
			{
				//Une erreur est survenue lors de la recherche de la prochaine salle
				objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
			    e.printStackTrace();
			}  
			
			//put in Controleur finded rooms
			//System.out.println(rooms + "NEW");	
			objControleurJeu.obtenirGestionnaireBD().fillRoomList(rooms);
			//System.out.println("end : " + System.currentTimeMillis());
	}// end methode detectNewRooms

	/**
	* Cette fonction permet d'initialiser une connexion avec le serveur MySQL
	* et de créer un objet requète
	*/
	private void connexionDB()
	{
		  GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		  String hote = config.obtenirString( "gestionnairebd.hote" );
		  String utilisateur = config.obtenirString( "gestionnairebd.utilisateur" );
		  String motDePasse = config.obtenirString( "gestionnairebd.mot-de-passe" );
		
		// établissement de la connexion avec la base de données
		try
		{
			connexion = DriverManager.getConnection( hote, utilisateur, motDePasse);
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la connexion ˆ la base de données
			objLogger.error(GestionnaireMessages.message("bd.erreur_connexion"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
		// Création de l'objet "requète"
		try
		{
			requete = connexion.createStatement();
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la création d'une requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
	}//end methode connexionDB
	
	
}
