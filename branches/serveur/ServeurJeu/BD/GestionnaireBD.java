package ServeurJeu.BD;

import java.sql.*;

import org.apache.log4j.Logger;
import Enumerations.Visibilite;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Language;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.text.SimpleDateFormat; 
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-François Brind'Amour
 * 
 * last changes Oloieri Lilian 11.05.2010
 */


public class GestionnaireBD 
{
	// Déclaration d'une référence vers le contrôleur de jeu
	private final ControleurJeu objControleurJeu;
	
    // Objet Connection nécessaire pour le contact avec le serveur MySQL
	private Connection connexion;
	
	// Objet Statement nécessaire pour envoyer une requète au serveur MySQL
	private  Statement requete;
	
	static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );
	
	
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
	public void connexionDB()
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
	 * Cette fonction permet de chercher dans la BD si le joueur dont le nom    ***
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
	 * This methode fill columns with the actual date for the player with the blocked account
	 */
	public void controlPlayerAccount()
	{
		
	    //  SQL for update
		String strSQL = "UPDATE user SET last_access_date = CURDATE(), last_access_time = CURTIME() where last_access_date LIKE '1111-01-01' OR last_access_time LIKE '55:55:55';"; 
		
		try
		{
			
			synchronized(requete)
			{
				requete.executeUpdate(strSQL);
			}
        }
        catch (Exception e)
        {
               System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_user_control_account") + e.getMessage());
        }
        
		
	}//end methode
	
	/**
	 * Cette fonction permet de chercher dans la BD le joueur et de remplir  ***
	 * les champs restants du joueur.
	 * 
	 * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
	 * 								informations et les définir dans l'objet
	 */
	public void remplirInformationsJoueur(JoueurHumain joueur)
	{
		int cle = 0;
		int role = 0;
		int niveau = 1; // default level is 1 - generic level
		
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery("SELECT user.user_id,last_name,name,role_id, level_id  FROM user " +
						" WHERE username = '" + joueur.obtenirNomUtilisateur() + 
						"';"); //
				if (rs.next())
				{
				
					String prenom = rs.getString("last_name");
					String nom = rs.getString("name");
					cle = rs.getInt("user_id");
					role = rs.getInt("role_id");
					niveau = rs.getInt("level_id");
						
					joueur.definirPrenom(prenom);
					joueur.definirNomFamille(nom);
					joueur.definirCleJoueur(cle);
					joueur.setRole(role);
					joueur.definirCleNiveau(niveau);
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_remplir_info_joueur"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		
		
		fillConnectedUser(cle);
		
	}//end methode
	

	/**
	 * Used to fill user's  level's from DB
	 * @param salle 
	 * 
	 * @param JoueurHumain player
	 
	public void fillUserLevels(JoueurHumain player, Salle salle)
	{
		//System.out.println("start level: " + System.currentTimeMillis());
		int cle = player.obtenirCleJoueur();
		int niveau = 0;
		try
		{
			
			
				synchronized(requete)
				{
					// we take level only for categorie 0 - the all level is the same now
					ResultSet rs = requete.executeQuery("SELECT user_subject_level.level  FROM user_subject_level " +
					" WHERE  user_subject_level.user_id = " + cle + " AND user_subject_level.category_id = 0;");
					if(rs.next())
					{
						niveau = rs.getInt("level");
						//System.out.println("level : " + cleNiveau[i] + " " + i);
	    			}
					
				}

			
		}
		catch (Exception e)
		{
			System.out.println(GestionnaireMessages.message("bd.erreur_adding_info_subject_user") + e.getMessage());
		}
				
		player.definirCleNiveau(niveau);
		//System.out.println("end level : " + System.currentTimeMillis());		
	}//end methode*/
	

	
	/**
	 * this fonction fill the fields in DB (user.last_access_time,lasr_access_time)
	 * with incorrect information to indicate that user is connected
	 *
	 */
	public void fillConnectedUser(int userId)
	{

 				
		//  SQL for update
		String strSQL = "UPDATE user SET last_access_date = '1111-01-01', last_access_time = '55:55:55' where user_id = " + userId + ";"; 
		
		try
		{
			
			synchronized(requete)
			{
				requete.executeUpdate(strSQL);
			}
        }
        catch (Exception e)
        {
               System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_user_connected") + e.getMessage());
        }
        
        
	}// end methode 
	
	/**
	 * this fonction fill the fields in DB (user.last_access_time,lasr_access_time)
	 * with the current date at the end of game 
	 */
	public void fillEndDate(int userId)
	{

     		
		//  SQL for update
		String strSQL = "UPDATE user SET last_access_date = CURDATE(), last_access_time = CURTIME() where user_id = " + userId + ";"; 
		
		try
		{
			
			synchronized(requete)
			{
				requete.executeUpdate(strSQL);
			}
        }
        catch (Exception e)
        {
               System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_user_game_endtime") + e.getMessage());
        }
        
        
	}// end methode 
        
   
	/** 
	 * La fonction rempli la boiteQuestions avec des questions que correspond
	 * a niveaux scolaires du joueur
     * This function fills a Question box with the questions of player's level 
     * for each category and player's lang 
     */
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, JoueurHumain player)
	{
		//System.out.println("start boite: " + System.currentTimeMillis());
        // Pour tenir compte de la langue
        int cleLang = 1;   
        Language language = boiteQuestions.obtenirLangue();
        String langue = language.getLanguage();
        if (langue.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (langue.equalsIgnoreCase("en"))
        	cleLang = 2;
       
        // pour catégories des questions on utilise room's categories 
        ArrayList<Integer> cat = player.obtenirSalleCourante().getCategories();
		ListIterator<Integer> it = cat.listIterator();		
        
		int niveau = player.obtenirCleNiveau();

		// pour chaque catégorie on prend le niveau scolaire du joueur
		StringBuffer categorie = new StringBuffer();
		
		for(int i = 0; i < cat.size(); i++)
		{
			categorie.append(",");
			categorie.append(it.next());
			
		}//fin for
				
		categorie.deleteCharAt(0);
		categorie.toString();
		//categorie = " 10,11,12,13,14 ";
		
		String strRequeteSQL = "SELECT question.answer_type_id, answer.is_right,question.question_id," +
				" question.category_id, question_info.question_flash_file, question_info.feedback_flash_file, question_level.value" +
		" FROM question_info, question_level, question, answer " +
		" WHERE  question.question_id = question_level.question_id " +
		" AND question.question_id = question_info.question_id " +
		" AND question.question_id = answer.question_id " +
		" AND question_info.language_id = " + cleLang +
		" and question_level.level_id = " + niveau + 
		" AND question.category_id IN (" + categorie + ") " +
		" AND question.answer_type_id IN (1,4,5) " +
		" AND question_info.is_valid = 1 " +
		" and question_level.value > 0 " +
		" and question_info.question_flash_file is not NULL " +
		" and question_info.feedback_flash_file is not NULL ";

		remplirBoiteQuestionsMC( boiteQuestions, strRequeteSQL); 

		String strRequeteSQL_SA = "SELECT DISTINCT q.category_id, a.answer_latex, qi.question_id, qi.question_flash_file, qi.feedback_flash_file, ql.value " +
		"FROM question q, question_info qi, question_level ql, answer_info a " +
		"where  q.question_id = ql.question_id " +
		" AND q.question_id = qi.question_id " +
		" AND q.question_id = a.question_id " +
		" AND q.category_id IN (" + categorie + ") and q.answer_type_id = 3 " +
		" AND qi.language_id = " + cleLang +
		" and ql.level_id = " + niveau + 
		" and ql.value > 0 " +
		" and qi.is_valid = 1 " +
		" and qi.question_flash_file is not NULL" +
		" and qi.feedback_flash_file is not NULL";

		remplirBoiteQuestionsSA( boiteQuestions, strRequeteSQL_SA);

		String strRequeteSQL_TF = "SELECT DISTINCT q.category_id,a.is_right,qi.question_id, qi.question_flash_file, qi.feedback_flash_file, ql.value " +
		" FROM question q, question_info qi, question_level ql, answer a " +
		"where  q.question_id = ql.question_id " +
		" AND q.question_id = qi.question_id " +
		" AND q.question_id = a.question_id " +
		" AND q.category_id IN (" + categorie + ") and q.answer_type_id = 2 " +
		" AND qi.language_id = " + cleLang +
		" and ql.level_id = " + niveau + 
		" and ql.value > 0 " +
		" and qi.is_valid = 1 " +
		" and qi.question_flash_file is not NULL" +
		" and qi.feedback_flash_file is not NULL";

		remplirBoiteQuestionsTF( boiteQuestions, strRequeteSQL_TF);

        //System.out.println("end boite: " + System.currentTimeMillis());
	}// fin méthode
	
    // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type MULTIPLE_CHOICE.
	private void remplirBoiteQuestionsMC( BoiteQuestions boiteQuestions, String strRequeteSQL)
	{	
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				rs.setFetchSize(5);
				//int countQuestionId = 0;
				int codeQuestionTemp = 0;
				int countReponse = 0;
				while(rs.next())
				{
					
					int codeQuestion = rs.getInt("question_id");
					if (codeQuestionTemp != codeQuestion )
					{
						//countQuestionId = 0;
						countReponse = 0;
					}
					int condition = rs.getInt("is_right");
					//countQuestionId++;
					countReponse++;
					if(condition == 1)
					{
						int typeQuestion = rs.getInt( "answer_type_id" );
						int categorie = rs.getInt( "category_id" );
						String question = rs.getString( "question_flash_file" );
						String explication = rs.getString("feedback_flash_file");
						int difficulte = rs.getInt("value");
						String reponse = "" + countReponse;

						//System.out.println("MC : question " + typeQuestion + " " + codeQuestion + " " + difficulte);
						String URL = boiteQuestions.obtenirLangue().getURLQuestionsAnswers();
						// System.out.println(URL+explication);
						boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL+question, reponse, URL+explication, categorie));
					}
					codeQuestionTemp = codeQuestion;
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
			objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question_MC"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		}
	}// fin méthode
        
	
	 // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type SHORT_ANSWER.
	private void remplirBoiteQuestionsSA( BoiteQuestions boiteQuestions, String strRequeteSQL)
	{	
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				rs.setFetchSize(5);
				while(rs.next())
				{
					int codeQuestion = rs.getInt("question_id");
					int categorie = rs.getInt( "category_id" );
					//int categorie =  Integer.parseInt(rs.getString("category_id"));
					int typeQuestion = 3;//rs.getString( "tag" );
					String question = rs.getString( "question_flash_file" );
					String reponse = rs.getString("answer_latex");
					String explication = rs.getString("feedback_flash_file");
					int difficulte = rs.getInt("value"); 
					
					//System.out.println("SA : question " + codeQuestion + " " + reponse + " " + difficulte);
					
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
			objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question_SA"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		}
	}// fin méthode
	
	// This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type TRUE_OR_FALSE.
	private void remplirBoiteQuestionsTF( BoiteQuestions boiteQuestions, String strRequeteSQL)
	{	
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				rs.setFetchSize(5);
				while(rs.next())
				{
					int codeQuestion = rs.getInt("question_id");
					int categorie = rs.getInt( "category_id" );
					//int categorie =  Integer.parseInt(rs.getString("category_id"));
					int typeQuestion = 2;   //rs.getString( "tag" );
					String question = rs.getString( "question_flash_file" );
					String reponse = rs.getString("is_right");
					String explication = rs.getString("feedback_flash_file");
					int difficulte = rs.getInt("value"); 
					
					//System.out.println("TF : question " + codeQuestion + " " + reponse + " " + difficulte);
					
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
			objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question_TF"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		}
	}// fin méthode

  /** This function queries the DB to find the player's musical preferences  ***
   * and returns a Vector containing URLs of MP3s the player might like
   */
  public ArrayList<Object> obtenirListeURLsMusique(JoueurHumain player)
	{
            ArrayList<Object> liste = new ArrayList<Object>();
          
            String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
            String strRequeteSQL = "SELECT music_file.filename FROM music_file  WHERE  music_file.level_id = ";
            // we use levels[0] - because all levels has the same value
            strRequeteSQL       += "(Select user.level_id from user where user_id = ";
            strRequeteSQL       += player.obtenirCleJoueur();
            strRequeteSQL       += ");";
            
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
	
  // This method updates a player's information in the DB  ***
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
					 requete.executeUpdate( "UPDATE user SET number_of_completed_game =" + partiesCompletes + ",best_score =" + meilleurPointage + ",total_time_played =" + tempsPartie + " WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
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
	
	/* Cette fonction permet d'ajouter les information sur une partie dans ***
	 * la base de données dans la table partie. 
	 *
	 * Retour: la clé de partie qui servira pour la table partieJoueur
	 */
	public int ajouterInfosPartieTerminee(Date dateDebut, int dureePartie)
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
	           	return rs.getInt("GENERATED_KEY");
			}
        }
        catch (Exception e)
        {
        	System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
        }
        
        // Au cas où il y aurait erreur, on retourne -1
        return -1;
	}

	/**
	 * Cette fonction permet d'ajouter les informations sur une partie pour  ***
	 * un joueur dans la table partieJoueur;
	 *
	 */
	public void ajouterInfosJoueurPartieTerminee(int clePartie, JoueurHumain joueur, boolean gagner)
	{
		int intGagner = 0;
		if (gagner == true)
		{
			intGagner = 1;
		}
		
		int cleJoueur = joueur.obtenirCleJoueur();
		int pointage = joueur.obtenirPartieCourante().obtenirPointage();
		int room_id = 0;
		String statistics = "";
				
		statistics = statistics + "/-/" + joueur.obtenirProtocoleJoueur().getQuestionsAnswers();
		
		
		try
		{
			
			synchronized(requete)
			{
				//String langue = joueur.obtenirProtocoleJoueur().langue;
				room_id = joueur.obtenirSalleCourante().getRoomID(); 
								
				// Création du SQL pour l'ajout
				String strSQL = "INSERT INTO game_user(game_id, user_id, score, has_won, questions_answers, room_id) VALUES " +
				    "(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ",'" + statistics + "'," + room_id + ");"; 
				// Ajouter l'information pour ce joueur
	            requete.executeUpdate(strSQL);
			}
        }
        catch (Exception e)
        {
            System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update") + e.getMessage());
        }
        
        fillEndDate(cleJoueur);
        
	}// end methode
	
	/**
	 * Methode used to update in DB the player's money ****
	 * @param cleJoueur
	 * @param newMoney
	 */
	public void setNewPlayersMoney(int cleJoueur, int newMoney) {
		// Update the money in player's account
		String strMoney = " UPDATE user SET money = " + newMoney + " WHERE user_id = " + cleJoueur + ";"; 
		
		try
		{
			
			synchronized(requete)
			{
				// Ajouter l'information pour ce joueur
	            requete.executeUpdate(strMoney);
			}
        }
        catch (Exception e)
            {
               System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos_update_money") + e.getMessage());
            }
		
	}//end methode
	
	/**
	 * Methode used to charge to player's money from DB for current game ***
	 * option can be disabled with
	 * @param userId 
	 */
	public int getPlayersMoney(int userId) {
		int money = 0;
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery("SELECT user.money  FROM user WHERE user_id = " + userId + ";"); 
				if (rs.next())
				{
					money = rs.getInt("money");
    			}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_get_money"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		return money;
	}// end methode


    /**
     * Méthode utilisé pour charger les salles avec les propriétes  ***
     * et les regles de la salle 
     * @param noeudLangue
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
	public void fillsRooms()  
	{
		
		ArrayList<Integer> rooms = new ArrayList<Integer>();
			
		//find all rooms  and fill in ArrayList
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT room.room_id FROM room where (beginDate < NOW() AND endDate > NOW()) OR beginDate is NULL OR endDate is NULL;" );
				while(rs.next())
				{
					int roomId = rs.getInt("room.room_id");
									
					rooms.add(roomId);
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
			//Une erreur est survenue lors de la recherche de la prochaine salle
			objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		}
		
		// create the rooms by this list of rooms ID  and put them in the ControleurJeu
		fillRoomList(rooms);
		
	}//end methode fillsRooms
	
	/**
	 * Methode satellite for fillsRooms()
	 * @param rooms
	 */
	public void fillRoomList(ArrayList<Integer> rooms) 
	{
		String nom = "";
		String motDePasse = "";
		String createur = "";
		Date beginDate = null;
	    Date endDate = null;
	    int masterTime = 0;
	    String categoriesString = "";
	    int role = 2;
	    String type = "General";
		    
		//now fill all the rooms with properties and add this rooms to the game
		
		try
		{
			for (int room : rooms){
				synchronized( requete )
				{
					ResultSet rs = requete.executeQuery( "SELECT room.password, user.username, user.role_id, beginDate, endDate, masterTime, categories " +
							" FROM room_info, room, user, game_type " +
							" WHERE room.room_id = " + room +  
							" AND room.room_id = room_info.room_id " +
							" AND user.user_id = room.user_id ;" );
					if(rs.next())
					{
						
						categoriesString = rs.getString("categories");
						motDePasse = rs.getString("password");
						createur = rs.getString("user.username");
						beginDate = rs.getTimestamp("beginDate");
						endDate = rs.getTimestamp("endDate");
						masterTime = rs.getInt("masterTime");
						role = rs.getInt("user.role_id");
												
						String roomDescription = fillRoomDescription(room);
						nom = fillRoomName(room);
						
						if(role == 3) type = "profsType";
						else type = "General";
											
						Salle objSalle = new Salle(nom, createur, motDePasse, objControleurJeu, room, beginDate, endDate, masterTime, type);
						//System.out.println("Test : " + type);
						objSalle.setRoomDescription(roomDescription);
						objSalle.setCategories(categoriesString);
						
						ArrayList<String> types = new ArrayList<String>();
						this.getAllowedRoomsGameTypes(types, room);
						objSalle.setRoomAllowedTypes(types);
												
						objControleurJeu.ajouterNouvelleSalle(objSalle);
											
						objControleurJeu.preparerEvenementNouvelleSalle(nom, objSalle.protegeeParMotDePasse(), createur,  
								           roomDescription, masterTime, room, objSalle.getRoomAllowedTypes().toString());
						
					}   

				}
			  }//end for
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

		
				
	}// fin méthode fillRoomList
	
	
	/**
	 * Méthode utilisée pour charger les types du jeux admissibles  
	 * @param types ***
	 * @param objetsUtilisables 
	 * @param roomId
	 * @param langId 
	 */
	private void getAllowedRoomsGameTypes(ArrayList<String> types, int roomId) {
		try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT game_type.name " +
  					" FROM room_game_types, game_type WHERE room_game_types.room_id = " + roomId + 
  				" And room_game_types.game_type_id = game_type.game_type_id;");
  				while(rst.next())
  				{
  					String tmp1 = rst.getString("name");
  					
  					types.add(tmp1);

  				}
  			}
  		}
  		catch (SQLException e)
  		{
  			// Une erreur est survenue lors de l'exécution de la requète
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_game_types"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}// fin catch
		
    }// fin méthode


 /**
  * Methode to fill the room name in both languages  ***
  * @param room
  * @return
  */
   private String fillRoomName(int room) {
	   String name = "";
	   try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT concat(r.name, ' / ',p.name) as room_bilingue " +
                " FROM (Select room_id, name from room_info where language_id = 1) as r, " +
                "(select room_id, name from room_info where language_id = 2) as p " +
                " where r.room_id = p.room_id AND r.room_id = " + room +
                " UNION " +
                " SELECT  name from room_info " +
                " where room_id not in (Select room_id from room_info where language_id = 2) " +
                " AND room_id = " + room + 
                " UNION " +
                " SELECT  name from room_info " +
                " where room_id not in (Select room_id from room_info where language_id = 1) " +
                " AND room_id = " + room + ";");
				
				if(rs.next())
				{
					name = rs.getString("room_bilingue");
                }
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_room_name"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		
		return name;
	}// end methode

   /**
    * Methode to fill the room description in both languages
    * @param room
    * @return
    */
     private String fillRoomDescription(int room) {
  	   String name = "";
  	   try
  		{
  			synchronized( requete )
  			{
  				ResultSet rs = requete.executeQuery( "SELECT concat(r.description, ' / ',p.description) as room_bilingue " +
                  " FROM (Select room_id, description from room_info where language_id = 1) as r, " +
                  "(select room_id, description from room_info where language_id = 2) as p " +
                  " where r.room_id = p.room_id AND r.room_id = " + room +
                  " UNION " +
                  " SELECT  description from room_info " +
                  " where room_id not in (Select room_id from room_info where language_id = 2) " +
                  " AND room_id = " + room + 
                  " UNION " +
                  " SELECT  description from room_info " +
                  " where room_id not in (Select room_id from room_info where language_id = 1) " +
                  " AND room_id = " + room + ";");
  				
  				if(rs.next())
  				{
  					name = rs.getString("room_bilingue");
                }
  			}
  		}
  		catch (SQLException e)
  		{
  			// Une erreur est survenue lors de l'exécution de la requète
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_room_description"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}
  		
  		return name;
  	}// end methode

   /**                                  
    * @param objReglesSalle
    */
	//@SuppressWarnings("unchecked")
	public void chargerRegllesTable(Regles objReglesTable, String gameType, int roomId) {
			
		int gameTypeID = 1; // default type - mathEnJeu
		if(gameType.equals("Tournament"))gameTypeID = 2;
		if(gameType.equals("Course"))gameTypeID = 3;
        try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT rule.*  FROM rule WHERE rule.rule_id = " + gameTypeID + ";" );
				while(rs.next())
				{
					boolean shownumber = rs.getBoolean("show_nb_questions");
					boolean chat = rs.getBoolean( "chat" );
					boolean money = rs.getBoolean( "money_permit" );
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
					int maxNbPlayers = rs.getInt( "maxNbPlayers" );
					//int maxNbObjectsAndMoney = rs.getInt( "max_object_coin" );
					int nbTracks = rs.getInt( "nbTracks" );
					int nbVirtualPlayers = rs.getInt("nbVirtualPlayers");
					
								
					//objReglesSalle.setMaxNbObjectsAndMoney(maxNbObjectsAndMoney);
					objReglesTable.setMaxNbPlayers(maxNbPlayers);
					objReglesTable.setShowNumber(shownumber);
					objReglesTable.setBolMoneyPermit(money);
					objReglesTable.definirPermetChat(chat);
					objReglesTable.definirRatioTrous( ratioTrous );
					objReglesTable.definirRatioMagasins( ratioMagasins );
					objReglesTable.definirRatioCasesSpeciales( ratioCasesSpeciales );
					objReglesTable.definirRatioPieces( ratioPieces );
					objReglesTable.definirRatioObjetsUtilisables(ratioObjetsUtilisables );
					objReglesTable.definirValeurPieceMaximale( valeurPieceMax );
					objReglesTable.definirTempsMinimal( tempsMin );
					objReglesTable.definirTempsMaximal( tempsMax );
					objReglesTable.definirDeplacementMaximal( deplacementMax );
					objReglesTable.setIntMaxSaledObjects(maxShopObjects);
					objReglesTable.setNbTracks(nbTracks);
					objReglesTable.setNbVirtualPlayers(nbVirtualPlayers);
											
                }
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_rules_charging"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
		
		// charger autres regles
		TreeSet magasins = objReglesTable.obtenirListeMagasinsPossibles();
		//TreeSet casesCouleur = objReglesTable.obtenirListeCasesCouleurPossibles();
		//TreeSet casesSpeciale = objReglesTable.obtenirListeCasesSpecialesPossibles();
		TreeSet<ReglesObjetUtilisable> objetsUtilisables = objReglesTable.obtenirListeObjetsUtilisablesPossibles();
		
		this.chargerReglesMagasins(magasins, roomId);
		//this.chargerReglesCasesCouleur(casesCouleur, roomId);
		//this.chargerReglesCasesSpeciale(casesSpeciale, roomId);
		this.chargerReglesObjetsUtilisables(objetsUtilisables, roomId);
		
	}// fin méthode chargerReglesSalle
	
	
	/**
	 * Méthode utilisée pour charger la liste des objets utilisables  ***
	 * @param objetsUtilisables 
	 * @param roomId
	 * @param langId 
	 */
	private void chargerReglesObjetsUtilisables(TreeSet<ReglesObjetUtilisable> objetsUtilisables, int roomId) {
		try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT room_object.priority, object_info.name " +
  					" FROM room_object, object_info " +
  					" WHERE room_object.room_id = " + roomId +
  					" AND room_object.object_id = object_info.object_id " +
  					" AND object_info.language_id = " + 1 +
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
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_objects_rules_"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}// fin catch
		
    }// fin méthode


	/**
	 * Méthode utilisée pour charger la liste des cases spéciales    ***
	 * @param casesSpeciale 
	 * @param roomId
	 */
	private void chargerReglesCasesSpeciale(TreeSet<ReglesCaseSpeciale> casesSpeciale, int roomId) {
		try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT special_square_rule.priority, special_square_rule.type " +
  					" FROM special_square_rule " +
  					" WHERE special_square_rule.room_id = " + roomId +
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
     * Méthode utilisée pour charger la liste des magasins dans les Regles du partie ***
     * @param magasins 
     * @param roomId
     */
    private void chargerReglesMagasins(TreeSet<ReglesMagasin> magasins, int roomId) {
    	 	
         try
 		{
 			synchronized( requete )
 			{
 				ResultSet rst = requete.executeQuery( "SELECT room_shop.priority, shop_info.name " +
 					" FROM room_shop, shop_info " +
 					" WHERE shop_info.language_id = " + 1 + 
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
     * Methode used to charge  
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
	 * Used to control if room has a language  ***
	 * @param salle
	 * @param language
	 * @param Boulean
	 * @return
	 */
	public Boolean roomLangControl(Salle salle, String language) {
		Boolean existe = false;
		
		// Pour tenir compte de la langue
        int cleLang = 1;   
        
        if (language.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (language.equalsIgnoreCase("en"))
        	cleLang = 2;
		int key = salle.getRoomID();
		try
 		{
 			synchronized( requete )
 			{
 				ResultSet rs = requete.executeQuery( "SELECT name FROM room_info " +
 					" WHERE  room_info.room_id = '" + key + 
 					"' AND room_info.language_id = '" + cleLang + "' ;");
 				if(rs.next())
 				{
 					String name = rs.getString("name");
 					existe = true;
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
		
		return existe;
	}//end methode


	/**
	 * Methode used to fill store with objects to sell   ***
	 * @param nomMagasin
	 * @param listObjects
	 */
	 
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


	
	/**
	 * Return full name (in both languages) by name  ***
	 * @param nomSalle
	 * @return
	 */
	public String getFullRoomName(String nomSalle) {
		
		String completName = "";
		
		try
 		{
 			synchronized( requete )
 			{
 				ResultSet rs = requete.executeQuery ("SELECT concat(r.name, ' / ',p.name) as room_bilingue " +
                " FROM (Select room_id, name from room_info where language_id = 1) as r, " +
                "(select room_id, name from room_info where language_id = 2) as p " +
                " where r.room_id = p.room_id AND r.room_id = (SELECT room_id FROM room_info r where name = '" + nomSalle + "')" + 
                " UNION " +
                " SELECT  name from room_info " +
                " where room_id not in (Select room_id from room_info where language_id = 2) " +
                " AND room_id = (SELECT room_id FROM room_info r where name = '" + nomSalle + "')" + 
                " UNION " +
                " SELECT  name from room_info " +
                " where room_id not in (Select room_id from room_info where language_id = 1) " +
                " AND room_id = (SELECT room_id FROM room_info r where name = '" + nomSalle + "');");
				
 				if(rs.next())
 				{
 					completName = rs.getString("room_bilingue");
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
		//System.out.println(completeName);
		return completName;
	}
	
	/*
	 * Methode used to get from DB table rule the new dimention of the game board
	 * and set it in the Regles 
	
	public void getNewTableDimentions()
	{
		String roomName = objSalle.getRoomName("");
		Regles objReglesSalle = objSalle.getRegles();
		boolean exist = false;
		String nomFr = "";
		String nomEng = "";
		for(int i = 0; i < roomName.length(); i++)
		{
			if(roomName.charAt(i) == '/')
				exist = true;
		}
	
		if(exist)
		{
		   StringTokenizer nomSalle = new StringTokenizer(roomName, "/");
		   nomFr = nomSalle.nextToken().trim();
		   nomEng = nomSalle.nextToken().trim();
		}
		
		Integer tmp1 = 0;
		Integer tmp2 = 0;
		
		try
  		{
  			synchronized( requete )
  			{
  				ResultSet rst = requete.executeQuery( "SELECT minimal_time, maximal_time FROM rule " + 
  						" where rule_id IN (SELECT rule_id FROM room,room_info where room.room_id = room_info.room_id " +
                        " AND (room_info.name = '" + nomFr + "' OR room_info.name = '" + nomEng + "' OR room_info.name = '" + roomName + "'));");
  				if(rst.next())
  			{
  					tmp1 = rst.getInt( "minimal_time" );
  			        tmp2 = rst.getInt( "maximal_time" );
  			         					 														
  				}
  			}
  		}
  		catch (SQLException e)
  		{
  			// Une erreur est survenue lors de l'exécution de la requète
  			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete_update_table_dimentions"));
  			objLogger.error(GestionnaireMessages.message("bd.trace"));
  			objLogger.error( e.getMessage() );
  		    e.printStackTrace();			
  		}// fin catch
		
  		if(tmp1 > 0)
  			objReglesSalle.definirTempsMinimal(tmp1);
  		if(tmp2 > 0)
  			objReglesSalle.definirTempsMaximal(tmp2);
	}//end methode */	
	
	
	//******************************************************************
	//  Bloc used to put new room in DB from room created in profModule
	//******************************************************************
	
	/**
	 * Method used to put new room in DB from room created in profModule
	 * put it in room table
	 */
	public int putNewRoom(String pass, int user_id, String name, String roomDesc, String langue, 
			String begin, String end, int masterTime, String roomCategories, String gameTypes )
	{
		int room_id = 0;
		
	    // Pour tenir compte de la langue
        int cleLang = 1;   
        
        if (langue.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (langue.equalsIgnoreCase("en"))
        	cleLang = 2;
        
        //System.out.println("categories " + roomCategories);
                
        String strSQL = "";
        
        
        	// Création du SQL pour l'ajout
    	strSQL = "INSERT INTO room (password, user_id, beginDate, endDate, masterTime, categories) VALUES (PASSWORD('" +
    		                 pass + "')," + user_id + ",'" + begin + "','" + end + "'," + masterTime + ",\"" + roomCategories + "\");";

    
		try
		{
			synchronized(requete)
			{
				// Ajouter l'information pour cette partie
				requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);

				// Aller chercher la clé de la salle qu'on vient d'ajouter
				ResultSet  rs = requete.getGeneratedKeys();

				// On retourne la clé de partie
				rs.next();
				room_id = rs.getInt("GENERATED_KEY");
			}
		}
		catch (Exception e)
		{
			System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_modProf") + e.getMessage());
		}

		//add information of the room to other tables of DB
		putNewRoomInfo(room_id, cleLang, name, roomDesc);
		//putNewRoomColorSquare(room_id);
		putNewRoomGameTypes(room_id, gameTypes);
		///putNewRoomShops(room_id); 
		
		//System.out.println(room_id);
		
		return room_id;
	}// end methode
	
	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put gameTypes in room_game_types table
	 */
	private void putNewRoomGameTypes(int room_id, String gameTypes) {
		
		ArrayList<Integer> roomAllowedTypes = new ArrayList<Integer>();
		StringTokenizer types = new StringTokenizer(gameTypes, ":");
		
		while(types.hasMoreTokens())
		{
			String var = types.nextToken();
			if(var.equals("mathEnJeu"))
				roomAllowedTypes.add(1);
			else if(var.equals("Tournament"))
				roomAllowedTypes.add(2);
			else if(var.equals("Course"))
				roomAllowedTypes.add(3);
			System.out.println("Add new room types : " + var);
		}
		
		int length = roomAllowedTypes.size();
		// Création du SQL pour l'ajout
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("INSERT INTO room_game_types (room_id, game_type_id) VALUES ( ? , ?);");
						
					for(int i = 0; i < length; i++)
					{

						// Ajouter l'information pour cette salle
						prepStatement.setInt(1, room_id);
						prepStatement.setInt(2, roomAllowedTypes.get(i));
						
						prepStatement.addBatch();//executeUpdate();

					}
					prepStatement.executeBatch();
				
			}
			catch (Exception e)
			{
				System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_gameTypes") + e.getMessage());
			}
	}


	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put infos in room_info table
	 */
	private void putNewRoomInfo(int room_id, int lang_id, String name, String roomDesc)
	{
		
		// Création du SQL pour l'ajout
		String strSQL = "INSERT INTO room_info (room_id, language_id, name, description) VALUES (" +
		                 room_id + "," + lang_id + ",\"" + name + "\",\"" + roomDesc + "\");";   
		try
		{
			synchronized(requete)
			{
				// Ajouter l'information pour cette salle
				requete.executeUpdate(strSQL);
			}
		}
		catch (Exception e)
		{
			System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_infosTable") + e.getMessage());
		}

		
	}// end methode
	
		
	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put infos in special_square_rule table
	 * @throws SQLException 
	 */
	private void putNewRoomSpecialSquare(int room_id) 
	{
		
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("INSERT INTO special_square_rule (room_id, type, priority) VALUES ( ? , ?, ?);");
						
					for(int i = 0; i < 5; i++)
					{

						// Ajouter l'information pour cette salle
						prepStatement.setInt(1, room_id);
						prepStatement.setInt(2, i + 1);
						prepStatement.setInt(3, i + 1);
						prepStatement.addBatch();//executeUpdate();

					}
					prepStatement.executeBatch();
				
			}
			catch (Exception e)
			{
				System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_specialSquare") + e.getMessage());
			}
		
		
	}// end methode
	
	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put infos in room_object table
	 * @throws SQLException 
	 */
	private void putNewRoomObjects(int room_id) 
	{
		
		PreparedStatement prepStatement = null;
		try {
			   prepStatement = connexion.prepareStatement("INSERT INTO room_object (room_id, object_id, priority) VALUES ( ? , ?, ?);");
		
				
					
					// Ajouter l'information pour cette salle
					prepStatement.setInt(1, room_id);
					prepStatement.setInt(2, 1);
					prepStatement.setInt(3, 1);
					prepStatement.addBatch();
					
					prepStatement.setInt(1, room_id);
					prepStatement.setInt(2, 3);
					prepStatement.setInt(3, 2);
					prepStatement.addBatch();
					
					prepStatement.setInt(1, room_id);
					prepStatement.setInt(2, 7);
					prepStatement.setInt(3, 3);
					prepStatement.addBatch();
				
					prepStatement.executeBatch();
				
			}
			catch (Exception e)
			{
				System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_objects") + e.getMessage());
			}
		
		
	}// end methode
	
	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put infos in room_shop table
	 * @throws SQLException 
	 */
	private void putNewRoomShops(int room_id) 
	{
		
		PreparedStatement prepStatement = null;
		try {
			   prepStatement = connexion.prepareStatement("INSERT INTO room_shop (room_id, shop_id, priority) VALUES ( ? , ?, ?);");
		
				
					for(int i = 0; i < 3; i++)
					{

						// Ajouter l'information pour cette salle
						prepStatement.setInt(1, room_id);
						prepStatement.setInt(2, i + 1);
						prepStatement.setInt(3, i + 1);
						prepStatement.addBatch();//executeUpdate();

					}
					prepStatement.executeBatch();
				
			}
			catch (Exception e)
			{
				System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_specialSquare") + e.getMessage());
			}
		
		
	}// end methode
	
	//******************************************************************
	
	/**
	 * Methode used to create the report for a room
	 * used for the moduleProf
	 * @param roomName 
	 */
	public String getReport(int creator_id, int room_id, String langue)
	{
		StringBuffer report = new StringBuffer();
		int user_id = 0;
		int score = 0;
		String questions_answers = "";
		Boolean won = null;
		String first_name = "";
		String last_name = "";
		String username = "";
		String roomName; 
		if (langue.equals("fr")){
			roomName =  this.getRoomName(room_id, 1) +  "\n";
			report.append("Salle Nr." + room_id + " - " + roomName);
		}else if (langue.equals("en")){
			roomName =  this.getRoomName(room_id, 2) + "\n";
			report.append("The Room Nr." + room_id + " - " + roomName);
		}
		
		try
		{

			synchronized(requete)
			{

				ResultSet rs = requete.executeQuery("SELECT game_user.user_id,name,last_name,username,score,questions_answers,has_won FROM game_user, user where room_id = '" + room_id + "' AND game_user.user_id = user.user_id;");
				while (rs.next()){
					user_id = rs.getInt("user_id");
					score = rs.getInt("score");
					questions_answers = rs.getString("questions_answers");
					won = rs.getBoolean("has_won");
					first_name = rs.getString("name");
					last_name = rs.getString("last_name");
					username = rs.getString("username");
					//report.append(user_id + score + questions_answers);
					makeReport(report, user_id, score, questions_answers, won, langue, first_name, last_name, username);
			    }

			}	
		}
        catch (Exception e)
        {
               System.out.println( GestionnaireMessages.message("bd.erreur_create_report") + e.getMessage());
        }
        
        
        return report.toString();
		
	}// end methode
	
	
	/**
	 * Methode satellite to getReport
	 * @param report
	 * @param user_id
	 * @param score
	 * @param answers
	 * @param won
	 * @param langue
	 */
	private void makeReport(StringBuffer report, int user_id, int score, String answers, Boolean won, String langue, String first_name, String last_name, String username)
	{
		StringTokenizer allAnswers = new StringTokenizer(answers, "/-/");
		
		String levels = "- ";
		String answersDescription = "- ";
		
		if(allAnswers.hasMoreTokens())
		{
			levels = allAnswers.nextToken();
		}
		
		if(allAnswers.hasMoreTokens())
		{
			answersDescription = allAnswers.nextToken();
		}
		if(langue.equals("fr"))
        {
        	
        	report.append("Joueur : " + first_name + " - " + last_name + " (Alias: "  + username + ")\n");
        	report.append("Le pointage pour cette partie : " + score + "  (gagnant: " + (won?"oui":"non") + ")\n");
        	report.append("Niveaux: " + levels + "\n");
        	report.append("Détails: (légende: q:numero de la question, r: réponse, c:correct, t:temps)\n " + answersDescription + "\n\n");
        }
        else if(langue.equals("en"))
        {
        	report.append("User : " + first_name + " " + last_name + " (Alias: " + username + ")\n");
        	report.append("Points : " + score + ". He won : " + won + "\n");
        	report.append("Levels : " + levels + "\n");
        	report.append("Details : (legend: q:number of question, r:answer, c:correct, t:time)\n" + answersDescription + "\n\n");
        }
		
	}// end methode
	
	
	/**
	 * Methode satellite to the makeReport()
	 */
	private String getRoomName(int roomId, int langue)
	{
		String roomName = "";
		try
		{

			synchronized(requete)
			{

				ResultSet rs = requete.executeQuery("SELECT name FROM room_info WHERE room_id = '" + roomId + "' AND language_id = '" + langue + "';");
				while (rs.next()){
					roomName = rs.getString("name");
				}

			}	
		}
        catch (Exception e)
            {
               System.out.println(GestionnaireMessages.message("bd.erreur_create_report_get_name") + e.getMessage());
            }
		return roomName;
		
	}// end methode
	


	/**
	 * Methode used to get from DB the list of rooms of user prof
	 * @param langue
	 * @param lstListeSalles 
	 * @param obtenirCleJoueur
	 * @return
	 */
	public void listRoomsProf(String langue, int userID, TreeMap<Integer, Salle> lstListeSalles) 
	{
		ArrayList<Integer> rooms = new ArrayList<Integer>();
		
        int cleLang = 1;   
        
        if (langue.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (langue.equalsIgnoreCase("en"))
        	cleLang = 2;
		//find all rooms  and fill in ArrayList
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT room.room_id FROM room, room_info where user_id = '" + userID + "' and room.room_id = room_info.room_id and language_id = " + cleLang + ";" );
				while(rs.next())
				{
					int roomId = rs.getInt("room.room_id");
					//System.out.println(roomId);				
					rooms.add(roomId);
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
			//Une erreur est survenue lors de la recherche de la prochaine salle
			objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		}
		
		fillRoomList(rooms, lstListeSalles);
	}// end methode
	
	/**
	 * Methode satellite for listRoomsProf()
	 * @param rooms
	 */
	public void fillRoomList(ArrayList<Integer> rooms, TreeMap<Integer, Salle> lstSalles) 
	{
		String nom = "";
		String motDePasse = "";
		String createur = "";
		Date beginDate = null;
	    Date endDate = null;
	    int masterTime = 0;
	    String categoriesString = "";
	    String type = "profsType";
	    int role = 2;
	    
		//now fill all the rooms with properties and add this rooms to the list
		for (int room : rooms){
			try
			{
				synchronized( requete )
				{
					ResultSet rs = requete.executeQuery( "SELECT room.password, user.username, user.role_id, beginDate, endDate, masterTime, categories " +
							" FROM room_info, room, user " +
							" WHERE room.room_id = " + room +  
							" AND room.room_id = room_info.room_id " +
							" AND user.user_id = room.user_id ;" );
					if(rs.next())
					{
						categoriesString = rs.getString("categories");
						motDePasse = rs.getString( "password" );
						createur = rs.getString("user.username");
						beginDate = rs.getTimestamp("beginDate");
						endDate = rs.getTimestamp("endDate");
						masterTime = rs.getInt("masterTime");
						role = rs.getInt("user.role_id");
						
						if(role == 3) type = "profsType";
						else type = "General";
																	
						String roomDescription = fillRoomDescription(room);
						nom = fillRoomName(room);
											
						Salle objSalle = new Salle(nom, createur, motDePasse, objControleurJeu, room, beginDate, endDate, masterTime, type);
						objSalle.setRoomDescription(roomDescription);
						
						// bloc to fill room's categories
						objSalle.setCategories(categoriesString);
																	
						lstSalles.put(room, objSalle);
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
				//Une erreur est survenue lors de la recherche de la prochaine salle
				objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_salle"));
				objLogger.error(GestionnaireMessages.message("bd.trace"));
				objLogger.error( e.getMessage() );
				e.printStackTrace();
			}

		}//end for
		
				
	}// fin méthode fillRoomList
	
	public String controlPWD(String clientPWD)
	{
		String encodedPWD = "";
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT PASSWORD('" + clientPWD + "') AS password;");
				
				if(rs.next())
				{
					encodedPWD = rs.getString("password");
				}
			}
		}catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete _PWD"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
			e.printStackTrace();			
		}
		catch( RuntimeException e)
		{
			//Une erreur est survenue lors de la recherche de la prochaine salle
			objLogger.error(GestionnaireMessages.message("bd.erreur_PWD"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
			e.printStackTrace();
		}

	   //System.out.println(encodedPWD);	
	   return encodedPWD;
	}


	public void reportBugQuestion(int user_id, int question, int language_id,
			String errorDescription) {
	try {
			synchronized(requete)
			{
				

					// Ajouter l'information pour cette salle
					requete.executeUpdate("INSERT INTO questions_with_error (question_id, user_id, language_id, description) VALUES ( " + question + " ," + user_id + " , " + language_id + " ,'" + errorDescription + "');");

			}		
		}
		catch (Exception e)
		{
			System.out.println(GestionnaireMessages.message("bd.erreur_adding_questions_errors") + e.getMessage());
		}
	
		
	}// end methode
	
}// end class
