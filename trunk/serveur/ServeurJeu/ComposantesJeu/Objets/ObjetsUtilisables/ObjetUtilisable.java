package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import ServeurJeu.ComposantesJeu.Objets.Objet;

/**
 * @author Jean-François Brind'Amour
 */
public abstract class ObjetUtilisable extends Objet
{
	// Déclaration d'une variable qui va garder si oui ou non l'objet 
	// courant est visible sur la case ou non
	protected boolean bolEstVisible;
	
	// Déclaration d'une variable qui va garder le numéro d'identification 
	// de l'objet courant
	protected int intId;
	
	/**
	 * Constructeur de la classe ObjetUtilisable qui permet d'initialiser 
	 * les propriétés de l'objet courant.
	 * 
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être 
	 * 							   visible ou non
	 */
	public ObjetUtilisable(int id, boolean estVisible)
	{
		// Définir les propriétés de l'objet courant
		intId = id;
		bolEstVisible = estVisible;
	}
	
	/**
	 * Cette fonction permet de retourner le Id généré pour l'objet utilisable 
	 * courant.
	 * 
	 * @return int : Le Id de l'objet
	 */
	public int obtenirId()
	{
	   return intId;
	}
	
	/**
	 * Cette fonction permet de retourner si oui ou non l'objet est visible.
	 * 
	 * @return boolean : true si l'objet est visible
	 * 					 false sinon
	 */
	public boolean estVisible()
	{
	   return bolEstVisible;
	}
}