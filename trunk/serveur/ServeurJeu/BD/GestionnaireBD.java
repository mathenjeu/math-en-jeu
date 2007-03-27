package ServeurJeu.BD;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.TreeMap;
import java.sql.*;

import org.apache.log4j.Logger;

import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Salle;
import ClassesUtilitaires.GenerateurPartie;
import ClassesUtilitaires.UtilitaireNombres;
import Enumerations.Visibilite;
import Enumerations.TypeQuestion;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import java.util.Date;
import java.text.SimpleDateFormat; 
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class GestionnaireBD 
{
	// D�claration d'une r�f�rence vers le contr�leur de jeu
	private ControleurJeu objControleurJeu;
	
    // Objet Connection n�cessaire pour le contact avec le serveur MySQL
	private Connection connexion;
	
	// Objet Statement n�cessaire pour envoyer une requ�te au serveur MySQL
	private Statement requete;
	
	private String urlQuestionReponse = "";
	
	static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );
	
	private static final String strValeurGroupeAge = "valeurGroupeAge";

	/**
	 * Constructeur de la classe GestionnaireBD qui permet de garder la 
	 * r�f�rence vers le contr�leur de jeu
	 */
	public GestionnaireBD(ControleurJeu controleur)
	{
		super();
		
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		urlQuestionReponse = config.obtenirString( "gestionnairebd.url-questions-reponses" );
		
		// Garder la r�f�rence vers le contr�leur de jeu
		objControleurJeu = controleur;
		
		//Cr�ation du driver JDBC
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
	 * et de cr�er un objet requ�te
	 */
	private void connexionDB()
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		String hote = config.obtenirString( "gestionnairebd.hote" );
		String utilisateur = config.obtenirString( "gestionnairebd.utilisateur" );
		String motDePasse = config.obtenirString( "gestionnairebd.mot-de-passe" );
		
		// �tablissement de la connexion avec la base de donn�es
		try
		{
			connexion = DriverManager.getConnection( hote, utilisateur, motDePasse);
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la connexion � la base de donn�es
			objLogger.error(GestionnaireMessages.message("bd.erreur_connexion"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
		// Cr�ation de l'objet "requ�te"
		try
		{
			requete = connexion.createStatement();
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de la cr�ation d'une requ�te
			objLogger.error(GestionnaireMessages.message("bd.erreur_creer_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();
		    return;			
		}
		
	}
	
	/**
	 * Cette fonction permet de chercher dans la BD si le joueur dont le nom
	 * d'utilisateur et le mot de passe sont pass�s en param�tres existe.
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
			
		int count=0;	//compteur du nombre d'essai de la requ�te

		//boucler la requ�te jusqu'� 5 fois si la connexion � MySQL
		//a �t� interrompu du � un manque d'activit� de la connexion
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
				//on v�rifie l'�tat de l'exception 
				//si l'�tat est �gal au codeErreur
				//on peut r�esayer la connexion
				if(e.getSQLState().equals(codeErreur))
				{
					count++;
				}
				else
				{
					// Une erreur est survenue lors de l'ex�cution de la requ�te
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
	 * 								informations et les d�finir dans l'objet
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
			// Une erreur est survenue lors de l'ex�cution de la requ�te
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
			objLogger.error(GestionnaireMessages.message("bd.trace"));
			objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}

	/**
	 * Cette m�thode permet de charger les salles en m�moire dans la liste
	 * des salles du contr�leur de jeu.
	 * 
	 * @param GestionnaireEvenements gestionnaireEv : Le gestionnaire d'�v�nements
	 * 				qu'on doit fournir � chaque salle pour qu'elles puissent 
	 * 				envoyer des �v�nements
	 * @deprecated
	 */
	public void chargerSalles(GestionnaireEvenements gestionnaireEv)
	{
		objLogger.error( "chargerSalles n'est pu utilis�e" );
		
		Regles objReglesSalle = new Regles();
		
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(2, 1));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(1, 2));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(3, 3));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(4, 4));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(5, 5));
		/*objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(6, 6));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(8, 7));
		objReglesSalle.obtenirListeCasesCouleurPossibles().add(new ReglesCaseCouleur(2, 8));*/
		
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(1, 1));
		/*objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(2, 2));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(3, 3));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(4, 4));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(5, 5));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(6, 6));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(7, 7));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(8, 8));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(9, 9));
		objReglesSalle.obtenirListeCasesSpecialesPossibles().add(new ReglesCaseSpeciale(10, 10));*/
		
		objReglesSalle.obtenirListeMagasinsPossibles().add(new ReglesMagasin(1, "Magasin1"));
		objReglesSalle.obtenirListeMagasinsPossibles().add(new ReglesMagasin(2, "Magasin2"));
		
		objReglesSalle.obtenirListeObjetsUtilisablesPossibles().add(new ReglesObjetUtilisable(1, "Reponse", Visibilite.Aleatoire));

		objReglesSalle.definirPermetChat(true);
		objReglesSalle.definirRatioTrous(0.30f);
		objReglesSalle.definirRatioMagasins(0.05f);
		objReglesSalle.definirRatioCasesSpeciales(0.05f);
		objReglesSalle.definirRatioPieces(0.10f);
		objReglesSalle.definirRatioObjetsUtilisables(0.05f);
		objReglesSalle.definirValeurPieceMaximale(25);
		objReglesSalle.definirTempsMinimal(10);
		objReglesSalle.definirTempsMaximal(60);
		objReglesSalle.definirDeplacementMaximal(6);
		
		/*Case[][] asdf = GenerateurPartie.genererPlateauJeu(objReglesSalle, 10, new Vector());
		for (int i = 0; i < asdf.length; i++)
		{
			for (int j = 0; j < asdf[i].length; j++)
			{
				if (asdf[i][j] == null)
				{
					System.out.println("(" + i + ", " + j + ") -> null");	
				}
				else if (asdf[i][j] instanceof CaseCouleur)
				{
					System.out.print("(" + i + ", " + j + ") -> case couleur:" + asdf[i][j].obtenirTypeCase() + ", objet:");
					
					if (((CaseCouleur) asdf[i][j]).obtenirObjetCase() == null)
					{
						System.out.print("null\n");
					}
					else if (((CaseCouleur) asdf[i][j]).obtenirObjetCase() instanceof Magasin)
					{
						System.out.print(((CaseCouleur) asdf[i][j]).obtenirObjetCase().getClass().getName() + "\n");
					}
					else if (((CaseCouleur) asdf[i][j]).obtenirObjetCase() instanceof ObjetUtilisable)
					{
						System.out.print(((CaseCouleur) asdf[i][j]).obtenirObjetCase().getClass().getName() + ", visible:" + ((ObjetUtilisable) ((CaseCouleur) asdf[i][j]).obtenirObjetCase()).estVisible() + "\n");
					}
					else
					{
						System.out.print("Piece, valeur:" + ((Piece) ((CaseCouleur) asdf[i][j]).obtenirObjetCase()).obtenirValeur() + "\n");
					}
				}
				else
				{
					System.out.println("(" + i + ", " + j + ") -> case speciale:" + asdf[i][j].obtenirTypeCase());
				}
			}
		}*/
		
	    Salle objSalle = new Salle(this, "G�n�rale", "Jeff", "", objReglesSalle, objControleurJeu);
	    //Salle objSalle2 = new Salle(gestionnaireEv, this, "Priv�e", "Jeff", "jeff", objReglesSalle);
	    
	    objControleurJeu.ajouterNouvelleSalle(objSalle);
	    //objControleurJeu.ajouterNouvelleSalle(objSalle2);
	}
	
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau )
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();

		String strRequeteSQL = "SELECT question.*,typereponse.nomType FROM question,typereponse " +
			"WHERE typereponse.cleType = question.typeReponse and ";
		
		/*
			 and cleQuestion >= " +
		    config.obtenirString("gestionnairebd.cle-question-min") + " and cleQuestion <= " +
		    config.obtenirString("gestionnairebd.cle-question-max") + " and ";
		    
		  */
		    
		strRequeteSQL += strValeurGroupeAge + niveau + " > 0";
		
		remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
	}
	
	public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, int intCategorie, int intDifficulte )
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		
		String strRequeteSQL = "SELECT question.*,typereponse.nomType FROM question,typereponse " +
		"WHERE typereponse.cleType = question.typeReponse and ";
		
		/*
		 and cleQuestion >= " +
		    config.obtenirString("gestionnairebd.cle-question-min") + " and cleQuestion <= " +
		    config.obtenirString("gestionnairebd.cle-question-max") + " and ";
		  */
		    
		strRequeteSQL += strValeurGroupeAge + niveau + " = " + intDifficulte;
		
		remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
	}
	
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
					boiteQuestions.ajouterQuestion( new Question( codeQuestion, typeQuestion, difficulte, urlQuestionReponse + question, reponse, urlQuestionReponse + explication ));
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'ex�cution de la requ�te
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
	 * Cette fonction permet de trouver une question dans la base de donn�es
	 * selon la cat�gorie de question et la difficult� et en tenant compte des
	 * questions d�j� pos�es.
	 * 
	 * @param int categorieQuestion : La cat�gorie de question dans laquelle 
	 * 								  trouver une question
	 * @param int difficulte : La difficult� de la question � retourner
	 * @param TreeMap listeQuestionsPosees : La liste des questions pos�es 
	 * @return Question : La question trouv�e, null si aucune n'est trouv�e.
	 *					  Plus la liste des questions d�j� pos�es est grande,
	 *					  alors il y a plus de chances de retourner null
	 * @deprecated
	 */
	public Question trouverProchaineQuestion(int categorieQuestion, int difficulte, TreeMap listeQuestionsPosees)
	{
		objLogger.error( "trouverProchaineQuestion n'est plus utilis�e" );
		
		// D�claration d'une question et de la requ�te SQL pour aller
		// chercher les questions dans la BD
		Question objQuestionTrouvee = null;
		/*String strRequeteSQL = "SELECT * FROM question WHERE categorie=" + categorieQuestion 
								+ " AND difficulte=" + difficulte; */
		
		String strRequeteSQL = "SELECT * FROM question WHERE cleQuestion >= 2 and cleQuestion <= 800 and cleQuestion NOT IN("; //TODO pour les test
		
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des questions pos�es (chaque �l�ment est un Map.Entry)
		Set lstEnsembleQuestions = listeQuestionsPosees.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les questions pos�es
		Iterator objIterateurListe = lstEnsembleQuestions.iterator();

		// Passer toutes les questions et ajouter ce qu'il faut dans la requ�te
		// SQL
		String codes = null;
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			int intCodeQuestion = ((Integer)(((Map.Entry)(objIterateurListe.next())).getKey())).intValue();
			// Ajouter ce qu'il faut dans la clause where de la requ�te SQL
			if( codes == null )
			{
				codes = "" + intCodeQuestion;
			}
			else
			{
				codes += "," + intCodeQuestion;
			}
			
		}
		strRequeteSQL += codes + ")";
		
		//TODO: Il y a des optimisations � faire ici concernant la structure
		// 		des questions gard�es en m�moire (on pourrait s�parer les 
		//		questions en cat�gories et en difficult�)
		// 
		
		try
		{
			synchronized( requete )
			{
				ResultSet rs = requete.executeQuery( strRequeteSQL );
				int intLength = 0;
				Vector listeQuestions = new Vector();
				while(rs.next())
				{
					int codeQuestion = rs.getInt("cleQuestion");
					String typeQuestion = TypeQuestion.ChoixReponse; //TODO aller chercher code dans bd
					String question = rs.getString( "FichierFlashQuestion" );
					String reponse = rs.getString("bonneReponse");
					String explication = rs.getString("FichierFlashReponse");
					listeQuestions.addElement( new Question( codeQuestion, typeQuestion, difficulte, urlQuestionReponse + question, reponse, urlQuestionReponse + explication ));
					intLength++;
				}
			
				if( intLength > 0 )
				{
					int intRandom = objControleurJeu.genererNbAleatoire( intLength );
					objQuestionTrouvee = (Question)listeQuestions.elementAt( intRandom );
				}
			}
		}
		catch (SQLException e)
		{
			// Une erreur est survenue lors de l'ex�cution de la requ�te
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
		
		return objQuestionTrouvee;
	}
	
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
			// Une erreur est survenue lors de l'ex�cution de la requ�te
			objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
		    objLogger.error(GestionnaireMessages.message("bd.trace"));
		    objLogger.error( e.getMessage() );
		    e.printStackTrace();			
		}
	}
	
	/**
	 * Cette m�thode permet de fermer la connexion de base de donn�es qui 
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
	 * la base de donn�es dans la table partie. 
	 *
	 * Retour: la cl� de partie qui servira pour la table partieJoueur
	 */
	public int ajouterInfosPartiePartieTerminee(Date dateDebut, int dureePartie)
	{

        SimpleDateFormat objFormatDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat objFormatHeure = new SimpleDateFormat("HH:mm:ss");
        
        String strDate = objFormatDate.format(dateDebut);
        String strHeure = objFormatHeure.format(dateDebut);

        // Cr�ation du SQL pour l'ajout
		String strSQL = "INSERT INTO partie(datePartie, heurePartie, dureePartie) VALUES ('" + 
		    strDate + "','" + strHeure + "'," + dureePartie + ")";

		try
		{
			
			synchronized(requete)
			{

				// Ajouter l'information pour cette partie
	            requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);
	            
	            // Aller chercher la cl� de partie qu'on vient d'ajouter
	            ResultSet  rs = requete.getGeneratedKeys();
	            
	            // On retourne la cl� de partie
	            rs.next();
	           	return Integer.parseInt(rs.getString("GENERATED_KEY"));
			}
        }
        catch (Exception e)
        {
        	System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
        }
        
        // Au cas o� il y aurait erreur, on retourne -1
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
		
		// Cr�ation du SQL pour l'ajout
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
