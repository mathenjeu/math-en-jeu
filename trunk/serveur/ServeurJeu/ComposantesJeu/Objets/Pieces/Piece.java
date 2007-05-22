package ServeurJeu.ComposantesJeu.Objets.Pieces;

import ServeurJeu.ComposantesJeu.Objets.Objet;

/**
 * @author Jean-Fran�ois Brind'Amour
 */

//NOTE: On a chang� le concept: les pi�ces ne donnent plus de points,
//      mais plut�t de "l'argent", pour acheter des objets. La valeur
//      des pi�ces est donc 0, et l'argent est enti�rement g�r� par
//      le client.

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
		intValeur = 0;
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