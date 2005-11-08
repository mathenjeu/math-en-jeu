package ServeurJeu.ComposantesJeu;

import java.awt.Point;
import java.util.TreeMap;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class InformationPartie
{
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
	public InformationPartie(Table tableCourante)
	{
	    // D�finir les propri�t�s de l'objet InformationPartie
	    intPointage = 0;
	    intIdPersonnage = 0;
	    
	    // Faire la r�f�rence vers la table courante
	    objTable = tableCourante;
	    
	    // Au d�part, le joueur est nul part
	    objPositionJoueur = null;
	    
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
}