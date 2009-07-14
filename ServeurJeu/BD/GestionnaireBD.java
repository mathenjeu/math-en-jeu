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
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
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
		
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery("SELECT user.user_id,last_name,name,role_id  FROM user " +
						" WHERE username = '" + joueur.obtenirNomUtilisateur() + 
						"';"); //
				if (rs.next())
				{
				
					String prenom = rs.getString("last_name");
					String nom = rs.getString("name");
					cle = rs.getInt("user_id");
					role = rs.getInt("role_id"); 
						
					joueur.definirPrenom(prenom);
					joueur.definirNomFamille(nom);
					joueur.definirCleJoueur(cle);
					joueur.setRole(role);
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
		
		// on prend dans BD les niveaux scolaires du joueur en utilisant enum Categories
		Categories[] catValues = Categories.values();
		int[] cleNiveau = new int[catValues.length];
				
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("SELECT user_subject_level.level  FROM user,user_subject_level " +
					" WHERE  user_subject_level.user_id = ? AND user_subject_level.category_id = ? ;");
		} catch (SQLException eper) {
			// TODO Auto-generated catch block
			// Une erreur est survenue lors de la création de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_create_preparedStatement_User"));
			eper.printStackTrace();
		}
		
		try{
			for(int i = 0; i < catValues.length; i++)
			{
				synchronized(prepStatement)
				{
					prepStatement.setInt(1, cle);
					prepStatement.setInt(2, catValues[i].getCode());
					ResultSet rs = prepStatement.executeQuery();
					if(rs.next())
					{
						cleNiveau[i] = rs.getInt("level");
	    			}
					
				}

			}
		}
		catch (Exception e)
		{
			System.out.println(GestionnaireMessages.message("bd.erreur_adding_info_subject_user") + e.getMessage());
		}
			
		joueur.definirCleNiveau(cleNiveau);
		fillConnectedUser(cle);
		
	}//end methode
	
