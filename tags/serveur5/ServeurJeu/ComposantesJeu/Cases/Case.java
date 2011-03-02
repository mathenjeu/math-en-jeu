package ServeurJeu.ComposantesJeu.Cases;

/**
 * @author Jean-François Brind'Amour
 */
public abstract class Case
{
	// Déclaration d'une variable qui va contenir le type de case
	protected final int intTypeCase;
	
	/**
	 * Constructeur de la classe abstraite Case.
	 * 
	 * @param int typeCase : Le type de la case à créer
	 */
	protected Case(int typeCase)
	{
		// Définir le type de la case
		intTypeCase = typeCase;
	}
	
	/**
	 * Cette fonction retourne le type de la case.
	 * 
	 * @return int : le type de la case
	 */
	public int obtenirTypeCase()
	{
		return intTypeCase;
	}
}
