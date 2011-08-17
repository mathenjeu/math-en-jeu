package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;


/**
 * @author Oloieri Lilian
 *
 */

public class Brainiac extends ObjetUtilisable {

	public static final int PRIX = 1;

	// Cette constante affirme que l'objet courant n'est pas limité 
	// lorsqu'on l'achète (c'est-à-dire qu'un magasin n'épuise jamais 
	// son stock de cet objet)
	public static final boolean EST_LIMITE = false;

	// Cette constante affirme que l'objet courant ne peut être armé 
	// et déposé sur une case pour qu'un autre joueur tombe dessus. Elle 
	// ne peut seulement être utilisée immédiatement par le joueur
	public static final boolean PEUT_ETRE_ARME = false;

	// Cette constante définit le nom de cet objet
	public static final String TYPE_OBJET = "Brainiac";
	
	//private static final long Seconds = 90;

	/**
	 * Constructeur de la classe Braniac qui permet de définir les propriétés 
	 * propres à l'objet courant.
	 *
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être visible ou non
	 */
	public Brainiac(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BRAINIAC, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}
	
}// end class
