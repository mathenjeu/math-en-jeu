package ServeurJeu.ComposantesJeu.ReglesJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class ReglesCaseCouleur extends ReglesObjet
{
	// Déclaration d'une variable qui va contenir le type de case
	private int intTypeCase;
	
	/**
	 * Constructeur de la classe ReglesCaseCouleur qui permet d'initialiser
	 * la priorité et le type de case.
	 * 
	 * @param int typeCase : Le type de case 
	 */
	public ReglesCaseCouleur(int typeCase)
	{
		super();
		
		// Initialiser les propriétés de la règle de la case couleur
		intTypeCase = typeCase;
	}
	
	/**
	 * Constructeur de la classe ReglesCaseCouleur qui permet d'initialiser
	 * la priorité et le type de case.
	 * 
	 * @param int priorite : La priorité de cette règle
	 * @param int typeCase : Le type de case 
	 */
	public ReglesCaseCouleur(int priorite, int typeCase)
	{
		super(priorite);
		
		// Initialiser les propriétés de la règle de la case couleur
		intTypeCase = typeCase;		
	}

	/**
	 * Cette fonction permet de retourner le type de case courant.
	 * 
	 * @return int : Le type de case de la case couleur
	 */
	public int obtenirTypeCase()
	{
	   return intTypeCase;
	}
}