package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import ServeurJeu.BD.GestionnaireBDJoueur;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.HumainPlayerBrainiacState;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.HumainPlayerBananaState;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ServeurJeu.ControleurJeu;
import java.util.LinkedList;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class InformationPartieHumain
{
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private final GestionnaireBDJoueur objGestionnaireBD;
	// D�claration d'une r�f�rence vers le gestionnaire d'evenements
	private final GestionnaireEvenements objGestionnaireEv;
	// D�claration d'une r�f�rence vers un joueur humain correspondant � cet
	// objet d'information de partie
	private final JoueurHumain objJoueurHumain;
	// D�claration d'une r�f�rence vers la table courante
	private final Table objTable;
	// D�claration d'une variable qui va contenir le num�ro Id du personnage
	private int intIdPersonnage;
	private int idDessin;
	// D�claration d'une variable qui va contenir le pointage de la
	// partie du joueur poss�dant cet objet
	private int intPointage;
	// Combien d'argent ce joueur a-t-il?
	private int intArgent;
	// D�claration d'une position du joueur dans le plateau de jeu
	private Point objPositionJoueur;
	// D�claration d'un point qui va garder la position o� le joueur
	// veut aller
	private Point objPositionJoueurDesiree;
	// D�claration d'une liste de questions qui ont �t� r�pondues
	// par le joueur
	private final LinkedList<InformationQuestion> lstQuestionsRepondues;
	// D�claration d'une variable qui va garder la question qui est
	// pr�sentement pos�e au joueur. S'il n'y en n'a pas, alors il y a
	// null dans cette variable
	private Question objQuestionCourante;
	// D�claration d'une liste d'objets utilisables ramass�s par le joueur
	private final HashMap<Integer, ObjetUtilisable> lstObjetsUtilisablesRamasses;
	// D�claration de la bo�te de question personnelle au joueur poss�dant
	// cet objet
	private final BoiteQuestions objBoiteQuestions;
	// object that describe and manipulate 
	// the Banana state of the player
	private HumainPlayerBananaState bananaState;
	// object that describe and manipulate 
	// the Braniac state of the player
	private HumainPlayerBrainiacState brainiacState;
	// to not get twice bonus
	// used in course ou tournament types of game
	private boolean wasOnFinish;
	// The number of cases on that user can to move. At the begining is set to 3.
	// After 3 correct answers add one unity. Not bigger than 6, but
	// in the case of Braniac is possible to have 7 cases. 
	private int moveVisibility;
	// Number for bonus in Tournament type of game
	// Bonus is given while arrived at finish line and is calculated
	// as number of rested game time(in sec)
	private int tournamentBonus;
	// this is the code of the set of colors of the clothes in the player's picture
	// user can change it in the frame 3 of the client
	// each of 12 picture in this set has his color
	// so final color is combination of this code and the picture
	// selected by user
	private int clothesColor;
	// used to count how many times the QuestionsBox is filled
	// if is filled after
	private int countFillBox;

	// relative time of the last change of players points
	// used for finish statistics
	private int pointsFinalTime;

	// temporary created for analyse the questions posed to the player
	private StringBuffer boiteQuestionsInfo;
	
	// time in seconds as reference to calculate time of reponse
    private int questionTimeReference;

	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propri�t�s de la partie et de faire la r�f�rence vers la table.
	 * @param gestionnaireEv
	 * @param gestionnaireBD
	 * @param joueur
	 * @param tableCourante
	 */
	public InformationPartieHumain(GestionnaireEvenements gestionnaireEv, JoueurHumain joueur, Table tableCourante) {

		// Faire la r�f�rence vers le gestionnaire de base de donn�es
		objGestionnaireBD = new GestionnaireBDJoueur(joueur);

		// Faire la r�f�rence vers le gestionnaire d'evenements
		objGestionnaireEv = gestionnaireEv;

		// Faire la r�f�rence vers le joueur humain courant
		objJoueurHumain = joueur;

		// D�finir les propri�t�s de l'objet InformationPartie
		//intPointage = 0;

		//intIdPersonnage = 0;

		// Faire la r�f�rence vers la table courante
		objTable = tableCourante;

		// charge money from DB if is permited
		//intArgent = 0;
		if (objTable.getRegles().isBolMoneyPermit()) {
			intArgent = objGestionnaireBD.getPlayersMoney(joueur.obtenirCleJoueur());
		}

		// Au d�part, le joueur est nul part
		//objPositionJoueur = null;

		// Au d�part, le joueur ne veut aller nul part
		//objPositionJoueurDesiree = null;

		// Au d�part, aucune question n'est pos�e au joueur
		//objQuestionCourante = null;

		// Cr�er la liste des questions qui ont �t� r�pondues
		lstQuestionsRepondues = new LinkedList<InformationQuestion>();

		// Cr�er la liste des objets utilisables qui ont �t� ramass�s
		lstObjetsUtilisablesRamasses = new HashMap<Integer, ObjetUtilisable>();

		//wasOnFinish = false;

		moveVisibility = 3;
		//tournamentBonus = 0;

		// set the color to default
		clothesColor = 0;

		// Brainiac state
		this.brainiacState = new HumainPlayerBrainiacState(joueur);

		// Banana state
		this.bananaState = new HumainPlayerBananaState(joueur);

		String language = joueur.obtenirProtocoleJoueur().getLang();
		this.setBoiteQuestionsInfo();
		this.objBoiteQuestions = new BoiteQuestions(language, objGestionnaireBD.transmitUrl(language),  this.boiteQuestionsInfo);

	}// fin constructeur

	public void destruction() {

		this.brainiacState.destruction();
		this.bananaState.destruction();
		this.brainiacState = null;
		this.bananaState = null;
		objGestionnaireBD.finalize();
		/*   objGestionnaireEv = null;
        objJoueurHumain = null;
        objTable = null;*/
	}

	/**
	 * @return the tournamentBonus
	 */
	public int getTournamentBonus() {
		return tournamentBonus;
	}

	/**
	 * @param tournamentBonus the tournamentBonus to set
	 */
	public void setTournamentBonus(int tournamentBonus) {
		this.tournamentBonus = tournamentBonus;
	}

	/**
	 * Cette fonction permet de retourner la r�f�rence vers la table courante
	 * du joueur.
	 *
	 * @return Table : La r�f�rence vers la table de cette partie
	 */
	public Table obtenirTable() {
		return objTable;
	}

	/**
	 * Cette fonction permet de retourner le pointage du joueur.
	 *
	 * @return int : Le pointage du joueur courant
	 */
	public int obtenirPointage() {
		return intPointage;
	}

	/**
	 * Cette fonction permet de red�finir le pointage du joueur.
	 *
	 * @param pointage Le pointage du joueur courant
	 */
	public void definirPointage(int pointage) {
		intPointage = pointage;
	}

	/**
	 * This method is used to add the value to player points.
	 *
	 * @param value - value to add
	 */
	public void addPoints(int value)
	{
		intPointage += value;
	}

	/**
	 * Cette fonction permet de retourner l'argent du joueur.
	 *
	 * @return int : L'argent du joueur courant
	 */
	public int obtenirArgent() {
		return intArgent;
	}
	
	/**
	 * This method is used to add the value to player money.
	 *
	 * @param value - value to add
	 */
	public void addMoney(int value)
	{
		intArgent += value;
	}


	/**
	 * Cette fonction permet de red�finir l'argent du joueur.
	 *
	 * @param argent L'argent du joueur courant
	 */
	public void definirArgent(int argent) {
		intArgent = argent;
	}

	/**
	 * Cette fonction permet de retourner le Id du personnage du joueur.
	 *
	 * @return int : Le Id du personnage choisi par le joueur
	 */
	public int obtenirIdPersonnage() {
		return intIdPersonnage;
	}

	/**
	 * Cette fonction permet de red�finir le personnage choisi par le joueur.
	 *
	 * @param idPersonnage Le num�ro Id du personnage choisi pour cette partie
	 */
	public void definirIdPersonnage(int idPersonnage) {
		intIdPersonnage = idPersonnage;
	}

	/**
	 * Cette fonction permet de retourner la position du joueur dans le
	 * plateau de jeu.
	 *
	 * @return Point : La position du joueur dans le plateau de jeu
	 */
	public Point obtenirPositionJoueur() {
		return objPositionJoueur;
	}

	/**
	 * Cette fonction permet de red�finir la nouvelle position du joueur.
	 *
	 * @param positionJoueur La position du joueur
	 */
	public void definirPositionJoueur(Point positionJoueur) {
		objPositionJoueur = positionJoueur;
	}

	/**
	 * Cette fonction retourne une liste contenant les questions pos�es au
	 * joueur avec l'information associ�e � la r�ponse donn�e (valide/invalide/non-r�pondue
	 * et le temps requis pour r�pondre)
	 *
	 * @return La liste des questions qui ont �t� r�pondues
	 */
	public LinkedList<InformationQuestion> obtenirListeQuestionsRepondues() {
		return lstQuestionsRepondues;
	}

	public boolean questionDejaPosee(int cle) {
		for (InformationQuestion iq : lstQuestionsRepondues)
			if (iq.obtenirQuestionId() == cle)
				return true;
		return false;
	}
	/**
	 * Cette fonction permet de retourner la question qui est pr�sentement
	 * pos�e au joueur.
	 *
	 * @return Question La question qui est pr�sentement pos�e au joueur
	 */
	public Question obtenirQuestionCourante() {
		return objQuestionCourante;
	}

	/**
	 * Cette fonction permet de red�finir la question pr�sentement pos�e
	 * au joueur.
	 *
	 * @param questionCourante La question qui est pr�sentement	pos�e au joueur
	 */
	public void definirQuestionCourante(Question questionCourante) {
		objQuestionCourante = questionCourante;
		this.boiteQuestionsInfo.append("Current question : " + questionCourante.obtenirCodeQuestion() + "\n");
	}

	/**
	 * Cette fonction d�termine si le d�placement vers une certaine
	 * case est permis ou non. Pour �tre permis, il faut que le d�placement
	 * d�sir� soit en ligne droite, qu'il n'y ait pas de trous le s�parant
	 * de sa position d�sir�e et que la distance soit accept�e comme niveau
	 * de difficult� pour la salle. La distance minimale � parcourir est 1.
	 *
	 * @param nouvellePosition La position vers laquelle le joueur veut aller
	 * @return boolean : true si le d�placement est permis false sinon
	 */
	public boolean deplacementEstPermis(Point nouvellePosition) {
		boolean bolEstPermis = true;

		// Si la position de d�part est la m�me que celle d'arriv�e, alors
		// il y a une erreur, car le personnage doit faire un d�placement d'au
		// moins 1 case
		if (nouvellePosition.x == objPositionJoueur.x && nouvellePosition.y == objPositionJoueur.y) {
			bolEstPermis = false;
		}

		// D�terminer si la position d�sir�e est en ligne droite par rapport
		// � la position actuelle
		if (bolEstPermis == true && nouvellePosition.x != objPositionJoueur.x && nouvellePosition.y != objPositionJoueur.y) {
			bolEstPermis = false;
		}

		// Si la distance parcourue d�passe le nombre de cases maximal possible, alors il y a une erreur
		// If we are in the Brainiac maximal cases = + 1
		if (this.brainiacState.isInBrainiac()) {

			if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.getRegles().obtenirDeplacementMaximal() + 1) ||
					(nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.getRegles().obtenirDeplacementMaximal() + 1))) {
				bolEstPermis = false;
			}
		} else {

			if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.getRegles().obtenirDeplacementMaximal()) ||
					(nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.getRegles().obtenirDeplacementMaximal()))) {
				bolEstPermis = false;
			}
		}

		// Si le d�placement est toujours permis jusqu'a maintenant, alors on
		// va v�rifier qu'il n'y a pas de trous s�parant le joueur de la
		// position qu'il veut aller
		if (bolEstPermis == true) {
			// Si on se d�place vers la gauche
			if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x > objPositionJoueur.x) {
				// On commence le d�placement � la case juste � gauche de la
				// position courante
				int i = objPositionJoueur.x + 1;

				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.x && bolEstPermis == true) {
					// S'il n'y a aucune case � la position courante, alors on
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.getCase(i, objPositionJoueur.y) == null) {
						bolEstPermis = false;
					}

					i++;
				}
			} // Si on se d�place vers la droite
			else if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x < objPositionJoueur.x) {
				// On commence le d�placement � la case juste � droite de la
				// position courante
				int i = objPositionJoueur.x - 1;

				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.x && bolEstPermis == true) {
					// S'il n'y a aucune case � la position courante, alors on
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[i][objPositionJoueur.y] == null) {
						bolEstPermis = false;
					}

					i--;
				}
			} // Si on se d�place vers le bas
			else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y > objPositionJoueur.y) {
				// On commence le d�placement � la case juste en bas de la
				// position courante
				int i = objPositionJoueur.y + 1;

				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.y && bolEstPermis == true) {
					// S'il n'y a aucune case � la position courante, alors on
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null) {
						bolEstPermis = false;
					}

					i++;
				}
			} // Si on se d�place vers le haut
			else if (nouvellePosition.y != objPositionJoueur.y && nouvellePosition.y < objPositionJoueur.y) {
				// On commence le d�placement � la case juste en haut de la
				// position courante
				int i = objPositionJoueur.y - 1;

				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.y && bolEstPermis == true) {
					// S'il n'y a aucune case � la position courante, alors on
					// a trouv� un trou et le d�placement n'est pas possible
					if (objTable.obtenirPlateauJeuCourant()[objPositionJoueur.x][i] == null) {
						bolEstPermis = false;
					}

					i--;
				}
			}
		}

		return bolEstPermis;
	} // fin m�thode

	/**
	 * Cette fonction permet de trouver une question selon la difficult�
	 * et le type de question � poser.
	 *
	 * @param nouvellePosition La position o� le joueur d�sire se d�placer
	 * @param doitGenererNoCommandeRetour Permet de savoir si on doit g�n�rer un
	 *        num�ro de commande � retourner
	 * @return La question trouv�e, s'il n'y a pas eu de d�placement, alors la
	 *         question retourn�e est null
	 */
	public Question trouverQuestionAPoser(Point nouvellePosition, boolean doitGenererNoCommandeRetour) {
		int intDifficulte = 0;
		Question objQuestionTrouvee = null;

		// Si la position en x est diff�rente de celle d�sir�e, alors
		// c'est qu'il y a eu un d�placement sur l'axe des x
		if (objPositionJoueur.x != nouvellePosition.x) {
			intDifficulte = Math.abs(nouvellePosition.x - objPositionJoueur.x);
		} // Si la position en y est diff�rente de celle d�sir�e, alors
		// c'est qu'il y a eu un d�placement sur l'axe des y
		else if (objPositionJoueur.y != nouvellePosition.y) {
			intDifficulte = Math.abs(nouvellePosition.y - objPositionJoueur.y);
		}

		//System.out.println("Difficulte de la question : " + intDifficulte);   // test

		// if is under Banana effects
		if (this.bananaState.isUnderBananaEffects() && intDifficulte < 6) {
			intDifficulte++;
		}
		// if is under Brainiac effects
		if (this.brainiacState.isInBrainiac() && intDifficulte > 1) {
			intDifficulte--;
		}

		// to be sure...
		if (intDifficulte > 6) {
			intDifficulte = 6;
		}
		if (intDifficulte < 1) {
			intDifficulte = 1;
		}

		//System.out.println("Difficulte de la question : " + intDifficulte);   // test

		do {
			// find a question
			objQuestionTrouvee = trouverQuestion(intDifficulte);
			//System.out.println("question : " + intDifficulte + " " + objQuestionTrouvee);

			// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste
			// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
			// traiter la r�ponse du joueur, on va aussi garder la position que le
			// joueur veut se d�placer
			if (objQuestionTrouvee != null) {
				lstQuestionsRepondues.add(new InformationQuestion(objQuestionTrouvee.obtenirCodeQuestion(), objTable.obtenirTempsRestant()));
				objQuestionCourante = objQuestionTrouvee;
				objPositionJoueurDesiree = nouvellePosition;

			} else if (objQuestionTrouvee == null && objBoiteQuestions.dontHaveQuestions()) {

				countFillBox++;
				objGestionnaireBD.remplirBoiteQuestions(countFillBox);
			}

		} while (objQuestionTrouvee == null && countFillBox < 10); // must find right number for countFillBox

		if(objQuestionTrouvee == null)
		{
			// en th�orie on ne devrait plus entrer dans ce if
			System.out.println( "�a va mal : aucune question" );
			this.boiteQuestionsInfo.append("�a va mal : aucune question " + this.objBoiteQuestions.getBoxSize() + "\n");
		}

		// Si on doit g�n�rer le num�ro de commande de retour, alors
		// on le g�n�re, sinon on ne fait rien (�a devrait toujours
		// �tre vrai, donc on le g�n�re tout le temps)
		if (doitGenererNoCommandeRetour == true) {
			// G�n�rer un nouveau num�ro de commande qui sera
			// retourn� au client
			objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();
		}
		
		questionTimeReference = objTable.obtenirTempsRestant();
        //System.out.println("Protocol for : " + lastQuestionTime);

		return objQuestionTrouvee;
	}// end method

	/**
	 * Cette fonction essaie de piger une question du niveau de dificult� proche
	 * de intDifficulte, si on y arrive pas, �a veut dire qu'il ne
	 * reste plus de questions de niveau de difficult� proche
	 * de intDifficulte
	 *
	 * @param intCategorieQuestion
	 * @return la question trouver ou null si aucune question n'a pu �tre pig�e
	 */
	private Question trouverQuestion(int intDifficulte) {
		Question objQuestionTrouvee = null;
		do {
			// pour le premier on voir la cat�gorie et difficult� demand�es
			objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulte);
			//System.out.println("trouve1 " + objQuestionTrouvee.obtenirCodeQuestion());

			//apr�s pour les difficult�s moins grands
			int intDifficulteTemp = intDifficulte;

			while (objQuestionTrouvee == null && intDifficulteTemp > 1) {
				intDifficulteTemp--;
				objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulteTemp);

			}// fin while

				//apr�s pour les difficult�s plus grands
			intDifficulteTemp = intDifficulte;
			while (objQuestionTrouvee == null && intDifficulteTemp < 6) {
				intDifficulteTemp++;
				objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulteTemp);

			}// fin while

				// to not repeat questions
			if (objQuestionTrouvee != null && questionDejaPosee(objQuestionTrouvee.obtenirCodeQuestion())) {
				//objBoiteQuestions.popQuestion(objQuestionTrouvee);
				objQuestionTrouvee = null;
			}

		} while (objQuestionTrouvee == null && !objBoiteQuestions.dontHaveQuestions());

		return objQuestionTrouvee;

	}// fin m�thode

	/**
	 * M�thode appel�e quand le joueur utilise la boule de cristal.  On trouve
	 * une nouvelle question � pos�e plus facile.
	 * @param tempsReponse le nombre de seconde �coul�es depuis que la question
	 *        courante � �t� pos�e.
	 * @param doitGenererNoCommandeRetour Permet de savoir si on doit g�n�rer un
	 *        num�ro de commande � retourner
	 * @return La question trouv�e, s'il n'y a pas eu de d�placement, alors la
	 *         question retourn�e est null
	 */
	public Question trouverQuestionAPoserCristall(boolean doitGenererNoCommandeRetour) {
		// D�clarations de variables qui vont contenir la cat�gorie de question
		// � poser, la difficult� et la question � retourner
		//***************************************************************************************
		int oldQuestion = objQuestionCourante.obtenirCodeQuestion();
		int intDifficulte = objQuestionCourante.obtenirDifficulte();
		Question objQuestionTrouvee = null;
		// to be sure...
		if (intDifficulte > 6) {
			intDifficulte = 6;
		}
		if (intDifficulte < 1) {
			intDifficulte = 1;
		}
		//System.out.println("Difficulte de la question : " + intDifficulte);   // test
		do {
			// find a question
			objQuestionTrouvee = trouverQuestionCristall(intDifficulte, oldQuestion);

			// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste
			// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
			// traiter la r�ponse du joueur, on va aussi garder la position que le
			// joueur veut se d�placer
			if (objQuestionTrouvee != null) {
				lstQuestionsRepondues.getLast().definirTempsRequis(questionTimeReference - objTable.obtenirTempsRestant());
				lstQuestionsRepondues.add(new InformationQuestion(objQuestionTrouvee.obtenirCodeQuestion(),objTable.obtenirTempsRestant()));
				objQuestionCourante = objQuestionTrouvee;
								
			} else if (objQuestionTrouvee == null && objBoiteQuestions.dontHaveQuestions()) {
				countFillBox++;
				objGestionnaireBD.remplirBoiteQuestions(countFillBox);

			}
		} while (objQuestionTrouvee == null && countFillBox < 10);

		if (objQuestionTrouvee == null) {
			// en th�orie on ne devrait plus entrer dans ce if
			System.out.println("�a va mal : aucune question");
			this.boiteQuestionsInfo.append("�a va mal : aucune question " + this.objBoiteQuestions.getBoxSize() + "\n");
		}

		// Si on doit g�n�rer le num�ro de commande de retour, alors
		// on le g�n�re, sinon on ne fait rien (�a devrait toujours
		// �tre vrai, donc on le g�n�re tout le temps)
		if (doitGenererNoCommandeRetour == true) {
			// G�n�rer un nouveau num�ro de commande qui sera
			// retourn� au client
			objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();
		}

		questionTimeReference = objTable.obtenirTempsRestant();
        
		return objQuestionTrouvee;
	}// end methode

	/**
	 * Created for the case of Cristall
	 * Cette fonction essaie de piger une question du niveau de dificult� proche
	 * de intDifficulte, si on y arrive pas, �a veut dire qu'il ne
	 * reste plus de questions de niveau de difficult� proche
	 * de intDifficulte
	 *
	 * @param intDifficulte
	 * @return la question trouver ou null si aucune question n'a pu �tre pig�e
	 */
	private Question trouverQuestionCristall(int intDifficulte, int codeOld) {

		Question objQuestionTrouvee = null;

		// to not get the same question
		do {
			// pour le premier on voir la cat�gorie et difficult� demand�es
			objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulte, codeOld);


			//apr�s pour les difficult�s moins grands
			int intDifficulteTemp = intDifficulte;

			while (objQuestionTrouvee == null && intDifficulteTemp > 1) {
				intDifficulteTemp--;
				objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulteTemp, codeOld);

			}// fin while

				//au pire cas les difficult�s plus grands
			intDifficulteTemp = intDifficulte;

			while (objQuestionTrouvee == null && intDifficulteTemp < 6) {
				intDifficulteTemp++;
				objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulteTemp, codeOld);

			}// fin while

				// to not repeat questions
			if (objQuestionTrouvee != null && questionDejaPosee(objQuestionTrouvee.obtenirCodeQuestion())) {
				//objBoiteQuestions.popQuestion(objQuestionTrouvee);
				objQuestionTrouvee = null;
			}


		} while (objQuestionTrouvee == null && !objBoiteQuestions.dontHaveQuestions());
		//System.out.println(" verification " + objQuestionTrouvee);

		return objQuestionTrouvee;

	}// fin m�thode

	/**
	 * Cette fonction met � jour le plateau de jeu si le joueur a bien r�pondu
	 * � la question. Les objets sur la nouvelle case sont enlev�s et le pointage et l'argent
	 * du joueur sont mis � jour. Utilis� par les joueurs humains et les joueurs virtuels
	 * @param reponse La r�ponse du joueur
	 * @param tempsReponse Le temps prit pour r�pondre � la question
	 * @param objPositionDesiree La nouvelle position desiree
	 * @param objJoueur  Le joueur pour qui la m�thode doit �tre ex�cut�e
	 * @return Un objet contenant toutes les valeurs � retourner au client
	 *
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse) {

		// D�claration de l'objet de retour
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
		
		int bonus = 0;
		Point positionJoueur;
		
		boolean bolReponseEstBonne;
		int deplacementJoueur = 0;
		boolean stopTheGame = false;
		
		bonus = getTournamentBonus();
		
		positionJoueur = obtenirPositionJoueur();
		
		// Si la position en x est diff�rente de celle d�sir�e, alors
		// c'est qu'il y a eu un d�placement sur l'axe des x
		if (positionJoueur.x != objPositionJoueurDesiree.x) {
			deplacementJoueur = Math.abs(objPositionJoueurDesiree.x - positionJoueur.x);
		} // Si la position en y est diff�rente de celle d�sir�e, alors
		// c'est qu'il y a eu un d�placement sur l'axe des y
		else if (positionJoueur.y != objPositionJoueurDesiree.y) {
			deplacementJoueur = Math.abs(objPositionJoueurDesiree.y - positionJoueur.y);
		}
		
		// If we're in debug mode, accept any answer
		if (ControleurJeu.modeDebug)
			bolReponseEstBonne = true;
		else
			bolReponseEstBonne = Question.reponseEstValide(reponse, objQuestionCourante.getStringAnswer());

		InformationQuestion iq = lstQuestionsRepondues.getLast();
		iq.definirTempsRequis( questionTimeReference - objTable.obtenirTempsRestant());
		iq.definirValiditee(bolReponseEstBonne?InformationQuestion.RIGHT_ANSWER:InformationQuestion.WRONG_ANSWER);

		// D�claration d'une r�f�rence vers l'objet ramass�
		ObjetUtilisable objObjetRamasse = null;

		// D�claration d'une r�f�rence vers l'objet subi
		ObjetUtilisable objObjetSubi = null;

		String collision = "";

		// D�claration d'une r�f�rence vers le magasin recontr�
		Magasin objMagasinRencontre = null;

		// Si la r�ponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne) {

			if (deplacementJoueur == 1 && bananaState.isUnderBananaEffects()) {
				addPoints(-1);
			}
			
			// Calculer le nouveau pointage du joueur
			switch (deplacementJoueur) {
			case 1:
				addPoints(2);
				break;
			case 2:
				addPoints(3);
				break;
			case 3:
				addPoints(5);
				break;
			case 4:
				addPoints(8);
				break;
			case 5:
				addPoints(13);
				break;
			case 6:
				addPoints(21);
				break;
			case 7:
				addPoints(34);
				break;
			}



			// Si la case de destination est une case de couleur, alors on
			// v�rifie l'objet qu'il y a dessus et si c'est un objet utilisable,
			// alors on l'enl�ve et on le donne au joueur, sinon si c'est une
			// pi�ce on l'enl�ve et on met � jour le pointage du joueur, sinon
			// on ne fait rien
			Case objCaseDestination = null;
			if(objTable.getCase(objPositionJoueurDesiree.x, objPositionJoueurDesiree.y) != null)
				objCaseDestination = objTable.getCase(objPositionJoueurDesiree.x, objPositionJoueurDesiree.y);
			if (objCaseDestination != null && objCaseDestination instanceof CaseCouleur) {
				// Faire la r�f�rence vers la case de couleur
				CaseCouleur objCaseCouleurDestination = (CaseCouleur)objCaseDestination;

				// S'il y a un objet sur la case, alors on va faire l'action
				// tout d�pendant de l'objet (pi�ce, objet utilisable ou autre)
				if (objCaseCouleurDestination.obtenirObjetCase() != null) {
					// Si l'objet est un objet utilisable, alors on l'ajoute �
					// la liste des objets utilisables du joueur
					if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable) {

						if (objCaseCouleurDestination.obtenirObjetCase() instanceof Brainiac) {

							// put the player on the Brainiac state
							getBrainiacState().putTheOneBrainiac();
							objTable.preparerEvenementUtiliserObjet(objJoueurHumain.obtenirNomUtilisateur(), objJoueurHumain.obtenirNomUtilisateur(), "Brainiac", "");
							// Enlever l'objet de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							// On va dire aux clients qu'il y a eu collision avec cet objet
							collision = "Brainiac";

						} else {
							// Faire la r�f�rence vers l'objet utilisable
							ObjetUtilisable objObjetUtilisable = (ObjetUtilisable)objCaseCouleurDestination.obtenirObjetCase();

							// Garder la r�f�rence vers l'objet utilisable pour l'ajouter � l'objet de retour
							objObjetRamasse = objObjetUtilisable;

							// Ajouter l'objet ramass� dans la liste des objets du joueur courant
							lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);

							// Enlever l'objet de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							// On va dire aux clients qu'il y a eu collision avec cet objet
							collision = objObjetUtilisable.obtenirTypeObjet();
						}

					} else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece) {

						// Faire la r�f�rence vers la pi�ce
						Piece objPiece = (Piece)objCaseCouleurDestination.obtenirObjetCase();

						// Mettre � jour l'argent du joueur
						addMoney(objPiece.obtenirMonnaie());

						// Enlever la pi�ce de la case du plateau de jeu
						objCaseCouleurDestination.definirObjetCase(null);

						collision = "piece";

						// TODO: Il faut peut-�tre lancer un algo qui va placer
						// 		 les pi�ces sur le plateau de jeu s'il n'y en n'a
						//		 plus

					} else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Magasin) {
						// D�finir la collision
						collision = "magasin";

						// D�finir la r�f�rence vers le magasin rencontr�
						objMagasinRencontre = (Magasin)objCaseCouleurDestination.obtenirObjetCase();
					}
				}

				// S'il y a un objet � subir sur la case, alors on va faire une
				// certaine action (TODO: � compl�ter)
				if (objCaseCouleurDestination.obtenirObjetArme() != null) {
					// Faire la r�f�rence vers l'objet utilisable
					ObjetUtilisable objObjetUtilisable = (ObjetUtilisable)objCaseCouleurDestination.obtenirObjetArme();

					// Garder la r�f�rence vers l'objet utilisable � subir
					objObjetSubi = objObjetUtilisable;

					//TODO: Faire une certaine action au joueur

					// Enlever l'objet subi de la case
					objCaseCouleurDestination.definirObjetArme(null);
				}

				//***********************************
				//for gametype tourmnament - bonus for finish line
				if (objTable.getGameType().equals("Tournament") || objTable.getGameType().equals("Course")) {
					int tracks = objTable.getRegles().getNbTracks();
					Point objPoint = new Point(objTable.getNbLines() - 1, objTable.getNbColumns() - 1);
					Point objPointFinish = new Point();

					// On v�rifie d'abord si le joueur a atteint le WinTheGame;
					boolean isOnThePointsOfFinish = false;

					for (int i = 0; i < tracks; i++) {
						objPointFinish.setLocation(objPoint.x, objPoint.y - i);
						if (objPositionJoueurDesiree.equals(objPointFinish)) {
							isOnThePointsOfFinish = true;
						}
					}


					if (isOnThePointsOfFinish && !wasOnFinish && objTable.getGameType().equals("Tournament")) {
						wasOnFinish = true;
						bonus = objTable.obtenirTempsRestant();
						addPoints(bonus);
					} else if (isOnThePointsOfFinish && !wasOnFinish && objTable.getGameType().equals("Course")) {
						wasOnFinish = true;
						bonus = objTable.obtenirTempsRestant();
						addPoints(bonus);
						// if all the humains is on the finish line we stop the game
						if (objTable.isAllTheHumainsOnTheFinish(objJoueurHumain)) {
							stopTheGame = true;
						}
					}
				} //************************************  end bonus
			}



			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, obtenirPointage(), obtenirArgent(), bonus);
			objRetour.definirObjetRamasse(objObjetRamasse);
			objRetour.definirObjetSubi(objObjetSubi);
			objRetour.definirNouvellePosition(objPositionJoueurDesiree);
			objRetour.definirCollision(collision);
			objRetour.definirMagasin(objMagasinRencontre);

			synchronized (objTable.obtenirListeJoueurs()) {
				// Pr�parer l'�v�nement de deplacement de personnage.
				// Cette fonction va passer les joueurs et cr�er un
				// InformationDestination pour chacun et ajouter l'�v�nement
				// dans la file de gestion d'�v�nements
				objTable.preparerEvenementJoueurDeplacePersonnage(objJoueurHumain.obtenirNomUtilisateur(), collision, positionJoueur, objPositionJoueurDesiree, obtenirPointage(), obtenirArgent(), bonus, "");

			}

			// Modifier la position, le pointage et l'argent et moveVisibility
			definirPositionJoueur(objPositionJoueurDesiree);
			setPointsFinalTime(objTable.obtenirTempsRestant());
			setTournamentBonus(bonus);
			setMoveVisibility(getMoveVisibility() + 1);

		} else { //bolReponseEstBonne == false

			setMoveVisibility(getMoveVisibility() - 1);

			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, obtenirPointage(), obtenirArgent(), bonus);

			// La question sera nulle pour les joueurs virtuels
			if (objQuestionCourante != null) {
				objRetour.definirExplications(objQuestionCourante.obtenirURLExplication());
			}
		}

		if (stopTheGame) {
			objTable.arreterPartie(""); //to do - cleaner end of game!!!!
		}

		objQuestionCourante = null;
		return objRetour;

	}// end method

	/**
	 * This method is used to cancel the question.
	 * The first use is for Banana - to cancel question if banana is applied
	 * when used read the question. Second after restart game.
	 *
	 */
	public void cancelPosedQuestion() {
		if( objQuestionCourante != null)
		{
			lstQuestionsRepondues.removeLast();
			objQuestionCourante = null;
		}
	}

	/*
	 * Retourne une r�f�rence vers la liste des objets ramass�s
	 */
	public HashMap<Integer, ObjetUtilisable> obtenirListeObjets() {
		return lstObjetsUtilisablesRamasses;
	}

	public void ajouterObjetUtilisableListe(ObjetUtilisable objObjetUtilisable) {
		lstObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);
	}

	/*
	 * Aller chercher une r�f�rence vers un objet de la liste des objets selon
	 * son id
	 */
	public ObjetUtilisable obtenirObjetUtilisable(int intObjetId) {

		for(ObjetUtilisable objObjet:lstObjetsUtilisablesRamasses.values())
		{
			if (objObjet instanceof ObjetUtilisable && objObjet.obtenirId() == intObjetId)
			{
				return objObjet;
			}
		}
		return null;
	}

	/*
	 * D�termine si le joueur poss�de un certain objet, permet
	 * de valider l'information envoy� par le client lorsqu'il utiliser l'objet
	 */
	public boolean joueurPossedeObjet(int id) {

		for(ObjetUtilisable objObjet:lstObjetsUtilisablesRamasses.values())
		{
			if (objObjet instanceof ObjetUtilisable && objObjet.obtenirId() == id)
			{
				return true;
			}
		}
		return false;
	}

	public GestionnaireEvenements obtenirGestionnaireEvenements() {
		return objGestionnaireEv;
	}

	public void enleverObjet(int intIdObjet, String strTypeObjet) {
		lstObjetsUtilisablesRamasses.remove(intIdObjet);
	}

	public Objet obtenirObjetCaseCourante() {
		// L'objet � retourn�
		Objet objObjet = null;  /// is not very good design .... 

		Case objCaseJoueur = objTable.getCase(objPositionJoueur.x, objPositionJoueur.y);

		// Si c'est une case couleur, retourner l'objet, sinon on va retourner null
		if (objCaseJoueur instanceof CaseCouleur) {
			objObjet = ((CaseCouleur)objCaseJoueur).obtenirObjetCase();
		}

		return objObjet;

	}

	public Point obtenirPositionJoueurDesiree() {
		return objPositionJoueurDesiree;
	}

	/**
	 * @return the bananaState
	 */
	public HumainPlayerBananaState getBananaState() {
		return bananaState;
	}

	public int obtenirDistanceAuFinish() {
		Point objPoint = objTable.getPositionPointFinish();
		return Math.abs(objPositionJoueur.x - objPoint.x) + Math.abs(objPositionJoueur.y - objPoint.y);
	}

	public BoiteQuestions getObjBoiteQuestions() {
		return objBoiteQuestions;
	}

	/**
	 * @return the moveVisibility
	 */
	public int getMoveVisibility() {
		return moveVisibility;
	}

	/**
	 * @param moveV the moveVisibility to set
	 */
	public void setMoveVisibility(int moveV) {
		this.moveVisibility = moveV;

		if (this.moveVisibility > 7 && this.brainiacState.isInBrainiac()) {
			this.moveVisibility = 7;
		} else if (this.moveVisibility > 6 && this.brainiacState.isInBrainiac() == false) {
			this.moveVisibility = 6;
		} else if (this.moveVisibility < 1) {
			this.moveVisibility = 1;
		}
	}

	public void setClothesColor(int colorCode) {
		this.clothesColor = colorCode;
	}

	public int getClothesColor() {
		return clothesColor;
	}

	/**
	 * @return the brainiacState
	 */
	public HumainPlayerBrainiacState getBrainiacState() {
		return brainiacState;
	}

	/**
	 * @param pointsFinalTime the pointsFinalTime to set
	 */
	public void setPointsFinalTime(int pointsFinalTime) {
		this.pointsFinalTime = pointsFinalTime;
	}

	/**
	 * @return the pointsFinalTime
	 */
	public int getPointsFinalTime() {
		return pointsFinalTime;
	}

	public double getRightAnswersStats() {
		double percents = 0.0;
		int total = 0;
		int right = 0;
		for (InformationQuestion iq : lstQuestionsRepondues)
			if (iq.answerStatus == iq.RIGHT_ANSWER)
			{
				total += 1; right += 1;
			}else if (iq.answerStatus == iq.WRONG_ANSWER)
			{
				total +=1;
			}
		if(total > 0)
			percents = (double)(right * 100 / total);
		return percents;
	}

	/**
	 * @param idDessin the idDessin to set
	 */
	 public void setIdDessin(int idDessin) {
		 this.idDessin = idDessin;
	 }

	 /**
	  * @return the idDessin
	  */
	 public int getIdDessin() {
		 return idDessin;
	 }

	 /**
	  * @param boiteQuestionsInfo the boiteQuestionsInfo to set
	  */
	 public void setBoiteQuestionsInfo() {
		 this.boiteQuestionsInfo = new StringBuffer();
		 String table = this.objTable.getTableName();
		 String joueur = this.objJoueurHumain.obtenirNomUtilisateur();

		 this.boiteQuestionsInfo.append("BoiteQuestions info's for " + joueur + " in the table " + table + "\n");

	 }

	 /**
	  * @return the boiteQuestionsInfo
	  */
	 public StringBuffer getBoiteQuestionsInfo() {
		 return boiteQuestionsInfo;
	 }

	 public void writeInfo(){
		 String table = this.objTable.getTableName();
		 String joueur = this.objJoueurHumain.obtenirNomUtilisateur();

		 this.boiteQuestionsInfo.append("END INFO ");
		 String info = this.boiteQuestionsInfo.toString();

		 //System.out.println("End info" + info.length());

		 BufferedWriter writer = null;
		 Date infoDate = new Date();

		 File file = new File("boiteInfo" + joueur + "_" + table + "_" + infoDate.getTime() + ".txt");
		 try {
			 writer = new BufferedWriter(new FileWriter(file));
			 writer.write(info);
			 writer.flush();
		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }

	 }

	 public Integer resetColor() {
		 int temp = this.clothesColor;
		 this.clothesColor = 0;
		 return temp;
	 }

	 public void setColorID() {
		 this.clothesColor = objTable.getOneColor();
	 }


	 /**
	  * @return the objGestionnaireBD
	  */
	 public GestionnaireBDJoueur getObjGestionnaireBD() {
		 return objGestionnaireBD;
	 }
} // end class

