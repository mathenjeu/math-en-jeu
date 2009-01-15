package ServeurJeu.BD;

import java.sql.*;
import org.apache.log4j.Logger;

import ClassesUtilitaires.UtilitaireNombres;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Langue;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import java.util.Date;
import java.text.SimpleDateFormat; 
import ServeurJeu.Configuration.GestionnaireMessages;
import java.util.Vector;

/**
 * @author Jean-François Brind'Amour
 */


public class GestionnaireBD 
{
	// Déclaration d'une référence vers le contrôleur de jeu
	private ControleurJeu objControleurJeu;
	
    // Objet Connection nécessaire pour le contact avec le serveur MySQL
	private Connection connexion;
	
	// Objet Statement nécessaire pour envoyer une requète au serveur MySQL
	private Statement requete;
	
	static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );
	
	//private static final String strCategoryLevel = "category_level";

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
				ResultSet rs = requete.executeQuery("SELECT user_id, last_name, name  FROM user WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
				if (rs.next())
				{
					
					String prenom = rs.getString("last_name");
					String nom = rs.getString("name");
					int cle = Integer.parseInt(rs.getString("user_id"));
					
					joueur.definirPrenom(prenom);
					joueur.definirNomFamille(nom);
					joueur.definirCleJoueur(cle);
					
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
		
		/*//////////////// mon code  /////////////////////
		try
		{
			synchronized( requete )
			{
				ResultSet rsl = requete.executeQuery("SELECT category_level*  FROM user_subject_level WHERE user_id = '" + joueur.obtenirCleJoueur() + "';");
				if (rsl.next())
				{
														
					String cleNiveau = rsl.getString("category_level");
					//joueur.definirCleNiveau(cleNiveau);
				          
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
		}*/
		
		/// simulaton niveaux du joueur
		int[] cleNiveau = {2,3,4,4,5,7,8,9,6,7,3,12,11,3,5,6,7,8,9,6,5,4,3,2,4,5,6,7,8,9,11,14,15,12};
		joueur.definirCleNiveau(cleNiveau);
	}// fin méthode

 
	/*// This method fills a Question box with only the player's level
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, int[] niveau )
	{
                String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
		String strRequeteSQL = "SELECT " + nomTable + ".*,answer_type.tag FROM " + nomTable +
                        ",answer_type WHERE answer_type.answer_type_id = " + nomTable + ".tag and " + nomTable + ".valide = 1 " +
			"and question_flash_file is not NULL and feedback_flash_file is not NULL and ";
		
		         	    
		strRequeteSQL += strValeurGroupeAge + niveau + " > 0";
		
		remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
	}*/
	
    /** This function fills a Question box with the player's level, a specified difficulty and a question category
     * 
     */
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, int[] niveau )
	{
		
        // Noter qu'on ne tient plus compte de la catégorie!!
        String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
        int cleLang = 1;
        // pour catégories des questions
        int[] tab = {11,12,13,14,21,22,23,24,31,32,33,34,35,36,37,38,41,42,43,44,45,46,47,48,49,51,52,53,54,61,62,63,64,65};
        
        // il faux choisir aussi la langue du question
        Langue lang = boiteQuestions.obtenirLangue();
        String langue = lang.obtenirLangue();
        if (langue.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (langue.equalsIgnoreCase("en"))
        	cleLang = 2;
        
        
        
     /*   // pour chaque catégorie on prend le niveau scolaire du joueur
        for(int i = 0; i < tab.length; i++){
       	   String strRequeteSQL = "SELECT question_info.*,answer_type_info.name,question_level.value,question_level.level_id " +
           "FROM question_info,answer_type_info,question_level,question " +
           "WHERE  question_info.language_id = " + cleLang +
           " AND question_info.category_id = " + tab[i]  +
           " AND question_info.question_id = question_level.question_id " +
           " AND  question_info.question_id = question.question_id " +
           "AND question_info.category_id = question.category_id " + 
           " AND answer_type_info.answer_type_id = question.answer_type_id " +
           " and question_info.is_valid = 1 " +
           " and question_info.question_flash_file is not NULL " +
           " and question_info.feedback_flash_file is not NULL "   +
           " and question_level.level_id = " + niveau[i]  +
           " and question_level.value != 0";   */
        
        // ine version simulation !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //  on ne prend pas  les niveaux scolaire du joueur
        
       	   String strRequeteSQL = "SELECT DISTINCT question_info.*,answer_type.tag,question_level.value " +
           " FROM question_info,answer_type,question_level,question " +
           " WHERE question_info.question_id = question.question_id " +
           " AND answer_type.answer_type_id = question.answer_type_id " +
           " AND question_info.language_id = " + cleLang +
           " and question_info.is_valid = 1 " +
           " and question_info.question_flash_file is not NULL " +
           " and question_info.feedback_flash_file is not NULL"  +
           " and question_level.value = 1 AND question_info.question_id NOT IN (6135,6136,6137,6138,6149,6150)";
           
				   
		    remplirBoiteQuestions( boiteQuestions, strRequeteSQL );
        //}//fin for
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
					int codeQuestion = rs.getInt("question_id");
					//System.out.println(codeQuestion);
					int categorie = UtilitaireNombres.genererNbAleatoire(7); // simulation !!!!!!!!! rs.getInt("category_id");
					//System.out.println(categorie);
					String typeQuestion = rs.getString( "tag" );
					String question = rs.getString( "question_flash_file" );
					String reponse = rs.getString("good_answer");
					String explication = rs.getString("feedback_flash_file");
					int difficulte = UtilitaireNombres.genererNbAleatoire(6); // simulation !!!!!!!!! rs.getInt("value");
					
                    String URL = boiteQuestions.obtenirLangue().obtenirURLQuestionsReponses();
                    System.out.println(URL+explication);
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
}
