package ServeurJeu.BD;

import java.sql.*;

import org.apache.log4j.Logger;

import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import java.util.Date;
import java.text.SimpleDateFormat; 
import ServeurJeu.Configuration.GestionnaireMessages;
import java.util.Vector;

/**
 * @author Jean-FranÁois Brind'Amour
 * @author Alexandre Couët - modifications été 2008
 */
public class GestionnaireBD 
{
	// DÈclaration d'une rÈfÈrence vers le contrÙleur de jeu
	private ControleurJeu objControleurJeu;
	
        // Objet Connection nÈcessaire pour le contact avec le serveur MySQL
	private Connection connexion;
	
	// Objet Statement nÈcessaire pour envoyer une requÍte au serveur MySQL
	private Statement requete;
	
	static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );
	
	private static final String strValeurGroupeAge = "valeurGroupeAge";
	private static final String strSubjectLevel = "subject_level";
	private static final String strCategoryLevel = "category_level";
	
	// les indices de toutes les categories pour faciliter 
	// les requetes dans la base de données tout en ayant
	// une structure de sa structure
	// TODO - ajuster les indices
	
	private static final int IND_SUJ_INI = 1;	// mode normal - seulement les sujets
	private static final int IND_SUJ_FIN = 8;
	private static final int IND_ALG_INI = 11;	// algèbre
	private static final int IND_ALG_FIN = 14;
	private static final int IND_ARI_INI = 21;	// arithmétique
	private static final int IND_ARI_FIN = 26;
	private static final int IND_LOG_INI = 31;	// énigmes de logique
	private static final int IND_LOG_FIN = 31;
	private static final int IND_GEO_INI = 41;	// géométrie
	private static final int IND_GEO_FIN = 44;
	private static final int IND_FCT_INI = 51;	// fonctions
	private static final int IND_FCT_FIN = 58;
	private static final int IND_PRO_INI = 61;	// probabilités
	private static final int IND_PRO_FIN = 65;
	private static final int IND_STT_INI = 71;	// statistiques
	private static final int IND_STT_FIN = 76;
	private static final int IND_HST_INI = 81;	// histoire
	private static final int IND_HST_FIN = 86;
	private static final int IND_ACC_INI = 91;	// accromath
	private static final int IND_ACC_FIN = 91;
	
	/**
	 * Constructeur de la classe GestionnaireBD qui permet de garder la 
	 * rÈfÈrence vers le contrÙleur de jeu
	 */
	public GestionnaireBD(ControleurJeu controleur)
	{
		super();
		
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		// Garder la rÈfÈrence vers le contrÙleur de jeu
		objControleurJeu = controleur;
		
		//CrÈation du driver JDBC
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
	 * et de crÈer un objet requÍte
	 */
	private void connexionDB()
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		String hote = config.obtenirString( "gestionnairebd.hote" );
		String utilisateur = config.obtenirString( "gestionnairebd.utilisateur" );
		String motDePasse = config.obtenirString( "gestionnairebd.mot-de-passe" );
		
		// …tablissement de la connexion avec la base de donnÈes
		try
		{
			connexion = DriverManager.getConnection( hote, utilisateur, motDePasse);
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la connexion ‡ la base de donnÈes
			objLogger.error(GestionnaireMessages.message("bd.erreur_connexion"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
		// CrÈation de l'objet "requÍte"
		try
		{
			requete = connexion.createStatement();
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la crÈation d'une requÍte
			objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
	}
	
	/**
	 * Cette fonction permet de chercher dans la BD si le joueur dont le nom
	 * d'utilisateur et le mot de passe sont passÈs en paramËtres existe.
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
			
		int count=0;	//compteur du nombre d'essai de la requÍte

		//boucler la requÍte jusqu'‡ 5 fois si la connexion ‡ MySQL
		//a ÈtÈ interrompu du ‡ un manque d'activitÈ de la connexion
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
				//on vÈrifie l'Ètat de l'exception 
				//si l'Ètat est Ègal au codeErreur
				//on peut rÈesayer la connexion
				if(e.getSQLState().equals(codeErreur))
				{
					count++;
				}
				else
				{
					// Une erreur est survenue lors de l'exÈcution de la requÍte
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
	 * 								informations et les dÈfinir dans l'objet
	 */
	public void remplirInformationsJoueur(JoueurHumain joueur)
	{
		try
		{
			synchronized( requete )
			{
				// modif acouet
				ResultSet rs = requete.executeQuery("SELECT user.* FROM user WHERE username = '" + joueur.obtenirNomUtilisateur() + "';"); /*enlever cleNiveau peutCreerSalles cleCatQuestionChoix cleSousCategorieQuestion*/
				if (rs.next())
				{
					String prenom = rs.getString("name");
					String nom = rs.getString("last_name");
					int cle = Integer.parseInt(rs.getString("user_id"));
				
					joueur.definirPrenom(prenom);
					joueur.definirNomFamille(nom);
					joueur.definirCleJoueur(cle);
					
					int i=1;
					
					// requête pour le niveau du joueur à chaque SUBJECT
					String strRequeteSQL = "SELECT " + strSubjectLevel + i;
					for(i = 2; i <= 8; i++)
					{
						strRequeteSQL += ", " + strSubjectLevel + i;
					}
					
					// dans la base de données, les indices des categories ne sont pas
					// continues. On fait alors plusieurs appels en utilisant les
					// indices appropriés. C'est plus simple en general dans la BD
					// puisque les sujets sont clairement distingables.
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_ALG_INI, IND_ALG_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_ARI_INI, IND_ARI_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_LOG_INI, IND_LOG_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_GEO_INI, IND_GEO_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_FCT_INI, IND_FCT_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_PRO_INI, IND_PRO_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_STT_INI, IND_STT_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_HST_INI, IND_HST_FIN);
					strRequeteSQL += obtenirNiveauxCategoriesBD(IND_ACC_INI, IND_ACC_FIN);
					
					strRequeteSQL += " FROM user WHERE username = '" + joueur.obtenirNomUtilisateur() + "';";
					
					Vector<Integer> lstNiveauSujet = new Vector<Integer>();
					Vector<Integer> lstNiveauCategorie = new Vector<Integer>();
					
					//System.out.println(strRequeteSQL);
					
					// exécuter la requête et garder les niveaux en mémoire
					ResultSet rs2 = requete.executeQuery( strRequeteSQL );
					if (rs2.next())
					{
						
						for(i = 1; i <=8; i++)
						{
							lstNiveauSujet.add(Integer.parseInt(rs2.getString(strSubjectLevel+i)));
						}
						
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_ALG_INI, IND_ALG_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_ARI_INI, IND_ARI_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_LOG_INI, IND_LOG_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_GEO_INI, IND_GEO_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_FCT_INI, IND_FCT_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_PRO_INI, IND_PRO_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_STT_INI, IND_STT_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_HST_INI, IND_HST_FIN);
						definirNiveauxCategoriesBD(lstNiveauCategorie, rs2, IND_ACC_INI, IND_ACC_FIN);
						
					}
					
					joueur.definirListeNiveauSujets(lstNiveauSujet);
					joueur.definirListeNiveauCategorie(lstNiveauCategorie);
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exÈcution de la requÍte
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}
	
	// Fonction utilitaire
	private String obtenirNiveauxCategoriesBD(int indiceInferieur, int indiceSuperieur)
	{
		String strReqSQL = "";
		// requête pour le niveau du joueur à chaque CATEGORY
		for(int i = indiceInferieur; i <= indiceSuperieur; i++)
		{
			strReqSQL += ", " + strCategoryLevel + i;
		}
		return strReqSQL;
	}
	
	// Fonction utilitaire
	private void definirNiveauxCategoriesBD(Vector<Integer> lstNiveauCategorie, ResultSet rs, int indiceInferieur, int indiceSuperieur) throws SQLException
	{
		// garder les niveaux en mémoire
		for(int i = indiceInferieur; i <= indiceSuperieur; i++)
		{
			lstNiveauCategorie.add(Integer.parseInt(rs.getString(strCategoryLevel+i)));
		}
	}
	
	// Fonction utilitaire de remplirBoiteQuestions
	private void genererRequeteSQL(int intDifficulte, int modeAvance, BoiteQuestions boiteQuestions, JoueurHumain joueur, String nomTable, int indiceInferieur, int indiceSuperieur)
	{
		int i,j;
		//System.out.println("JOUEUR MODE : " + joueur.obtenirModeAvance());
		
		if(modeAvance == 1)
		{
			// récupérer les niveaux académiques pour toutes les catégories
			Vector lstNiveauCategorie = new Vector(92);

			lstNiveauCategorie = joueur.obtenirListeNiveauCategorie();
			// System.out.println("vecteur : " + lstNiveauCategorie.toString());
			
			for(i = indiceInferieur; i <= indiceSuperieur; i++)
			{
				// make sure the players asked for questions from this category
				if(Integer.parseInt(lstNiveauCategorie.get(i).toString()) > 0 && Integer.parseInt(lstNiveauCategorie.get(i).toString()) < 16)
				{
					// c'est ici que l'on construit la requete pour la BD
					// selon les differentes tables qu'elle contient.
					j = i+1;
					String strRequeteSQL =  "SELECT " + nomTable + ".*, answer_type_info.name, answer_type_info.answer_type_id, answer_type_info.language_id, question.question_id, question.answer_type_id " +
						"FROM " + nomTable + ", answer_type_info, question, user " +
						"WHERE answer_type_info.answer_type_id = question.answer_type_id and " +
						nomTable + ".question_id = question.question_id and " +
						nomTable + ".is_valid = 1 and " +
						nomTable + ".question_id >= 1 and " +
						nomTable + ".question_id <= 100 " +
						"and user.username = " + "'" + joueur.obtenirNomUtilisateur() + "'" + " " +
						"and answer_type_info.language_id = user.language_id " +
						"and question_flash_file is not NULL and feedback_flash_file is not NULL " +
						"and question_info.category_id = " + j + " ";
						if(intDifficulte == -1)
							strRequeteSQL += "and question_info.valueLevel" + lstNiveauCategorie.get(i)  + " > 0";
						else
							strRequeteSQL += "and question_info.valueLevel" + lstNiveauCategorie.get(i)  + " = " + intDifficulte;
						
						remplirBoiteQuestions( boiteQuestions, lstNiveauCategorie.get(i).toString(), strRequeteSQL );	
				}
				else
				{
					System.out.println("ds else - question demandée pour categorie : " + lstNiveauCategorie.get(i) + "indice : " + i);
				}
			}
		}
				
		else if(modeAvance == 0)
		{
			// récupérer les niveaux académiques pour tous les sujets
			Vector lstNiveauSujet = joueur.obtenirListeNiveauSujets();
			
			for(i = indiceInferieur; i < indiceSuperieur; i++)
			{
				j = i+1;
				String strRequeteSQL =  "SELECT " + nomTable + ".*, answer_type_info.name, answer_type_info.answer_type_id, answer_type_info.language_id, question.question_id, question.answer_type_id " +
					"FROM " + nomTable + ", answer_type_info, question, user " +
					"WHERE answer_type_info.answer_type_id = question.answer_type_id and " +
					nomTable + ".question_id = question.question_id and " +
					nomTable + ".is_valid = 1 and " +
					nomTable + ".question_id >= 1 and " +
					nomTable + ".question_id <= 20 " +
					"and user.username = " + "'" + joueur.obtenirNomUtilisateur() + "'" + " " +
					"and answer_type_info.language_id = user.language_id " +
					"and question_flash_file is not NULL and feedback_flash_file is not NULL " +
					"and question_info.subject_id = " + j + " ";
					if(intDifficulte == -1)
						strRequeteSQL += "and question_info.valueLevel" + lstNiveauSujet.get(i)  + " > 0";
					else
						strRequeteSQL += "and question_info.valueLevel" + lstNiveauSujet.get(i)  + " = " + intDifficulte;
			
				remplirBoiteQuestions( boiteQuestions, lstNiveauSujet.get(i).toString(), strRequeteSQL );
			}
		}
		else
		{
			System.out.println("ERREUR - mode inconnu");
		}
	}
	
	/**
	 *  This method fills a Question box with only the player's level
	 * 
	 * @param BoiteQuestions boiteQuestions : La boîte à remplir de questions
	 * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
	 * 								informations et les dÈfinir dans l'objet
	 */
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, JoueurHumain joueur )
	{
		// pour savoir si on utilise le mode avancé ou non dans le remplissage
		// de la boîte de questions
		int modeAvance = joueur.obtenirModeAvance();
		// modeAvance = getModeAvance();
		
		// cette valeur indique qu'on ne considère pas le niveau de difficulté 
		// de la question dans notre requête
		// sert pour éviter de d'ajouter du code inutilement
		int DIFFICULTE_OFF = -1;
		String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
			
		if(modeAvance == 0) // mode normal
		{
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_SUJ_INI-1, IND_SUJ_FIN-1);
		}
		else if(modeAvance == 1) // mode avance
		{
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_ALG_INI-1, IND_ALG_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_ARI_INI-1, IND_ARI_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_LOG_INI-1, IND_LOG_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_GEO_INI-1, IND_GEO_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_FCT_INI-1, IND_FCT_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_PRO_INI-1, IND_PRO_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_STT_INI-1, IND_STT_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_HST_INI-1, IND_HST_FIN-1);
			genererRequeteSQL(DIFFICULTE_OFF, modeAvance, boiteQuestions, joueur, nomTable, IND_ACC_INI-1, IND_ACC_FIN-1);
			
			
		}
	}


    // This function fills a Question box with the player's level, a specified difficulty and a question category
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, int intDifficulte, JoueurHumain joueur)
	{

		// pour savoir si on utilise le mode avancé ou non dans le remplissage
		// de la boîte de questions
		int modeAvance = joueur.obtenirModeAvance();
		// modeAvance = getModeAvance();
		
		String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
			
		if(modeAvance == 0) // mode normal
		{
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_SUJ_INI-1, IND_SUJ_FIN-1);
		}
		else if(modeAvance == 1) // mode avance
		{
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_ALG_INI-1, IND_ALG_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_ARI_INI-1, IND_ARI_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_LOG_INI-1, IND_LOG_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_GEO_INI-1, IND_GEO_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_FCT_INI-1, IND_FCT_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_PRO_INI-1, IND_PRO_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_STT_INI-1, IND_STT_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_HST_INI-1, IND_HST_FIN-1);
			genererRequeteSQL(intDifficulte, modeAvance, boiteQuestions, joueur, nomTable, IND_ACC_INI-1, IND_ACC_FIN-1);
			
			
		}
	}
	

	/**
	 * This function follows one of the two previous functions. 
	 * It queries the database and does the actual filling of the question box.
	 * 
	 * @param BoiteQuestions boiteQuestions : La boîte à remplir de questions
	 * @param String niveau 		: le niveau de la question
	 * @param String strRequeteSQL  : la requete SQL complete a soumettre a la base de données 
	 */
	private void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, String strRequeteSQL )
	{	
		try
		{
			synchronized( requete )
			{
				//System.out.println(strRequeteSQL);
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				while(rs.next())
				{
					int codeQuestion = rs.getInt("question_id");
					String typeQuestion = rs.getString( "name" );
					String question = rs.getString( "question_flash_file" );
					String reponse = rs.getString("good_answer");
					String explication = rs.getString("feedback_flash_file");
					
					int sujet = rs.getInt( "subject_id" );
					int categorie = rs.getInt( "category_id" );
					int difficulte = rs.getInt( "valueLevel" + niveau );
		
                    String URL = boiteQuestions.obtenirLangue().obtenirURLQuestionsReponses();
					boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL+question, reponse, URL+explication, sujet, categorie));
                       
					//System.out.println("q_type : " + typeQuestion);
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exÈcution de la requÍte
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
	}
        
	
	/**
	 * This function queries the DB to find the player's musical preferences
	 * and returns a Vector containing URLs of MP3s the player might like
	 *
	 * @param int cleJoueur : le id du joueur
	 * @param Vector 		: URL des MP3 que le joueur souhaite entendre
	 */
        public Vector obtenirListeURLsMusique(int cleJoueur)
	{
            Vector liste = new Vector();
            String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
            String strRequeteSQL = "SELECT music_file.filename FROM music_file, music_file_category, music_category, music_category_user WHERE ";
            strRequeteSQL       += "music_file.music_file_id = music_file_category.music_file_id AND ";
            strRequeteSQL       += "music_file_category.music_category_id = music_category.music_category_id AND ";
            strRequeteSQL       += "music_category.music_category_id= music_category_user.music_category_id AND ";
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
                    // Une erreur est survenue lors de l'exÈcution de la requÍte
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

        
	/**
	 * This method updates a player's information in the DB
	 *
	 * @param JoueurHumain joueur : le joueur (humain) a mettre a jour
	 * @param int tempsTotal : le temps total de jeu du joueur
	 */
	public void mettreAJourJoueur( JoueurHumain joueur, int tempsTotal )
	{
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery("SELECT number_of_completed_game, best_score, total_time_played FROM user WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
				if (rs.next())
				{
					int number_of_completed_game = rs.getInt( "number_of_completed_game" ) + 1;
					int best_score = rs.getInt( "best_score" );
					int pointageActuel = joueur.obtenirPartieCourante().obtenirPointage();
					if( best_score < pointageActuel )
					{
						best_score = pointageActuel;
					}
					
					int total_time_played = tempsTotal + rs.getInt("total_time_played");
					
					//mise-a-jour
					int result = requete.executeUpdate( "UPDATE user SET number_of_completed_game=" + number_of_completed_game + ",best_score=" + best_score + ",total_time_played=" + total_time_played + " WHERE username = '" + joueur.obtenirNomUtilisateur() + "';");
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exÈcution de la requÍte
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
		    objLogger.error(GestionnaireMessages.message("bd.trace"));
		    objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}
	
	/**
	 * Cette mÈthode permet de fermer la connexion de base de donnÈes qui 
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

    /**
     * Cette fonction permet d'ajouter les information sur une partie dans 
	 * la base de donnÈes dans la table game. 
     *
     * @param Date dateDebut : la date de la partie
     * @param int dureePartie : la duree de la partie
     * @return int : la cle de partie qui servira pour la table game_user
     */
	public int ajouterInfosPartiePartieTerminee(Date dateDebut, int dureePartie)
	{

        SimpleDateFormat objFormatDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat objFormatHeure = new SimpleDateFormat("HH:mm:ss");
        
        String strDate = objFormatDate.format(dateDebut);
        String strHeure = objFormatHeure.format(dateDebut);

        // CrÈation du SQL pour l'ajout
		String strSQL = "INSERT INTO game(date, hour, duration) VALUES ('" + 
		    strDate + "','" + strHeure + "'," + dureePartie + ")";

		try
		{
			
			synchronized(requete)
			{

				// Ajouter l'information pour cette partie
	            requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);
	            
	            // Aller chercher la clÈ de partie qu'on vient d'ajouter
	            ResultSet  rs = requete.getGeneratedKeys();
	            
	            // On retourne la clÈ de partie
	            rs.next();
	           	return Integer.parseInt(rs.getString("GENERATED_KEY"));
			}
        }
        catch (Exception e)
        {
        	System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
        }
        
        // Au cas ou il y aurait erreur, on retourne -1
        return -1;
	}

	
    /**
     * Cette fonction permet d'ajouter les informations sur une partie pour
	 * un joueur dans la table game_user;
     *
     * @param int clePartie : la cle de la partie
     * @param int cleJoueur : le id du joueur
     * @param int pointage  : le pointage final
     * @param boolean gagner: indique si le joueur a gagne ou non
     */
	public void ajouterInfosJoueurPartieTerminee(int clePartie, int cleJoueur, int pointage, boolean gagner)
	{
		int intGagner = 0;
		if (gagner == true)
		{
			intGagner = 1;
		}
		
		// CrÈation du SQL pour l'ajout
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
            }
	}
}