/*	// This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the player categories levels
	private int fillLevels(  String strRequeteSQL )
	{	
		int level = 0;
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				while(rs.next())
				{
                   level = rs.getInt("level");
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
		return level;
	}// end methode  */
	
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
        	
        	String strRequeteSQL = "SELECT answer.is_right,question.question_id, question_info.question_flash_file,question_info.feedback_flash_file, question_level.value, answer_type.tag " +
        	" FROM question_info, question_level, question, answer_type, answer " +
        	" WHERE  question_info.language_id = " + cleLang +
        	" AND question.question_id = question_level.question_id " +
        	" AND question_info.question_id = question.question_id " +
        	" AND question.category_id = " + catScolaires[i] +
        	" and question_info.is_valid = 1 " +
        	" and question_info.question_flash_file is not NULL " +
        	" and question_info.feedback_flash_file is not NULL " +
        	" and question_level.level_id = " + niveau[i] + 
        	" and question_level.value > 0 " +
        	" and question.answer_type_id = answer_type.answer_type_id " +
        	" and question.question_id = answer.question_id " +
        	" and (answer_type.tag='MULTIPLE_CHOICE' OR answer_type.tag='MULTIPLE_CHOICE_5' OR answer_type.tag='MULTIPLE_CHOICE_5')";
        	
        	remplirBoiteQuestionsMC( boiteQuestions, strRequeteSQL, catScolaires[i] );
        	
        	String strRequeteSQL_SA = "SELECT DISTINCT a.answer_latex, qi.question_id, qi.question_flash_file, qi.feedback_flash_file, ql.value " +
        	"FROM question_info qi, question_level ql, answer_info a " +
        	"where qi.question_id IN (select q.question_id from question q, answer_type at " +
        	"where q.answer_type_id = at.answer_type_id and q.category_id = " + catScolaires[i] + " and at.tag='SHORT_ANSWER')" +
        	" AND qi.question_id = a.question_id and qi.question_id = ql.question_id " +
        	" AND qi.language_id = " + cleLang +
        	" and ql.level_id = " + niveau[i] + 
        	" and ql.value > 0 " +
        	" and qi.is_valid = 1 " +
        	" and qi.question_flash_file is not NULL" +
        	" and qi.feedback_flash_file is not NULL";
        	remplirBoiteQuestionsSA( boiteQuestions, strRequeteSQL_SA, catScolaires[i] );
        	
        	String strRequeteSQL_TF = "SELECT DISTINCT a.is_right,qi.question_id, qi.question_flash_file, qi.feedback_flash_file, ql.value " +
        	" FROM question_info qi, question_level ql, answer a " +
        	"where qi.question_id IN (select q.question_id from question q, answer_type a " +
        	"where q.answer_type_id=a.answer_type_id and q.category_id = " + catScolaires[i] + " and a.tag='TRUE_OR_FALSE') " +
        	" AND qi.question_id=a.question_id and qi.question_id=ql.question_id " +
        	" AND qi.language_id = " + cleLang +
        	" and ql.level_id = " + niveau[i] + 
        	" and ql.value > 0 " +
        	" and qi.is_valid = 1 " +
        	" and qi.question_flash_file is not NULL" +
        	" and qi.feedback_flash_file is not NULL";
        	remplirBoiteQuestionsTF( boiteQuestions, strRequeteSQL_TF, catScolaires[i] );
        	
       	   
       	   
		}//fin for
	}// fin méthode
	
    // This function follows one of the two previous functions. It queries the database and
    // does the actual filling of the question box with questions of type MULTIPLE_CHOICE.
	private void remplirBoiteQuestionsMC( BoiteQuestions boiteQuestions, String strRequeteSQL, int categorie )
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
						String typeQuestion = rs.getString( "tag" );
						String question = rs.getString( "question_flash_file" );
						String explication = rs.getString("feedback_flash_file");
						int difficulte = rs.getInt("value");
						String reponse = "" + countReponse;

						//System.out.println("MC : question " + codeQuestion + " " + reponse + " " + difficulte);
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
	private void remplirBoiteQuestionsSA( BoiteQuestions boiteQuestions, String strRequeteSQL, int categorie )
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
					//int categorie =  Integer.parseInt(rs.getString("category_id"));
					String typeQuestion = "SHORT_ANSWER";//rs.getString( "tag" );
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
	private void remplirBoiteQuestionsTF( BoiteQuestions boiteQuestions, String strRequeteSQL, int categorie )
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
					//int categorie =  Integer.parseInt(rs.getString("category_id"));
					String typeQuestion = "TRUE_OR_FALSE";   //rs.getString( "tag" );
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
  public Vector<Object> obtenirListeURLsMusique(int cleJoueur)
	{
            Vector<Object> liste = new Vector<Object>();
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
		int[] levels = joueur.obtenirCleNiveau();
		String statistics = "Levels ";
		for(int i = 0; i < levels.length; i++)
		{
			statistics =  statistics + ":" + levels[i];
		}
		
		statistics = statistics + "||" + joueur.obtenirProtocoleJoueur().getQuestionsAnswers();
		
		// Création du SQL pour l'ajout
		String strSQL = "INSERT INTO game_user(game_id, user_id, score, has_won, questions_answers) VALUES " +
		    "(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ",'" + statistics + "');"; 
		
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
     */
	public void fillsRooms()
	{
		
		ArrayList<Integer> rooms = new ArrayList<Integer>();
	/*	int langId = 0;
		if (language.equalsIgnoreCase("fr")) 
            langId = 1;
        else if (language.equalsIgnoreCase("en"))
        	langId = 2; */
		String nom = "";
		String motDePasse = "";
		String createur = "";
		String gameType = "";
		
		//find all rooms  and fill in ArrayList
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT room.room_id FROM room ;" );
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
			
		//now fill all the rooms with properties and add this rooms to the game
		for (int room : rooms){
			try
			{
				synchronized( requete )
				{
					ResultSet rs = requete.executeQuery( "SELECT room.password, user.name, game_type.name " +
							" FROM room_info, room, user, game_type " +
							" WHERE room.room_id = " + room +  
							" AND room.room_id = room_info.room_id " +
							" AND room.game_type_id = game_type.game_type_id " +
							" AND user.user_id = room.user_id ;" );
					if(rs.next())
					{
						
						motDePasse = rs.getString( "password" );
						createur = rs.getString("user.name");
						gameType = rs.getString("game_type.name");
						
						String roomDescription = fillRoomDescription(room);
						nom = fillRoomName(room);
														
						Regles objReglesSalle = new Regles();
						chargerRegllesSalle(objReglesSalle, room);
						Salle objSalle = new Salle(nom, createur, motDePasse, objReglesSalle, objControleurJeu, gameType);
						objSalle.setRoomDescription(roomDescription);
						objControleurJeu.ajouterNouvelleSalle(objSalle);
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
				
	}// fin méthode chargerSalle
	

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
		//System.out.println(name);
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
  		//System.out.println(name);
  		return name;
  	}// end methode

   /**                                  
    *                                          ***
    * @param objReglesSalle
    * @param roomId
    * @param langId 
    */
	@SuppressWarnings("unchecked")
	public void chargerRegllesSalle(Regles objReglesSalle, int roomId) {
				
        try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT rule.*, room.tournament FROM rule, room" +
						" WHERE room.room_id = " + roomId +
				        " AND rule.rule_id = room.rule_id ;" );
				while(rs.next())
				{
					boolean shownumber = rs.getBoolean("show_nb_questions");
					boolean tournament =  rs.getBoolean("tournament");
					boolean chat = rs.getBoolean( "chat" );
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
					int maxNbObjectsAndMoney = rs.getInt( "max_object_coin" );
					int nbTracks = rs.getInt( "nbTracks" );
					int nbVirtualPlayers = rs.getInt("nbVirtualPlayers");
					
					//System.out.println(tournament);
					
					objReglesSalle.setMaxNbObjectsAndMoney(maxNbObjectsAndMoney);
					objReglesSalle.setMaxNbPlayers(maxNbPlayers);
					objReglesSalle.setShowNumber(shownumber);
					objReglesSalle.setTournamentState(tournament);
					objReglesSalle.definirPermetChat(chat);
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
					objReglesSalle.setNbTracks(nbTracks);
					objReglesSalle.setNbVirtualPlayers(nbVirtualPlayers);
											
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
		TreeSet magasins = objReglesSalle.obtenirListeMagasinsPossibles();
		TreeSet casesCouleur = objReglesSalle.obtenirListeCasesCouleurPossibles();
		TreeSet casesSpeciale = objReglesSalle.obtenirListeCasesSpecialesPossibles();
		TreeSet objetsUtilisables = objReglesSalle.obtenirListeObjetsUtilisablesPossibles();
		
		this.chargerReglesMagasins(magasins, roomId);
		this.chargerReglesCasesCouleur(casesCouleur, roomId);
		this.chargerReglesCasesSpeciale(casesSpeciale, roomId);
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
     * Méthode utilisée pour charger la liste des cases couleur  ***
     * dans les Regles du partie
     * @param casesCouleur 
	 * @param roomId
     */
     private void chargerReglesCasesCouleur(TreeSet<ReglesCaseCouleur> casesCouleur, int roomId) {
                   
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
		
		String answer = "";
		String nom = salle.getRoomName(language);
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
	 * Methode to determine from DB if is permited to charge money from user's  ***
	 * account in DB
	 * @param roomName
	 * @return permit
	 */
	public boolean getMoneyRule(String roomName) {
		
		boolean permit = true; // as default is permited to take money from DB
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( "SELECT rule.money_permit FROM rule, room, room_info " +
                " WHERE room_info.name = '" + roomName + "' AND room_info.room_id = room.room_id " +
                " AND rule.rule_id = room.rule_id;" );
				while(rs.next())
				{
					permit = rs.getBoolean("money_permit");
					//System.out.println(permit);								
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
	
		return permit;
	}


	/**
	 * Return full name (in both languages) by name  ***
	 * @param nomSalle
	 * @return
	 */
	public String getFullRoomName(String nomSalle) {
		String completeName = "";
		
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
 					completeName = rs.getString("room_bilingue");
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
		return completeName;
	}
	
	/*
	 * Methode used to get from DB table rule the new dimention of the game board
	 * and set it in the Regles 
	 */
	public void getNewTableDimentions(Regles objReglesSalle, String roomName)
	{
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
	}//end methode
	
	
	//******************************************************************
	//  Bloc used to put new room in DB from room created in profModule
	//******************************************************************
	
	/**
	 * Method used to put new room in DB from room created in profModule
	 * put it in room table
	 */
	public int putNewRoom(String pass, int user_id, String name, String roomDesc, String langue )
	{
		int room_id = 0;
		
		 // Pour tenir compte de la langue
        int cleLang = 1;   
        
        if (langue.equalsIgnoreCase("fr")) 
            cleLang = 1;
        else if (langue.equalsIgnoreCase("en"))
        	cleLang = 2;
		// Création du SQL pour l'ajout
		String strSQL = "INSERT INTO room (password, game_type_id, user_id, official, rule_id) VALUES ('" +
		                 pass + "',1," + user_id + ",1,1);";

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
			System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_infos") + e.getMessage());
		}

		//add information of the room to other tables of DB
		putNewRoomInfo(room_id, cleLang, name, roomDesc);
		putNewRoomColorSquare(room_id);
		putNewRoomObjects(room_id);
		putNewRoomShops(room_id); 
		
		//System.out.println(room_id);
		
		return room_id;
	}// end methode
	
	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put infos in room_info table
	 */
	public void putNewRoomInfo(int room_id, int lang_id, String name, String roomDesc)
	{
		
		// Création du SQL pour l'ajout
		String strSQL = "INSERT INTO room_info (room_id, language_id, name, description) VALUES (" +
		                 room_id + "," + lang_id + ",'" + name + "','" + roomDesc + "');";

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
	 * put infos in color_square_rule table
	 * @throws SQLException 
	 */
	public void putNewRoomColorSquare(int room_id) 
	{
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("INSERT INTO color_square_rule (room_id, type, priority) VALUES ( ? , ?, ?);");
		} catch (SQLException eper) {
			// TODO Auto-generated catch block
			// Une erreur est survenue lors de la création de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_create_preparedStatement_NewRoom"));
			eper.printStackTrace();
		}
		
			try
			{
				for(int i = 0; i < 5; i++)
				{
					synchronized(prepStatement)
					{
						// Ajouter l'information pour cette salle
						prepStatement.setInt(1, room_id);
						prepStatement.setInt(2, i + 1);
						prepStatement.setInt(3, i + 1);
						prepStatement.addBatch();//executeUpdate();
					}
				}
				prepStatement.executeBatch();
			}
			catch (Exception e)
			{
				System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_colorSquare") + e.getMessage());
			}
		
		
	}// end methode
	
	/**
	 * Method satellite to putNewRoom() used to put new room in DB from room created in profModule
	 * put infos in special_square_rule table
	 * @throws SQLException 
	 */
	public void putNewRoomSpecialSquare(int room_id) 
	{
		
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("INSERT INTO special_square_rule (room_id, type, priority) VALUES ( ? , ?, ?);");
		} catch (SQLException eper) {
			// TODO Auto-generated catch block
			// Une erreur est survenue lors de la création de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_create_preparedStatement_NewRoom"));
			eper.printStackTrace();
		}
				
		
			try
			{
				synchronized(prepStatement)
				{
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
	public void putNewRoomObjects(int room_id) 
	{
		
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("INSERT INTO room_object (room_id, object_id, priority) VALUES ( ? , ?, ?);");
		} catch (SQLException eper) {
			// TODO Auto-generated catch block
			// Une erreur est survenue lors de la création de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_create_preparedStatement_NewRoomObjects"));
			eper.printStackTrace();
		}
				
		
			try
			{
				synchronized(prepStatement)
				{
					
					// Ajouter l'information pour cette salle
					prepStatement.setInt(1, room_id);
					prepStatement.setInt(2, 1);
					prepStatement.setInt(3, 1);
					prepStatement.addBatch();
					
					prepStatement.setInt(1, room_id);
					prepStatement.setInt(2, 3);
					prepStatement.setInt(3, 2);
					prepStatement.addBatch();
				
					prepStatement.executeBatch();
				}
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
	public void putNewRoomShops(int room_id) 
	{
		
		PreparedStatement prepStatement = null;
		try {
			prepStatement = connexion.prepareStatement("INSERT INTO room_shop (room_id, shop_id, priority) VALUES ( ? , ?, ?);");
		} catch (SQLException eper) {
			// TODO Auto-generated catch block
			// Une erreur est survenue lors de la création de la requète
			objLogger.error(GestionnaireMessages.message("bd.erreur_create_preparedStatement_shops"));
			eper.printStackTrace();
		}
				
		
			try
			{
				synchronized(prepStatement)
				{
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
			}
			catch (Exception e)
			{
				System.out.println(GestionnaireMessages.message("bd.erreur_adding_rooms_specialSquare") + e.getMessage());
			}
		
		
	}// end methode
	//******************************************************************
	
	
}// end class
