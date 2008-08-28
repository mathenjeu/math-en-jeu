package ServeurJeu.ComposantesJeu.ReglesJeu;

/**
 * @author Jean-François Brind'Amour
 */
public abstract class ReglesObjet
{
	// Cette variable va contenir le numéro de priorité
	protected int intPriorite;
	
	/**
	 * Constructeur de la classe ReglesObjet qui permet d'initialiser
	 * la priorité.
	 */
	protected ReglesObjet()
	{
		// Initialiser la priorité à -1 (celà signifie que la priorité 
		// importe peu
		intPriorite = -1;
	}
	
	/**
	 * Constructeur de la classe ReglesObjet qui permet d'initialiser
	 * la priorité.
	 */
	protected ReglesObjet(int priorite)
	{
		// Initialiser la priorité
		intPriorite = priorite;
	}

	/**
	 * Cette fonction permet de retourner la priorité de la règle.
	 * 
	 * @return int : La priorité de la règle
	 */
	public int obtenirPriorite()
	{
	   return intPriorite;
	}
}