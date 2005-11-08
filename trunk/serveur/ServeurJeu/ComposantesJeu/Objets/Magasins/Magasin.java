package ServeurJeu.ComposantesJeu.Objets.Magasins;

import java.util.Vector;
import ServeurJeu.ComposantesJeu.Objets.Objet;

/**
 * @author Jean-François Brind'Amour
 */
public abstract class Magasin extends Objet
{
	// Déclaration d'une liste d'objets utilisables qui va servir à savoir 
	// quels objets le magasin vend
	protected Vector lstObjetsUtilisables;
	
	/**
	 * Constructeur de la classe Magasin qui permet d'initialiser
	 * la liste des ObjetsUtilisables.
	 */
	protected Magasin()
	{
		// Créer une nouvelle liste d'objets utilisables
		lstObjetsUtilisables = new Vector();
	}

	/**
	 * Cette fonction permet de retourner la liste des objets utilisables 
	 * que le magasin vend.
	 * 
	 * @return Vector : La liste des ObjetsUtilisables que le magasin vend
	 */
	public Vector obtenirListeObjetsUtilisables()
	{
	   return lstObjetsUtilisables;
	}
}