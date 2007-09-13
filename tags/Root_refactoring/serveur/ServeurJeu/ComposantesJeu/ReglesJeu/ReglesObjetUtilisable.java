package ServeurJeu.ComposantesJeu.ReglesJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class ReglesObjetUtilisable extends ReglesObjet
{
	// Déclaration d'une variable qui va permettre de savoir si l'objet doit 
	// être toujours visible, jamais visible ou aléatoire
	private String strVisibilite;
	
	// Déclaration d'une variable qui va contenir le nom de l'objet utilisable
	private String strNomObjetUtilisable;
	
	/**
	 * Constructeur de la classe ReglesObjetUtilisable qui permet d'initialiser
	 * la visibilité et le nom de l'objet utilisable.
	 *
	 * @param String nom : Le nom de l'objet
	 * @param String visibilite : La visibilité de l'objet
	 */
	public ReglesObjetUtilisable(String nom, String visibilite)
	{
		super();
		
		// Initialiser la visibilité et le nom de l'objet
		strVisibilite = visibilite;
		strNomObjetUtilisable = nom;
	}
	
	/**
	 * Constructeur de la classe ReglesObjetUtilisable qui permet d'initialiser
	 * la priorité, la visibilité et le nom de l'objet.
	 * 
	 * @param int priorite : La priorité pour cette règle
	 * @param String nom : Le nom de l'objet
	 * @param String visibilite : La visibilité de l'objet
	 */
	public ReglesObjetUtilisable(int priorite, String nom, String visibilite)
	{
		super(priorite);
		
		// Initialiser la visibilité et le nom de l'objet
		strVisibilite = visibilite;
		strNomObjetUtilisable = nom;
	}
	
	/**
	 * Cette fonction permet de retourner la visibilité de l'objet.
	 * 
	 * @return String : La visibilité de l'objet
	 */
	public String obtenirVisibilite()
	{
	   return strVisibilite;
	}
	
	/**
	 * Cette fonction permet de retourner le nom de l'objet.
	 * 
	 * @return String : Le nom de l'objet utilisable
	 */
	public String obtenirNomObjetUtilisable()
	{
	   return strNomObjetUtilisable;
	}
}