package ServeurJeu.ComposantesJeu.Cases;

import ServeurJeu.ComposantesJeu.Objets.Objet;
import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;

/**
 * @author Jean-François Brind'Amour
 */
public class CaseCouleur extends Case
{
	// Déclaration d'une variable qui va contenir l'objet qui peut se 
	// trouver sur la case (1 seul objet peut se trouver sur la case : 
	// soit une pièce, un objet utilisable ou un magasin)
	private Objet objObjetCase;
	
	// Déclaration d'une variable qui va contenir un objet armé qu'un 
	// autre joueur aura mis sur la case (il ne peut y en avoir qu'un)
	private ObjetUtilisable objObjetArme;
	
	/**
	 * Constructeur de la classe CaseCouleur.
	 * 
	 * @param int typeCase : Le type de la case à créer
	 */
	public CaseCouleur(int typeCase)
	{
		// Appeler le constructeur parent
		super(typeCase);
		
		// Au départ, il n'y a aucuns objets sur la case
		//objObjetCase = null;
		//objObjetArme = null;
	}
	
	/**
	 * Cette fonction retourne l'objet sur la case.
	 * 
	 * @return Objet : L'objet se trouvant sur la case
	 */
	public Objet obtenirObjetCase()
	{
		return objObjetCase;
	}
	
	/**
	 * Cette méthode définit l'objet devant se trouver sur la case.
	 * 
	 * @param Objet objetCase : L'objet qui va maintenant se trouver 
	 * 							sur la case
	 */
	public void definirObjetCase(Objet objetCase)
	{
		objObjetCase = objetCase;
	}
	
	/**
	 * Cette fonction retourne l'objet armé de la case.
	 * 
	 * @return ObjetUtilisable : L'objet armé se trouvant sur la case
	 */
	public ObjetUtilisable obtenirObjetArme()
	{
		return objObjetArme;
	}
	
	/**
	 * Cette méthode définit l'objet armé sur la case.
	 * 
	 * @param ObjetUtilisable objetArme : L'objet armé qui va maintenant se 
	 * 									  trouver sur la case
	 */
	public void definirObjetArme(ObjetUtilisable objetArme)
	{
		objObjetArme = objetArme;
	}
}
