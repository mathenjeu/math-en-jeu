package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementJoueurDeplacePersonnage;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class InformationPartie
{
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private GestionnaireBD objGestionnaireBD;
	
//	 D�claration d'une r�f�rence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;
	
	// D�claration d'une r�f�rence vers un joueur humain correspondant � cet
	// objet d'information de partie
	private JoueurHumain objJoueurHumain;
	
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
	
	// D�claration d'une liste d'objets utilisables ramass�s par le joueur
	private TreeMap lstObjetsUtilisablesRamasses;
	
	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propri�t�s de la partie et de faire la r�f�rence vers la table.
	 */
	public InformationPartie( GestionnaireEvenements gestionnaireEv, GestionnaireBD gestionnaireBD, JoueurHumain joueur, Table tableCourante)
	{
		// Faire la r�f�rence vers le gestionnaire de base de donn�es
		objGestionnaireBD = gestionnaireBD;
		
//		 Faire la r�f�rence vers le gestionnaire d'evenements
		objGestionnaireEv = gestionnaireEv;
		
		// Faire la r�f�rence vers le joueur humain courant
		objJoueurHumain = joueur;
		
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
	    
	    // Cr�er la liste des objets utilisables qui ont �t� ramass�s
	    lstObjetsUtilisablesRamasses = new TreeMap();
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
					
					i--;
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
					
					i--;
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
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						g�n�rer un num�ro de commande � retourner
	 * @return Question : La question trouv�e, s'il n'y a pas eu de d�placement,
	 * 					  alors la question retourn�e est null
	 */
	public Question trouverQuestionAPoser(Point nouvellePosition, boolean doitGenererNoCommandeRetour)
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
		
		// Si on doit g�n�rer le num�ro de commande de retour, alors
		// on le g�n�re, sinon on ne fait rien (�a devrait toujours
		// �tre vrai, donc on le g�n�re tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// G�n�rer un nouveau num�ro de commande qui sera 
		    // retourn� au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		return objQuestionTrouvee;
	}
	
	/**
	 * Cette fonction met � jour le plateau de jeu si le joueur a bien r�pondu
	 * � la question. Les objets sur la nouvelle case sont enlev�s et le pointage
	 * du joueur est mis � jour.
	 * 
	 * @param String reponse : La r�ponse du joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						g�n�rer un num�ro de commande � retourner
	 * @return RetourVerifierReponseEtMettreAJourPlateauJeu : Un objet contenant 
	 * 				toutes les valeurs � retourner au client
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse, boolean doitGenererNoCommandeRetour)
	{
		// D�claration de l'objet de retour 
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
		
		// V�rifier la r�ponse du joueur
		boolean bolReponseEstBonne = objQuestionCourante.reponseEstValide(reponse);
		
		// Le pointage est initialement celui courant
		int intNouveauPointage = intPointage;
		
		// D�claration d'une r�f�rence vers l'objet ramass�
		ObjetUtilisable objObjetRamasse = null;
		
		// D�claration d'une r�f�rence vers l'objet subi
		ObjetUtilisable objObjetSubi = null;
		
		// Si la r�ponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne == true)
		{
			// Faire la r�f�rence vers la case de destination
			Case objCaseDestination = objTable.obtenirPlateauJeuCourant()[objPositionJoueurDesiree.x][objPositionJoueurDesiree.y];
			
			// Calculer le nouveau pointage du joueur (on ajoute la difficult� 
			// de la question au pointage)
			intNouveauPointage += objQuestionCourante.obtenirDifficulte();
			
			// Si la case de destination est une case de couleur, alors on 
			// v�rifie l'objet qu'il y a dessus et si c'est un objet utilisable, 
			// alors on l'enl�ve et on le donne au joueur, sinon si c'est une 
			// pi�ce on l'enl�ve et on met � jour le pointage du joueur, sinon 
			// on ne fait rien
			if (objCaseDestination instanceof CaseCouleur)
			{
				// Faire la r�f�rence vers la case de couleur
				CaseCouleur objCaseCouleurDestination = (CaseCouleur) objCaseDestination;
				
				// S'il y a un objet sur la case, alors on va faire l'action 
				// tout d�pendant de l'objet (pi�ce, objet utilisable ou autre)
				if (objCaseCouleurDestination.obtenirObjetCase() != null)
				{
					// Si l'objet est un objet utilisable, alors on l'ajoute � 
					// la liste des objets utilisables du joueur
					if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable)
					{
						// Faire la r�f�rence vers l'objet utilisable
						ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetCase();
						
						// Garder la r�f�rence vers l'objet utilisable pour l'ajouter � l'objet de retour
						objObjetRamasse = objObjetUtilisable;
						
						// Ajouter l'objet ramass� dans la liste des objets du joueur courant
						lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
						
						// Enlever l'objet de la case du plateau de jeu
						objCaseCouleurDestination.definirObjetCase(null);
					}
					else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece)
					{
						// Faire la r�f�rence vers la pi�ce
						Piece objPiece = (Piece) objCaseCouleurDestination.obtenirObjetCase();
						
						// Mettre � jour le pointage du joueur
						intNouveauPointage += objPiece.obtenirValeur();
						
						// Enlever la pi�ce de la case du plateau de jeu
						objCaseCouleurDestination.definirObjetCase(null);
						
						// TODO: Il faut peut-�tre lancer un algo qui va placer 
						// 		 les pi�ces sur le plateau de jeu s'il n'y en n'a
						//		 plus
					}
				}
				
				// S'il y a un objet � subir sur la case, alors on va faire une
				// certaine action (TODO: � compl�ter)
				if (objCaseCouleurDestination.obtenirObjetArme() != null)
				{
					// Faire la r�f�rence vers l'objet utilisable
					ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetArme();
					
					// Garder la r�f�rence vers l'objet utilisable � subir
					objObjetSubi = objObjetUtilisable;
					
					//TODO: Faire une certaine action au joueur
					
					// Enlever l'objet subi de la case
					objCaseCouleurDestination.definirObjetArme(null);
				}
			}
			
			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage);
			objRetour.definirObjetRamasse(objObjetRamasse);
			objRetour.definirObjetSubi(objObjetSubi);
			objRetour.definirNouvellePosition(objPositionJoueurDesiree);
			
			synchronized (objTable.obtenirListeJoueurs() )
		    {
				// Pr�parer l'�v�nement de deplacement de personnage. 
				// Cette fonction va passer les joueurs et cr�er un 
				// InformationDestination pour chacun et ajouter l'�v�nement 
				// dans la file de gestion d'�v�nements
				preparerEvenementJoueurDeplacePersonnage( objCaseDestination );		    	
		    }
			
			definirPositionJoueur( objPositionJoueurDesiree );
			intPointage = intNouveauPointage;
		}
		else
		{
			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage);
			objRetour.definirExplications(objQuestionCourante.obtenirURLExplication());
		}
		
		// Si on doit g�n�rer le num�ro de commande de retour, alors
		// on le g�n�re, sinon on ne fait rien (�a devrait toujours
		// �tre vrai, donc on le g�n�re tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// G�n�rer un nouveau num�ro de commande qui sera 
		    // retourn� au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		objQuestionCourante = null;

		return objRetour;
	}
	
	private void preparerEvenementJoueurDeplacePersonnage( Case objCaseDestination )
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur d�marr� une partie
		String nomUtilisateur = objJoueurHumain.obtenirNomUtilisateur();
		String collision = "";
		if( objCaseDestination instanceof CaseCouleur )
		{
			Objet objet = ((CaseCouleur)objCaseDestination).obtenirObjetCase();
			if( objet != null )
			{
				if( objet instanceof ObjetUtilisable )
				{
					collision = "objet";
				}
				else if( objet instanceof Piece )
				{
					collision = "piece";
				}
				else if( objet instanceof Magasin ) 
				{
					collision = "magasin";
				}
			}
		}
		EvenementJoueurDeplacePersonnage joueurDeplacePersonnage = new EvenementJoueurDeplacePersonnage( nomUtilisateur, objPositionJoueur, objPositionJoueurDesiree, collision );
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = objTable.obtenirListeJoueurs().entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de d�marrer la partie, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
				joueurDeplacePersonnage.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEv.ajouterEvenement(joueurDeplacePersonnage);
	}
}