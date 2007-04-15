package ServeurJeu.ComposantesJeu.Objets.Pieces;

import ServeurJeu.ComposantesJeu.Objets.Objet;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Piece extends Objet
{
	// Cette variable va contenir la valeur de la pi�ce
	private int intValeur;
	
	/**
	 * Constructeur de la classe Piece qui permet d'initialiser
	 * la valeur de la pi�ce.
	 * 
	 * @param int valeur : La valeur de la pi�ce
	 */
	public Piece(int valeur)
	{
		// Initialiser la valeur de la pi�ce
		//TODO intValeur = valeur;
		intValeur = 10;
	}

	/**
	 * Cette fonction permet de retourner la valeur de la pi�ce.
	 * 
	 * @return int : La valeur de la pi�ce
	 */
	public int obtenirValeur()
	{
	   return intValeur;
	}
}