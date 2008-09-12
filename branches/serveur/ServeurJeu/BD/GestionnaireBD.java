package ServeurJeu.BD;

import java.sql.*;

import org.apache.log4j.Logger;

import ServeurJeu.ComposantesJeu.BoiteQuestions;
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
	
	// Objet Statement nécessaire pour envoyer une requête au serveur MySQL
	private Statement requete;
	
	static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );
	
	private static final String strValeurGroupeAge = "valeurGroupeAge";

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
	 * et de créer un objet requête
	 */
	private void connexionDB()
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		String hote = config.obtenirString( "gestionnairebd.hote" );
		String utilisateur = config.obtenirString( "gestionnairebd.utilisateur" );
		String motDePasse = config.obtenirString( "gestionnairebd.mot-de-passe" );
		
		// Établissement de la connexion avec la base de données
		try
		{
			connexion = DriverManager.getConnection( hote, utilisateur, motDePasse);
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la connexion à la base de données
			objLogger.error(GestionnaireMessages.message("bd.erreur_connexion"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
		// Création de l'objet "requête"
		try
		{
			requete = connexion.createStatement();
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la création d'une requête
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
			
		int count=0;	//compteur du nombre d'essai de la requête

		//boucler la requête jusqu'à 5 fois si la connexion à MySQL
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
					ResultSet rs = requete.executeQuery("SELECT * FROM joueur WHERE alias = '" + nomUtilisateur + "' AND motDePasse = '" + motDePasse + "';");
					return rs.next();
				}
			}
			catch (SQLException e)
			{
				//on vérifie l'état de l'exception 
				//si l'état est égal au codeErreur
				//on peut réesayer la connexion
				if(e.getSQLState().equals(codeErreur))
				{
					count++;
				}
				else
				{
					// Une erreur est survenue lors de l'exécution de la requête
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
				ResultSet rs = requete.executeQuery("SELECT cleJoueur, prenom, nom, cleNiveau, peutCreerSalles FROM joueur WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
				if (rs.next())
				{
					if (rs.getInt("peutCreerSalles") != 0)
					{
						joueur.definirPeutCreerSalles(true);
					}
					String prenom = rs.getString("prenom");
					String nom = rs.getString("nom");
					int cle = Integer.parseInt(rs.getString("cleJoueur"));
					String cleNiveau = rs.getString( "cleNiveau" );
					joueur.definirPrenom(prenom);
					joueur.definirNomFamille(nom);
					joueur.definirCleJoueur(cle);
					joueur.definirCleNiveau( cleNiveau );
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requête
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}

        // This method fills a Question box with only the player's level
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau )
	{
                String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
		String strRequeteSQL = "SELECT " + nomTable + ".*,typereponse.nomType FROM " + nomTable +
                        ",typereponse WHERE typereponse.cleType = " + nomTable + ".typeReponse and " + nomTable + ".valide = 1 " +
			"and FichierFlashQuestion is not NULL and FichierFlashReponse is not NULL and ";
		
		
                strRequeteSQL += "cleQuestion >= " + boiteQuestions.obtenirLangue().obtenirCleQuestionMin()
                                 + " and cleQuestion <= " + boiteQuestions.obtenirLangue().obtenirCleQuestionMax()
                                 + " and ";
		    
		strRequeteSQL += strValeurGroupeAge + niveau + " > 0";
		
		remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
	}
	
        // This function fills a Question box with the player's level, a specified difficulty and a question category
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, int intCategorie, int intDifficulte )
	{
                // Noter qu'on ne tient plus compte de la catégorie!!
                String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
            
		String strRequeteSQL = "SELECT " + nomTable + ".*,typereponse.nomType FROM " + nomTable + ",typereponse " +
			"WHERE typereponse.cleType = " + nomTable + ".typeReponse and " + nomTable + ".valide = 1 " +
			"and FichierFlashQuestion is not NULL and FichierFlashReponse is not NULL ";
		
                strRequeteSQL += "and cleQuestion >= " +
		    boiteQuestions.obtenirLangue().obtenirCleQuestionMin() + " and cleQuestion <= " +
		    boiteQuestions.obtenirLangue().obtenirCleQuestionMax() + " and ";
		    
		strRequeteSQL += strValeurGroupeAge + niveau + " = " + intDifficulte;
		remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
	}
	
        // This function follows one of the two previous functions. It queries the database and
        // does the actual filling of the question box.
	private void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, String strRequeteSQL )
	{	
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				while(rs.next())
				{
					int codeQuestion = rs.getInt("cleQuestion");
					//String typeQuestion = TypeQuestion.ChoixReponse; //TODO aller chercher code dans bd
					String typeQuestion = rs.getString( "nomType" );
					String question = rs.getString( "FichierFlashQuestion" );
					String reponse = rs.getString("bonneReponse");
					String explication = rs.getString("FichierFlashReponse");
					int difficulte = rs.getInt( strValeurGroupeAge + niveau );
					//TODO la categorie???
                                        String URL = boiteQuestions.obtenirLangue().obtenirURLQuestionsReponses();
					boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, URL+question, reponse, URL+explication));
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requête
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
        
        // This function queries the DB to find the player's musical preferences
        // and returns a Vector containing URLs of MP3s the player might like
        public Vector obtenirListeURLsMusique(int cleJoueur)
	{
            Vector liste = new Vector();
            String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
            String strRequeteSQL = "SELECT musique_Fichiers.nomFichier FROM musique_Fichiers,musique_Fichiers_Categories,musique_Categories,musique_Categorie_Joueur WHERE ";
            strRequeteSQL       += "musique_Fichiers.cleFichier = musique_Fichiers_Categories.cleFichier AND ";
            strRequeteSQL       += "musique_Fichiers_Categories.cleCategorie = musique_Categories.cleCategorie AND ";
            strRequeteSQL       += "musique_Categories.cleCategorie = musique_Categorie_Joueur.cleCategorie AND ";
            strRequeteSQL       += "musique_Categorie_Joueur.cleJoueur = " + Integer.toString(cleJoueur);
            try
            {
                    synchronized( requete )
                    {
                            ResultSet rs = requete.executeQuery(strRequeteSQL);
                            while(rs.next())
                            {
                                liste.add(URLMusique + rs.getString("nomFichier"));
                            }
                    }
            }
            catch (SQLException e)
            {
                    // Une erreur est survenue lors de l'exécution de la requête
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
				ResultSet rs = requete.executeQuery("SELECT partiesCompletes, meilleurPointage, tempsPartie FROM joueur WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
				if (rs.next())
				{
					int partiesCompletes = rs.getInt( "partiesCompletes" ) + 1;
					int meilleurPointage = rs.getInt( "meilleurPointage" );
					int pointageActuel = joueur.obtenirPartieCourante().obtenirPointage();
					if( meilleurPointage < pointageActuel )
					{
						meilleurPointage = pointageActuel;
					}
					
					int tempsPartie = tempsTotal + rs.getInt("tempsPartie");
					
					//mise-a-jour
					int result = requete.executeUpdate( "UPDATE joueur SET partiesCompletes=" + partiesCompletes + ",meilleurPointage=" + meilleurPointage + ",tempsPartie=" + tempsPartie + " WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'exécution de la requête
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
		String strSQL = "INSERT INTO partie(datePartie, heurePartie, dureePartie) VALUES ('" + 
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
		String strSQL = "INSERT INTO partiejoueur(clePartie, cleJoueur, pointage, gagner) VALUES " +
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
