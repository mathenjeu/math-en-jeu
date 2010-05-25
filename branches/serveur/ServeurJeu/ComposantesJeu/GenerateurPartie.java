package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;

/**
 * @author Jean-Fran�ois Brind'Amour
 * last changed 31.12.2009 Oloieri Lilian
 */
public abstract class GenerateurPartie 
{
	// Cr�ation d'un objet permettant de g�n�rer des nombres al�atoires
	protected Random objRandom;
    		
	// D�claration de points
	protected Point objPoint;
	
	// D�claration d'une liste de points contenant les points qui ont 
	// �t� pass�s
	protected ArrayList<Point> lstPointsCasesPresentes;

	// D�claration d'une liste de points contenant les points qui 
	// contiennent des cases sp�ciales
	protected ArrayList<Point> lstPointsCasesSpeciales;
	
	// D�claration d'une liste de points contenant les points qui 
	// contiennent des cases de couleur
	protected ArrayList<Point> lstPointsCasesCouleur;
	
	// D�claration d'une liste de points contenant les points qui 
	// contiennent des magasins
	protected ArrayList<Point> lstPointsMagasins;
	
	// D�claration d'une liste de points contenant les points qui 
	// contiennent des pi�ces
	protected ArrayList<Point> lstPointsPieces;
	
	// D�claration d'une liste de points contenant les points qui 
	// contiennent des objets utilisables
	protected ArrayList<Point> lstPointsObjetsUtilisables;
	
	// D�claration d'une liste de points contenant les points de start
	//ArrayList<Point> lstPointsStart = new ArrayList<Point>();
	
	// D�claration d'une liste de points contenant les points de finish
	//ArrayList<Point> lstPointsEnd = new ArrayList<Point>();
					
	// D�claration d'un compteur de cases
	protected int intCompteurCases;

    // D�claration d'un compteur des id des objets
	protected int intCompteurIdObjet;
    
	// D�claration d'une case dont le type est -1 (�a n'existe pas) qui
	// va nous servir pour identifier les cases qui ont �t� pass�es
	protected CaseCouleur objCaseParcourue;
			
	// Nbs lines and columns in the table to be constracted   
	protected int intNbColumns;
	protected int intNbLines;
	
	protected Regles reglesPartie;
	
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	protected GestionnaireBD objGestionnaireBD;
	
	// D�claration d'une r�f�rence vers la salle parente ou se trouve cet objet 
	protected Salle objSalle;
	
		
	
	// Constractor	
	protected GenerateurPartie() {
		super();
		this.objRandom = new Random();
		this.lstPointsCasesPresentes = new ArrayList<Point>();
		this.lstPointsCasesSpeciales = new ArrayList<Point>();
		this.lstPointsCasesCouleur = new ArrayList<Point>();
		this.lstPointsMagasins = new ArrayList<Point>();
		this.lstPointsPieces = new ArrayList<Point>();
		this.lstPointsObjetsUtilisables = new ArrayList<Point>();
		this.intCompteurCases = 0;
		this.intCompteurIdObjet = 1;
		this.objCaseParcourue = new CaseCouleur(1);
		this.reglesPartie = null;//salle.getRegles();
		this.objGestionnaireBD = null;//salle.getObjControleurJeu().obtenirGestionnaireBD();
		this.objSalle = null;//salle;
		this.intNbColumns = 0;
		this.intNbLines = 0;
		
	}

	/**
     * Cette fonction permet de retourner une matrice � deux dimensions
     * repr�sentant le plateau de jeu qui contient les informations sur 
     * chaque case selon des param�tres.
     * @param lstPointsFinish 
     * @param Regles reglesPartie : L'ensemble des r�gles pour la partie
     * @param Vector listePointsCaseLibre : La liste des points des cases 
     * 										libres (param�tre de sortie)
     * @return Case[][] : Un tableau � deux dimensions contenant l'information
     * 					  sur chaque case.
     * @throws NullPointerException : Si la liste pass�e en param�tre qui doit 
     * 								  �tre remplie est nulle
     */
    protected abstract Case[][] genererPlateauJeu(ArrayList<Point> lstPointsCaseLibre, Integer objDernierIdObjets, ArrayList<Point> lstPointsFinish, 
    		Table table) throws NullPointerException;


	/**
	 * Method for game board
	 * @param intNbCasesSpeciales
	 * @param objttPlateauJeu
	 */
	//protected abstract void caseDefinition(int intNbCasesSpeciales, Case[][] objttPlateauJeu);

    /**
     * Method used to create the game board 
     * @param intNbTrous 
     * @param objttPlateauJeu
     */
	//protected abstract void boardCreation(int intNbTrous, Case[][] objttPlateauJeu);

  
    
    /**
     * Cette fonction permet de g�n�rer la position des joueurs. Chaque joueur 
     * est g�n�r� sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont g�n�rer la position
     * @param Vector listePointsCaseLibre : La liste des points des cases libres
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    protected abstract Point[] genererPositionJoueurs(Table table, int nbJoueurs, ArrayList<Point> lstPointsCaseLibre);
}