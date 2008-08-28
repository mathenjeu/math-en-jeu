package ServeurJeu.ComposantesJeu.ReglesJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class ReglesCaseSpeciale extends ReglesObjet
{
	// Déclaration d'une variable qui va contenir le type de case
	private int intTypeCase;
	
	/**
	 * Constructeur de la classe ReglesCaseSpeciale qui permet d'initialiser
	 * la priorité et le type de case.
	 * 
	 * @param int typeCase : Le type de case 
	 */
	public ReglesCaseSpeciale(int typeCase)
	{
		super();
		
		// Initialiser les propriétés de la règle de la case spéciale
		intTypeCase = typeCase;
	}
	
	/**
	 * Constructeur de la classe ReglesCaseSpeciale qui permet d'initialiser
	 * la priorité et le type de case.
	 * 
	 * @param int priorite : La priorité de cette règle
	 * @param int typeCase : Le type de case 
	 */
	public ReglesCaseSpeciale(int priorite, int typeCase)
	{
		super(priorite);
		
		// Initialiser les propriétés de la règle de la case spéciale
		intTypeCase = typeCase;
	}

	/**
	 * Cette fonction permet de retourner le type de case courant.
	 * 
	 * @return int : Le type de case de la case spéciale
	 */
	public int obtenirTypeCase()
	{
	   return intTypeCase;
	}
}