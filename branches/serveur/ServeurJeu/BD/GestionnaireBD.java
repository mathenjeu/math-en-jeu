package ServeurJeu.BD;

import java.sql.*;
import org.apache.log4j.Logger;
import Enumerations.Categories;
import Enumerations.Visibilite;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Lang;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;
import java.text.SimpleDateFormat; 
import ServeurJeu.Configuration.GestionnaireMessages;
import java.util.Vector;

/**
 * @author Jean-François Brind'Amour
 */


public class GestionnaireBD 
{
	// Déclaration d'une référence vers le contrôleur de jeu
	private  ControleurJeu objControleurJeu;
	
    // Objet Connection nécessaire pour le contact avec le serveur MySQL
	private Connection connexion;
	
	// Objet Statement nécessaire pour envoyer une requète au serveur MySQL
	private  Statement requete;
	
	static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );
	
	//private static final String strCategoryLevel = "category_level";   // not used any more!!!!!!!!!!!!

	/**
	 * Constructeur de la classe GestionnaireBD qui permet de garder la 
	 * référence vers le contrôleur de jeu
	 */
	public GestionnaireBD(ControleurJeu controleur)
	{
	  	super();
		
		  GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		  // Garder la référence vers le contrôleur de jeu
		  objControleurJeu = controleur;
		
		  //Création du driver JDBC
		  try
		  {
			   String driver = config.obtenirString( "gestionnairebd.jdbc-driver" );
			   Class.forName( driver );
		  }
		  catch (Exception e)
		  {
			// Une erreur est survenue lors de l'instanciation du pilote
		    objLogger.error(GestionnaireMessages.message("bd.erreur_creer_pilote1"));
		    objLogger.error(GestionnaireMessages.message("bd.erreur_creer_pilote2"));
		    objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		  }
		
		   connexionDB();
	}
	
	
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
		
	}
	
	/**
	 * Cette fonction permet de chercher dans la BD si le joueur dont le nom
	 * d'utilisateur et le mot de passe sont passés en paramètres existe.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @param String motDePasse : Le mot de passe du joueur
	 * @return true  : si le joueur existe et que son mot de passe est correct
	 * 		   false : si le joueur n'existe pas ou que son mot de passe n'est 
	 * 				   pas correct
	 */
	public boolean joueurExiste(String nomUtilisateur, String motDePasse)
	{
		
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		String codeErreur = config.obtenirString( "gestionnairebd.code_erreur_inactivite" );
			
		int count=0;	//compteur du nombre d'essai de la requète

		//boucler la requète jusqu'à 5 fois si la connexion à MySQL
		//a été interrompu du à un manque d'activité de la connexion
		while(count<5)
		{
			try
			{
				if(count!=0)
				{
					connexionDB();
				}
				synchronized( requete )
				{
					ResultSet rs = requete.executeQuery("SELECT * FROM user WHERE username = '" + nomUtilisateur + "' AND password = '" + motDePasse + "';");
					return rs.next();
				}
			}
			catch (SQLException e)
			{
				//on vérifie l'état de l'exception 
				//si l'état est égal au codeErreur on peut réesayer la connexion
				if(e.getSQLState().equals(codeErreur))
				{
					count++;
				}
				else
				{
					// Une erreur est survenue lors de l'exécution de la requète
					objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
					objLogger.error(GestionnaireMessages.message("bd.trace"));
					objLogger.error( e.getMessage() );
					e.printStackTrace();
					return false;	
				}
			}
		}
		return false;
	}
	
	/**
	 * Cette fonction permet de chercher dans la BD le joueur et de remplir
	 * les champs restants du joueur.
	 * 
	 * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
	 * 								informations et les définir dans l'objet
	 */
	public void remplirInformationsJoueur(JoueurHumain joueur)
	{
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery("SELECT user.user_id,last_name,name,user_subject_level.*  FROM user,user_subject_level " +
						" WHERE username = '" + joueur.obtenirNomUtilisateur() + 
						"' AND user.user_id = user_subject_level.user_id;"); //
				if (rs.next())
				{
					
					String prenom = rs.getString("last_name");
					String nom = rs.getString("name");
					int cle = Integer.parseInt(rs.getString("user_id"));
					
					joueur.definirPrenom(prenom);
					joueur.definirNomFamille(nom);
					joueur.definirCleJoueur(cle);
					
					// on prend dans BD les niveaux scolaires du joueur en utilisant enum Categories
					Categories[] catValues = Categories.values();
					
					int[] cleNiveau = new int[catValues.length];
					for(int i = 0; i < catValues.length; i++)
					{
						cleNiveau[i] = Integer.parseInt(rs.getString(catValues[i].name()));
					}
					
					joueur.definirCleNiveau(cleNiveau);
				   
				}
				
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		
	}// fin méthode

 
	/** 
	 * La fonction rempli la boiteQuestions avec des questions que correspond
	 * a niveaux scolaires du joueur
     * This function fills a Question box with the questions of player's level 
     * for each category and player's lang 
     */
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, int[] niveau )
	{
		
        // Pour tenir compte de la langue
        int cleLang = 1;   
        Lang lang = boiteQuestions.obtenirLangue();
        String langue = lang.getLanguage();
        if (langue.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (langue.equalsIgnoreCase("en"))
        	cleLang = 2;
       
        // pour catégories des questions on utilise enum Categories
        
        Categories[] catValues = Categories.values();
        int[] catScolaires = new int[catValues.length];
        for(int i = 0; i < catValues.length; i++)
		{
			catScolaires[i] = catValues[i].getCode();
		}
              
     // pour chaque catégorie on prend le niveau scolaire du joueur
        for(int i = 0; i < catValues.length; i++)
		{
       	   String strRequeteSQL = "SELECT DISTINCT question_info.*,answer_type.tag,question_level.value,question_level.level_id " +
           "FROM question_info,answer_type_info,question_level,question,answer_type " +
           "WHERE  question_info.language_id = " + cleLang +
           " AND question_info.category_id = " + catScolaires[i] +
           " AND question_info.question_id = question_level.question_id " +
           " AND question_info.question_id = question.question_id " +
           " AND question_info.category_id = question.category_id " + 
           " AND answer_type_info.answer_type_id = question.answer_type_id " +
           " and question_info.is_valid = 1 " +
           " and question_info.question_flash_file is not NULL " +
           " and question_info.feedback_flash_file is not NULL "   +
           " and question_level.level_id =  " + niveau[i] + 
           " and question_level.value != 0 ";   
        
      
				   
		    remplirBoiteQuestions( boiteQuestions, strRequeteSQL );
		}//fin for
	}// fin méthode
	
    // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box.
	private void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String strRequeteSQL )
	{	
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				while(rs.next())
				{
					int codeQuestion = Integer.parseInt(rs.getString("question_id"));
					int categorie =  Integer.parseInt(rs.getString("category_id"));
					String typeQuestion = rs.getString( "tag" );
					String question = rs.getString( "question_flash_file" );
					String reponse = rs.getString("good_answer");
					String explication = rs.getString("feedback_flash_file");
					int difficulte = Integer.parseInt(rs.getString("value")); 
					
                    String URL = boiteQuestions.obtenirLangue().getURLQuestionsAnswers();
                   // System.out.println(URL+explication);
					boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL+question, reponse, URL+explication, categorie));
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		catch( RuntimeException e)
		{
			//Une erreur est survenue lors de la recherche de la prochaine question
			objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		}
	}// fin méthode
        
  
  /** This function queries the DB to find the player's musical preferences
   * and returns a Vector containing URLs of MP3s the player might like
   */
  public Vector obtenirListeURLsMusique(int cleJoueur)
	{
            Vector liste = new Vector();
            String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
            String strRequeteSQL = "SELECT music_file.filename FROM music_file,music_file_category,music_category,music_category_user WHERE ";
            strRequeteSQL       += "music_file.music_file_id = music_file_category.music_file_id AND ";
            strRequeteSQL       += "music_file_category.music_category_id = music_category.music_category_id AND ";
            strRequeteSQL       += "music_category.music_category_id = music_category_user.music_category_id AND ";
            strRequeteSQL       += "music_category_user.user_id = " + Integer.toString(cleJoueur);
            try
            {
                    synchronized( requete )
                    {
                            ResultSet rs = requete.executeQuery(strRequeteSQL);
                            while(rs.next())
                            {
                                liste.add(URLMusique + rs.getString("filename"));
                            }
                    }
            }
            catch (SQLException e)
            {
                    // Une erreur est survenue lors de l'exécution de la requète
                    objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error( e.getMessage() );
                e.printStackTrace();			
            }
            catch( RuntimeException e)
            {
                    // Ce n'est pas le bon message d'erreur mais ce n'est pas grave
                    objLogger.error(GestionnaireMessages.message("bd.error_music"));
                    objLogger.error(GestionnaireMessages.message("bd.trace"));
                    objLogger.error( e.getMessage() );
                e.printStackTrace();
            }
            return liste;
	}
	
  // This method updates a player's information in the DB
	public void mettreAJourJoueur( JoueurHumain joueur, int tempsTotal )
	{
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery("SELECT number_of_completed_game, best_score, total_time_played FROM user WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
				if (rs.next())
				{
					int partiesCompletes = rs.getInt( "number_of_completed_game" ) + 1;
					int meilleurPointage = rs.getInt( "best_score" );
					int pointageActuel = joueur.obtenirPartieCourante().obtenirPointage();
					if( meilleurPointage < pointageActuel )
					{
						meilleurPointage = pointageActuel;
					}
					
					int tempsPartie = tempsTotal + rs.getInt("total_time_played");
					
					//mise-a-jour
					int result = requete.executeUpdate( "UPDATE user SET number_of_completed_game =" + partiesCompletes + ",best_score =" + meilleurPointage + ",total_time_played =" + tempsPartie + " WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
		    objLogger.error(GestionnaireMessages.message("bd.trace"));
		    objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}
	
	/**
	 * Cette méthode permet de fermer la connexion de base de données qui 
	 * est ouverte.
	 */
	public void arreterGestionnaireBD()
	{
		try
		{
			connexion.close();
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la fermeture de la connexion
			objLogger.error(GestionnaireMessages.message("bd.erreur_fermeture_conn"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}
	
	/* Cette fonction permet d'ajouter les information sur une partie dans 
	 * la base de données dans la table partie. 
	 *
	 * Retour: la clé de partie qui servira pour la table partieJoueur
	 */
	public int ajouterInfosPartiePartieTerminee(Date dateDebut, int dureePartie)
	{

        SimpleDateFormat objFormatDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat objFormatHeure = new SimpleDateFormat("HH:mm:ss");
        
        String strDate = objFormatDate.format(dateDebut);
        String strHeure = objFormatHeure.format(dateDebut);

        // Création du SQL pour l'ajout
		String strSQL = "INSERT INTO game(date, hour, duration) VALUES ('" + 
		    strDate + "','" + strHeure + "'," + dureePartie + ")";

		try
		{
			
			synchronized(requete)
			{

				// Ajouter l'information pour cette partie
	            requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);
	            
	            // Aller chercher la clé de partie qu'on vient d'ajouter
	            ResultSet  rs = requete.getGeneratedKeys();
	            
	            // On retourne la clé de partie
	            rs.next();
	           	return Integer.parseInt(rs.getString("GENERATED_KEY"));
			}
        }
        catch (Exception e)
        {
        	System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
        }
        
        // Au cas où il y aurait erreur, on retourne -1
        return -1;
	}

	/* Cette fonction permet d'ajouter les informations sur une partie pour
	 * un joueur dans la table partieJoueur;
	 *
	 */
	public void ajouterInfosJoueurPartieTerminee(int clePartie, int cleJoueur, int pointage, boolean gagner)
	{
		int intGagner = 0;
		if (gagner == true)
		{
			intGagner = 1;
		}
		
		// Création du SQL pour l'ajout
		String strSQL = "INSERT INTO game_user(game_id, user_id, score, has_won) VALUES " +
		    "(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ");";
		
		try
		{
			
			synchronized(requete)
			{
				// Ajouter l'information pour ce joueur
	            requete.executeUpdate(strSQL);
			}
        }
        catch (Exception e)
            {
               System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
            }
	}

    /**
     * Méthode utilisé pour charger les salles avec les propriétes 
     * et les regles de la salle 
     * @param noeudLangue
     */
	public void fillsRooms(String language)
	{
		Regles objReglesSalle = new Regles();
		
		int roomId = 0;
		int langId = 0;
		String nom = "";
		String motDePasse = "";
		String createur = "";
		String gameType = "";
		
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT room_info.name, room.password, game_type.name,user.name, room.room_id, room_info.language_id " +
					" FROM room_info,room, game_type,user,language " +
					" WHERE room.game_type_id = game_type.game_type_id " +
					"  AND room.room_id = room_info.room_id " +
					"  AND user.user_id = room.user_id " +
					"  AND room_info.language_id = language.language_id " +
					"  AND language.short_name = '" + language + "';" );
				while(rs.next())
				{
					nom = rs.getString( "room_info.name" );
					System.out.println(nom);
					motDePasse = rs.getString( "password" );
					createur = rs.getString("user.name");
					gameType = rs.getString("game_type.name");
					roomId = rs.getInt("room.room_id");
					langId = rs.getInt("room_info.language_id");
						
                 }   
					chargerRegllesSalle(objReglesSalle, roomId, langId);
					Salle objSalle = new Salle(this, nom, createur, motDePasse, objReglesSalle, objControleurJeu, gameType);
					chargerMaxObjets(objSalle, roomId);
					objControleurJeu.ajouterNouvelleSalle(objSalle);
				
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
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
			
	}// fin méthode chargerSalle

   /**
    * 
    * @param roomId 
 * @param objSalle 
 * @param roomId
    */
	public void chargerMaxObjets(Salle objSalle, int roomId) {
		
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT rule.max_object_coin FROM rule, room" +
					" WHERE room.room_id = " + 1 +
					" AND rule.rule_id = room.rule_id ;" );
				if(rs.next())
				{
					
					int maxPiecesObjects = rs.getInt( "max_object_coin" );
					Salle.setMaxPossessionPieceEtObjet(maxPiecesObjects);					
                }
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		
	}

   /**
    * 
    * @param objReglesSalle
    * @param roomId
    * @param langId 
    */
	@SuppressWarnings("unchecked")
	private void chargerRegllesSalle(Regles objReglesSalle, int roomId, int langId) {
				
        try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT rule.* FROM rule, room" +
						" WHERE room.room_id = " + roomId +
				        " AND rule.rule_id = room.rule_id ;" );
				while(rs.next())
				{
					boolean chat = Boolean.parseBoolean(rs.getString( "chat" ));
					Float ratioTrous  = Float.parseFloat( rs.getString( "hole_ratio" ));
					Float ratioMagasins  = Float.parseFloat( rs.getString( "shop_ratio" ));
					Float ratioCasesSpeciales  = Float.parseFloat( rs.getString( "special_square_ratio" ));
					Float ratioPieces  = Float.parseFloat( rs.getString( "coin_ratio" ));
					Float ratioObjetsUtilisables  = Float.parseFloat( rs.getString( "object_ratio" ));
					int valeurPieceMax = rs.getInt( "max_coin_value" );
					int tempsMin = rs.getInt( "minimal_time" );
					int tempsMax = rs.getInt( "maximal_time" );
					int deplacementMax = rs.getInt( "max_movement" );
					int maxShopObjects = rs.getInt( "max_object_shop" );
					
					objReglesSalle.definirPermetChat( chat );
					objReglesSalle.definirRatioTrous( ratioTrous );
					objReglesSalle.definirRatioMagasins( ratioMagasins );
					objReglesSalle.definirRatioCasesSpeciales( ratioCasesSpeciales );
					objReglesSalle.definirRatioPieces( ratioPieces );
					objReglesSalle.definirRatioObjetsUtilisables(ratioObjetsUtilisables );
					objReglesSalle.definirValeurPieceMaximale( valeurPieceMax );
					objReglesSalle.definirTempsMinimal( tempsMin );
					objReglesSalle.definirTempsMaximal( tempsMax );
					objReglesSalle.definirDeplacementMaximal( deplacementMax );
					objReglesSalle.setIntMaxSaledObjects(maxShopObjects);
					
					//System.out.println("temp min: " + objReglesSalle.obtenirDeplacementMaximal());
									
                }
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		
		// charger autres regles
		TreeSet magasins = objReglesSalle.obtenirListeMagasinsPossibles();
		TreeSet casesCouleur = objReglesSalle.obtenirListeCasesCouleurPossibles();
		TreeSet casesSpeciale = objReglesSalle.obtenirListeCasesSpecialesPossibles();
		TreeSet objetsUtilisables = objReglesSalle.obtenirListeObjetsUtilisablesPossibles();
		
		this.chargerReglesMagasins(magasins, roomId, langId);
		this.chargerReglesCasesCouleur(casesCouleur, roomId);
		this.chargerReglesCasesSpeciale(casesSpeciale, roomId);
		this.chargerReglesObjetsUtilisables(objetsUtilisables, roomId, langId);
		
	}// fin méthode chargerReglesSalle
	
	
	/**
	 * Méthode utilisée pour charger la liste des objets utilisables
	 * @param objetsUtilisables 
	 * @param roomId
	 * @param langId 
	 */
	private void chargerReglesObjetsUtilisables(TreeSet objetsUtilisables, int roomId, int langId ) {
		try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT room_object.priority, object_info.name " +
  					" FROM room_object, object_info " +
  					" WHERE room_object.room_id = " + roomId +
  					" AND room_object.object_id = object_info.object_id " +
  					" AND object_info.language_id = " + langId +
  					";");
  				while(rst.next())
  				{
  					Integer tmp1 = rst.getInt( "priority" );
  			        String tmp2 = rst.getString( "name" );
  			        
  			        objetsUtilisables.add(new ReglesObjetUtilisable(tmp1, tmp2, Visibilite.Aleatoire));
  			      												
                  }
  			}
  		}
  		catch (SQLException e)
  		{
  			// Une erreur est survenue lors de l'exécution de la requète
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}// fin catch
		
    }// fin méthode


	/**
	 * Méthode utilisée pour charger la liste des cases spéciales 
	 * @param casesSpeciale 
	 * @param roomId
	 */
	private void chargerReglesCasesSpeciale(TreeSet casesSpeciale, int roomId) {
		try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT color_square_rule.priority, color_square_rule.type " +
  					" FROM color_square_rule " +
  					" WHERE color_square_rule.room_id = " + roomId +
  					 ";");
  				while(rst.next())
  				{
  					Integer tmp1 = rst.getInt( "priority" );
  			        Integer tmp2 = rst.getInt( "type" );
  			        
  			        casesSpeciale.add(new ReglesCaseSpeciale(tmp1, tmp2));
  					 														
                  }
  			}
  		}
  		catch (SQLException e)
  		{
  			// Une erreur est survenue lors de l'exécution de la requète
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}// fin catch
	
    }// fin méthode


	/**
     * Méthode utilisée pour charger la liste des cases couleur 
     * dans les Regles du partie
     * @param casesCouleur 
	 * @param roomId
     */
     private void chargerReglesCasesCouleur(TreeSet casesCouleur, int roomId) {
                   
        try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT color_square_rule.priority, color_square_rule.type " +
  					" FROM color_square_rule " +
  					" WHERE color_square_rule.room_id = " + roomId +
  					 ";");
  				while(rst.next())
  				{
  					Integer tmp1 = rst.getInt( "priority" );
  			        Integer tmp2 = rst.getInt( "type" );
  			        
  			        casesCouleur.add(new ReglesCaseCouleur(tmp1, tmp2));
  					 														
                  }
  			}
  		}
  		catch (SQLException e)
  		{
  			// Une erreur est survenue lors de l'exécution de la requète
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}// fin catch
	
     }// fin méthode


    /**
     * Méthode utilisée pour charger la liste des magasins dans les Regles du partie
     * @param magasins 
     * @param roomId
     * @param langId 
     */
     @SuppressWarnings("unchecked")
	private void chargerReglesMagasins(TreeSet magasins, int roomId, int langId) {
    	 	
         try
 		{
 			synchronized( requete )
 			{
 				ResultSet rst = requete.executeQuery( "SELECT room_shop.priority, shop_info.name " +
 					" FROM room_shop, shop_info " +
 					" WHERE shop_info.language_id = " + langId + 
 					" AND room_shop.shop_id = shop_info.shop_id " +
 					" AND  room_shop.room_id = " + roomId +
 					";");
 				while(rst.next())
 				{
 					
 			        String tmp2 = rst.getString("name");
 			        Integer tmp1 = rst.getInt( "priority" );
 			        magasins.add(new ReglesMagasin(tmp1, tmp2));
 					 														
                 }
 			}
 		}
 		catch (SQLException e)
 		{
 			// Une erreur est survenue lors de l'exécution de la requète
 			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
 			objLogger.error(GestionnaireMessages.message("bd.trace"));
 			objLogger.error( e.getMessage() );
 		    e.printStackTrace();			
 		}
     }// fin méthode

 
    /**
     * Methode that 
     * @param user's language 
     * @return URL of Questions-Answers on server
     */
	public String transmitUrl(String language) {
		String url = "";
		try
 		{
 			synchronized( requete )
 			{
 				ResultSet rs = requete.executeQuery( "SELECT language.url FROM language " +
 					" WHERE language.short_name = '" + language + "';");
 				while(rs.next())
 				{
 					url = rs.getString("url");
                }
 			}
 		}
 		catch (SQLException e)
 		{
 			// Une erreur est survenue lors de l'exécution de la requète
 			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
 			objLogger.error(GestionnaireMessages.message("bd.trace"));
 			objLogger.error( e.getMessage() );
 		    e.printStackTrace();			
 		}
		return url;
	}//end methode


	/**
	 * Used to control if room has a language
	 * @param salle
	 * @param language
	 * @param Boulean
	 * @return
	 */
	public Boolean roomLangControl(Salle salle, String language) {
		
		String answer = "";
		String nom = salle.obtenirNomSalle();
		try
 		{
 			synchronized( requete )
 			{
 				ResultSet rs = requete.executeQuery( "SELECT language.short_name FROM language, room_info " +
 					" WHERE  room_info.name = '" + nom + 
 					"' AND room_info.language_id = language.language_id ;");
 				if(rs.next())
 				{
 					answer = rs.getString("language.short_name");
                }
 			}
 		}
 		catch (SQLException e)
 		{
 			// Une erreur est survenue lors de l'exécution de la requète
 			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
 			objLogger.error(GestionnaireMessages.message("bd.trace"));
 			objLogger.error( e.getMessage() );
 		    e.printStackTrace();			
 		}
		
		return language.equalsIgnoreCase(answer);
	}//end methode


	public void fillShopObjects(String nomMagasin, ArrayList<String> listObjects) {
		
		
		 try
	 		{
	 			synchronized( requete )
	 			{
	 				ResultSet rst = requete.executeQuery( "SELECT object_info.name " +
	 					" FROM shop_info, shop_object, object_info " +
	 					" WHERE shop_info.name = '" + nomMagasin + 
	 					"' AND shop_info.shop_id = shop_object.shop_id " +
	 					" AND  shop_object.object_id = object_info.object_id ;");
	 				while(rst.next())
	 				{
	 					
	 			        String object = rst.getString("name");
	 			       
	 			        listObjects.add(object);
	 			    	 														
	                }
	 			}
	 		}
	 		catch (SQLException e)
	 		{
	 			// Une erreur est survenue lors de l'exécution de la requète
	 			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
	 			objLogger.error(GestionnaireMessages.message("bd.trace"));
	 			objLogger.error( e.getMessage() );
	 		    e.printStackTrace();			
	 		}
	}// end methode


		
	
}// end class
