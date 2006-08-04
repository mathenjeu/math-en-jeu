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
	
	// Déclaration d'une variable qui contient un numéro id identifiant le
	// type d'objet
	protected int intUID;
	
	// Le prix de l'objet utilsable
	protected int intPrix;
	
	// Indique si l'objet peut être armé
	protected boolean bolPeutEtreArme;
	
	// Indique si l'objet se vend de façon illimité dans les magasins
	protected boolean bolEstLimite;
	
	// Le type de l'objet en chaîne de caractères, utilisé de façon
	// unique pour communiquer avec le client
	protected String strTypeObjet;
	
	/**
	 * Constructeur de la classe ObjetUtilisable qui permet d'initialiser 
	 * les propriétés de l'objet courant.
	 * 
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être 
	 * 							   visible ou non
	 */
	public ObjetUtilisable(int id, boolean estVisible, int intUniqueId,
	    int prix, boolean peutEtreArme, boolean estLimite, String typeObjet)
	{
		// Définir les propriétés de l'objet courant
		intId = id;
		bolEstVisible = estVisible;
		intUID = intUniqueId;
		intPrix = prix;
		bolPeutEtreArme = peutEtreArme;
		bolEstLimite = estLimite;
		strTypeObjet = typeObjet;
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
	
	public int obtenirUniqueId()
	{
		return intUID;
	}
	
	public int obtenirPrix()
	{
		return intPrix;
	}
	
	public boolean obtenirEstLimite()
	{
		return bolEstLimite;
	}
	
	public boolean obtenirPeutEtreArme()
	{
		return bolPeutEtreArme;
	}
	
	public String obtenirTypeObjet()
	{
		return strTypeObjet;
	}
}