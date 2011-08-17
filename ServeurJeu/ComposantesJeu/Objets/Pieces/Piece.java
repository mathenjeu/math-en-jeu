package ServeurJeu.ComposantesJeu.Objets.Pieces;

import ServeurJeu.ComposantesJeu.Objets.Objet;
//import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;

/**
 * @author Jean-François Brind'Amour
 */

//NOTE: On a changé le concept: les pièces ne donnent plus de points,
//      mais plutôt de "l'argent", pour acheter des objets. La valeur
//      des pièces est donc 0.

public class Piece extends Objet
{
	// Cette variable va contenir la valeur en points de la pièce
	private final int intValeur;
        
    // Cette variable va contenir la valeur en dollars de la pièce
    private final int intMonnaie;
	
	/**
	 * Constructeur de la classe Piece qui permet d'initialiser
	 * les valeurs de la pièce.
	 * 
	 * @param int valeur : La valeur en points de la pièce
     * @param int monnaie : La valeur en dollars de la pièce
	 */
	public Piece(int valeur, int monnaie)
	{
		// Initialiser la valeur de la pièce
		intValeur = 0;
        intMonnaie = 1;
	}

	/**
	 * Cette fonction permet de retourner la valeur de la pièce.
	 * 
	 * @return int : La valeur de la pièce
	 */
	public int obtenirValeur()
	{
	   return intValeur;
	}
        
    public int obtenirMonnaie()
    {
        return intMonnaie;
    }
}