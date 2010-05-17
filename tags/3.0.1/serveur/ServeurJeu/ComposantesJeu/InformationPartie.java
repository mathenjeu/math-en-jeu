package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.PlayerBananaState;
import ServeurJeu.ComposantesJeu.Joueurs.PlayerBraniacState;
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ServeurJeu.ControleurJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class InformationPartie
{
	// Déclaration d'une référence vers le gestionnaire de bases de données
	private GestionnaireBD objGestionnaireBD;
	
    // Déclaration d'une référence vers le gestionnaire d'evenements
	private GestionnaireEvenements objGestionnaireEv;
	
	// Déclaration d'une référence vers un joueur humain correspondant ˆ cet
	// objet d'information de partie
	private JoueurHumain objJoueurHumain;
	
	// Déclaration d'une référence vers la table courante
	private Table objTable;
	
    // Déclaration d'une variable qui va contenir le numéro Id du personnage 
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
	private TreeMap<Integer, Question> lstQuestionsRepondues;
	
	// Déclaration d'une variable qui va garder la question qui est 
	// présentement posée au joueur. S'il n'y en n'a pas, alors il y a 
	// null dans cette variable
	private Question objQuestionCourante;
	
	// Déclaration d'une liste d'objets utilisables ramassés par le joueur
	private TreeMap<Integer, ObjetUtilisable> lstObjetsUtilisablesRamasses;
        
    // Déclaration de la boîte de question personnelle au joueur possédant
    // cet objet
    private BoiteQuestions objBoiteQuestions;
        
    // object that describe and manipulate 
    // the Banana state of the player
    private PlayerBananaState bananaState;
    
    
    // object that describe and manipulate 
    // the Braniac state of the player
    private PlayerBraniacState braniacState;
        
	// If is true intArgent is taken from DB and at the end 
    //of the game is writen to the DB
    private boolean moneyPermit;
    
    // to not get twice bonus
    // used in course ou tournament types of game
    private boolean wasOnFinish;

    // The number of cases on that user can to move. At the begining is set to 3.
	// After 3 correct answers add one unity. Not bigger than 6, but 
    // in the case of Braniac is possible to have 7 cases. 
	private int moveVisibility;
	
	// Number for bonus in Tournament type of game
	// Bonus is given while arrived at finish line and is calculated
	// as number of rested sec to game time
	private int tournamentBonus;
	
	// the color of the clothes in the player's picture
	// user can change it in the frame 3 of the client
	// if we use default color it will remain = 0
	private String clothesColor;
	 
    
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
	        
	        // is permited or not to charge money from DB
	        setMoneyPermit(objGestionnaireBD.getMoneyRule(joueur.obtenirSalleCourante().getRoomName(joueur.obtenirProtocoleJoueur().langue)));
	        
	        // charge money from DB if is permited
	        if (isMoneyPermit()){
	        	intArgent = objGestionnaireBD.getPlayersMoney(joueur.obtenirCleJoueur());
		    }else {
		       	intArgent = 0;
		    }
	        	        
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
	        lstQuestionsRepondues = new TreeMap<Integer, Question>();
	    
	        // Créer la liste des objets utilisables qui ont été ramassés
	        lstObjetsUtilisablesRamasses = new TreeMap<Integer, ObjetUtilisable>();
	        
	        wasOnFinish = false;
	        
	        moveVisibility = 3;
			tournamentBonus = 0;
			
			// set the color to default
			clothesColor = "0";
									
			// Braniac state
			this.braniacState = new PlayerBraniacState(joueur);
			
			// Banana state
			this.bananaState = new PlayerBananaState(joueur);
	        
			String language = joueur.obtenirProtocoleJoueur().langue;
            setObjBoiteQuestions(new BoiteQuestions(language, objGestionnaireBD.transmitUrl(language)));
            objGestionnaireBD.remplirBoiteQuestions(getObjBoiteQuestions(), objJoueurHumain);  
            
	}// fin constructeur

	
	
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
	public TreeMap<Integer, Question> obtenirListeQuestionsRepondues()
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
	 * de difficulté pour la salle. La distance minimale ˆ parcourir est 1.
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
		// ˆ la position actuelle
		if (bolEstPermis == true && nouvellePosition.x != objPositionJoueur.x && nouvellePosition.y != objPositionJoueur.y)
		{
			bolEstPermis = false;
		}

		// Si la distance parcourue dépasse le nombre de cases maximal possible, alors il y a une erreur
		// If we are in the Braniac maximal cases = + 1
		if(this.braniacState.isInBraniac()){
			
			if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.getObjSalle().getRegles().obtenirDeplacementMaximal() + 1) || 
					(nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.getObjSalle().getRegles().obtenirDeplacementMaximal() + 1)))
			{
				bolEstPermis = false;
			}
		}else{
			
			if (bolEstPermis == true && ((nouvellePosition.x != objPositionJoueur.x && Math.abs(nouvellePosition.x - objPositionJoueur.x) > objTable.getObjSalle().getRegles().obtenirDeplacementMaximal()) || 
					(nouvellePosition.y != objPositionJoueur.y && Math.abs(nouvellePosition.y - objPositionJoueur.y) > objTable.getObjSalle().getRegles().obtenirDeplacementMaximal())))
			{
				bolEstPermis = false;
			}
		}
		
		// Si le déplacement est toujours permis jusqu'a maintenant, alors on 
		// va vérifier qu'il n'y a pas de trous séparant le joueur de la 
		// position qu'il veut aller
		if (bolEstPermis == true)
		{
			// Si on se déplace vers la gauche
			if (nouvellePosition.x != objPositionJoueur.x && nouvellePosition.x > objPositionJoueur.x)
			{
				// On commence le déplacement ˆ la case juste ˆ gauche de la 
				// position courante
				int i = objPositionJoueur.x + 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.x && bolEstPermis == true)
				{
					// S'il n'y a aucune case ˆ la position courante, alors on 
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
				// On commence le déplacement ˆ la case juste ˆ droite de la 
				// position courante
				int i = objPositionJoueur.x - 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.x && bolEstPermis == true)
				{
					// S'il n'y a aucune case ˆ la position courante, alors on 
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
				// On commence le déplacement ˆ la case juste en bas de la 
				// position courante
				int i = objPositionJoueur.y + 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i <= nouvellePosition.y && bolEstPermis == true)
				{
					// S'il n'y a aucune case ˆ la position courante, alors on 
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
				// On commence le déplacement ˆ la case juste en haut de la 
				// position courante
				int i = objPositionJoueur.y - 1;
				
				// On boucle tant qu'on n'a pas atteint la case de destination
				// et qu'on a pas eu de trous
				while (i >= nouvellePosition.y && bolEstPermis == true)
				{
					// S'il n'y a aucune case ˆ la position courante, alors on 
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
	} // fin méthode
	
	
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
		int intDifficulte = 0;
        //int grandeurDeplacement = 0;
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

		//System.out.println("Difficulte de la question : " + intDifficulte);   // test

		// if is under Banana effects
		if(this.bananaState.isUnderBananaEffects() && intDifficulte < 6)
			intDifficulte++;
		// if is under Braniac effects
		if(this.braniacState.isInBraniac() && intDifficulte > 1 )
			intDifficulte--;

		if(intDifficulte > 6) intDifficulte = 6;
		//System.out.println("Difficulte de la question2 : " + intDifficulte);   // test

		// Il faut que la difficulté soit plus grande que 0 pour pouvoir trouver 
		// une question
		if (intDifficulte > 0)
		{
			objQuestionTrouvee = trouverQuestion(intDifficulte);
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
			objBoiteQuestions.popQuestion(objQuestionTrouvee);
		}
		else if (intDifficulte > 0)
		{
			objGestionnaireBD.remplirBoiteQuestions( getObjBoiteQuestions(), objJoueurHumain);
			objQuestionTrouvee = trouverQuestion(intDifficulte);
			
			lstQuestionsRepondues.clear();
			
			// S'il y a eu une question trouvée, alors on l'ajoute dans la liste 
			// des questions posées et on la garde en mémoire pour pouvoir ensuite
			// traiter la réponse du joueur
			if (objQuestionTrouvee != null)
			{
				lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
				objQuestionCourante = objQuestionTrouvee;
				objPositionJoueurDesiree = nouvellePosition;
				objBoiteQuestions.popQuestion(objQuestionTrouvee);
			}
			else
			{
				// en théorie on ne devrait plus entrer dans ce else 
				System.out.println( "‚a va mal : aucune question" );
			}
		}
		
		// Si on doit générer le numéro de commande de retour, alors
		// on le génêre, sinon on ne fait rien (ùa devrait toujours
		// être vrai, donc on le génêre tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// Générer un nouveau numéro de commande qui sera 
		    // retourné au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		return objQuestionTrouvee;
	}
	
	
	/**
	 * Methode used if player use the Cristal ball
     * int intDifficulte - level of the last question. The new question must be < difficult	
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						générer un numéro de commande à retourner
	 * @return Question : La question trouvée, s'il n'y a pas eu de déplacement,
	 * 					  alors la question retournée est null
	 */
	public Question trouverQuestionAPoserCristall(JoueurHumain objJoueurHumain, boolean doitGenererNoCommandeRetour)
	{
		// Déclarations de variables qui vont contenir la catégorie de question 
		// à poser, la difficulté et la question à retourner
		//***************************************************************************************
	   int oldQuestion = objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante().obtenirCodeQuestion();
	   int intDifficulte = objJoueurHumain.obtenirPartieCourante().obtenirQuestionCourante().obtenirDifficulte();


	   Question objQuestionTrouvee = null;


		if (intDifficulte > 1)
			intDifficulte--;
		//if is Banana used to this player
		//if(!isUnderBananaEffect.equals(""))
		//	intDifficulte++;
		
		if (intDifficulte > 0)
		{
		   objQuestionTrouvee = trouverQuestionCristall(intDifficulte, oldQuestion);
		}
				
		// S'il y a eu une question trouvée, alors on l'ajoute dans la liste 
		// des questions posées et on la garde en mémoire pour pouvoir ensuite
		// traiter la réponse du joueur, on va aussi garder la position que le
		// joueur veut se déplacer
		if (objQuestionTrouvee != null)
		{
			lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
			objQuestionCourante = objQuestionTrouvee;
			//objPositionJoueurDesiree = nouvellePosition;
			objBoiteQuestions.popQuestion(objQuestionTrouvee);
		}
		else 
		{
			objGestionnaireBD.remplirBoiteQuestions( getObjBoiteQuestions(), objJoueurHumain);
			
			objQuestionTrouvee = trouverQuestionCristall(intDifficulte, oldQuestion);
			
			lstQuestionsRepondues.clear();
			
			// S'il y a eu une question trouvée, alors on l'ajoute dans la liste 
			// des questions posées et on la garde en mémoire pour pouvoir ensuite
			// traiter la réponse du joueur
			if (objQuestionTrouvee != null)
			{
				lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
				objQuestionCourante = objQuestionTrouvee;
				//objPositionJoueurDesiree = nouvellePosition;
				objBoiteQuestions.popQuestion(objQuestionTrouvee);
			}
			else
			{
				// en théorie on ne devrait plus entrer dans ce else 
				System.out.println( "‚a va mal : aucune question" );
			}
		}
		
		// Si on doit générer le numéro de commande de retour, alors
		// on le génêre, sinon on ne fait rien (ùa devrait toujours
		// être vrai, donc on le génêre tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// Générer un nouveau numéro de commande qui sera 
		    // retourné au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		return objQuestionTrouvee;
	}// end methode
	
	/**
	 * Created for the case of Cristall
	 * Cette fonction essaie de piger une question du niveau de dificulté proche 
	 * de intDifficulte, si on y arrive pas, ça veut dire qu'il ne 
	 * reste plus de questions de niveau de difficulté proche 
	 * de intDifficulte
	 * 
	 * @param intDifficulte
	 * @return la question trouver ou null si aucune question n'a pu être pigée
	 */
	private Question trouverQuestionCristall(int intDifficulte, int codeOld)
	{
		
		Question objQuestionTrouvee = null;
		
		// to not get the same question
		// pour le premier on voir la catégorie et difficulté demandées

		objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulte, codeOld);

		
		//après pour les difficultés moins grands 
		int intDifficulteTemp = intDifficulte;

		while(objQuestionTrouvee == null && intDifficulteTemp > 0 ) 
		{
			intDifficulteTemp--;
			objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulteTemp, codeOld);

		}// fin while
		
		//au pire cas les difficultés plus grands 
		intDifficulteTemp = intDifficulte;

		while(objQuestionTrouvee == null && intDifficulteTemp < 7 ) 
		{
			intDifficulteTemp++;
			objQuestionTrouvee = getObjBoiteQuestions().pigerQuestionCristall(intDifficulteTemp, codeOld);

		}// fin while

		//System.out.println(" verification " + objQuestionTrouvee);
		return objQuestionTrouvee;
		
	}// fin méthode
	
	/**
	 * Cette fonction essaie de piger une question du niveau de dificulté proche 
	 * de intDifficulte, si on y arrive pas, ça veut dire qu'il ne 
	 * reste plus de questions de niveau de difficulté proche 
	 * de intDifficulte
	 * 
	 * @param intCategorieQuestion
	 * @return la question trouver ou null si aucune question n'a pu être pigée
	 */
	private Question trouverQuestion(int intDifficulte)
	{
		Question objQuestionTrouvee = null;
				
		// pour le premier on voir la catégorie et difficulté demandées
		objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion(intDifficulte);
			
		//après pour les difficultés moins grands 
		int intDifficulteTemp = intDifficulte;
		        
		while(objQuestionTrouvee == null && intDifficulteTemp > 0 ) 
		{
			intDifficulteTemp--;
			objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion( intDifficulteTemp);
		   	
		}// fin while
		
		//après pour les difficultés plus grands 
		intDifficulteTemp = intDifficulte;
		while(objQuestionTrouvee == null &&  intDifficulteTemp < 7 ) 
		{
			intDifficulteTemp++;
			objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion( intDifficulteTemp);
		   	
		}// fin while      
			
		return objQuestionTrouvee;
		
	}// fin méthode
	
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
        int bonus = 0;
		Table table;
		int intDifficulteQuestion;
		TreeMap<Integer, ObjetUtilisable> objListeObjetsUtilisablesRamasses; 
		Point positionJoueur; 
		GestionnaireEvenements gestionnaireEv;
		Question objQuestion; 
		String nomJoueur; 
		boolean bolReponseEstBonne;
		boolean boolWasOnFinish = false;
		int intNouveauPointage = 0;
		int deplacementJoueur = 0;
		
		
		
		
		// Obtenir les divers informations à utiliser dépendamment de si
		// la fonction s'applique à un joueur humain ou un joueur virtuel
		if (objJoueur instanceof JoueurHumain)
		{
			InformationPartie objPartieCourante = ((JoueurHumain)objJoueur).obtenirPartieCourante();
			
			// Obtenir les informations du joueur humain
			intPointageCourant = objPartieCourante.obtenirPointage();
            intArgentCourant = objPartieCourante.obtenirArgent();
            bonus = objPartieCourante.getTournamentBonus();
		    table = objPartieCourante.obtenirTable();
		    intDifficulteQuestion = objPartieCourante.obtenirQuestionCourante().obtenirDifficulte();
		    objListeObjetsUtilisablesRamasses = objPartieCourante.obtenirListeObjets();
		    positionJoueur = objPartieCourante.obtenirPositionJoueur();
		    gestionnaireEv = objPartieCourante.obtenirGestionnaireEvenements();
		    objQuestion = objPartieCourante.obtenirQuestionCourante();
		    nomJoueur = ((JoueurHumain)objJoueur).obtenirNomUtilisateur();
		    boolWasOnFinish = objPartieCourante.wasOnFinish;
		    
		    
		    // Si la position en x est différente de celle désirée, alors
	        // c'est qu'il y a eu un déplacement sur l'axe des x
	        if (positionJoueur.x != objPositionDesiree.x)
	        {
	        	deplacementJoueur = Math.abs(objPositionDesiree.x - positionJoueur.x);
	        }
	        // Si la position en y est différente de celle désirée, alors
	        // c'est qu'il y a eu un déplacement sur l'axe des y
	        else if (positionJoueur.y != objPositionDesiree.y)
	        {
	        	deplacementJoueur = Math.abs(objPositionDesiree.y - positionJoueur.y);
	        }
		    
	        if(deplacementJoueur == 1 && objPartieCourante.bananaState.isUnderBananaEffects())
	        	intNouveauPointage -= 1;
	        	 
		            // If we're in debug mode, accept any answer
                    if(ControleurJeu.modeDebug)
                    {
                        bolReponseEstBonne = true;
                    }
                    else
                    {
                        bolReponseEstBonne = objQuestion.reponseEstValide(reponse);
                    }		    
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
		    
		    // Si la position en x est différente de celle désirée, alors
	        // c'est qu'il y a eu un déplacement sur l'axe des x
	        if (positionJoueur.x != objPositionDesiree.x)
	        {
	        	deplacementJoueur = Math.abs(objPositionDesiree.x - positionJoueur.x);
	        }
	        // Si la position en y est différente de celle désirée, alors
	        // c'est qu'il y a eu un déplacement sur l'axe des y
	        else if (positionJoueur.y != objPositionDesiree.y)
	        {
	        	deplacementJoueur = Math.abs(objPositionDesiree.y - positionJoueur.y);
	        }
		    
	        if(deplacementJoueur == 1 && objJoueurVirtuel.getBananaState().isUnderBananaEffects())
	        	intNouveauPointage -= 1;
	        
		    // Pas de question pour les joueurs virtuels
		    objQuestion = null;
		    nomJoueur = objJoueurVirtuel.obtenirNom();
		    
		    // On appelle jamais cette fonction si le joueur virtuel rate 
		    // la question
		    bolReponseEstBonne = true;

		}
		
		// Le nouveau pointage est initialement le pointage courant
		intNouveauPointage += intPointageCourant;
                
        int intNouvelArgent = intArgentCourant;
		
		// Déclaration d'une référence vers l'objet ramassé
		ObjetUtilisable objObjetRamasse = null;
		
		// Déclaration d'une référence vers l'objet subi
		ObjetUtilisable objObjetSubi = null;
		
		String collision = "";
		
		// Déclaration d'une référence vers le magasin recontré
		Magasin objMagasinRencontre = null;
		
		// Si la réponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne)
		{
			
			
			// Faire la référence vers la case de destination
			Case objCaseDestination = table.obtenirPlateauJeuCourant()[objPositionDesiree.x][objPositionDesiree.y];
			
			// Calculer le nouveau pointage du joueur
                        switch(deplacementJoueur)
                        {
                            case 1:
                                intNouveauPointage += 2;
                                break;
                            case 2:
                                intNouveauPointage += 3;
                                break;
                            case 3:
                                intNouveauPointage += 5;
                                break;
                            case 4:
                                intNouveauPointage += 8;
                                break;
                            case 5:
                                intNouveauPointage += 13;
                                break;
                            case 6:
                                intNouveauPointage += 21;
                                break;
                            case 7:
                                intNouveauPointage += 34;
                                break;
                        }
			
			// Si la case de destination est une case de couleur, alors on 
			// vérifie l'objet qu'il y a dessus et si c'est un objet utilisable, 
			// alors on l'enlêve et on le donne au joueur, sinon si c'est une 
			// piêce on l'enlêve et on met à jour le pointage du joueur, sinon 
			// on ne fait rien
			if (objCaseDestination instanceof CaseCouleur)
			{
				// Faire la référence vers la case de couleur
				CaseCouleur objCaseCouleurDestination = (CaseCouleur) objCaseDestination;
				
				// S'il y a un objet sur la case, alors on va faire l'action 
				// tout dépendant de l'objet (piêce, objet utilisable ou autre)
				if (objCaseCouleurDestination.obtenirObjetCase() != null)
				{
					// Si l'objet est un objet utilisable, alors on l'ajoute à 
					// la liste des objets utilisables du joueur
					if (objCaseCouleurDestination.obtenirObjetCase() instanceof ObjetUtilisable)
					{

						if (objCaseCouleurDestination.obtenirObjetCase() instanceof Braniac)
						{
							
							// put the player on the Braniac state
							if (objJoueur instanceof JoueurHumain)
							{
								((JoueurHumain) objJoueur).obtenirPartieCourante().getBraniacState().putTheOneBraniac();
								table.preparerEvenementUtiliserObjet(((JoueurHumain) objJoueur).obtenirNomUtilisateur(), ((JoueurHumain) objJoueur).obtenirNomUtilisateur(), "Braniac", "");
								
							}
							else if (objJoueur instanceof JoueurVirtuel)
							{
								((JoueurVirtuel)objJoueur).getBraniacState().putTheOneBraniac();
								table.preparerEvenementUtiliserObjet(((JoueurVirtuel) objJoueur).obtenirNom(), ((JoueurVirtuel) objJoueur).obtenirNom(), "Braniac", "");
								
							}
							
							// Enlever l'objet de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							// On va dire aux clients qu'il y a eu collision avec cet objet
							collision = "Braniac";
							
						}else{
							// Faire la référence vers l'objet utilisable
							ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetCase();

							// Garder la référence vers l'objet utilisable pour l'ajouter à l'objet de retour
							objObjetRamasse = objObjetUtilisable;

							// Ajouter l'objet ramassé dans la liste des objets du joueur courant
							objListeObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);

							// Enlever l'objet de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							// On va dire aux clients qu'il y a eu collision avec cet objet
							collision = objObjetUtilisable.obtenirTypeObjet();
						}
						
					}
					else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece)
					{
						
							// Faire la référence vers la piêce
							Piece objPiece = (Piece) objCaseCouleurDestination.obtenirObjetCase();

							// Mettre à jour l'argent du joueur
							intNouvelArgent += objPiece.obtenirMonnaie();

							// Enlever la piêce de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							collision = "piece";

							// TODO: Il faut peut-être lancer un algo qui va placer 
							// 		 les piêces sur le plateau de jeu s'il n'y en n'a
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
				
				//***********************************
				//for gametype tourmnament - bonus for finish line
				 if(table.getObjSalle().getGameType().equals("Tournament")||table.getObjSalle().getGameType().equals("Course"))
				 {
					 int tracks = table.getObjSalle().getRegles().getNbTracks();
					 Point  objPoint = new Point(table.getNbLines() - 1, table.getNbColumns() - 1);
					 Point objPointFinish = new Point();
					 
					 // On vérifie d'abord si le joueur a atteint le WinTheGame;
					 boolean isOnThePointsOfFinish = false;
				 	 
                     	 			 
		 			 if(objJoueur instanceof JoueurHumain)
		 			 {

		 				 for(int i = 0; i < tracks; i++ )
						 {
							 objPointFinish.setLocation(objPoint.x, objPoint.y - i);
							 if(objPositionDesiree.equals(objPointFinish))
								 isOnThePointsOfFinish = true;
						 }
					 	 
		 				 
		 				 if(isOnThePointsOfFinish && !boolWasOnFinish && table.getObjSalle().getGameType().equals("Tournament"))
		 				 {
		 					 ((JoueurHumain)objJoueur).obtenirPartieCourante().wasOnFinish = true;
		 					 bonus = table.obtenirTempsRestant();
		 					 intNouveauPointage += bonus; 
		 				 }
		 				 else if (isOnThePointsOfFinish && !boolWasOnFinish && table.getObjSalle().getGameType().equals("Course"))
		 				 {
		 					((JoueurHumain)objJoueur).obtenirPartieCourante().wasOnFinish = true;
		 					bonus = table.obtenirTempsRestant();
		 					intNouveauPointage += bonus; 
		 					// if all the humains is on the finish line we stop the game
		 					if(table.isAllTheHumainsOnTheFinish((JoueurHumain)objJoueur))
		 						 table.arreterPartie(((JoueurHumain)objJoueur).obtenirNomUtilisateur());
		 				 }
		 			 }
		 			 else if (objJoueur instanceof JoueurVirtuel)
		 			 {
		 				 boolWasOnFinish = ((JoueurVirtuel)objJoueur).isPlayerNotArrivedOnce();
		 				 for(int i = 0; i < tracks; i++ )
						 {
							 objPointFinish.setLocation(objPoint.x, objPoint.y - i);
							 if(objPositionDesiree.equals(objPointFinish))
								 isOnThePointsOfFinish = true;
						 }
					 	 
		 				 if(isOnThePointsOfFinish && boolWasOnFinish )
		 				 {
		 				    ((JoueurVirtuel)objJoueur).setPlayerNotArrivedOnce(false);
		 				    bonus = table.obtenirTempsRestant();
		 				    intNouveauPointage += bonus; 
		 				 }
		 			 }

				 }
				//************************************  end bonus
			}
			
			// Créer l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent, bonus);
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
				table.preparerEvenementJoueurDeplacePersonnage(nomJoueur, collision, positionJoueur, objPositionDesiree, intNouveauPointage, intNouvelArgent, bonus, "");
						    	
		    }
		    
			// Modifier la position, le pointage et l'argent et moveVisibility
			if (objJoueur instanceof JoueurHumain)
			{
				((JoueurHumain)objJoueur).obtenirPartieCourante().definirPositionJoueur(objPositionDesiree);
				((JoueurHumain)objJoueur).obtenirPartieCourante().definirPointage(intNouveauPointage);
				((JoueurHumain)objJoueur).obtenirPartieCourante().definirArgent(intNouvelArgent);
				((JoueurHumain)objJoueur).obtenirPartieCourante().setTournamentBonus(bonus);
				
					((JoueurHumain)objJoueur).obtenirPartieCourante().setMoveVisibility(((JoueurHumain)objJoueur).obtenirPartieCourante().getMoveVisibility() + 1);
			
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
			//((JoueurHumain)objJoueur).obtenirPartieCourante().setRunningAnswers(0);
			((JoueurHumain)objJoueur).obtenirPartieCourante().setMoveVisibility(((JoueurHumain)objJoueur).obtenirPartieCourante().getMoveVisibility() - 1);
			
			// Créer l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent, bonus);
			
			// La question sera nulle pour les joueurs virtuels
			if (objQuestion != null)
			{
				objRetour.definirExplications(objQuestion.obtenirURLExplication());
			}
		}
		 
		return objRetour;
		
	}// end method
	
	/**
	 * This method is used to cancel the question. 
	 * The first use is for Banana - to cancel question if banana is applied
	 * then used read the question. 
	 *  
	 */
	public void cancelPosedQuestion(boolean doitGenererNoCommandeRetour)
	{
		// Si on doit générer le numéro de commande de retour, alors
		// on le génêre, sinon on ne fait rien (ùa devrait toujours
		// être vrai, donc on le génêre tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// Générer un nouveau numéro de commande qui sera 
		    // retourné au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		getObjBoiteQuestions().popQuestion(objQuestionCourante);
		objQuestionCourante = null;
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
		// on le génêre, sinon on ne fait rien (ùa devrait toujours
		// être vrai, donc on le génêre tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// Générer un nouveau numéro de commande qui sera 
		    // retourné au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		getObjBoiteQuestions().popQuestion(objQuestionCourante);
		objQuestionCourante = null;

		return objRetour;
	}
	
	/*
	 * Retourne une référence vers la liste des objets ramassés
	 */
	public TreeMap<Integer, ObjetUtilisable> obtenirListeObjets()
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
	     Set<Map.Entry<Integer,ObjetUtilisable>> lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
	     Iterator<Entry<Integer, ObjetUtilisable>> objIterateurListeObjets = lstEnsembleObjets.iterator();
	     while (objIterateurListeObjets.hasNext() == true)
	     {
	     	Objet objObjet = (Objet)(((Map.Entry<Integer,ObjetUtilisable>)(objIterateurListeObjets.next())).getValue());
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
	 * Détermine si le joueur possêde un certain objet, permet
	 * de valider l'information envoyé par le client lorsqu'il utiliser l'objet
	 */
	 public boolean joueurPossedeObjet(int id)
	 {
	     // Préparation pour parcourir la liste d'objets
	     Set<Map.Entry<Integer,ObjetUtilisable>> lstEnsembleObjets = lstObjetsUtilisablesRamasses.entrySet();
	     Iterator<Entry<Integer, ObjetUtilisable>> objIterateurListeObjets = lstEnsembleObjets.iterator();
	     
	     // Parcours du TreeMap
	     while (objIterateurListeObjets.hasNext() == true)
	     {
	     	Objet objObjet = (Objet)(((Map.Entry<Integer,ObjetUtilisable>)(objIterateurListeObjets.next())).getValue());
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
	
        public Point obtenirPositionJoueurDesiree()
        {
            return objPositionJoueurDesiree;
        }
        
        public GestionnaireBD obtenirGestionnaireBD()
        {
            return objGestionnaireBD;
        }
        
             
        /**
		 * @return the bananaState
		 */
		public PlayerBananaState getBananaState() {
			return bananaState;
		}



		/**
		 * @param bananaState the bananaState to set
		 */
		public void setBananaState(PlayerBananaState bananaState) {
			this.bananaState = bananaState;
		}

        public int obtenirDistanceAuFinish()
        {
            Point objPoint = objTable.getPositionPointFinish();
        	return Math.abs(objPositionJoueur.x - objPoint.x) + Math.abs(objPositionJoueur.y - objPoint.y);
        }

     
        public void setMoneyPermit(boolean moneyPermit) {
			this.moneyPermit = moneyPermit;
		}

		public boolean isMoneyPermit() {
			return moneyPermit;
		}

		public void setObjBoiteQuestions(BoiteQuestions objBoiteQuestions) {
			this.objBoiteQuestions = objBoiteQuestions;
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
		 * @param moveVisibility the moveVisibility to set
		 */
		public void setMoveVisibility(int moveV) {
			this.moveVisibility = moveV;
			
			if (this.moveVisibility > 7 && this.braniacState.isInBraniac()){
				this.moveVisibility = 7;
			}else if (this.moveVisibility > 6 && this.braniacState.isInBraniac() == false){
				this.moveVisibility = 6;
			}else if (this.moveVisibility < 1){
				this.moveVisibility = 1;
			}
		}

			
		public void setClothesColor(String string) {
			this.clothesColor = string;
		}

		public String getClothesColor() {
			return clothesColor;
		}



		/**
		 * @return the braniacState
		 */
		public PlayerBraniacState getBraniacState() {
			return braniacState;
		}



		/**
		 * @param braniacState the braniacState to set
		 */
		public void setBraniacState(PlayerBraniacState braniacState) {
			this.braniacState = braniacState;
		}

			
}
