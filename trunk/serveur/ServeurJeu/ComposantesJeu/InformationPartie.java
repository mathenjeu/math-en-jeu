package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class InformationPartie
{
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
    // Déclaration d'une référence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;
	
	// Déclaration d'une référence vers un joueur humain correspondant à cet
	// objet d'information de partie
	private JoueurHumain objJoueurHumain;
	
	// Déclaration d'une référence vers la table courante
	private Table objTable;
	
    // Déclaration d'une variable qui va contenir le numéro Id du personnage 
	// choisit par le joueur
	private int intIdPersonnage;
	
    // Déclaration d'une variable qui va contenir le pointage de la 
    // partie du joueur possédant cet objet
	private int intPointage;
        
        // Combien d'argent ce joueur a-t-il?
        private int intArgent;
	
	// Déclaration d'une position du joueur dans le plateau de jeu
	private Point objPositionJoueur;
	
	// Déclaration d'un point qui va garder la position où le joueur
	// veut aller
	private Point objPositionJoueurDesiree;
	
	// Déclaration d'une liste de questions qui ont été répondues 
	// par le joueur
	private TreeMap lstQuestionsRepondues;
	
	// Déclaration d'une variable qui va garder la question qui est 
	// présentement posée au joueur. S'il n'y en n'a pas, alors il y a 
	// null dans cette variable
	private Question objQuestionCourante;
	
	// Déclaration d'une liste d'objets utilisables ramassés par le joueur
	private TreeMap lstObjetsUtilisablesRamasses;
	
	// Pour empêcher le joueur d'acheter plus qu'un seul objet à la fois
	private boolean bolObjetAcheter;
        
        // Déclaration de la boîte de question personnelle au joueur possédant
        // cet objet
        BoiteQuestions objBoiteQuestions;
	
    // Compteur pour l'objet réponse
    private int intCompteurObjetLivre;
	
	 
	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propriétés de la partie et de faire la référence vers la table.
	 */
	public InformationPartie( GestionnaireEvenements gestionnaireEv, GestionnaireBD gestionnaireBD, JoueurHumain joueur, Table tableCourante)
	{
		// Faire la référence vers le gestionnaire de base de données
		objGestionnaireBD = gestionnaireBD;
		
        // Faire la référence vers le gestionnaire d'evenements
		objGestionnaireEv = gestionnaireEv;
		
		// Faire la référence vers le joueur humain courant
		objJoueurHumain = joueur;
		
	    // Définir les propriétés de l'objet InformationPartie
	    intPointage = 0;
            intArgent = 0;
	    intIdPersonnage = 0;
	    
	    // Faire la référence vers la table courante
	    objTable = tableCourante;
	    
	    // Au départ, le joueur est nul part
	    objPositionJoueur = null;
	    
	    // Au départ, le joueur ne veut aller nul part
	    objPositionJoueurDesiree = null;
	    
	    // Au départ, aucune question n'est posée au joueur
	    objQuestionCourante = null;
	    
	    // Créer la liste des questions qui ont été répondues
	    lstQuestionsRepondues = new TreeMap();
	    
	    // Créer la liste des objets utilisables qui ont été ramassés
	    lstObjetsUtilisablesRamasses = new TreeMap();

            objBoiteQuestions = new BoiteQuestions(joueur.obtenirProtocoleJoueur().langue);
            objGestionnaireBD.remplirBoiteQuestions(objBoiteQuestions, objJoueurHumain.obtenirCleNiveau());
            
        intCompteurObjetLivre = 0;

	}

	/**
	 * Cette fonction permet de retourner la référence vers la table courante 
	 * du joueur.
	 * 
	 * @return Table : La référence vers la table de cette partie
	 */
	public Table obtenirTable()
	{
	   return objTable;
	}
	
	/**
	 * Cette fonction permet de retourner le pointage du joueur.
	 * 
	 * @return int : Le pointage du joueur courant
	 */
	public int obtenirPointage()
	{
	   return intPointage;
	}
	
	/**
	 * Cette fonction permet de redéfinir le pointage du joueur.
	 * 
	 * @param int pointage : Le pointage du joueur courant
	 */
	public void definirPointage(int pointage)
	{
	   intPointage = pointage;
	}
        
	/**
	 * Cette fonction permet de retourner l'argent du joueur.
	 * 
	 * @return int : L'argent du joueur courant
	 */
	public int obtenirArgent()
	{
	   return intArgent;
	}
	/**
	 * Cette fonction permet de redéfinir l'argent du joueur.
	 * 
	 * @param int argent : L'argent du joueur courant
	 */
	public void definirArgent(int argent)
	{
	   intArgent = argent;
	}
	
	/**
	 * Cette fonction permet de retourner le Id du personnage du joueur.
	 * 
	 * @return int : Le Id du personnage choisi par le joueur
	 */
	public int obtenirIdPersonnage()
	{
	   return intIdPersonnage;
	}
	
	/**
	 * Cette fonction permet de redéfinir le personnage choisi par le joueur.
	 * 
	 * @param int idPersonnage : Le numéro Id du personnage choisi 
	 * 							 pour cette partie
	 */
	public void definirIdPersonnage(int idPersonnage)
	{
	   intIdPersonnage = idPersonnage;
	}
	
	/**
	 * Cette fonction permet de retourner la position du joueur dans le 
	 * plateau de jeu.
	 * 
	 * @return Point : La position du joueur dans le plateau de jeu
	 */
	public Point obtenirPositionJoueur()
	{
	   return objPositionJoueur;
	}
	
	/**
	 * Cette fonction permet de redéfinir la nouvelle position du joueur.
	 * 
	 * @param Point positionJoueur : La position du joueur
	 */
	public void definirPositionJoueur(Point positionJoueur)
	{
		objPositionJoueur = positionJoueur;
	}
	
	/**
	 * Cette fonction permet de retourner la liste des questions répondues.
	 * 
	 * @return TreeMap : La liste des questions qui ont été répondues
	 */
	public TreeMap obtenirListeQuestionsRepondues()
	{
	   return lstQuestionsRepondues;
	}
	
	/**
	 * Cette fonction permet de retourner la question qui est présentement 
	 * posée au joueur.
	 * 
	 * @return Question : La question qui est présentement posée au joueur
	 */
	public Question obtenirQuestionCourante()
	{
	   return objQuestionCourante;
	}
	
	/**
	 * Cette fonction permet de redéfinir la question présentement posée 
	 * au joueur.
	 * 
	 * @param Question questionCourante : La question qui est présentement 
	 * 									  posée au joueur
	 */
	public void definirQuestionCourante(Question questionCourante)
	{
		objQuestionCourante = questionCourante;
	}
	
	/**
	 * Cette fonction détermine si le déplacement vers une certaine
	 * case est permis ou non. Pour être permis, il faut que le déplacement
	 * désiré soit en ligne droite, qu'il n'y ait pas de trous le séparant
	 * de sa position désirée et que la distance soit acceptée comme niveau
	 * de difficulté pour la salle. La distance minimale à parcourir est 1.
	 * 
	 * @param Point nouvellePosition : La position vers laquelle le joueur
	 * 								   veut aller
	 * @return boolean : true si le déplacement est permis
	 * 					 false sinon
	 */
	public boolean deplacementEstPermis(Point nouvellePosition)
	{
		boolean bolEstPermis = true;
		
		// Si la position de départ est la même que celle d'arrivée, alors
		// il y a une erreur, car le personnage doit faire un déplacement d'au
		// moins 1 case
		if (nouvellePosition.x == objPositionJoueur.x && nouvellePosition.y == objPositionJoueur.y)
		{
			bolEstPermis = false;
		}
		
		// Déterminer si la position désirée est en ligne droite par rapport 
		// à la position actuelle
		if (bolEstPermis == true && nouvellePosition.x != objPositionJoueur.x && nouvellePosition.y != objPositionJoueur.y)
		{
			bolEstPermis = false;
		}

		// Si la distance parcourue dépasse le nombre de cases maximal possible, alors il y a une erreur
		if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.obtenirRegles().obtenirDeplacementMaximal()) || 
									 (nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.obtenirRegles().obtenirDeplacementMaximal())))
		{
			bolEstPermis = false;
		}
		
		// Si le déplacement est toujours permis jusqu'à maintenant, alors on 
		// va vérifier qu'il n'y a pas de trous séparant le joueur de la 
		// position qu'il veut aller
		if (bolEstPermis == true)
		{
			// Si on se déplace vers la gauche
			if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x > objPositionJoueur.x)
			{
				// On commence le déplacement à la case juste à gauche de la 
				// position courante
				int i = objPositionJoueur.x + 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.x && bolEstPermis == true)
				{
					// S'il n'y a aucune case à la position courante, alors on 
					// a trouvé un trou et le déplacement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[i][objPositionJoueur.y] == null)
					{
						bolEstPermis = false;
					}
					
					i++;
				}
			}
			// Si on se déplace vers la droite
			else if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x < objPositionJoueur.x)
			{
				// On commence le déplacement à la case juste à droite de la 
				// position courante
				int i = objPositionJoueur.x - 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.x && bolEstPermis == true)
				{
					// S'il n'y a aucune case à la position courante, alors on 
					// a trouvé un trou et le déplacement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[i][objPositionJoueur.y] == null)
					{
						bolEstPermis = false;
					}
					
					i--;
				}
			}
			// Si on se déplace vers le bas
			else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y > objPositionJoueur.y)
			{
				// On commence le déplacement à la case juste en bas de la 
				// position courante
				int i = objPositionJoueur.y + 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.y && bolEstPermis == true)
				{
					// S'il n'y a aucune case à la position courante, alors on 
					// a trouvé un trou et le déplacement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null)
					{
						bolEstPermis = false;
					}
					
					i++;
				}
			}
			// Si on se déplace vers le haut
			else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y < objPositionJoueur.y)
			{
				// On commence le déplacement à la case juste en haut de la 
				// position courante
				int i = objPositionJoueur.y - 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.y && bolEstPermis == true)
				{
					// S'il n'y a aucune case à la position courante, alors on 
					// a trouvé un trou et le déplacement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null)
					{
						bolEstPermis = false;
					}
					
					i--;
				}
			}
		}
		
		return bolEstPermis;
	}
	
	/**
	 * Cette fonction permet de trouver une question selon la difficulté
	 * et le type de question à poser.
	 * 
	 * @param Point nouvellePosition : La position où le joueur désire se déplacer
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						générer un numéro de commande à retourner
	 * @return Question : La question trouvée, s'il n'y a pas eu de déplacement,
	 * 					  alors la question retournée est null
	 */
	public Question trouverQuestionAPoser(Point nouvellePosition, boolean doitGenererNoCommandeRetour)
	{
		// Déclarations de variables qui vont contenir la catégorie de question 
		// à poser, la difficulté et la question à retourner
		int intCategorieQuestion = objTable.obtenirPlateauJeuCourant()[nouvellePosition.x][nouvellePosition.y].obtenirTypeCase();
		int intDifficulte = 0;
		Question objQuestionTrouvee = null;
		
		// Si la position en x est différente de celle désirée, alors
		// c'est qu'il y a eu un déplacement sur l'axe des x
		if (objPositionJoueur.x != nouvellePosition.x)
		{
			intDifficulte = Math.abs(nouvellePosition.x - objPositionJoueur.x);
		}
		// Si la position en y est différente de celle désirée, alors
		// c'est qu'il y a eu un déplacement sur l'axe des y
		else if (objPositionJoueur.y != nouvellePosition.y)
		{
			intDifficulte = Math.abs(nouvellePosition.y - objPositionJoueur.y);
		}
		
		// Il faut que la difficulté soit plus grande que 0 pour pouvoir trouver 
		// une question
		if (intDifficulte > 0)
		{
			//TODO enlever ce commentaire : objQuestionTrouvee = objGestionnaireBD.trouverProchaineQuestion(intCategorieQuestion, intDifficulte, lstQuestionsRepondues);
			objQuestionTrouvee = trouverQuestion(intCategorieQuestion, intDifficulte);
		}
		
		// S'il y a eu une question trouvée, alors on l'ajoute dans la liste 
		// des questions posées et on la garde en mémoire pour pouvoir ensuite
		// traiter la réponse du joueur, on va aussi garder la position que le
		// joueur veut se déplacer
		if (objQuestionTrouvee != null)
		{
			lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
			objQuestionCourante = objQuestionTrouvee;
			objPositionJoueurDesiree = nouvellePosition;
		}
		else if (intDifficulte > 0)
		{
			objGestionnaireBD.remplirBoiteQuestions( objBoiteQuestions, objJoueurHumain.obtenirCleNiveau());
			objQuestionTrouvee = trouverQuestion(intCategorieQuestion, intDifficulte);
			
			lstQuestionsRepondues.clear();
			
			// S'il y a eu une question trouvée, alors on l'ajoute dans la liste 
			// des questions posées et on la garde en mémoire pour pouvoir ensuite
			// traiter la réponse du joueur
			if (objQuestionTrouvee != null)
			{
				lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
				objQuestionCourante = objQuestionTrouvee;
				objPositionJoueurDesiree = nouvellePosition;
			}
			else
			{
				// en théorie on ne devrait plus entrer dans ce else 
				System.out.println( "Ça va mal : aucune question" );
			}
		}
		
		// Si on doit générer le numéro de commande de retour, alors
		// on le génère, sinon on ne fait rien (ça devrait toujours
		// être vrai, donc on le génère tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// Générer un nouveau numéro de commande qui sera 
		    // retourné au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		return objQuestionTrouvee;
	}
	
	/**
	 * Cette fonction essaie de de piger une question du niveau de dificulté proche 
	 * de intDifficulte, si on y arrive pas, ça veut dire qu'il ne 
	 * reste plus de questions de niveau de difficulté proche 
	 * de intDifficulte
	 * 
	 * @param intCategorieQuestion
	 * @param intDifficulte
	 * @return la question trouver ou null si aucune question n'a pu être pigée
	 */
	private Question trouverQuestion(int intCategorieQuestion, int intDifficulte)
	{
		
		int intDifficulteTmp=intDifficulte;
		Question objQuestionTrouvee = null;
		int i=0;
		do
		{
			if(i%2==0)
			{
				intDifficulteTmp+=i;
			}
			else
			{
				intDifficulteTmp-=i;
			}
			
			i++;
			
			if(intDifficulteTmp>0)
			{
				objQuestionTrouvee = objBoiteQuestions.pigerQuestion( intCategorieQuestion, intDifficulteTmp );
			}
			if(i>=5)
			{
				break;
			}
		}while(objQuestionTrouvee==null);
		
		return objQuestionTrouvee;
		
	}
	
	/**
	 * Cette fonction met à jour le plateau de jeu si le joueur a bien répondu
	 * à la question. Les objets sur la nouvelle case sont enlevés et le pointage et l'argent
	 * du joueur sont mis à jour. Utilisé par les joueurs humains et les joueurs virtuels
	 *
	 */
	public static RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse, 
	    Point objPositionDesiree, Joueur objJoueur)
    {
        
		// Déclaration de l'objet de retour 
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
		
		int intPointageCourant; 
                int intArgentCourant;
		Table table;
		int intDifficulteQuestion;
		TreeMap objListeObjetsUtilisablesRamasses; 
		Point positionJoueur; 
		GestionnaireEvenements gestionnaireEv;
		Question objQuestion; 
		String nomJoueur; 
		boolean bolReponseEstBonne; 
		
		// Obtenir les divers informations à utiliser dépendamment de si
		// la fonction s'applique à un joueur humain ou un joueur virtuel
		if (objJoueur instanceof JoueurHumain)
		{
			InformationPartie objPartieCourante = ((JoueurHumain)objJoueur).obtenirPartieCourante();
			
			// Obtenir les informations du joueur humain
			intPointageCourant = objPartieCourante.obtenirPointage();
                        intArgentCourant = objPartieCourante.obtenirArgent();
		    table = objPartieCourante.obtenirTable();
		    intDifficulteQuestion = objPartieCourante.obtenirQuestionCourante().obtenirDifficulte();
		    objListeObjetsUtilisablesRamasses = objPartieCourante.obtenirListeObjets();
		    positionJoueur = objPartieCourante.obtenirPositionJoueur();
		    gestionnaireEv = objPartieCourante.obtenirGestionnaireEvenements();
		    objQuestion = objPartieCourante.obtenirQuestionCourante();
		    nomJoueur = ((JoueurHumain)objJoueur).obtenirNomUtilisateur();
		    bolReponseEstBonne = objQuestion.reponseEstValide(reponse);
		    
		}
		else
		{
			JoueurVirtuel objJoueurVirtuel = (JoueurVirtuel)objJoueur;
			
			
			// Obtenir les informations du joueur virtuel
			intPointageCourant = objJoueurVirtuel.obtenirPointage();
                        intArgentCourant   = objJoueurVirtuel.obtenirArgent();
		    table = objJoueurVirtuel.obtenirTable();
		    intDifficulteQuestion = objJoueurVirtuel.obtenirPointage(objJoueurVirtuel.obtenirPositionJoueur(), objPositionDesiree);
		    objListeObjetsUtilisablesRamasses = objJoueurVirtuel.obtenirListeObjetsRamasses();
		    positionJoueur = objJoueurVirtuel.obtenirPositionJoueur();
		    gestionnaireEv = objJoueurVirtuel.obtenirGestionnaireEvenements();
		    
		    // Pas de question pour les joueurs virtuels
		    objQuestion = null;
		    nomJoueur = objJoueurVirtuel.obtenirNom();
		    
		    // On appelle jamais cette fonction si le joueur virtuel rate 
		    // la question
		    bolReponseEstBonne = true;

		}
		
		// Le nouveau pointage est initialement le pointage courant
		int intNouveauPointage = intPointageCourant;
                
                int intNouvelArgent = intArgentCourant;
		
		// Déclaration d'une référence vers l'objet ramassé
		ObjetUtilisable objObjetRamasse = null;
		
		// Déclaration d'une référence vers l'objet subi
		ObjetUtilisable objObjetSubi = null;
		
		String collision = "";
		
		// Déclaration d'une référence vers le magasin recontré
		Magasin objMagasinRencontre = null;
		
		// Si la réponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne == true)
		{
			// Faire la référence vers la case de destination
			Case objCaseDestination = table.obtenirPlateauJeuCourant()[objPositionDesiree.x][objPositionDesiree.y];
			
			// Calculer le nouveau pointage du joueur (on ajoute le carré 
			// de la difficulté de la question au pointage)
			intNouveauPointage += intDifficulteQuestion*intDifficulteQuestion;
			
			// Si la case de destination est une case de couleur, alors on 
			// vérifie l'objet qu'il y a dessus et si c'est un objet utilisable, 
			// alors on l'enlève et on le donne au joueur, sinon si c'est une 
			// pièce on l'enlève et on met à jour le pointage du joueur, sinon 
			// on ne fait rien
			if (objCaseDestination instanceof CaseCouleur)
			{
				// Faire la référence vers la case de couleur
				CaseCouleur objCaseCouleurDestination = (CaseCouleur) objCaseDestination;
				
				// S'il y a un objet sur la case, alors on va faire l'action 
				// tout dépendant de l'objet (pièce, objet utilisable ou autre)
				if (objCaseCouleurDestination.obtenirObjetCase() != null)
				{
					// Si l'objet est un objet utilisable, alors on l'ajoute à 
					// la liste des objets utilisables du joueur
					if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable)
					{
						// Faire la référence vers l'objet utilisable
						ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetCase();
						
						// Garder la référence vers l'objet utilisable pour l'ajouter à l'objet de retour
						objObjetRamasse = objObjetUtilisable;
						
						// Ajouter l'objet ramassé dans la liste des objets du joueur courant
						objListeObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
						
						// Enlever l'objet de la case du plateau de jeu
						objCaseCouleurDestination.definirObjetCase(null);
					}
					else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece)
					{
						// Faire la référence vers la pièce
						Piece objPiece = (Piece) objCaseCouleurDestination.obtenirObjetCase();
						
						// Mettre à jour l'argent du joueur
						intNouvelArgent += objPiece.obtenirMonnaie();
						
						// Enlever la pièce de la case du plateau de jeu
						objCaseCouleurDestination.definirObjetCase(null);
						
						collision = "piece";
						
						// TODO: Il faut peut-être lancer un algo qui va placer 
						// 		 les pièces sur le plateau de jeu s'il n'y en n'a
						//		 plus
					}
					else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Magasin)
					{
						// Définir la collision
						collision = "magasin";
						
						// Définir la référence vers le magasin rencontré
						objMagasinRencontre = (Magasin) objCaseCouleurDestination.obtenirObjetCase();
					}
				}
				
				// S'il y a un objet à subir sur la case, alors on va faire une
				// certaine action (TODO: à compléter)
				if (objCaseCouleurDestination.obtenirObjetArme() != null)
				{
					// Faire la référence vers l'objet utilisable
					ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetArme();
					
					// Garder la référence vers l'objet utilisable à subir
					objObjetSubi = objObjetUtilisable;
					
					//TODO: Faire une certaine action au joueur
					
					// Enlever l'objet subi de la case
					objCaseCouleurDestination.definirObjetArme(null);
				}
			}
			
			// Créer l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent);
			objRetour.definirObjetRamasse(objObjetRamasse);
			objRetour.definirObjetSubi(objObjetSubi);
			objRetour.definirNouvellePosition(objPositionDesiree);
			objRetour.definirCollision( collision );
			objRetour.definirMagasin(objMagasinRencontre);
			
			synchronized (table.obtenirListeJoueurs())
		    {
				// Préparer l'événement de deplacement de personnage. 
				// Cette fonction va passer les joueurs et créer un 
				// InformationDestination pour chacun et ajouter l'événement 
				// dans la file de gestion d'événements
				table.preparerEvenementJoueurDeplacePersonnage(nomJoueur, collision, positionJoueur, objPositionDesiree, intNouveauPointage, intNouvelArgent);
						    	
		    }
		    
			// Modifier la position, le pointage et l'argent
			if (objJoueur instanceof JoueurHumain)
			{
				((JoueurHumain)objJoueur).obtenirPartieCourante().definirPositionJoueur(objPositionDesiree);
			    ((JoueurHumain)objJoueur).obtenirPartieCourante().definirPointage(intNouveauPointage);
                            ((JoueurHumain)objJoueur).obtenirPartieCourante().definirArgent(intNouvelArgent);
			}
			else if (objJoueur instanceof JoueurVirtuel)
			{
				((JoueurVirtuel)objJoueur).definirPositionJoueurVirtuel(objPositionDesiree);
				((JoueurVirtuel)objJoueur).definirPointage(intNouveauPointage);
                                ((JoueurVirtuel)objJoueur).definirArgent(intNouvelArgent);
			}
		}
		else
		{
			// Créer l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent);
			
			// La question sera nulle pour les joueurs virtuels
			if (objQuestion != null)
			{
				objRetour.definirExplications(objQuestion.obtenirURLExplication());
			}
		}
		
		return objRetour;
		
	}
	
	/**
	 * Cette fonction met à jour le plateau de jeu si le joueur a bien répondu
	 * à la question. Les objets sur la nouvelle case sont enlevés et le pointage
	 * et l'argent du joueur sont mis à jour.
	 * 
	 * @param String reponse : La réponse du joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						générer un numéro de commande à retourner
	 * @return RetourVerifierReponseEtMettreAJourPlateauJeu : Un objet contenant 
	 * 				toutes les valeurs à retourner au client
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse, boolean doitGenererNoCommandeRetour)
	{
		
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour =
		    verifierReponseEtMettreAJourPlateauJeu(reponse, objPositionJoueurDesiree, objJoueurHumain);
		
		// Si on doit générer le numéro de commande de retour, alors
		// on le génère, sinon on ne fait rien (ça devrait toujours
		// être vrai, donc on le génère tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// Générer un nouveau numéro de commande qui sera 
		    // retourné au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		objQuestionCourante = null;

		return objRetour;
	}
	
	/*
	 * Retourne une référence vers la liste des objets ramassés
	 */
	public TreeMap obtenirListeObjets()
	{
		return lstObjetsUtilisablesRamasses;
	}
	
	public void ajouterObjetUtilisableListe(ObjetUtilisable objObjetUtilisable)
	{
		lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
	}
	
	/* 
	 * Aller chercher une référence vers un objet de la liste des objets selon
	 * son id
	 */
	public ObjetUtilisable obtenirObjetUtilisable(int intObjetId)
	{
	     Set lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
	     Iterator objIterateurListeObjets = lstEnsembleObjets.iterator();
	     while (objIterateurListeObjets.hasNext() == true)
	     {
	     	Objet objObjet = (Objet)(((Map.Entry)(objIterateurListeObjets.next())).getValue());
	     	if (objObjet instanceof ObjetUtilisable)
	     	{
	     		if (((ObjetUtilisable)objObjet).obtenirId() == intObjetId)
	     		{
	     			return (ObjetUtilisable)objObjet;
	     		}
	     	}
	     }
	     return null;
	}
	
	/*
	 * Détermine si le joueur possède un certain objet, permet
	 * de valider l'information envoyé par le client lorsqu'il utiliser l'objet
	 */
	 public boolean joueurPossedeObjet(int id)
	 {
	     // Préparation pour parcourir la liste d'objets
	     Set lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
	     Iterator objIterateurListeObjets = lstEnsembleObjets.iterator();
	     
	     // Parcours du TreeMap
	     while (objIterateurListeObjets.hasNext() == true)
	     {
	     	Objet objObjet = (Objet)(((Map.Entry)(objIterateurListeObjets.next())).getValue());
	     	if (objObjet instanceof ObjetUtilisable)
	     	{
	     		if (((ObjetUtilisable)objObjet).obtenirId() == id)
	     		{
	     			return true;
	     		}
	     	}
	     }
	     
	     return false;
	 }
	 
	 public GestionnaireEvenements obtenirGestionnaireEvenements()
	 {
	 	return objGestionnaireEv;
	 }
	 
	public void enleverObjet(int intIdObjet, String strTypeObjet)
	{
		lstObjetsUtilisablesRamasses.remove(intIdObjet);
	}
	
	public Objet obtenirObjetCaseCourante()
	{
		// L'objet à retourné
		Objet objObjet = null;
		
		// Aller chercher le plateau de jeu
		Case[][] objPlateauJeu = objTable.obtenirPlateauJeuCourant();
		
		// Aller chercher la case où le joueur se trouve
		Case objCaseJoueur = objPlateauJeu[objPositionJoueur.x][objPositionJoueur.y];
		
		// Si c'est une case couleur, retourner l'objet, sinon on va retourner null
		if (objCaseJoueur instanceof CaseCouleur)
		{
			objObjet = ((CaseCouleur) objCaseJoueur).obtenirObjetCase();
		}
		
		return objObjet;
		
	}
	
	public boolean peutAcheterObjet()
	{
		return !bolObjetAcheter;
	}
	
	public void definirObjetAcheter(boolean valeur)
	{
		bolObjetAcheter = valeur;
	}
	
	public void initialiserCompteurObjetLivre()
	{
        intCompteurObjetLivre = Livre.NOMBRE_CHARGE;
	}
	
	public String obtenirMauvaiseReponse1(Question objQuestionPoser)
	{
		String strRetour = "";
		
		// Il faut qu'il reste des charges à l'objet livre précédamment utilisé
		if (intCompteurObjetLivre > 0)
		{
			strRetour = objQuestionPoser.obtenirMauvaiseReponse();
			if (strRetour.equals("PasUnChoixDeReponse") ||
			    strRetour.equals("Erreur"))
			{
				strRetour = "";
			}
			else
			{
				intCompteurObjetLivre--;
			}

		}
		
		return strRetour;
		
	}
}