package ServeurJeu.ComposantesJeu.Objets.Pieces;

import ServeurJeu.ComposantesJeu.Objets.Objet;

/**
 * @author Jean-François Brind'Amour
 */
public class Piece extends Objet
{
	// Cette variable va contenir la valeur de la pièce
	private int intValeur;
	
	/**
	 * Constructeur de la classe Piece qui permet d'initialiser
	 * la valeur de la pièce.
	 * 
	 * @param int valeur : La valeur de la pièce
	 */
	public Piece(int valeur)
	{
		// Initialiser la valeur de la pièce
		//TODO intValeur = valeur;
		intValeur = 10;
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
}