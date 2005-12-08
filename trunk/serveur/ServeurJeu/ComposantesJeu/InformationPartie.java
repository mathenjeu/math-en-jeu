package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.TreeMap;
import ServeurJeu.BD.GestionnaireBD;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class InformationPartie
{
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private GestionnaireBD objGestionnaireBD;
	
	// D�claration d'une r�f�rence vers la table courante
	private Table objTable;
	
    // D�claration d'une variable qui va contenir le num�ro Id du personnage 
	// choisit par le joueur
	private int intIdPersonnage;
	
    // D�claration d'une variable qui va contenir le pointage de la 
    // partie du joueur poss�dant cet objet
	private int intPointage;
	
	// D�claration d'une position du joueur dans le plateau de jeu
	private Point objPositionJoueur;
	
	// D�claration d'un point qui va garder la position o� le joueur
	// veut aller
	private Point objPositionJoueurDesiree;
	
	// D�claration d'une liste de questions qui ont �t� r�pondues 
	// par le joueur
	private TreeMap lstQuestionsRepondues;
	
	// D�claration d'une variable qui va garder la question qui est 
	// pr�sentement pos�e au joueur. S'il n'y en n'a pas, alors il y a 
	// null dans cette variable
	private Question objQuestionCourante;
	
	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propri�t�s de la partie et de faire la r�f�rence vers la table.
	 */
	public InformationPartie(GestionnaireBD gestionnaireBD, Table tableCourante)
	{
		// Faire la r�f�rence vers le gestionnaire de base de donn�es
		objGestionnaireBD = gestionnaireBD;
		
	    // D�finir les propri�t�s de l'objet InformationPartie
	    intPointage = 0;
	    intIdPersonnage = 0;
	    
	    // Faire la r�f�rence vers la table courante
	    objTable = tableCourante;
	    
	    // Au d�part, le joueur est nul part
	    objPositionJoueur = null;
	    
	    // Au d�part, le joueur ne veut aller nul part
	    objPositionJoueurDesiree = null;
	    
	    // Au d�part, aucune question n'est pos�e au joueur
	    objQuestionCourante = null;
	    
	    // Cr�er la liste des questions qui ont �t� r�pondues
	    lstQuestionsRepondues = new TreeMap();
	}

	/**
	 * Cette fonction permet de retourner la r�f�rence vers la table courante 
	 * du joueur.
	 * 
	 * @return Table : La r�f�rence vers la table de cette partie
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
	 * Cette fonction permet de red�finir le pointage du joueur.
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
	 * Cette fonction permet de red�finir le personnage choisi par le joueur.
	 * 
	 * @param int idPersonnage : Le num�ro Id du personnage choisi 
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
	 * Cette fonction permet de red�finir la nouvelle position du joueur.
	 * 
	 * @param Point positionJoueur : La position du joueur
	 */
	public void definirPositionJoueur(Point positionJoueur)
	{
		objPositionJoueur = positionJoueur;
	}
	
	/**
	 * Cette fonction permet de retourner la liste des questions r�pondues.
	 * 
	 * @return TreeMap : La liste des questions qui ont �t� r�pondues
	 */
	public TreeMap obtenirListeQuestionsRepondues()
	{
	   return lstQuestionsRepondues;
	}
	
	/**
	 * Cette fonction permet de retourner la question qui est pr�sentement 
	 * pos�e au joueur.
	 * 
	 * @return Question : La question qui est pr�sentement pos�e au joueur
	 */
	public Question obtenirQuestionCourante()
	{
	   return objQuestionCourante;
	}
	
	/**
	 * Cette fonction permet de red�finir la question pr�sentement pos�e 
	 * au joueur.
	 * 
	 * @param Question questionCourante : La question qui est pr�sentement 
	 * 									  pos�e au joueur
	 */
	public void definirQuestionCourante(Question questionCourante)
	{
		objQuestionCourante = questionCourante;
	}
	
	/**
	 * Cette fonction d�termine si le d�placement vers une certaine
	 * case est permis ou non. Pour �tre permis, il faut que le d�placement
	 * d�sir� soit en ligne droite, qu'il n'y ait pas de trous le s�parant
	 * de sa position d�sir�e et que la distance soit accept�e comme niveau
	 * de difficult� pour la salle. La distance minimale � parcourir est 1.
	 * 
	 * @param Point nouvellePosition : La position vers laquelle le joueur
	 * 								   veut aller
	 * @return boolean : true si le d�placement est permis
	 * 					 false sinon
	 */
	public boolean deplacementEstPermis(Point nouvellePosition)
	{
		boolean bolEstPermis = true;
		
		// Si la position de d�part est la m�me que celle d'arriv�e, alors
		// il y a une erreur, car le personnage doit faire un d�placement d'au
		// moins 1 case
		if (nouvellePosition.x == objPositionJoueur.x && nouvellePosition.y == objPositionJoueur.y)
		{
			bolEstPermis = false;
		}
		
		// D�terminer si la position d�sir�e est en ligne droite par rapport 
		// � la position actuelle
		if (bolEstPermis == true && nouvellePosition.x != objPositionJoueur.x && nouvellePosition.y != objPositionJoueur.y)
		{
			bolEstPermis = false;
		}

		// Si la distance parcourue d�passe le nombre de cases maximal possible, alors il y a une erreur
		if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.obtenirRegles().obtenirDeplacementMaximal()) || 
									 (nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.obtenirRegles().obtenirDeplacementMaximal())))
		{
			bolEstPermis = false;
		}
		
		// Si le d�placement est toujours permis jusqu'� maintenant, alors on 
		// va v�rifier qu'il n'y a pas de trous s�parant le joueur de la 
		// position qu'il veut aller
		if (bolEstPermis == true)
		{
			// Si on se d�place vers la gauche
			if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x > objPositionJoueur.x)
			{
				// On commence le d�placement � la case juste � gauche de la 
				// position courante
				int i = objPositionJoueur.x + 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.x && bolEstPermis == true)
				{
					// S'il n'y a aucune case � la position courante, alors on 
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[i][objPositionJoueur.y] == null)
					{
						bolEstPermis = false;
					}
					
					i++;
				}
			}
			// Si on se d�place vers la droite
			else if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x < objPositionJoueur.x)
			{
				// On commence le d�placement � la case juste � droite de la 
				// position courante
				int i = objPositionJoueur.x - 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.x && bolEstPermis == true)
				{
					// S'il n'y a aucune case � la position courante, alors on 
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[i][objPositionJoueur.y] == null)
					{
						bolEstPermis = false;
					}
					
					i++;
				}
			}
			// Si on se d�place vers le bas
			else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y > objPositionJoueur.y)
			{
				// On commence le d�placement � la case juste en bas de la 
				// position courante
				int i = objPositionJoueur.y + 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.y && bolEstPermis == true)
				{
					// S'il n'y a aucune case � la position courante, alors on 
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null)
					{
						bolEstPermis = false;
					}
					
					i++;
				}
			}
			// Si on se d�place vers le haut
			else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y < objPositionJoueur.y)
			{
				// On commence le d�placement � la case juste en haut de la 
				// position courante
				int i = objPositionJoueur.y - 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.y && bolEstPermis == true)
				{
					// S'il n'y a aucune case � la position courante, alors on 
					// a trouv� un trou et le d�placement n'est pas possible
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
	 * Cette fonction permet de trouver une question selon la difficult�
	 * et le type de question � poser.
	 * 
	 * @param Point nouvellePosition : La position o� le joueur d�sire se d�placer
	 * @return Question : La question trouv�e, s'il n'y a pas eu de d�placement,
	 * 					  alors la question retourn�e est null
	 */
	public Question trouverQuestionAPoser(Point nouvellePosition)
	{
		// D�clarations de variables qui vont contenir la cat�gorie de question 
		// � poser, la difficult� et la question � retourner
		int intCategorieQuestion = objTable.obtenirPlateauJeuCourant()[nouvellePosition.x][nouvellePosition.y].obtenirTypeCase();
		int intDifficulte = 0;
		Question objQuestionTrouvee = null;
		
		// Si la position en x est diff�rente de celle d�sir�e, alors
		// c'est qu'il y a eu un d�placement sur l'axe des x
		if (objPositionJoueur.x != nouvellePosition.x)
		{
			intDifficulte = Math.abs(nouvellePosition.x - objPositionJoueur.x);
		}
		// Si la position en y est diff�rente de celle d�sir�e, alors
		// c'est qu'il y a eu un d�placement sur l'axe des y
		else if (objPositionJoueur.y != nouvellePosition.y)
		{
			intDifficulte = Math.abs(nouvellePosition.y - objPositionJoueur.y);
		}

		// Il faut que la difficult� soit plus grande que 0 pour pouvoir trouver 
		// une question
		if (intDifficulte > 0)
		{
			objQuestionTrouvee = objGestionnaireBD.trouverProchaineQuestion(intCategorieQuestion, intDifficulte, lstQuestionsRepondues);
		}
		
		// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste 
		// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
		// traiter la r�ponse du joueur, on va aussi garder la position que le
		// joueur veut se d�placer
		if (objQuestionTrouvee != null)
		{
			lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
			objQuestionCourante = objQuestionTrouvee;
			objPositionJoueurDesiree = nouvellePosition;
		}
		else if (intDifficulte > 0)
		{
			// Toutes les questions de cette cat�gorie et de cette difficult�
			// ont toutes �t� pos�es, on vide donc toute la liste de questions
			// et on recommence du d�but
			//TODO: Il y aurait moyen d'am�liorer �a en divisant la liste des 
			// 		questions pos�es en cat�gorie et difficult� et r�initialiser
			//		ici seulement un cat�gorie et difficult�, mais pas toute la
			//		liste. Cela rendrait aussi la recherche d'une question plus
			//		efficace lorsqu'on construit la requ�te SQL.
			lstQuestionsRepondues.clear();
			
			// Aller chercher de nouveau une question dans la BD
			objQuestionTrouvee = objGestionnaireBD.trouverProchaineQuestion(intCategorieQuestion, intDifficulte, lstQuestionsRepondues);
			
			// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste 
			// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
			// traiter la r�ponse du joueur
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
	 * Cette fonction met � jour le plateau de jeu si le joueur a bien r�pondu
	 * � la question. Les objets sur la nouvelle case sont enlev�s et le pointage
	 * du joueur est mis � jour.
	 * 
	 * @param String reponse : La r�ponse du joueur
	 * @return RetourVerifierReponseEtMettreAJourPlateauJeu : Un objet contenant 
	 * 				toutes les valeurs � retourner au client
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse)
	{
		// D�claration de l'objet de retour 
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
		
		// V�rifier la r�ponse du joueur
		boolean bolReponseEstBonne = objQuestionCourante.reponseEstValide(reponse);
		
		// Le pointage est initialement celui courant
		int intNouveauPointage = intPointage;
		
		// Si la r�ponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne == true)
		{
			// Calculer le nouveau pointage du joueur
			
			// TODO:
			// Enlever les objets sur la nouvelle case
			// Donner les objets au joueur
			// Modifier le pointage du joueur
			// 		-> Il a un pointage pour son d�placement
			// 		-> Son pointage contient les pi�ces ramass�es
			
			// TODO: Il faut aussi traiter les objets que le joueur aurait subis
			// 		 On enl�ve les objets subis de sur les cases

			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage);
			objRetour.definirObjetRamasse(null);
			// Ajouter l'objet aux objets que le joueur poss�de
			objRetour.definirObjetSubi(null);
			objRetour.definirNouvellePosition(objPositionJoueurDesiree);
		}
		else
		{
			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage);
			objRetour.definirExplications(objQuestionCourante.obtenirURLExplication());
		}

		return objRetour;
	}
}