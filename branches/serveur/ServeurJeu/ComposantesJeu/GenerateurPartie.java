package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;

/**
 * @author Jean-François Brind'Amour
 * last changed 31.12.2009 Oloieri Lilian
 */
public abstract class GenerateurPartie 
{
	// Création d'un objet permettant de générer des nombres aléatoires
	protected Random objRandom;
    		
	// Déclaration de points
	protected Point objPoint;
	
	// Déclaration d'une liste de points contenant les points qui ont 
	// été passés
	protected ArrayList<Point> lstPointsCasesPresentes;

	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des cases spéciales
	protected ArrayList<Point> lstPointsCasesSpeciales;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des cases de couleur
	protected ArrayList<Point> lstPointsCasesCouleur;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des magasins
	protected ArrayList<Point> lstPointsMagasins;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des pièces
	protected ArrayList<Point> lstPointsPieces;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des objets utilisables
	protected ArrayList<Point> lstPointsObjetsUtilisables;
	
	// Déclaration d'une liste de points contenant les points de start
	//ArrayList<Point> lstPointsStart = new ArrayList<Point>();
	
	// Déclaration d'une liste de points contenant les points de finish
	//ArrayList<Point> lstPointsEnd = new ArrayList<Point>();
					
	// Déclaration d'un compteur de cases
	protected int intCompteurCases;

    // Déclaration d'un compteur des id des objets
	protected int intCompteurIdObjet;
    
	// Déclaration d'une case dont le type est -1 (ça n'existe pas) qui
	// va nous servir pour identifier les cases qui ont été passées
	protected CaseCouleur objCaseParcourue;
			
	// Nbs lines and columns in the table to be constracted   
	protected int intNbColumns;
	protected int intNbLines;
	
	protected Regles reglesPartie;
	
	// Déclaration d'une référence vers le gestionnaire de bases de données
	protected GestionnaireBD objGestionnaireBD;
	
	// Déclaration d'une référence vers la salle parente ou se trouve cet objet 
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
     * Cette fonction permet de retourner une matrice à deux dimensions
     * représentant le plateau de jeu qui contient les informations sur 
     * chaque case selon des paramètres.
     * @param lstPointsFinish 
     * @param Regles reglesPartie : L'ensemble des règles pour la partie
     * @param Vector listePointsCaseLibre : La liste des points des cases 
     * 										libres (paramètre de sortie)
     * @return Case[][] : Un tableau à deux dimensions contenant l'information
     * 					  sur chaque case.
     * @throws NullPointerException : Si la liste passée en paramètre qui doit 
     * 								  être remplie est nulle
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
     * Cette fonction permet de générer la position des joueurs. Chaque joueur 
     * est généré sur une case vide.
     * 
     * @param int nbJoueurs : Le nombre de joueurs dont générer la position
     * @param Vector listePointsCaseLibre : La liste des points des cases libres
     * @return Point[] : Un tableau de points pour chaque joueur 
     */
    protected abstract Point[] genererPositionJoueurs(Table table, int nbJoueurs, ArrayList<Point> lstPointsCaseLibre);
}