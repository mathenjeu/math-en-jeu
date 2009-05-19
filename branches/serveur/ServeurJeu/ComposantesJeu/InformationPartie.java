package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
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
import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.*;
import ServeurJeu.ComposantesJeu.Objets.Pieces.Piece;
import ClassesRetourFonctions.RetourVerifierReponseEtMettreAJourPlateauJeu;
import ClassesUtilitaires.UtilitaireNombres;
import Enumerations.Categories;
import ServeurJeu.ControleurJeu;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class InformationPartie
{
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private GestionnaireBD objGestionnaireBD;
	
    // D�claration d'une r�f�rence vers le gestionnaire d'evenements
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
        
    // Combien d'argent ce joueur a-t-il?
    private int intArgent;
	
	// D�claration d'une position du joueur dans le plateau de jeu
	private Point objPositionJoueur;
	
	// D�claration d'un point qui va garder la position o� le joueur
	// veut aller
	private Point objPositionJoueurDesiree;
	
	// D�claration d'une liste de questions qui ont �t� r�pondues 
	// par le joueur
	private TreeMap<Integer, Question> lstQuestionsRepondues;
	
	// D�claration d'une variable qui va garder la question qui est 
	// pr�sentement pos�e au joueur. S'il n'y en n'a pas, alors il y a 
	// null dans cette variable
	private Question objQuestionCourante;
	
	// D�claration d'une liste d'objets utilisables ramass�s par le joueur
	private TreeMap<Integer, ObjetUtilisable> lstObjetsUtilisablesRamasses;
        
    // D�claration de la bo�te de question personnelle au joueur poss�dant
    // cet objet
    private BoiteQuestions objBoiteQuestions;
        
    // D�claration d'un boolean qui dit si le joueur est 'target�' pour subir une banane
    // (si le string n'est pas "", et alors le string dit qui l'a utilis�e)
    private String vaSubirUneBanane;
        
    // D�claration d'un int qui va garder le mode du jeu : Normal - 0 et Avanc�e - 1
    //private int modeJeu;
    
    // If is true intArgent is taken from DB and at the end 
    //of the game is writen to the DB
    private boolean moneyPermit;
    
    private static int maxNbObj;
    
    // to not get twice bonus
    private boolean isPlayerNotArrivedOnce; 
     
	 
	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propri�t�s de la partie et de faire la r�f�rence vers la table.
	 */
	public InformationPartie( GestionnaireEvenements gestionnaireEv, GestionnaireBD gestionnaireBD, JoueurHumain joueur, Table tableCourante)
	{
            maxNbObj = tableCourante.obtenirRegles().getMaxNbObjectsAndMoney();    
		
		    // Au d�but, on ne subit pas de banane!
            vaSubirUneBanane = "";
            
            // Faire la r�f�rence vers le gestionnaire de base de donn�es
            objGestionnaireBD = gestionnaireBD;

            // Faire la r�f�rence vers le gestionnaire d'evenements
            objGestionnaireEv = gestionnaireEv;

            // Faire la r�f�rence vers le joueur humain courant
            objJoueurHumain = joueur;
		
	        // D�finir les propri�t�s de l'objet InformationPartie
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
	        
	        
	        // Faire la r�f�rence vers la table courante
	        objTable = tableCourante;
	    
	        // Au d�part, le joueur est nul part
	        objPositionJoueur = null;
	    
	        // Au d�part, le joueur ne veut aller nul part
	        objPositionJoueurDesiree = null;
	    
	        // Au d�part, aucune question n'est pos�e au joueur
	        objQuestionCourante = null;
	    
	        // Cr�er la liste des questions qui ont �t� r�pondues
	        lstQuestionsRepondues = new TreeMap<Integer, Question>();
	    
	        // Cr�er la liste des objets utilisables qui ont �t� ramass�s
	        lstObjetsUtilisablesRamasses = new TreeMap<Integer, ObjetUtilisable>();
	        
	        isPlayerNotArrivedOnce = true;
	        
	        

	        String language = joueur.obtenirProtocoleJoueur().langue;
            setObjBoiteQuestions(new BoiteQuestions(language, objGestionnaireBD.transmitUrl(language) , joueur));
            objGestionnaireBD.remplirBoiteQuestions(getObjBoiteQuestions(), objJoueurHumain.obtenirCleNiveau());  
            
	}// fin constructeur

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
	 * Cette fonction permet de retourner l'argent du joueur.
	 * 
	 * @return int : L'argent du joueur courant
	 */
	public int obtenirArgent()
	{
	   return intArgent;
	}
	
	/**
	 * Cette fonction permet de red�finir l'argent du joueur.
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
	public TreeMap<Integer, Question> obtenirListeQuestionsRepondues()
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
	} // fin m�thode
	
	
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
		//***************************************************************************************
		
		Categories[] catValues = Categories.values();
        int[] catScolaires = new int[catValues.length];
        //System.out.println("catValues.length : " + catValues.length);
        for(int i = 0; i < catValues.length; i++)
		{
			catScolaires[i] = catValues[i].getCode();
						
		}
		
		int intCategorieQuestion = catScolaires[UtilitaireNombres.genererNbAleatoire(catValues.length - 1)]; 
		
		//***************************************************************************************
		
		int intDifficulte = 0;
        //int grandeurDeplacement = 0;
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
                
                System.out.println("Difficulte de la question : " + intDifficulte);   // test
		
		// Il faut que la difficult� soit plus grande que 0 pour pouvoir trouver 
		// une question
		if (intDifficulte > 0)
		{
			objQuestionTrouvee = trouverQuestion(intCategorieQuestion, intDifficulte, true);
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
			objGestionnaireBD.remplirBoiteQuestions( getObjBoiteQuestions(), objJoueurHumain.obtenirCleNiveau());
			objQuestionTrouvee = trouverQuestion(intCategorieQuestion, intDifficulte, true);
			
			lstQuestionsRepondues.clear();
			
			// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste 
			// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
			// traiter la r�ponse du joueur
			if (objQuestionTrouvee != null)
			{
				lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
				objQuestionCourante = objQuestionTrouvee;
				objPositionJoueurDesiree = nouvellePosition;
			}
			else
			{
				// en th�orie on ne devrait plus entrer dans ce else 
				System.out.println( "�a va mal : aucune question" );
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
	 * Methode used if player use the Cristal ball
     * int intDifficulte - level of the last question. The new question must be < difficult	
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						g�n�rer un num�ro de commande � retourner
	 * @return Question : La question trouv�e, s'il n'y a pas eu de d�placement,
	 * 					  alors la question retourn�e est null
	 */
	public Question trouverQuestionAPoser(int intDifficulte, boolean doitGenererNoCommandeRetour)
	{
		// D�clarations de variables qui vont contenir la cat�gorie de question 
		// � poser, la difficult� et la question � retourner
		//***************************************************************************************
		
		Categories[] catValues = Categories.values();
        int[] catScolaires = new int[catValues.length];
        //System.out.println("catValues.length : " + catValues.length);
        for(int i = 0; i < catValues.length; i++)
		{
			catScolaires[i] = catValues[i].getCode();
						
		}
		
		int intCategorieQuestion = catScolaires[UtilitaireNombres.genererNbAleatoire(catValues.length - 1)]; 
				
		Question objQuestionTrouvee = null;
		if (intDifficulte > 1)
			intDifficulte--;
		
		// Il faut que la difficult� soit plus grande que 0 pour pouvoir trouver 
		// une question
		if (intDifficulte > 0)
		{
			objQuestionTrouvee = trouverQuestion(intCategorieQuestion, intDifficulte, false);
		}
		
		// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste 
		// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
		// traiter la r�ponse du joueur, on va aussi garder la position que le
		// joueur veut se d�placer
		if (objQuestionTrouvee != null)
		{
			lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
			objQuestionCourante = objQuestionTrouvee;
			//objPositionJoueurDesiree = nouvellePosition;
		}
		else if (intDifficulte > 0)
		{
			objGestionnaireBD.remplirBoiteQuestions( getObjBoiteQuestions(), objJoueurHumain.obtenirCleNiveau());
			objQuestionTrouvee = trouverQuestion(intCategorieQuestion, intDifficulte, false);
			
			lstQuestionsRepondues.clear();
			
			// S'il y a eu une question trouv�e, alors on l'ajoute dans la liste 
			// des questions pos�es et on la garde en m�moire pour pouvoir ensuite
			// traiter la r�ponse du joueur
			if (objQuestionTrouvee != null)
			{
				lstQuestionsRepondues.put(new Integer(objQuestionTrouvee.obtenirCodeQuestion()), objQuestionTrouvee);
				objQuestionCourante = objQuestionTrouvee;
				//objPositionJoueurDesiree = nouvellePosition;
			}
			else
			{
				// en th�orie on ne devrait plus entrer dans ce else 
				System.out.println( "�a va mal : aucune question" );
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
	}// end methode
	
	
	/**
	 * Cette fonction essaie de piger une question du niveau de dificult� proche 
	 * de intDifficulte, si on y arrive pas, �a veut dire qu'il ne 
	 * reste plus de questions de niveau de difficult� proche 
	 * de intDifficulte
	 * 
	 * @param intCategorieQuestion
	 * @param intDifficulte
	 * @return la question trouver ou null si aucune question n'a pu �tre pig�e
	 */
	private Question trouverQuestion(int intCategorieQuestion, int intDifficulte, boolean moreDifficultQuestions)
	{
		
		Question objQuestionTrouvee = null;
		
		
		// pour le premier on voir la cat�gorie et difficult� demand�es
		objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion( intCategorieQuestion, intDifficulte);
		
		//on prend les cat�gories scolaires en utilisant enum Categories
		Categories[] catValues = Categories.values();
        int[] catScolaires = new int[catValues.length];
        for(int i = 0; i < catValues.length; i++)
		{
			catScolaires[i] = catValues[i].getCode();
		}
        
        LinkedList<Integer> catScolairesTemp = new LinkedList<Integer>();
        for(int numbers : catScolaires)
        	catScolairesTemp.add(numbers);
        int intRandom = 0;
        System.out.println("Avant diff : " + intDifficulte);
        
       	//sinon on cherche pour toutes les cat�gories de la m�me difficult� 
		int i = 0;
	    while(i < catScolaires.length && objQuestionTrouvee == null )
	    {
	       intRandom = UtilitaireNombres.genererNbAleatoire( catScolairesTemp.size() );	
	   	   intCategorieQuestion =  catScolairesTemp.get(intRandom).intValue();
	   	   objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion( intCategorieQuestion, intDifficulte);
	   	   catScolairesTemp.remove(intRandom); 
	   	   i++;
	   	      	  
	    }
	    
	    //apr�s pour les difficult�s moins grands 
		int intDifficulteTemp = intDifficulte;
		LinkedList<Integer> catScolairesTemp2 = new LinkedList<Integer>();
        
		while(objQuestionTrouvee == null && intDifficulteTemp > 0 ) 
		{
			for(int numbers : catScolaires)
	        	catScolairesTemp2.add(numbers);
			intDifficulteTemp--;
			i = 0;
		    while(i < catScolaires.length && objQuestionTrouvee == null )
		    {
		       intRandom = UtilitaireNombres.genererNbAleatoire( catScolairesTemp2.size() );	
			   intCategorieQuestion =  catScolairesTemp2.get(intRandom).intValue();
		   	   objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion( intCategorieQuestion, intDifficulteTemp);
		   	   catScolairesTemp2.remove(intRandom);
		   	   i++;
		    }
		}// fin while
		
		//apr�s pour les difficult�s plus grands
		intDifficulteTemp = intDifficulte;
		LinkedList<Integer> catScolairesTemp3 = new LinkedList<Integer>();
        
		while(objQuestionTrouvee == null && intDifficulteTemp < 7 && moreDifficultQuestions) 
		{
			for(int numbers : catScolaires)
	        	catScolairesTemp3.add(numbers);
			intDifficulteTemp++;
			i = 0;
		    while(i < catScolaires.length && objQuestionTrouvee == null )
		    {
		       intRandom = UtilitaireNombres.genererNbAleatoire( catScolairesTemp3.size() );	
			   intCategorieQuestion =  catScolairesTemp3.get(intRandom).intValue();
		   	   objQuestionTrouvee = getObjBoiteQuestions().pigerQuestion( intCategorieQuestion, intDifficulteTemp);
		   	   catScolairesTemp3.remove(intRandom);
		   	   i++;
		    }
		}// fin while
		
		
		return objQuestionTrouvee;
		
	}// fin m�thode
	
	/**
	 * Cette fonction met � jour le plateau de jeu si le joueur a bien r�pondu
	 * � la question. Les objets sur la nouvelle case sont enlev�s et le pointage et l'argent
	 * du joueur sont mis � jour. Utilis� par les joueurs humains et les joueurs virtuels
	 *
	 */
	public static RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse, 
	    Point objPositionDesiree, Joueur objJoueur)
    {
        
		// D�claration de l'objet de retour 
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour = null;
		
		int intPointageCourant; 
        int intArgentCourant;
		Table table;
		int intDifficulteQuestion;
		TreeMap<Integer, ObjetUtilisable> objListeObjetsUtilisablesRamasses; 
		Point positionJoueur; 
		GestionnaireEvenements gestionnaireEv;
		Question objQuestion; 
		String nomJoueur; 
		boolean bolReponseEstBonne;
		boolean boolWasOnFinish = false;
		
		// Obtenir les divers informations � utiliser d�pendamment de si
		// la fonction s'applique � un joueur humain ou un joueur virtuel
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
		    boolWasOnFinish = objPartieCourante.isPlayerNotArrivedOnce;
                    
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
		
		// D�claration d'une r�f�rence vers l'objet ramass�
		ObjetUtilisable objObjetRamasse = null;
		
		// D�claration d'une r�f�rence vers l'objet subi
		ObjetUtilisable objObjetSubi = null;
		
		String collision = "";
		
		// D�claration d'une r�f�rence vers le magasin recontr�
		Magasin objMagasinRencontre = null;
		
		// Si la r�ponse est bonne, alors on modifie le plateau de jeu
		if (bolReponseEstBonne == true)
		{
			// Faire la r�f�rence vers la case de destination
			Case objCaseDestination = table.obtenirPlateauJeuCourant()[objPositionDesiree.x][objPositionDesiree.y];
			
			// Calculer le nouveau pointage du joueur
                        switch(intDifficulteQuestion)
                        {
                            case 1:
                                intNouveauPointage += 1;
                                break;
                            case 2:
                                intNouveauPointage += 2;
                                break;
                            case 3:
                                intNouveauPointage += 3;
                                break;
                            case 4:
                                intNouveauPointage += 5;
                                break;
                            case 5:
                                intNouveauPointage += 8;
                                break;
                            case 6:
                                intNouveauPointage += 13;
                                break;
                        }
			
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

						if(maxNbObj > intNouvelArgent + objListeObjetsUtilisablesRamasses.size())   
						{
							// Faire la r�f�rence vers l'objet utilisable
							ObjetUtilisable objObjetUtilisable = (ObjetUtilisable) objCaseCouleurDestination.obtenirObjetCase();

							// Garder la r�f�rence vers l'objet utilisable pour l'ajouter � l'objet de retour
							objObjetRamasse = objObjetUtilisable;

							// Ajouter l'objet ramass� dans la liste des objets du joueur courant
							objListeObjetsUtilisablesRamasses.put(new Integer(objObjetUtilisable.obtenirId()), objObjetUtilisable);

							// Enlever l'objet de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							// On va dire aux clients qu'il y a eu collision avec cet objet
							collision = objObjetUtilisable.obtenirTypeObjet();
						}
					}
					else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Piece)
					{
						if(maxNbObj > intNouvelArgent + objListeObjetsUtilisablesRamasses.size()) 
						{
							// Faire la r�f�rence vers la pi�ce
							Piece objPiece = (Piece) objCaseCouleurDestination.obtenirObjetCase();

							// Mettre � jour l'argent du joueur
							intNouvelArgent += objPiece.obtenirMonnaie();

							// Enlever la pi�ce de la case du plateau de jeu
							objCaseCouleurDestination.definirObjetCase(null);

							collision = "piece";

							// TODO: Il faut peut-�tre lancer un algo qui va placer 
							// 		 les pi�ces sur le plateau de jeu s'il n'y en n'a
							//		 plus
						}
					}
					else if (objCaseCouleurDestination.obtenirObjetCase() instanceof Magasin)
					{
						// D�finir la collision
						collision = "magasin";
						
						// D�finir la r�f�rence vers le magasin rencontr�
						objMagasinRencontre = (Magasin) objCaseCouleurDestination.obtenirObjetCase();
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
				
				//***********************************
				//for gametype tourmnament - bonus for finish line
				 if(table.getObjSalle().getGameType().equals("Tournament"))
				 {
					 // On v�rifie d'abord si le joueur a atteint le WinTheGame;
					 boolean isWinTheGame = false;

					 int tracks = ((JoueurHumain)objJoueur).obtenirSalleCourante().getRegles().getNbTracks();
					 Point  objPoint = ((JoueurHumain)objJoueur).obtenirPartieCourante().obtenirTable().obtenirPositionWinTheGame();
					 Point objPointFinish = new Point();

					 for(int i = 0; i < tracks; i++ )
					 {
						 objPointFinish.setLocation(objPoint.x, objPoint.y - i);
						 if(objPositionDesiree.equals(objPointFinish))
							 isWinTheGame = true;
					 }

					// System.out.println(boolWasOnFinish);
					 
					 if(isWinTheGame && boolWasOnFinish )
					 {
						 intNouveauPointage += ((JoueurHumain)objJoueur).obtenirPartieCourante().obtenirTable().obtenirTempsRestant()/2;
						 System.out.println("bonus : " + ((JoueurHumain)objJoueur).obtenirPartieCourante().obtenirTable().obtenirTempsRestant());
						 ((JoueurHumain)objJoueur).obtenirPartieCourante().isPlayerNotArrivedOnce = false;
					 }
				 }
				//************************************  end bonus
			}
			
			// Cr�er l'objet de retour
			objRetour = new RetourVerifierReponseEtMettreAJourPlateauJeu(bolReponseEstBonne, intNouveauPointage, intNouvelArgent);
			objRetour.definirObjetRamasse(objObjetRamasse);
			objRetour.definirObjetSubi(objObjetSubi);
			objRetour.definirNouvellePosition(objPositionDesiree);
			objRetour.definirCollision( collision );
			objRetour.definirMagasin(objMagasinRencontre);
			
			synchronized (table.obtenirListeJoueurs())
		    {
				// Pr�parer l'�v�nement de deplacement de personnage. 
				// Cette fonction va passer les joueurs et cr�er un 
				// InformationDestination pour chacun et ajouter l'�v�nement 
				// dans la file de gestion d'�v�nements
				table.preparerEvenementJoueurDeplacePersonnage(nomJoueur, collision, positionJoueur, objPositionDesiree, intNouveauPointage, intNouvelArgent, "");
						    	
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
			// Cr�er l'objet de retour
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
	 * Cette fonction met � jour le plateau de jeu si le joueur a bien r�pondu
	 * � la question. Les objets sur la nouvelle case sont enlev�s et le pointage
	 * et l'argent du joueur sont mis � jour.
	 * 
	 * @param String reponse : La r�ponse du joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 						g�n�rer un num�ro de commande � retourner
	 * @return RetourVerifierReponseEtMettreAJourPlateauJeu : Un objet contenant 
	 * 				toutes les valeurs � retourner au client
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu verifierReponseEtMettreAJourPlateauJeu(String reponse, boolean doitGenererNoCommandeRetour)
	{
		
		RetourVerifierReponseEtMettreAJourPlateauJeu objRetour =
		    verifierReponseEtMettreAJourPlateauJeu(reponse, objPositionJoueurDesiree, objJoueurHumain);
		
		// Si on doit g�n�rer le num�ro de commande de retour, alors
		// on le g�n�re, sinon on ne fait rien (�a devrait toujours
		// �tre vrai, donc on le g�n�re tout le temps)
		if (doitGenererNoCommandeRetour == true)
		{
			// G�n�rer un nouveau num�ro de commande qui sera 
		    // retourn� au client
		    objJoueurHumain.obtenirProtocoleJoueur().genererNumeroReponse();					    
		}
		
		getObjBoiteQuestions().popQuestion(objQuestionCourante);
		objQuestionCourante = null;

		return objRetour;
	}
	
	/*
	 * Retourne une r�f�rence vers la liste des objets ramass�s
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
	 * Aller chercher une r�f�rence vers un objet de la liste des objets selon
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
	 * D�termine si le joueur poss�de un certain objet, permet
	 * de valider l'information envoy� par le client lorsqu'il utiliser l'objet
	 */
	 public boolean joueurPossedeObjet(int id)
	 {
	     // Pr�paration pour parcourir la liste d'objets
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
		// L'objet � retourn�
		Objet objObjet = null;
		
		// Aller chercher le plateau de jeu
		Case[][] objPlateauJeu = objTable.obtenirPlateauJeuCourant();
		
		// Aller chercher la case o� le joueur se trouve
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
        
        public String obtenirVaSubirUneBanane()
        {
            return vaSubirUneBanane;
        }
        
        public void definirVaSubirUneBanane(String b)
        {
            vaSubirUneBanane = b;
        }
        
        public int obtenirDistanceAuWinTheGame()
        {
            return Math.abs(objPositionJoueur.x - objTable.obtenirPositionWinTheGame().x) + Math.abs(objPositionJoueur.y - objTable.obtenirPositionWinTheGame().y);
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
}
