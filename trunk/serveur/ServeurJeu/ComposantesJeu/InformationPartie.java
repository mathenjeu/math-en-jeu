package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.TreeMap;

/**
 * @author Jean-François Brind'Amour
 */
public class InformationPartie
{
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
	public InformationPartie(Table tableCourante)
	{
	    // Définir les propriétés de l'objet InformationPartie
	    intPointage = 0;
	    intIdPersonnage = 0;
	    
	    // Faire la référence vers la table courante
	    objTable = tableCourante;
	    
	    // Au départ, le joueur est nul part
	    objPositionJoueur = null;
	    
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
}