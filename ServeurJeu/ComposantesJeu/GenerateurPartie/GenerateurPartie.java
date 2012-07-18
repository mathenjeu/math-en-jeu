package ServeurJeu.ComposantesJeu.GenerateurPartie;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import ServeurJeu.BD.GestionnaireBDControleur;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.ComposantesJeu.Cases.CaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.Tables.Table;

/**
 * @author Jean-François Brind'Amour
 * last changed 31.12.2009 Oloieri Lilian
 */
public abstract class GenerateurPartie 
{
	// Création d'un objet permettant de générer des nombres aléatoires
	protected final static Random objRandom = new Random();
    		
	// Déclaration de points
	protected Point objPoint;
	
	// Déclaration d'une liste de points contenant les points qui ont 
	// été passés
	protected LinkedList<Point> lstPointsCasesPresentes;

	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des cases spéciales
	protected LinkedList<Point> lstPointsCasesSpeciales;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des cases de couleur
	protected LinkedList<Point> lstPointsCasesCouleur;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des magasins
	protected LinkedList<Point> lstPointsMagasins;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des pièces
	protected LinkedList<Point> lstPointsPieces;
	
	// Déclaration d'une liste de points contenant les points qui 
	// contiennent des objets utilisables
	protected LinkedList<Point> lstPointsObjetsUtilisables;
		
	// Déclaration d'un compteur de cases
	protected int intCompteurCases;

    // Déclaration d'un compteur des id des objets
	protected int intCompteurIdObjet;
    
	// Déclaration d'une case dont le type est -1 (ça n'existe pas) qui
	// va nous servir pour identifier les cases qui ont été passées
	protected CaseCouleur objCaseParcourue;
			
	// Nbs lines and columns in the table to be build   
	protected int intNbColumns;
	protected int intNbLines;
	
	protected Regles reglesPartie;
	
	// Déclaration d'une référence vers le gestionnaire de bases de données
	protected GestionnaireBDControleur objGestionnaireBD;
	
	// Déclaration d'une référence vers la salle parente ou se trouve cet objet 
	protected Salle objSalle;
	
		
	
	// Constractor	
	protected GenerateurPartie() {
		super();
		//this.objRandom = 
		lstPointsCasesPresentes = new LinkedList<Point>();
		lstPointsCasesSpeciales = new LinkedList<Point>();
		lstPointsCasesCouleur = new LinkedList<Point>();
		lstPointsMagasins = new LinkedList<Point>();
		lstPointsPieces = new LinkedList<Point>();
		lstPointsObjetsUtilisables = new LinkedList<Point>();
		intCompteurCases = 0;
		intCompteurIdObjet = 1;
		objCaseParcourue = new CaseCouleur(1);
		reglesPartie = null;
		objGestionnaireBD = null;//salle.getObjControleurJeu().obtenirGestionnaireBD();
		objSalle = null;
		intNbColumns = 0;
		intNbLines = 0;
		
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
    public abstract Case[][] genererPlateauJeu(ArrayList<Point> lstPointsCaseLibre, ArrayList<Point> lstPointsFinish, 
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
    public abstract Point[] genererPositionJoueurs(Table table, int nbJoueurs, ArrayList<Point> lstPointsCaseLibre);
    
    /**
     * @return the nbLines
     */
    public int getNbLines() {
        return intNbLines;
    }

    /**
     * @param nbLines the nbLines to set
     */
    public void setNbLines(int nbLines) {
        this.intNbLines = nbLines;
    }
 

    /**
     * @return the nbColumns
     */
    public int getNbColumns() {
        return intNbColumns;
    }

    /**
     * @param nbColumns the nbColumns to set
     */
    public void setNbColumns(int nbColumns) {
        this.intNbColumns = nbColumns;
    }
}