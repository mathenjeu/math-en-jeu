package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.TreeMap;
import ServeurJeu.BD.GestionnaireBD;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class InformationPartie
{
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
	// Déclaration d'une référence vers la table courante
	private Table objTable;
	
    // Déclaration d'une variable qui va contenir le numéro Id du personnage 
	// choisit par le joueur
	private int intIdPersonnage;
	
    // Déclaration d'une variable qui va contenir le pointage de la 
    // partie du joueur possédant cet objet
	private int intPointage;
	
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
	
	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propriétés de la partie et de faire la référence vers la table.
	 */
	public InformationPartie(GestionnaireBD gestionnaireBD, Table tableCourante)
	{
		// Faire la référence vers le gestionnaire de base de données
		objGestionnaireBD = gestionnaireBD;
		
	    // Définir les propriétés de l'objet InformationPartie
	    intPointage = 0;
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
					
					i++;
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
					
					i++;
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
	 * @return Question : La question trouvée, s'il n'y a pas eu de déplacement,
	 * 					  alors la question retournée est null
	 */
	public Question trouverQuestionAPoser(Point nouvellePosition)
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
			objQuestionTrouvee = objGestionnaireBD.trouverProchaineQuestion(intCategorieQuestion, intDifficulte, lstQuestionsRepondues);
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
			// Toutes les questions de cette catégorie et de cette difficulté
			// ont toutes été posées, on vide donc toute la liste de questions
			// et on recommence du début
			//TODO: Il y aurait moyen d'améliorer ça en divisant la liste des 
			// 		questions posées en catégorie et difficulté et réinitialiser
			//		ici seulement un catégorie et difficulté, mais pas toute la
			//		liste. Cela rendrait aussi la recherche d'une question plus
			//		efficace lorsqu'on construit la requête SQL.
			lstQuestionsRepondues.clear();
			
			// Aller chercher de nouveau une question dans la BD
			objQuestionTrouvee = objGestionnaireBD.trouverProchaineQuestion(intCategorieQuestion, intDifficulte, lstQuestionsRepondues);
			
			// S'il y a eu une question trouvée, alors on l'ajoute dans la liste 
			// des questions posées et on la garde en mémoire pour pouvoir ensuite
			// traiter la réponse du joueur
			if (objQuestionTrouvee != null)
			{
				lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
				objQuestionCourante = objQuestionTrouvee;
				objPositionJoueurDesiree = nouvellePosition;
			}
		}
		
		return objQuestionTrouvee;
	}
	
	/**
	 * Cette fonction met à jour le plateau de jeu si le joueur a bien répondu
	 * à la question. Les objets sur la nouvelle case sont enlevés et le pointage
	 * du joueur est mis à jour.
	 * 
	 * @param String reponse : La réponse du joueur
	 * @return RetourVerifierReponseEtMettreAJourPlateauJeu : Un objet contenant 
	 * 				toutes les valeurs à retourner au client
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse)
	{
		// Déclaration de l'objet de retour 
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
		
		// Vérifier la réponse du joueur
		boolean bolReponseEstBonne = objQuestionCourante.reponseEstValide(reponse);
		
		// Le pointage est initialement celui courant
		int intNouveauPointage = intPointage;
		
		// Si la réponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne == true)
		{
			// Calculer le nouveau pointage du joueur
			
			// TODO:
			// Enlever les objets sur la nouvelle case
			// Donner les objets au joueur
			// Modifier le pointage du joueur
			// 		-> Il a un pointage pour son déplacement
			// 		-> Son pointage contient les pièces ramassées
			
			// TODO: Il faut aussi traiter les objets que le joueur aurait subis
			// 		 On enlève les objets subis de sur les cases

			// Créer l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage);
			objRetour.definirObjetRamasse(null);
			// Ajouter l'objet aux objets que le joueur possède
			objRetour.definirObjetSubi(null);
			objRetour.definirNouvellePosition(objPositionJoueurDesiree);
		}
		else
		{
			// Créer l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage);
			objRetour.definirExplications(objQuestionCourante.obtenirURLExplication());
		}

		return objRetour;
	}
}